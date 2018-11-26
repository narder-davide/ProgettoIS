package it.unive.dais.ingsoftware.forza4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import it.dais.forza4.R;

public class NewGameActivity extends AppCompatActivity {

    private final int MAX_COIN = 20;

    TextView timerValue = null;
    Thread threadTimer = null;
    int minutes = 0, seconds = 0;

    private int userCoin = MAX_COIN;
    private int robotCoin = MAX_COIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        timerValue = findViewById(R.id.timerValue);

        TextView userCoinCount = (TextView)findViewById(R.id.userCoinCount);
        TextView robotCoinCount = (TextView)findViewById(R.id.robotCoinCount);

        userCoinCount.setText("" + userCoin);
        robotCoinCount.setText("" + robotCoin);

        startClock();
    }

    @Override
    protected void onPause(){
        super.onPause();

        minutes = 0;
        seconds = 0;
        threadTimer.interrupt();
    }

    private void startClock(){
        threadTimer = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seconds++;
                                if (seconds == 60){
                                    minutes++;
                                    seconds = 0;
                                }
                                String curTime = String.format("%02d:%02d", minutes, seconds);
                                timerValue.setText(curTime);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        threadTimer.start();
    }
}
