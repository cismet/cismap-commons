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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;

import java.net.URL;

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
public class Download extends Observable implements Runnable, Comparable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(Download.class);
    private static final int MAX_BUFFER_SIZE = 1024;

    public static final int DOWNLOADING = 0;
    public static final int COMPLETE = 1;
    public static final int CANCELLED = 2;
    public static final int ERROR = 3;

    //~ Instance fields --------------------------------------------------------

    private URL url;
    private String request;
    private File fileToSaveTo;
    private int status;
    private String topic;
    private Thread downloadThread;
    private Exception exceptionCatchedWhileDownloading;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for Download.
     *
     * @param  url           DOCUMENT ME!
     * @param  request       DOCUMENT ME!
     * @param  topic         saveDir DOCUMENT ME!
     * @param  fileToSaveTo  DOCUMENT ME!
     */
    public Download(final URL url, final String request, final String topic, final File fileToSaveTo) {
        this.url = url;
        this.request = request;
        this.topic = topic;
        this.fileToSaveTo = fileToSaveTo;

        status = DOWNLOADING;
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
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTopic() {
        return topic;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public File getFileToSaveTo() {
        return fileToSaveTo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fileToSaveTo  DOCUMENT ME!
     */
    public void setFileToSaveTo(final File fileToSaveTo) {
        this.fileToSaveTo = fileToSaveTo;
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
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getStatus() {
        return status;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Exception getExceptionCatchedWhileDownloading() {
        return exceptionCatchedWhileDownloading;
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
     *
     * @param  exception  DOCUMENT ME!
     */
    private void error(final Exception exception) {
        LOG.error("Exception occurred while downloading '" + fileToSaveTo + "'.", exception);
        exceptionCatchedWhileDownloading = exception;
        status = ERROR;
        stateChanged();
    }

    /**
     * DOCUMENT ME!
     */
    public void startDownload() {
        if (downloadThread == null) {
            downloadThread = new Thread(this);
            downloadThread.start();
        }
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
        FileOutputStream out = null;
        InputStream resp = null;

        try {
            resp = WebAccessManager.getInstance()
                        .doRequest(
                                url,
                                new StringReader(request),
                                ACCESS_METHODS.POST_REQUEST);

            status = DOWNLOADING;
            out = new FileOutputStream(fileToSaveTo);
            while (status == DOWNLOADING) {
                // Size buffer according to how much of the file is left to download.
                final byte[] buffer;
                buffer = new byte[MAX_BUFFER_SIZE];

                // Read from server into buffer.
                final int read = resp.read(buffer);
                if (read == -1) {
                    break;
                }

                // Write buffer to file.
                out.write(buffer, 0, read);
                stateChanged();
            }

            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
            }
        } catch (MissingArgumentException ex) {
            error(ex);
        } catch (AccessMethodIsNotSupportedException ex) {
            error(ex);
        } catch (RequestFailedException ex) {
            error(ex);
        } catch (NoHandlerForURLException ex) {
            error(ex);
        } catch (Exception ex) {
            error(ex);
        } finally {
            // Close file.
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    LOG.warn("Exception occured while closing file.", e);
                }
            }

            // Close connection to server.
            if (resp != null) {
                try {
                    resp.close();
                } catch (Exception e) {
                    LOG.warn("Exception occured while closing response stream.", e);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Download)) {
            return false;
        }

        final Download other = (Download)obj;

        boolean result = true;

        if ((this.url != other.url) && ((this.url == null) || !this.url.equals(other.url))) {
            result &= false;
        }
        if ((this.request == null) ? (other.request != null) : (!this.request.equals(other.request))) {
            result &= false;
        }
        if ((this.fileToSaveTo == null) ? (other.fileToSaveTo != null)
                                        : (!this.fileToSaveTo.equals(other.fileToSaveTo))) {
            result &= false;
        }

        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = (43 * hash) + ((this.url != null) ? this.url.hashCode() : 0);
        hash = (43 * hash) + ((this.request != null) ? this.request.hashCode() : 0);
        hash = (43 * hash) + ((this.fileToSaveTo != null) ? this.fileToSaveTo.hashCode() : 0);

        return hash;
    }

    @Override
    public int compareTo(final Object o) {
        if (!(o instanceof Download)) {
            return 1;
        }

        final Download other = (Download)o;
        return this.topic.compareTo(other.topic);
    }
}
