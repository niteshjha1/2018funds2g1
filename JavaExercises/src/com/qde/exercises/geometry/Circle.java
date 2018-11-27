package com.qde.exercises.geometry;

public class Circle {
	
	
	private double radius;

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	
	public double getArea() {
		return Math.PI * this.getRadius() * this.getRadius();
	}
	
	public double getPerimeter() {
		return 2* Math.PI * this.getRadius();
	}
}
