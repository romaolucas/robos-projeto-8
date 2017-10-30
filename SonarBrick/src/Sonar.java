import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;
import lejos.robotics.localization.*;

class Scanner extends Thread {
	DataOutputStream output;
	OdometryPoseProvider provider;
	boolean run;
	public boolean scann;
	public int increment = 5;
	int position = 0;
	
	Scanner (DataOutputStream output, OdometryPoseProvider provider) {
		super();
		this.output = output;
		this.provider = provider;
		run = true;
		scann = false;
	}
	
	public void stop () {
		run = false;
	}
	
	public void run() {
		NXTRegulatedMotor scannerm = Motor.C;
		UltrasonicSensor sensor = new UltrasonicSensor(SensorPort.S1) ;
		
		while (run) {
			if (scann == false) {
				position = 0;
				scannerm.rotateTo(0);
				scannerm.waitComplete();
			}
			
			scannerm.waitComplete();

			if (scann == false)
				continue;
			
			float distance = sensor.getDistance();
						
			Pose pose = provider.getPose();
			float x = pose.getX();
			float y = pose.getY();
			float alpha = pose.getHeading();
			
			try {
				output.write('@');
				output.write(position);
				output.writeFloat(alpha);
				output.writeFloat(x);
				output.writeFloat(distance);
				output.writeFloat(y);
				output.flush();
			} catch (IOException e) {
			}
		
			
			position += increment;
			if (position == 90 || position == -90)
				increment *= -1;

			scannerm.rotateTo(position*5);
		}
		
		scannerm.rotateTo(0);
		scannerm.waitComplete();
	}
}

public class Sonar {
	
	private static final byte FORWARD = 0;
	private static final byte STOP = 1;
	private static final byte EXIT = 2;
	private static final byte LEFT = 3;
	private static final byte RIGHT = 4;
	private static final byte BACKWARD = 5;
	
	public static final byte STOPSCANN = 6;
	public static final byte STARTSCANN = 7;
	public static final byte MOVE = 8;
	public static final byte ROTATE = 9;
	public static final byte SETPOSE = 10;
	public static final byte SETSCANANGLE = 11;
	
	public static void main(String[] args) throws Exception {
		
		BTConnection btc = Bluetooth.waitForConnection();
		//USBConnection btc = USB.waitForConnection();
		
		DataInputStream input = btc.openDataInputStream();
		DataOutputStream output = btc.openDataOutputStream();
		
		DifferentialPilot pilot = new DifferentialPilot(5.6f, 11.2f, Motor.B, Motor.A, false); 
		OdometryPoseProvider provider = new OdometryPoseProvider(pilot);
		pilot.setRotateSpeed(5);
		pilot.setTravelSpeed(20);
		
		Scanner scan = new Scanner(output, provider);
		scan.start();
		int input_byte;
		boolean run = true;
		
		Sound.twoBeeps();
		
		while (run) {
			if (input.available() <= 0) {
				Thread.yield();
				continue;
			}
			input_byte = input.readByte();

			System.out.println(input_byte);
			
			switch (input_byte) {
				case FORWARD:
					pilot.forward();
					break;
				case STOP:
					pilot.stop();
					break;
				case EXIT:
					run = false;
					break;
				case LEFT:
					pilot.rotateLeft();
					break;
				case RIGHT:
					pilot.rotateRight();
					break;
				case BACKWARD:
					pilot.backward();
					break;
				case STOPSCANN:
					scan.scann = false;
					break;
				case STARTSCANN:
					scan.position = 90;
					scan.increment = -Math.abs(scan.increment);
					scan.scann = true;
					break;
				case MOVE:
					float d = input.readFloat();
					pilot.travel(d);
					break;
				case ROTATE:
					float r = input.readFloat();
					pilot.rotate(r);
					break;
				case SETPOSE:
					float x = input.readFloat();
					float y = input.readFloat();
					float a = input.readFloat();
					System.out.println("x: "+x);
					System.out.println("y: "+y);
					System.out.println("a: "+a);
					provider.setPose(new Pose(x,y,a));
					break;
				case SETSCANANGLE:
					int i = input.readByte();
					System.out.println("ang: "+i);
					scan.increment = i;
					break;
			}
		}
		Sound.beep();
		scan.stop();
		scan.join();
	}
}