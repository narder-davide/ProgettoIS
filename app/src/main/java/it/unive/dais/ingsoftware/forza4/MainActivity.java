package it.unive.dais.ingsoftware.forza4;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import it.dais.forza4.R;

public class MainActivity extends AppCompatActivity {

    private Button newGameButton;
    private Button loadGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        IntentFilter btEvents = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, btEvents);

        // Gestione delle impostazioni di gioco
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();


        // Valori per statistiche
        int app;
        String app_s;

        app = settings.getInt("easyGiocate",0);
        if (app == 0){ editor.putInt("easyGiocate", 0); }
        app = settings.getInt("easyVinte",0);
        if (app == 0){ editor.putInt("easyVinte", 0); }
        app_s = settings.getString("easyTempoGioco","00:00");
        if (app_s.compareTo("00:00") == 0){ editor.putString("easyTempoGioco", "00:00"); }
        app = settings.getInt("easyPCVittore",0);
        if (app == 0){ editor.putInt("easyPCVittore", 0); }

        app = settings.getInt("middleGiocate",0);
        if (app == 0){ editor.putInt("middleGiocate", 0); }
        app = settings.getInt("middleVinte",0);
        if (app == 0){ editor.putInt("middleVinte", 0); }
        app_s = settings.getString("middleTempoGioco","00:00");
        if (app_s.compareTo("00:00") == 0){ editor.putString("middleTempoGioco", "00:00"); }
        app = settings.getInt("middlePCVittore",0);
        if (app == 0){ editor.putInt("middlePCVittore", 0); }

        app = settings.getInt("hardGiocate",0);
        if (app == 0){ editor.putInt("hardGiocate", 0); }
        app = settings.getInt("hardVinte",0);
        if (app == 0){ editor.putInt("hardVinte", 0); }
        app_s = settings.getString("hardTempoGioco","00:00");
        if (app_s.compareTo("00:00") == 0){ editor.putString("hardTempoGioco", "00:00"); }
        app = settings.getInt("hardPCVittore",0);
        if (app == 0){ editor.putInt("hardPCVittore", 0); }

        editor.commit();
       
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
        
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            enableDisableButtons(false);
            Toast.makeText(this,"Il dispositivo non supporta il Bluetooth",Toast.LENGTH_LONG).show();
        }
        else {
            if (!bluetoothAdapter.isEnabled()) {
                enableDisableButtons(false);
                Toast.makeText(this,"Bluetooth non abilitato",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action!=null)
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch(state) {
                        case BluetoothAdapter.STATE_OFF:
                            enableDisableButtons(false);
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            enableDisableButtons(false);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            enableDisableButtons(true);
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            enableDisableButtons(true);
                            break;
                    }
                }
        }
    };

    private void enableDisableButtons(boolean b) {
        newGameButton.setEnabled(b);
        loadGameButton.setEnabled(b);
    }
}
