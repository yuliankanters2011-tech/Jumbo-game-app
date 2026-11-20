package nl.jumbospeur.game;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import com.journeyapps.barcodescanner.*;
import com.google.zxing.BarcodeFormat;
import java.util.Arrays;

/** Offline viewfinder: camera preview first, virtual posts and controls on top. */
public class FieldCamera extends Activity {
    private BarcodeView preview;
    private TextView message;
    private boolean ended;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private FrameLayout.LayoutParams at(int w,int h,int gravity){return new FrameLayout.LayoutParams(w,h,gravity);}
    private void finishWith(String key,String value){
        if(ended)return;
        ended=true; setResult(RESULT_OK,new Intent().putExtra(key,value)); finish();
    }
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        FrameLayout frame=new FrameLayout(this); frame.setBackgroundColor(Color.BLACK);setContentView(frame);
        preview=new BarcodeView(this);frame.addView(preview,at(-1,-1,Gravity.CENTER));
        preview.addStateListener(new CameraPreview.StateListener(){
            public void previewSized(){} public void previewStarted(){} public void previewStopped(){} public void cameraClosed(){}
            public void cameraError(Exception ex){finishWith("error","Camera bezet of niet beschikbaar. Sluit andere camera-apps of typ de barcode.");}
        });
        preview.setDecoderFactory(new DefaultDecoderFactory(Arrays.asList(BarcodeFormat.EAN_13,BarcodeFormat.EAN_8,BarcodeFormat.UPC_A,BarcodeFormat.UPC_E,BarcodeFormat.ITF,BarcodeFormat.CODE_128)));
        preview.decodeContinuous(result->{String s=result.getText();if(s!=null && s.matches("[0-9]{8,14}"))finishWith("code",s);});
        String store=getIntent().getStringExtra("store");
        for(int i=0;i<2;i++){
            LinearLayout post=new LinearLayout(this);post.setOrientation(LinearLayout.VERTICAL);post.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView sign=new TextView(this);sign.setText(i==0?"⚑ WINKEL":"▥ SCAN");sign.setTextColor(0xff153627);sign.setGravity(Gravity.CENTER);sign.setBackgroundColor(0xffffce50);post.addView(sign,dp(90),dp(36));
            TextView stem=new TextView(this);stem.setBackgroundColor(0xffffce50);post.addView(stem,dp(5),dp(55));
            FrameLayout.LayoutParams pos=at(dp(96),dp(100),Gravity.CENTER_VERTICAL|(i==0?Gravity.LEFT:Gravity.RIGHT));frame.addView(post,pos);
            final String hint=i==0?(store==null||store.isEmpty()?"Kies een filiaal op de speurradar.":"Speurpunt: "+store):"Houd de streepjescode tussen de paaltjes.";
            post.setOnClickListener(v->message.setText(hint));
        }
        message=new TextView(this); message.setText("VELDCAMERA  /  Richt tussen de virtuele paaltjes");message.setGravity(Gravity.CENTER);message.setTextColor(Color.WHITE);message.setBackgroundColor(0xd919352a);message.setPadding(dp(12),dp(16),dp(12),dp(16));frame.addView(message,at(-1,-2,Gravity.TOP));
        Button back=new Button(this);back.setText("Sluiten · handmatig verder");back.setOnClickListener(v->finish());frame.addView(back,at(-1,-2,Gravity.BOTTOM));
    }
    @Override protected void onResume(){super.onResume();if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){finishWith("error","Geen cameratoestemming. Typ de barcode of geef toestemming in Android.");return;}try{preview.resume();}catch(RuntimeException e){finishWith("error","Camera kan niet starten. Typ de barcode.");}}
    @Override protected void onPause(){if(preview!=null)preview.pause();super.onPause();}
}
