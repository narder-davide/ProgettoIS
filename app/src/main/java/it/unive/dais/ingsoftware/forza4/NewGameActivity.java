package it.unive.dais.ingsoftware.forza4;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import it.dais.forza4.R;
import it.unive.dais.legodroid.lib.EV3;
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

    int coordinateRobot;

    TextView timerValue = null;
    Thread threadTimer = null;
    int minutes = 0, seconds = 0;

    MediaPlayer mediaPlayer = null;

    private RobotControl r;
    private EV3 ev3;
    private char win;
    private boolean gameOver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        mediaPlayer = MediaPlayer.create(this, R.raw.coin_sound);

        // Associazione robot con Bluetooth
        new ConnectTask().execute(this);
    }
    public class ConnectTask extends AsyncTask<RobotControl.OnTasksFinished,Void,Void>{
        @Override
        protected Void doInBackground(RobotControl.OnTasksFinished... a) {
            r = RobotControl.connectToEv3(a[0]);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(r!=null){
                boolean resumed = getIntent().getBooleanExtra("resume", false);
                startGame(resumed);
            }else{
                Toast.makeText(NewGameActivity.this, "Errore Bluetooth", Toast.LENGTH_LONG).show();
                textTurno.setText("Errore Bluetooth");
            }
        }
    }
    private void startGame(boolean res){
        // Inizio logica di gioco
        gameLogic = new GameLogic(gameGrid, lastGame);
        gameLogic.initializeGame();

        userCoinCount.setText("" + gameLogic.getUserCoin());
        robotCoinCount.setText("" + gameLogic.getRobotCoin());
        if(res){
            char w=gameLogic.winner();
            if(w!='H'){
                this.checkWin(w);
                this.r.interrupt();
                return;
            }
        }
        startTimer();
        if (!res)
            r.calibrate(true);
        else {
            r.setCurrentPos(5,0);
            r.calibrate(true);
        }
        // MOSSA UTENTE
        textTurno.setText(R.string.textTurnoGiocatore);
    }

    private void checkWin(char win){

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

            Boolean vibr = settings.getBoolean("VIBRATION", true);
            if (vibr == true) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {100, 100, 100};
                v.vibrate(pattern, -1); // -1 indica di vibrare una sola volta
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

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (r != null) {
            r.interrupt();
        }
        if(gameLogic!=null){
            Log.i("SAVE","saving");
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("LASTGAME", gameLogic.getLastGame());
            Log.i("SAVE",gameLogic.getLastGame());
            editor.commit();
        }

        minutes = 0;
        seconds = 0;
        if(threadTimer!=null)
            threadTimer.interrupt();
    }

    @Override
    protected void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);
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
    public void calibrated() { }

    @Override
    public void columnRead(int c) {
        runOnUiThread(()->{
            textTurno.setText(R.string.textTurnoRobot);
        });
        Log.i("CAL","Activity: column"+c+" quota: "+gameLogic.quote[c]);
        while(gameLogic.quote[c]==ROWS){
            c++;
        }
        if(c<=COLS-1){
            r.getCoinAt(gameLogic.quote[c], c);
        }
    }

    @Override
    public void colorRead(LightSensor.Color color,int r, int c) {
        if (color == LightSensor.Color.RED){
            // Mossa UTENTE
            runOnUiThread(()-> {
                gameLogic.setCoin(c, 'R');
                decreaseUserCoin();
            });
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            win = gameLogic.winner();
            Log.i("CAL","1 winner "+win);
            if (win != 'H'){
                runOnUiThread(()-> {
                    this.checkWin(win);
                });
                this.r.gameOver(-1, false);
                Log.i("CAL","return "+win);
                return;
            }
            else {
                Log.i("CAL","Mossa Robot");
                // Mossa ROBOT
                coordinateRobot = gameLogic.calculateRobotAction(diff);

                runOnUiThread(() -> {
                    gameLogic.setCoin(coordinateRobot, 'Y');
                    decreaseRobotCoin();
                });
                gameLogic.incrementTurno();

                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                win = gameLogic.winner();
                if (win != 'H'){
                    runOnUiThread(()-> {
                        this.checkWin(win);
                    });
                    gameOver=true;
                    this.r.gameOver(coordinateRobot,true);
                    return;
                }
                else {
                    Log.i("CAL","Drop Token");
                    this.r.dropToken(coordinateRobot);
                    mediaPlayer.start();
                }
            }
        }
        else if (c < COLS-1){
            this.r.getCoinAt(gameLogic.quote[c+1],c+1);
        }
    }
}
