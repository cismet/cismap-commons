/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.util;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import java.util.HashMap;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FilePersistenceManager {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FilePersistenceManager.class);

    //~ Instance fields --------------------------------------------------------

    private Map<Long, Integer> idLengthMap = new HashMap<Long, Integer>();
    private RandomAccessFile raFile;
    private File file;
    private long fileIndex = 0;
    private long lastId = -1;
    private Object lastObject = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FilePersistenceManager object.
     *
     * @param  folder  DOCUMENT ME!
     */
    public FilePersistenceManager(final File folder) {
        final String fileNameBase = "tmp";
        int index = 0;

        do {
            file = new File(folder, fileNameBase + (++index));
        } while (file.exists());

        try {
            raFile = new RandomAccessFile(file, "rw");
        } catch (Exception e) {
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long save(final Object o) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ObjectOutputStream oout = new ObjectOutputStream(bos);
            oout.writeObject(o);
            oout.close();

            final byte[] b = bos.toByteArray();
            final long position = fileIndex;
            raFile.seek(fileIndex);
            raFile.write(b);
            fileIndex += b.length;
            idLengthMap.put(position, b.length);
            return position;
        } catch (Exception e) {
            LOG.error("Error while saving object.", e);
            return -1;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object load(final long id) {
        try {
            if (id == lastId) {
                return lastObject;
            }
            final int length = idLengthMap.get(id);
            final byte[] b = new byte[length];
            raFile.seek(id);
            raFile.read(b, 0, length);
            final ByteArrayInputStream bis = new ByteArrayInputStream(b);
            final ObjectInputStream oin = new ObjectInputStream(bis);
            final Object o = oin.readObject();
            oin.close();

            lastId = id;
            lastObject = o;

            return o;
        } catch (Exception e) {
            LOG.error("Error while loading object.", e);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void close() {
        try {
            idLengthMap = new HashMap<Long, Integer>();
            raFile.close();
            file.delete();
        } catch (Exception e) {
            LOG.error("Error while closing FilePersistenceManager.", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
