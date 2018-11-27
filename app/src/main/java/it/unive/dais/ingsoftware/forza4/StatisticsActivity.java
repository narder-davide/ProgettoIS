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

import it.dais.forza4.R;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_statistics);


        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();

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
    }
}
