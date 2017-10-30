import java.awt.Color;
import java.awt.Graphics;
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

public class ScannerImage extends JPanel {
	private Pose pose;
	private LineMap map;
	private ArrayList<SonarRead> lista_leituras;
	private Semaphore semaphore;

	class SonarRead {
		public double distance;
		public double ang;
		public double expected;

		SonarRead(double distance, double ang, double expected) {
			this.ang = ang;
			this.distance = distance;
			this.expected = expected;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		int w = this.getWidth() / 2;
		int h = this.getHeight();
		super.paintComponent(g);
		int distance = h * 2 / 25;
		g.setColor(new Color(0f, 0f, 0f, 0.4f));
		for (int i = 1; i <= 18; i++) {
			int r = distance * i;
			g.drawArc(w - r, h - r, 2 * r, 2 * r, 0, 180);
			g.drawString(new Integer(i * 20).toString(), w - 7, h - r);
		}
		g.setColor(new Color(0f, 0f, 0f, 0.5f));
		for (int i = 1; i < 6; i++) {
			int lw = (int) (Math.cos(Math.PI / 6.0 * i) * distance * 18);
			int lh = (int) (Math.sin(Math.PI / 6.0 * i) * distance * 18);
			g.drawLine(w, h, lw + w, h - lh);
		}
		g.setColor(new Color(0f, 0f, 0f, 0.1f));
		for (int i = 1; i < 12; i++) {
			int lw = (int) (Math.cos(Math.PI / 12.0 * i) * distance * 18);
			int lh = (int) (Math.sin(Math.PI / 12.0 * i) * distance * 18);
			g.drawLine(w, h, lw + w, h - lh);
		}

		if (semaphore.tryAcquire()) {
			double d = h * 2.0 / 25.0 / 20.0;

			g.setColor(new Color(0f, 1f, 0f));
			if (map != null && pose != null) {
				Line[] lines = map.getLines();
				for (int i = 0; i < lines.length; i++) {
					Line l = lines[i];
					double sin = Math.sin(-Math.toRadians(pose.getHeading() - 180));
					double cos = Math.cos(-Math.toRadians(pose.getHeading() - 180));

					double x1 = (l.x1 - pose.getX()) * d;
					double y1 = (l.y1 - pose.getY()) * d;

					double x2 = (l.x2 - pose.getX()) * d;
					double y2 = (l.y2 - pose.getY()) * d;

					double xx1 = x1 * cos - y1 * sin;
					double yy1 = y1 * cos + x1 * sin;

					double xx2 = x2 * cos - y2 * sin;
					double yy2 = y2 * cos + x2 * sin;

					g.drawLine((int) (w + xx1), (int) (h - yy1), (int) (w + xx2), (int) (h - yy2));
				}
			}

			drawDots(g, w, h);
			semaphore.release();
		}
	}

	private void drawDots(Graphics g, int w, int h) {
		int oval_size = 16;
		int distance = h * 2 / 25;
		double d = distance / 20.0;
		double a = -oval_size / 4;
		double x, y;

		g.setColor(new Color(1f, 0f, 0f));
		for (SonarRead r : lista_leituras) {
			x = a + (d * r.distance) * Math.sin(Math.toRadians(r.ang));
			y = a + (d * r.distance) * Math.cos(Math.toRadians(r.ang));
			x = w - x;
			y = h - y;
			g.setColor(new Color(1f, 0f, 0f));
			g.fillOval((int) (x - oval_size / 2.0), (int) (y - oval_size / 2.0), oval_size / 2, oval_size / 2);

			x = a + (d * r.expected) * Math.sin(Math.toRadians(r.ang));
			y = a + (d * r.expected) * Math.cos(Math.toRadians(r.ang));
			x = w - x;
			y = h - y;
			g.setColor(new Color(0f, 0f, 1f));
			g.fillOval((int) (x - oval_size / 2.0), (int) (y - oval_size / 2.0), oval_size / 2, oval_size / 2);
		}
		if (map == null)
			return;
	}

	public Pose getPose() {
		return pose;
	}
	
	public void setPose (Pose p) {
		pose = p;
		repaint();
	}
	
	public void addRead(Pose p, double distance, double ang, double expected) {
		if (semaphore.tryAcquire()) {
			if (pose == null || lista_leituras.size() >= 360) {
				pose = new Pose(p.getX(), p.getY(), p.getHeading());
				lista_leituras.clear();
			}
			lista_leituras.add(new SonarRead(distance, ang + 90, expected));
			semaphore.release();
		}
		repaint();
	}

	public ScannerImage(LineMap map) {
		this.map = map;
		semaphore = new Semaphore(1);
		lista_leituras = new ArrayList<SonarRead>();
	}

	public void clean() {
		if (semaphore.tryAcquire()) {
			lista_leituras.clear();
			semaphore.release();
		}
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

}
