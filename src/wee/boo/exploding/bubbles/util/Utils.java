package wee.boo.exploding.bubbles.util;

import java.util.HashMap;

import com.google.android.gms.ads.AdRequest;

import wee.boo.exploding.bubbles.MainGamePanel;
import wee.boo.exploding.bubbles.R;
import wee.boo.exploding.bubbles.model.Particle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;

public class Utils {
	public enum AdState {
		SHOWN_IN_PAUSE,
		SHOWN_IN_GAMEOVER,
		NOT_SHOWN_YET
	}
	
	private static final String TAG = Utils.class.getSimpleName();
	
	// Return an integer that ranges from min inclusive to max inclusive.
	public static int rndInt(int min, int max) {
		return (int) (min + Math.random() * (max - min + 1));
	}

	public static double rndDbl(double min, double max) {
		return min + (max - min) * Math.random();
	}

	
	
	public static boolean intersects(Particle p, MotionEvent m) {
		return intersects(p.getX(), p.getY() , m.getX() , m.getY() , p.getWidth());
	}
	/**
	 * check if point(new_x,new_y) intersect a circle(x,y) with radius r
	 * @param x
	 * @param y
	 * @param new_x
	 * @param new_y
	 * @param width
	 * @return the truth
	 */
	public static boolean intersects(float x, float y, float new_x, float new_y, float r) {
		float dx = x-new_x;
		float dy = y-new_y;
	    return dx*dx+dy*dy <= r*r;
	}
	
	
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// RECREATE THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,	matrix, false);
		return resizedBitmap;
	}
	
	public static void resetFontProperties(AssetManager am, Paint timer_font, Paint score_font, Paint highscore_font, Paint gameover_font, Paint level_font, Paint level_up_font )  {
		Typeface fontFace = Typeface.createFromAsset(am ,"fonts/kw.ttf");	//    kw.ttf
		Typeface fontFace2 = Typeface.createFromAsset(am ,"fonts/aaa.ttf");
		Typeface fontFace3 = Typeface.createFromAsset(am ,"fonts/scoreboard.ttf");	//    kw.ttf
		Typeface fontFace4 = Typeface.createFromAsset(am ,"fonts/04B_19__.ttf");
		
		Typeface face = Typeface.create(fontFace, Typeface.BOLD);
		Typeface face2 = Typeface.create(fontFace2, Typeface.NORMAL);
		Typeface face3 = Typeface.create(fontFace3, Typeface.NORMAL);
		Typeface face4 = Typeface.create(fontFace4, Typeface.NORMAL);
		timer_font.setTextAlign(Paint.Align.CENTER);
		timer_font.setARGB(255, 255, 255, 255);
		timer_font.setTextSize(132);
		timer_font.setTypeface(face);
		timer_font.setFlags(Paint.ANTI_ALIAS_FLAG);
		
		score_font.setTextAlign(Paint.Align.CENTER);
		score_font.setARGB(255, 255, 255, 255);
		score_font.setTypeface(face3);
		score_font.setFlags(Paint.ANTI_ALIAS_FLAG);
		score_font.setColor(Color.WHITE);
		score_font.setTextSize(65);
		
		highscore_font.setTextAlign(Paint.Align.CENTER);
		highscore_font.setARGB(255, 255, 255, 255);
		highscore_font.setColor(Color.WHITE);
		highscore_font.setTypeface(face3);
		highscore_font.setFlags(Paint.ANTI_ALIAS_FLAG);
		highscore_font.setTextSize(38);
		
		gameover_font.setTextAlign(Paint.Align.CENTER);
		gameover_font.setTypeface(face2);
		gameover_font.setFlags(Paint.ANTI_ALIAS_FLAG);
		
		level_font.setTextAlign(Paint.Align.CENTER);
		level_font.setTypeface(face4);
		level_font.setColor(Color.RED);
		level_font.setTextSize(48);
		level_font.setFlags(Paint.ANTI_ALIAS_FLAG);
		
		level_up_font.setTextAlign(Paint.Align.CENTER);
		level_up_font.setARGB(255, 255, 255, 255);
		level_up_font.setColor(Color.CYAN);
		level_up_font.setTypeface(face4);
		level_up_font.setFlags(Paint.ANTI_ALIAS_FLAG);
	}
	
	
	public static int getAllowedOnesInView(int level) {
		if(level==1)
			return 1;
		else if(level==2 || level==3)
			return 2;
		else if(level>=4 && level<7)
			return 3;
		else if(level>=7 && level<11)
			return 4;
		else if(level>=11 && level<15)
			return 5;
		else if(level>=15 && level<20)
			return 6;
		else if(level>=20 && level<26)
			return 7;
		else if(level>=26 && level<32)
			return 8;
		else if(level>=32 && level<38)
			return 9;
		else
			return 10;
	}
	
	public static double getSpeedBasedOnLevel(Integer level) {
		double lvl = getLevelSpeedConstant(level);
		double tmp = (Utils.rndDbl(0, lvl * 2) - lvl);
		while(Math.abs(tmp) < lvl*0.3)		// if in the lower 30% , re random it
			tmp = (Utils.rndDbl(0, lvl * 2) - lvl);
		return tmp;
	}
	
	public static double getLevelSpeedConstant(Integer level) {
		if(level==1)
			return 2;
		else if(level==2 || level==3)
			return 7;
		else if(level>=4 && level<7)
			return 12;
		else if(level>=7 && level<11)
			return 18;
		else if(level>=11 && level<15)
			return 27;
		else if(level>=15 && level<20)
			return 37;
		else if(level>=20 && level<26)
			return 46;
		else if(level>=26 && level<32)
			return 54;
		else if(level>=32 && level<38)
			return 60;
		else if(level>=38 && level<41)
			return 68;
		else if(level>=41 && level<44)
			return 74;
		else if(level>=44 && level<47)
			return 82;
		else if(level>=47 && level<50)
			return 90;
		else
			return 100;
	}

	public static void wrapGameState(Bundle icicle, MainGamePanel mgp) {
		icicle.putSerializable("explosions", mgp.getExplosions());
		icicle.putSerializable("circles", mgp.getTouchCircles());
		icicle.putSerializable("bonuses_display", mgp.getBonusesDisplay());
		icicle.putSerializable("level_up_string", mgp.getLevelUpStrings());
		icicle.putSerializable("currentState", mgp.getGameState());
		icicle.putString("avgFps",mgp.getAvgFps());
		icicle.putInt("hit_greener", mgp.getHitGreener());
		icicle.putLong("score", mgp.getScore());
		icicle.putBoolean("new_high_score", mgp.getNewHighScore());
		icicle.putInt("level", mgp.getLevel());
		icicle.putInt("good_hits_for_next_level", mgp.getGoodHitsForNextLevel());
		icicle.putLong("bonuses", mgp.getBonuses());
		icicle.putLong("timeLeft", mgp.getTimeLeft());
		icicle.putInt("frame_number", mgp.getFramNumber());
	}
	
	
	/**
	 *	taken from http://stackoverflow.com/questions/10380989/how-do-i-get-the-current-orientation-activityinfo-screen-orientation-of-an-a
	 * @param WindowManager
	 * @return ActivityInfo.ScreenOrientation
	 */
	public static int getScreenOrientation(int rotation, int width, int height) {
	    int orientation;
	    // if the device's natural orientation is portrait:
	    if ((rotation == Surface.ROTATION_0
	            || rotation == Surface.ROTATION_180) && height > width ||
	        (rotation == Surface.ROTATION_90
	            || rotation == Surface.ROTATION_270) && width > height) {
	        switch(rotation) {
	            case Surface.ROTATION_0:
	                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;
	            case Surface.ROTATION_90:
	                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;
	            case Surface.ROTATION_180:
	                orientation =
	                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	                break;
	            case Surface.ROTATION_270:
	                orientation =
	                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	                break;
	            default:
	                Log.e(TAG, "Unknown screen orientation. Defaulting to portrait.");
	                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;              
	        }
	    }
	    // if the device's natural orientation is landscape or if the device
	    // is square:
	    else {
	        switch(rotation) {
	            case Surface.ROTATION_0:
	                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;
	            case Surface.ROTATION_90:
	                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;
	            case Surface.ROTATION_180:
	                orientation =
	                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	                break;
	            case Surface.ROTATION_270:
	                orientation =
	                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	                break;
	            default:
	                Log.e(TAG, "Unknown screen orientation. Defaulting to landscape.");
	                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;              
	        }
	    }

	    return orientation;
	}
	
	public static String getOrientationName(int n) {
		String orientation_name="";
		switch(n) {
		case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
			orientation_name = "LANDSCAPE";
			break;
			
		case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
			orientation_name = "PORTRAIT";
			break;
		}
		return orientation_name;
	}

	public static String getLevelUpText(int level) {
		switch(level) {
			case 2:
				return "LVL UP";
			case 3:
				return "UP UP UP";
			case 4:
				return "Keep It Up!";
			case 5:
				return "GOOD!";
			case 6:
				return "You're Good!";
			case 7:
				return "Keep Up";
			case 8:
				return "GO GO GO";
			case 9:
				return "LEVEL UP";
			case 10:
				return "GOOD!";
			case 11:
				return "Sweet";
			case 12:
				return "UP YOU GO";
			case 13:
				return "UP UP UP";
			case 14:
				return "EXCELENT!";
			case 15:
				return "YOU'RE a KILLER";
			case 16:
				return "Keep Up";
			case 17:
				return "GO GO GO";
			case 18:
				return "LEVEL UP";
			case 19:
				return "GOOD!";
			case 20:
				return "Sweet";
			default:
				return "You're The Best!";
		}
	}
	public static int getSoundBasedOnLevel(int level) {
		switch(level) {
		case 2:
			return 1;
		case 3:
			return 2;
		case 4:
			return 3;
		case 5:
			return 4;
		case 6:
			return 5;
		case 7:
			return 6;
		case 8:
			return 7;
		case 9:
			return 1;
		case 10:
			return 4;
		case 11:
			return 8;
		case 12:
			return 9;
		case 13:
			return 2;
		case 14:
			return 10;
		case 15:
			return 11;
		case 16:
			return 6;
		case 17:
			return 7;
		case 18:
			return 1;
		case 19:
			return 4;
		case 20:
			return 8;
		default:
			return 12;
		}
	}
	
	/**
	 * Currently this method is locking the display to Portait Only!
	 * @param activity
	 */
	public static void lockOrientation(Activity activity) {
	    Display display = activity.getWindowManager().getDefaultDisplay();
	    int rotation = display.getRotation();
	    int height;
	    int width;
        height = display.getHeight();
        width = display.getWidth();
	    switch (rotation) {
	    case Surface.ROTATION_90:
	        if (width > height)
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        else
	            activity.setRequestedOrientation(9/* reversePortait */);
	        break;
	    case Surface.ROTATION_180:
	        if (height > width)
	            activity.setRequestedOrientation(9/* reversePortait */);
	        else
	            activity.setRequestedOrientation(9    /*8 reverseLandscape */);
	        break;          
	    case Surface.ROTATION_270:
	        if (width > height)
	            activity.setRequestedOrientation(9    /*8 reverseLandscape */);
	        else
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        break;
	    default :
	        if (height > width)
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        else
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    }
	}

	public static String getErrorReason(int errorCode) {
		String errorReason = "";
		switch (errorCode) {
		case AdRequest.ERROR_CODE_INTERNAL_ERROR:
			errorReason = "Internal error";
			break;
		case AdRequest.ERROR_CODE_INVALID_REQUEST:
			errorReason = "Invalid request";
			break;
		case AdRequest.ERROR_CODE_NETWORK_ERROR:
			errorReason = "Network Error";
			break;
		case AdRequest.ERROR_CODE_NO_FILL:
			errorReason = "No fill";
			break;
		}
		return errorReason;
	}

	
	/** heavy lifting 
	 * @param height 
	 * @param width 
	 * @param resources */
	public static void populateBitmaps(int width, int height, Resources resources, HashMap<String, Bitmap> bitmaps) {
		Bitmap bm = BitmapFactory.decodeResource(resources, R.drawable.a001sun);
		bm = Utils.getResizedBitmap(bm, height, bm.getWidth());
		bitmaps.put("l1", bm);
		bm = BitmapFactory.decodeResource(resources, R.drawable.a002huricane);
		bm = Utils.getResizedBitmap(bm, height, bm.getWidth());
		bitmaps.put("l2", bm);
		bm = BitmapFactory.decodeResource(resources, R.drawable.a003brazil);
		bm = Utils.getResizedBitmap(bm, height, bm.getWidth());
		bitmaps.put("l3", bm);
		bm = BitmapFactory.decodeResource(resources, R.drawable.a004soft);
		bm = Utils.getResizedBitmap(bm, height, bm.getWidth());
		bitmaps.put("l4", bm);
	}

	
}
