package robots;

import java.util.ArrayList;

import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.remote.RemoteMotor;

public class LCPRobot implements Robot {
	private Pose pose;
	static DifferentialPilot pilot;
	static UltrasonicSensor sonar;
	RemoteMotor scannerm = Motor.C;
	//RemoteMotor ma = Motor.A;
	//RemoteMotor mb = Motor.B;

	public LCPRobot() {
		pose = new Pose();
	}

	@Override
	public void move(double x) {
		double dx = Math.sin(Math.toRadians(pose.getHeading())) * x;
		double dy = Math.cos(Math.toRadians(pose.getHeading())) * x;
		pose.translate((float) dx, (float) -dy);
		pilot.travel(x);
	}

	@Override
	public void rotate(double x) {
		pose.rotateUpdate((float)-x);
		pilot.rotate(x);
	}

	@Override
	public ArrayList<DataPose> scann(int ini, int end, int interval) {
		ArrayList<DataPose> result = new ArrayList<DataPose>();
		int val = 0;
		int engine_mult = 5;
		scannerm.setSpeed(80);
		for (int j = ini; j <= end; j += interval) {
			scannerm.rotateTo(-(j * engine_mult));
			scannerm.waitComplete();
			val = sonar.getDistance();

			DataPose data = new DataPose();
			data.setDistance(val);
			data.setSensorAngle(j);
			data.setPose(pose);
			result.add(data);
			System.out.println(pose);
		}
		scannerm.rotateTo(-10);
		scannerm.waitComplete();
		System.out.println("end");
		return result;
	}
	

	@Override
	public String toString() {
		return "LCP Robot";
	}

	@Override
	public void moveForward() {
		pilot.forward();
	}

	@Override
	public void moveLeft() {
		pilot.rotateLeft();
	}

	@Override
	public void moveRight() {
		pilot.rotateRight();
	}

	@Override
	public void moveBackward() {
		pilot.backward();
	}

	@Override
	public void stop() {
		pilot.stop();
	}

	@Override
	public void scann(RobotReturn r) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopScann() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean connect() {
		//pilot = new DifferentialPilot(5.6f, 11.2f, Motor.B, Motor.A);
		pilot = new DifferentialPilot(5.6f, 11.2f, Motor.B, Motor.A);
		sonar = new UltrasonicSensor(SensorPort.S1);
		pilot.setTravelSpeed(10);
		pilot.setRotateSpeed(40);
		return true;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public void setPose(float x, float y, float a) {
		pose.setHeading(a);
		pose.setLocation(x, y);
	}

}
