package it.unive.dais.ingsoftware.forza4;

import android.content.Intent;
import android.os.AsyncTask;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;

import it.dais.forza4.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;

public class MainActivity extends AppCompatActivity implements RobotControl.OnCalibrationFinished {

    private RobotControl r;
    private EV3 ev3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Gestione delle impostazioni di gioco
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();

        Boolean vibr = settings.getBoolean("VIBRATION", true);
        String diff = settings.getString("DIFFICULT", "easy");
        String statusLastGame = settings.getString("LASTGAME", "");
        editor.putBoolean("VIBRATION", vibr);
        editor.putString("DIFFICULT", diff);
        editor.putString("LASTGAME", statusLastGame);
        editor.commit();

        // Recupero bottone per NUOVA PARTITA
        Button newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNewGameActivity = new Intent(MainActivity.this, NewGameActivity.class);
                startActivity(openNewGameActivity);
            }
        });

        // Recupero bottone per CARICA PARTITA
        Button loadGameButton = findViewById(R.id.loadGameButton);
        loadGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openLoadGameActivity = new Intent(MainActivity.this, LoadGameActivity.class);
                startActivity(openLoadGameActivity);
            }
        });

        // Recupero bottone per STATISTICHE
        Button statisticsButton = findViewById(R.id.statisticsButton);
        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openStatisticsActivity = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(openStatisticsActivity);
            }
        });

        // Recupero bottone per OPZIONI
        Button optionsButton = findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openOptionsActivity = new Intent(MainActivity.this, OptionsActivity.class);
                startActivity(openOptionsActivity);
            }
        });

        // Apertura connessione Bluetooth
        BluetoothConnection conn = new BluetoothConnection("F4Bot");
        Channel channel = null;
        try {
            channel = conn.connect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ev3 = new EV3(new SpooledAsyncChannel(channel));

        r=new RobotControl(ev3,this);
        r.calibrate();
    }
    @Override
    public void calibrated() {
        Log.i("CAL","finisched calib");
        ev3.cancel();
        AsyncTask<Void, Void, Void> a=new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                //mouvi in 1 e cade in 4    dist 3
                //Thread.sleep(7000);
                r.dropToken();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ev3.cancel();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                r.move(0,-1);
                return null;
            }
        };
        a.execute();
    }
}


/*
                for(int i=0;i<3;i++){
                    r.move(5,0);
                    Thread.sleep(7000);
                    r.move(0,6);
                    Thread.sleep(7000);
                    r.move(5,6);
                    Thread.sleep(7000);
                    r.move(0,0);
                    Thread.sleep(7000);
                }
                */