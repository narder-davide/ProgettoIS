package it.unive.dais.ingsoftware.forza4;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import it.dais.forza4.R;

public class NewGameActivity extends AppCompatActivity {

    TextView timerValue = findViewById(R.id.timerValue);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);


    }
}
