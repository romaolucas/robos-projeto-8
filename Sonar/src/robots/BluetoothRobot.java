package robots;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import lejos.robotics.navigation.Pose;

public class BluetoothRobot implements Robot {
	private String name;
	private NXTComm nxtComm;
	private RobotReturn rr;
	private Semaphore readm;
	private Semaphore sendm;
	
	private int lastcmd = -1;
	
	public static final byte FORWARD = 0;
	public static final byte STOP = 1;
	public static final byte EXIT = 2;
	public static final byte LEFT = 3;
	public static final byte RIGHT = 4;
	public static final byte BACKWARD = 5;
	
	public static final byte STOPSCANN = 6;
	public static final byte STARTSCANN = 7;
	public static final byte MOVE = 8;
	public static final byte ROTATE = 9;
	public static final byte SETPOSE = 10;
	public static final byte SETSCANANGLE = 11;
	
	public static final byte TYPE_INT = 1;
	public static final byte TYPE_CMD = 2;
	public static final byte TYPE_POSE = 3;
	public static final byte TYPE_FLOAT = 4;
	
	private static final int scannangle = 5;

	private DataOutputStream output;
	private DataInputStream input;
	private Receiver receivethread;
	private Sender sendthread;
	
	private ArrayList<DataPose> reads = null;
	private ArrayList<SendData> tosend;
		
	private class Receiver extends Thread {
		public boolean run = true;
		@Override
		public void run() {
			int bytes_valiable = -1;
			
			while(run) {
				try {
					bytes_valiable = input.available();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (bytes_valiable >= 0) {
					try {
						if (input.readByte() != '@') continue;
						int angle = input.readByte();
						float alpha = input.readFloat();
						float x = input.readFloat();
						float distance = input.readFloat();
						float y = input.readFloat();
						System.out.println(distance);
						DataPose d = new DataPose();
						d.setDistance(distance);
						d.setSensorAngle(-angle);
						d.setPose(new Pose(y, -x, alpha));
						if (rr != null)
							rr.robotData(d);
						
						if (reads != null) {
							readm.tryAcquire();
							reads.add(d);
							readm.release();
						}
						
					} catch (IOException e1) {
						continue;
					}
				}
			}
		}
	}

	private class Sender extends Thread {
		public boolean run = true;
		@Override
		public void run() {		
			while(run) {
				SendData value = null;
				if (sendm.tryAcquire()) {
					if (tosend.size() > 0) {
						value = tosend.get(0);
						tosend.remove(0);
					}
					sendm.release();
				}
				if (value != null)
					value.send(output);
				else
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {

					}
			}
		}
	}
	
	private class SendData {
		float f;
		int i;
		int cmd;
		Pose p;
		int type;
		SendData(int cmd) {
			this.cmd = cmd;
			this.type = TYPE_CMD;
		}
		SendData(int cmd, int i) {
			this.cmd = cmd;
			this.i = i;
			this.type = TYPE_INT;
		}
		SendData(int cmd, float f) {
			this.cmd = cmd;
			this.f = f;
			this.type = TYPE_FLOAT;
		}
		SendData(int cmd, Pose p) {
			this.cmd = cmd;
			this.p = p;
			this.type = TYPE_POSE;
		}
		public boolean send(DataOutputStream output) {
			try {
				switch (type) {
				case TYPE_CMD:
					output.write(cmd);
					break;
				case TYPE_INT:
					output.write(cmd);
					output.write(i);
					break;
				case TYPE_FLOAT:
					output.write(cmd);
					output.writeFloat(f);
					break;
				case TYPE_POSE:
					output.write(cmd);
					output.writeFloat(p.getX());
					output.writeFloat(p.getY());
					output.writeFloat(p.getHeading());
					break;
				default:
					return false;
				}
				output.flush();
			} catch (IOException e) {
				return false;
			}
			return true;
		}
	}
	
	public BluetoothRobot (String name) {
		
		readm = new Semaphore(1);
		
		sendm = new Semaphore(1);
		tosend = new ArrayList<SendData>();
		
		receivethread = new Receiver();
		sendthread = new Sender();
		
		this.name = name;
	}
	
	public void send (SendData sd) {
		if (sendm.tryAcquire()) {
			tosend.add(sd);
			sendm.release();
		}
	}

	@Override
	public void moveForward() {
		if (lastcmd == FORWARD) return;
		send(new SendData(FORWARD));
		lastcmd = FORWARD;
	}

	@Override
	public void moveLeft() {
		if (lastcmd == LEFT) return;
		send(new SendData(LEFT));
		lastcmd = LEFT;
	}

	@Override
	public void moveRight() {
		if (lastcmd == RIGHT) return;
		send(new SendData(RIGHT));
		lastcmd = RIGHT;
	}

	@Override
	public void moveBackward() {
		if (lastcmd == BACKWARD) return;
		send(new SendData(BACKWARD));
		lastcmd = BACKWARD;
	}

	@Override
	public boolean connect () {
		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			NXTInfo[] nxtInfo = nxtComm.search(name); //find brick with NXT_ID by doing a Bluetooth inquiry
			if (nxtInfo.length == 0) { // failed to find a brick with the ID
				System.err.println("NO NXT found");
				return false;
			}
			if (!nxtComm.open(nxtInfo[0])) { // the brick was found but a connection could not be establish
				System.err.println("Failed to open NXT");
				return false;
			}
			
			input = new DataInputStream(nxtComm.getInputStream()); // open data input stream 
			output = new DataOutputStream(nxtComm.getOutputStream()); // open data output stream
			send(new SendData(SETSCANANGLE, scannangle)); // vai scanear em 5 em 5 graus
		} catch (NXTCommException e) {
			return false;
		}
		
		receivethread.start();
		sendthread.start();
		
		return true;	
	}

	@Override
	public void stop() {
		send(new SendData(STOP));
		lastcmd = -1;
	}


	@Override
	public void move(double x) {
		send(new SendData(MOVE, (float)x));
	}

	@Override
	public void rotate(double x) {
		send(new SendData(ROTATE, (float)x));
	}

	@Override
	public ArrayList<DataPose> scann(int ini, int end, int interval) {
		int readsn = Math.abs(ini-end)/interval;
		send(new SendData(SETSCANANGLE, interval));
		ArrayList<DataPose> tmp;
		reads = new ArrayList<DataPose>();
		scann(null);
		while(true) {
			readm.tryAcquire();
			if (reads.size() >= readsn+1) break;
			readm.release();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
		while (readsn+1 < reads.size() ) {
			reads.remove(reads.size()-1);
		}
		tmp = reads;
		reads = null;
		readm.release();
		stopScann();
		send(new SendData(SETSCANANGLE, scannangle));
		return tmp;
	}

	@Override
	public void scann(RobotReturn r) {
		rr = r;
		send(new SendData(STARTSCANN));
	}

	@Override
	public void stopScann() {
		send(new SendData(STOPSCANN));
	}

	@Override
	public void disconnect() {
		send(new SendData(EXIT));
		if (receivethread == null) return;
		receivethread.run = false;
		try {
			receivethread.join();
		} catch (InterruptedException e1) {
			System.out.println("Nao foi possivel finalizar as threads...");
		}

		if (sendthread == null) return;
		sendthread.run = false;
		try {
			sendthread.join();
		} catch (InterruptedException e1) {
			System.out.println("Nao foi possivel finalizar as threads...");
		}
		
		try {
			nxtComm.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void setPose(float x, float y, float a) {
		send(new SendData(SETPOSE, new Pose(-y, x, a)));
	}
	
	
	@Override
	public String toString() {
		return "Bluetooth Mestre/Escravo";
	}

}
