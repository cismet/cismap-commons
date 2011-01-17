/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.demo;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Test extends JFrame {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Test object.
     */
    public Test() {
        setContentPane(new Desktop());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(Test.class, "Test.title")); // NOI18N
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  parameters  DOCUMENT ME!
     */
    public static void main(final String[] parameters) {
        new Test();
        final String s =
            "SRID=31466;POLYGON((2583920 5677047,2583920 5677547,2584420 5677547,2584420 5677047,2583920 5677047))"; // NOI18N

        System.out.println("s:" + s.replaceAll("SRID=31466", "SRID=-1")); // NOI18N
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * the desktop; note that JLayeredPane is very picky about the order in which components are being added.
     *
     * @version  $Revision$, $Date$
     */
    private class Desktop extends JDesktopPane {

        //~ Instance fields ----------------------------------------------------

        private Wall wall;
        private TestPanel testPanel;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Desktop object.
         */
        public Desktop() {
            // we need a null layout for the dialog: a border layout would work
            // for the test panel and the invisible wall, but then the dialog
            // would take up all available space too (and that's undesired
            // behaviour [read: if that was what you wanted, you wouldn't
            // need a desktop pane at all])

            setLayout(null);

            wall = new Wall();
            wall.setVisible(false);
            testPanel = new TestPanel(this);

            add(wall, -1);
            add(testPanel, -2);

            // makes sure that the panels are being resized if the frame is
            // (since we use a null layout manager) - the first time it will run
            // is when the frame is being packed

            addComponentListener(new ComponentAdapter() {

                    @Override
                    public void componentResized(final ComponentEvent event) {
                        resizePanels();
                    }
                });
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void resizePanels() {
            // if you had more than two layers you'd do something a bit more
            // elegant here, but for this example it doesn't really matter

            wall.setBounds(0, 0, getWidth(), getHeight());
            testPanel.setBounds(0, 0, getWidth(), getHeight());
        }

        /**
         * DOCUMENT ME!
         *
         * @param  message  DOCUMENT ME!
         */
        public void openModalDialog(final String message) {
            wall.setVisible(true);

            final JInternalFrame dialog = new TestFrame(message);
            dialog.addInternalFrameListener(new InternalFrameAdapter() {

                    @Override
                    public void internalFrameClosing(final InternalFrameEvent event) {
                        wall.setVisible(false);
                    }
                });

            add(dialog);
            dialog.setLocation((getWidth() / 2) - (dialog.getWidth() / 2),
                (getHeight() / 2)
                        - (dialog.getHeight() / 2));
            dialog.setVisible(true);
        }
    }

    /**
     * this is your panel (can you tell which part of the code is not really necessary?).
     *
     * @version  $Revision$, $Date$
     */
    private class TestPanel extends JPanel {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TestPanel object.
         *
         * @param  desktop  DOCUMENT ME!
         */
        public TestPanel(final Desktop desktop) {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(new JButton(
                    new AbstractAction(
                        org.openide.util.NbBundle.getMessage(Test.class, "Test.TestPanel.button.text")) {             // NOI18N
                        @Override
                        public void actionPerformed(final ActionEvent event) {
                            desktop.openModalDialog(
                                org.openide.util.NbBundle.getMessage(Test.class, "Test.TestPanel.modalDialog.text")); // NOI18N
                        }
                    }));
        }

//        public void paintComponent (Graphics g) {
//            super.paintComponent (g);
//
//            Graphics2D g2d = (Graphics2D) g;
//
//            g2d.setPaint (new GradientPaint (0, 0, Color.BLUE,
//                getWidth (), getHeight (), Color.RED));
//            g2d.fillRect (0, 0, getWidth (), getHeight ());
//        }
    }

    /**
     * the invisible wall, that blocks all input on panels below it (here, the test panel).
     *
     * @version  $Revision$, $Date$
     */
    private class Wall extends JPanel {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Wall object.
         */
        public Wall() {
            // the invisible wall should be invisible
            setOpaque(false);
            // not all of these might be needed for your panel - for this
            // example, just the mouse event mask should suffice
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
            enableEvents(AWTEvent.KEY_EVENT_MASK);
        }
    }

    /**
     * a very simple internal frame that functions as a dialog.
     *
     * @version  $Revision$, $Date$
     */
    private class TestFrame extends JInternalFrame {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TestFrame object.
         *
         * @param  message  DOCUMENT ME!
         */
        public TestFrame(final String message) {
            putClientProperty("JInternalFrame.isPalette", Boolean.TRUE); // NOI18N
//            setIconifiable (false);
//            setMaximizable (false);
//            setResizable (false);
            setClosable(true);

            setDefaultCloseOperation(HIDE_ON_CLOSE);
            setTitle(org.openide.util.NbBundle.getMessage(Test.class, "Test.TestFrame.title")); // NOI18N

            getContentPane().setLayout(new FlowLayout());
            getContentPane().add(new JLabel("57657667")); // NOI18N

            pack();
        }
    }
}
