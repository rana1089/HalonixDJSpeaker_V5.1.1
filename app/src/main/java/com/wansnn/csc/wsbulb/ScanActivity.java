package com.wansnn.csc.wsbulb;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.Toast;

import com.wansnn.csc.wsbulb.BlueTooth.BleService;
import com.wansnn.csc.wsbulb.BlueTooth.MyGattAttributes;
import com.wansnn.csc.wsbulb.music.myBLEDataSenderReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csc on 2017/10/30.
 */

public class ScanActivity extends Activity {

    private static final String TAG = ScanActivity.class.getName();

    private Button skipButton;
    private boolean isFront = false;
    //Constant
    public static final int SERVICE_BIND = 1;
    public static final int SERVICE_SHOW = 2;
    public static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private BleService mBleService;


    private List<String> serviceList;
    private List<String[]> characteristicList;

    myBLEDataSenderReceiver bleDateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        skipButton = findViewById(R.id.glas_scan_bt_skip);//获取按钮资源
        skipButton.setOnClickListener(listener);//设置监听

        RotateAnimation anim = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(2000);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setFillBefore(false);
        anim.setFillAfter(false);
        anim.setFillEnabled(false);
        findViewById(R.id.blue_scan_iv_prog).setAnimation(anim);

        serviceList = new ArrayList<>();
        characteristicList = new ArrayList<>();
        registerReceiver(bleReceiver, makeIntentFilter());
        doBindService();

        //实例化IntentFilter对象
        IntentFilter filter = new IntentFilter();
        filter.addAction("BleData");
        bleDateReceiver = new myBLEDataSenderReceiver();
        //注册广播接收
        registerReceiver(bleDateReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnBindService();
        Log.e(TAG, "onDestroy: 撤销注册通知" );
        unregisterReceiver(bleReceiver);
        unregisterReceiver(bleDateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFront = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFront = false;
    }

    Button.OnClickListener listener = new Button.OnClickListener(){//创建监听对象
        public void onClick(View v) {
            startActivity(new Intent(ScanActivity.this, MainActivity.class));
        }
    };


    private BroadcastReceiver bleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BleService.ACTION_BLUETOOTH_DEVICE)) {
                if (intent.hasExtra("name")){
                    String tmpDevName = intent.getStringExtra("name");
                    String tmpDevAddress = intent.getStringExtra("address");
                    Log.e(TAG, "发现设备name: " + tmpDevName + ", address: " + tmpDevAddress);
//                    && tmpDevName.startsWith("BSW")&&tmpDevName.endsWith("SYNC")
//                    if(tmpDevName != null ){
//                        Log.e(TAG, "onReceive: 找到BSW-SYNC" );
//                        mBleService.connect(tmpDevAddress);
//                    }
                }
            } else if (intent.getAction().equals(BleService.ACTION_GATT_CONNECTED)) {
                Log.e(TAG, "onReceive: 连接成功" );

                if(isFront){
                    startActivity(new Intent(ScanActivity.this, MainActivity.class));
                }
            } else if (intent.getAction().equals(BleService.ACTION_GATT_DISCONNECTED)) {
                serviceList.clear();
                characteristicList.clear();
//                String tmpDevAddress = intent.getStringExtra("address");
//                mBleService.connect(tmpDevAddress);
                mBleService.scanLeDevice(true);
                Log.e(TAG, "onReceive: 失去连接" );
            } else if (intent.getAction().equals(BleService.ACTION_SCAN_FINISHED)) {
                Log.e(TAG, "onReceive: 扫描完成" );
            }
        }
    };

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_BLUETOOTH_DEVICE);
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_SCAN_FINISHED);
        intentFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        return intentFilter;
    }

    /**
     * 绑定服务
     */
    private void doBindService() {
        Intent serviceIntent = new Intent(this, BleService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑服务
     */
    private void doUnBindService() {
        unbindService(serviceConnection);
        mBleService = null;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = BleService.getInstance();
            if (mBleService != null) mHandler.sendEmptyMessage(SERVICE_BIND);
            if (mBleService.initialize()) {
                if (mBleService.enableBluetooth(true)) {
                    verifyIfRequestPermission();
                    Log.e(TAG, "Bluetooth was opened" );
                }
            } else {
                Log.e(TAG, "not support Bluetooth" );
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
            Log.e(TAG, "onServiceDisconnected: 设备断开连接" );
        }
    };

    private void verifyIfRequestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            Log.e(TAG, "onCreate: checkSelfPermission   " );
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onCreate: Android 6.0 动态申请权限");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    Log.e(TAG, "*********onCreate: shouldShowRequestPermissionRationale**********");
                    Toast.makeText(this, "只有允许访问位置才能搜索到蓝牙设备", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_COARSE_LOCATION);
                }
            } else {
                mBleService.scanLeDevice(true);
            }
        } else {
            mBleService.scanLeDevice(true);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVICE_BIND:
                    setBleServiceListener();
                    break;
                case SERVICE_SHOW:
                    break;
            }
        }
    };

    private void setBleServiceListener() {

        //Ble服务发现回调
        mBleService.setOnServicesDiscoveredListener(new BleService.OnServicesDiscoveredListener() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.e(TAG, "onServicesDiscovered: 发现服务"+gatt.getServices().toString());
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    serviceList.clear();

                    for (BluetoothGattService service : gatt.getServices()) {
                        String serviceUuid = service.getUuid().toString();

                        serviceList.add(MyGattAttributes.lookup(serviceUuid, "Unknown") + "===" + serviceUuid);

                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        String[] charArra = new String[characteristics.size()];
                        for (int i = 0; i < characteristics.size(); i++) {
                            String charUuid = characteristics.get(i).getUuid().toString();
                            charArra[i] = MyGattAttributes.lookup(charUuid, "Unknown") + "\n" + charUuid;

                        }
                        characteristicList.add(charArra);
                    }
                    mHandler.sendEmptyMessage(SERVICE_SHOW);
                }
            }
        });

        mBleService.setOnReadRemoteRssiListener(new BleService.OnReadRemoteRssiListener() {
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                Log.e(TAG, "onReadRemoteRssi: rssi = " + rssi);
            }
        });

        mBleService.setOnDataAvailableListener(new BleService.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                //处理特性读取返回的数据
                Log.e(TAG, "onCharacteristicRead: 处理读取的数据" );
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                //处理通知返回的数据
                Log.e(TAG, "onCharacteristicChanged:处理通知返回的数据 " );
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.e(TAG, "onDescriptorRead: $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" );
            }
        });
    }
}

