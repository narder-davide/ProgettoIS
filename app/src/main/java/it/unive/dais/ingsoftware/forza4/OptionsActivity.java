package it.unive.dais.ingsoftware.forza4;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import it.dais.forza4.R;


public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        // Recupero componenti grafici
        RadioGroup radioGroupDifficolta = findViewById(R.id.radioGroupDifficolta);
        RadioButton radioEasy = findViewById(R.id.radioEasy);
        RadioButton radioMid = findViewById(R.id.radioMid);
        RadioButton radioHard = findViewById(R.id.radioHard);

        Switch switchVibrazione = findViewById(R.id.switchVibrazione);

        // DA METTERE SUL MAIN
            SharedPreferences difficultSettingsMAIN = getSharedPreferences("DIFFICULT", 0);
            String diffMAIN = difficultSettingsMAIN.getString("DIFFICULT", "easy"); // ritorna il livello di difficoltà
            SharedPreferences.Editor editorDifficultMAIN = null;
            editorDifficultMAIN = difficultSettingsMAIN.edit();
            editorDifficultMAIN.putString("DIFFICULT", "easy" );
            editorDifficultMAIN.commit();

            SharedPreferences stateGame = getSharedPreferences("SAVEDGAME", 0);
            String savedGame = stateGame.getString("SAVEDGAME", ""); // ritorna la stringa corrispondente alla partita salvata
        // END





        // classe SharedPreference per salvare le opzioni di gioco e la stringa che corrisponde allo stato della partita
        SharedPreferences difficultSettings = getSharedPreferences("DIFFICULT", 0);
        SharedPreferences.Editor editorDifficult = null;
        String diff = difficultSettings.getString("DIFFICULT", "noValueInsert");

        SharedPreferences vibrationSettings = getSharedPreferences("VIBRATION", 0);
        SharedPreferences.Editor editorVibration = null;
        boolean vibration = vibrationSettings.getBoolean("VIBRATION", true);

        // Vibrazione all'apertura dell'activity
        if (vibration == true) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            //long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
            //v.vibrate(pattern, -1); // -1 indica di vibrare una sola volta
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }
        }



        // GESTIONE VIBRAZIONE
        if (vibration == true){
            switchVibrazione.setChecked(true);
            editorVibration = vibrationSettings.edit();
            editorVibration.putBoolean("VIBRATION", true);
            editorVibration.commit();
        }
        else {
            switchVibrazione.setChecked(false);
            editorVibration = vibrationSettings.edit();
            editorVibration.putBoolean("VIBRATION", false);
            editorVibration.commit();
        }

        switchVibrazione.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = null;
                if (isChecked){
                    editor = vibrationSettings.edit();
                    editor.putBoolean("VIBRATION", true);
                }
                else {
                    editor = vibrationSettings.edit();
                    editor.putBoolean("VIBRATION", false);
                }
                editor.commit();
            }
        });

        // GESTIONE DIFFICOLTA'
        if (diff.compareTo("easy") == 0 || diff.compareTo("noValueInsert") == 0){
            radioEasy.setChecked(true);
            radioMid.setChecked(false);
            radioHard.setChecked(false);
            editorDifficult = difficultSettings.edit();
            editorDifficult.putString("DIFFICULT", "easy");
            editorDifficult.commit();
        }
        else if (diff.compareTo("mid") == 0){
            radioEasy.setChecked(false);
            radioMid.setChecked(true);
            radioHard.setChecked(false);
            editorDifficult = difficultSettings.edit();
            editorDifficult.putString("DIFFICULT", "mid");
            editorDifficult.commit();
        }
        else if (diff.compareTo("hard") == 0){
            radioEasy.setChecked(false);
            radioMid.setChecked(false);
            radioHard.setChecked(true);
            editorDifficult = difficultSettings.edit();
            editorDifficult.putString("DIFFICULT", "hard");
            editorDifficult.commit();
        }

        radioGroupDifficolta.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                SharedPreferences.Editor editor = null;
                boolean isChecked = checkedRadioButton.isChecked();

                if (checkedRadioButton.getId() == R.id.radioEasy){
                    if (isChecked){
                        editor = difficultSettings.edit();
                        editor.putString("DIFFICULT", "easy");
                    }
                }
                else if (checkedRadioButton.getId() == R.id.radioMid){
                    if (isChecked){
                        editor = difficultSettings.edit();
                        editor.putString("DIFFICULT", "mid");
                    }
                }
                else {  // R.id.radioHard
                    if (isChecked){
                        editor = difficultSettings.edit();
                        editor.putString("DIFFICULT", "hard");
                    }
                }
                editor.commit();
            }
        });
    }
}
