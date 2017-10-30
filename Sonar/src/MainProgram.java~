import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import config.Map;
import config.Models;
import lejos.robotics.mapping.LineMap;
import lejos.robotics.navigation.Pose;
import robots.BluetoothRobot;
import robots.DataPose;
import robots.LCPRobot;
import robots.Robot;
import robots.RobotReturn;
import robots.VirtualRobot;

public class MainProgram extends JPanel implements KeyListener, WindowListener, RobotReturn {

	private MapImage imap;
	private Robot robot;
	private ScannerImage scanner;
	private Models smodel;

	public static final byte FORWARD = 0;
	public static final byte STOP = 1;
	public static final byte EXIT = 2;
	public static final byte LEFT = 3;
	public static final byte RIGHT = 4;
	public static final byte BACKWARD = 5;

	public MainProgram(LineMap map, Robot robot) {
		this.robot = robot;
		JFrame frame = new JFrame("Mapa MAC0318");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		imap = new MapImage(map);
		scanner = new ScannerImage(map);
		smodel = new Models(map);
		// frame.add(this.map);
		frame.setSize(800, 800);
		frame.setVisible(true);

		frame.setFocusable(true);
		frame.requestFocusInWindow();
		frame.addKeyListener(this);
		frame.addWindowListener(this);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scanner, imap);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((int) (frame.getHeight() / 2));
		frame.add(splitPane);

		showHelp();
	}

	private void showHelp() {
		String text = "1,2,3 - Change global map view\n";
		text += "s - Set Pose.\n";
		text += "l - Show global map trace.\n";
		text += "c - Clean maps.\n";
		text += "m - Enter robot movement.\n";
		text += "r - Enter robo rotation.\n";
		text += "a - Colect sonar data.\n";
		text += "z - Make sonar continuous scanner.\n";
		text += "i - Stop continuous scanner.\n";
		text += "g - Save global map image.\n";
		text += "f - Save scanner image.\n";
		text += "<arrows> - Move robot.\n";
		text += "h - help.\n";
		JOptionPane.showMessageDialog(null, text, "HELP", JOptionPane.PLAIN_MESSAGE);
	}

	public void addPoint(Pose p) {
		imap.addPoint(p);
	}

	@Override
	public void keyPressed(KeyEvent e) {

		char input = e.getKeyChar();
		switch (input) {
		case '1':
			imap.setVisual(0);
			break;
		case '2':
			imap.setVisual(1);
			break;
		case '3':
			imap.setVisual(2);
			break;
		case 'l':
			imap.showLine();
			break;
		case 'g':
			imap.save();
			break;
		case 'f':
			scanner.save();
			break;
		case 'c':
			imap.clean();
			scanner.clean();
			break;
		case 's':
			setRobotPose();
			break;
		case 'm':
			moveRobot();
			break;
		case 'r':
			rotateRobot();
			break;
		case 'a':
			colectSonar();
			break;
		case 'z':
			robot.scann(this);
			break;
		case 'i':
			robot.stopScann();
			break;
		case 'h':
			showHelp();
			break;
		default:
			break;
		}

		if (robot == null)
			return;

		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			robot.moveForward();
			scanner.clean();
			break;
		case KeyEvent.VK_DOWN:
			robot.moveBackward();
			scanner.clean();
			break;
		case KeyEvent.VK_LEFT:
			robot.moveLeft();
			scanner.clean();
			break;
		case KeyEvent.VK_RIGHT:
			robot.moveRight();
			scanner.clean();
			break;
		}
	}

	private void colectSonar() {
		int interval;
		try {
			String rs = JOptionPane.showInputDialog("Interval (degress):");
			interval = Integer.parseInt(rs);
		} catch (Exception e) {
			return;
		}
		ArrayList<DataPose> data = robot.scann(-90, 90, interval);
		if (data == null) return;

		Integer name = new Integer((int) (Math.random() * 1000000));
		String rand_name = name.toString() + ".txt";
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(rand_name);
		} catch (IOException e) {
			return;
		}

		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.print("x,y,headinng,sonar_ang,read,expected\n");
		for (DataPose d : data) {
			robotData(d);

			double expected = smodel.expectedSonarRead(d.getPose(), d.getSensorAngle()-90);
			printWriter.print(d.getPose().getX() + ",");
			printWriter.print(d.getPose().getY() + ",");
			printWriter.print(d.getPose().getHeading() + ",");
			printWriter.print((d.getSensorAngle()*-1) + ",");
			printWriter.print(d.getDistance() + ",");
			printWriter.print(expected + "\n");
		}

		printWriter.close();
		JOptionPane.showMessageDialog(null, "Reads saved in " + rand_name);
	}

	private void rotateRobot() {
		try {
			String rs = JOptionPane.showInputDialog("Enter rotation (degress-clockwise):");
			double r = Double.parseDouble(rs);
			robot.rotate(r);
			scanner.clean();
		} catch (Exception e) {
		}
	}

	private void moveRobot() {
		try {
			String rs = JOptionPane.showInputDialog("Enter distance (cm):");
			double r = Double.parseDouble(rs);
			robot.move(r);
			scanner.clean();
		} catch (Exception e) {
		}
	}

	private void setRobotPose() {
		try {
			String xs = JOptionPane.showInputDialog("Enter x (cm):");
			if (xs.length() == 0)
				return;
			String ys = JOptionPane.showInputDialog("Enter y (cm):");
			if (ys.length() == 0)
				return;
			String as = JOptionPane.showInputDialog("Enter heading (degress):");
			if (as.length() == 0)
				return;

			float x = Float.parseFloat(xs);
			float y = Float.parseFloat(ys);
			float a = Float.parseFloat(as);
			robot.setPose(x, y, a);
			scanner.setPose(new Pose(x, y, a));
			scanner.clean();
		} catch (Exception e) {
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (robot == null)
			return;
		robot.stop();
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		System.err.println("Fechando...");
		if (robot == null)
			return;
		robot.disconnect();

	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void robotData(DataPose data) {
		// posicao do robo
		Pose p = data.getPose();
		System.out.print(p);
		System.out.print("   sang:"+data.getSensorAngle());
		System.out.println("   distance:"+data.getDistance());
		imap.addPoint(p);
		scanner.setPose(p);

		// ponto do ultrasonico
		double sensor_ang = Math.toRadians(data.getSensorAngle() + p.getHeading()-90);
		double dx = Math.cos(sensor_ang) * data.getDistance();
		double dy = Math.sin(sensor_ang) * data.getDistance();
		double expected = smodel.expectedSonarRead(p, data.getSensorAngle()-90);
		
		if (data.getDistance() != 255) {
			imap.addRead(p.getX() + dx, p.getY() + dy);
		}
		scanner.addRead(p, data.getDistance(), data.getSensorAngle()-90, expected);
	}

	public static void main(String[] args) {

		LineMap map = Map.makeMap();
		Robot robotv = new VirtualRobot(map);
		Robot robotbt = new BluetoothRobot(null);
		String s = "LCPRobot";

		Object[] possibleValues = { robotbt, s, robotv };
		Object robottmp = JOptionPane.showInputDialog(null, "Choose one", "Input", JOptionPane.PLAIN_MESSAGE, null,
				possibleValues, possibleValues[0]);
		Robot robot;
		
		
		boolean result = false;
		if (robottmp == s) {
			robot = new LCPRobot();
		} else {
			robot = (Robot) robottmp;
		}

		if (robot != null)
			result = ((Robot) robot).connect();

		if (result == false) {
			JOptionPane.showMessageDialog(null, "Não foi possível conectar ao robô");
			System.exit(ERROR);
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainProgram(map, (Robot) robot);
			}
		});
	}
}
