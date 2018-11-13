package it.unive.dais.legodroid.lib.sensors;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;


public class UltrasonicSensor extends AbstractSensor {
    public UltrasonicSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_ULTRASONIC);
    }

    @NonNull
    public Future<Float> getDistance() throws IOException {
        return getSi1(Const.US_CM);
    }
}
