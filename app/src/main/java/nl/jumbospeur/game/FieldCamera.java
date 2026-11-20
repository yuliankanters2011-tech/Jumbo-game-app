package nl.jumbospeur.game;

import android.Manifest;
import androidx.activity.ComponentActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.*;
import java.nio.ByteBuffer;
import android.os.SystemClock;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import java.util.EnumMap;
import com.google.zxing.BarcodeFormat;
import java.util.Arrays;

/** Offline viewfinder: camera preview first, virtual posts and controls on top. */
public class FieldCamera extends ComponentActivity {
    private PreviewView preview;
    private final ExecutorService decoder=Executors.newSingleThreadExecutor();
    private final MultiFormatReader reader=new MultiFormatReader();
    private ProcessCameraProvider cameras;
    private long lastFrame;
    private byte[] pixels=new byte[0];
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
        preview=new PreviewView(this);
        preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        frame.addView(preview,at(-1,-1,Gravity.CENTER));
        EnumMap<DecodeHintType,Object> hints=new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS,Arrays.asList(BarcodeFormat.EAN_13,BarcodeFormat.EAN_8,BarcodeFormat.UPC_A,BarcodeFormat.UPC_E,BarcodeFormat.ITF,BarcodeFormat.CODE_128));
        reader.setHints(hints);
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
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){finishWith("error","Geen cameratoestemming. Typ de barcode of geef toestemming in Android.");return;}
        preview.post(this::startCamera);
    }
    private void startCamera(){
        if(isFinishing()||isDestroyed())return;
        try{
            ListenableFuture<ProcessCameraProvider> future=ProcessCameraProvider.getInstance(this);
            future.addListener(()->{
                if(isFinishing()||isDestroyed())return;
                try{
                    cameras=future.get();
                    CameraSelector lens=cameras.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)?CameraSelector.DEFAULT_BACK_CAMERA:CameraSelector.DEFAULT_FRONT_CAMERA;
                    Preview view=new Preview.Builder().build();view.setSurfaceProvider(preview.getSurfaceProvider());
                    ImageAnalysis frames=new ImageAnalysis.Builder().setTargetResolution(new android.util.Size(1280,720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                    frames.setAnalyzer(decoder,this::readFrame);
                    Camera camera=cameras.bindToLifecycle(this,lens,view,frames);
                    camera.getCameraInfo().getCameraState().observe(this,state->{if(state.getError()!=null)cameraFailed();});
                    preview.getPreviewStreamState().observe(this,state->{if(state==PreviewView.StreamState.STREAMING)message.setText("Camera actief · Richt tussen de virtuele paaltjes");});
                }catch(Exception e){cameraFailed();}
            },ContextCompat.getMainExecutor(this));
        }catch(RuntimeException e){cameraFailed();}
    }
    private void cameraFailed(){finishWith("error","Camera niet beschikbaar. Controleer cameratoegang, sluit andere camera-apps of typ de barcode.");}
    private void readFrame(ImageProxy image){
        try{
            long now=SystemClock.elapsedRealtime();if(now-lastFrame<200)return;lastFrame=now;
            int w=image.getWidth(),h=image.getHeight(),rotation=image.getImageInfo().getRotationDegrees();
            ImageProxy.PlaneProxy y=image.getPlanes()[0];ByteBuffer buffer=y.getBuffer();int stride=y.getRowStride(),step=y.getPixelStride(),base=buffer.position();
            if(pixels.length!=w*h)pixels=new byte[w*h];
            boolean turn=rotation==90||rotation==270;
            for(int row=0;row<h;row++)for(int col=0;col<w;col++){
                int dest=turn?col*h+(h-1-row):row*w+col;
                pixels[dest]=buffer.get(base+row*stride+col*step);
            }
            int width=turn?h:w,height=turn?w:h;
            PlanarYUVLuminanceSource src=new PlanarYUVLuminanceSource(pixels,width,height,0,0,width,height,false);
            String code=reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(src))).getText();
            if(code!=null&&code.matches("[0-9]{8,14}"))runOnUiThread(()->finishWith("code",code));
        }catch(NotFoundException ignored){}catch(RuntimeException error){runOnUiThread(this::cameraFailed);}finally{reader.reset();image.close();}
    }
    @Override protected void onDestroy(){if(cameras!=null)cameras.unbindAll();decoder.shutdown();super.onDestroy();}
}
