package viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MainProgram extends JFrame{
	
	private DrawingPanel dp;
	private FileInputStream fis;
	private JFileChooser fc;
	private Timer timer;
	
	private String magicNo = "";
	private int width;
	private int height;
	private int maxNum;
	private int[] image;
	
	private int cursor;
	private Color[] colors;
	private double zoomScale = 1;
	private double dragDistanceX = 0;
	private double dragDistanceY = 0;
	private double mouseX;
	private double mouseY;
	
	private JButton fileChooser;
	private JButton animation1;
	private boolean ani1 = false;
	private JButton animation2;
	private boolean ani2 = false;
	private JButton animation3;
	private boolean ani3 = false;
	
	private int tempWidth;
	private int tempWidth2;
	private int tempHeight;
	private int spaceOnX;
	private int spaceOnY;
	
	MainProgram() {
		
		File dir = new File("/Users/Selim/Desktop/Java sources/Image Viewer Project");
		FileFilter filter = new ImageFileFilter();
		
		fc = new JFileChooser(dir);
		fc.setFileFilter(filter);
		
		timer = new Timer(10, new TimerListener());
				
		dp = new DrawingPanel();
			
		animation1 = new JButton("Animation 1");
		animation1.setBounds(615, 20, 180, 160);
		
		animation1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spaceOnX=150;
				spaceOnY=150;
				ani1 = true;
				ani2 = false;
				ani3 = false;
			}	
		});
		
		animation2 = new JButton("Animation 2");
		animation2.setBounds(615, 220, 180, 160);
		
		animation2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tempHeight = height;
				tempWidth = 0;
				ani2 = true;
				ani1 = false;
				ani3 = false;
			}	
		});
		
		animation3 = new JButton("Animation 3");
		animation3.setBounds(615, 420, 180, 160);
		
		animation3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tempWidth = 1;
				tempWidth2 = width-2;
				ani3 = true;
				ani1 = false;
				ani2 = false;
			}	
		});
		
		fileChooser = new JButton("Choose an image");
		fileChooser.setBounds(150, 620, 300, 100);		
		
		fileChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ani1 = false;
				ani2 = false;
				ani3 = false;
				fc.showOpenDialog(null);
				
				try {
					fis = new FileInputStream(fc.getSelectedFile());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
								
				getImage();
				dp.repaint();
				timer.start();
			}
		});
		this.setLayout(null);
		
		dp.setBackground(new Color(200,200,200));
		dp.setSize(600, 600);
		this.add(dp);
		
		this.add(fileChooser);
		this.add(animation1);
		this.add(animation2);
		this.add(animation3);
		
		this.setVisible(true);
		this.setSize(825, 800);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
				
		dp.addMouseWheelListener(new MouseHandler());
		dp.addMouseMotionListener(new MouseHandler());
		dp.addMouseListener(new MouseHandler());
	}
	
	private String getMagicNumber() {
		byte[] mn = new byte[2];
		try {
			fis.read(mn);
		} catch (IOException e) {}	
		return new String(mn);
	}
	
	private void skipWhiteSpace() {
		try {
			cursor = fis.read();			
			while(Character.isWhitespace(cursor)) {
				cursor = fis.read();			
			}			
		} catch (IOException e) {}
	}
	
	private int readInteger() {	
		String sizes = "";
		while(!Character.isWhitespace(cursor)) {
			sizes = sizes + (cursor - '0');
			try {
				cursor = fis.read();
			} catch (IOException e) {}
		}
		return Integer.parseInt(sizes);
	}	
	
	private void getImage() {	
		
		ani1 = false;
		ani2 = false;
		ani3 = false;
		
		magicNo = getMagicNumber();
		skipWhiteSpace();
		
		width = readInteger();
		skipWhiteSpace();
		
		height = readInteger();
		skipWhiteSpace();
				
		if(!magicNo.equals("P1") && !magicNo.equals("P4")) {
			maxNum = readInteger();
			skipWhiteSpace();
		}		
		
		colors = new Color[width*height];
		image = new int[width*height];
		
		if(magicNo.equals("P1")) {
			for(int i=0; i<width*height; i++) {
				image[i] = readInteger();
				colors[i] = image[i] == 0 ? Color.WHITE : Color.BLACK;
				skipWhiteSpace();
			}
		}
		if(magicNo.equals("P2")) {
			for(int i=0; i<width*height; i++) {
				image[i] = readInteger();
				int color = (255/maxNum)*image[i];
				colors[i] = new Color(color, color, color);
				skipWhiteSpace();
			}
		}
		if(magicNo.equals("P3")) {
			for(int i=0; i<width*height; i++) {
				int r,g,b;
				r = (255/maxNum)*readInteger();
				skipWhiteSpace();
				g = (255/maxNum)*readInteger();
				skipWhiteSpace();
				b = (255/maxNum)*readInteger();
				skipWhiteSpace();
				colors[i] = new Color(r,g,b);
			}
		}
		if(magicNo.equals("P4")) {
			int bitCounter = 128;
			int indexCounter = 0;
			
			byte[] imageByte = new byte[(width/8 + 1)*height];
			imageByte[0] = (byte)cursor;
			
			for(int i=1; i<(width/8 + 1)*height; i++) {
				try {
					imageByte[i] = (byte)fis.read();
				} catch (IOException e) {}
			}
			
			for(int i=0; i<(width/8 + 1)*height; i++) {				
				for(int j=0; j<8; j++) {
					colors[indexCounter]=((imageByte[i] & bitCounter)==0 ? Color.WHITE : Color.BLACK);
					bitCounter = bitCounter/2;
					indexCounter++;
					if(indexCounter%width == 0) {
						break;
					}
				}
				bitCounter = 128;
			}
		}
		if(magicNo.equals("P5")) {
			image[0] = (255/maxNum)*cursor;
			colors[0] = new Color(image[0], image[0], image[0]);
			for(int i=1; i<width*height; i++) {				
				try {				
					image[i] = fis.read();
				} catch (IOException e) {}
				int color = (255/maxNum)*image[i];
				colors[i] = new Color(color, color, color);
			}		
		}
		if(magicNo.equals("P6")) {
			int r,g = 0,b = 0;
			r = (255/maxNum)*cursor;
			try {
				g = (255/maxNum)*fis.read();
				b = (255/maxNum)*fis.read();
			} catch (IOException e) {}
			colors[0] = new Color(r, g, b);
			
			for(int i=1; i<width*height; i++) {
				colors[i] = new Color(r, g, b);
				try {
					r = (255/maxNum)*fis.read();
					g = (255/maxNum)*fis.read();
					b = (255/maxNum)*fis.read();
				} catch (IOException e) {}			
			}
		}
	}
	
	class DrawingPanel extends JPanel {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D  g2 = (Graphics2D)g;
			AffineTransform at = new AffineTransform();
			at.scale(zoomScale, zoomScale);
			at.translate(dragDistanceX, dragDistanceY);
			g2.transform(at);
			
			if(ani1) {
				for(int y=0; y<height; y++) {
					for(int x=0; x<width; x++) {
						g.setColor(colors[y*width+x]);
						if(x < width/2) {					
							if(y < height/2)
								g.fillRect(x+1, y, 1, 1);			
							else 
								g.fillRect(x+1, (y+spaceOnY), 1, 1);
						}
						else {
							if(y < height/2)
								g.fillRect((x+1+spaceOnX), y, 1, 1);			
							else 
								g.fillRect((x+1+spaceOnX), (y+spaceOnY), 1, 1);
						}
					}
				}
			}
			
			else if(ani2) {
				for(int y=0; y<tempHeight; y++) {
					for(int x=0; x<tempWidth; x++) {
						g.setColor(colors[y*width+x]);
						g.fillRect(x+1, y, 1, 1);
					}
				}
			}
			
			else if(ani3) {
				for(int y=0; y<height; y++) {				
					for(int x=0; x<tempWidth; x++) {
						g.setColor(colors[y*width+x]);
						g.fillRect(x+1, y, 1, 1);
					}
					for(int x=width-1; x>tempWidth2; x--) {
						g.setColor(colors[y*width+x]);
						g.fillRect(x+1, y, 1, 1);
					}
				}
			}
			
			else {
				for(int y=0; y<height; y++) {
					for(int x=0; x<width; x++) {
						g.setColor(colors[y*width+x]);
						g.fillRect(x+1, y, 1, 1);
					}
				}
			}			
		}
	}
	
	class TimerListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if(ani1) {
				spaceOnX -= 4;
				spaceOnY -= 4;
				if(spaceOnX <= 0) spaceOnX=0;
				if(spaceOnY <= 0) spaceOnY=0;
				dp.repaint();
			}
			else if(ani2) {
				tempWidth += width/80;
				if(tempWidth > width) {
					tempWidth = width;
				}
				dp.repaint();
			}
			else if(ani3) {							
				if(tempWidth < width/2) tempWidth += 2;
				if(tempWidth2 > width/2) tempWidth2 -= 2;
				dp.repaint();
			}
		}			
	}
	
	class MouseHandler implements MouseWheelListener, MouseMotionListener, MouseListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent m) {			
			if(m.getWheelRotation() < 0) {
				zoomScale = zoomScale*(1.1);
				dp.repaint();
			}
			if(m.getWheelRotation() > 0) {
				zoomScale = zoomScale/(1.1);
				dp.repaint();
			}		
		}
		@Override
		public void mouseDragged(MouseEvent m) {
			dragDistanceX = (m.getX() - mouseX) + dragDistanceX;
			dragDistanceY = (m.getY() - mouseY) + dragDistanceY;
			mouseX = m.getX();
			mouseY = m.getY();
			dp.repaint();
		}
		@Override
		public void mouseMoved(MouseEvent m) {}
		public void mouseClicked(MouseEvent m) {}
		public void mouseEntered(MouseEvent m) {}
		public void mouseExited(MouseEvent m) {}
		public void mousePressed(MouseEvent m) {
			mouseX = m.getX();
			mouseY = m.getY();
		}
		@Override
		public void mouseReleased(MouseEvent m) {}	
	}
	
	class ImageFileFilter extends FileFilter {
		private final String[] okFileExtensions = new String[] {"pgm", "pbm", "ppm"};	
		@Override
		public boolean accept(File file) {
			for (String extension : okFileExtensions) {
				if (file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
		return false;
		}
		@Override
		public String getDescription() {			
			return null;
		}
	}
	
	public static void main(String[] args) {
		new MainProgram();
	}
}