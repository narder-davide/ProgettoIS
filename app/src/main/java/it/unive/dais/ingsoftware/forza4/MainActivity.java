package it.unive.dais.ingsoftware.forza4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import it.dais.forza4.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;
import it.unive.dais.legodroid.lib.util.Consumer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        BluetoothConnection conn = new BluetoothConnection("EV3");
        Channel channel = null;
        try {
            channel = conn.connect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        EV3 ev3 = new EV3(new SpooledAsyncChannel(channel));

        ev3.run(data -> {
            try {
                TouchSensor touchSensor = data.getTouchSensor(EV3.InputPort._1);
                boolean running = true;
                while (running) {
                    // Set motor speed to 10 (port A)
                    data.getTachoMotor(EV3.OutputPort.A).setSpeed(10);

                    // Stop job and motor if pressed
                    if (touchSensor.getPressed().get()) {
                        data.getTachoMotor(EV3.OutputPort.A).setSpeed(0);
                        running = false;
                    }
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}
