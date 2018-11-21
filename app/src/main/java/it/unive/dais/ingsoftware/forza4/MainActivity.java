package it.unive.dais.ingsoftware.forza4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.dais.forza4.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recupero bottone per NUOVA PARTITA
        Button newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNewGameActivity = new Intent(MainActivity.this, NewGameActivity.class);
                startActivity(openNewGameActivity);
            }
        });

        // Recupero bottone per CARICA PARTITA
        Button loadGameButton = findViewById(R.id.loadGameButton);
        loadGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openLoadGameActivity = new Intent(MainActivity.this, LoadGameActivity.class);
                startActivity(openLoadGameActivity);
            }
        });

        // Recupero bottone per STATISTICHE
        Button statisticsButton = findViewById(R.id.statisticsButton);
        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openStatisticsActivity = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(openStatisticsActivity);
            }
        });

        // Recupero bottone per OPZIONI
        Button optionsButton = findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openOptionsActivity = new Intent(MainActivity.this, OptionsActivity.class);
                startActivity(openOptionsActivity);
            }
        });
        
        BluetoothConnection conn = new BluetoothConnection("F4Bot");
        Channel channel = null;
        try {
            channel = conn.connect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        EV3 ev3 = new EV3(new SpooledAsyncChannel(channel));
        try {
            ev3.run(data -> {
                try {
                    LightSensor lightSensor = data.getLightSensor(EV3.InputPort._1);
                    boolean end=false,last=false;
                    TachoMotor motor=data.getTachoMotor(EV3.OutputPort.A);
                    Future<LightSensor.Color> c=lightSensor.getColor();

                    while(!end){
                        c=lightSensor.getColor();
                        if(c.get()==LightSensor.Color.BLUE){
                            data.soundTone(50,1000,1000);
                            motor.setStepPower(30,200,1,200,true);
                            last=false;
                        }else if(!last){
                            motor.setStepPower(30,200,1,200,true);
                            data.soundTone(50,500,200);
                            last=true;
                        }else {
                            end = true;
                        }
                        Thread.sleep(1500);
                    }
                    end=false;
                    while(!end){
                        
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } catch (EV3.AlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}
