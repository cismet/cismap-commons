/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 jweintraut
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.commons.gui.shapeexport;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;

import java.net.URL;

import java.util.HashMap;
import java.util.Observable;

import de.cismet.security.AccessHandler.ACCESS_METHODS;

import de.cismet.security.WebAccessManager;

import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
import de.cismet.security.exceptions.MissingArgumentException;
import de.cismet.security.exceptions.NoHandlerForURLException;
import de.cismet.security.exceptions.RequestFailedException;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class Download extends Observable implements Runnable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(Download.class);
    private static final int MAX_BUFFER_SIZE = 1024;
    public static final String[] STATUSES = {
            "Downloading", "Paused", "Complete", "Cancelled",
            "Error"
        };
    // TODO: The strings in STATUSES are read in DownloadsTableModel using following constants as array indices.
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;

    //~ Instance fields --------------------------------------------------------

    /*public static final boolean proxyRequired = true;
     * public static final String proxyIP = "127.0.0.1"; public static final String proxyPort = "8080"; public static
     * final String proxyUsername = "proxyUser";public static final String proxyPassword = "proxyPassword";*/
    private URL url;
    private String request;
    private String saveDir;
    private int currentSize;
    private int downloaded;
    private int status;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for Download.
     *
     * @param  url      DOCUMENT ME!
     * @param  request  DOCUMENT ME!
     * @param  saveDir  DOCUMENT ME!
     */
    public Download(final URL url, final String request, final String saveDir) {
        this.url = url;
        this.request = request;
        this.saveDir = saveDir;

        currentSize = -1;
        downloaded = 0;
        status = DOWNLOADING;

        download();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Get this download's URL.
     *
     * @return  DOCUMENT ME!
     */
    public String getUrl() {
        return url.toString();
    }

    /**
     * Get this download's request.
     *
     * @return  DOCUMENT ME!
     */
    public String getRequest() {
        return request;
    }

    /**
     * Get this download's size.
     *
     * @return  DOCUMENT ME!
     */
    public int getSize() {
        return currentSize;
    }

    /**
     * Get this download's progress.
     *
     * @return  DOCUMENT ME!
     */
    public float getProgress() {
        return ((float)downloaded / currentSize) * 100;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getDownloaded() {
        return downloaded;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getStatus() {
        return status;
    }

    /**
     * DOCUMENT ME!
     */
    public void pause() {
        status = PAUSED;
        stateChanged();
    }

    /**
     * DOCUMENT ME!
     */
    public void resume() {
        status = DOWNLOADING;
        stateChanged();
        download();
    }

    /**
     * DOCUMENT ME!
     */
    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    /**
     * DOCUMENT ME!
     */
    private void error() {
        status = ERROR;
        stateChanged();
    }

    /**
     * DOCUMENT ME!
     */
    private void download() {
        final Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Get file name portion of URL.
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFileName(final URL url) {
        final String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1);
    }

    @Override
    public void run() {
        RandomAccessFile file = null;
        FileOutputStream out = null;
        InputStream resp = null;
        final HashMap<String, String> requestOptions = new HashMap<String, String>();
        // int availableBytes = 0;

        requestOptions.put("Range", "bytes=" + downloaded + "-");
        try {
            resp = WebAccessManager.getInstance()
                        .doRequest(
                                url,
                                new StringReader(request),
                                ACCESS_METHODS.POST_REQUEST,
                                requestOptions);

            // Check for valid content length.
            /*availableBytes = resp.available();
             * if (availableBytes < 1) { error();}*/

            // Set the size for this download if it hasn't been already set.
            /*if (currentSize == -1) {
             *  currentSize = availableBytes; stateChanged();}*/

            // Open file and seek to the end of it.
            file = new RandomAccessFile(getFileName(url), "rw");
            file.seek(downloaded);

            status = DOWNLOADING;
            out = new FileOutputStream(saveDir + File.separator + this.getFileName(url));
            while (status == DOWNLOADING) {
                // Size buffer according to how much of the file is left to download.
                final byte[] buffer;
                // if ((currentSize - downloaded) > MAX_BUFFER_SIZE) {
                buffer = new byte[MAX_BUFFER_SIZE];
                // } else {
                // buffer = new byte[currentSize - downloaded];
                // }

                // Read from server into buffer.
                final int read = resp.read(buffer);
                if (read == -1) {
                    break;
                }

                // Write buffer to file.
                // file.write(buffer, 0, read);
                out.write(buffer, 0, read);
                downloaded += read;
                stateChanged();
            }

            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
            }
        } catch (MissingArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (AccessMethodIsNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (RequestFailedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoHandlerForURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            // Close file.
            if (file != null) {
                try {
                    out.close();
                    file.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Close connection to server.
            if (resp != null) {
                try {
                    resp.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /*final char[] buffer = new char[256];
         * final StringBuilder response = new StringBuilder(); final BufferedReader br = new BufferedReader(new
         * InputStreamReader(resp)); int count = br.read(buffer, 0, buffer.length);
         *
         * while (count != -1) { response.append(buffer, 0, count); count = br.read(buffer, 0, buffer.length); }
         * br.close();
         *
         *return response.toString();*/

        /*try {
         * if (proxyRequired) { final Properties systemSettings = System.getProperties();
         * systemSettings.put("http.proxyHost", proxyIP); systemSettings.put("http.proxyPort", proxyPort);
         * System.setProperties(systemSettings); }
         *
         * // Open connection to URL. final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         *
         * // Specify what portion of file to download. connection.setRequestProperty("Range", "bytes=" + downloaded +
         * "-");
         *
         * if (proxyRequired) { final String encoded = new String(new sun.misc.BASE64Encoder().encode( new
         * String(proxyUsername + ":" + proxyPassword).getBytes()));
         * connection.setRequestProperty("Proxy-Authorization", "Basic " + encoded); }
         *
         * // Connect to server. connection.connect();
         *
         * final int responseCode = connection.getResponseCode();
         *
         * // Make sure response code is in the 200 range. // 200 - no partial download // 206 - supports resume // TODO:
         * Support full download and resuming if ((responseCode == 200) || (responseCode == 206)) { error(); }
         *
         * // Check for valid content length. final int contentLength = connection.getContentLength(); if (contentLength <
         * 1) { error(); }
         *
         * // Set the size for this download if it hasn't been already set. if (size == -1) { size = contentLength;
         * stateChanged(); }
         *
         * // Open file and seek to the end of it. file = new RandomAccessFile(getFileName(url), "rw");
         * file.seek(downloaded);
         *
         * stream = connection.getInputStream(); status = DOWNLOADING; out = new FileOutputStream(saveDir + File.separator
         * + this.getFileName(url)); while (status == DOWNLOADING) { // Size buffer according to how much of the file is
         * left to download. byte[] buffer; if ((size - downloaded) > MAX_BUFFER_SIZE) { buffer = new
         * byte[MAX_BUFFER_SIZE]; } else { buffer = new byte[size - downloaded]; }
         *
         * // Read from server into buffer. final int read = stream.read(buffer); if (read == -1) { break; }
         *
         * // Write buffer to file. // file.write(buffer, 0, read); out.write(buffer, 0, read); downloaded += read;
         * stateChanged();}*/

        /*
         * Change status to complete if this point was reached because downloading has finished.
         */
        /*if (status == DOWNLOADING) {
         * status = COMPLETE;
         *
         * stateChanged(); } } catch (Exception e) { e.printStackTrace(); error(); } finally { // Close file. if (file !=
         * null) { try { out.close(); file.close(); } catch (Exception e) { e.printStackTrace(); } }
         *
         * // Close connection to server. if (stream != null) { try { stream.close(); } catch (Exception e) {
         * e.printStackTrace(); } }}*/
    }

    /**
     * DOCUMENT ME!
     */
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }
}
