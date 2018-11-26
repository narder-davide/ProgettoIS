package it.unive.dais.ingsoftware.forza4;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;

public class RobotControl {
    private UltrasonicSensor ultrasonicSensor;
    private TachoMotor tokenMotor;

    interface OnCalibrationFinished{
        void calibrated();
    }
    private float xstart,ystart;
    private EV3 ev3;
    private TachoMotor motor,sensorMotor;
    private int currentRow,currentCol;
    private boolean calibrated=false;
    private LightSensor lightSensor;
    private OnCalibrationFinished callback;
    public RobotControl(EV3 e,OnCalibrationFinished c){
        ev3=e;
        callback=c;
    }

    public void calibrate() {
        Consumer<EV3.Api> calibrate = (data -> {
            try {
                ultrasonicSensor = data.getUltrasonicSensor(EV3.InputPort._2);
                lightSensor = data.getLightSensor(EV3.InputPort._1);
                boolean end = false, last = false;
                motor = data.getTachoMotor(EV3.OutputPort.A);
                sensorMotor = data.getTachoMotor(EV3.OutputPort.B);
                tokenMotor = data.getTachoMotor(EV3.OutputPort.C);

                Future<LightSensor.Color> c;

                motor.setPolarity(TachoMotor.Polarity.FORWARD);
                motor.setType(TachoMotor.Type.LARGE);
                sensorMotor.setType(TachoMotor.Type.LARGE);

                end = false;
                motor.setPower(40);
                motor.start();
                while (!end) {
                    c = lightSensor.getColor();
                    if (c.get() == LightSensor.Color.TRANSPARENT) {
                        Log.i("CAL", "2-Not_Blue");
                    } else {
                        motor.stop();
                        xstart = motor.getPosition().get();
                        end = true;
                        Log.i("CAL", "2-FoundBlue X:" + xstart);
                    }
                    Thread.sleep(20);
                }

                end = false;
                sensorMotor.setPower(70);
                sensorMotor.start();
                while (!end) {
                    c = lightSensor.getColor();
                    LightSensor.Color color = c.get();
                    if (color != LightSensor.Color.TRANSPARENT) {
                        Log.i("CAL", "3-NotTrans");
                    } else {
                        sensorMotor.brake();
                        end = true;
                    }
                    Thread.sleep(40);
                }

                sensorMotor.setStepPower(-50, 20, 207, 50, true);

                motor.setStepPower(40, 60, 105, 45, true);
                Thread.sleep(1000);

                motor.clearCount();
                sensorMotor.clearCount();

                xstart = motor.getPosition().get();
                ystart = sensorMotor.getPosition().get();
                currentCol=0;
                currentRow=5;

                Log.i("CAL", "0 Color: " + lightSensor.getColor().get());
                //X step 282 in orizzontale
                //Y step 82 in verticale

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            callback.calibrated();
        });
        try {
            ev3.run(calibrate);
        } catch (EV3.AlreadyRunningException e) {
            e.printStackTrace();
        }
    }


    public void move(int r, int c){
        if(c==-1){
            moveOut();
            return;
        }
        if(r>5 || r<0 || c>6 || c<0){
            return;
        }else{
            Consumer<EV3.Api> move_c=(data ->{
                try {
                    Log.i("CAL","move");
                    int newr = currentRow - r;
                    int newc = currentCol - c;

                    int stepm=Math.abs(282*newc);
                    int steps=Math.abs(331*newr);

                    Log.i("CAL","motor: "+stepm+"Power: "+(-1*Integer.signum(newc)*30));
                    Log.i("CAL","sensor: "+steps+"Power: "+(Integer.signum(newr)*30));

                    if(newc!=0)
                        motor.setStepPower(-1*Integer.signum(newc)*100, 50,stepm-100, 50, true);
                    if(newr!=0)
                        sensorMotor.setStepPower(-1*Integer.signum(newr)*50, 20,steps-40, 20, true);
                    Thread.sleep(5000);
                    currentRow=r;
                    currentCol=c;
                    Log.i("CAL","currR "+currentRow+"  currC "+currentCol);

                    LightSensor.Rgb rgb=lightSensor.getRgb().get();
                    LightSensor.Color col= lightSensor.getColor().get();
                    Log.i("CAL","Color "+col);
                    if(col==LightSensor.Color.YELLOW || col==LightSensor.Color.BROWN){
                        Log.i("CAL","Color Giallo/Marrone");
                    }else{
                        if(rgb.B-37<5 && rgb.G-68<5 && rgb.R-255<5 && col==LightSensor.Color.BLUE) {
                            Log.i("CAL","Color ROSSO rgb+col");
                        }else{
                            Log.i("CAL","Color err");
                        }
                    }
                } catch (InterruptedException | IOException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            try {
                ev3.run(move_c);
                Log.i("CAL","ev3_running");
            } catch (EV3.AlreadyRunningException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveOut() {
        Consumer<EV3.Api> move_out=(data ->{
            try {
                Log.i("CAL","Muovi Fuori");
                Thread.sleep(100);
                int stepm=Math.abs(282*currentCol);
                motor.setStepPower(-100, 50,stepm-100+2300, 50, true);
                currentCol=-1;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        try {
            ev3.run(move_out);
            Log.i("CAL","ev3_running");
        } catch (EV3.AlreadyRunningException e) {
            e.printStackTrace();
        }
    }
    
    public float getDistance() {
        Float dist = null;
        try {
            dist = ultrasonicSensor.getDistance().get();
        } catch (ExecutionException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("CAL", "dist: " + dist);
        return dist;
    }

    public void dropToken(){
        Consumer<EV3.Api> drop=(data ->{
            try {
                Log.i("CAL","Drop token");

                //prima il motore drop -> wait -> dopo motor in dir <-
                int stepm=Math.abs(282*1);
                //
                tokenMotor.setStepPower(-5, 20,90, 1, true);
                motor.setStepPower(50, 50,stepm-100, 50, true);
                Thread.sleep(2000);
                tokenMotor.setStepPower(5, 20,90, 1, true);
                Thread.sleep(2000);
                motor.setStepPower(-50, 50,stepm-100, 50, true);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        try {
            ev3.run(drop);
        } catch (EV3.AlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}
