/**
 * 
 */
package wee.boo.exploding.bubbles.model;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

/**
 * @author Yaniv Bokobza
 *
 */
public class Explosion implements Serializable {
	private static final long serialVersionUID = 5582053819706899103L;

	private static final String TAG = Explosion.class.getSimpleName();
	
	public static final int STATE_ALIVE 	= 0;	// at least 1 particle is alive
	public static final int STATE_DEAD 		= 1;	// all particles are dead
	
//	private static final int CHANCE_FOR_THE_ONE = 40;	// in percentage
	private Particle the_one=null;						// the one left after the explosion
	private Particle[] particles;						// particles in the explosion
	private int x, y;									// the explosion's origin
	private float gravity;								// the gravity of the explosion (+ upward, - down)
	private float wind;									// speed of wind on horizontal
	private int size;									// number of particles
	private int state;									// whether it's still active or not
	
	public Explosion(int particleNr, int x, int y , boolean start_one, Integer level) {
//		Log.d(TAG, "Explosion created at " + x + "," + y);
		this.state = STATE_ALIVE;
		if(start_one) {
			the_one = new Particle(x, y, 3000, true ,level);
			particleNr--;
		}
		this.particles = new Particle[particleNr];
	 	for (int i = 0; i < this.particles.length; i++) {
			Particle p = new Particle(x, y);
			this.particles[i] = p;
		}
	 	this.size = particleNr;
	 	
	}
	
	public Particle[] getParticles() {
		return particles;
	}
	public void setParticles(Particle[] particles) {
		this.particles = particles;
	}
	public Particle getTheOne() {
		return the_one;
	}
	public void setTheOne(Particle the_one) {
		this.the_one = the_one;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public float getGravity() {
		return gravity;
	}
	public void setGravity(float gravity) {
		this.gravity = gravity;
	}
	public float getWind() {
		return wind;
	}
	public void setWind(float wind) {
		this.wind = wind;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	// helper methods -------------------------
	public boolean isAlive() {
		return this.state == STATE_ALIVE;
	}
	public boolean isDead() {
		return this.state == STATE_DEAD;
	}
	
	public void update(Rect container) {
		if (this.state != STATE_DEAD) {
			boolean isDead = true;
			for (int i = 0; i < this.particles.length; i++) {
				if (this.particles[i].isAlive()) {
					this.particles[i].update(container);
					isDead = false;
				}
			}
			if(the_one != null && the_one.isAlive()) {
				this.the_one.update(container, 1.01f, 1.01f );
				isDead = false;
			}
			if (isDead)
				this.state = STATE_DEAD; 
		}
	}

	public void draw(Canvas canvas) {
		for(int i = 0; i < this.particles.length; i++) {
			if (this.particles[i].isAlive()) {
				this.particles[i].draw(canvas);
			}
		}
		if(the_one != null && the_one.isAlive()) {
			this.the_one.draw(canvas,true);
		}
	}

	
}
