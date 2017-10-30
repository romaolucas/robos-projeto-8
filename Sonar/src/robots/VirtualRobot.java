package robots;

import java.util.ArrayList;
import java.util.Random;

import lejos.robotics.mapping.LineMap;
import lejos.robotics.navigation.Pose;

public class VirtualRobot implements Robot {
	private Pose pose;
	private Simulate simthread;
	private RobotReturn rr;
	private LineMap map;
	private int angle = 0;
	private Random rand;

	private class Simulate extends Thread {
		public boolean run = true;

		public void run() {
			angle = 0;
			int add = -5;
			while (run) {
				DataPose data = new DataPose();

				double dist = getDistance();

				data.setDistance((int) dist);
				data.setPose(pose);
				data.setSensorAngle(angle + 90);

				rr.robotData(data);

				angle += add;
				if (angle == -180 || angle == 0)
					add *= -1;

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
		}

	}

	public double getDistance() {
		Pose tmppose = new Pose(pose.getX(), pose.getY(), pose.getHeading());
		float mindist = Float.POSITIVE_INFINITY;
		int cone = 30;
		for (int angulo = -cone / 2; angulo <= cone / 2; angulo++) {
			tmppose.setHeading((float) (pose.getHeading() - angulo + angle));
			float dist = map.range(tmppose);
			if (dist > 0 && dist < mindist)
				mindist = dist;
		}
		double v = mindist + rand.nextGaussian() * 4 + Math.random() * 2;
		v = Math.min(255, v);
		return v;
	}

	public VirtualRobot(LineMap map) {
		pose = new Pose();
		pose.setHeading(0);
		this.map = map;
		rand = new Random();
	}

	@Override
	public void moveForward() {
		move(5);
	}

	@Override
	public void moveLeft() {
		pose.rotateUpdate(45);
	}

	@Override
	public void moveRight() {
		pose.rotateUpdate(-45);
	}

	@Override
	public void moveBackward() {
		move(-5);
	}

	@Override
	public boolean connect() {
		return true;
	}

	@Override
	public void stop() {
	}

	@Override
	public void move(double x) {
		double dx = Math.sin(Math.toRadians(pose.getHeading())) * x;
		double dy = Math.cos(Math.toRadians(pose.getHeading())) * x;
		pose.translate((float) dx, (float) -dy);
	}

	@Override
	public void rotate(double x) {
		pose.rotateUpdate((float) -x);
	}

	@Override
	public ArrayList<DataPose> scann(int ini, int end, int interval) {
		ArrayList<DataPose> result = new ArrayList<DataPose>();
		for (angle = ini-90; angle <= end-90; angle += interval) {
			DataPose data = new DataPose();
			double dist = getDistance();
			data.setDistance((int) dist);
			data.setPose(pose);
			data.setSensorAngle(angle+90);
			result.add(data);
		}
		angle = 0;
		return result;
	}

	@Override
	public void scann(RobotReturn r) {
		rr = r;
		stopScann();
		simthread = new Simulate();
		simthread.start();
	}

	@Override
	public void stopScann() {
		if (simthread == null)
			return;
		simthread.run = false;
		try {
			simthread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void disconnect() {

	}

	@Override
	public void setPose(float x, float y, float a) {
		pose.setHeading(a);
		pose.setLocation(x, y);
	}

	@Override
	public String toString() {
		return "Virtual Robot";
	}

}
