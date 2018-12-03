package it.unive.dais.ingsoftware.forza4;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import it.dais.forza4.R;

public class NewGameActivity extends AppCompatActivity {

    private final int MAX_COIN = 20;

    final int ROWS = 6;
    final int COLS = 7;

    private int userCoin = MAX_COIN;
    private int robotCoin = MAX_COIN;

    TextView userCoinCount;
    TextView robotCoinCount;

    TextView textTurno;

    SharedPreferences settings;
    String lastGame;
    String diff;

    GameLogic gameLogic;

    TableLayout gameGrid;

    char control;
    int[] coordinateRobot;

    TextView timerValue = null;
    Thread threadTimer = null;
    int minutes = 0, seconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_new_game);

        // Gestione delle impostazioni di gioco
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        lastGame = settings.getString("LASTGAME", "");
        diff = settings.getString("DIFFICULT", "easy");

        // Ottenimento degli oggetti grafici
        timerValue = findViewById(R.id.timerValue);

        userCoinCount = findViewById(R.id.userCoinCount);
        userCoinCount.setText("" + userCoin);

        robotCoinCount = findViewById(R.id.robotCoinCount);
        robotCoinCount.setText("" + robotCoin);

        textTurno = findViewById(R.id.textTurno);

        gameGrid = findViewById(R.id.gamegrid);

        /*TableRow row0 = findViewById(R.id.row0);
        TableRow row1 = findViewById(R.id.row1);
        TableRow row2 = findViewById(R.id.row2);
        TableRow row3 = findViewById(R.id.row3);
        TableRow row4 = findViewById(R.id.row4);
        TableRow row5 = findViewById(R.id.row5);*/

        // Dà inizio al gioco
        startGame();
    }

    private void startGame(){
        // Inizio logica di gioco
        gameLogic = new GameLogic(gameGrid, lastGame);
        gameLogic.initializeGame();

        gameLogic.setCoin(0,0,'Y');
        gameLogic.setCoin(5,6,'R');
        gameLogic.setCoin(0,6,'Y');
        gameLogic.setCoin(5,0,'R');
        gameLogic.setCoin(3,3,'Y');

        startTimer();

        do {
            // MOSSA UTENTE

            decreaseUserCoin();

            // MOSSA ROBOT
            coordinateRobot = gameLogic.calculateRobotAction(diff);
            decreaseRobotCoin();

            control = gameLogic.winner();
            gameLogic.incrementTurno();
        } while(control == 'H');

        if (control == 'R'){    // vince RED
            textTurno.setText(R.string.textRedWin);
            Toast.makeText(this, "VINCE IL ROSSO", Toast.LENGTH_LONG).show();
        }
        else if (control == 'Y'){   // vince YELLOW
            textTurno.setText(R.string.textYellowWin);
            Toast.makeText(this, "VINCE IL GIALLO", Toast.LENGTH_LONG).show();
        }
        else {  // partita patta (gettoni esauriti)
            textTurno.setText(R.string.textPartitaPatta);
            Toast.makeText(this, "PARTITA PATTA", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("LASTGAME", gameLogic.getLastGame());
        editor.commit();

        minutes = 0;
        seconds = 0;
        threadTimer.interrupt();
    }

    private void decreaseUserCoin(){
        this.userCoin--;
        userCoinCount.setText("" + userCoin);
    }

    private void decreaseRobotCoin(){
        this.robotCoin--;
        robotCoinCount.setText("" + robotCoin);
    }

    private void startTimer(){
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
                }
                catch (InterruptedException e) {
                    Toast.makeText(getApplicationContext(), "InterruptedException occurred", Toast.LENGTH_LONG);
                }
            }
        };
        threadTimer.start();
    }
}
