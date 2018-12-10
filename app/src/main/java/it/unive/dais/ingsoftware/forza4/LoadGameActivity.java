package it.unive.dais.ingsoftware.forza4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import it.dais.forza4.R;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class LoadGameActivity extends AppCompatActivity implements RobotControl.OnTasksFinished{

    private static final int ROWS = 6,COLS=7;
    private RobotControl r;
    private GameLogic g;
    private char mat[][]=new char[ROWS][COLS];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_load_game);

        Button buttonLoad = findViewById(R.id.buttonLoad);
        Button buttonScan = findViewById(R.id.buttonScan);

        // Caricamento leggendo stringa salvata nelle SharedPreferences
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNewGameActivity = new Intent(LoadGameActivity.this, NewGameActivity.class);
                startActivity(openNewGameActivity);
            }
        });


        // Scansione della plancia di gioco e creazione della stringa corrispondente alla partita
        r=RobotControl.connectToEv3(this);

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // lettura delle cella della plancia e str += VALORE_LETTO_DA_SENSORE_ROBOT
                r.getCoinAt(0,0);

            }
        });
    }

    @Override
    public void calibrated() {

    }

    @Override
    public void columnRead(int c) {

    }

    @Override
    public void colorRead(LightSensor.Color color,int r, int c) {
        if(color==LightSensor.Color.TRANSPARENT){
            mat[r][c]='X';
            for(int i=r;i<ROWS;i++)
                mat[i][c]='X';
            if(c<5){
                this.r.getCoinAt(0,c+1);
            }else{
                startLoadedGame();
            }
        }else if (color==LightSensor.Color.RED) {
            mat[r][c]='R';
        }else if(color==LightSensor.Color.YELLOW){
            mat[r][c]='Y';
        }
        if(r<5){
            this.r.getCoinAt(r+1,c);
        }else if (c<5){
            this.r.getCoinAt(0,c+1);
        }else{
            startLoadedGame();
        }


    }
    private void startLoadedGame(){
        String s="";

        for(int i=0;i<ROWS;i++){
            for(int j=0;j<COLS;j++){
                s+=mat[i][j];
            }
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("LASTGAME", s);
        editor.commit();

        Intent openNewGameActivity = new Intent(LoadGameActivity.this, NewGameActivity.class);
        startActivity(openNewGameActivity);
    }
}
