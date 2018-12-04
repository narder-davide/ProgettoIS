package it.unive.dais.ingsoftware.forza4;

import android.content.SharedPreferences;
import android.os.PowerManager;
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

import java.io.IOException;

import it.dais.forza4.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class NewGameActivity extends AppCompatActivity implements RobotControl.OnTasksFinished {

    final int ROWS = 6;
    final int COLS = 7;

    TextView userCoinCount;
    TextView robotCoinCount;

    TextView textTurno;

    SharedPreferences settings;
    SharedPreferences.Editor editor;
    String lastGame;
    String diff;

    GameLogic gameLogic;

    TableLayout gameGrid;

    char win = 'X';
    int coordinateRobot;

    TextView timerValue = null;
    Thread threadTimer = null;
    int minutes = 0, seconds = 0;

    private RobotControl r;
    private EV3 ev3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_new_game);

        // Gestione delle impostazioni di gioco
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = settings.edit();
        lastGame = settings.getString("LASTGAME", "");
        diff = settings.getString("DIFFICULT", "easy");

        // Ottenimento degli oggetti grafici
        timerValue = findViewById(R.id.timerValue);

        userCoinCount = findViewById(R.id.userCoinCount);
        robotCoinCount = findViewById(R.id.robotCoinCount);

        textTurno = findViewById(R.id.textTurno);

        gameGrid = findViewById(R.id.gamegrid);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*TableRow row0 = findViewById(R.id.row0);
        TableRow row1 = findViewById(R.id.row1);
        TableRow row2 = findViewById(R.id.row2);
        TableRow row3 = findViewById(R.id.row3);
        TableRow row4 = findViewById(R.id.row4);
        TableRow row5 = findViewById(R.id.row5);*/

        // Associazione robot con Bluetooth
        BluetoothConnection conn = new BluetoothConnection("F4Bot");
        Channel channel = null;
        try {
            channel = conn.connect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ev3 = new EV3(new SpooledAsyncChannel(channel));

        r = new RobotControl(ev3,this);
        r.calibrate();

        // Dà inizio al gioco
        startGame();
    }

    private void startGame(){
        // Inizio logica di gioco
        gameLogic = new GameLogic(gameGrid, lastGame);
        gameLogic.initializeGame();

        userCoinCount.setText("" + gameLogic.getUserCoin());
        robotCoinCount.setText("" + gameLogic.getRobotCoin());

        startTimer();

        // MOSSA UTENTE
        textTurno.setText(R.string.textTurnoGiocatore);
        /*
        do {
            // MOSSA UTENTE
            textTurno.setText(R.string.textTurnoGiocatore);

            decreaseUserCoin();

            // MOSSA ROBOT
            textTurno.setText(R.string.textTurnoRobot);
            coordinateRobot = gameLogic.calculateRobotAction(diff);
            gameLogic.setCoin(coordinateRobot[0],coordinateRobot[1],'Y');

            r.dropToken(coordinateRobot[1]);

            decreaseRobotCoin();

            win = gameLogic.winner();
            gameLogic.incrementTurno();
        } while(win == 'H');*/


        if (win == 'R'){    // vince RED - UTENTE
            textTurno.setText(R.string.textRedWin);
            Toast.makeText(this, "VINCE IL ROSSO", Toast.LENGTH_LONG).show();
        }
        else if (win == 'Y'){   // vince YELLOW - ROBOT
            textTurno.setText(R.string.textYellowWin);
            Toast.makeText(this, "VINCE IL GIALLO", Toast.LENGTH_LONG).show();
        }
        else {  // partita patta (gettoni esauriti)
            textTurno.setText(R.string.textPartitaPatta);
            Toast.makeText(this, "PARTITA PATTA", Toast.LENGTH_LONG).show();
        }

        /* Salvataggio delle statistiche SOLO a fine partita */
        if (diff.compareTo("easy") == 0){
            int eg = settings.getInt("easyGiocate",0);
            editor.putInt("easyGiocate", eg+1);

            int ev = settings.getInt("easyVinte", 0);
            if (win == 'R') {
                editor.putInt("easyVinte", ev + 1);
            }

            String t = settings.getString("easyTempoGioco", "00:00");
            String[] split_temp = t.split(":");
            int[] split = new int[split_temp.length];
            split[0] = Integer.parseInt(split_temp[0]);
            split[1] = Integer.parseInt(split_temp[1]);
            t = String.format("%02d:%02d", split[0]+minutes, split[1]+seconds);
            editor.putString("easyTempoGioco", t);

            if (eg != 0){
                editor.putInt("easyPCVittore", (ev/eg)*100);
            }
            else {
                editor.putInt("easyPCVittore", 0);
            }
        }
        else if (diff.compareTo("norm") == 0){
            int mg = settings.getInt("middleGiocate",0);
            editor.putInt("middleGiocate", mg+1);

            int mv = settings.getInt("middleVinte", 0);
            if (win == 'R') {
                editor.putInt("middleVinte", mv + 1);
            }

            String t = settings.getString("middleTempoGioco", "00:00");
            String[] split_temp = t.split(":");
            int[] split = new int[split_temp.length];
            split[0] = Integer.parseInt(split_temp[0]);
            split[1] = Integer.parseInt(split_temp[1]);
            t = String.format("%02d:%02d", split[0]+minutes, split[1]+seconds);
            editor.putString("middleTempoGioco", t);

            if (mg != 0){
                editor.putInt("middlePCVittore", (mv/mg)*100);
            }
            else {
                editor.putInt("middlePCVittore", 0);
            }
        }
        else {
            int hg = settings.getInt("hardGiocate",0);
            editor.putInt("hardGiocate", hg+1);

            int hv = settings.getInt("hardVinte", 0);
            if (win == 'R') {
                editor.putInt("hardVinte", hv + 1);
            }

            String t = settings.getString("hardTempoGioco", "00:00");
            String[] split_temp = t.split(":");
            int[] split = new int[split_temp.length];
            split[0] = Integer.parseInt(split_temp[0]);
            split[1] = Integer.parseInt(split_temp[1]);
            t = String.format("%02d:%02d", split[0]+minutes, split[1]+seconds);
            editor.putString("hardTempoGioco", t);

            if (hg != 0){
                editor.putInt("hardPCVittore", (hv/hg)*100);
            }
            else {
                editor.putInt("hardPCVittore", 0);
            }
        }
        editor.commit();
        /* fine salvataggio statistiche di gioco */
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
        gameLogic.decreaseUserCoin();
        userCoinCount.setText("" + gameLogic.getUserCoin());
    }

    private void decreaseRobotCoin(){
        gameLogic.decreaseRobotCoin();
        robotCoinCount.setText("" + gameLogic.getRobotCoin());
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
                    //Toast.makeText(getApplicationContext(), "InterruptedException occurred", Toast.LENGTH_LONG);
                }
            }
        };
        threadTimer.start();
    }

    @Override
    public void calibrated() {

    }

    @Override
    public void columnRead(int c) {
        runOnUiThread(()->{
            decreaseUserCoin();
            textTurno.setText(R.string.textTurnoRobot);
        });

        //trova coin dell'utente
        searchCoin(c);

        // MOSSA ROBOT

    }

    private void searchCoin(int c) {
        for (int j=0;j<gameLogic.quote.length;j++) {
            r.getCoinAt(gameLogic.quote[c], c);
            // aggiungere 1 alla posizione quote[c] presente in GameLogic
            // chiamare setCoin in GameLogic alla posizione trovata
        }
    }

    @Override
    public void colorRead(LightSensor.Color color,int r,int c) {
        if (color == LightSensor.Color.RED){
            gameLogic.setCoin(c,'R');
            coordinateRobot = gameLogic.calculateRobotAction(diff);
            gameLogic.setCoin(coordinateRobot,'Y');

            runOnUiThread(()->{
                decreaseRobotCoin();
            });
            this.r.dropToken(coordinateRobot);
        }
        else if (c<6){
            this.r.getCoinAt(gameLogic.quote[c+1],c+1);
        }
    }
}
