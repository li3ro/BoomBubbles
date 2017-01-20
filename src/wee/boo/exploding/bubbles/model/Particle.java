/**
 * 
 */
package wee.boo.exploding.bubbles.model;

import java.io.Serializable;

import wee.boo.exploding.bubbles.util.Utils;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * @author Yaniv Bokobza
 *
 */
public class Particle implements Serializable {
	private static final long serialVersionUID = -870857772612028838L;
	private static final String TAG = Particle.class.getSimpleName();
	
	public static final int STATE_ALIVE = 0;	// particle is alive
	public static final int STATE_DEAD = 1;		// particle is dead
	
	public static final int DEFAULT_LIFETIME 	= 200;	// play with this
	public static final int MAX_DIMENSION		= 12;	// the maximum width or height
	public static final int MAX_SPEED			= 24;	// maximum speed (per update)
	
	private int state;			// particle is alive or dead
	private float width;		// width of the particle
	private float height;		// height of the particle
	private float x, y;			// horizontal and vertical position
	private double xv, yv;		// vertical and horizontal velocity
	private int age;			// current age of the particle
	private int lifetime;		// particle dies when it reaches this value
	private int color;			// the color of the particle
	private transient Paint paint;		// internal use to avoid instantiation
	private boolean the_one = false;
	private final float maxsize=50f;
	
	private boolean y_changed=false;
	private boolean x_changed=false;
	private boolean fading_out = false;
	
	public Particle(int x, int y) {
		this(x, y, null, false , null);
	}
	public Particle(int x, int y, Integer lifetime, boolean is_the_one, Integer level) {
		this.x = x;
		this.y = y;
		this.state = Particle.STATE_ALIVE;
		
		if(lifetime != null)
			this.lifetime = lifetime;
		else
			this.lifetime = DEFAULT_LIFETIME;
		this.age = 0;
		
		
		this.width = Utils.rndInt(1, MAX_DIMENSION);
		
		if (is_the_one) {
			this.height = this.width;
			this.the_one = true;
			double lvl_max_speed = Utils.getLevelSpeedConstant(level);
			xv = Utils.getSpeedBasedOnLevel(level);
			yv = Utils.getSpeedBasedOnLevel(level);
			if ((xv * xv + yv * yv > lvl_max_speed * lvl_max_speed)) {
				xv *= 0.7;
				yv *= 0.7;
			}
		} else {
			this.xv = (Utils.rndDbl(0, MAX_SPEED * 2) - MAX_SPEED);
			this.yv = (Utils.rndDbl(0, MAX_SPEED * 2) - MAX_SPEED);
			// smoothing out the diagonal speed
			if ((xv * xv + yv * yv > MAX_SPEED * MAX_SPEED)) {
				xv *= 0.7;
				yv *= 0.7;
			}
			this.height = Utils.rndInt(1, MAX_DIMENSION);
		}
		
		int r = Utils.rndInt(0, 255);
		int g = Utils.rndInt(0, 255);
		int b = Utils.rndInt(0, 255);
		while((r+g+b)/3 < 128) {
			r = Utils.rndInt(0, 255);
			g = Utils.rndInt(0, 255);
			b = Utils.rndInt(0, 255);
		}
			
		this.color = Color.argb(255, r, g, b);
		this.paint = new Paint(this.color);
	}
	
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
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

	public double getXv() {
		return xv;
	}

	public void setXv(double xv) {
		this.xv = xv;
	}

	public double getYv() {
		return yv;
	}

	public void setYv(double yv) {
		this.yv = yv;
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
	
	// helper methods -------------------------
	public boolean isAlive() {
		return this.state == STATE_ALIVE;
	}
	public boolean isDead() {
		return this.state == STATE_DEAD;
	}
	public boolean isFadingOut() {
		return this.fading_out;
	}
	
	
	/**
	 * Resets the particle
	 * @param x
	 * @param y
	 */
	public void reset(float x, float y) {
		this.state = Particle.STATE_ALIVE;
		this.x = x;
		this.y = y;
		this.age = 0;
	}

	
	public void update(Float width, Float height) {
		if (this.state != STATE_DEAD) {
			this.x += this.xv;
			this.y += this.yv;
			
			// extract alpha
			int a = this.color >>> 24;
			if(!the_one)
				a -= 2;								// fade by 5
			if(isFadingOut())
				a -= 4;	//	fade out quickly
			if (a <= 0) {						// if reached transparency kill the particle
				this.state = STATE_DEAD;
			} else {
				if(the_one && this.age >= lifetime*0.7)
					a -= 1;
				this.color = (this.color & 0x00ffffff) + (a << 24);		// set the new alpha
				if(paint==null)  paint = new Paint(this.color);
				this.paint.setAlpha(a);
				this.age++;						// increase the age of the particle
				if(width!=null && height!=null && this.width<maxsize && this.height<maxsize) {
					this.width *= width;
					this.height *= height;
				}
			}
			if (this.age >= this.lifetime) {	// reached the end if its life
				this.state = STATE_DEAD;
			}

		}
	}
	public void update(Rect container) {
		update(container,null,null);
	}
	public void update(Rect container, Float width_dx, Float height_dx) {
		// update with collision
		if (this.isAlive()) {
			if(this.the_one) {
				if (x - width <= container.left || x + width >= container.right) {
					if(!x_changed) {	// if a ball edge gets stuck in the top/bottom left or right , this should fix it.
						xv *= -1;		// it happens because if there is a diff in the sizes of bottom for example, it will switch the direction for the next frame
						x_changed=true;	// but if on the next frame the size has not passed the threshold, it will change the "direction" (the sign) again- causing a loop
					}
				} else x_changed=false;
				
				if (y + height >= container.bottom || y - height <= container.top ) {
					if(!y_changed) {
						yv *= -1;
						y_changed=true;
					}
				} else y_changed = false;
				
			} else {
				if (this.x <= container.left || this.x >= container.right - this.width) {
					this.xv *= -1;
				}
				
				if (this.y <= container.top || this.y >= container.bottom - this.height) {
					this.yv *= -1;
				}
			}
		}
		update(width_dx, height_dx);
	}

	public void draw(Canvas canvas, Boolean b) {
//		paint.setARGB(255, 128, 255, 50);
		if(paint==null)  paint = new Paint(this.color);
		paint.setColor(this.color);
		if(b!=null && b)
			canvas.drawCircle(x, y, width, paint);
		else
			canvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, paint);
		
	}
	public void draw(Canvas canvas) {
		this.draw(canvas, null);
	}
	
	

	public void setFadingOut(boolean b) {
		this.fading_out  = b;
	}

	

}
