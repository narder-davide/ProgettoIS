package it.unive.dais.ingsoftware.forza4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;

public class MainActivity extends AppCompatActivity {

    private Float xstart,ystart;

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

        Consumer<EV3.Api> calibrate=(data -> {
            try {
                LightSensor lightSensor = data.getLightSensor(EV3.InputPort._1);
                boolean end=false,last=false;
                TachoMotor motor=data.getTachoMotor(EV3.OutputPort.A);
                TachoMotor sensorMotor=data.getTachoMotor(EV3.OutputPort.B);
                Future<LightSensor.Color> c;

                motor.setPolarity(TachoMotor.Polarity.FORWARD);

                motor.setType(TachoMotor.Type.LARGE);
                sensorMotor.setType(TachoMotor.Type.LARGE);

                end=false;
                motor.setSpeed(-5);
                motor.start();
                while(!end){
                    c=lightSensor.getColor();
                    if(c.get()==LightSensor.Color.TRANSPARENT){
                        Log.i("CAL","2-Not_Blue");
                    }else{
                        motor.stop();
                        xstart=motor.getPosition().get();
                        end=true;
                        Log.i("CAL","2-FoundBlue X:"+xstart);
                    }
                    Thread.sleep(50);
                }

                end=false;
                sensorMotor.setSpeed(-5);
                sensorMotor.start();
                while(!end){
                    c=lightSensor.getColor();
                    LightSensor.Color color=c.get();
                    Log.i("CAL","3- "+color);
                    if(color!=LightSensor.Color.TRANSPARENT){
                        Log.i("CAL","3-NotTrans");
                    }else{
                        sensorMotor.stop();
                        ystart=sensorMotor.getPosition().get();
                        Log.i("CAL","3-Trans  Y: "+ystart);
                        end=true;
                    }
                    Thread.sleep(50);
                }
                sensorMotor.stop();

                sensorMotor.setStepPower(22,0,35,0,false);

                for(int i=0;i<5;i++) {
                    motor.setStepPower(-20, 10, 10, 1, false);
                    Thread.sleep(2000);
                    LightSensor.Color color = lightSensor.getColor().get();
                    Log.i("CAL", i + " Color: " + color);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        Consumer<EV3.Api> ultra=(data ->  {
            UltrasonicSensor us=data.getUltrasonicSensor(EV3.InputPort._1);
            try {
                while(true){

                    Future<Float> d=us.getDistance();
                    Log.i("CAL","dist: "+d.get());
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
        try {
            ev3.run(calibrate);
        } catch (EV3.AlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}
