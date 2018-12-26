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
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import it.dais.forza4.R;

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_options);

        // Recupero componenti grafici
        RadioGroup radioGroupDifficolta = findViewById(R.id.radioGroupDifficolta);
        RadioButton radioEasy = findViewById(R.id.radioEasy);
        RadioButton radioMid = findViewById(R.id.radioMid);
        RadioButton radioHard = findViewById(R.id.radioHard);

        Switch switchVibrazione = findViewById(R.id.switchVibrazione);

        Switch switchSound = findViewById(R.id.switchSound);

        // Gestione delle opzioni di gioco
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();

        Boolean vibr = settings.getBoolean("VIBRATION", true);
        Boolean sound = settings.getBoolean("SOUND", true);
        String diff = settings.getString("DIFFICULT", "easy");

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

        /*** GESTIONE VIBRAZIONE ***/
        if (vibr == true){
            switchVibrazione.setChecked(true);
            editor.putBoolean("VIBRATION", true);
            editor.commit();
        }
        else {
            switchVibrazione.setChecked(false);
            editor.putBoolean("VIBRATION", false);
            editor.commit();
        }

        switchVibrazione.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editor.putBoolean("VIBRATION", true);
                }
                else {
                    editor.putBoolean("VIBRATION", false);
                }
                editor.commit();
            }
        });

        /*** GESTIONE SUONI ***/
        if (sound == true){
            switchSound.setChecked(true);
            editor.putBoolean("SOUND", true);
            editor.commit();
        }
        else {
            switchSound.setChecked(false);
            editor.putBoolean("SOUND", false);
            editor.commit();
        }

        switchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editor.putBoolean("SOUND", true);
                }
                else {
                    editor.putBoolean("SOUND", false);
                }
                editor.commit();
            }
        });

        /*** GESTIONE DIFFICOLTA' ***/
        if (diff.compareTo("easy") == 0){
            radioEasy.setChecked(true);
            radioMid.setChecked(false);
            radioHard.setChecked(false);
            editor.putString("DIFFICULT", "easy");
            editor.commit();
        }
        else if (diff.compareTo("norm") == 0){
            radioEasy.setChecked(false);
            radioMid.setChecked(true);
            radioHard.setChecked(false);
            editor.putString("DIFFICULT", "norm");
            editor.commit();
        }
        else if (diff.compareTo("hard") == 0){
            radioEasy.setChecked(false);
            radioMid.setChecked(false);
            radioHard.setChecked(true);
            editor.putString("DIFFICULT", "hard");
            editor.commit();
        }

        radioGroupDifficolta.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);

                if (checkedRadioButton.getId() == R.id.radioEasy){
                    editor.putString("DIFFICULT", "easy");
                }
                else if (checkedRadioButton.getId() == R.id.radioMid){
                    editor.putString("DIFFICULT", "norm");
                }
                else {  // R.id.radioHard
                    editor.putString("DIFFICULT", "hard");
                }
                editor.commit();
            }
        });
    }
}
