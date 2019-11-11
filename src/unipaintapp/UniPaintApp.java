
package unipaintapp;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class UniPaintApp extends JFrame{

    //variables 
    private int ctrlPanelWidth = 300;
    private int canvasDefaultWidth = 400;
    private int canvasDefaultHeight = 400;
    
    //colour and drawing type
    private Color selectedColour = new Color(0.0F, 0.0F, 0.0F);
    private String drawingTool = "Line";
    
    //freehand
    private final int maxFreehandPixels = 10000;
    private Color[] freehandColour = new Color[maxFreehandPixels];
    private int[][] fxy = new int[maxFreehandPixels][3];
    private int freehandPixelCount = 0;
    private int freehandThickness = 10;
    
    //line
    private final int maxLineCount = 10;
    private int currentLineCount= 0;
    private Color[] lineColour = new Color[maxLineCount];
    private int[][] lxy = new int[maxLineCount][4];
   
    private int pointStartX,pointStartY,pointEndX,pointEndY;
    
    //rectangle
    private final int maxRectangles = 10;
    private Color[] rectangleColour = new Color[maxRectangles];
    private int[][] rxy = new int[maxRectangles][4];
    private int rectangleCount = 0;
    private int x1;
    private int y1;
    

    private Canvas canvas;
    private JPanel ctrlPanel;
    private JTextArea msgBox;
    private JMenuBar menuBar;

    private JButton colourButton, clearButton, animateButton;
    private JRadioButton lineRadioButton, rectangleRadioButton, circleRadioButton, freehandRadioButton;
    private JLabel mousePointer;
    private JCheckBox coarseCheckBox, fineCheckBox;
    private JSlider drawSize;
    
    class Canvas extends JPanel {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw(g);
        }
    }
    public UniPaintApp() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1900, 1000));

        //canvas
        canvas = new Canvas();
        canvas.setBorder(new TitledBorder(new EtchedBorder(), "Canvas"));
        canvas.setPreferredSize(new Dimension(canvasDefaultWidth, canvasDefaultHeight));
        canvas.addMouseMotionListener(new CanvasMouseMotionListener());
        canvas.addMouseListener(new CanvasMouseListener());
        canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        add(canvas, BorderLayout.CENTER);

        //control panel
        ctrlPanel = new JPanel();
        //ctrlPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        ctrlPanel.setBorder(new TitledBorder(new EtchedBorder(), "Control Panel"));
        ctrlPanel.setPreferredSize(new Dimension(ctrlPanelWidth, 100));

        //Drawing position label
        JPanel coordinatesPointer = new JPanel();
        coordinatesPointer.setBorder(new TitledBorder(new EtchedBorder(), "Drawing Position"));
        coordinatesPointer.setPreferredSize(new Dimension(ctrlPanelWidth - 50, 60));
        mousePointer = new JLabel();
        coordinatesPointer.add(mousePointer);
        ctrlPanel.add(coordinatesPointer);

        //drawing tool radio buttons
        JPanel drawingToolsPanel = new JPanel();
        drawingToolsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Drawing Tools"));
        drawingToolsPanel.setPreferredSize(new Dimension(ctrlPanelWidth - 50, 120));
        drawingToolsPanel.setLayout(new BoxLayout(drawingToolsPanel, BoxLayout.PAGE_AXIS));
        lineRadioButton = new JRadioButton("Line");
        rectangleRadioButton = new JRadioButton("Rectangle");
        circleRadioButton = new JRadioButton("Circle");
        freehandRadioButton = new JRadioButton("Freehand");
        //grouping buttons so only one can be selected at a time
        ButtonGroup group = new ButtonGroup();
        group.add(lineRadioButton);
        group.add(rectangleRadioButton);
        group.add(circleRadioButton);
        group.add(freehandRadioButton);
        //button selected
        lineRadioButton.addActionListener(new RadioButtonListener());
        rectangleRadioButton.addActionListener(new RadioButtonListener());
        circleRadioButton.addActionListener(new RadioButtonListener());
        freehandRadioButton.addActionListener(new RadioButtonListener());
        
        drawingToolsPanel.add(lineRadioButton);
        drawingToolsPanel.add(rectangleRadioButton);
        drawingToolsPanel.add(circleRadioButton);
        drawingToolsPanel.add(freehandRadioButton);
        ctrlPanel.add(drawingToolsPanel);
        
        //colour selector
        JPanel colourButtonPanel = new JPanel();
        colourButtonPanel.setBorder(new TitledBorder(new EtchedBorder(), "Colour"));
        colourButtonPanel.setPreferredSize(new Dimension(ctrlPanelWidth - 50, 140));
        colourButton = new JButton();
        colourButton.setPreferredSize(new Dimension(ctrlPanelWidth - 200, 100));
        colourButton.addActionListener(new ButtonListener());
        colourButtonPanel.add(colourButton);
        ctrlPanel.add(colourButtonPanel);

        //thickness slider
        JPanel thicknessPanel = new JPanel();
        thicknessPanel.setBorder(new TitledBorder(new EtchedBorder(), "Freehand Size"));
        thicknessPanel.setPreferredSize(new Dimension(ctrlPanelWidth - 50, 100));
        drawSize = new JSlider(0, 20);
        drawSize.setMajorTickSpacing(5);
        drawSize.setMinorTickSpacing(1);
        drawSize.setPaintTicks(true);
        drawSize.setPaintLabels(true);
        drawSize.addChangeListener(new FreehandSliderListener());
        thicknessPanel.add(drawSize);
       
        ctrlPanel.add(thicknessPanel);

        //grid checkbox
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setBorder(new TitledBorder(new EtchedBorder(), "Grid"));
        checkBoxPanel.setPreferredSize(new Dimension(ctrlPanelWidth - 50, 80));
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.PAGE_AXIS));
        coarseCheckBox = new JCheckBox("Coarse");
        fineCheckBox = new JCheckBox("Fine");
        fineCheckBox.addChangeListener(new MyCheckBoxListener());
        coarseCheckBox.addChangeListener(new MyCheckBoxListener());
        checkBoxPanel.add(coarseCheckBox);
        checkBoxPanel.add(fineCheckBox);
        ctrlPanel.add(checkBoxPanel);

        //Buttons
        clearButton = new JButton("Clear Canvas");
        animateButton = new JButton("Animate");
        clearButton.setPreferredSize(new Dimension(ctrlPanelWidth - 50, 60));
        animateButton.setPreferredSize(new Dimension(ctrlPanelWidth - 50, 60));
        clearButton.addActionListener(new ClearButtonListener());
        ctrlPanel.add(clearButton);
        ctrlPanel.add(animateButton);
        add(ctrlPanel, BorderLayout.LINE_START);

        //message area
        msgBox = new JTextArea();
        msgBox.setEditable(false);
        msgBox.setBorder(new TitledBorder(new EtchedBorder(), "Message Box"));
       // msgBox.setBackground(null);
        msgBox.setPreferredSize(new Dimension(200, 200));
        add(msgBox, BorderLayout.PAGE_END);

        //JMenuBar
        //save
        menuBar = new JMenuBar();
        JMenu fileBar = new JMenu("File");
        JMenuItem fileSaveMenuItem = new JMenuItem("Save");
        JMenuItem fileLoadMenuItem = new JMenuItem("Load");
        JMenuItem fileExitMenuItem = new JMenuItem("Exit");
        fileBar.add(fileSaveMenuItem);
        fileBar.add(fileLoadMenuItem);
        fileBar.add(fileExitMenuItem);
        menuBar.add(fileBar);
        //help
        JMenu helpBar = new JMenu("Help");
        JMenuItem helpAboutMenuItem = new JMenuItem("About");
        helpBar.add(helpAboutMenuItem);
        menuBar.add(helpBar);

        add(menuBar, BorderLayout.PAGE_START);

        pack();
        setVisible(true);
    }

    void draw(Graphics g) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

     

        if (fineCheckBox.isSelected()) {
            g.setColor(new Color(0.8F, 0.8F, 0.8F));

            for (int i = 0; i < w; i += 10) { //vertical lines
                g.drawLine(i, 0, i, h);
            }
            for (int j = 0; j < h; j += 10) { //horizontal lines 
                g.drawLine(0, j, w, j);
            }
        }
        if (coarseCheckBox.isSelected()) {
            g.setColor(new Color(0.6F, 0.6F, 0.6F));
            for (int i = 0; i < w; i += 50) {
                g.drawLine(i, 0, i, h);
            }
            for (int j = 0; j < h; j += 50) {
                g.drawLine(0, j, w, j);
            }

        }
        //freehand
        for (int i = 0; i < freehandPixelCount; i++) {
            g.setColor(freehandColour[i]);
            g.fillRect(fxy[i][0], fxy[i][1], fxy[i][2], fxy[i][2]);
        }
        
        //line 
        for (int i = 0; i <= currentLineCount; i++) {
            g.setColor(lineColour[i]);
            g.drawLine(lxy[i][0], lxy[i][1], lxy[i][2], lxy[i][3]);
        }
           
        //rectangle
        for (int i = 0; i < rectangleCount; i++) {
            g.drawRect(rxy[i][0], rxy[i][1], rxy[i][2], rxy[i][3]);
        }
    }

    class CanvasMouseMotionListener implements MouseMotionListener {
    //    @Override
        public void mouseMoved(MouseEvent e) {
            mousePointer.setText(String.format("%04dpx, %04dpx", e.getX(), e.getY()));
            switch(drawingTool){
                case "Line":
//                    pointEndX = e.getX();
//                    pointEndY = e.getY();
//                    drawLineB(e,pointEndX,pointEndY); 
                    break;
                case "Rectangle":
                    break;
                case "Circle":
                    break;
                case "Freehand":
                    break;
            }
        }
    //    @Override
        public void mouseDragged(MouseEvent e) {
            switch (drawingTool) {
                case "Line":
                    drawLineDrag(e); 
                    break;
                case "Rectangle":
             
                    break;
                case "Oval":
                    break;
                case "Freehand":
                    drawFreehand(e, freehandThickness, selectedColour);
                    break;
            }
         mouseMoved(e);
         canvas.repaint();
        }
    }

    class CanvasMouseListener implements MouseListener {

        public void mousePressed(MouseEvent e) {
            switch (drawingTool) {
                case "Line":
                    drawLinePress(e,selectedColour);
                    break;
                case "Rectangle":
                  
                case "Oval":
                    break;
                case "Freehand":
                    drawFreehand(e, freehandThickness, selectedColour);
                    
                    break;
            }
        }

        public void mouseReleased(MouseEvent e) {
            switch (drawingTool) {
                case "Line":
                   drawLineRelease(e);
                    break;
                case "Rectangle":
                 
                    break;
                case "Oval":
                    break;
                case "Freehand":
                    
                    break;
            }
        }

        public void mouseClicked(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }
    }

    class MyCheckBoxListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            canvas.repaint();
        }
    }

    class RadioButtonListener implements ActionListener {
   //     @Override
        public void actionPerformed(ActionEvent e) {
            if (lineRadioButton.isSelected()) {
                drawingTool = "Line";
                //msgBox.append(drawingTool);
            } else if (rectangleRadioButton.isSelected()) {
                drawingTool = "Rectangle";
               // msgBox.append(drawingTool);
            } else if (circleRadioButton.isSelected()) {
                drawingTool = "Circle";
               // msgBox.append(drawingTool);
            } else {
                drawingTool = "Freehand";
               //  msgBox.append(drawingTool);
            }
           
        }
    }

    class ButtonListener implements ActionListener {
        //@Override
        public void actionPerformed(ActionEvent e) {
            JColorChooser colourPicker = new JColorChooser(selectedColour);
            Color newColour = colourPicker.showDialog(null, "Choose new Drawing colour", selectedColour);
            selectedColour = newColour;
     
         
            colourButton.setBackground(newColour);
        }
    }
    class ClearButtonListener implements ActionListener {
        //@Override
        public void actionPerformed(ActionEvent e) {
            canvas.repaint();
            freehandPixelCount = 0;
            rectangleCount = 0;
        }
    }

    public void drawFreehand(MouseEvent e, int freehandThickness, Color selectedColour) {
        freehandColour[freehandPixelCount] = selectedColour;
        fxy[freehandPixelCount][0] = e.getX(); //x coordinate
        fxy[freehandPixelCount][1] = e.getY();// y coordinate
        fxy[freehandPixelCount][2] = freehandThickness; //dimension
        freehandPixelCount++;
        msgBox.setText("");
        msgBox.append("Remaining Pixels: "+(maxFreehandPixels-freehandPixelCount));
    }
    
    public void drawLinePress(MouseEvent e, Color selectedColour){
        if (currentLineCount<maxLineCount){
            lineColour[maxLineCount]= selectedColour;
            lxy[maxLineCount][0] = e.getX();
            lxy[maxLineCount][1] = e.getY();
        } else{
          
        }
     
    }
    
    
    public void drawLineDrag(MouseEvent e){
        if (currentLineCount<maxLineCount){
            lxy[maxLineCount][2] = e.getX();
            lxy[maxLineCount][3] = e.getY();
           }else{
               
           }
         canvas.repaint(); 

    }
        
    public void drawLineRelease(MouseEvent e){
        if (currentLineCount<maxLineCount){
            lxy[maxLineCount][2] = e.getX();
            lxy[maxLineCount][3] = e.getY();
        } else {
            
         }
        currentLineCount++;

    }
  
    public void drawRectangle(MouseEvent e) {
       // rectangleColour[rectangleCount] = selectedColour;
     
        rxy[rectangleCount][0] = e.getX(); //x coordinate
        rxy[rectangleCount][1] = e.getY();// y coordinate
        x1=e.getX();
        y1=e.getY();
        rxy[rectangleCount][2] = e.getX(); //width
        rxy[rectangleCount][3] = e.getY(); //width
        rectangleCount++;
    }
    

    class FreehandSliderListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
         
            freehandThickness = drawSize.getValue();
         
        }
    }

    public static void main(String[] args) {
        UniPaintApp framePanels = new UniPaintApp();

    }

}




