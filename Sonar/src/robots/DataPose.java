package robots;

import lejos.robotics.navigation.Pose;

public class DataPose {
	private Pose pose;
	private double distance;
	private double sensorangle;
	
	public Pose getPose() {
		return pose;
	}
	
	public void setPose(Pose pose) {
		this.pose = pose;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getSensorAngle() {
		return sensorangle;
	}

	public void setSensorAngle(double sensorangle) {
		this.sensorangle = sensorangle;
	}
}
