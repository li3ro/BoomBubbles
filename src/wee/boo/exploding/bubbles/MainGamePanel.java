package wee.boo.exploding.bubbles;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import wee.boo.exploding.bubbles.model.Explosion;
import wee.boo.exploding.bubbles.model.Particle;
import wee.boo.exploding.bubbles.model.TouchCircle;
import wee.boo.exploding.bubbles.util.Utils;
import wee.boo.exploding.bubbles.util.Utils.AdState;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author Yaniv Bokobza
 * This is the main surface that handles the onTouch events,
 * and draws to the screen.
 */
public class MainGamePanel extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = MainGamePanel.class.getSimpleName();
	private IActivityRequestHandler myRequestHandler;
	private static final String PREFS_NAME = "highScoreFileStore";
	private static final int ROUND_TIME = 10;
	private static final int EXPLOSION_SIZE = 18;
	private static final int MAX_EXPLOSIONS = 100;		// 100 equals to unlimited
	
	SoundPool sounds = new SoundPool(13, AudioManager.STREAM_MUSIC, 0);
	int soundIds[] = new int[14];
	
	private MainThread thread;
	private Explosion[] explosions;
	private TouchCircle[] circles;
	private int hit_greener = 0;	// on hit, change the color of the edges to green for short period
	// the fps to be displayed
	private String avgFps;
	
	private long score = 0;
	private long highScore = 0;
	private boolean new_high_score=false;
	private int level = 1;
	private HashMap<String,String> level_up_string = new HashMap<String,String>();
	private int good_hits_for_next_level=1;
	private long startTime=0;
	private long timeLeft = ROUND_TIME;
	private long bonuses = 0;
	private HashMap<String,String> bonuses_display = new HashMap<String,String>();
	private Paint timer_font = new Paint();
	private Paint score_font = new Paint();
	private Paint highscore_font = new Paint();
	private Paint gameover_font = new Paint();
	private Paint level_font = new Paint();
	private Paint level_up_font = new Paint();
	private Boolean gameover_animation_up=null;
	private GameState currentState;
	
	// game over animation
	private int toMidScreen;
	private int text_alpha = 45;
	private int text_size = 1;
	
	private Bitmap bg;
	private int frame_number=0;
	private int frames_to_show_the_same_background=7;
	HashMap<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();
	
	public enum GameState {	    READY, RUNNING, FADEOUT, GAMEOVER, PAUSE	}
	
	final Handler handler = new Handler(); 
	Runnable mLongPressed = new Runnable() { 
	    public void run() { 
	        Log.i("", "Long press! Executing bomb..");
	        
	        if(sounds != null)
				sounds.play(soundIds[13], 1.8f, 1.8f, 1, 0, 1.0f);
	        
	        if(circles == null) {
				circles = new TouchCircle[MAX_EXPLOSIONS];
			}
	        
	        for(Explosion exp : explosions) {
	        	if(exp == null || exp.getTheOne()==null) {
	        		continue;
	        	}
	        	int age = exp.getTheOne().getAge();
	        	
	        	long bonus = 0;
				if(age<=100) {
					bonus=3;
				} else if(age>100 && age<140) {
					bonus=2;
				} else {
					bonus=1; 
				}
	        	
	        	score+=1;
				bonuses+=bonus;
				int font_extra_size = 0;		// the higher the bonus the bigger the font
				if(bonus==3) font_extra_size=12;
				if(bonus==2) font_extra_size=7;
				
				bonuses_display.put("+"+bonus+" seconds!___"+exp.getTheOne().getX()+","+exp.getTheOne().getY()+","+60  ,  ""+font_extra_size);
				
				exp.getTheOne().setState(Particle.STATE_DEAD);
				
				hit_greener = 20;	// on hit, change the color of the edges to green for 20 frames
				
				good_hits_for_next_level--;
				if(good_hits_for_next_level==0) {
					level++;
					if(level==3) {
						myRequestHandler.changeMusic(2);
					} else if(level==19) {
						myRequestHandler.changeMusic(3);
					}
					
					level_up_string.put((int)(getHeight() / 2 + getHeight()*0.02) +"," + 255 +","+5 , Utils.getLevelUpText(level));
					good_hits_for_next_level = level;
				}
	        	
				if(!isFadingOut()) {
					for(int i=0 ; i < circles.length ; i++) {
						if (circles[i] == null || circles[i].getState() == TouchCircle.STATE_DEAD) {
							circles[i] = new TouchCircle((int)exp.getTheOne().getX(), (int)exp.getTheOne().getY() ,null);
							break;
						}
					}
				}
	        }
	        
	    }
	};
	
	
	@SuppressWarnings("unchecked")
	public MainGamePanel(MainActivity c, Bundle icicle) {
		super(c);
		this.myRequestHandler = c;
		getHolder().addCallback(this);
		getHolder().setFormat(0x00000004); //RGB_565	  -- http://gamedev.stackexchange.com/questions/35434/how-to-handle-loading-and-keeping-many-bitmaps-in-an-android-2d-game/38058#38058
		
		setFocusable(true);
		
		if (icicle != null) {
	    	explosions = (Explosion[]) icicle.getSerializable("explosions");
	    	circles = (TouchCircle[]) icicle.getSerializable("circles");
	    	bonuses_display = (HashMap<String, String>) icicle.getSerializable("bonuses_display");
	    	level_up_string = (HashMap<String, String>) icicle.getSerializable("level_up_string");
	    	currentState = (GameState) icicle.getSerializable("currentState");
	    	avgFps = icicle.getString("avgFps");
	    	hit_greener = icicle.getInt("hit_greener");
	    	score = icicle.getLong("score");
	    	new_high_score = icicle.getBoolean("new_high_score");
	    	level = icicle.getInt("level");
	    	good_hits_for_next_level = icicle.getInt("good_hits_for_next_level");
	    	bonuses = icicle.getLong("bonuses");
	    	timeLeft = icicle.getLong("timeLeft");
	    	frame_number = icicle.getInt("frame_number");
	    	setupBitmap(frame_number);
	    	
		    if(!isGameOver())
		    	currentState=GameState.PAUSE;
		    else
		    	restart();
		
		} else {
//			// setup background
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565;
			Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.bg1, opt);
			bg = Utils.getResizedBitmap(bm, c.height, bm.getWidth());
			
		}
		
		// sound effects
		soundIds[0] = sounds.load(c, R.raw.hit, 1);
		soundIds[1] = sounds.load(c, R.raw.level_up, 1);		// level 2 , 9 , 18
		soundIds[2] = sounds.load(c, R.raw.up_up_up, 1);		// level 3 , 13
		soundIds[3] = sounds.load(c, R.raw.keep_it_up, 1);		// level 4
		soundIds[4] = sounds.load(c, R.raw.good, 1);			// level 5 , 10
		soundIds[5] = sounds.load(c, R.raw.youre_good, 1);		// level 6
		soundIds[6] = sounds.load(c, R.raw.keep_up, 1);			// level 7
		soundIds[7] = sounds.load(c, R.raw.go_go_go, 1);		// level 8
		soundIds[8] = sounds.load(c, R.raw.sweet, 1);			// level 11 , 20
		soundIds[9] = sounds.load(c, R.raw.up_you_go, 1);		// level 12
		soundIds[10] = sounds.load(c, R.raw.excelent, 1);		// level 14
		soundIds[11] = sounds.load(c, R.raw.youre_a_killer, 1);	// level 15
		soundIds[12] = sounds.load(c, R.raw.youre_the_best, 1);	// level 21+
		soundIds[13] = sounds.load(c, R.raw.explosion, 1);		// Explosion
		
		Utils.populateBitmaps(c.width, c.height, getResources(), bitmaps);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		Log.v(TAG, "surfaceChanged()!");
		// here I must re construct the pre saved state!!
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// create the game loop thread
		Log.v(TAG, "surfaceCreated()!");
		thread = new MainThread(getHolder(), this);
		if(!isPause()) {
			currentState=GameState.READY;
		}
		
		if(sounds==null) {
			sounds = new SoundPool(13, AudioManager.STREAM_MUSIC, 0);
		}
		
		Utils.resetFontProperties(getContext().getAssets() , timer_font, score_font , highscore_font , gameover_font , level_font , level_up_font);
		
		toMidScreen = (int) (this.getHeight()*0.8);
		
//		// setup bullets			--- This is OLD implementation where I limited the amount of allowed shots - now its unlimitted
//		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
//		int new_height = (int) (this.getHeight()*0.07);
//		int shrinked_percentage = (int)((float)new_height / bm.getHeight() * 100);
//		bullet = Utils.getResizedBitmap(bm, new_height, (int)((float)bm.getWidth()*((float)shrinked_percentage/100)) );
		
		// setup high score
	    SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, 0);
	    this.highScore = prefs.getLong("highscore", 0);
	    
		// at this point the surface is created and we can safely start the game loop
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed()!");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.setRunning(false);
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				Log.w(TAG, "Fail to stop MainThread from MainGamePanel.");
			}
		}
		
		sounds.release();
		sounds=null;
		
		Log.d(TAG, "Thread was shut down cleanly");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN)
	        handler.postDelayed(mLongPressed, 3000);
	    if((event.getAction() == MotionEvent.ACTION_UP))
	        handler.removeCallbacks(mLongPressed);
		super.onTouchEvent(event);
		boolean start_one = false;
		int count_ones = 0;
		if(isFadingOut())
			return true;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if(isPause()) {
				resumePause();
				return true;
			}
			
			if(startTime==0)
				if(isReady()) {
					start();
					startTime = new Date().getTime();	// get now time
					start_one = true;
				}
			if(explosions == null) {
				explosions = new Explosion[MAX_EXPLOSIONS];
				start_one = true;
			} else {
				// check for hits on the_one's , or multiple hit
				long multiplier = 1;
				for(Explosion exp : explosions) {
					if(exp != null && exp.getTheOne() != null && Utils.intersects(exp.getTheOne(), event) )	{
						if(sounds != null)
							sounds.play(soundIds[0], 1f, 1f, 1, 0, 1.0f);
						int age = exp.getTheOne().getAge();
						long bonus = 0;
						if(age<=100) {
							bonus=3;
						} else if(age>100 && age<140) {
							bonus=2;
						} else {
							bonus=1; 
						}
						score+=1;
						bonuses+=bonus;
						int font_extra_size = 0;		// the higher the bonus the bigger the font
						if(bonus==3) font_extra_size=12;
						if(bonus==2) font_extra_size=7;
						
						bonuses_display.put("+"+bonus+" seconds!___"+exp.getTheOne().getX()+","+exp.getTheOne().getY()+","+60  ,  ""+font_extra_size);
						
						exp.getTheOne().setState(Particle.STATE_DEAD);
						
						hit_greener = 15;	// on hit, change the color of the edges to green for 15 frames
						multiplier+=1;
						
						good_hits_for_next_level--;
						if(good_hits_for_next_level==0) {
							level++;
							if(level==13) {
								myRequestHandler.changeMusic(2);
							} else if(level==19) {
								myRequestHandler.changeMusic(3);
							}
							sounds.play(soundIds[ Utils.getSoundBasedOnLevel(level) ], 1f, 1f, 3, 0, 1.0f);
							level_up_string.put((int)(getHeight() / 2 + getHeight()*0.02) +"," + 255 +","+5 , Utils.getLevelUpText(level));
							good_hits_for_next_level = level;
						}
					}
					
					if(isFadingOut() && exp != null && exp.getTheOne() != null && exp.getTheOne().isAlive()) {
						exp.getTheOne().setFadingOut(true);
					}
					
					if(exp != null && exp.getTheOne() != null && exp.getTheOne().isAlive()) {
						count_ones++;
					}
				}
				
				if(multiplier>1)
					score += multiplier*2;
			}
			
			if(circles == null) {
				circles = new TouchCircle[MAX_EXPLOSIONS];
			}
			
			if(!isFadingOut()) {
				for(int i=0 ; i < explosions.length ; i++) {
					if (explosions[i] == null || explosions[i].getState() == Explosion.STATE_DEAD) {
						int max_allowed_ones = Utils.getAllowedOnesInView(level);
						Integer sent_level = null;
						if(count_ones < max_allowed_ones) {
							start_one = true;
							sent_level = level;
						}
						explosions[i] = new Explosion(EXPLOSION_SIZE, (int)event.getX(), (int)event.getY() , start_one , sent_level);
						break;
					}
				}
				for(int i=0 ; i < circles.length ; i++) {
					if (circles[i] == null || circles[i].getState() == TouchCircle.STATE_DEAD) {
						circles[i] = new TouchCircle((int)event.getX(), (int)event.getY() ,null);
						break;
					}
				}
			}
			
			if(isGameOver()) {
				restart();
			}
		}
		return true;
	}
	
	
	public void render(Canvas canvas) {
		Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
		if(!isPause() && !isGameOver() && !isReady() && frame_number<66) {
			if(frames_to_show_the_same_background > 0) {
				frame_number++;
				frames_to_show_the_same_background--;
			} else
				frames_to_show_the_same_background=7;
			setupBitmap(frame_number);
			canvas.drawBitmap(bg, 0,0, bitmapPaint);
		} else if (isGameOver()) {
			if(frame_number>0) {
				if(frames_to_show_the_same_background > 0) {
					frame_number--;
					frames_to_show_the_same_background--;
				} else
					frames_to_show_the_same_background=7;
				setupBitmap(frame_number);
				canvas.drawBitmap(bg, 0,0, bitmapPaint);
			} else {
				canvas.drawColor(Color.WHITE);
			}
		} else {
			if(isReady()) {
				if(frame_number>0) {
					if(frames_to_show_the_same_background > 0) {
						frame_number--;
						frames_to_show_the_same_background--;
					} else
						frames_to_show_the_same_background=7;
					setupBitmap(frame_number);
					canvas.drawBitmap(bg, 0,0, bitmapPaint);
				} else {
					canvas.drawColor(Color.WHITE);
				}
			} else {
				
				if(level>=8) {
					if(level >= 8 && level <= 12) {
						canvas.drawBitmap(bitmaps.get("l1"), 0,0, bitmapPaint);
					} else if(level >= 13 && level <= 15) {
						canvas.drawBitmap(bitmaps.get("l2"), 0,0, bitmapPaint);
					} else if(level >= 16 && level <= 17) {
						canvas.drawBitmap(bitmaps.get("l3"), 0,0, bitmapPaint);
					} else if(level >= 18) {
						canvas.drawBitmap(bitmaps.get("l4"), 0,0, bitmapPaint);
					} 
				} else {
					canvas.drawColor(Color.BLACK);
				}
			}
		}
		
		// render explosions
		if (explosions != null && !isPause()) {
			for(Explosion explosion : explosions)
				if(explosion != null)
					explosion.draw(canvas);
		}

		// render circles
		if (circles != null && !isPause()) {
			for (TouchCircle circle : circles)
				if (circle != null)
					circle.draw(canvas);
		}
		
//		display fps
//		displayFps(canvas, "FPS: "+avgFps);
		
		if(isPause()) {
			gameover_font.setColor(Color.WHITE);
			gameover_font.setTextSize(67);
			canvas.drawText(getResources().getString(R.string.pause) , (int)this.getWidth() / 2, (int)this.getHeight() / 2, gameover_font);
			myRequestHandler.showInterstitial(true);
			
		} else if(isGameOver() || isFadingOut()) {
			displayGameOver(canvas);
			
			if(isGameOver()) {
				myRequestHandler.showInterstitial(true);
			}
			
		} else {
			myRequestHandler.showAds(false);
			
			// draw score
			if(!isReady())
				canvas.drawText(""+score, this.getWidth()-this.getWidth()*0.87f, this.getHeight()-this.getHeight()*0.95f, score_font);
		}
		
		if(startTime>0 && timeLeft>-1 && !isPause())	{
			// display timer
			timeLeft = ROUND_TIME-((new Date().getTime() - startTime)/1000);
			timeLeft += bonuses;
			if(timeLeft < 5)
				timer_font.setColor(Color.RED);
			else
				timer_font.setARGB(255, 255, 255, 255);
			if(timeLeft==0)
				currentState = GameState.FADEOUT;
			timer_font.setTextSize(132);
			if(timeLeft != -1)
				canvas.drawText(""+timeLeft, this.getWidth()-this.getWidth()*0.5f, this.getHeight()-this.getHeight()*0.8f,timer_font);
			
			// display bonuses
			if(bonuses_display != null && bonuses_display.size() > 0) {
				HashMap<String,String> toAdd = new HashMap<String,String>();
				HashMap<String,String> tmp = new HashMap<String,String>();
				synchronized (bonuses_display) {
					tmp.putAll(bonuses_display);
				}
				
				Iterator<String> itr = tmp.keySet().iterator();
				while(itr.hasNext()) {
					String bonus_disp = itr.next();
					String text = bonus_disp.split("___")[0];
					float x = Float.parseFloat(bonus_disp.split("___")[1].split(",")[0]);
					float y = Float.parseFloat(bonus_disp.split("___")[1].split(",")[1]);
					int frames = Integer.parseInt(bonus_disp.split("___")[1].split(",")[2]);
					timer_font.setColor(Color.GREEN);
					timer_font.setTextSize(36 +  (!"".equals(tmp.get(bonus_disp)) ? Integer.parseInt(tmp.get(bonus_disp)) : 0));
					if(frames==0) {
						continue;
					}
					toAdd.put(text + "___" + x + "," + --y + "," + --frames,"");
					
					canvas.drawText(text, x, y, timer_font);
				}
				if(toAdd.size() > 0) {
					bonuses_display = toAdd;
				}
			}
			
			// display level up
			if(level_up_string != null && level_up_string.size() > 0) {
				HashMap<String,String> toAdd = new HashMap<String,String>();
				HashMap<String,String> tmp = new HashMap<String,String>();
				
				synchronized (level_up_string) {
					tmp.putAll(level_up_string);
				}
				
				Iterator<String> itr = tmp.keySet().iterator();
				while(itr.hasNext()) {
					String key = itr.next();
					String text = tmp.get(key);
					int y = Integer.parseInt(key.split(",")[0]);
					int frames = Integer.parseInt(key.split(",")[1]);
					level_up_font.setAlpha(frames);
					float text_size = Float.parseFloat(key.split(",")[2]);
					level_up_font.setTextSize(text_size);
					if (text_size>=100)
						text_size=100;
					else
						text_size++;
					if(frames==0) {
						level_up_font.setColor(Color.CYAN);
						continue;
					}
					toAdd.put(--y + "," + --frames + "," + text_size ,  text);
					
					canvas.drawText(text, getWidth() / 2, y , level_up_font);
				}
				if(toAdd.size() > 0) {
					level_up_string = toAdd;
				}
			}
		}
		
		// draw bullets left
//		int x = (int) (this.getWidth()*0.99);
//		for(int i=0 ; i<shots_left ; i++)
//			canvas.drawBitmap(bullet, x-=10, (float)(this.getHeight()*0.01),null);
		
		// display border
		Paint paint = new Paint();
		if(hit_greener!=0) {
			paint.setColor(Color.GREEN);
			hit_greener--;
		} else if(toMidScreen==590 && text_alpha==255) {	// if done animating the GameOver plus the score..
			paint.setColor(Color.RED);
		} else
			paint.setColor(Color.WHITE);
		
		if(isReady()) {
			gameover_font.setColor(frame_number>30 ? Color.WHITE : Color.BLACK);
			gameover_font.setTextSize(67);
			canvas.drawText(getResources().getString(R.string.ready) , (int)this.getWidth() / 2, (int)this.getHeight() / 2, gameover_font);
		}
		
		if(!isReady() && !isPause())
			canvas.drawText("LV "+ level , (int) (this.getWidth()*0.99)-72, (int)(this.getHeight()*0.05) , level_font);
		
		canvas.drawLines(new float[]{
				0,0, canvas.getWidth()-1,0, 
				canvas.getWidth()-1,0, canvas.getWidth()-1,canvas.getHeight()-1, 
				canvas.getWidth()-1,canvas.getHeight()-1, 0,canvas.getHeight()-1,
				0,canvas.getHeight()-1, 0,0
		}, paint);
	}

	/**
	 * This is the game update method. It iterates through all the objects
	 * and calls their update method if they have one or calls specific
	 * engine's update method.
	 */
	public void update(Canvas canvas) {
        switch (currentState) {
        case PAUSE:
        	updatePause(canvas);
        	break;
        	
        case READY:
            updateReady(canvas);
            break;
            
        case RUNNING:
        default:
            updateRunning(canvas);
            break;
        }

    }

    private void updatePause(Canvas canvas) {
    	// Stop update of objects (like explosions)
	}
	private void updateReady(Canvas canvas) {
        // Do nothing for now
    }
	public void updateRunning(Canvas canvas) {
		boolean no_explosions_in_view = true;
//		shots_left=MAX_EXPLOSIONS;
		// update explosions
		if(explosions != null)
			for(Explosion explosion : explosions)
				if (explosion != null && explosion.isAlive()) {
//					shots_left--;
					no_explosions_in_view=false;
					explosion.update(getHolder().getSurfaceFrame());
					if(isFadingOut() && explosion.getTheOne() != null)
						explosion.getTheOne().setFadingOut(true);
				}
		if(circles != null)
			for(TouchCircle circle : circles)
				if (circle != null && circle.isExpending())
					circle.update();
		if(isFadingOut()) {
			if(no_explosions_in_view) {
				currentState = GameState.GAMEOVER;
			}
			
			// check if new highscore
			if (highScore < score) {
				new_high_score = true;
				SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, 0);
			    SharedPreferences.Editor editor = prefs.edit();
			    editor.putLong("highscore", score);
			    editor.commit();
			    this.highScore = score;
			}
		}
	}
    
	
    public Explosion[] getExplosions() {
    	return explosions;
    }
    public TouchCircle[] getTouchCircles() {
    	return circles;
    }
    public HashMap<String,String> getBonusesDisplay() {
    	return bonuses_display;
    }
	public boolean isPause() {
        return currentState == GameState.PAUSE;
    }
    public boolean isReady() {
        return currentState == GameState.READY;
    }
    private boolean isFadingOut() {
    	return currentState == GameState.FADEOUT;
	}
    public void start() {
        currentState = GameState.RUNNING;
    }
    public boolean isGameOver() {
        return currentState == GameState.GAMEOVER;
    }
    
    private void displayGameOver(Canvas canvas) {
		if(toMidScreen >= this.getHeight()/2)
			toMidScreen--;
		if(text_alpha < 255)
			text_alpha++;
		if(gameover_animation_up != null) {
			if(gameover_animation_up) {
				if(text_size <= 67)
					text_size++;
				else
					gameover_animation_up=!gameover_animation_up;
			} else if(!gameover_animation_up) {
				if(text_size > 21)
					text_size--;
				else
					gameover_animation_up=!gameover_animation_up;
			}
		} else {
			if(text_size <= 67)
				text_size++;
			else
				gameover_animation_up=false;
		}
		gameover_font.setTextSize(text_size);
		gameover_font.setARGB(text_alpha, 255, 255, 255);
		gameover_font.setColor(frame_number>30 ? Color.WHITE : Color.BLACK);
		canvas.drawText(getResources().getString(R.string.game_over), (int)this.getWidth() / 2 , this.getHeight() - toMidScreen, gameover_font);
		
		score_font.setAlpha(text_alpha);
		score_font.setColor(frame_number>30 ? Color.WHITE : Color.BLACK);
		canvas.drawText(getResources().getString(R.string.your_score) + score , (int)this.getWidth() / 2, this.getHeight() - toMidScreen - 168, score_font);
		
		// draw high score
		if(new_high_score) {
			highscore_font.setTextSize(27);
			highscore_font.setColor(Color.RED);
			highscore_font.setAlpha(text_alpha);
			canvas.drawText(getResources().getString(R.string.new_high_score) , (int)this.getWidth() / 2, this.getHeight() - toMidScreen - 100, highscore_font);
			highscore_font.setTextSize(18);
			highscore_font.setColor(Color.WHITE);
		} else {
			highscore_font.setAlpha(text_alpha);
			highscore_font.setColor(frame_number>30 ? Color.WHITE : Color.BLACK);
			canvas.drawText(getResources().getString(R.string.top_score) + highScore, (int)this.getWidth() / 2, this.getHeight() - toMidScreen + 144, highscore_font);
		}
	}
    
    public void restart() {
        currentState = GameState.READY;
        score = 0;
        new_high_score = false;
        explosions = null;
        circles = null;
        startTime = 0;
        bonuses = 0;
        bonuses_display = new HashMap<String,String>();
        level_up_string = new HashMap<String,String>();
        timeLeft = ROUND_TIME;
        gameover_animation_up=null;
        level=1;
        good_hits_for_next_level=1;
        
        // game over animation reset parameters
        toMidScreen= (int) (this.getHeight()*0.8);
        text_alpha = 45;
    	text_size = 1;
    	
    }
    private void resumePause() {
		currentState = GameState.RUNNING;
		startTime = new Date().getTime() - 1000*ROUND_TIME + 1000*timeLeft - 1000*bonuses;
	}
    
//    private void displayFps(Canvas canvas, String fps) {
//		if (canvas != null && fps != null) {
//			Paint paint = new Paint();
//			paint.setARGB(255, 255, 255, 255);
//			canvas.drawText(fps, this.getWidth() - 150, 100, paint);
//		}
//	}
	public void setAvgFps(String avgFps) {
		this.avgFps = avgFps;
	}
	public void setState(GameState state) {
		currentState = state;
	}
	public GameState getGameState() {
		return currentState;
	}
	public String getAvgFps() {
		return avgFps;
	}
	public long getTimeLeft() {
		return timeLeft;
	}
	public int getHitGreener() {
		return hit_greener;
	}
	public long getScore() {
		return score;
	}
	public boolean getNewHighScore() {
		return new_high_score;
	}
	public int getLevel() {
		return level;
	}
	public int getGoodHitsForNextLevel() {
		return good_hits_for_next_level;
	}
	public long getStartTime() {
		return startTime;
	}
	public long getBonuses() {
		return bonuses;
	}
	public HashMap<String, String> getLevelUpStrings() {
		return level_up_string;
	}
	public int getFramNumber() {
		return frame_number;
	}
	
	
	private void setupBitmap(int ii) {
		final R.drawable drawableResources = new R.drawable();
		final Class<R.drawable> c = R.drawable.class;
		final Field[] fields = c.getDeclaredFields();
		int resourceId = 0;
		for (int i = 0, max = fields.length; i < max; i++) {
		    try {
		    	if( fields[i].getName().equalsIgnoreCase("bg"+ii) ) {
		    		resourceId = fields[i].getInt(drawableResources);
		    		break;
		    	}
		    } catch (Exception e) {
		        continue;
		    }
		    /* make use of resourceId for accessing Drawables here */
		}
		if(resourceId==0)
			return;
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bm = BitmapFactory.decodeResource(getResources(), resourceId, opt);
		bg = Utils.getResizedBitmap(bm, getHeight(), bm.getWidth());
	}

	
}
