package de.cismet.cismap.commons.gui.layerwidget.test;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;


/**
* Demonstrates how to display a 'drag image' when using drag and drop on those platforms
* whose JVMs do not support it natively (eg Win32).
*/
public class CTree extends JTree implements DragSourceListener, 
											DragGestureListener, 
											Autoscroll,
											TreeModelListener
{
// Constants...

    private static final ResourceBundle I18N =
            ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle");

// Fields...
	private TreePath	_pathSource;			// The path being dragged
	private BufferedImage	_imgGhost;			// The 'drag image' 
	private Point		_ptOffset = new Point();	// Where, in the drag image, the mouse was clicked


// Constructors...
	public CTree ()	// Use the default JTree constructor so that we get a sample TreeModel built for us
	{

		putClientProperty("JTree.lineStyle", "Angled");	// I like this look
		
		// Make this JTree a drag source
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);

		// Also, make this JTree a drag target
		DropTarget dropTarget = new DropTarget(this, new CDropTargetListener());
		dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);

	}


// Test harness...
	public static void main (String argv [])
	{
		try
		{
			UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
		

		CTree tree = new CTree();
		tree.setPreferredSize(new Dimension(300,300));
		JScrollPane scrollPane = new JScrollPane(tree);


		JFrame frame = new JFrame(I18N.getString("de.cismet.cismap.commons.gui.layerwidget.test.CTree.main().frame.title"));
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		frame.pack();

		Dimension dimScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dimFrame  = frame.getSize();
		frame.setLocation((dimScreen.width	- dimFrame.width) /2,
						  (dimScreen.height - dimFrame.height)/2);

		frame.addWindowListener
		(
			new WindowAdapter() 
			{
				public void windowClosing(WindowEvent e) 
				{
					System.exit(0);
				}
			}
		);

		frame.show();

	}





// Interface: DragGestureListener
	public void dragGestureRecognized(DragGestureEvent e)
	{
		
		Point ptDragOrigin = e.getDragOrigin();
		TreePath path = getPathForLocation(ptDragOrigin.x, ptDragOrigin.y);
		if (path == null)
			return;
		if (isRootPath(path))
			return;	// Ignore user trying to drag the root node
		


		// Work out the offset of the drag point from the TreePath bounding rectangle origin
		Rectangle raPath = getPathBounds(path);
		_ptOffset.setLocation(ptDragOrigin.x-raPath.x, ptDragOrigin.y-raPath.y);
			
		// Get the cell renderer (which is a JLabel) for the path being dragged
		JLabel lbl = (JLabel) getCellRenderer().getTreeCellRendererComponent
								(
									this, 											// tree
									path.getLastPathComponent(),					// value
									false,											// isSelected	(dont want a colored background)
									isExpanded(path), 								// isExpanded
									getModel().isLeaf(path.getLastPathComponent()), // isLeaf
									0, 												// row			(not important for rendering)
									false											// hasFocus		(dont want a focus rectangle)
								);
		lbl.setSize((int)raPath.getWidth(), (int)raPath.getHeight()); // <-- The layout manager would normally do this

		// Get a buffered image of the selection for dragging a ghost image
		_imgGhost = new BufferedImage((int)raPath.getWidth(), (int)raPath.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = _imgGhost.createGraphics();

		// Ask the cell renderer to paint itself into the BufferedImage
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));		// Make the image ghostlike
		lbl.paint(g2);

		// Now paint a gradient UNDER the ghosted JLabel text (but not under the icon if any)
		// Note: this will need tweaking if your icon is not positioned to the left of the text
		Icon icon = lbl.getIcon();
		int nStartOfText = (icon == null) ? 0 : icon.getIconWidth()+lbl.getIconTextGap();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.5f));	// Make the gradient ghostlike
		g2.setPaint(new GradientPaint(nStartOfText,	0, SystemColor.controlShadow, 
									  getWidth(),	0, new Color(255,255,255,0)));
		g2.fillRect(nStartOfText, 0, getWidth(), _imgGhost.getHeight());

		g2.dispose();


		
		setSelectionPath(path);	// Select this path in the tree
		
		System.out.println("DRAGGING: "+path.getLastPathComponent());

		// Wrap the path being transferred into a Transferable object
		Transferable transferable = new CTransferableTreePath(path);

		// Remember the path being dragged (because if it is being moved, we will have to delete it later)
		_pathSource = path;	
		
		// We pass our drag image just in case it IS supported by the platform
		e.startDrag(null, _imgGhost, new Point(5,5), transferable, this);
	}	
	







// Interface: DragSourceListener
	public void dragEnter(DragSourceDragEvent e)
	{
	}	
	public void dragOver(DragSourceDragEvent e)
	{
	}	
	public void dragExit(DragSourceEvent e)
	{
	}	
	public void dropActionChanged(DragSourceDragEvent e)
	{
	}	
	public void dragDropEnd(DragSourceDropEvent e)
	{
		if (e.getDropSuccess())
		{
			int nAction = e.getDropAction();
			if (nAction == DnDConstants.ACTION_MOVE)
			{	// The dragged item (_pathSource) has been inserted at the target selected by the user.
				// Now it is time to delete it from its original location.
				System.out.println("REMOVING: " + _pathSource.getLastPathComponent());

				// .
				// .. ask your TreeModel to delete the node 
				// .

				_pathSource = null;
			}
		}
	}	
	
		





	






// DropTargetListener interface object...
	class CDropTargetListener implements DropTargetListener
	{
		// Fields...
		private TreePath		_pathLast		= null;
		private Rectangle2D 	_raCueLine		= new Rectangle2D.Float();
		private Rectangle2D 	_raGhost		= new Rectangle2D.Float();
		private Color			_colorCueLine;
		private Point			_ptLast			= new Point();
		private Timer			_timerHover;
		private int				_nLeftRight		= 0;	// Cumulative left/right mouse movement
		private BufferedImage	_imgRight		= new CArrowImage(15,15,CArrowImage.ARROW_RIGHT);
		private BufferedImage	_imgLeft		= new CArrowImage(15,15,CArrowImage.ARROW_LEFT );
		private int			 	_nShift			= 0;
		
		// Constructor...
		public CDropTargetListener()
		{
			_colorCueLine = new Color(
										SystemColor.controlShadow.getRed(),
										SystemColor.controlShadow.getGreen(),
										SystemColor.controlShadow.getBlue(),
										64
									  );

			// Set up a hover timer, so that a node will be automatically expanded or collapsed
			// if the user lingers on it for more than a short time
			_timerHover = new Timer(1000, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					_nLeftRight = 0;	// Reset left/right movement trend
					if (isRootPath(_pathLast))
						return;	// Do nothing if we are hovering over the root node
					if (isExpanded(_pathLast))
						collapsePath(_pathLast);
					else
						expandPath(_pathLast);
				}
			});
			_timerHover.setRepeats(false);	// Set timer to one-shot mode
		}
		
		// DropTargetListener interface
		public void dragEnter(DropTargetDragEvent e)
		{
			if (!isDragAcceptable(e))
				e.rejectDrag();
			else
				e.acceptDrag(e.getDropAction());	
		}
		
		public void dragExit(DropTargetEvent e)
		{
			if (!DragSource.isDragImageSupported())
			{
				repaint(_raGhost.getBounds());				
			}
		}

		/**
		* This is where the ghost image is drawn
		*/		
		public void dragOver(DropTargetDragEvent e)
		{
			// Even if the mouse is not moving, this method is still invoked 10 times per second
			Point pt = e.getLocation();
			if (pt.equals(_ptLast))
				return;
			
			// Try to determine whether the user is flicking the cursor right or left
			int nDeltaLeftRight = pt.x - _ptLast.x;
			if ( (_nLeftRight > 0 && nDeltaLeftRight < 0) || (_nLeftRight < 0 && nDeltaLeftRight > 0) )
				_nLeftRight = 0;
			_nLeftRight += nDeltaLeftRight;	


			_ptLast = pt;	
			

			Graphics2D g2 = (Graphics2D) getGraphics();

			// If a drag image is not supported by the platform, then draw my own drag image
			if (!DragSource.isDragImageSupported())
			{
				paintImmediately(_raGhost.getBounds());	// Rub out the last ghost image and cue line
				// And remember where we are about to draw the new ghost image
				_raGhost.setRect(pt.x - _ptOffset.x, pt.y - _ptOffset.y, _imgGhost.getWidth(), _imgGhost.getHeight());
				g2.drawImage(_imgGhost, AffineTransform.getTranslateInstance(_raGhost.getX(), _raGhost.getY()), null);				
			}
			else	// Just rub out the last cue line
				paintImmediately(_raCueLine.getBounds());				
			
			
			
			TreePath path = getClosestPathForLocation(pt.x, pt.y);
			if (!(path == _pathLast))			
			{
				_nLeftRight = 0; 	// We've moved up or down, so reset left/right movement trend
				_pathLast = path;
				_timerHover.restart();
			}

			// In any case draw (over the ghost image if necessary) a cue line indicating where a drop will occur
			Rectangle raPath = getPathBounds(path);
			_raCueLine.setRect(0,  raPath.y+(int)raPath.getHeight(), getWidth(), 2);

			g2.setColor(_colorCueLine);
			g2.fill(_raCueLine);
			
			// Now superimpose the left/right movement indicator if necessary
			if (_nLeftRight > 20)
			{
				g2.drawImage(_imgRight, AffineTransform.getTranslateInstance(pt.x - _ptOffset.x, pt.y - _ptOffset.y), null);				
				_nShift = +1;
			}
			else if (_nLeftRight < -20)
			{
				g2.drawImage(_imgLeft, AffineTransform.getTranslateInstance(pt.x - _ptOffset.x, pt.y - _ptOffset.y), null);				
				_nShift = -1;
			}
			else
				_nShift = 0;
			

			// And include the cue line in the area to be rubbed out next time
			_raGhost = _raGhost.createUnion(_raCueLine);	

/*				
			// Do this if you want to prohibit dropping onto the drag source
			if (path.equals(_pathSource))			
				e.rejectDrag();
			else
				e.acceptDrag(e.getDropAction());	
*/				
		}
		
		public void dropActionChanged(DropTargetDragEvent e)
		{
			if (!isDragAcceptable(e))
				e.rejectDrag();
			else
				e.acceptDrag(e.getDropAction());	
		}
		
		public void drop(DropTargetDropEvent e)
		{
			_timerHover.stop();	// Prevent hover timer from doing an unwanted expandPath or collapsePath
			
			if (!isDropAcceptable(e))
			{
				e.rejectDrop();
				return;
			}
			
			e.acceptDrop(e.getDropAction());
			
			Transferable transferable = e.getTransferable();
			
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++ )
			{
				DataFlavor flavor = flavors[i];
				if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType))
				{
					try
					{
						Point pt = e.getLocation();
						TreePath pathTarget = getClosestPathForLocation(pt.x, pt.y);
						TreePath pathSource = (TreePath) transferable.getTransferData(flavor);

						System.out.println("DROPPING: "+pathSource.getLastPathComponent());
						TreeModel model =  getModel();
						TreePath pathNewChild = null;
						
						// .
						// .. Add your code here to ask your TreeModel to copy the node and act on the mouse gestures...
						// .
						
						// For example:

						// If pathTarget is an expanded BRANCH, 
						// 		then insert source UNDER it (before the first child if any)
						// If pathTarget is a collapsed BRANCH (or a LEAF), 
						//		then insert source AFTER it
						// 		Note: a leaf node is always marked as collapsed
						// You ask the model to do the copying...
						// ...and you supply the copyNode method in the model as well of course.
//						if (_nShift == 0)
//							pathNewChild = model.copyNode(pathSource, pathTarget, isExpanded(pathTarget)); 
//						else if (_nShift > 0)	// The mouse is being flicked to the right (so move the node right)
//							pathNewChild = model.copyNodeRight(pathSource, pathTarget); 
//						else					// The mouse is being flicked to the left (so move the node left)
//							pathNewChild = model.copyNodeLeft(pathSource); 
						
						if (pathNewChild != null)
							setSelectionPath(pathNewChild);	// Mark this as the selected path in the tree
						break; // No need to check remaining flavors
					}
					catch (UnsupportedFlavorException ufe)
					{
						System.out.println(ufe);
						e.dropComplete(false);
						return;
					}
					catch (IOException ioe)
					{
						System.out.println(ioe);
						e.dropComplete(false);
						return;
					}
				}
			}
			
			e.dropComplete(true);
		}
		
		
		
		// Helpers...
		public boolean isDragAcceptable(DropTargetDragEvent e)
		{
			// Only accept COPY or MOVE gestures (ie LINK is not supported)
			if ((e.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0)
				return false;

			// Only accept this particular flavor	
			if (!e.isDataFlavorSupported(CTransferableTreePath.TREEPATH_FLAVOR))
				return false;
				
/*				
			// Do this if you want to prohibit dropping onto the drag source...
			Point pt = e.getLocation();
			TreePath path = getClosestPathForLocation(pt.x, pt.y);
			if (path.equals(_pathSource))			
				return false;

*/
				
/*				
			// Do this if you want to select the best flavor on offer...
			DataFlavor[] flavors = e.getCurrentDataFlavors();
			for (int i = 0; i < flavors.length; i++ )
			{
				DataFlavor flavor = flavors[i];
				if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType))
					return true;
			}
*/
			return true;
		}

		public boolean isDropAcceptable(DropTargetDropEvent e)
		{
			// Only accept COPY or MOVE gestures (ie LINK is not supported)
			if ((e.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0)
				return false;

			// Only accept this particular flavor	
			if (!e.isDataFlavorSupported(CTransferableTreePath.TREEPATH_FLAVOR))
				return false;

/*				
			// Do this if you want to prohibit dropping onto the drag source...
			Point pt = e.getLocation();
			TreePath path = getClosestPathForLocation(pt.x, pt.y);
			if (path.equals(_pathSource))			
				return false;
*/				
				
/*				
			// Do this if you want to select the best flavor on offer...
			DataFlavor[] flavors = e.getCurrentDataFlavors();
			for (int i = 0; i < flavors.length; i++ )
			{
				DataFlavor flavor = flavors[i];
				if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType))
					return true;
			}
*/
			return true;
		}
		

	}
	




// Autoscroll Interface...
// The following code was borrowed from the book:
//		Java Swing
//		By Robert Eckstein, Marc Loy & Dave Wood
//		Paperback - 1221 pages 1 Ed edition (September 1998) 
//		O'Reilly & Associates; ISBN: 156592455X 
//
// The relevant chapter of which can be found at:
//		http://www.oreilly.com/catalog/jswing/chapter/dnd.beta.pdf
	
	private static final int AUTOSCROLL_MARGIN = 12;

	// Ok, we've been told to scroll because the mouse cursor is in our
	// scroll zone.
	public void autoscroll(Point pt) 
	{
		// Figure out which row we're on.
		int nRow = getRowForLocation(pt.x, pt.y);
		
		// If we are not on a row then ignore this autoscroll request
		if (nRow < 0)
			return;

		Rectangle raOuter = getBounds();
		// Now decide if the row is at the top of the screen or at the
		// bottom. We do this to make the previous row (or the next
		// row) visible as appropriate. If we're at the absolute top or
		// bottom, just return the first or last row respectively.
		
		nRow =	(pt.y + raOuter.y <= AUTOSCROLL_MARGIN)			// Is row at top of screen? 
				 ?	
				(nRow <= 0 ? 0 : nRow - 1)						// Yes, scroll up one row
				 :
				(nRow < getRowCount() - 1 ? nRow + 1 : nRow);	// No, scroll down one row

		scrollRowToVisible(nRow);
	}
	// Calculate the insets for the *JTREE*, not the viewport
	// the tree is in. This makes it a bit messy.
	public Insets getAutoscrollInsets() 
	{
		Rectangle raOuter = getBounds();
		Rectangle raInner = getParent().getBounds();
		return new Insets(
			raInner.y - raOuter.y + AUTOSCROLL_MARGIN, raInner.x - raOuter.x + AUTOSCROLL_MARGIN,
			raOuter.height - raInner.height - raInner.y + raOuter.y + AUTOSCROLL_MARGIN,
			raOuter.width - raInner.width - raInner.x + raOuter.x + AUTOSCROLL_MARGIN);
	}
/*	
	// Use this method if you want to see the boundaries of the
	// autoscroll active region. Toss it out, otherwise.
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		Rectangle raOuter = getBounds();
		Rectangle raInner = getParent().getBounds();
		g.setColor(Color.red);
		g.drawRect(-raOuter.x + 12, -raOuter.y + 12,
			raInner.width - 24, raInner.height - 24);
	}
	
*/




// TreeModelListener interface...
	public void treeNodesChanged(TreeModelEvent e)
	{
		System.out.println("treeNodesChanged");
		sayWhat(e);
		// We dont need to reset the selection path, since it has not moved
	}

	public void treeNodesInserted(TreeModelEvent e)
	{
		System.out.println("treeNodesInserted ");
		sayWhat(e);

		// We need to reset the selection path to the node just inserted
		int nChildIndex = e.getChildIndices()[0];
		TreePath pathParent = e.getTreePath();
		setSelectionPath(getChildPath(pathParent, nChildIndex));
	}

	public void treeNodesRemoved(TreeModelEvent e)
	{
		System.out.println("treeNodesRemoved ");
		sayWhat(e);
	}

	public void treeStructureChanged(TreeModelEvent e)
	{
		System.out.println("treeStructureChanged ");
		sayWhat(e);
	}











// More helpers...
	private TreePath getChildPath(TreePath pathParent, int nChildIndex)
	{
		TreeModel model =  getModel();
		return pathParent.pathByAddingChild(model.getChild(pathParent.getLastPathComponent(), nChildIndex));
	}


	private boolean isRootPath(TreePath path)
	{
		return isRootVisible() && getRowForPath(path) == 0;
	}



	private void sayWhat(TreeModelEvent e)
	{
		System.out.println(e.getTreePath().getLastPathComponent());
		int[] nIndex = e.getChildIndices();
		for (int i = 0; i < nIndex.length ;i++ )
		{
			System.out.println(i+". "+nIndex[i]);
		}
	}

}

