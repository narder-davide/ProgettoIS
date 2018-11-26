package it.unive.dais.ingsoftware.forza4;

import android.graphics.Color;
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
    public interface OnCalibrationFinished{
        public void calibrated();
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
                lightSensor = data.getLightSensor(EV3.InputPort._1);
                boolean end = false, last = false;
                motor = data.getTachoMotor(EV3.OutputPort.A);
                sensorMotor = data.getTachoMotor(EV3.OutputPort.B);
                Future<LightSensor.Color> c;

                motor.setPolarity(TachoMotor.Polarity.FORWARD);

                motor.setType(TachoMotor.Type.LARGE);
                sensorMotor.setType(TachoMotor.Type.LARGE);

                end = false;
                motor.setPower(30);
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
                    Thread.sleep(50);
                }

                end = false;
                sensorMotor.setSpeed(-5);
                sensorMotor.start();
                while (!end) {
                    c = lightSensor.getColor();
                    LightSensor.Color color = c.get();
                    Log.i("CAL", "3- " + color);
                    if (color != LightSensor.Color.TRANSPARENT) {
                        Log.i("CAL", "3-NotTrans");
                    } else {
                        sensorMotor.stop();
                        ystart = sensorMotor.getPosition().get();
                        Log.i("CAL", "3-Trans  Y: " + ystart);
                        end = true;
                    }
                    Thread.sleep(50);
                }
                sensorMotor.stop();

                sensorMotor.setStepPower(30, 10, 28, 10, false);
                motor.setStepPower(40, 100, 100, 1, true);
                Thread.sleep(1000);

                motor.clearCount();
                sensorMotor.clearCount();

                xstart = motor.getPosition().get();
                ystart = sensorMotor.getPosition().get();
                currentCol=0;
                currentRow=5;

                Log.i("CAL", "0 Color: " + lightSensor.getColor().get());
                int i = 1;
                LightSensor.Color color;

                //X step 282 in orizzontale
                //Y step 82 in verticale
                /*
                int j=0;
                int pow=100;
                while(j<6){
                    while(i<7) {
                        motor.setStepPower(pow, 100, 180, 1, true);
                        Thread.sleep(2000);
                        color= lightSensor.getColor().get();

                        xstart=motor.getPosition().get();
                        ystart=sensorMotor.getPosition().get();

                        Log.i("CAL", "X: " +xstart+" Y: "+ystart);
                        Log.i("CAL", i + " Color: " + color);
                        i++;
                    }
                    pow=pow*-1;
                    sensorMotor.setStepPower(100,20,20,0,false);
                    j++;
                    i=0;
                }
                */
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
                    Log.i("CAL","inizio");
                    int newr = currentRow - r;
                    int newc=currentCol-c;
                    Thread.sleep(100);

                    int stepm=Math.abs(282*newc);
                    int steps=Math.abs(82*newr)-(5*newr);

                    Log.i("CAL","motor: "+stepm+"Power: "+(-1*Integer.signum(newc)*30));
                    Log.i("CAL","sensor: "+steps+"Power: "+(Integer.signum(newr)*30));

                    if(newc!=0)
                        motor.setStepPower(-1*Integer.signum(newc)*100, 50,stepm-100, 50, true);
                    if(newr!=0)
                        sensorMotor.setStepPower(Integer.signum(newr)*30, 30,steps-45, 15, true);
                    Thread.sleep(5000);

                    currentRow=newr;
                    currentCol=newc;

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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                } catch (ExecutionException e) {
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e){
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

    public LightSensor.Color readColor(){
        return LightSensor.Color.BLACK;
    }

    public float getDistance() {
        Consumer<EV3.Api> ultra = (data -> {
            UltrasonicSensor us = data.getUltrasonicSensor(EV3.InputPort._1);
            try {
                while (true) {
                    Future<Float> d = us.getDistance();
                    Log.i("CAL", "dist: " + d.get());
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
        return 0;
    }
}
