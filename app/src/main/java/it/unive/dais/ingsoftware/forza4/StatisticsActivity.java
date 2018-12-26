package it.unive.dais.ingsoftware.forza4;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import it.dais.forza4.R;

public class StatisticsActivity extends AppCompatActivity {

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    TextView easyGiocate;
    TextView easyVinte;
    TextView easyTempoGioco;
    TextView easyPCVittorie;

    TextView middleGiocate;
    TextView middleVinte;
    TextView middleTempoGioco;
    TextView middlePCVittorie;

    TextView hardGiocate;
    TextView hardVinte;
    TextView hardTempoGioco;
    TextView hardPCVittorie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_statistics);

        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = settings.edit();

        easyGiocate = findViewById(R.id.easyGiocate);
        easyVinte = findViewById(R.id.easyVinte);
        easyTempoGioco = findViewById(R.id.easyTempoGioco);
        easyPCVittorie = findViewById(R.id.easyPcVittorie);

        middleGiocate = findViewById(R.id.middleGiocate);
        middleVinte = findViewById(R.id.middleVinte);
        middleTempoGioco = findViewById(R.id.middleTempoGioco);
        middlePCVittorie = findViewById(R.id.middlePcVittorie);

        hardGiocate = findViewById(R.id.hardGiocate);
        hardVinte = findViewById(R.id.hardVinte);
        hardTempoGioco = findViewById(R.id.hardTempoGioco);
        hardPCVittorie = findViewById(R.id.hardPcVittorie);

        Button buttonResetStat = findViewById(R.id.buttonResetStat);

        Boolean vibr = settings.getBoolean("VIBRATION", true);
        // Vibrazione all'apertura dell'activity
        if (vibr == true) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            //long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
            //v.vibrate(pattern, -1); // -1 indica di vibrare una sola volta
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else {
                v.vibrate(300);
            }
        }

        this.updateStats();

        buttonResetStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("easyGiocate", 0);
                editor.putInt("easyVinte", 0);
                editor.putString("easyTempoGioco", "00:00");
                editor.putInt("easyPCVittore", 0);

                editor.putInt("middleGiocate", 0);
                editor.putInt("middleVinte", 0);
                editor.putString("middleTempoGioco", "00:00");
                editor.putInt("middlePCVittore", 0);

                editor.putInt("hardGiocate", 0);
                editor.putInt("hardVinte", 0);
                editor.putString("hardTempoGioco", "00:00");
                editor.putInt("hardPCVittore", 0);

                editor.commit();

                updateStats();
            }
        });
    }

    private void updateStats(){
        // MODALITA' easy
        easyGiocate.setText("" + settings.getInt("easyGiocate", 0));
        easyVinte.setText("" + settings.getInt("easyVinte", 0));
        easyTempoGioco.setText(settings.getString("easyTempoGioco", "00:00"));
        easyPCVittorie.setText("" + settings.getInt("easyPCVittore", 0)  + "%");

        // MODALITA' normal
        middleGiocate.setText("" + settings.getInt("middleGiocate", 0));
        middleVinte.setText("" + settings.getInt("middleVinte", 0));
        middleTempoGioco.setText(settings.getString("middleTempoGioco", "00:00"));
        middlePCVittorie.setText("" + settings.getInt("middlePCVittore", 0)  + "%");

        // MODALITA' hard
        hardGiocate.setText("" + settings.getInt("hardGiocate", 0));
        hardVinte.setText("" + settings.getInt("hardVinte", 0));
        hardTempoGioco.setText(settings.getString("hardTempoGioco", "00:00"));
        hardPCVittorie.setText("" + settings.getInt("hardPCVittore", 0)  + "%");
    }
}
