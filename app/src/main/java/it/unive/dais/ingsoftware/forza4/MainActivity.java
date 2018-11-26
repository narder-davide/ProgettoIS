package it.unive.dais.ingsoftware.forza4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    private Float xstart,ystart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView app = findViewById(R.id.textView);

        // Gestione delle impostazioni di gioco
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        Boolean vibr = settings.getBoolean("VIBRATION", true);
        String diff = settings.getString("DIFFICULT", "noValueInsert");
        editor.putBoolean("VIBRATION", vibr);
        editor.putString("DIFFICULT", diff);
        editor.commit();

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

        // Apertura connessione Bluetooth
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
                    TachoMotor sensorMotor=data.getTachoMotor(EV3.OutputPort.B);
                    Future<LightSensor.Color> c;

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
                        c=lightSensor.getColor();
                        if(c.get()!=LightSensor.Color.BLUE){
                            motor.setStepPower(30,30,1,30,true);
                        }else {
                            xstart=motor.getPosition().get();
                            end=true;
                        }
                        Thread.sleep(1000);
                    }
                    end=false;
                    while(!end){
                        c=lightSensor.getColor();
                        if(c.get()!=LightSensor.Color.BLUE){
                            sensorMotor.setStepPower(30,30,1,30,true);
                        }else {
                            ystart=sensorMotor.getPosition().get();
                            end=true;
                        }
                        Thread.sleep(1000);
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
