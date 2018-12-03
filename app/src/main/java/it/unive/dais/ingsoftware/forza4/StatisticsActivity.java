package it.unive.dais.ingsoftware.forza4;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import it.dais.forza4.R;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_statistics);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();

        TextView easyGiocate = findViewById(R.id.easyGiocate);
        TextView easyVinte = findViewById(R.id.easyVinte);
        TextView easyTempoGioco = findViewById(R.id.easyTempoGioco);
        TextView easyPCVittorie = findViewById(R.id.easyPcVittorie);

        TextView middleGiocate = findViewById(R.id.middleGiocate);
        TextView middleVinte = findViewById(R.id.middleVinte);
        TextView middleTempoGioco = findViewById(R.id.middleTempoGioco);
        TextView middlePCVittorie = findViewById(R.id.middlePcVittorie);

        TextView hardGiocate = findViewById(R.id.hardGiocate);
        TextView hardVinte = findViewById(R.id.hardVinte);
        TextView hardTempoGioco = findViewById(R.id.hardTempoGioco);
        TextView hardPCVittorie = findViewById(R.id.hardPcVittorie);

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

        // MODALITA' easy
        easyGiocate.setText(settings.getInt("easyGiocate", 0));
        easyVinte.setText(settings.getInt("easyVinte", 0));
        easyTempoGioco.setText(settings.getString("easyTempoGioco", "00:00"));
        easyPCVittorie.setText(settings.getInt("easyPCVittore", 0));

        // MODALITA' normal
        middleGiocate.setText(settings.getInt("middleGiocate", 0));
        middleVinte.setText(settings.getInt("middleVinte", 0));
        middleTempoGioco.setText(settings.getString("middleTempoGioco", "00:00"));
        middlePCVittorie.setText(settings.getInt("middlePCVittore", 0));

        // MODALITA' hard
        hardGiocate.setText(settings.getInt("hardGiocate", 0));
        hardVinte.setText(settings.getInt("hardVinte", 0));
        hardTempoGioco.setText(settings.getString("hardTempoGioco", "00:00"));
        hardPCVittorie.setText(settings.getInt("hardPCVittore", 0));
    }
}
