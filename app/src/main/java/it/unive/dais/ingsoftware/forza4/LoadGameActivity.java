package it.unive.dais.ingsoftware.forza4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import it.dais.forza4.R;

public class LoadGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_load_game);

        Button buttonLoad = findViewById(R.id.buttonLoad);
        Button buttonScan = findViewById(R.id.buttonScan);

        // Caricamento leggendo stringa salvata nelle SharedPreferences
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNewGameActivity = new Intent(LoadGameActivity.this, NewGameActivity.class);
                startActivity(openNewGameActivity);
            }
        });

        // Scansione della plancia di gioco e creazione della stringa corrispondente alla partita
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "";

                // lettura delle cella della plancia e str += VALORE_LETTO_DA_SENSORE_ROBOT

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("LASTGAME", str);
                editor.commit();

                Intent openNewGameActivity = new Intent(LoadGameActivity.this, NewGameActivity.class);
                startActivity(openNewGameActivity);
            }
        });
    }
}
