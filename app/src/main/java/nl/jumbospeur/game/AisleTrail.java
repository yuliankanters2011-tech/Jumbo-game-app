package nl.jumbospeur.game;

import android.content.Context;
import android.graphics.*;
import android.hardware.*;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

/** Local camera encounter. The arrow preserves a walking direction, not a store floor plan. */
final class AisleTrail extends View implements SensorEventListener {
    private final SensorManager sensors;
    private final TextView status;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] rotation = new float[9];
    private final Runnable collect;
    private final Runnable expire = () -> clearTarget();
    private final float density;
    private volatile int steps;
    private long previous;
    private boolean armed=true, oriented, locked;
    private float heading, bearing, gx=.5f, gy=.5f;
    private long seen;
    private String caption="";
    AisleTrail(Context context, TextView label, Runnable reward, int savedSteps) {
        super(context);status=label;collect=reward;steps=savedSteps;
        density=getResources().getDisplayMetrics().density;
        sensors=(SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        setOnClickListener(v->{if(seen!=0 && SystemClock.elapsedRealtime()-seen<1300)collect.run();});
        update();
    }
    int progress(){return steps;}
    void start(){
        Sensor accel=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accel==null || !sensors.registerListener(this,accel,SensorManager.SENSOR_DELAY_GAME)){
            steps=10;update();status.setText("Geen bewegingssensor · Richt op de barcode in het pastarek.");return;
        }
        Sensor rot=sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(rot!=null)sensors.registerListener(this,rot,SensorManager.SENSOR_DELAY_UI);
    }
    void stop(){sensors.unregisterListener(this);clearTarget();}
    boolean atShelf(){return steps>=10;}
    private void update(){
        caption=steps<4?"Loop rustig richting het pastarek · "+steps+"/4 stappen":steps<10?"Volg de vloerpijl als het pad vrij is · "+(steps-4)+"/6 stappen":"Sta stil · Richt op het pastarek en zoek de barcode van Barilla Nº5 (500 g)";
        status.setText(caption);setContentDescription(caption);invalidate();
    }
    void target(float x,float y){
        if(!atShelf())return;gx=Math.max(.08f,Math.min(.92f,x));gy=Math.max(.12f,Math.min(.86f,y));seen=SystemClock.elapsedRealtime();
        status.setText("Vondst! Tik op het gloeiende doel om te scannen.");setContentDescription("Scan gloeiende vondst");removeCallbacks(expire);postDelayed(expire,1300);invalidate();
    }
    private void clearTarget(){removeCallbacks(expire);seen=0;update();}
    @Override public void onSensorChanged(SensorEvent e){
        if(e.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rotation,e.values);
            if(Math.hypot(rotation[2],rotation[5])>.18){heading=(float)Math.atan2(-rotation[2],-rotation[5]);oriented=true;if(steps>=4&&!locked){bearing=heading;locked=true;}if(steps>=4&&steps<10)invalidate();}return;
        }
        if(atShelf())return;float x=e.values[0],y=e.values[1],z=e.values[2];double force=Math.sqrt(x*x+y*y+z*z);
        if(force<10.2)armed=true;long t=e.timestamp/1000000;
        if(armed&&force>11.6&&t-previous>330){armed=false;previous=t;steps++;if(steps==4&&oriented){bearing=heading;locked=true;}update();}
    }
    public void onAccuracyChanged(Sensor sensor,int accuracy){}
    @Override protected void onDraw(Canvas c){
        float w=getWidth(),h=getHeight();paint.reset();paint.setAntiAlias(true);
        if(steps>=4&&!atShelf()){
            float a=locked?bearing-heading:0;
            c.save();c.translate(w*.5f,h*.75f);c.scale(1,.55f);c.rotate((float)Math.toDegrees(a));
            float r=Math.min(w*.17f,85*density);Path p=new Path();p.moveTo(0,-r);p.lineTo(r*.78f,0);p.lineTo(r*.28f,-r*.1f);p.lineTo(r*.28f,r*.8f);p.lineTo(-r*.28f,r*.8f);p.lineTo(-r*.28f,-r*.1f);p.lineTo(-r*.78f,0);p.close();paint.setColor(0xffffd143);paint.setShadowLayer(8*density,0,0,0xffffa900);c.drawPath(p,paint);c.restore();
        }else if(atShelf()){
            boolean glow=seen!=0;float x=w*(glow?gx:.5f),y=h*(glow?gy:.5f);float r=Math.min(w,h)*(glow?.115f:.15f);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(density*(glow?5:2));paint.setColor(glow?0xffffdf4e:0x99ffffff);if(glow)paint.setShadowLayer(12*density,0,0,0xffffaa00);c.drawCircle(x,y,r,paint);
        }
    }
}
