package nl.jumbospeur.game;
import android.hardware.*;import android.location.*;import android.Manifest;import android.content.pm.PackageManager;import android.app.*;import android.os.*;import android.webkit.*;import android.content.Intent;import com.google.zxing.integration.android.*;
public class Main extends Activity implements SensorEventListener,LocationListener { WebView web; public void onCreate(Bundle b){super.onCreate(b); web=new WebView(this);setContentView(web);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.setWebViewClient(new WebViewClient(){public boolean shouldOverrideUrlLoading(WebView w,WebResourceRequest r){return true;}});web.addJavascriptInterface(this,"Game");web.loadUrl("file:///android_asset/index.html");}
 @JavascriptInterface public void openWifi(){runOnUiThread(()->{
   if(Build.VERSION.SDK_INT>=29 && openSettings(android.provider.Settings.Panel.ACTION_WIFI))return;
   if(!openSettings(android.provider.Settings.ACTION_WIFI_SETTINGS))android.widget.Toast.makeText(this,"Open Wifi via de instellingen van je toestel.",android.widget.Toast.LENGTH_LONG).show();
 });}
 boolean openSettings(String action){try{startActivity(new Intent(action));return true;}catch(android.content.ActivityNotFoundException|SecurityException unavailable){return false;}}
 boolean gps,motion,foreground,prompted,tracking,listening;
 LocationManager lm(){return (LocationManager)getSystemService(LOCATION_SERVICE);} SensorManager sm(){return (SensorManager)getSystemService(SENSOR_SERVICE);}
 @JavascriptInterface public void instruments(boolean g,boolean m){runOnUiThread(()->{if(!g)prompted=false;gps=g;motion=m;refresh();});}
 void js(String script){web.evaluateJavascript(script,null);}
 void refresh(){
 if(!foreground||!gps){if(tracking){lm().removeUpdates(this);tracking=false;}}
 else if(!tracking){if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){if(!prompted){prompted=true;requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},72);}else js("note('gps','Geen locatietoestemming')");}
 else try{String provider=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED && lm().isProviderEnabled(LocationManager.GPS_PROVIDER)?LocationManager.GPS_PROVIDER:LocationManager.NETWORK_PROVIDER;if(lm().isProviderEnabled(provider)){lm().requestLocationUpdates(provider,10000,8,this);tracking=true;}else js("note('gps','Locatie niet beschikbaar')");}catch(RuntimeException ex){js("note('gps','Geen locatie beschikbaar')");}}
 if(foreground&&motion&&!listening){Sensor a=sm().getDefaultSensor(Sensor.TYPE_ACCELEROMETER);if(a!=null)listening=sm().registerListener(this,a,100000);if(!listening)js("for(const k of ['steps','motion'])if(prefs[k])note(k,'Geen bewegingssensor')");}else if((!foreground||!motion)&&listening){sm().unregisterListener(this);listening=false;}}
 public void onRequestPermissionsResult(int r,String[] p,int[] results){super.onRequestPermissionsResult(r,p,results);if(r==72)refresh();}
 protected void onResume(){super.onResume();foreground=true;refresh();} protected void onPause(){foreground=false;refresh();super.onPause();}
 public void onLocationChanged(Location l){if(foreground&&gps)js("locationFix("+l.getLatitude()+","+l.getLongitude()+","+l.getAccuracy()+")");}
 public void onProviderEnabled(String p){} public void onProviderDisabled(String p){if(gps)js("note('gps','Locatie uitgeschakeld')");}public void onStatusChanged(String p,int s,Bundle b){}
 public void onAccuracyChanged(Sensor s,int a){} public void onSensorChanged(SensorEvent e){if(motion&&foreground)js("acceleration("+e.values[0]+","+e.values[1]+","+e.values[2]+")");}
 @JavascriptInterface public void scan(){runOnUiThread(()->new IntentIntegrator(this).setBeepEnabled(false).setOrientationLocked(false).setPrompt("Richt op een productbarcode").initiateScan());} protected void onActivityResult(int r,int c,Intent i){super.onActivityResult(r,c,i);IntentResult res=IntentIntegrator.parseActivityResult(r,c,i);if(res!=null && res.getContents()!=null)web.evaluateJavascript("barcode("+org.json.JSONObject.quote(res.getContents())+")",null);} }
