package it.unive.dais.ingsoftware.forza4;

import android.bluetooth.BluetoothAdapter;
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
import android.widget.Toast;

import java.io.IOException;

import it.dais.forza4.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class MainActivity extends AppCompatActivity {

    private Button newGameButton,loadGameButton,statisticsButton,optionsButton;

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
        Boolean sound = settings.getBoolean("SOUND", true);
        String LastGame = settings.getString("LASTGAME", "");
       
        // Recupero bottone per NUOVA PARTITA
        newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("LASTGAME", "");
                editor.commit();
                Intent openNewGameActivity = new Intent(MainActivity.this, NewGameActivity.class);
                startActivity(openNewGameActivity);
            }
        });

        // Recupero bottone per CARICA PARTITA
        loadGameButton = findViewById(R.id.loadGameButton);
        loadGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openLoadGameActivity = new Intent(MainActivity.this, LoadGameActivity.class);
                startActivity(openLoadGameActivity);
            }
        });

        // Recupero bottone per STATISTICHE
        statisticsButton = findViewById(R.id.statisticsButton);
        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openStatisticsActivity = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(openStatisticsActivity);
            }
        });

        // Recupero bottone per OPZIONI
        optionsButton = findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openOptionsActivity = new Intent(MainActivity.this, OptionsActivity.class);
                startActivity(openOptionsActivity);
            }
        });
        
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            enableDisableButtons(false);
            Toast.makeText(this,"Dispositivo non supporta Bluetooth",Toast.LENGTH_LONG).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                enableDisableButtons(false);
                Toast.makeText(this,"Bluetooth non abilitato",Toast.LENGTH_LONG).show();
            }
        }

    }

    private void enableDisableButtons(boolean b) {
        newGameButton.setEnabled(b);
        loadGameButton.setEnabled(b);
    }
}
