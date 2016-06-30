package com.led.led;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.led.led.archive.HitListener;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Maggi on 01.06.2016.
 * gets the BlueTooth connection from the PlayerSelection. Testclass to get the data transfer going.
 */
public class GameScreen extends ActionBarActivity {
    private BTConnection btConn;
    private static int[] highscore;
    private static TextView[] container;
    private TextView l_Player1, l_Player2;
    private static TextView debugView;
    private final int zoneCount = 2;
    private final long startTime = System.currentTimeMillis();
    private final Handler timerHandler = new Handler();
    private static MediaPlayer sound1;
    private static MediaPlayer sound2;
    private static MediaPlayer sound3;
    private static MediaPlayer soundfail1;
    private static MediaPlayer soundfail2;
    private static MediaPlayer soundfail3;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        String address = newint.getStringExtra(PlayerSelection.EXTRA_ADDRESS);

        highscore = new int[PlayerSelection.myBlueComms.size()];
        container = new TextView[2];
        for (int i = 0; i < PlayerSelection.myBlueComms.size(); i++) {
            for (int u = 0; u < zoneCount; u++) {
                String msg = String.valueOf(u);
                msg += msg + msg + msg;
                sendWithDelay(msg, 200 * u);
            }
            highscore[i] = 0;

            if (address.equalsIgnoreCase(PlayerSelection.myBlueComms.get(i).getAddress())) {
                Observer obs = new HitListener();
                btConn = PlayerSelection.myBlueComms.get(i);
                btConn.addObserver(obs);
                btConn.beginListenForData();
            }
        }

        if (btConn == null) {
            msg("Something went wrong");
            finish();
        }

        sound1 = MediaPlayer.create(getApplicationContext(), R.raw.hit);
        sound2 = MediaPlayer.create(getApplicationContext(), R.raw.hit_2);
        sound3 = MediaPlayer.create(getApplicationContext(), R.raw.lightsaberhit);
        soundfail1 = MediaPlayer.create(getApplicationContext(), R.raw.fail1);
        soundfail2 = MediaPlayer.create(getApplicationContext(), R.raw.fail2);
        soundfail3 = MediaPlayer.create(getApplicationContext(), R.raw.fail3);
        int maxVolume = 100;
        int currVolume = 100; //the volume we actually want
        float log1 = (float) (Math.log(maxVolume - currVolume) / Math.log(maxVolume));
        sound1.setVolume(1.0f - log1, 1.0f - log1);
        sound2.setVolume(1.0f - log1, 1.0f - log1);
        sound3.setVolume(1.0f - log1, 1.0f - log1);
        soundfail1.setVolume(1.0f - log1, 1.0f - log1);
        soundfail2.setVolume(1.0f - log1, 1.0f - log1);
        soundfail3.setVolume(1.0f - log1, 1.0f - log1);

        //view of the ledControl
        setContentView(R.layout.activity_game_screen);

        //call the widgtes
        l_Player1 = (TextView) findViewById(R.id.player1);
        l_Player2 = (TextView) findViewById(R.id.player2);
        TextView l_Score1 = (TextView) findViewById(R.id.scorePlayer1);
        TextView l_Score2 = (TextView) findViewById(R.id.scorePlayer2);
        debugView = (TextView) findViewById(R.id.debug);
        Switch switch_On = (Switch) findViewById(R.id.switch1);

        l_Player1.setText("Casper");
        l_Player2.setText("Markus");
        container[0]= l_Score1;
        container[1]= l_Score2;
        l_Score1.setText("0");
        l_Score2.setText("0");

        //SeekBar brightness = (SeekBar) findViewById(R.id.seekBar);
        //lumn = (TextView) findViewById(R.id.lumn);


        //commands to be sent to bluetooth
        switch_On.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();      //method to turn on
            }
        });

        l_Player1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void startTimer() {
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                //int seconds = (int) (millis / 1000);
                //seconds = seconds % 60;

                //Log.d("Timer", String.valueOf(seconds));

                if (millis % 6 == 0) {
                    selectZoneToHit();
                }
                //detectHit();
                //l_Score1.setText(String.valueOf(highscore[0]));
                //inputText2.setText(PlayerSelection.myBlueComms.get(0).getInputText());

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void selectZoneToHit() {
        Random randomGenerator = new Random();
        //Zahl >0 und <zoneCount. nextint schneidet Kommastellen ab
        int randomInt = randomGenerator.nextInt(zoneCount);
        //Zone wird mit 50% Chance auf + oder - gesetzt. z.B 0000 Zone0 +, 4444 Zone 0 -
        if ( randomGenerator.nextInt(2) == 0){
            randomInt += 4;
        }
        String msg = String.valueOf(randomInt);
        msg += msg + msg + msg;
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

//    private void zoneOff(int zone) {
//        String msg = String.valueOf(zone);
//        msg += msg + msg + msg;
//        for (int i = 1; i < 2; i++) {
//            sendWithDelay(msg, 200 * i);
//        }
//    }

//    private void gotHit(int PlayerID, int zoneID) {
//        Log.d("Treffer" + zoneID, "Score" + String.valueOf(highscore[PlayerID]));
//        highscore[PlayerID]++;
//        zoneOff(zoneID);
//        PlayerSelection.myBlueComms.get(PlayerID).resetInputText();
//        playSound(HIT_SOUND_NORMAL_2);
//    }

//    private static void playSound(int soundCode) {
//        if (soundCode == HIT_SOUND_NORMAL) {
//            sound1.start();
//        }
//        if (soundCode == HIT_SOUND_LIGHTSABER) {
//            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.lightsaberhit);
//            int maxVolume = 100;
//            int currVolume = 100; //the volume we actually want
//            float log1 = (float) (Math.log(maxVolume - currVolume) / Math.log(maxVolume));
//            mediaPlayer.setVolume(1.0f - log1, 1.0f - log1);
//            mediaPlayer.start();
//        }
//        if (soundCode == HIT_SOUND_NORMAL_2) {
//            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hit_2);
//            int maxVolume = 100;
//            int currVolume = 100; //the volume we actually want
//            float log1 = (float) (Math.log(maxVolume - currVolume) / Math.log(maxVolume));
//            mediaPlayer.setVolume(1.0f - log1, 1.0f - log1);
//            mediaPlayer.start();
//        }
//    }

    public static void setScore(Observable o, String msg) {
        int i = 0;
        for (BTConnection bt : PlayerSelection.myBlueComms) {
            if (bt.equals(o)) {
                Log.d("ObserverOf", "Player" + i);

                switch (msg) {
                    case "h1\r":
                        highscore[i] += 1;
                        sound1.start();
                        break;
                    case "h2\r":
                        highscore[i] += 2;
                        sound2.start();
                        break;
                    case "h3\r":
                        highscore[i] += 3;
                        sound3.start();
                        break;
                    case "n1\r":
                        highscore[i] -= 1;
                        soundfail1.start();
                        break;
                    case "n2\r":
                        highscore[i] -= 1;
                        soundfail2.start();
                        break;
                    case "n3\r":
                        highscore[i] -= 1;
                        soundfail3.start();
                        break;
                }
                container[i].setText(String.valueOf(highscore[i]));
            }
            i++;
        }
        debugView.setText(msg);
    }
}
