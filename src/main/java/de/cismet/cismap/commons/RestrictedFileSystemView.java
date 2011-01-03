/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import java.io.File;
import java.io.IOException;

import java.text.MessageFormat;

import java.util.Vector;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

/**
 * Fallback implementation of a FileSystemView.
 *
 * <p>Intendend usage:<br>
 * If the standard JFileChooser cannot open due to an exception, as described in <a
 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857">
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857</a></p>
 *
 * <p>Example:</p>
 *
 * <pre>
     File currentDir = ...;
     JFrame parentFrame = ...;
     JFileChooser chooser;
     try {
         chooser = new JFileChooser(currentDir);
     } catch (Exception e) {
         chooser = new JFileChooser(currentDir, new RestrictedFileSystemView());
     }
     int returnValue = chooser.showOpenDialog(parentFrame);
 * </pre>
 *
 * <p>This FileSystemView only provides basic functionality (and probably a poor look & feel), but it can be a life
 * saver if otherwise no dialog pops up in your application.</p>
 *
 * <p>The implementation does <strong>not</strong> use <code>sun.awt.shell.*</code> classes.</p>
 *
 * @version  $Revision$, $Date$
 */
public class RestrictedFileSystemView extends FileSystemView {

    //~ Static fields/initializers ---------------------------------------------

    private static final String newFolderString = UIManager.getString("FileChooser.other.newFolder"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private File _defaultDirectory;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RestrictedFileSystemView object.
     */
    public RestrictedFileSystemView() {
        this(null);
    }

    /**
     * Creates a new RestrictedFileSystemView object.
     *
     * @param  defaultDirectory  DOCUMENT ME!
     */
    public RestrictedFileSystemView(final File defaultDirectory) {
        _defaultDirectory = defaultDirectory;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Determines if the given file is a root in the navigatable tree(s).
     *
     * @param   f  a <code>File</code> object representing a directory
     *
     * @return  <code>true</code> if <code>f</code> is a root in the navigatable tree.
     *
     * @see     #isFileSystemRoot
     */
    @Override
    public boolean isRoot(final File f) {
        if ((f == null) || !f.isAbsolute()) {
            return false;
        }

        final File[] roots = getRoots();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].equals(f)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the file (directory) can be visited. Returns false if the directory cannot be traversed.
     *
     * @param   f  the <code>File</code>
     *
     * @return  <code>true</code> if the file/directory can be traversed, otherwise <code>false</code>
     *
     * @see     JFileChooser#isTraversable
     * @see     FileView#isTraversable
     */
    @Override
    public Boolean isTraversable(final File f) {
        return Boolean.valueOf(f.isDirectory());
    }

    /**
     * Name of a file, directory, or folder as it would be displayed in a system file browser.
     *
     * @param   f  a <code>File</code> object
     *
     * @return  the file name as it would be displayed by a native file chooser
     *
     * @see     JFileChooser#getName
     */
    @Override
    public String getSystemDisplayName(final File f) {
        String name = null;
        if (f != null) {
            if (isRoot(f)) {
                name = f.getAbsolutePath();
            } else {
                name = f.getName();
            }
        }
        return name;
    }

    /**
     * Type description for a file, directory, or folder as it would be displayed in a system file browser.
     *
     * @param   f  a <code>File</code> object
     *
     * @return  the file type description as it would be displayed by a native file chooser or null if no native
     *          information is available.
     *
     * @see     JFileChooser#getTypeDescription
     */
    @Override
    public String getSystemTypeDescription(final File f) {
        return null;
    }

    /**
     * Icon for a file, directory, or folder as it would be displayed in a system file browser.
     *
     * @param   f  a <code>File</code> object
     *
     * @return  an icon as it would be displayed by a native file chooser, null if not available
     *
     * @see     JFileChooser#getIcon
     */
    @Override
    public Icon getSystemIcon(final File f) {
        if (f != null) {
            return UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon"); // NOI18N
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   folder  a <code>File</code> object repesenting a directory
     * @param   file    a <code>File</code> object
     *
     * @return  <code>true</code> if <code>folder</code> is a directory and contains <code>file</code>.
     */
    @Override
    public boolean isParent(final File folder, final File file) {
        if ((folder == null) || (file == null)) {
            return false;
        } else {
            return folder.equals(file.getParentFile());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parent    a <code>File</code> object repesenting a directory
     * @param   fileName  a name of a file or folder which exists in <code>parent</code>
     *
     * @return  a File object. This is normally constructed with <code>new File(parent, fileName)</code>.
     */
    @Override
    public File getChild(final File parent, final String fileName) {
        return createFileObject(parent, fileName);
    }

    /**
     * Checks if <code>f</code> represents a real directory or file as opposed to a special folder such as <code>
     * "Desktop"</code>. Used by UI classes to decide if a folder is selectable when doing directory choosing.
     *
     * @param   f  a <code>File</code> object
     *
     * @return  <code>true</code> if <code>f</code> is a real file or directory.
     */
    @Override
    public boolean isFileSystem(final File f) {
        return true;
    }

    /**
     * Returns whether a file is hidden or not.
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isHiddenFile(final File f) {
        return f.isHidden();
    }

    /**
     * Is dir the root of a tree in the file system, such as a drive or partition.
     *
     * @param   dir  f a <code>File</code> object representing a directory
     *
     * @return  <code>true</code> if <code>f</code> is a root of a filesystem
     *
     * @see     #isRoot
     */
    @Override
    public boolean isFileSystemRoot(final File dir) {
        return isRoot(dir);
    }

    /**
     * Used by UI classes to decide whether to display a special icon for drives or partitions, e.g. a "hard disk" icon.
     *
     * <p>The default implementation has no way of knowing, so always returns false.</p>
     *
     * @param   dir  a directory
     *
     * @return  <code>false</code> always
     */
    @Override
    public boolean isDrive(final File dir) {
        return false;
    }

    /**
     * Used by UI classes to decide whether to display a special icon for a floppy disk. Implies isDrive(dir).
     *
     * <p>The default implementation has no way of knowing, so always returns false.</p>
     *
     * @param   dir  a directory
     *
     * @return  <code>false</code> always
     */
    @Override
    public boolean isFloppyDrive(final File dir) {
        return false;
    }

    /**
     * Used by UI classes to decide whether to display a special icon for a computer node, e.g. "My Computer" or a
     * network server.
     *
     * <p>The default implementation has no way of knowing, so always returns false.</p>
     *
     * @param   dir  a directory
     *
     * @return  <code>false</code> always
     */
    @Override
    public boolean isComputerNode(final File dir) {
        return false;
    }

    /**
     * Returns all root partitions on this system. For example, on Windows, this would be the "Desktop" folder, while on
     * DOS this would be the A: through Z: drives.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public File[] getRoots() {
        return File.listRoots();
    }

    // Providing default implementations for the remaining methods
    // because most OS file systems will likely be able to use this
    // code. If a given OS can't, override these methods in its
    // implementation.
    @Override
    public File getHomeDirectory() {
        return createFileObject(System.getProperty("user.home")); // NOI18N
    }

    /**
     * Return the user's default starting directory for the file chooser.
     *
     * @return  a <code>File</code> object representing the default starting folder
     */
    @Override
    public File getDefaultDirectory() {
        if (_defaultDirectory == null) {
            try {
                final File tempFile = File.createTempFile("filesystemview", "restricted"); // NOI18N
                tempFile.deleteOnExit();
                _defaultDirectory = tempFile.getParentFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return _defaultDirectory;
    }

    /**
     * Returns a File object constructed in dir from the given filename.
     *
     * @param   dir       DOCUMENT ME!
     * @param   filename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public File createFileObject(final File dir, final String filename) {
        if (dir == null) {
            return new File(filename);
        } else {
            return new File(dir, filename);
        }
    }

    /**
     * Returns a File object constructed from the given path string.
     *
     * @param   path  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public File createFileObject(final String path) {
        File f = new File(path);
        if (isFileSystemRoot(f)) {
            f = createFileSystemRoot(f);
        }
        return f;
    }

    /**
     * Gets the list of shown (i.e. not hidden) files.
     *
     * @param   dir            DOCUMENT ME!
     * @param   useFileHiding  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public File[] getFiles(final File dir, final boolean useFileHiding) {
        final Vector files = new Vector();

        // add all files in dir
        final File[] names;
        names = dir.listFiles();
        File f;

        final int nameCount = (names == null) ? 0 : names.length;
        for (int i = 0; i < nameCount; i++) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            f = names[i];
            if (!useFileHiding || !isHiddenFile(f)) {
                files.addElement(f);
            }
        }

        return (File[])files.toArray(new File[files.size()]);
    }

    /**
     * Returns the parent directory of <code>dir</code>.
     *
     * @param   dir  the <code>File</code> being queried
     *
     * @return  the parent directory of <code>dir</code>, or <code>null</code> if <code>dir</code> is <code>null</code>
     */
    @Override
    public File getParentDirectory(final File dir) {
        if ((dir != null) && dir.exists()) {
            final File psf = dir.getParentFile();
            if (psf != null) {
                if (isFileSystem(psf)) {
                    File f = psf;
                    if ((f != null) && !f.exists()) {
                        // This could be a node under "Network Neighborhood".
                        final File ppsf = psf.getParentFile();
                        if ((ppsf == null) || !isFileSystem(ppsf)) {
                            // We're mostly after the exists() override for windows below.
                            f = createFileSystemRoot(f);
                        }
                    }
                    return f;
                } else {
                    return psf;
                }
            }
        }
        return null;
    }

    /**
     * Creates a new <code>File</code> object for <code>f</code> with correct behavior for a file system root directory.
     *
     * @param   f  a <code>File</code> object representing a file system root directory, for example "/" on Unix or
     *             "C:\" on Windows.
     *
     * @return  a new <code>File</code> object
     */
    @Override
    protected File createFileSystemRoot(final File f) {
        return new FileSystemRoot(f);
    }

    @Override
    public File createNewFolder(final File containingDir) throws IOException {
        if (containingDir == null) {
            throw new IOException("Containing directory is null:"); // NOI18N
        }
        File newFolder = null;
        newFolder = createFileObject(containingDir, newFolderString);
        int i = 2;
        while (newFolder.exists() && (i < 100)) {
            newFolder = createFileObject(
                    containingDir,
                    MessageFormat.format(newFolderString,
                        new Object[] { new Integer(i) }));
            i++;
        }

        if (newFolder.exists()) {
            throw new IOException("Directory already exists:" + newFolder.getAbsolutePath()); // NOI18N
        } else {
            newFolder.mkdirs();
        }

        return newFolder;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class FileSystemRoot extends File {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FileSystemRoot object.
         *
         * @param  f  DOCUMENT ME!
         */
        public FileSystemRoot(final File f) {
            super(f, ""); // NOI18N
        }

        /**
         * Creates a new FileSystemRoot object.
         *
         * @param  s  DOCUMENT ME!
         */
        public FileSystemRoot(final String s) {
            super(s);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public String getName() {
            return getPath();
        }
    }
}
