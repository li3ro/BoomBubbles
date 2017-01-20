package wee.boo.exploding.bubbles;

import wee.boo.exploding.bubbles.MainGamePanel.GameState;
import wee.boo.exploding.bubbles.util.Utils;
import wee.boo.exploding.bubbles.util.Utils.AdState;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends Activity implements IActivityRequestHandler {
	private static final String TAG = MainActivity.class.getSimpleName();
	MainGamePanel mgp;
	Context context = this;
	
	private AdView mAdView;
	private InterstitialAd mInterstitialView;
	private AdState adInterstitialState = AdState.NOT_SHOWN_YET;
	private AdState adBunnerState = AdState.NOT_SHOWN_YET;
	
	private final int SHOW_ADS = 1;
	private final int HIDE_ADS = 0;
	private final int SHOW_INTERSTITIAL = 2;
	private final int CHANGE_MUSIC_1 = 10;
    private final int CHANGE_MUSIC_2 = 20;
	private final int CHANGE_MUSIC_3 = 30;
    
	int width,height;
	
	SharedPreferences prefs;
	
	// game music and sound effects
	MediaPlayer backgroundMusic;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        Utils.lockOrientation(this);
        
        // music
        startMusic(1);
        
        prefs = this.getSharedPreferences("wee.boo.exploding.bubbles", Context.MODE_PRIVATE);
        
	    width = getWindowManager().getDefaultDisplay().getWidth();
	    height = getWindowManager().getDefaultDisplay().getHeight();
	    
	    RelativeLayout layout = new RelativeLayout(this);
	    
	    // Admob view setup
	    mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        mAdView.setAdUnitId( getString(R.string.banner_ad_unit_id) );
	    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    mAdView.setLayoutParams(lp);
	    
        mgp = new MainGamePanel(this, icicle);
        // set our MainGamePanel as the View
        
        layout.addView(mgp);
        layout.addView(mAdView);
        setContentView(layout);
        
        mInterstitialView = new InterstitialAd(this);
        mInterstitialView.setAdUnitId( getString(R.string.interstitial_ad_unit_id) );
        requestNewInterstitial();
        
        mInterstitialView.setAdListener(new AdListener() {
        	/** Called when an ad is loaded. */
        	public void onAdLoaded(){
        		Log.d(TAG, "onAdLoaded");
            }
        	
        	/**
			 * Called when an ad is clicked and about to return to the application.
			 */
			@Override
			public void onAdClosed() {
				Log.d(TAG, "onAdClosed");
				requestNewInterstitial();
			}

			/** Called when an ad failed to load. */
			@Override
			public void onAdFailedToLoad(int error) {
				String message = "onAdFailedToLoad: "+ Utils.getErrorReason(error);
				Log.d(TAG, message);
			}

			/**
			 * Called when an ad is clicked and going to start a new Activity
			 * that will leave the application (e.g. breaking out to the Browser
			 * or Maps application).
			 */
			@Override
			public void onAdLeftApplication() {
				Log.d(TAG, "onAdLeftApplication");
			}

			/**
			 * Called when an Activity is created in front of the app (e.g. an
			 * interstitial is shown, or an ad is clicked and launches a new
			 * Activity).
			 */
			@Override
			public void onAdOpened() {
				Log.d(TAG, "onAdOpened");
			}
        });
        
		mAdView.setAdListener(new AdListener() {
			/**
			 * Called when an ad is clicked and about to return to the application.
			 */
			@Override
			public void onAdClosed() {
				Log.d(TAG, "onAdClosed");
			}

			/** Called when an ad failed to load. */
			@Override
			public void onAdFailedToLoad(int error) {
				String message = "onAdFailedToLoad: "
						+ Utils.getErrorReason(error);
				Log.d(TAG, message);
			}

			/**
			 * Called when an ad is clicked and going to start a new Activity
			 * that will leave the application (e.g. breaking out to the Browser
			 * or Maps application).
			 */
			@Override
			public void onAdLeftApplication() {
				Log.d(TAG, "onAdLeftApplication");
			}

			/**
			 * Called when an Activity is created in front of the app (e.g. an
			 * interstitial is shown, or an ad is clicked and launches a new
			 * Activity).
			 */
			@Override
			public void onAdOpened() {
				Log.d(TAG, "onAdOpened");
			}

			/** Called when an ad is loaded. */
			@Override
			public void onAdLoaded() {
				Log.d(TAG, "onAdLoaded");
			}
		});
		
		showAd();
    }
    
    
    @Override
	protected void onSaveInstanceState(Bundle icicle) {
    	super.onSaveInstanceState(icicle);
    	if(!mgp.isGameOver() && !mgp.isReady()) {	// on game over - don't store the state
    		Utils.wrapGameState(icicle , mgp);
    	}
	}
    
	@Override
	protected void onStop() {
		Log.i(TAG, "onStop()");
		super.onStop();
		if(backgroundMusic!=null) {
			if(backgroundMusic.isPlaying())
				backgroundMusic.stop();
			backgroundMusic.reset();
        	backgroundMusic.release();
        	backgroundMusic=null;
        }
	}
	
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		super.onResume();
		if (mAdView != null)
            mAdView.resume();
        
        startMusic(1);
	}
    
	@Override
    protected void onDestroy() {
		Log.i(TAG, "onDestroy()");
    	if (mAdView != null) {
    		mAdView.removeAllViews();
    		mAdView.destroy();
    	}
    	stopMusic();
    	super.onDestroy();
    }
    
    @Override
    protected void onPause() {
    	Log.i(TAG, "onPause()");
    	super.onPause();
    	if (mAdView != null)
            mAdView.pause();
    	stopMusic();
    }
    
    private void stopMusic() {
    	if(backgroundMusic!=null) {
			if(backgroundMusic.isPlaying())
				backgroundMusic.stop();
			backgroundMusic.reset();
        	backgroundMusic.release();
        	backgroundMusic=null;
        }
    }
    
    private void startMusic(int musicNumber) {
    	if(backgroundMusic != null) {
    		stopMusic();
    	}
		int RID=1;
		if(musicNumber==1)
			RID = R.raw.music_1;
		else if(musicNumber==2)
			RID = R.raw.music_2;
		else if(musicNumber==3)
			RID = R.raw.music_3;
		backgroundMusic = MediaPlayer.create(MainActivity.this, RID);
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.8f, 0.8f);
        backgroundMusic.start();
	}
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK))  {
	    	Log.v(TAG, "back key pressed.");
	    	if(!mgp.isPause() && !mgp.isGameOver()) {
	    		mgp.setState(GameState.PAUSE);
	    		Toast.makeText(context, "Back again to exit", Toast.LENGTH_SHORT).show();
	    		return false;
	    	} else
	    		return super.onKeyDown(keyCode, event);
	    }
	    return super.onKeyDown(keyCode, event);
	}

    
    // This is the callback that posts a message for the handler
    @Override
    public void showAds(boolean show) {
       handler.sendEmptyMessage(show ? SHOW_ADS : HIDE_ADS);
       
       if(!show) {
    	   adInterstitialState = AdState.NOT_SHOWN_YET;
    	   adBunnerState = AdState.NOT_SHOWN_YET;
       }
    }
	@Override
	public void showInterstitial(boolean show) {
		if(show) {
			handler.sendEmptyMessage(SHOW_INTERSTITIAL);
		}
	}
	@Override
    public void changeMusic(int musicNumber) {
		handler.sendEmptyMessage(musicNumber == 1 ? CHANGE_MUSIC_1 : (musicNumber == 2 ? CHANGE_MUSIC_2 : (musicNumber == 3 ? CHANGE_MUSIC_3 : null)));
    }
	
    protected Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SHOW_ADS:
                {
                	showAd();
                    break;
                }
                case HIDE_ADS:
                {
                	if(mAdView != null)
                		mAdView.setVisibility(View.GONE);
                    break;
                }
                case SHOW_INTERSTITIAL:
                {
                	showInterstitial();
                    break;
                }
                case CHANGE_MUSIC_1:
                {
                	startMusic(1);
                    break;
                }
                case CHANGE_MUSIC_2:
                {
                	startMusic(2);
                    break;
                }
                case CHANGE_MUSIC_3:
                {
                	startMusic(3);
                    break;
                }
            }
        }
    };
    
    /** PRIVATE METHODS TO HANDLE ADS */
    private void showInterstitial() {
        if(mInterstitialView.isLoaded() && isAllowToShowAd(adInterstitialState)) {
        	mInterstitialView.show();
        	if(mgp.isPause()) {
        		adInterstitialState = AdState.SHOWN_IN_PAUSE;
        	} else if (mgp.isGameOver()) {
        		adInterstitialState = AdState.SHOWN_IN_GAMEOVER;
        	}
        } else {
        	showAd();
        }
	}
    
    private boolean isAllowToShowAd(AdState adState) {
    	if(adState.equals(AdState.NOT_SHOWN_YET)) {
    		return true;
    	} else if(adState.equals(AdState.SHOWN_IN_PAUSE) && mgp.isPause()) {
    		return false;
    	} else if(adState.equals(AdState.SHOWN_IN_GAMEOVER) && mgp.isGameOver()) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    private void showAd() {
		if(mAdView == null) {
    		mAdView = new AdView(context);
            mAdView.setAdSize(AdSize.SMART_BANNER);
            mAdView.setAdUnitId( getString(R.string.banner_ad_unit_id) );
    	    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    	    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    	    mAdView.setLayoutParams(lp);
    	}
		
		mAdView.setVisibility(View.VISIBLE);
		if(isAllowToShowAd(adBunnerState)) {
			AdRequest adBunnerRequest = new AdRequest.Builder().build();
			mAdView.loadAd(adBunnerRequest);
			
			if(mgp.isPause()) {
				adBunnerState = AdState.SHOWN_IN_PAUSE;
        	} else if (mgp.isGameOver()) {
        		adBunnerState = AdState.SHOWN_IN_GAMEOVER;
        	}
		}
	}
    private void requestNewInterstitial() {
    	AdRequest adInterstitialRequest = new AdRequest.Builder().build();
        mInterstitialView.loadAd(adInterstitialRequest);	// only for loading the interstitial - not showing right away..
    }
    
}