import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.io.*;
import javax.imageio.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PaintProgram extends JPanel implements MouseMotionListener, ActionListener,MouseListener,ChangeListener{
    private ArrayList<Point> points;
    private ArrayList<ArrayList<Point>> lines;
    private ArrayList<Shape> shapes;
    private Stack<ArrayList<Point>> undoLines;
    private Stack<Shape> undoShapes;
    private Stack<String> commandOrder;
    private Stack<String> undoCommandOrder;
    JFrame frame;
    JMenuBar bar;
    JMenu menu,file;
    JMenuItem save,load;
    JButton[] colorOptions;
    Color[] colors;
    JColorChooser colorChooser;
    Color currentColor;
    JScrollBar penWidth;
    JButton freeLineOption,rectangleOption,ovalOption,redo,undo;
    boolean freeLineOn = true;
    boolean rectangleOn = false;
    int currX=0,currY=0,currWidth=0,currHeight=0;
    boolean first = true;
    Shape currentShape;
    ImageIcon undoImg,redoImg,freeLineImg,rectImg;
    JFileChooser fileChooser;
    BufferedImage loadedImage;
    public PaintProgram(){
        points = new ArrayList<Point>();
        lines = new ArrayList<ArrayList<Point>>();
        shapes = new ArrayList<Shape>();
        undoLines = new Stack<ArrayList<Point>>();
        undoShapes = new Stack<Shape>();
        commandOrder = new Stack<String>();
        undoCommandOrder = new Stack<String>();
        file = new JMenu("File");
        save = new JMenuItem("Save");
        save.addActionListener(this);
        load = new JMenuItem("Load");
        load.addActionListener(this);
        file.add(save);
        file.add(load);
        frame = new JFrame("Paint Program");
        frame.add(this);
        bar = new JMenuBar();
        bar.add(file);
        menu = new JMenu("Colors");
        colorOptions = new JButton[4];
        colors = new Color[]{Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
        currentColor = colors[0];
        colorChooser = new JColorChooser();
        colorChooser.getSelectionModel().addChangeListener(this);
        menu.setLayout(new GridLayout(1, 4));
        for(int x=0;x<colorOptions.length;x++){
            colorOptions[x] = new JButton();
            colorOptions[x].addActionListener(this);
            colorOptions[x].putClientProperty("colorIndex", x);
            colorOptions[x].setBackground(colors[x]);
            colorOptions[x].setOpaque(true);
            colorOptions[x].setBorderPainted(false);

        }
        menu.add(colorChooser);
        freeLineOption = new JButton();
        rectangleOption = new JButton();
        freeLineOption.addActionListener(this);
        rectangleOption.addActionListener(this);

        redo = new JButton();
        undo = new JButton();
        redo.addActionListener(this);
        undo.addActionListener(this);



        freeLineImg = new ImageIcon("freeline.png");
        freeLineImg = new ImageIcon(freeLineImg.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH));
        freeLineOption.setIcon(freeLineImg);
        freeLineOption.setFocusPainted(false);

        rectImg = new ImageIcon("rectangle.png");
        rectImg = new ImageIcon(rectImg.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH));
        rectangleOption.setIcon(rectImg);
        rectangleOption.setFocusPainted(false);

        undoImg = new ImageIcon("undo.png");
        undoImg = new ImageIcon(undoImg.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH));
        undo.setIcon(undoImg);
        undo.setFocusPainted(false);

    	redoImg = new ImageIcon("redo.png");
        redoImg = new ImageIcon(redoImg.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH));
        redo.setIcon(redoImg);
        redo.setFocusPainted(false);


		bar.add(redo);
		bar.add(undo);
        bar.add(freeLineOption);
        bar.add(rectangleOption);
		penWidth = new JScrollBar(JScrollBar.HORIZONTAL,1,0,1,50);
		bar.add(penWidth);
        bar.add(menu);
        this.addMouseMotionListener(this);

		String currDir = System.getProperty("user.dir");
        fileChooser = new JFileChooser();

        frame.add(bar, BorderLayout.NORTH);
        frame.setSize(900, 700);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	public void paintComponent(Graphics g){

		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g.setColor(Color.BLACK);
		g.fillRect(0,0,frame.getWidth(),frame.getHeight());

		if(loadedImage!=null){
			g.drawImage(loadedImage,0,0,null);
		}


		for(Shape s: shapes){
			g.setColor(s.getColor());
			g2.setStroke(new BasicStroke(s.getPenWidth()));

			if(s instanceof Block){
				g2.draw(((Block)s).getRect());
			}
		}

		if(freeLineOn){

			for(int x = 0; x < points.size()-1; x++){
				Point p1 = points.get(x);
				Point p2 = points.get(x+1);
				g.setColor(p1.getColor());
				g2.setStroke(new BasicStroke(p1.getPenWidth()));
				g.drawLine(p1.getX(),p1.getY(),p2.getX(),p2.getY());

			}
		}

		if  (rectangleOn){
			g.setColor(currentColor);
			g2.setStroke(new BasicStroke(currentShape.getPenWidth()));
			g2.draw(((Block)currentShape).getRect());
		}


	}

	public BufferedImage createImage(){

		int width = this.getWidth();
		int height = this.getHeight();
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		this.paint(g2);
		g2.dispose();
		return img;
	}

	public void stateChanged(ChangeEvent e){

		currentColor=colorChooser.getColor();

	}

	public void actionPerformed(ActionEvent e){

		for(int x=0;x<colorOptions.length;x++){

			if(e.getSource() == colorOptions[x]){
				currentColor = colors[x];
			}
		}

		if(e.getSource() == freeLineOption){
			freeLineOn = true;
			rectangleOn = false;
			freeLineOption.setBackground(Color.LIGHT_GRAY);
			rectangleOption.setBackground(null);
		}

		if(e.getSource() == rectangleOption){

			freeLineOn = false;
			rectangleOn = true;

			rectangleOption.setBackground(Color.LIGHT_GRAY);
			freeLineOption.setBackground(null);

		}

		if(e.getSource() == undo){
			if(commandOrder.size()>0){

				String lastCommand = commandOrder.pop();
				undoCommandOrder.push(lastCommand);
				if(lastCommand.equals("freeLine")){

					if(lines.size()>0){
						undoLines.push(lines.remove(lines.size()-1));
						repaint();
					}
				}
				if(lastCommand.equals("shape")){
					if(shapes.size()>0){
						undoShapes.push(shapes.remove(shapes.size()-1));
						repaint();
					}
				}
			}
		}

		if(e.getSource() == redo){
			if(commandOrder.size()>0){

				String lastRemovedCommand = undoCommandOrder.pop();
				commandOrder.push(lastRemovedCommand);
				if(lastRemovedCommand.equals("freeLine")){

					if(undoLines.size()>0){
						lines.add(undoLines.pop());
						repaint();
					}
				}
				if(lastRemovedCommand.equals("shape")){
					if(undoShapes.size()>0){
						shapes.add(undoShapes.pop());
						repaint();
					}
				}
			}
		}

		if(e.getSource() == save){
			FileFilter filter = new FileNameExtensionFilter("*.png","png");
			fileChooser.setFileFilter(filter);
			if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
				File file = fileChooser.getSelectedFile();
				try
				{
					String st=file.getAbsolutePath();
					if(st.indexOf(".png")>=0){
						st=st.substring(0,st.length()-4);
					}
					ImageIO.write(createImage(),"png",new File(st+".png"));
				}catch(IOException exc)
				{
				}
			}
		}

		if(e.getSource() == load){
			fileChooser.showOpenDialog(null);
			File imgFile = fileChooser.getSelectedFile();

			try
			{
				loadedImage = ImageIO.read(imgFile);
			}catch(IOException exc)
			{
			}

			lines = new ArrayList<ArrayList<Point>>();
			undoLines = new Stack<ArrayList<Point>>();
			points = new ArrayList<Point>();
			shapes = new ArrayList<Shape>();
			undoShapes = new Stack<Shape>();
			commandOrder = new Stack<String>();
			undoCommandOrder = new Stack<String>();
			repaint();
		}




	}

	public void mouseReleased(MouseEvent e){

		if(freeLineOn){
			lines.add(points);
			points = new ArrayList<Point>();
			commandOrder.push("freeLine");
		}

		if(rectangleOn){
			commandOrder.push("shape");
		}
		repaint();

	}


	public void mouseDragged(MouseEvent e){
		if(freeLineOn){
			points.add(new Point(e.getX(), e.getY(), currentColor, (int)penWidth.getValue()));

		}

		if(rectangleOn){

			if(first){

				currX = e.getX();
				currY = e.getY();
				first = false;
				currentShape = new Block(currX, currY, currentColor,0,0,(int)penWidth.getValue());
				shapes.add(currentShape);
			}else{
				currWidth = Math.abs(e.getX()-currX);
				currHeight = Math.abs(e.getY()-currY);
				currentShape.setWidth(currWidth);
				currentShape.setHeight(currHeight);
			}

		}

		repaint();
	}
	public void mouseClicked(MouseEvent e){

	}

	public void mouseExited(MouseEvent e){

	}

	public void mouseEntered(MouseEvent e){
	}

	public void mousePressed(MouseEvent e){
	}

	public void mouseMoved(MouseEvent e){
	}

	public static void main(String []args){
		PaintProgram app = new PaintProgram();
	}


	public class Point{
		private int x;
		private int y;
		int width;
		int height;
		private int penWidth;
		Color color;

		public Point(int x, int y, Color color, int penWidth){
			this.x=x;
			this.y=y;
			this.color = color;
			this.penWidth=penWidth;
			height = 0;
			width = 0;

		}

		public Point(int x, int y, Color color, int height, int width, int penWidth){
			this.x=x;
			this.y=y;
			this.color = color;
			this.penWidth=penWidth;
			this.height = height;
			this.width = width;

		}


		public int getX(){
			return x;
		}

		public int getY(){
			return y;
		}

		public Color getColor(){
			return color;
		}

		public int getPenWidth(){
			return penWidth;
		}

		public int getHeight(){

			return height;
		}

		public int getWidth(){
			return width;
		}

		public void setHeight(int height){
			this.height = height;
		}

		public void setWidth(int width){
			this.width = width;
		}

		public class Block extends Shape{

			public Block(int x, int y, Color color, int height, int width, int penWidth){
				super(x,y,color,height,width,penWidth);
			}

			public Rectangle getRect(){
				return new Rectangle(getX(), getY(), getWidth(), getHeight());
			}

		}

	}

	public class Shape{
		private int x;
		private int y;
		int width;
		int height;
		private int penWidth;
		Color color;



		public Shape(int x, int y, Color color, int height, int width, int penWidth){
			this.x=x;
			this.y=y;
			this.color = color;
			this.penWidth=penWidth;
			this.height = height;
			this.width = width;

		}


		public int getX(){
			return x;
		}

		public int getY(){
			return y;
		}

		public Color getColor(){
			return color;
		}

		public int getPenWidth(){
			return penWidth;
		}

		public int getHeight(){

			return height;
		}

		public int getWidth(){
			return width;
		}

		public void setHeight(int height){
			this.height = height;
		}

		public void setWidth(int width){
			this.width = width;
		}



	}

	public class Block extends Shape{

		public Block(int x, int y, Color color, int height, int width, int penWidth){
			super(x,y,color,height,width,penWidth);
		}

		public Rectangle getRect(){
			return new Rectangle(getX(), getY(), getWidth(), getHeight());
		}

	}





}