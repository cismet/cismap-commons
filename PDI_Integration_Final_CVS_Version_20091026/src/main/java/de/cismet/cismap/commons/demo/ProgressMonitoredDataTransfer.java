package de.cismet.cismap.commons.demo;
import de.cismet.tools.CismetThreadPool;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 * Transfer data from one stream to another,
 * providing a Swing ProgressMonitor to handle
 * notifying the user and allowing the user to
 * cancel the transfer.  The progress updates
 * every half-second.
 * <p>Objects of this class
 * also act as Observables, updating their 
 * observers after each block of data is
 * transfered, and again at the end of the
 * transfer.
 */
public class ProgressMonitoredDataTransfer
    extends java.util.Observable
    implements Runnable, ActionListener 
{ 
    private InputStream readFrom; 
    private OutputStream writeTo; 
    private ProgressMonitor monitor; 
    private boolean done;
    private int max;
    private int current;
    private Timer mytimer;
    private Thread mythread;
    private boolean closeWhenDone;

    public static final int DELAY = 1;
    public static final int BUFSIZ = 1024;
    /**
     * Create a progress monitored data transfer.
     *
     * @param par Parent Swing Window or Frame
     * @param from Stream to read data from
     * @param to Stream to write data out to
     * @param maxtransfer Maximum number of bytes we expect to transfer
     * @param message  Progress monitor message object (usually String)
     * @param close  If set, then close the streams at end of transfer
     */
    public ProgressMonitoredDataTransfer(Component par, 
                                         InputStream from, OutputStream to,
                                         int maxtransfer, Object message,
                                         boolean close)
    {
        readFrom = new BufferedInputStream(from);
        writeTo = to;
        max = maxtransfer;
        monitor = new ProgressMonitor(par, message, null, 0, max);
        monitor.setMillisToPopup(2*DELAY);
        mythread = new Thread(this);
        mytimer = new Timer(DELAY, this);
        done = false;
        closeWhenDone = close;
        current = 0;
        CismetThreadPool.execute(mythread);
        mytimer.start();
    }

    /**
     * Handle timer actions.
     */
    public void actionPerformed(ActionEvent e) {
        if (isDone()) {
            // if the transfer is done, close the monitor
            monitor.close();
            mytimer.stop();
        }
        else if (monitor.isCanceled()) {
            // if the user hits cancel, then interrupt our thread
            if (mythread != null && mythread.isAlive()) 
                mythread.interrupt();
            monitor.close();
            mytimer.stop();
        }
        else {
            // otherwise, just
            // retrieve the current progress value and...
            int cur = getCurrent();
            // ...then update the monitor!
            monitor.setProgress(cur);
        }
    }

    /**
     * Override this to handle problems differently.  Note that
     * this method may or may not be called from the
     * Swing event thread.  The default implementation
     * just prints to System.err.
     *
     * @param msg Message about the problem
     */
    public void handleProblem(String msg) {
        System.err.println("Transfer problem: " + msg);
    }

    /**
     * Retrieve the value of current, which is the
     * number of bytes transferred so far.
     * 
     * @returns number of bytes transfered
     */
    public synchronized int getCurrent() { return current; }
    private synchronized void addToCurrent(int x) { current += x; }

    /**
     * Check whether the task is done yet.
     */
    public synchronized boolean isDone() { return done; }
    private synchronized void setDone() { 
        done = true; 
        setChanged();
        notifyObservers("Done");
    }

    /**
     * Wait for the task to be done.
     * It is probably not a good idea to use this in Swing.
     */
    public void waitForDone() {
        try { mythread.join(); } catch (InterruptedException ie) { }
        return;
    }

    /**
     * Dont override this.
     */
    public void run() {
        try {
            byte buf[] = new byte[BUFSIZ];
            int cc;
            for(cc = readFrom.read(buf, 0, BUFSIZ);
                cc > 0;
                cc = readFrom.read(buf, 0, BUFSIZ)) 
            {
                writeTo.write(buf, 0, cc);
                addToCurrent(cc);  // update the current progress value
                // Thread.sleep(200);
                if (mythread.isInterrupted()) break;
                setChanged();
                notifyObservers(null);
            }
        }
        catch (InterruptedIOException ioe) {
            handleProblem("Cancelled by user after " + 
                          current + " bytes.");
        }
        catch (Exception ie) {
            handleProblem("IO exception: " + ie);
        }
        finally {
            setDone();
            if (closeWhenDone) {
                if (readFrom != null) {
                    try { readFrom.close(); } 
                    catch (Exception e2) { }
                }
                if (writeTo != null) {
                    try { writeTo.close(); } 
                    catch (Exception e3) { }
                }
            }
        }
    }
}