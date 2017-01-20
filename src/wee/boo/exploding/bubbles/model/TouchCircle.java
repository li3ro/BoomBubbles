package wee.boo.exploding.bubbles.model;

import java.io.Serializable;

import wee.boo.exploding.bubbles.util.Utils;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * 
 * @author Yaniv Bokobza
 *
 */
public class TouchCircle implements Serializable {	
	private static final long serialVersionUID = -3264835886924427863L;
	
	public static final int STATE_EXPENDING = 0;	// particle is alive
	public static final int STATE_DEAD = 1;		// particle is dead
	
	public static final int DEFAULT_LIFETIME 	= 111;	// play with this
	public static final int MAX_DIMENSION		= 266;	// the maximum radius
	
	private int state;			// circle is alive or dead
	private float radius;		// radius of the circle
	private float x, y;			// horizontal and vertical position
	private int age;			// current age of the particle
	private int lifetime;		// particle dies when it reaches this value
	private int color;			// the color of the particle
	private transient Paint paint;		// internal use to avoid instantiation
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public float getRadius() {
		return radius;
	}
	public void setRadius(float r) {
		this.radius = r;
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public int getLifetime() {
		return lifetime;
	}
	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
	public boolean isExpending() {
		return this.state == STATE_EXPENDING;
	}
	public boolean isDead() {
		return this.state == STATE_DEAD;
	}
	
	public TouchCircle(int x, int y, Integer lifetime) {
		this.x = x;
		this.y = y;
		this.state = Particle.STATE_ALIVE;
		this.radius = 0;
		if(lifetime != null)
			this.lifetime = lifetime;
		else
			this.lifetime = DEFAULT_LIFETIME;
		this.age = 0;
		this.color = Color.WHITE;
		
		initPaint();
	}
	
	public void update() {
		if (this.state != STATE_DEAD) {
			
			// extract alpha
			int a = this.color >>> 24;
			a -= 2;								// fade by 5
			if (a <= 0) {						// if reached transparency kill the particle
				this.state = STATE_DEAD;
			} else {
				this.color = (this.color & 0x00ffffff) + (a << 24);		// set the new alpha
				if(paint == null)  initPaint();
				this.paint.setAlpha(a);
				
				this.age++;						// increase the age of the particle
				if(this.radius > MAX_DIMENSION) {
					this.state = STATE_DEAD;
				} else
					radius++;
			}
			if (this.age >= this.lifetime) {	// reached the end if its life
				this.state = STATE_DEAD;
			}

		}
	}
	
	public void draw(Canvas canvas) {
//		paint.setARGB(255, 128, 255, 50);
		if(state != STATE_DEAD) {
			if(paint == null)  initPaint();
			paint.setColor(this.color);
			canvas.drawCircle(x, y, radius, paint);
		}
	}
	
	private void initPaint() {
		this.paint = new Paint(this.color);
		this.paint.setStrokeWidth(5);
		this.paint.setColor(android.graphics.Color.RED);
		this.paint.setStyle(Paint.Style.STROKE);
		this.paint.setAlpha(180);
	}
}
