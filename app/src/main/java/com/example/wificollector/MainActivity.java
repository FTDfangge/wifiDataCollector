package com.example.wificollector;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button buttonRefresh;
    private Button buttonRecord;
    private TextView displayText;
    WifiManager wifiManager;
    private List<ScanResult> resultList;
    private static final int REQUEST_CODE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
//            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };
    private String recordPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wifidata/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

//        if (Build.VERSION.SDK_INT >= 30){
//            if (!Environment.isExternalStorageManager()){
//                Intent getpermission = new Intent();
//                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                startActivity(getpermission);
//            }
//        }

        buttonRefresh = findViewById(R.id.buttonRefresh);
        buttonRecord = findViewById(R.id.buttonRecord);
        displayText = findViewById(R.id.textView2);
        displayText.setMovementMethod(new ScrollingMovementMethod());
        buttonRefresh.setText("REFRESH");
        buttonRecord.setText("RECORD");
        displayText.setText("");

        buttonRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final boolean[] couldUnregister = {false};
                Log.d("Click","clicked the button");
                buttonRefresh.setEnabled(false);
                buttonRecord.setEnabled(false);
                Context context = getApplicationContext();
                wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                            if (success) {
                                resultList = scanSuccess();
                                buttonRefresh.setEnabled(true);
                                buttonRecord.setEnabled(true);
                                Log.d("WIFI REFRESHED","WIFI REFRESHED");


                                couldUnregister[0] = true;

                            } else {
                                scanFailed();
                                buttonRefresh.setEnabled(true);
                                buttonRecord.setEnabled(true);
                                couldUnregister[0] = true;
                            }
                        }catch (RuntimeException e){
                            e.printStackTrace();
                        }
                    }
                };

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                context.registerReceiver(wifiScanReceiver,intentFilter);
                wifiManager.startScan();

                new Thread(){
                    public void run() {
                        while(!couldUnregister[0]){

                        }
                        if (couldUnregister[0]){
                            context.unregisterReceiver(wifiScanReceiver);
                            wifiManager = null;
                            System.gc();
                            Log.d("Thread","wifiScanner has benn unregistered");
                        }
                    }
                }.start();
            }
        });

        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                Date date = new Date();
                try {
                    FileOperation fileOperation = new FileOperation(recordPath, formatter.format(date) +".csv");
                    for (ScanResult result : resultList){
                        fileOperation.writeCsv(result.BSSID+","+result.SSID+","+String.valueOf(result.level)+","+String.valueOf(result.frequency));

                    }
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "记录成功",Toast.LENGTH_SHORT);
                    toast.show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "记录失败", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });
    }

    private List<ScanResult> scanSuccess()
    {
        displayText.setText("");
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, "SCAN SUCCESS",Toast.LENGTH_SHORT);
        toast.show();
        List<ScanResult> scanResultList = wifiManager.getScanResults();
        Iterator<ScanResult> itr = scanResultList.iterator();
        while (itr.hasNext())
        {
            ScanResult result = itr.next();
            displayText.append("\n"+String.format("%-20s",result.BSSID)+","+String.format("%-20s",result.SSID)+","+result.level+"\n");
        }
        return scanResultList;
    }

    public List<ScanResult> getResultList()
    {
        return resultList;
    }

    private void scanFailed()
    {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, "SCAN FAILED",Toast.LENGTH_SHORT);
        toast.show();
        displayText.setText("\nSCAN FAILED\n");
    }

    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_CODE
            );
        } else {
            //Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
            Log.i("Permission", "checkPermission: 已经授权！");
        }
        //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
        }
    }
}