package it.unive.dais.ingsoftware.forza4;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import it.dais.forza4.R;

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
        v.vibrate(pattern, -1); // -1 indica di vibrare una sola volta

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else {
            v.vibrate(500);
        }*/

        // DA METTERE SUL MAIN
            SharedPreferences difficultSettingsMAIN = getSharedPreferences("DIFFICULT", 0);
            String diffMAIN = difficultSettingsMAIN.getString("DIFFICULT", "easy"); // ritorna il livello di difficoltà
            SharedPreferences.Editor editorDifficult = null;
            editorDifficult = difficultSettingsMAIN.edit();
            editorDifficult.putString("DIFFICULT", "easy" );
            editorDifficult.commit();

            SharedPreferences stateGame = getSharedPreferences("SAVEDGAME", 0);
            String savedGame = stateGame.getString("SAVEDGAME", ""); // ritorna la stringa corrispondente alla partita salvata
        // END


        RadioGroup radioGroupDifficolta = findViewById(R.id.radioGroupDifficolta);
        RadioButton radioEasy = findViewById(R.id.radioEasy);
        RadioButton radioMid = findViewById(R.id.radioMid);
        RadioButton radioHard = findViewById(R.id.radioHard);

        // classe SharedPreference per salvare le modifiche e stringa che corrisponde allo stato della partita
        SharedPreferences difficultSettings = getSharedPreferences("DIFFICULT", 0);
        SharedPreferences vibrationSettings = getSharedPreferences("VIBRATION", 0);

        int vibration = vibrationSettings.getInt("VIBRATION", 0);

        String diff = difficultSettings.getString("DIFFICULT", "noValueInsert");
        if (diff == "easy" || diff == "noValueInsert"){
            radioEasy.setChecked(true);
            radioMid.setChecked(false);
            radioHard.setChecked(false);
        }
        else if (diff == "mid"){
            radioEasy.setChecked(false);
            radioMid.setChecked(true);
            radioHard.setChecked(false);
        }
        else if (diff == "hard"){
            radioEasy.setChecked(false);
            radioMid.setChecked(false);
            radioHard.setChecked(true);
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
                        editor.putString("DIFFICULT","easy" );
                    }
                }
                else if (checkedRadioButton.getId() == R.id.radioMid){
                    if (isChecked){
                        editor = difficultSettings.edit();
                        editor.putString("DIFFICULT","mid" );
                    }
                }
                else {  // R.id.radioHard
                    if (isChecked){
                        editor = difficultSettings.edit();
                        editor.putString("DIFFICULT","hard" );
                    }
                }
                editor.commit();
            }
        });
    }
}
