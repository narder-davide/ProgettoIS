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
import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class MainActivity extends AppCompatActivity implements RobotControl.OnTasksFinished {

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
        Log.i("CAL","finished calib");
        r.getCoinAt(4,3);
    }

    @Override
    public void columnRead(int c) {
        Log.i("CAL","Column: "+c);
    }
    @Override
    public void colorRead(LightSensor.Color color) {
        Log.i("CAL","Color: "+color);
        r.dropToken(1);
    }
}
