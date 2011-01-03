/***************************************************** cismet GmbH, Saarbruecken, Germany**              ... and it just works.*****************************************************/import edu.umd.cs.piccolo.PCanvas;import edu.umd.cs.piccolo.nodes.PPath;import edu.umd.cs.piccolox.PFrame;import edu.umd.cs.piccolox.nodes.PComposite;import java.awt.Color;import java.awt.geom.GeneralPath;/** * This example shows how to create a composite node. A composite node is a group of nodes that behave as a single node * when interacted with. * * @version  $Revision$, $Date$ */public class CompositeExample extends PFrame {    //~ Constructors -----------------------------------------------------------    /**     * Creates a new CompositeExample object.     */    public CompositeExample() {        this(null);    }    /**     * Creates a new CompositeExample object.     *     * @param  aCanvas  DOCUMENT ME!     */    public CompositeExample(final PCanvas aCanvas) {        super("CompositeExample", false, aCanvas);    }    //~ Methods ----------------------------------------------------------------    @Override    public void initialize() {//              PNode composite = new PNode();////              PNode circle = PPath.createEllipse(0, 0, 100, 100);//              PNode rectangle = PPath.createRectangle(50, 50, 100, 100);//              PNode text = new PText("Hello world!");////              composite.addChild(circle);//              composite.addChild(rectangle);//              composite.addChild(text);//                circle.translate(20.0,50.0);//              rectangle.rotate(Math.toRadians(45));//              rectangle.setPaint(Color.RED);////              text.scale(2.0);//              text.setPaint(Color.GREEN);//                composite.setPaint(Color.green);//                composite.setBounds(10,10,100,100);////              getCanvas().getLayer().addChild(composite);//              getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());//              getCanvas().addInputEventListener(new PDragEventHandler());        final PPath path = new PPath();        path.moveTo(100, 10);        path.lineTo(20, 10);        path.lineTo(20, 20);        path.lineTo(10, 20);        path.lineTo(10, 10);        path.moveTo(15, 15);        path.lineTo(17, 15);        path.lineTo(17, 17);        path.lineTo(15, 17);        path.lineTo(15, 15);        path.getPathReference().setWindingRule(GeneralPath.WIND_EVEN_ODD);        path.setPaint(Color.BLUE);        getCanvas().getLayer().addChild(path);    }    /**     * DOCUMENT ME!     *     * @param  args  DOCUMENT ME!     */    public static void main(final String[] args) {        new CompositeExample();    }}