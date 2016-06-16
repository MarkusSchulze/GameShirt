package com.led.led;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Maggi on 01.06.2016.
 * gets the BlueTooth connection from the PlayerSelection. Testclass to get the data transfer going.
 */
public class GameScreen extends ActionBarActivity {
    private OutputStream mmOutputStream;
    private BTConnection btConn;
    private TextView lumn, inputText1, inputText2;
    private final int zoneCount = 2;
    private final long startTime = System.currentTimeMillis();
    private final Handler timerHandler = new Handler();
    private int HIT_SOUND_NORMAL = 0;
    private int HIT_SOUND_LIGHTSABER = 1;
    private int HIT_SOUND_NORMAL_2 = 2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        String address = newint.getStringExtra(PlayerSelection.EXTRA_ADDRESS);
        //Versuche die richtige BT-Verbindung in der Arraylist von Playerselection zu finden
        //DEBUG
        for (int i = 0; i < PlayerSelection.myBlueComms.size(); i++) {
            for (int u = 0; u < zoneCount; u++) {
                sendWithDelay(String.valueOf(u + 1) + "f", 200 * u);
            }

            if (address.equalsIgnoreCase(PlayerSelection.myBlueComms.get(i).getAddress())) {
                btConn = PlayerSelection.myBlueComms.get(i);
                btConn.beginListenForData();
                btConn.getInputText();
                try {
                    mmOutputStream = btConn.getMmSocket().getOutputStream();
                } catch (IOException e) {
                    msg(e.toString());
                    finish();
                }

            }
        }

        if (btConn == null) {
            msg("Something went wrong");
            finish();
        }

        //view of the ledControl
        setContentView(R.layout.activity_game_screen);

        //call the widgtes
        Button btnOn = (Button) findViewById(R.id.button2);
        Button btnOff = (Button) findViewById(R.id.button3);
        Button btnDis = (Button) findViewById(R.id.button4);
        inputText1 = (TextView) findViewById(R.id.textView4);
        inputText2 = (TextView) findViewById(R.id.textView5);
        SeekBar brightness = (SeekBar) findViewById(R.id.seekBar);
        lumn = (TextView) findViewById(R.id.lumn);


        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();      //method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                try {
//                    btConn.closeBT(); //close connection
//                } catch (IOException e) {
//                    msg("Error");
//                }
                finish();
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    lumn.setText(String.valueOf(progress));
                    try {
                        mmOutputStream.write(String.valueOf(progress).getBytes());
                    } catch (IOException e) {
                        msg(e.toString());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void turnOffLed() {
        try {
            inputText2.setText(btConn.getInputText());
            inputText1.setText(PlayerSelection.myBlueComms.get(0).getInputText());
            mmOutputStream.write("zone1off".getBytes());
        } catch (IOException e) {
            msg(e.toString());
        }
    }

    private void turnOnLed() {
        try {
            mmOutputStream.write("zone1on".getBytes());
        } catch (IOException e) {
            msg(e.toString());
        }
        startTimer();
//        try {
//            mmOutputStream.write("TO".getBytes());
//            inputText2.setText(btConn.getInputText());
//        } catch (IOException e) {
//            msg(e.toString());
//        }
    }

    private void startTimer() {
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                //int seconds = (int) (millis / 1000);
                //seconds = seconds % 60;

                //Log.d("Timer", String.valueOf(seconds));

                if (millis % 10 == 0) {
                    selectZoneToHit();
                }
                detectHit();

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void selectZoneToHit() {
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(zoneCount) + 1;
        String msg = String.valueOf(randomInt) + "n";
        for (int i = 1; i < 2; i++) {
            sendWithDelay(msg, 333 * i);
        }
    }

    private void sendWithDelay(final String msg, int delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (BTConnection bt : PlayerSelection.myBlueComms) {
                    try {
                        Log.d("send", msg);
                        bt.getMmSocket().getOutputStream().write(msg.getBytes());
                    } catch (IOException e) {
                        msg(e.toString());
                    }
                }
            }
        }, delay);
    }

    private void zoneOff(int zone) {
        String msg = String.valueOf(zone) + "f";
        for (int i = 1; i < 2; i++) {
            sendWithDelay(msg, 200 * i);
        }
    }

    private void detectHit() {
        for (BTConnection bt : PlayerSelection.myBlueComms) {

            //TODO Highscore erhöhen, wenn man einen Treffer gelandet hat

            switch (bt.getInputText()) {
                case "hit1\r":
                    Log.d("Treffer1", "true");
                    zoneOff(1);
                    bt.resetInputText();
                    playSound(HIT_SOUND_NORMAL);
                    break;
                case "hit2\r":
                    Log.d("Treffer2", "true");
                    zoneOff(2);
                    bt.resetInputText();
                    playSound(HIT_SOUND_NORMAL_2);
                    break;
                case "hit3\r":
                    Log.d("Treffer3", "true");
                    zoneOff(3);
                    bt.resetInputText();
                    playSound(HIT_SOUND_NORMAL);
                    break;
                case "hit4\r":
                    Log.d("Treffer4", "true");
                    zoneOff(4);
                    bt.resetInputText();
                    playSound(HIT_SOUND_NORMAL);
                    break;
            }
        }
    }

    private void playSound(int soundCode){
        if(soundCode == HIT_SOUND_NORMAL){
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hit);
            int maxVolume = 100;
            int currVolume = 100; //the volume we actually want
            float log1 = (float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));
            mediaPlayer.setVolume(1.0f-log1, 1.0f-log1);
            mediaPlayer.start();
        }
        if(soundCode == HIT_SOUND_LIGHTSABER){
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.lightsaberhit);
            int maxVolume = 100;
            int currVolume = 100; //the volume we actually want
            float log1 = (float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));
            mediaPlayer.setVolume(1.0f-log1, 1.0f-log1);
            mediaPlayer.start();
        }
        if(soundCode == HIT_SOUND_NORMAL_2){
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hit_2);
            int maxVolume = 100;
            int currVolume = 100; //the volume we actually want
            float log1 = (float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));
            mediaPlayer.setVolume(1.0f-log1, 1.0f-log1);
            mediaPlayer.start();
        }
    }
}
