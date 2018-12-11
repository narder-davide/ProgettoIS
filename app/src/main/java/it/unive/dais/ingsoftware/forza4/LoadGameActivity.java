package it.unive.dais.ingsoftware.forza4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import it.dais.forza4.R;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class LoadGameActivity extends AppCompatActivity implements RobotControl.OnTasksFinished{

    private static final int ROWS = 6,COLS=7;
    private RobotControl r;
    private GameLogic g;
    private char mat[][]=new char[ROWS][COLS];
    private boolean starting;
    private Button buttonLoad,buttonScan;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_load_game);

        buttonLoad = findViewById(R.id.buttonLoad);
        buttonScan = findViewById(R.id.buttonScan);
        enableDisableButtons(false);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Caricamento leggendo stringa salvata nelle SharedPreferences
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNewGameActivity = new Intent(LoadGameActivity.this, NewGameActivity.class);
                startActivity(openNewGameActivity);
            }
        });

        progressBar=findViewById(R.id.progressBar);
        progressBar.setEnabled(false);
        progressBar.setVisibility(View.INVISIBLE);

        // Scansione della plancia di gioco e creazione della stringa corrispondente alla partita
        new ConnectTask().execute(this);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // lettura delle cella della plancia e str += VALORE_LETTO_DA_SENSORE_ROBOT
                enableDisableButtons(false);
                progressBar.setEnabled(true);
                progressBar.setVisibility(View.VISIBLE);
                r.calibrate(false);

            }
        });
    }

    @Override
    public void calibrated() {
        r.getCoinAt(0,0);
    }

    @Override
    public void columnRead(int c) {

    }

    @Override
    public void colorRead(LightSensor.Color color,int r, int c) {
        if(starting){
            startLoadedGame();
            return;//prova calibrazione dopo load game
        }
        if (color==LightSensor.Color.RED) {
            mat[r][c]='R';
        }else if(color==LightSensor.Color.YELLOW){
            mat[r][c]='Y';
        }else{
            mat[r][c]='X';
            for(int i=r;i<ROWS;i++)
                mat[i][c]='X';
            if(c<=5){
                this.r.getCoinAt(0,c+1);
            }else{
                starting=true;
                this.r.getCoinAt(5,0);
            }
        }
        if(color==LightSensor.Color.RED || color==LightSensor.Color.YELLOW){
            if(r<5){
                this.r.getCoinAt(r+1,c);
            }else if (c<=5){
                this.r.getCoinAt(0,c+1);
            }else{
                starting=true;
                this.r.getCoinAt(5,0);
            }
        }
    }
    private void startLoadedGame(){
        String s="";
        for(int i=0;i<ROWS;i++){
            for(int j=0;j<COLS;j++){
                s+=mat[i][j];
            }
        }
        Log.i("CAL",s);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("LASTGAME", s);
        editor.commit();

        Intent openNewGameActivity = new Intent(LoadGameActivity.this, NewGameActivity.class);
        openNewGameActivity.putExtra("resume",true);
        startActivity(openNewGameActivity);
    }

    public class ConnectTask extends AsyncTask<RobotControl.OnTasksFinished,Void,RobotControl> {
        @Override
        protected RobotControl doInBackground(RobotControl.OnTasksFinished... a) {
            return RobotControl.connectToEv3(a[0]);
        }
        @Override
        protected void onPostExecute(RobotControl rc) {
            super.onPostExecute(rc);
            if(rc!=null){
                r=rc;
                enableDisableButtons(true);
            }else{
                enableDisableButtons(false);
                Toast.makeText(LoadGameActivity.this, "Errore Bluetooth", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void enableDisableButtons(boolean b) {
        buttonScan.setEnabled(b);
        buttonLoad.setEnabled(b);
    }
}
