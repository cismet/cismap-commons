/* Copyright 2004, Sam Reid */


package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.PNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Vector;
import pswing.PSwing;
import pswing.PSwingCanvas;

/**
 * User: Sam Reid
 * Date: Jul 11, 2005
 * Time: 12:15:55 PM
 * Copyright (c) Jul 11, 2005 by Sam Reid
 */

public class TestPSwingFull extends JFrame {
    public TestPSwingFull() {
        ClassLoader loader;
        PSwingCanvas canvas;

        // Set up basic frame
        setBounds( 100, 100, 400, 400 );
        setResizable( true );
        setBackground( null );
        setVisible( true );
        canvas = new PSwingCanvas();
        getContentPane().add( canvas );
        validate();
        loader = getClass().getClassLoader();

        ZVisualLeaf leaf;
        ZTransformGroup transform;
        PSwing swing;
        PSwing swing2;

        // JButton
        JButton button = new JButton( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.button.text") );//NOI18N
        button.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
        swing = new PSwing( canvas, button );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( -500, -500 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // JButton
        JSpinner spinner = new JSpinner( new SpinnerNumberModel( 0, 0, 10, 1 ) );
        spinner.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
        swing = new PSwing( canvas, spinner );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( -800, -500 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );


        // 2nd Copy of JButton
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( -450, -450 );
        transform.rotate( Math.PI / 2 );
        transform.scale( 0.5 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // Growable JTextArea
        JTextArea textArea = new JTextArea( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.textArea.text") );//NOI18N
        textArea.setBorder( new LineBorder( Color.blue, 3 ) );
        swing = new PSwing( canvas, textArea );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( -250, -500 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // Growable JTextField
        JTextField textField = new JTextField( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.textField.text1") );//NOI18N
        swing = new PSwing( canvas, textField );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( 0, -500 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // A Slider
        JSlider slider = new JSlider();
        swing = new PSwing( canvas, slider );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( 250, -500 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // A Scrollable JTree
        JTree tree = new JTree();
        tree.setEditable( true );
        JScrollPane p = new JScrollPane( tree );
        p.setPreferredSize( new Dimension( 150, 150 ) );
        swing = new PSwing( canvas, p );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( -500, -250 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // A Scrollable JTextArea
        JScrollPane pane = new JScrollPane( new JTextArea( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.pane.textArea.text") ) );//NOI18N
        pane.setPreferredSize( new Dimension( 150, 150 ) );
        swing = new PSwing( canvas, pane );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.setHasOneChild( true );
        transform.translate( -250, -250 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );
        swing2 = swing;

        // A non-scrollable JTextField
        // A panel MUST be created with double buffering off
        JPanel panel = new JPanel( false );
        textField = new JTextField( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.textField.text2") );//NOI18N
        panel.setLayout( new BorderLayout() );
        panel.add( textField );
        swing = new PSwing( canvas, panel );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( 0, -250 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

//        // A JComboBox
//        String[] listItems = {"Summer Teeth", "Mermaid Avenue", "Being There", "A.M."};
//        ZComboBox box = new ZComboBox( listItems );
//        swing = new PSwing( canvas, box );
//        leaf = new ZVisualLeaf( swing );
//        transform = new ZTransformGroup();
//        transform.translate( 0, -150 );
//        transform.addChild( leaf );
//        canvas.getLayer().addChild( transform );

        // A panel with TitledBorder and JList
        panel = new JPanel( false );
        panel.setBackground( Color.lightGray );
        panel.setLayout( new BorderLayout() );
        panel.setBorder( new TitledBorder( new EtchedBorder( EtchedBorder.RAISED ), org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.panel.border.TitledBorder.title"), TitledBorder.LEFT, TitledBorder.TOP ) );//NOI18N
        panel.setPreferredSize( new Dimension( 200, 200 ) );
        Vector data = new Vector();
        data.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.choice1") );//NOI18N
        data.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.choice2") );//NOI18N
        data.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.choice3") );//NOI18N
        data.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.choice4") );//NOI18N
        data.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.choice5") );//NOI18N
        JList list = new JList( data );
        list.setBackground( Color.lightGray );
        panel.add( list );
        swing = new PSwing( canvas, panel );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( 250, -250 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // A JLabel
        JLabel label = new JLabel( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.label.text1"));//,//NOI18N
                                   //new ImageIcon( loader.getResource( "HCIL-logo.gif" ) ), SwingConstants.CENTER );

        swing = new PSwing( canvas, label );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( -500, 0 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // Rotated copy of the Scrollable JTextArea
        leaf = new ZVisualLeaf( swing2 );
        transform = new ZTransformGroup();
        transform.setHasOneChild( true );
        transform.translate( -100, 0 );
        transform.rotate( Math.PI / 2 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // A panel with layout
        // A panel MUST be created with double buffering off
        panel = new JPanel( false );
        panel.setLayout( new BorderLayout() );
        JButton button1 = new JButton( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.button1.text") );//NOI18N
        JButton button2 = new JButton( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.button2.text") );//NOI18N
        label = new JLabel( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.data.label.text2") );//NOI18N
        label.setHorizontalAlignment( SwingConstants.CENTER );
        label.setForeground( Color.white );
        panel.setBackground( Color.red );
        panel.setPreferredSize( new Dimension( 150, 150 ) );
        panel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        panel.add( button1, "North" );//NOI18N
        panel.add( button2, "South" );//NOI18N
        panel.add( label, "Center" );//NOI18N
        panel.revalidate();
        swing = new PSwing( canvas, panel );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( 0, 0 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // JTable Example
        Vector columns = new Vector();
        columns.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.columns.element1") );//NOI18N
        columns.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.columns.element2") );//NOI18N
        columns.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.columns.element3") );//NOI18N
        Vector rows = new Vector();
        Vector row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row1.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row1.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row1.element3") );//NOI18N
        rows.addElement( row );
        row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row2.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row2.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row21.element3") );//NOI18N
        rows.addElement( row );
        row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row3.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row3.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row3.element3") );//NOI18N
        rows.addElement( row );
        row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row4.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row4.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row4.element3") );//NOI18N
        rows.addElement( row );
        row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row5.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row5.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row5.element3") );//NOI18N
        rows.addElement( row );
        row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row6.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row6.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row6.elemen") );//NOI18N
        rows.addElement( row );
        row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row7.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row7.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row7.element3") );//NOI18N
        rows.addElement( row );
        row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row8.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row8.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row8.element3") );//NOI18N
        rows.addElement( row );
        row = new Vector();
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row9.element1") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row9.element2") );//NOI18N
        row.addElement( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.row9.element3") );//NOI18N
        rows.addElement( row );
        JTable table = new JTable( rows, columns );
        table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        table.setRowHeight( 30 );
        TableColumn c = table.getColumn( table.getColumnName( 0 ) );
        c.setPreferredWidth( 150 );
        c = table.getColumn( table.getColumnName( 1 ) );
        c.setPreferredWidth( 150 );
        c = table.getColumn( table.getColumnName( 2 ) );
        c.setPreferredWidth( 150 );
        pane = new JScrollPane( table );
        pane.setPreferredSize( new Dimension( 200, 200 ) );
        table.setDoubleBuffered( false );
        swing = new PSwing( canvas, pane );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( 250, 0 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

        // JEditorPane - HTML example
        try {


            final JEditorPane editorPane = new JEditorPane( loader.getResource( "csdept.html" ) );//NOI18N
            editorPane.setDoubleBuffered( false );
            editorPane.setEditable( false );
            pane = new JScrollPane( editorPane );
            pane.setDoubleBuffered( false );
            pane.setPreferredSize( new Dimension( 400, 400 ) );
            editorPane.addHyperlinkListener( new HyperlinkListener() {
                public void hyperlinkUpdate( HyperlinkEvent e ) {
                    if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
                        try {
                            editorPane.setPage( e.getURL() );
                        }
                        catch( IOException ioe ) {
                            System.out.println( "Couldn't Load Web Page" );//NOI18N
                        }
                    }
                }
            } );
            swing = new PSwing( canvas, pane );
            leaf = new ZVisualLeaf( swing );
            transform = new ZTransformGroup();
            transform.translate( -500, 250 );
            transform.addChild( leaf );
            canvas.getLayer().addChild( transform );

        }
        catch( IOException ioe ) {
            System.out.println( "Couldn't Load Web Page" );//NOI18N
        }

        // A JInternalFrame with a JSplitPane - a JOptionPane - and a
        // JToolBar
        JInternalFrame iframe = new JInternalFrame( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.iframe.title") );//NOI18N
        iframe.getRootPane().setDoubleBuffered( false );
        ( (JComponent)iframe.getContentPane() ).setDoubleBuffered( false );
        iframe.setPreferredSize( new Dimension( 500, 500 ) );
        JTabbedPane tabby = new JTabbedPane();
        tabby.setDoubleBuffered( false );
        iframe.getContentPane().setLayout( new BorderLayout() );
        JOptionPane options = new JOptionPane( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.options.message"),//NOI18N
                                               JOptionPane.INFORMATION_MESSAGE,
                                               JOptionPane.DEFAULT_OPTION );
        options.setDoubleBuffered( false );
        options.setMinimumSize( new Dimension( 50, 50 ) );
        options.setPreferredSize( new Dimension( 225, 225 ) );
        JPanel tools = new JPanel( false );
        tools.setMinimumSize( new Dimension( 150, 150 ) );
        tools.setPreferredSize( new Dimension( 225, 225 ) );
        JToolBar bar = new JToolBar();
        Action letter = new AbstractAction( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.letter.name"), new ImageIcon( loader.getResource( "letter.gif" ) ) ) {//NOI18N

            public void actionPerformed( ActionEvent e ) {}
        };

        Action hand = new AbstractAction( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.hand.name"), new ImageIcon( loader.getResource( "hand.gif" ) ) ) {//NOI18N
            public void actionPerformed( ActionEvent e ) {}
        };
        Action select = new AbstractAction( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.select.name"),//NOI18N
                                            new ImageIcon( loader.getResource( "select.gif" ) ) ) {//NOI18N
            public void actionPerformed( ActionEvent e ) {}
        };

        label = new JLabel( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.label.text") );//NOI18N
        label.setHorizontalAlignment( SwingConstants.CENTER );
        bar.add( letter );
        bar.add( hand );
        bar.add( select );
        bar.setFloatable( false );
        bar.setBorder( new LineBorder( Color.black, 2 ) );
        tools.setLayout( new BorderLayout() );
        tools.add( bar, "North" );//NOI18N
        tools.add( label, "Center" );//NOI18N

        JSplitPane split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, options, tools );
        split.setDoubleBuffered( false );
        iframe.getContentPane().add( split );
        swing = new PSwing( canvas, iframe );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( 0, 250 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );

//        JMenuBar menuBar = new JMenuBar();
//        ZMenu menu = new ZMenu( "File" );
//        ZMenu sub = new ZMenu( "Export" );
//        JMenuItem gif = new JMenuItem( "Funds" );
//        sub.add( gif );
//        menu.add( sub );
//        menuBar.add( menu );
//        iframe.setJMenuBar( menuBar );

        iframe.setVisible( true );

        // A JColorChooser - also demonstrates JTabbedPane
//        JColorChooser chooser = new JColorChooser();
        JCheckBox chooser = new JCheckBox( org.openide.util.NbBundle.getMessage(TestPSwingFull.class, "TestPSwingFull.chooser.text") );//NOI18N
        swing = new PSwing( canvas, chooser );
        leaf = new ZVisualLeaf( swing );
        transform = new ZTransformGroup();
        transform.translate( -250, 850 );
        transform.addChild( leaf );
        canvas.getLayer().addChild( transform );



        // Revalidate and repaint
        canvas.revalidate();
        canvas.repaint();
    }

    public static void main( String[] args ) {
        new TestPSwingFull().setVisible( true );
    }

    public static class ZVisualLeaf extends PNode {

        public ZVisualLeaf( PNode node ) {
            addChild( node );
        }
    }

    public static class ZTransformGroup extends PNode {

        public void setHasOneChild( boolean b ) {

        }
    }

}