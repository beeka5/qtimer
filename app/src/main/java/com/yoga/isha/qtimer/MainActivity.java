package com.yoga.isha.qtimer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.vstechlab.easyfonts.EasyFonts;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.concurrent.*;

import java.util.*;


public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener{


    int elapsedTime = 0;
    int CurrentQueuedTime = 0;

    private TextView tv_Counter;
    private Button startBtn,stopBtn;
    private EditText et_inputText;
    String[] times;
    boolean isRunning = false;
    CircleProgressBar circleProgressBar;
    PowerManager.WakeLock wakeLock;

    Timer timer;
    //private static String elapsedTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv_Counter = (TextView) findViewById(R.id.textViewTime);
        startBtn = (Button)findViewById(R.id.btnStart);
        stopBtn = (Button)findViewById(R.id.btnStop);
        et_inputText = (EditText) findViewById(R.id.inputTextField);
        tv_Counter.setTypeface(EasyFonts.robotoThin(this));
        et_inputText.setTypeface(EasyFonts.robotoThin(this));
        et_inputText.setHint(R.string.editText);
        circleProgressBar = (CircleProgressBar) findViewById(R.id.custom_progressBar);
        circleProgressBar.setProgress(0);


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "My wakelook");
        wakeLock.acquire();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                hidekeyboard();

                String inputText = et_inputText.getText().toString().trim();
                if (inputText.isEmpty()){
                    Toast.makeText(MainActivity.this,"Empty",Toast.LENGTH_SHORT).show();
                }else{
                    if (!isRunning){

                        isRunning=!isRunning;
                        et_inputText.setEnabled(false);
                        times = inputText.split(",");
                        startTimer(times[CurrentQueuedTime]);
                        startBtn.setBackgroundResource(R.drawable.pause);

                    }else{

                       pausetimer();
                    }
                }


                YoYo.with(Techniques.BounceInDown)
                        .duration(700)
                        .playOn(startBtn);

//                Intent intent = new Intent();
//                intent.setType("audio/mp3");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(
//                        intent, "Open Audio (mp3) file"), 1);
//

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stoptimer();
                resetTime();
            }
        });
    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        tv_Counter.setText(savedInstanceState.getString("elapsedtime"));
        et_inputText.setText(savedInstanceState.getString("queuetime"));

        System.out.println("Elapsed Time : -"+savedInstanceState.getString("elapsedtime"));
        System.out.println("Queued times : -"+savedInstanceState.getString("queuetime"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) if (requestCode == 1) {



            Uri audioFileUri = data.getData();
            //save this uri



            final MediaPlayer mPlayer = new MediaPlayer();

            if (mPlayer != null){
                mPlayer.reset();

            }


            try {
                mPlayer.setDataSource(this, audioFileUri);
            } catch (IllegalArgumentException e) {
                Toast.makeText(getApplicationContext(), "1 You might not set the URI correctly!", Toast.LENGTH_LONG).show();
            } catch (SecurityException e) {
                Toast.makeText(getApplicationContext(), "2 You might not set the URI correctly!", Toast.LENGTH_LONG).show();
            } catch (IllegalStateException e) {
                Toast.makeText(getApplicationContext(), "3 You might not set the URI correctly!", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(this);



        }
    }

    void startTimer(final String duration){


        final float finished = Float.parseFloat(duration) * 60 *1000;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime++;
                try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int millis = elapsedTime*1000;

                                String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                                        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));


                                tv_Counter.setText(hms);
                                circleProgressBar.setProgressWithAnimation(millis/finished*100);

                                if (finished == millis) {

                                    stoptimer();
                                    resetSingleProcess();
                                    playAudio();


                                }




                            }
                        });


                } catch (Exception e) {
                }
            }
        }, 0, 1000);

    }

    void resetTime(){

        resetSingleProcess();

        //reset full process
        CurrentQueuedTime=0;
        isRunning=false;
        et_inputText.setEnabled(true);
        startBtn.setBackgroundResource(R.drawable.play);

    }

    void resetSingleProcess(){
        //reset single process
        tv_Counter.setText("00:00:00");
        elapsedTime=0;
        circleProgressBar.setProgressWithAnimation(0);
    }

    void playAudio(){

        MediaPlayer a = MediaPlayer.create(this,R.raw.chime);
        a.start();
        a.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startNextCounter();

            }
        });

    }
    void startNextCounter(){

        CurrentQueuedTime++;

        if (times.length > CurrentQueuedTime) {

            startTimer(times[CurrentQueuedTime]);

        }else{

            stoptimer();
            resetTime();

        }

    }
    void pausetimer(){
        stoptimer();
        isRunning=false;
        startBtn.setBackgroundResource(R.drawable.play);
    }
    void stoptimer(){

        if (timer == null || !isRunning){
            return;
        }

        timer.cancel();


    }


    void hidekeyboard(){
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        pausetimer();


    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void releaseWakelock(View v) {
        wakeLock.release();

    }
}
