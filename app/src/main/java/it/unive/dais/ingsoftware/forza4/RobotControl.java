package it.unive.dais.ingsoftware.forza4;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;

@SuppressWarnings("WeakerAccess")
public class RobotControl {

    private static final int OUT_DISTANCE = 2500;
    private UltrasonicSensor ultrasonicSensor;
    private TachoMotor tokenMotor;
    private boolean outOfBoard = false;

    public void dropToken(int c) {
        move(currentRow,c,true);
    }

    public void getCoinAt(int r, int c) {
        move(r,c,false);
    }

    interface OnTasksFinished{
        void calibrated();
        void columnRead(int c);
        void colorRead(LightSensor.Color color,int r,int c);
    }

    private EV3 ev3;
    private TachoMotor motor, sensorMotor;
    private int currentRow, currentCol;
    private LightSensor lightSensor;
    private OnTasksFinished callback;

    private RobotControl(EV3 e, OnTasksFinished c){
        ev3 = e;
        callback = c;
    }

    public static RobotControl connectToEv3(OnTasksFinished act){
        try {
            BluetoothConnection conn = new BluetoothConnection("F4Bot");
            Channel channel = null;
            EV3 ev3;
            channel = conn.connect();
            ev3 = new EV3(new SpooledAsyncChannel(channel));
            RobotControl r = new RobotControl(ev3,act);
            return r;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void calibrate() {
        Consumer<EV3.Api> calibrate = (data -> {
            try {
                ultrasonicSensor = data.getUltrasonicSensor(EV3.InputPort._2);

                lightSensor = data.getLightSensor(EV3.InputPort._1);
                boolean end;
                motor = data.getTachoMotor(EV3.OutputPort.A);
                sensorMotor = data.getTachoMotor(EV3.OutputPort.D);

                Future<LightSensor.Color> c;

                motor.setPolarity(TachoMotor.Polarity.FORWARD);
                motor.setType(TachoMotor.Type.LARGE);
                sensorMotor.setType(TachoMotor.Type.LARGE);

                end = false;
                motor.setPower(35);
                motor.start();
                while (!end) {
                    c = lightSensor.getColor();
                    if (c.get() == LightSensor.Color.TRANSPARENT) {
                        Log.i("CAL", "2-Not_Blue");
                    }
                    else {
                        motor.stop();
                        end = true;
                        Log.i("CAL", "2-FoundBlue X:");
                    }
                    Thread.sleep(40);
                }

                end = false;
                sensorMotor.setPower(30);
                sensorMotor.start();
                while (!end) {
                    c = lightSensor.getColor();
                    LightSensor.Color color = c.get();

                    if (color != LightSensor.Color.TRANSPARENT) {
                        Log.i("CAL", "3-NotTrans");
                    }
                    else {
                        sensorMotor.brake();
                        end = true;
                    }
                    Thread.sleep(40);
                }

                sensorMotor.setStepPower(-50, 20, 207, 50, true);

                motor.setStepPower(40, 60, 105, 45, true);
                sleepTime(210);
                //Thread.sleep(1000);

                currentCol=0;
                currentRow=5;
                //2500 giri in 2.7 sec 3.3 2.9

                motor.setStepPower(-100, 50,RobotControl.OUT_DISTANCE-100, 50, true);
                sleepTime(OUT_DISTANCE);
                outOfBoard=true;
                data.soundTone(40,440,600);
                Thread.sleep(800);
                getDistance();
            }
            catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        try {
            ev3.run(calibrate);
        }
        catch (EV3.AlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void sleepTime(int giri){
        try {
            float g=(42/25)*giri;
            Thread.sleep(1500+(int)g);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void move(int r, int c, boolean dropping){
        if ((r>5 || r<0 || c>6 || c<0) && !dropping){
            return;
        }
        else{
            Consumer<EV3.Api> move_c=(data ->{
                try {
                    Log.i("CAL","move");
                    motor = data.getTachoMotor(EV3.OutputPort.A);
                    sensorMotor = data.getTachoMotor(EV3.OutputPort.D);
                    motor.setType(TachoMotor.Type.LARGE);
                    sensorMotor.setType(TachoMotor.Type.LARGE);
                    int newr;
                    int newc;

                    if(!outOfBoard){
                        newc = currentCol - c;
                    }
                    else{
                        motor.setStepPower(100, 50,RobotControl.OUT_DISTANCE-100, 50, true);
                        sleepTime(OUT_DISTANCE);
                        //Thread.sleep(4000);
                        currentCol=0;
                        newc=currentCol-c;
                        outOfBoard=false;
                    }

                    if(dropping){
                        //move(currentRow,c+3,true);
                        newc=newc+3;
                    }
                    newr = currentRow - r;

                    //X step 282 in orizzontale
                    //Y step 331 in verticale

                    int stepm=Math.abs(282*newc);
                    int steps=Math.abs(331*newr);

                    Log.i("CAL","motor: "+stepm+"Power: "+(-1*Integer.signum(newc)*30));
                    Log.i("CAL","sensor: "+steps+"Power: "+(Integer.signum(newr)*30));

                    if(newc!=0)
                        motor.setStepPower(-1*Integer.signum(newc)*100, 50,stepm-100, 50, true);
                    if(newr!=0)
                        sensorMotor.setStepPower(-1*Integer.signum(newr)*50, 20,steps-40, 20, true);
                    //questa attesa in base alla distanza
                    sleepTime(Math.max(stepm,steps)+500);
                    currentRow=r;
                    currentCol=(dropping ? c-3: c);
                    Log.i("CAL","currR "+currentRow+"  currC "+currentCol);

                    if(dropping){
                        Log.i("CAL","Drop token");
                        stepm=Math.abs(150);
                        tokenMotor=data.getTachoMotor(EV3.OutputPort.C);
                        tokenMotor.setType(TachoMotor.Type.MEDIUM);
                        Thread.sleep(500);
                        tokenMotor.setStepPower(-15, 20,90, 5, true);
                        //sleepTime(115);
                        Thread.sleep(1300);
                        tokenMotor.setStepPower(15, 20,90, 5, true);
                        //sleepTime(115);
                        Thread.sleep(1000);
                        motor.setStepPower(50, 50,stepm-100, 50, true);
                        sleepTime(stepm);
                        //Thread.sleep(1000);
                        motor.setStepPower(-50, 50,stepm-100, 50, true);


                        Log.i("CAL","Muovi Fuori");
                        stepm=(282*currentCol);
                        sleepTime(stepm);
                        //Thread.sleep(1000);

                        motor.setStepPower(-100, 50,stepm-100+RobotControl.OUT_DISTANCE, 50, true);
                        outOfBoard=true;
                        Thread.sleep(stepm+OUT_DISTANCE);

                        //tempo di uscita dalla griglia
                        data.soundTone(40,440,600);
                        getDistance();
                    }
                    else {
                        LightSensor.Rgb rgb=lightSensor.getRgb().get();
                        LightSensor.Color col= lightSensor.getColor().get();
                        Log.i("CAL","Color "+col);
                        if(col==LightSensor.Color.YELLOW || col==LightSensor.Color.BROWN){
                            //Log.i("CAL","Color Giallo/Marrone");
                            colorRead(LightSensor.Color.YELLOW,currentRow,currentCol);
                        }
                        else {
                            if(rgb.B-37<5 && rgb.G-68<5 && rgb.R-255<5 && col==LightSensor.Color.BLUE) {
                                //Log.i("CAL","Color ROSSO rgb+col");
                                colorRead(LightSensor.Color.RED,currentRow,currentCol);
                            }
                            else {
                                colorRead(col,currentRow,currentCol);
                            }
                        }
                    }
                } catch (InterruptedException | IOException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

            try {
                ev3.run(move_c);
                Log.i("CAL","ev3_running");
            }
            catch (EV3.AlreadyRunningException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void getDistance() {
        Float dist;
        int col=-1,t=0;
        boolean end=false;
        try {
            while(!end){
                //(dist<30f) || col==-1
                Thread.sleep(250);
                dist = ultrasonicSensor.getDistance().get();
                Log.i("CAL", "dist: " + dist);
                if(dist<21){
                    col=(dist>8 ? 2 : 0);//sistemare distsnza->colonne
                }else if(col!=-1){
                    while(dist>30f && t<7){
                        dist = ultrasonicSensor.getDistance().get();
                        Thread.sleep(300);
                        Log.i("CAL", "dist: " + dist);
                        t++;
                    }
                    if(t>=7){
                        end=true;
                    }
                    t=0;
                }
            }
            Thread.sleep(200);
            columnRead(col);
        }
        catch (ExecutionException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void colorRead(LightSensor.Color color,int r,int c) {
        AsyncTask<Object, Void, Void> a = new MyTasks(TaskType.COLOR);
        a.execute(color,r,c);
    }

    private void columnRead(int i) {
        AsyncTask<Object, Void, Void> a = new MyTasks(TaskType.DISTANCE);
        a.execute(i);
    }


    private enum TaskType {
        /**
         *
         */
        DISTANCE, COLOR, CALIBRATED
    }
/*
    private void bottoneRosso(){
        while(true){
            try {
                Thread.sleep(200);
                if(redButton.getPressed().get()==true){
                    sensorMotor.stop();
                    motor.stop();
                    ev3.cancel();
                    break;
                }
            }
            catch (ExecutionException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    @SuppressLint("StaticFieldLeak")
    private class MyTasks extends AsyncTask<Object, Void, Void> {
        private TaskType t;

        public MyTasks(TaskType taskType) {
            t=taskType;
        }

        @Override
        protected Void doInBackground(Object... ob) {
            try {
                Thread.sleep(600);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            switch (t){
                case DISTANCE:
                    callback.columnRead((int)ob[0]);
                    break;
                case COLOR:
                    callback.colorRead((LightSensor.Color)ob[0],(int)ob[1],(int)ob[2]);
                    break;
                case CALIBRATED:
                    callback.calibrated();
                    break;
            }
            return null;
        }
    }
}

/*-sistemare drop
  -sensore ultrasuoni distanze
 */