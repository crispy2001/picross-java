package me.Proj.Picross;

public class Vector2D
{
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double x;
	public double y;

	public void zero() {
		x = 0;
		y = 0;
	}

	public Vector2D add(double x, double y) {
		return new Vector2D(this.x + x, this.y + y);
	}

	public Vector2D subtract(Vector2D v) {
		return subtract(v.x, v.y);
	}

	public Vector2D subtract(double x, double y) {
		return new Vector2D(this.x - x, this.y - y);
	}

	public Vector2D multiply(double scalar) {
		return new Vector2D(x * scalar, y * scalar);
	}

	public double length() {
		return Math.hypot(x, y);
	}

	public void normalize() {
		double magnitude = length();
		x /= magnitude;
		y /= magnitude;
	}

	public double distance(Vector2D v) {
		return distance(v.x, v.y);
	}

	public double getAngle() {
		return Math.atan2(y, x) * 360 / (2 * Math.PI);
	}

	public double distance(double vx, double vy) {
		double dx = vx - x;
		double dy = vy - y;
		return Math.hypot(dx, dy);
	}
}
