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

    private static final int OUT_DISTANCE = 2530;
    private static final int MOTOR_STEP = 290;//282
    private static final int SENSOR_STEP = 340;

    private static EV3 ev3;
    private static String BRICK_NAME="F4Bot";
    private UltrasonicSensor ultrasonicSensor;
    private TachoMotor tokenMotor;
    private boolean outOfBoard = false;
    private boolean endGame;
    private boolean robotWin;

    public void dropToken(int c) {
        move(currentRow,c,true);
    }

    public void getCoinAt(int r, int c) {
        move(r,c,false);
    }

    public void setCurrentPos(int r, int c) {
        currentRow=r;
        currentCol=c;
    }

    public void gameOver(int coordinateRobot, boolean robotWin) {
        endGame=true;
        this.robotWin=robotWin;
        if (robotWin){
            move(currentRow, coordinateRobot, true);
        }else{
            interrupt();
        }
    }

    public void interrupt() {
        Consumer<EV3.Api> interrupt=(data -> {
            moveOut(data);
        });
        try {
            ev3.cancel();
            Thread.sleep(1000);
            ev3.run(interrupt);
        } catch (InterruptedException | EV3.AlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    interface OnTasksFinished{
        void calibrated();
        void columnRead(int c);
        void colorRead(LightSensor.Color color,int r,int c);
    }

    private TachoMotor motor, sensorMotor;
    private int currentRow, currentCol;
    private LightSensor lightSensor;
    private OnTasksFinished callback;

    private RobotControl(EV3 e, OnTasksFinished c){
        ev3 = e;
        callback = c;
        currentRow=-1;
        currentCol=-1;
    }

    public static RobotControl connectToEv3(OnTasksFinished act){
        try {
            if(ev3==null){
                BluetoothConnection conn = new BluetoothConnection(BRICK_NAME);
                Channel channel = null;
                channel = conn.connect();
                ev3 = new EV3(new SpooledAsyncChannel(channel));
            }
            RobotControl r = new RobotControl(ev3,act);
            return r;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void calibrate(boolean out) {
        Consumer<EV3.Api> calibrate = (data -> {
            try {
                ultrasonicSensor = data.getUltrasonicSensor(EV3.InputPort._2);
                lightSensor = data.getLightSensor(EV3.InputPort._1);
                boolean end;
                motor = data.getTachoMotor(EV3.OutputPort.A);
                sensorMotor = data.getTachoMotor(EV3.OutputPort.D);

                Future<LightSensor.Color> c;
                LightSensor.Color color;
                motor.setPolarity(TachoMotor.Polarity.FORWARD);
                motor.setType(TachoMotor.Type.LARGE);
                sensorMotor.setType(TachoMotor.Type.LARGE);
                if(currentCol!=0 && currentRow!=5) {
                    end = false;

                    Thread.sleep(100);
                    motor.resetPosition();
                    motor.setPower(10);
                    motor.start();
                    while (!end){
                        c = lightSensor.getColor();
                        color=c.get();
                        if (color!=LightSensor.Color.TRANSPARENT) {
                            end=true;
                        }else{
                            Thread.sleep(50);
                        }
                        Log.i("CAL", "motor: "+color);
                    }

                    Thread.sleep(10000);
                    Thread.sleep(10000);
                    Thread.sleep(10000);

                    end = false;
                    /*
                    while (!end) {
                        c=lightSensor.getColor();
                        color=c.get();
                        if (color==LightSensor.Color.TRANSPARENT) {
                            end = true;
                        }else{
                            sensorMotor.setStepPower(30,20,20,20,true);
                            Thread.sleep(700);
                        }
                        Log.i("CAL", "sensor: "+color);
                    }
                    Thread.sleep(1000);
                    sensorMotor.setStepPower(-40, 20, 190, 50, true);
                    motor.setStepPower(40, 60, 90, 40, true);
                    sleepTime(210);
                    //Thread.sleep(1000);
                    /*
                    motor.setPower(20);
                    motor.start();
                    while (!end){
                        c = lightSensor.getColor();
                        color=c.get();
                        if (color!=LightSensor.Color.TRANSPARENT) {
                            motor.stop();
                            end=true;
                        }
                        Log.i("CAL", "motor: "+color);
                    }

                    Thread.sleep(2000);
                    end = false;
                    sensorMotor.setPower(30);
                    sensorMotor.start();
                    while (!end) {
                        c=lightSensor.getColor();
                        color=c.get();
                        if (color==LightSensor.Color.TRANSPARENT) {
                            sensorMotor.brake();
                            end = true;
                        }
                        Log.i("CAL", "sensor: "+color);
                        Thread.sleep(40);
                    }
                    Thread.sleep(2000);
                    sensorMotor.setStepPower(-40, 20, 190, 50, true);
                    motor.setStepPower(40, 60, 90, 40, true);
                    sleepTime(210);
                    //Thread.sleep(1000);

                    */
                    currentCol=0;
                    currentRow=5;
                }

                //2500 giri in 2.7 sec 3.3 2.9
                if(out){
                    motor.setStepPower(-100, 50,RobotControl.OUT_DISTANCE-100, 50, true);
                    sleepTime(OUT_DISTANCE);
                    outOfBoard=true;
                    data.soundTone(40,440,600);
                    Thread.sleep(800);
                    getDistance(data);
                }else{
                    calibrationFinished();
                }
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
            Thread.sleep(1800+(42/25)*giri);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void move(int r, int c, boolean dropping){
        Log.i("CAL","Moving: r: "+r+" c: "+c+"drop: "+dropping);
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
                        motor.setStepPower(100, 50,RobotControl.OUT_DISTANCE-700, 50, true);
                        sleepTime(OUT_DISTANCE);
                        currentCol=0;
                        boolean end = false;
                        motor.setPower(30);
                        motor.start();
                        Future<LightSensor.Color> color;

                        while (!end) {
                            color = lightSensor.getColor();
                            if (color.get() == LightSensor.Color.TRANSPARENT) {
                                Log.i("CAL", "2-Not_Blue");
                            } else {
                                motor.brake();
                                end = true;
                            }
                            Thread.sleep(40);
                        }

                        Thread.sleep(400);
                        motor.setStepPower(30,20,120,40,true);
                        sleepTime(50);
                        newc=currentCol-c;
                        outOfBoard=false;
                    }

                    if(dropping){
                        newc=newc+3;
                    }
                    newr = currentRow - r;

                    //X step 282 in orizzontale
                    //Y step 331 in verticale

                    int stepm=Math.abs(MOTOR_STEP*newc);
                    int steps=Math.abs(SENSOR_STEP*newr);

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
                        stepm=150;
                        tokenMotor=data.getTachoMotor(EV3.OutputPort.C);
                        tokenMotor.setType(TachoMotor.Type.MEDIUM);
                        Thread.sleep(500);
                        tokenMotor.setStepPower(-15, 20,90, 5, true);
                        Thread.sleep(1300);
                        tokenMotor.setStepPower(15, 20,90, 5, true);
                        Thread.sleep(1000);
                        motor.setStepPower(50, 50,stepm-100, 50, true);
                        sleepTime(stepm);
                        motor.setStepPower(-50, 50,stepm-100, 50, true);
                        moveOut(data);
                        if(!endGame){
                            data.soundTone(40,440,600);
                            getDistance(data);
                        }else{
                            //vince robot
                        }
                    }
                    else {
                        Thread.sleep(130);
                        LightSensor.Color col= lightSensor.getColor().get();
                        Log.i("CAL","Color "+col);
                        if(col==LightSensor.Color.YELLOW || col==LightSensor.Color.BROWN){
                            //Log.i("CAL","Color Giallo/Marrone");
                            colorRead(LightSensor.Color.YELLOW,currentRow,currentCol);
                        }
                        else {
                            if(col==LightSensor.Color.BLUE) {
                                //Log.i("CAL","Color ROSSO rgb+col");
                                colorRead(LightSensor.Color.RED,currentRow,currentCol);
                            }
                            else {
                                Log.i("CAL","Color ERR"+col);
                                colorRead(col,currentRow,currentCol);
                            }
                        }
                    }
                } catch (InterruptedException | IOException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

            try {
                ev3.cancel();
                ev3.run(move_c);
            }catch (EV3.AlreadyRunningException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveOut(EV3.Api data){
        //esci
        try {
            Log.i("CAL","Muovi Fuori");
            int stepm = (MOTOR_STEP * currentCol);
            motor=data.getTachoMotor(EV3.OutputPort.A);
            //tempo di uscita dalla griglia
            if(!outOfBoard)
                motor.setStepPower(-100, 50,stepm-100+RobotControl.OUT_DISTANCE, 50, true);
            Thread.sleep(stepm+OUT_DISTANCE);
            outOfBoard=true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void getDistance(EV3.Api data) {
        Float dist,last = (float) -1;
        int col=-1,t=0,stuck=0;
        boolean end=false;
        try {
            while(!end){
                Thread.sleep(250);
                dist = ultrasonicSensor.getDistance().get();
                //dist=dist/10;
                if(Float.compare(dist,last)==0){
                    stuck++;
                    if(stuck>5){
                        data.soundTone(40,440,300);
                        data.soundTone(30,200,100);
                        stuck=0;
                    }
                }
                last=dist;

                Log.i("CAL", "dist: " + dist);
                if(dist<23){
                    col=(dist>9.8 ? 2 : 0);
                }else if(col!=-1){
                    while(dist>30f && t<7){
                        dist = ultrasonicSensor.getDistance().get();
                       // dist=dist/10;
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
            Log.i("CAL","readColumn: "+col);
            columnRead(col);
        }
        catch (ExecutionException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calibrationFinished() {
        AsyncTask<Object, Void, Void> a = new MyTasks(TaskType.CALIBRATED);
        a.execute();
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

    @SuppressLint("StaticFieldLeak")
    private class MyTasks extends AsyncTask<Object, Void, Void> {
        private TaskType t;

        public MyTasks(TaskType taskType) {
            t=taskType;
        }

        @Override
        protected Void doInBackground(Object... ob) {
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            switch (t){
                case DISTANCE:
                    Log.i("CAL","Task Distance: "+(int)ob[0]);
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

/*
  -CALIBRAZIONE
  -eccezzioni varie

  - rotazione
  -
  -tolto rgb su move and read
 */
