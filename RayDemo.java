import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import ptolemy.plot.*;
import edu.washington.apl.aganse.ptolemyUpdates.plot.*;
import edu.washington.apl.aganse.dataTools.*;
import java.net.URL;
import java.lang.Math;
import java.io.*;
import java.util.StringTokenizer;


//on applet tags themselves:
//http://java.sun.com/applets/
//on getting data from applet tags:
//http://java.sun.com/docs/books/tutorial/applet/appletsonly/getParam.html
//on the action architecture:
//http://java.sun.com/products/jfc/tsc/articles/actions/index.html

public class RayDemo extends JApplet {

    private int MAXRUNS=200;
    private Action clearAction, propertiesAction, runAction, stopAction;
    private Action helpAction, aboutAction, showPlotsAction;
    private DataSeries VZData = new DataSeries();
    private DataSeries TauPData = new DataSeries();
    private DataSeries TXData = new DataSeries();
    private DataSeries TPData = new DataSeries();
    private DataSeries PXData = new DataSeries();
    private DataSeries ZXData[] = new DataSeries[MAXRUNS];
    private JPanel contentPanel;
    private PlotMatrix plotMatrix;
    private JPanel directionsPanel;
    private PXPanel pxpanel;
    private TauPPanel tauppanel;
    private TXPanel txpanel;
    private TPPanel tppanel;
    private VZPanel vzpanel;
    private ZXPanel zxpanel;
    private JButton btnRun;
    private JTextField srcDepthTxt = new JTextField("0",2);
    private JTextField startAngleTxt = new JTextField("89",3);
    private JTextField endAngleTxt = new JTextField("0",3);
    private JTextField angleIncrTxt = new JTextField("1",2);
    private CalcThread calcThread = null;
    private boolean stopCalc = false, alreadyRunning=false;
    private JComboBox cmbRunType;
    private String runType = "Sweep Rays";
    private double maxZXrange;
    private JCheckBox cbxUseRV = new JCheckBox("",true);
    private JLabel uservLbl = new JLabel("Use Reduction Velocity : ");
    private JTextField redVelTxt = new JTextField("5",2);
    private int width=750, height=350;
    private String widthString="750", heightString="350", plotMode;


    public void init() {

        //Get width, height and plotmode params from HTML APPLET or OBJECT call, or use defaults
        //widthString = getParameter("width");  // for running as an applet
        //heightString = getParameter("height");  // for running as an applet
        if(widthString!=null) {
            width = Integer.parseInt(widthString);
        }
        if(heightString!=null) {
            height = Integer.parseInt(heightString); 
        }
        // Plotmode can be "VZ-ZX", "VZ-ZX-TX", "VZ-ZX-TX-PX", "VZ-ZX-TX-PX-TP":
        if(plotMode==null) plotMode="VZ-ZX";
        //if(plotMode==null) plotMode="VZ-ZX-TX";
        //plotMode = getParameter("plotmode");  // for running as an applet

        //Put correct plots into plots panel depending on raydemo args
        plotMatrix=new PlotMatrix();
        plotMatrix.build(plotMode);

        //Hook up actions to toolbar buttons:
        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        createActionComponents(toolBar);

        //Lay out toolbar and plots panel in main app window:
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setVisible(true);
        //contentPanel.setPreferredSize(new Dimension(width, width/2+50));
        contentPanel.add(toolBar, BorderLayout.NORTH);
        contentPanel.add(plotMatrix, BorderLayout.CENTER);
        getContentPane().add(contentPanel);

    }

    private class PlotMatrix extends Box {
        public PlotMatrix() {
            super(BoxLayout.X_AXIS);
            if(width < 850) {
                maxZXrange=50;
            } else if(width>=850 && width<1050) maxZXrange=60;
            else maxZXrange=70;
            vzpanel = new VZPanel();
            zxpanel = new ZXPanel();
            pxpanel = new PXPanel();
            tauppanel = new TauPPanel();
            txpanel = new TXPanel();
            tppanel = new TPPanel();
            vzpanel = new VZPanel();
            zxpanel = new ZXPanel();
        }
        public void build(String plotmode) {
            if(plotmode.equals("VZ-ZX")) {
                Box box0 = new Box(BoxLayout.X_AXIS);
                vzpanel.setVisible(true);
                vzpanel.setPreferredSize(new Dimension(200,height));
                zxpanel.setPreferredSize(new Dimension(width-200,height));
                zxpanel.setVisible(true);
                box0.add(vzpanel);
                box0.add(zxpanel);
                box0.setPreferredSize(new Dimension(width,height));
                add(box0);
            }
            if(plotmode.equals("VZ-ZX-TX")) {
                Box box0 = new Box(BoxLayout.Y_AXIS);
                JPanel blankPanel = new JPanel();
                blankPanel.setPreferredSize(new Dimension(200,height));
                txpanel.setPreferredSize(new Dimension(width-200,height));
                vzpanel.setPreferredSize(new Dimension(200,height));
                zxpanel.setPreferredSize(new Dimension(width-200,height));
                txpanel.setVisible(true);
                vzpanel.setVisible(true);
                zxpanel.setVisible(true);
                Box box1 = new Box(BoxLayout.X_AXIS);
                Box box2 = new Box(BoxLayout.X_AXIS);
                box1.add(blankPanel);
                box1.add(txpanel);
                box2.add(vzpanel);
                box2.add(zxpanel);
                box0.add(box1);
                box0.add(box2);
                add(box0);
            }
            if(plotmode.equals("VZ-ZX-TX-PX-TP")) {
                Box box0 = new Box(BoxLayout.Y_AXIS);
                JPanel blankPanel1 = new JPanel();
                blankPanel1.setPreferredSize(new Dimension(200,height));
                JPanel blankPanel3 = new JPanel();
                blankPanel3.setPreferredSize(new Dimension(200,height));
                JPanel blankPanel2 = new JPanel();
                blankPanel2.setPreferredSize(new Dimension((width-200)/2,height));
                JPanel blankPanel4 = new JPanel();
                blankPanel4.setPreferredSize(new Dimension((width-200)/2,height));
                txpanel.setPreferredSize(new Dimension((width-200)/2,height));
                pxpanel.setPreferredSize(new Dimension((width-200)/2,height));
                tppanel.setPreferredSize(new Dimension((width-200)/2,height));
                vzpanel.setPreferredSize(new Dimension(200,height));
                zxpanel.setPreferredSize(new Dimension((width-200)/2,height));
                txpanel.setVisible(true);
                pxpanel.setVisible(true);
                tppanel.setVisible(true);
                vzpanel.setVisible(true);
                zxpanel.setVisible(true);
                Box box1 = new Box(BoxLayout.X_AXIS);
                Box box2 = new Box(BoxLayout.X_AXIS);
                Box box3 = new Box(BoxLayout.X_AXIS);
                box1.add(blankPanel1);
                box1.add(pxpanel);
                box1.add(blankPanel2);
                box2.add(blankPanel3);
                box2.add(txpanel);
                box2.add(tppanel);
                box3.add(vzpanel);
                box3.add(zxpanel);
                box3.add(blankPanel4);
                box0.add(box1);
                box0.add(box2);
                box0.add(box3);
                add(box0);
            }
        }
    }
    
    
    private class PlotPanel extends JPanel {
        private MenuPlot myplot;
        private String pointsOnOff="on";
        private JPopupMenu plotMenu;
        private int datasetnum;
        private DataSeries data;
        private DataSeries dataMulti[];
        private double xmin, ymin, xmax, ymax;
        public PlotPanel(String title, String xlabel, String ylabel, int datasetnum,
                         DataSeries data, double xmin, double xmax, double ymin,
                         double ymax) {
            this.xmin=xmin; this.xmax=xmax; this.ymin=ymin; this.ymax=ymax;
            this.data=data;
            this.datasetnum=datasetnum;
            init(title, xlabel, ylabel);
            plotMenu = new JPopupMenu();
            addPlotMenuItems(plotMenu);
        }
        public PlotPanel(String title, String xlabel, String ylabel,
                         DataSeries data[], double xmin, double xmax, double ymin,
                         double ymax) {
            this.xmin=xmin; this.xmax=xmax; this.ymin=ymin; this.ymax=ymax;
            this.dataMulti=data;
            init(title, xlabel, ylabel);
            plotMenu = new JPopupMenu();
            addPlotMenuItems(plotMenu);
        }
        public void fill() {myplot.fillPlot();}
        public void init(String title, String xlabel, String ylabel) {
            myplot = new MenuPlot();
            super.setLayout(new BorderLayout());
            super.add(myplot,BorderLayout.CENTER);
            myplot.setVisible(true);
            myplot.setButtons(false);
            myplot.setTitle(title);
            myplot.setXLabel(xlabel);
            myplot.setYLabel(ylabel);
            myplot.setMarksStyle("dots");
            resetXYRanges();
        }
        public void clear() {
            for(int i=0; i<MAXRUNS; i++) {
                myplot.clear(i);
            }
        }
        public void update() {
            myplot.clear(datasetnum);
            myplot.setNextDataColor(new Color(0x000000));
            //System.out.println("datasetnum = "+datasetnum);
            datasetnum=0;  // temporary hack to get things to mostly work
            // FIXME: somehow datasetnum is getting out of sync with how many
            // colors are available to plot with.  
            // I.e. ultimately, in Plot.java at line 1643 the _linecolor Vector
            // doesn't have requested datasetnum: _linecolor.elementAt(dataset)
            myplot.addPoints(datasetnum, data.xToArray(), data.yToArray(), true);
        }
        public void updateMulti() {
            double startAngle = Double.parseDouble(startAngleTxt.getText());
            double endAngle = Double.parseDouble(endAngleTxt.getText());
            double angleIncr = Double.parseDouble(angleIncrTxt.getText());
            int numrays = (int)((startAngle-endAngle)/angleIncr);
            for(int i=0; i<numrays && stopCalc==false; i++) {
                myplot.clear(i);
                //if(dataMulti[i]!=null) {
                    Color tmp = new Color(Color.HSBtoRGB(
                       (float)i / (float)(numrays) ,1,1) );
                    myplot.setNextDataColor(tmp);
                    myplot.addPoints(i, dataMulti[i].xToArray(),
                                     dataMulti[i].yToArray(), true);
                //}
            }
        }
        public void resetXYRanges() {
            myplot.setXRange(xmin,xmax);
            myplot.setYRange(ymin,ymax);
        }
        public XPlot getPlotHandle() { return myplot; }
        public class MenuPlot extends XPlot {
            public MenuPlot() {
            }
            /** catch mouse click and if location is valid add data point */
            public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger() || e.getModifiers()==MouseEvent.BUTTON2_MASK
                    || e.getModifiers()==MouseEvent.BUTTON3_MASK) {
                    plotMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
        public void addPlotMenuItems(JPopupMenu plotMenu) {
            JMenuItem menuItem0 = new JMenuItem("Orig Data Range");
            menuItem0.setMnemonic(KeyEvent.VK_O);
            menuItem0.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        resetXYRanges();
                        repaint();
                    }
                });
            plotMenu.add(menuItem0);
            JMenuItem menuItem1 = new JMenuItem("Points On/Off");
            menuItem1.setMnemonic(KeyEvent.VK_C);
            menuItem1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        togglePlotPoints();
                    }
                });
            plotMenu.add(menuItem1);
        }
        public void togglePlotPoints() {
            if(pointsOnOff.equals("on")) {
                myplot.setMarksStyle("none");
                pointsOnOff="off";
            }
            else if(pointsOnOff.equals("off")) {
                myplot.setMarksStyle("dots");
				myplot.setConnected(false,0);
                pointsOnOff="on";
            }           
            repaint();
        }
    }


    private class VZPanel extends JPanel {
        private EditPlot myplot;
        private String pointsOnOff="on";
        private JPopupMenu _velMenu;
        private double xmin, ymin, xmax, ymax;
        public VZPanel() {
            _velMenu = new JPopupMenu();
            addVelMenuItems(_velMenu);
            myplot = new EditPlot();
            super.setLayout(new BorderLayout());
            super.add(myplot,BorderLayout.CENTER);
            //add(myplot);
            myplot.setVisible(true);
            myplot.setButtons(false);
            //myplot.clear(true);
            myplot.setTitle("WaveVel vs Depth");
            myplot.setXLabel("vel (km/s)");
            myplot.setYLabel("depth (km)");
            myplot.setMarksStyle("dots");
            xmin=0.0; xmax=6.0;
            ymin=-25.0; ymax=0.0; //wave vel V in km/s
            resetXYRanges(); //depth Z in km (beyond 30km can't assume flat)
        }
        public void resetXYRanges() {
            myplot.setXRange(xmin,xmax);
            myplot.setYRange(ymin,ymax);
        }
        public void update() {
            myplot.clear(0);
            myplot.addPoints(0, VZData.yToArray(), VZData.xToArray(), true);
        }
        public class EditPlot extends XPlot {
            public EditPlot() {
            }
            /** catch mouse click and if location is valid add data point */
            public void mousePressed(MouseEvent e) {
                if(e.getModifiers()==MouseEvent.BUTTON1_MASK) {
                    e.consume();
                    int tmpx=e.getX();
                    int tmpy=e.getY();
                    if(inPlotRegion(tmpx,tmpy) && getDataY(tmpy)<0.5) {
                        // the following is to make it easy for the user to enter pts @ z=0:
                        double datay;
                        if(getDataY(tmpy)<0.5 && getDataY(tmpy)>-0.5) datay=0;
                        else datay=getDataY(tmpy);
                        // note swapping x & y because want z-value (depth) on y axis
                        VZData.add(datay,getDataX(tmpx));
                        VZData.sort();
                        vzpanel.update();
                    }
                }
                if (e.isPopupTrigger() || e.getModifiers()==MouseEvent.BUTTON2_MASK
                    || e.getModifiers()==MouseEvent.BUTTON3_MASK) {
                    _velMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
        public void addVelMenuItems(JPopupMenu velMenu) {
            JMenuItem menuItem0 = new JMenuItem("Orig Data Range");
            menuItem0.setMnemonic(KeyEvent.VK_O);
            menuItem0.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        resetXYRanges();
                        repaint();
                    }
                });
            velMenu.add(menuItem0);
            JMenuItem menuItem1 = new JMenuItem("Points On/Off");
            menuItem1.setMnemonic(KeyEvent.VK_C);
            menuItem1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(pointsOnOff.equals("on")) {
                            myplot.setMarksStyle("none");
                            pointsOnOff="off";
                        }
                        else if(pointsOnOff.equals("off")) {
                            myplot.setMarksStyle("dots");
                            pointsOnOff="on";
                        }
                        repaint();
                    }
                });
            velMenu.add(menuItem1);
            velMenu.addSeparator();
            JMenuItem menuItem10 = new JMenuItem("Clear");
            menuItem10.setMnemonic(KeyEvent.VK_C);
            menuItem10.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VZData.clear();
                        vzpanel.update();
                    }
                });
            velMenu.add(menuItem10);
            //velMenu.add(JPopupMenu.Separator);   // J2 v1.4 only?
            JMenuItem menuItem11 = new JMenuItem("Isovelocity");
            menuItem11.setMnemonic(KeyEvent.VK_S);
            menuItem11.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VZData.clear();
                        VZData.add(0.0,3.0);
                        VZData.add(-25.0,3.0);
                        vzpanel.update();
                    }
                });
            velMenu.add(menuItem11);
            JMenuItem menuItem12 = new JMenuItem("Const. Increasing");
            menuItem12.setMnemonic(KeyEvent.VK_I);
            menuItem12.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VZData.clear();
                        VZData.add(0.0,2.0);
                        VZData.add(-25.0,4.0);
                        vzpanel.update();
                    }
                });
            velMenu.add(menuItem12);
            JMenuItem menuItem13 = new JMenuItem("Const. Decreasing");
            menuItem13.setMnemonic(KeyEvent.VK_D);
            menuItem13.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VZData.clear();
                        VZData.add(0.0,4.0);
                        VZData.add(-25.0,2.0);
                        vzpanel.update();
                    }
                });
            velMenu.add(menuItem13);
            JMenuItem menuItem14 = new JMenuItem("Rapid Increase");
            menuItem14.setMnemonic(KeyEvent.VK_R);
            menuItem14.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VZData.clear();
                        VZData.add(0.0,2.0);
                        VZData.add(-5.0,2.3);
                        VZData.add(-7.0,3.7);
                        VZData.add(-25.0,6.0);
                        vzpanel.update();
                    }
                });
            velMenu.add(menuItem14);
            JMenuItem menuItem15 = new JMenuItem("LVZ");
            menuItem15.setMnemonic(KeyEvent.VK_L);
            menuItem15.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VZData.clear();
                        VZData.add(0.0,1.0);
                        VZData.add(-5.0,3.0);
                        VZData.add(-7.0,2.0);
                        VZData.add(-25.0,5.0);
                        vzpanel.update();
                    }
                });
            velMenu.add(menuItem15);
            JMenuItem menuItem16 = new JMenuItem("File \"veldata.txt\"");
            menuItem16.setMnemonic(KeyEvent.VK_D);
            menuItem16.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VZData.clear();
                        try{
                        // Open the file
                        FileInputStream fstream=new FileInputStream("veldata.txt");
                        // Get the object of DataInputStream
                        DataInputStream in = new DataInputStream(fstream);
                            BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String strLine;
                        // Read File Line By Line
                        while ((strLine = br.readLine()) != null)   {
                          // Print the content on the console
                          //System.out.println (strLine);
                          StringTokenizer st = new StringTokenizer(strLine);
                          //System.out.println(st.nextToken()); // depth
                          double z = -Double.valueOf(st.nextToken()).doubleValue();
                          //System.out.println(st.nextToken()); // velocity
                          double v = Double.valueOf(st.nextToken()).doubleValue();
                          VZData.add(z/1000.,v/1000.); // conv to km and km/s
                        }
                        // Close the input stream
                        in.close();
                        }catch (Exception exc){//Catch exception if any
                          System.err.println("Error: " + exc.getMessage());
                        }
                        vzpanel.update();
                    }
                });
            velMenu.add(menuItem16);            
        }
    }
    
    private class ZXPanel extends PlotPanel {
        public ZXPanel() {
            super("Range vs Depth","range (km)","depth (km)",ZXData,0.0,maxZXrange,-25.0,0.0);
            togglePlotPoints();
        }
    }
    
    private class TXPanel extends PlotPanel {
        public TXPanel() {
			super("","range (km)","time (s)",2,TXData,0.0,maxZXrange,0.0,15.0);
			if(cbxUseRV.isSelected()) {
				getPlotHandle().setTitle("TravelTime vs Range (rv="+
										 Double.parseDouble(redVelTxt.getText())+"km/s)");
			} else {
				getPlotHandle().setTitle("TravelTime vs Range");
			}
        }
    }
    
    private class PXPanel extends PlotPanel {
        public PXPanel() {
            super("P vs Range","range (km)","P",4,PXData,0.0,maxZXrange,0.0,1.0);
        }
    }
    
    private class TPPanel extends PlotPanel {
        public TPPanel() {
            super("TravelTime vs P (rv="+Double.parseDouble(redVelTxt.getText())+
                  "km/s)","P","time (s)",3,TPData,0.0,1.0,0.0,15.0);
        }
    }
    
    private class TauPPanel extends PlotPanel {
        public TauPPanel() {
            super("Tau-P plot","P","Tau",5,TauPData,0.0,1.0,0.0,10.0);
            add(new JSeparator(JSeparator.VERTICAL),BorderLayout.WEST);
        }
    }
    
    
    /** Set up Actions and connect them to toolbar buttons */
    private void createActionComponents(JToolBar toolBar) {
        
        JButton button = null;       // gets reused as tmp placeholder
        
        //toolBar.add(new JToolBar.Separator());
        
        //Clear Action:
        URL url = this.getClass().getResource("images/New24.gif");
        clearAction = 
            new AbstractAction("Clear", new ImageIcon(url)) {
                public void actionPerformed(ActionEvent e) {
                    clearData();
                    VZData.clear(); vzpanel.update();
                }
            };
        button = toolBar.add(clearAction);
        button.setSize(18,18);
        button.setText(""); //an icon-only button
        button.setToolTipText("Clear Data");
        
        //Properties Action:
        url = this.getClass().getResource("images/Edit24.gif");
        propertiesAction = 
            new AbstractAction("Properties", new ImageIcon(url)) {
                public void actionPerformed(ActionEvent e) {
                    openPropertiesDialog();
                }
            };
        button = toolBar.add(propertiesAction);
        button.setSize(18,18);
        button.setText(""); //an icon-only button
        button.setToolTipText("Edit Run Properties");
        
        //Run Action:
        url = this.getClass().getResource("images/Refresh24.gif");
        runAction = 
            new AbstractAction("Run", new ImageIcon(url)) {
                public void actionPerformed(ActionEvent e) {
                    runCalc();
                }
            };
        button = toolBar.add(runAction);
        button.setSize(18,18);
        button.setText(""); //an icon-only button
        button.setToolTipText("Run Calculation");
        
        //Stop Action:
        url = this.getClass().getResource("images/Stop24.gif");
        stopAction = 
            new AbstractAction("Stop", new ImageIcon(url)) {
                public void actionPerformed(ActionEvent e) {
                    runCalc();
                }
            };
        button = toolBar.add(stopAction);
        button.setSize(18,18);
        button.setText(""); //an icon-only button
        button.setToolTipText("Stop Calculation Before Finished");
        stopAction.setEnabled(false);

        //Only define ShowPlots action if not in plotmode that already shows them all
        if(plotMode.equals("VZ-ZX-TX") || plotMode.equals("VZ-ZX")) {
            toolBar.add(new JToolBar.Separator());
            //ShowPlots Action:
            url = this.getClass().getResource("images/Plots24.gif");
            showPlotsAction = 
                new AbstractAction("Show Plots", new ImageIcon(url)) {
                    public void actionPerformed(ActionEvent e) {
                        showRemainingPlots();
                    }
                };
            button = toolBar.add(showPlotsAction);
            button.setSize(18,18);
            button.setText(""); //an icon-only button
            button.setToolTipText("Show Window with Remaining Plots");
            showPlotsAction.setEnabled(false);
        }
            
        toolBar.add(Box.createHorizontalGlue());

        //Help Action:
        url = this.getClass().getResource("images/Help24.gif");
        helpAction =
            new AbstractAction("Help",
                               new ImageIcon(url)) {
                public void actionPerformed(ActionEvent e) {
                    openHelpDialog();
                }
            };
        button = toolBar.add(helpAction);
        button.setSize(18,18);
        button.setText(""); //an icon-only button
        button.setToolTipText("Help/Instructions");

        //About Action:
        url = this.getClass().getResource("images/About24.gif");
        aboutAction =
            new AbstractAction("About",
                               new ImageIcon(url)) {
                public void actionPerformed(ActionEvent e) {
                    openAboutDialog();
                }
            };
        button = toolBar.add(aboutAction);
        button.setSize(18,18);
        button.setText(""); //an icon-only button
        button.setToolTipText("About this applet");

        //toolBar.add(new JToolBar.Separator());



    }

    private void clearData(){
        // clear out plots from any previous runs
        for(int r=0; r<MAXRUNS; r++) {
            if(ZXData[r]!=null) {
                ZXData[r].clear();
            }
        }
        zxpanel.clear();
        TXData.clear(); txpanel.update();
        TPData.clear(); tppanel.update();
        PXData.clear(); pxpanel.update();
        TauPData.clear(); tauppanel.update();        

        // Disable show-remaining-plots (tx,px,tp) since data gone now
        if(plotMode.equals("VZ-ZX-TX") || plotMode.equals("VZ-ZX")) {
            showPlotsAction.setEnabled(false);
        }

    }

    private void openPropertiesDialog(){

        final JDialog propertiesDialog = new JDialog();
        propertiesDialog.setTitle("RayDemo Run Properties");
        Box propertiesPanel = new Box(BoxLayout.Y_AXIS);
        //String[] runTypeStrings = { "Sweep Rays", "Advance Together" };
        //cmbRunType = new JComboBox(runTypeStrings);
        //cmbRunType.addActionListener(new ActionListener() {
        //        public void actionPerformed(ActionEvent e) {
        //            runType = (String)cmbRunType.getSelectedItem();
        //        }
        //    });
        //JPanel p1 = new JPanel();
        //p1.add(cmbRunType);
        JPanel p2 = new JPanel();
        p2.add(new JLabel("Source Depth :"));
        p2.add(srcDepthTxt);
        JPanel p3 = new JPanel();
        p3.add(new JLabel("Angles (wrt vertical/down) :"));
        JPanel p4 = new JPanel();
        p4.add(new JLabel("1st:"));
        p4.add(startAngleTxt);
        p4.add(new JLabel("Last:"));
        p4.add(endAngleTxt);
        p4.add(new JLabel("Incr:"));
        p4.add(angleIncrTxt);
        JPanel p5 = new JPanel();
        p5.setLayout(new GridLayout(2,1));
        p5.add(p3); p5.add(p4);
        JPanel p6 = new JPanel();
        cbxUseRV.addActionListener( new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if(cbxUseRV.isSelected()) {
                    redVelTxt.setEnabled(true);
                    uservLbl.setEnabled(true);
                    redVelTxt.setText("5");
                }
                else {
                    redVelTxt.setEnabled(false);
                    uservLbl.setEnabled(false);
                    redVelTxt.setText("-");
				}
            }
		});
        p6.add(cbxUseRV);
        p6.add(uservLbl);
        p6.add(redVelTxt);
        JPanel p7 = new JPanel();
        JTextPane note = new JTextPane();
        note.setText("(Note if you change/disable the reduction\nvelocity you must "+
                     "rerun the calculation\nto see a change in the plots.)");
		note.setEnabled(false);
		note.setBackground(new Color(220,220,220,0));
        p7.add(note);
        JPanel p8 = new JPanel();
        Action okAction = new AbstractAction("Ok") {
            public void actionPerformed(ActionEvent e) {
				syncRedVel();
                propertiesDialog.dispose();
            }
        };
        p8.add(new JButton(okAction));
        //propertiesPanel.add(p1);
        propertiesPanel.add(p2);
        propertiesPanel.add(p5);
        propertiesPanel.add(p6);
        propertiesPanel.add(p7);
        propertiesPanel.add(p8);
        propertiesPanel.setBorder(new 
            TitledBorder(BorderFactory.createEtchedBorder(), "RayDemo Properties"));
        propertiesDialog.getContentPane().add(propertiesPanel);
        propertiesDialog.setSize(270,290);
        propertiesDialog.setVisible(true);
		propertiesDialog.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
				syncRedVel();
			}
		});				
    }
	public void syncRedVel() {
		if(cbxUseRV.isSelected()) {
			txpanel.getPlotHandle().setTitle(
				 "TravelTime vs Range (rv="+Double.parseDouble(redVelTxt.getText())+"km/s)");
			tppanel.getPlotHandle().setTitle(
				 "TravelTime vs P (rv="+Double.parseDouble(redVelTxt.getText())+"km/s)");
		} else {
			txpanel.getPlotHandle().setTitle("TravelTime vs Range");
			tppanel.getPlotHandle().setTitle("TravelTime vs P");
		}
		plotMatrix.repaint();
	}


    private void showRemainingPlots(){
        JDialog remainingPlotsWindow = new JDialog();
        remainingPlotsWindow.setTitle("RayDemo Plots");
		TXPanel txpanel2;
        if(plotMode.equals("VZ-ZX-TX")) {
		    txpanel2 = new TXPanel();
		} else {txpanel2 = txpanel;}
        Box box0 = new Box(BoxLayout.Y_AXIS);
        JPanel blankPanel = new JPanel();
        blankPanel.setPreferredSize(new Dimension(width/2,height/2));
        txpanel2.setPreferredSize(new Dimension(width/2,height/2));
        pxpanel.setPreferredSize(new Dimension(width/2,height/2));
        tppanel.setPreferredSize(new Dimension(width/2,height/2));
        tauppanel.setPreferredSize(new Dimension(width/2,height/2));
		if(cbxUseRV.isSelected()) {
			txpanel2.getPlotHandle().setTitle(
		    "TravelTime vs Range (rv="+Double.parseDouble(redVelTxt.getText())+"km/s)");
			tppanel.getPlotHandle().setTitle(
		    "TravelTime vs P (rv="+Double.parseDouble(redVelTxt.getText())+"km/s)");
		}
		else {
			txpanel2.getPlotHandle().setTitle("TravelTime vs Range");
			tppanel.getPlotHandle().setTitle("TravelTime vs P");
		}
        txpanel2.setVisible(true);
        pxpanel.setVisible(true);
        tppanel.setVisible(true);
        tauppanel.setVisible(true);        
        blankPanel.setVisible(true);
        Box box1 = new Box(BoxLayout.X_AXIS);
        Box box2 = new Box(BoxLayout.X_AXIS);
        box1.add(pxpanel);
        box1.add(tauppanel);   // box1.add(blankPanel);
        box2.add(txpanel2);
        box2.add(tppanel);
        box0.add(box1);
        box0.add(box2);
        remainingPlotsWindow.getContentPane().add(box0);
        txpanel2.update();
        tppanel.update();
        tauppanel.update();
        pxpanel.update();
        remainingPlotsWindow.setSize(new Dimension(width,height));
        remainingPlotsWindow.setVisible(true);
	}

    private void runCalc(){
        if(alreadyRunning) {
            alreadyRunning = false;
            stopCalc = true;
            stopAction.setEnabled(false);
            runAction.setEnabled(true);
        }
        else {
            clearData();
            VZData.sort();
            if(VZData.isEmpty()) {
                JOptionPane.showMessageDialog(contentPanel,
                    "You must enter some velocity data before the processing can run.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            else if(VZData.getNumPts()==1) {
                JOptionPane.showMessageDialog(contentPanel,
                    "You must enter more than one velocity point.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            else if( ((DataSeries.Point)(VZData.lastElement())).getX() < 0.0) {
                JOptionPane.showMessageDialog(contentPanel,
                    "Your velocity profile must have a zero-depth entry to run.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                alreadyRunning = true;
                stopCalc = false;
                stopAction.setEnabled(true);
                runAction.setEnabled(false);
                calcThread = new CalcThread(runType);
            }
        }
    }
    private void openHelpDialog(){
        JOptionPane.showMessageDialog(contentPanel,
            "Brief help entry for \"RayDemo\" geophysical raytrace applet:\n\n" +
            "1.) Right-clicking on the backgrounds of the plots brings up menu for those plots.\n\n" +
            "2.) Hovering over a toolbar button will bring up a description of button.\n\n" +
            "3.) You can click & drag to zoom in on plots, and return to orig bounds via menus.\n\n" +
            "4.) \"Quick-start\" steps to use:\n" +
            "   - right-click on depth-vel plot and choose a premade profile type.\n" +
            "   - optionally left-click additional points into the depth-vel plot.\n" +
            "   - click the Run (swirling arrows) toolbar button to calculate/display rays.\n" +
            "   - optionally click the Plots toolbar button to show TX,PX,TP plots.\n" +
            "   - click the Clear toolbar button to start again.\n" +
            "   - click the Edit Properties toolbar button to tailor run parameters.\n" +
            "\n",
            "Help", JOptionPane.QUESTION_MESSAGE);
    }
    private void openAboutDialog(){
        JOptionPane.showMessageDialog(contentPanel,
            "\"RayDemo\" geophysical raytrace applet\n" +
            "                   version 2.0\n" +
            "     by Andy Ganse, APL-UW, 2015\n" +
            "      aganse@apl.washington.edu\n" +
            "(includes code by P. Brodsky, APL-UW)",
            "About RayDemo", JOptionPane.INFORMATION_MESSAGE);
    }

    
    /** To close the application from window menu (when running as app) */
    private static class WL extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
    RayDemo applet = new RayDemo();
    Frame myFrame = new Frame("Ray Trace Demonstration");
    myFrame.addWindowListener(new WL());
    myFrame.add(applet, BorderLayout.CENTER);
    myFrame.setSize(700,570);  // comment out for web applet (how to check state?)
    applet.init();
    applet.start();
    myFrame.setVisible(true);
    }


    /** Thread-handling class for running calculation without freezing up the UI */
    private class CalcThread extends Thread {
        String runType;
        CalcThread(String runType) {
            this.start();
            this.runType=runType;
        }
        public void run() {
            if(runType=="Sweep Rays") doSweepCalc();
            else if(runType=="Advance Together") doTogetherCalc();
        }
    }


    private void doTogetherCalc() {
    /** not implemented yet */
    }


    private void doSweepCalc() {
        double t, p, timeIncr=0.2;
        double currentAngle, currentC, currentZ, currentX=0.0;
        double deltaZ, deltaX, lastC;
        
        // convert textfield entries to numbers
        double startAngle = Double.parseDouble(this.startAngleTxt.getText());
        double endAngle = Double.parseDouble(this.endAngleTxt.getText());
        double angleIncr = Double.parseDouble(this.angleIncrTxt.getText());
        // making so that srcDepth is always negative:
        double srcDepth = -Math.abs(Double.parseDouble(this.srcDepthTxt.getText()));
        
        
        // make sure VZdata is in sorted order for finding surface wavespeed.
        // depth is assumed negative downward, so this should put surface at end.
        VZData.sort();
        
        // preparation for interpolation below
        VZData.computeLinearGradients();
        
        // calc number of rays from start angle, end angle, and angle increment.
        // we will purposely leave out the 0.0 and 90.0 cases, but will take
        // care of that later.
        int numrays = (int)((startAngle-endAngle)/angleIncr);

        // loop over rays
        for(int r=0; r<numrays && stopCalc==false; r++) {
            
            ZXData[r] = new DataSeries();
            ZXData[r].add(0,srcDepth);
            currentZ=srcDepth;
            currentX=0.0;
            
            // calc takeoff angle for this ray
            currentAngle = startAngle - r*angleIncr;
            
            //System.out.println("on ray "+r+" out of "+numrays+
            //                 " with takeoff angle "+currentAngle);
            
            // we don't want angle=0 (straight down), as that won't return to surf
            if(currentAngle>0.0) {
                
                // set initial depth at source depth.  if source depth is zero we
                // go down just slightly because of zero-check for end-of-loop
                if(srcDepth==0.0) currentZ=-0.1;
                else currentZ = srcDepth;

                calcThread.yield();
                
                // get wavespeed at source depth
                currentC = VZData.interpolateY(currentZ);
                
                calcThread.yield();

                // calc p for this ray : just sin(takeoff angle)/initial vel
                p = sind(currentAngle)/currentC;
                
                // the code I put into loop below assumes horiz angle so:
                currentAngle=90.0-currentAngle;
                // note that core code in loop below originated from Pete
                // Brodsky, APL-UW...
                
                // loop over time t for this ray :
                for(t=0.0; currentZ<=0.0 && t<250.0 && stopCalc==false; t+=timeIncr) {
                    
                    // propagate ray forward one timestep:
                    deltaZ = (-1)*currentC*sind( currentAngle )*timeIncr;
                    currentZ += deltaZ;
                    deltaX = currentC*cosd( currentAngle )*timeIncr;
                    
                    if( currentZ <= 0.0 ) {
                        currentX += deltaX;
                        
                        // Update wavespeed
                        lastC = currentC;
                        currentC = VZData.interpolateY( currentZ ); //interp wavespd
                        
                        // Update Theta via Snell's law.  
                        // Be sure to maintain sign, because arccos won't.
                        // Also, handle limiting case where rays curve back up
                        double arg = currentC*cosd(currentAngle)/lastC;
                        if( arg < 1 )
                            currentAngle = sign(currentAngle)*arccos( arg );
                        else
                            currentAngle = -sign(currentAngle)*arccos( 2-arg );

                        // update zxpanel with new zx pt
                        ZXData[r].add(currentX,currentZ);
                        //zxpanel.updateMulti();

                    }
                    
                    calcThread.yield();

                }
                
                if(currentZ>=0.0) {
                    //apply reduction velocity to traveltime if specified:
                    if(cbxUseRV.isSelected()) {
                        double redvel = Double.parseDouble(this.redVelTxt.getText());
                        t -= currentX/redvel;
                    }

                    // update txpanel, tppanel, pxpanel with new t/x/p pt,
					// show updates in plots at each calc iteration:
                    //if(txpanel.isVisible()) {TXData.add(currentX,t); txpanel.update();}
                    //if(tppanel.isVisible()) {TPData.add(p,t); tppanel.update();}
                    //if(pxpanel.isVisible()) {PXData.add(currentX,p); pxpanel.update();}

                    // update txpanel, tppanel, pxpanel with new t/x/p pt
                    //if(txpanel.isVisible()) {TXData.add(currentX,t);}
                    //if(tppanel.isVisible()) {TPData.add(p,t);}
                    //if(pxpanel.isVisible()) {PXData.add(currentX,p);}
                    TXData.add(currentX,t);
                    TPData.add(p,t);
                    PXData.add(currentX,p);
                    TauPData.add(p,t-p*currentX);
                }
            }

        }
        zxpanel.updateMulti();
        if(txpanel.isVisible()) {txpanel.update();}
        if(tppanel.isVisible()) {tppanel.update();}
        if(pxpanel.isVisible()) {pxpanel.update();}
        if(tauppanel.isVisible()) {tauppanel.update();}


        // Pause just a sec before returning to prevent accidental double-click from restarting
        //try{Thread.sleep(2000);} catch(InterruptedException e){}
        
        // "Run" button was changed to "Stop" button during run, so change back now
        stopAction.setEnabled(false);
        runAction.setEnabled(true);
        alreadyRunning=false;

        // Enable show-remaining-plots (tx,px,tp) now that data available
        if(plotMode.equals("VZ-ZX-TX") || plotMode.equals("VZ-ZX")) {
            showPlotsAction.setEnabled(true);
        }
    }



    //conversion factor "degress per radian" */
    private static final double DPR = 180./Math.PI;
    //Purpose: Cosine of angle in degrees
    private static double cosd( double Angle ) { 
    return( Math.cos( Angle/DPR ) );
    }
    //Purpose: Sine of angle in degrees
    private static double sind( double Angle ) {
    return( Math.sin( Angle/DPR ) );
    }
    //Purpose: Tangent of angle in degrees
    private static double tand( double Angle ) {
    return( Math.tan( Angle/DPR ) );
    }
    //Purpose: Arc-Cosine of angle in degrees
    private static double arccos( double arg ) {
    return( Math.acos( arg ) * DPR );
    }
    //Purpose: Arc-Sine of angle in degrees
    private static double arcsin( double arg ) {
    return( Math.asin( arg ) * DPR );
    }
    //Purpose: 2-argument arc-tangent, in degrees
    private static double arctan( double y, double x ) {
    double temp = Math.atan2( y, x ) * DPR;
    return( temp < 0.0 ? temp+360. : temp );
    }
    //Purpose: Determine sign of a double,int.  Return + or - 1 as per sign.
    private static int sign( double x ) { return ( x >=0 ? 1:-1 ); }
    private static int sign( int x ) { return sign( (double)x ); }

    public String[][] getParameterInfo() {
        String[][] info = {
           // Parameter Name     Kind of Value   Description
           {"demowidth",         "int",          "desired pixel width of applet"},
           {"plotmode",          "String",       "VZ-ZX, VZ-ZX-TX, VZ-ZX-TX-PX-TP"}
        };
        return info;
    }

}
