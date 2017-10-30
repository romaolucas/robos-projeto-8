import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lejos.geom.Line;
import lejos.robotics.mapping.LineMap;
import lejos.robotics.navigation.Pose;

public class MapImage extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener  {
    private double zoom = 2.0; // pixel per cm
    private double grid = 10.0; // cm
    private double centerx = 0.0;
    private double centery = 0.0; // cm
    private Point mousePt;
    private ArrayList<Pose> lista_pontos;
    private ArrayList<Pose> lista_ultra;
    private int visual_method = 0;
    private boolean line = false;
    
    private Semaphore semaphore;
    
    private LineMap map;
    
	public MapImage() {
		super();
		semaphore = new Semaphore(1);
		lista_pontos = new ArrayList<Pose>();
		lista_ultra = new ArrayList<Pose>();
		setBackground(Color.BLACK);
		addMouseWheelListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public MapImage(LineMap map) {
		this();
		this.map = map;
	}
	
    private void drawModel (Graphics g) {
        int width = (int) (getWidth()+2*centerx);
        int height = (int) (getHeight()+2*centery);
        int count = 0;
        int x_tmp = 0, y_tmp = 0;
        
	    	for (Pose p : lista_pontos) {
			double hading = Math.toRadians(p.getHeading());
				
	    		int x = width/2+(int)(p.getX()*zoom);
	    		int y = height/2+(int)(p.getY()*zoom)*-1;
	    		
			if (visual_method == 0) {
				g.setColor(Color.getHSBColor((float) (hading/(2.0*Math.PI)), 1, 1));
				g.fillOval(
						x-(int)(zoom/2.0*1.5),
						y-(int)(zoom/2.0*1.5),
						(int)(zoom*1.5),
						(int)(zoom*1.5)
				);
			} else if (visual_method == 1) {
	            g.setColor(Color.WHITE);
	            g.drawLine(
	                width/2+(int)(p.getX()*zoom),
	                height/2-(int)(p.getY()*zoom), 
	                width/2+(int)(p.getX()*zoom+Math.sin(hading)*zoom),
	                height/2-(int)(p.getY()*zoom-Math.cos(hading)*zoom)
	            );
	
	           g.drawLine(
	                width/2+(int)(p.getX()*zoom+zoom*Math.sin(hading)),
	                height/2-(int)(p.getY()*zoom-zoom*Math.cos(hading)),
	                width/2+(int)(p.getX()*zoom+0.6*zoom*Math.sin(Math.PI/8+hading)),
	                height/2-(int)(p.getY()*zoom-0.6*zoom*Math.cos(Math.PI/8+hading))
	            );
			} else if (visual_method == 2) {
				g.setColor(Color.WHITE);
				g.fillOval(
						x-(int)(zoom/2.0*5),
						y-(int)(zoom/2.0*5),
						(int)(zoom*5),
						(int)(zoom*5)
				);
	            g.setColor(Color.BLACK);
	            g.drawLine(
	                width/2+(int)(p.getX()*zoom),
	                height/2-(int)(p.getY()*zoom), 
	                width/2+(int)(p.getX()*zoom+Math.sin(hading)*zoom*2.5),
	                height/2-(int)(p.getY()*zoom-Math.cos(hading)*zoom*2.5)
	            );
			}
	
		    	if (line && count != 0) {
		    		g.setColor(Color.LIGHT_GRAY);
		    		g.drawLine(x_tmp, y_tmp, x, y);
		    	}
	
		    	x_tmp = x;
		    	y_tmp = y;
		    	count++;
		}
	    	
	    	g.setColor(Color.RED);
	    	for (Pose p : lista_ultra) {
	    		int x = width/2+(int)(p.getX()*zoom);
	    		int y = height/2+(int)(p.getY()*zoom)*-1;
	    		g.fillRect(
					x-(int)(zoom/2.0*1.0),
					y-(int)(zoom/2.0*1.0),
					(int)(zoom*1.0),
					(int)(zoom*1.0)
			);
	    	}
	    	
	    	if (map != null) {
	    		Line[] lines = map.getLines();
	    		for (int i = 0; i < lines.length; i++) {
	    			Line l = lines[i];
	            g.drawLine(
		                width/2+(int)(l.x1*zoom),
		                height/2-(int)(l.y1*zoom), 
		                width/2+(int)(l.x2*zoom),
		                height/2-(int)(l.y2*zoom)
		        );
	    		}
	    	}
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        int width = (int) (getWidth());
        int height = (int) (getHeight());
        int width2 = (int) (getWidth()+2*centerx);
        int height2 = (int) (getHeight()+2*centery);
        super.paintComponent(g);
    
        g.setColor(new Color(20, 20, 20));
        
        int initial_x = height2/2;
        while (initial_x < width) {
        	initial_x += grid*zoom;
        	g.drawLine(0, initial_x, width, initial_x); 
        }
        initial_x = height2/2;
        while (initial_x > 0) {
        	initial_x -= grid*zoom;
        	g.drawLine(0, initial_x, width, initial_x); 
        }
        int initial_y = width2/2;
        while (initial_y < width) {
        	initial_y += grid*zoom;
            g.drawLine(initial_y, 0, initial_y, height);
        }
        initial_y = width2/2;
        while (initial_y > 0) {
        	initial_y -= grid*zoom;
            g.drawLine(initial_y, 0, initial_y, height);
        }

        g.setColor(Color.ORANGE);
        g.drawLine(width2/2, 0, width2/2, height);
        g.drawLine(0, height2/2, width, height2/2);

        if (semaphore.tryAcquire()) {
	        drawModel(g);
	        semaphore.release();
		}
    }
    
    /**
     * Adiciona um ponto ao mapa
     * @param p ponto
     */
    public void addPoint(Pose p) {
    		if (semaphore.tryAcquire()) {
    			lista_pontos.clear();
    			lista_pontos.add(p);
	        semaphore.release();
		}
    		repaint();
	}

    public void addPoint(float x, float y, float z) {
		if (semaphore.tryAcquire()) {
			lista_pontos.add(new Pose(x, y, z));
	        semaphore.release();
		}
    		repaint();
	}
    

    public void addRead(float x, float y) {
		if (semaphore.tryAcquire()) {
			lista_ultra.add(new Pose(x, y, 0));
	        semaphore.release();
		}
    		repaint();
	}
    

    public void addPoint(double x, double y, double z) {
    		addPoint((float)x, (float)y, (float)z);
	}
    

    public void addRead(double x, double y) {
    		addRead((float)x, (float)y);
	}
    
    
    public void showLine () {
    		line = !line;
    		repaint();
    }

    public void setVisual (int method) {
    		visual_method = method;
    		repaint();
    }
    
    public void save () {
	    	Integer name = new Integer((int) (Math.random()*1000000));
	    	BufferedImage imagebuf = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	    	Graphics g = imagebuf.createGraphics();
	    	g.fillRect(0, 0, imagebuf.getWidth(), imagebuf.getHeight());
	    	print(g);
	    	try {
			ImageIO.write(imagebuf, "png",  new File(name.toString()+".png"));
			JOptionPane.showMessageDialog(null, "Image saved.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Image not saved.");
		}
    }
    
	public void clean() {
		if (semaphore.tryAcquire()) {
			lista_pontos.clear();
			lista_ultra.clear();
	        semaphore.release();
		}
		repaint();
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		centerx += e.getX() - mousePt.x;
		centery += e.getY() - mousePt.y;
		mousePt = e.getPoint();
		repaint();
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {		
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		mousePt = e.getPoint();
		repaint();
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	
	@Override
	public void mouseExited(MouseEvent e) {	
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.getWheelRotation()<0){
			if (zoom < 15.0)
				zoom *= 1.1;
			repaint();
		}
		//Zoom out
		if(e.getWheelRotation()>0){
			if (zoom > 1.0)
				zoom /= 1.1;
			repaint();
		}
	}
}
