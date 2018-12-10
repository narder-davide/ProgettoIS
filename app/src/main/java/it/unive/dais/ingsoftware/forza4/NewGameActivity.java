package it.unive.dais.ingsoftware.forza4;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

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

        // Associazione robot con Bluetooth
        try {
            BluetoothConnection conn = new BluetoothConnection("F4Bot");
            Channel channel = null;
            channel = conn.connect();


            ev3 = new EV3(new SpooledAsyncChannel(channel));
            r = new RobotControl(ev3,this);

            // Dà inizio al gioco
            startGame();
        }catch(Exception e){
            Toast.makeText(this,"Errore Bluetooth",Toast.LENGTH_LONG).show();
        }
    }

    private void startGame(){
        // Inizio logica di gioco
        gameLogic = new GameLogic(gameGrid, lastGame);
        gameLogic.initializeGame();

        userCoinCount.setText("" + gameLogic.getUserCoin());
        robotCoinCount.setText("" + gameLogic.getRobotCoin());

        startTimer();

        r.calibrate();

        // MOSSA UTENTE
        textTurno.setText(R.string.textTurnoGiocatore);
    }

    private char checkWin(){
        win = gameLogic.winner();

        if (win != 'H') {
            if (win == 'R') {    // vince RED - UTENTE
                textTurno.setText(R.string.textRedWin);
                Toast.makeText(this, "VINCE IL ROSSO", Toast.LENGTH_LONG).show();
            }
            else if (win == 'Y') {   // vince YELLOW - ROBOT
                textTurno.setText(R.string.textYellowWin);
                Toast.makeText(this, "VINCE IL GIALLO", Toast.LENGTH_LONG).show();
            }
            else if (win == 'X') {  // partita patta (gettoni esauriti)
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

        return win;
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
                    //Toast.makeText(getApplicationContext(), "InterruptedException occurred in timer", Toast.LENGTH_LONG).show();
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

        searchCoin(c);
    }

    private void searchCoin(int c) {
        r.getCoinAt(gameLogic.quote[c], c);
    }

    @Override
    public void colorRead(LightSensor.Color color, int c) {
        if (color == LightSensor.Color.RED){
            runOnUiThread(()-> {
                gameLogic.setCoin(c, 'R');
            });
            gameLogic.incrementTurno();
            runOnUiThread(()-> {
                char winner = this.checkWin();
                if (winner == 'R'){
                    return;
                }
            });

            coordinateRobot = gameLogic.calculateRobotAction(diff);
            runOnUiThread(()-> {
                gameLogic.setCoin(coordinateRobot, 'Y');
            });
            gameLogic.incrementTurno();
            runOnUiThread(()-> {
                char winner = this.checkWin();
                if (winner == 'Y'){
                    return;
                }
            });

            runOnUiThread(()->{
                decreaseRobotCoin();
            });
            this.r.dropToken(coordinateRobot);
        }
        else if (c < COLS-1){
            this.r.getCoinAt(gameLogic.quote[c+1],c+1);
        }
    }
}
