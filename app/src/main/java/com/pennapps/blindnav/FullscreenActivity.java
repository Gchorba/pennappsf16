package com.pennapps.blindnav;

/**
 * Created by Gene on 2016-09-09.
 */
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.speech.tts.TextToSpeech;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class FullscreenActivity extends Activity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{
    private static final int KEY_BUTTON_UP = 0;
    private static final int KEY_BUTTON_DOWN = 1;
    private static final int KEY_BUTTON_SELECT = 2;
    private static final int KEY_LONG_SELECT = 3;
    private static final int KEY_LONG_UP = 4;
    private static final int KEY_LONG_DOWN = 5;
    private final int SPEECH_REQUEST_CODE = 123;


    private static final UUID APP_UUID = UUID.fromString("3783cff2-5a14-477d-baee-b77bd423d079");

    private PebbleKit.PebbleDataReceiver mDataReceiver;

    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;
    private NavController controller;

    private static boolean handled = false;

    float x1,x2;
    float y1, y2;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(this,this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);

        controller = new NavController();

        final Button button1 = (Button) findViewById(R.id.camerabutton);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("willzma.iris");
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }
            }
        });
        final Button button2 = (Button) findViewById(R.id.voicebutton);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                showGoogleInputDialog();
            }
        });
        if(mDataReceiver == null) {
            mDataReceiver = new PebbleKit.PebbleDataReceiver(APP_UUID) {

                @Override
                public void receiveData(Context context, int transactionId, PebbleDictionary dict) {
                    // Always ACK
                    PebbleKit.sendAckToPebble(context, transactionId);
                    Log.i("receiveData", "Got message from Pebble!");

                    // Up received?
                    if (dict.getInteger(KEY_BUTTON_UP) != null) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.upwardstairs);
                        mediaPlayer.start();
                    }
                    // Down received?
                    if (dict.getInteger(KEY_BUTTON_DOWN) != null) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.downwardstairs);
                        mediaPlayer.start();
                    }
                    // select received?
                    if (dict.getInteger(KEY_BUTTON_SELECT) != null) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.johnarrival);
                        mediaPlayer.start();
                    }
                    // long down received?
                    if (dict.getInteger(KEY_LONG_DOWN) != null) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.turnleft);
                        mediaPlayer.start();
                    }
                    // select long received?
                    if (dict.getInteger(KEY_LONG_SELECT) != null) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.redline);
                        mediaPlayer.start();
                    }
                    // up long received?
                    if (dict.getInteger(KEY_LONG_UP) != null) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.turnright);
                        mediaPlayer.start();
                    }
                }

            };
            PebbleKit.registerReceivedDataHandler(getApplicationContext(), mDataReceiver);
        }
    }
    public void showGoogleInputDialog() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Your device is not supported!",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPEECH_REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).equalsIgnoreCase("take me home")){
//                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("me.lyft.android");
//                        if (launchIntent != null) {
//                            startActivity(launchIntent);//null pointer check in case package name was not found
//                        }

                        String url = "lyft://ridetype?id=lyft&destination[latitude]=39.968965&destination[longitude]=-75.217029";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                }
                break;
            }

        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){

        FullscreenActivity.handled = false;
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        if(!FullscreenActivity.handled) {
            switch (event.getAction()) {
                // when user first touches the screen we get x and y coordinate
                case MotionEvent.ACTION_DOWN: {
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    x2 = event.getX();
                    y2 = event.getY();

                    //if left to right sweep event on screen
                    if (x1 < x2) {
                    /*
                    try {
                        controller.deselect_item(this);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */
                    }

                    // if right to left sweep event on screen
                    if (x1 > x2) {
                    /*
                    try {
                        controller.select_item(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */
                    }

                    // if UP to Down sweep event on screen
                    if (y1 < y2) {
                        controller.next_item(this);
                        Log.d(DEBUG_TAG, "SWIPE DOWN: ");
                    }

                    //if Down to UP sweep event on screen
                    if (y1 > y2) {
                        controller.prev_item(this);
                        Log.d(DEBUG_TAG, "SWIPE UP: ");
                    }
                    break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDown: " + event.toString());
        //FullscreenActivity.handled = true;
        return false;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {

        Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
        //FullscreenActivity.handled = true;
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {

        try {
            controller.deselect_item(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
        FullscreenActivity.handled = true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
        //FullscreenActivity.handled = true;
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
        FullscreenActivity.handled = true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {

        //controller.repeat_item(this);

        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        FullscreenActivity.handled = true;
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        try {
            controller.select_item(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        FullscreenActivity.handled = true;
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        FullscreenActivity.handled = true;
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());

        controller.repeat_item(this);
        FullscreenActivity.handled = true;
        return true;
    }
}