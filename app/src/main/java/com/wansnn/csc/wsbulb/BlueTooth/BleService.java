
package com.wansnn.csc.wsbulb.BlueTooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.wansnn.csc.wsbulb.BluetoothUtils;
import com.wansnn.csc.wsbulb.ScanActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BleService extends Service implements Constants, BleListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("code_count");
    }
    public native byte[] enCodeBleData(BleData buf, int length);
    public native byte[] deCodeBleData(byte[] buf);

    //Debug
    private static final String TAG = BleService.class.getName();

    //Member fields
    private BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothDevice> mScanLeDeviceList = new ArrayList<>();
    private boolean isScanning;
    private boolean isConnect;
    private String mBluetoothDeviceAddress;
    private int mConnState = STATE_DISCONNECTED;
    // Stop scanning after 10 seconds.
    private static final long SCAN_PERIOD = 60*60*1000;

    private OnLeScanListener mOnLeScanListener;
    private OnConnectionStateChangeListener mOnConnectionStateChangeListener;
    private OnServicesDiscoveredListener mOnServicesDiscoveredListener;
    private OnDataAvailableListener mOnDataAvailableListener;
    private OnReadRemoteRssiListener mOnReadRemoteRssiListener;
    private OnMtuChangedListener mOnMtuChangedListener;

    private final IBinder mBinder = new LocalBinder();
    private static BleService instance = null;
    private ScanCallback mScanCallback;

    public long sendDataTime;
    public long receiveTime;

    private byte sendChar[] = new byte[6];
    private boolean isVerified = false;
    public String musicBleMac;

    public BleService() {
        instance = this;
    }

    public static BleService getInstance() {
        if (instance == null) throw new NullPointerException("BleService is not bind.");
        return instance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(mReceiver, filter);
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    /**
     * Check for your device to support Ble
     *
     * @return true is support    false is not support
     */
    public boolean isSupportBle() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return If return true, the initialization is successful.
     */
    public boolean initialize() {
        //For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
//            new Thread(new Runnable(){
//                @Override
//                public void run(){
//                    while (true) {
//                        try {
//                            if (isConnect &&  receiveTime!=0){
//                                sendDataTime = System.currentTimeMillis();
//                                writeCharacteristic(BUUID.BLESERVICE,BUUID.BLESENDCHARACTERISTIC, "0000".getBytes());
//                                if( receiveTime!=0 && sendDataTime - receiveTime >5000){
//                                    Log.e(TAG, sendDataTime+"断开连接 =============="+receiveTime );
//                                    receiveTime = 0;
//                                    disconnect();
//                                }
//                            }
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to initialize BluetoothAdapter.");
            return false;
        }
        return true;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        Log.e(TAG, "onScanResult: 收到广播数据" );
                        for (BluetoothDevice device :mScanLeDeviceList){
                            String tmpName = device.getName();
                            String tmpAddress = device.getAddress();
                            if (tmpName != null && tmpAddress != null){
//                                Log.e(TAG, "已存在设备: "+ tmpName.toString()+"   "+tmpAddress.toString());
                            }else if (tmpName != null){
                                Log.e(TAG, "已存在名称: "+ tmpName.toString());
                            }else if (tmpAddress != null){
//                                Log.e(TAG, "已存在地址: "+ tmpAddress.toString());
                            }else {
                                Log.e(TAG, "已存在: 未知名称，未知地址");
                            }
                        }
                        if (mScanLeDeviceList.contains(result.getDevice())) return;
                        String tmpDevName = result.getDevice().getName();
                        String tmpDevAddress = result.getDevice().getAddress().toString();
//                        Log.e(TAG, "onScanResult: 新的设备"+tmpDevAddress);
                        if (tmpDevName != null){
                            tmpDevName = tmpDevName.toString();
//                            Log.e(TAG, "onScanResult: 新的设备名称="+tmpDevName);
                        }else {
                            tmpDevName = "未命名";
                        }
                        mScanLeDeviceList.add(result.getDevice());
                        if (mOnLeScanListener != null) {
                            mOnLeScanListener.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                        }
                        if (result.getScanRecord().getServiceUuids() == null) return;

                        String mServiceUuids = result.getScanRecord().getServiceUuids().toString();
//                        Log.e(TAG, "getServiceUuids:不为空 ：" +mServiceUuids);
                        //ad120387-0000-1000-8000-00805f9b34fb
                        if (mServiceUuids.indexOf("ad120387-0000-1000-8000-00805f9b34fb") != -1 || tmpDevName.equals("SmartBulb") ) {
//                            Log.e(TAG, "发现设备name: " + tmpDevName + ", address: " + tmpDevAddress);
                            broadcastUpdate(ACTION_BLUETOOTH_DEVICE, result.getDevice());
                            connect(result.getDevice().getAddress().toString());
                        } else {
//                            Log.e(TAG, "onScanResult: 广播数据不对");
                        }
                    }
                }
            };
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device == null || mScanLeDeviceList.contains(device)) return;
                    mScanLeDeviceList.add(device);
                    if (mOnLeScanListener != null) {
                        mOnLeScanListener.onLeScan(device, rssi, scanRecord);
                    }
                    broadcastUpdate(ACTION_BLUETOOTH_DEVICE, device);
                }
            };
        }
    }

    public boolean enableBluetooth(boolean enable) {
        if (enable) {
            if (!mBluetoothAdapter.isEnabled()) {
                return mBluetoothAdapter.enable();
            }
            return true;
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                return mBluetoothAdapter.disable();
            }
            return false;
        }
    }

    /**
     * Return true if Bluetooth is currently enabled and ready for use.
     *
     * @return true if the local adapter is turned on
     */
    public boolean isEnableBluetooth() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Scan Ble device.
     *
     * @param enable     If true, start scan ble device.False stop scan.
     * @param scanPeriod scan ble period time
     */
    public void scanLeDevice(final boolean enable, long scanPeriod) {
        if (enable) {
            if (isScanning) return;
            //Stop scanning after a predefined scan period.
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    } else {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                    broadcastUpdate(ACTION_SCAN_FINISHED);
                    mScanLeDeviceList.clear();
                }
            }, scanPeriod);
            mScanLeDeviceList.clear();
            isScanning = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(mBluetoothAdapter.getBluetoothLeScanner() != null){
                    Log.e(TAG, "scanLeDevice: 1111");
                    mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
                }else {
                    Log.e(TAG, "scanLeDevice: 222");
                    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(receiver, filter);
                }
            } else {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {
            isScanning = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            broadcastUpdate(ACTION_SCAN_FINISHED);
            mScanLeDeviceList.clear();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: 有一条消息" );
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                Log.e(TAG, "onReceive: 消息编号:"+state );
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG, "onReceive: 蓝牙关闭" );
                        enableBluetooth(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e(TAG, "onReceive: 蓝牙手动关闭" );
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //to check if BluetoothAdapter is enable by your code
                        isScanning = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if(mBluetoothAdapter.getBluetoothLeScanner() != null){
                                mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
                            }
                        } else {
                            mBluetoothAdapter.startLeScan(mLeScanCallback);
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e(TAG, "onReceive: 正在打开" );
                        break;
                }
            }
        }
    };

    /**
     * Scan Ble device.
     *
     * @param enable If true, start scan ble device.False stop scan.
     */
    public void scanLeDevice(boolean enable) {
        this.scanLeDevice(enable, SCAN_PERIOD);
    }

    /**
     * If Ble is scaning return true, if not return false.
     *
     * @return ble whether scanning
     */
    public boolean isScanning() {
        return isScanning;
    }

    public boolean connect(final String address) {
        if (isScanning) scanLeDevice(false);
        close();
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        //Previously connected device.  Try to reconnect.
        if (mBluetoothGatt != null && mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)) {
            Log.e(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        //We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the BluetoothGattCallback#onConnectionStateChange.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized.");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        isConnect = false;
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(String serviceUUID, String characteristicUUID) {
        if (mBluetoothGatt != null) {
            BluetoothGattService service =
                    mBluetoothGatt.getService(UUID.fromString(serviceUUID));
            BluetoothGattCharacteristic characteristic =
                    service.getCharacteristic(UUID.fromString(characteristicUUID));
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }



    public boolean writeCharacteristic(String serviceUUID, String characteristicUUID, byte[] value) {
        if (mBluetoothGatt != null) {
            BluetoothGattService service =
                    mBluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (service == null){
                Log.e("writeCharacteristic","发送数据时未能获取到service");
                return false;
            }
            BluetoothGattCharacteristic characteristic =
                    service.getCharacteristic(UUID.fromString(characteristicUUID));
            if (characteristic == null){
                Log.e("writeCharacteristic","发送数据时未能获取到characteristic");
                return false;
            }
            if (value.length == 18){
                Log.e(TAG, "======是验证指令 " );
            }else if(!isVerified){
                Log.e(TAG, "======未验证，不能发送指令" );
                return false;
            }
            Log.e(TAG, "writeCharacteristic: =====数据发送:"+BluetoothUtils.byteArray2HexStr(value) );
            characteristic.setValue(value);

            return mBluetoothGatt.writeCharacteristic(characteristic);
        }
        return false;
    }


    public void setCharacteristicNotification(String serviceUUID, String characteristicUUID, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService service =
                mBluetoothGatt.getService(UUID.fromString(serviceUUID));
        BluetoothGattCharacteristic characteristic =
                service.getCharacteristic(UUID.fromString(characteristicUUID));

        boolean isEnableNotification = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (isEnableNotification){
            List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
            if(descriptorList != null && descriptorList.size() > 0) {
                for(BluetoothGattDescriptor descriptor : descriptorList) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    boolean isSuccess = mBluetoothGatt.writeDescriptor(descriptor);
                    if (isSuccess) Log.e(TAG, "setCharacteristicNotification: 成功" );
                    else Log.e(TAG, "setCharacteristicNotification: 失败" );
                }
            }
        }else {
            Log.e(TAG, "setCharacteristicNotification: 没有写入" );
        }

    }


    public boolean readDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothGatt is null");
            return false;
        }
        return mBluetoothGatt.readDescriptor(descriptor);
    }

    public boolean readDescriptor(String serviceUUID, String characteristicUUID,
                                  String descriptorUUID) {
        if (mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothGatt is null");
            return false;
        }
//        try {
        BluetoothGattService service =
                mBluetoothGatt.getService(UUID.fromString(serviceUUID));
        BluetoothGattCharacteristic characteristic =
                service.getCharacteristic(UUID.fromString(characteristicUUID));
        BluetoothGattDescriptor descriptor =
                characteristic.getDescriptor(UUID.fromString(descriptorUUID));
        return mBluetoothGatt.readDescriptor(descriptor);
//        } catch (Exception e) {
//            Log.e(TAG, "read descriptor exception", e);
//            return false;
//        }
    }

    public boolean readRemoteRssi() {
        if (mBluetoothGatt == null) return false;
        return mBluetoothGatt.readRemoteRssi();
    }


    public boolean requestMtu(int mtu) {
        if (mBluetoothGatt == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//Android API level >= 21
            return mBluetoothGatt.requestMtu(mtu);
        } else {
            return false;
        }
    }

    public boolean isConnect() {
        return isConnect;
    }

    public BluetoothDevice getConnectDevice() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getDevice();
    }

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    public List<BluetoothDevice> getConnectDevices() {
        if (mBluetoothManager == null) return null;
        return mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
    }

    /**
     * Implements callback methods for GATT events that the app cares about.  For example,
     * connection change and services discovered.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e(TAG, "onConnectionStateChange: 设备状态发生变化" );
            String intentAction;
            String address = gatt.getDevice().getAddress();
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "onConnectionStateChange: DISCONNECTED: " + getConnectDevices().size());
                isVerified = false;
                intentAction = ACTION_GATT_DISCONNECTED;
                isConnect = false;
                mConnState = STATE_DISCONNECTED;
                Log.e(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction, address);
               // close();
                /* todo: changed for checking by manmohan chauhan
                    uncomment below line if you want to display scan activity*/
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
             //   scanLeDevice(true);
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                isConnect = false;
                intentAction = ACTION_GATT_CONNECTING;
                mConnState = STATE_CONNECTING;
                broadcastUpdate(intentAction, address);
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                isConnect = true;
                mConnState = STATE_CONNECTED;
                broadcastUpdate(intentAction, address);

                Log.e(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.e(TAG, "Attempting to start service discovery:" +mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                Log.e(TAG, "onConnectionStateChange: DISCONNECTING: " + getConnectDevices().size());
                isConnect = false;
                intentAction = ACTION_GATT_DISCONNECTING;
                mConnState = STATE_DISCONNECTING;
                Log.e(TAG, "Disconnecting from GATT server.");
                broadcastUpdate(intentAction, address);
                disconnect();
            }
        }

        // New services discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt,status);
            for (BluetoothGattService service : gatt.getServices()) {
                String serviceUuid = service.getUuid().toString();
//                Log.e(TAG, "服务===" + serviceUuid);
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for (int i = 0; i < characteristics.size(); i++) {
                    String charUuid = characteristics.get(i).getUuid().toString();
//                    Log.e(TAG, "特性===: "+charUuid );
                    int charaProp = characteristics.get(i).getProperties();
                    if((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                        Log.e(TAG, charUuid+"具备通知属性"+serviceUuid );
                        setCharacteristicNotification(serviceUuid,charUuid,true);
                    }else {
//                                Log.e(TAG, charUuid+"====不具备通知属性" );
                    }
                }
            }
            if (mOnServicesDiscoveredListener != null) {
                mOnServicesDiscoveredListener.onServicesDiscovered(gatt, status);
            }
            receiveTime = System.currentTimeMillis();
            sendDataTime = receiveTime;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }



        // Result of a characteristic read operation
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "收到数据======="+BluetoothUtils.byteArray2HexStr(characteristic.getValue()) );
            super.onCharacteristicRead(gatt,characteristic,status);
            receiveTime = System.currentTimeMillis();
            if (mOnDataAvailableListener != null) {
                mOnDataAvailableListener.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            String address = gatt.getDevice().getAddress();
            Log.e(TAG, "address: " + address + ",Write: " + BluetoothUtils.byteArray2HexStr(characteristic.getValue()));
            if (sendDataTime != 0){
                sendDataTime = System.currentTimeMillis();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt,characteristic);
            Log.e(TAG, "onCharacteristicChanged : "+ BluetoothUtils.byteArray2HexStr(characteristic.getValue()));
            receiveTime = System.currentTimeMillis();
            if (mOnDataAvailableListener != null) {
                mOnDataAvailableListener.onCharacteristicChanged(gatt, characteristic);
            }
            if (characteristic.getValue().length == 18){
                Log.e(TAG, "onCharacteristicChanged getValue is : "+characteristic.getValue());
                byte[] tou = deCodeBleData(characteristic.getValue());

                for(Byte b:tou){
                    Log.e(TAG, "onCharacteristicChanged tou byte is : "+b);
                }
                for(Byte b:sendChar){
                    Log.e(TAG, "onCharacteristicChanged sendchar byte is : "+b);
                }
                if (isVerified){
                    tou[3] += 3;
                    send((byte) 'C',6, tou);
                    Log.e(TAG, "=====反馈验证指令 " );
                }else if(tou[3] == sendChar[3]+3){
                    Log.e(TAG, "=====验证成功 " );
                    isVerified = true;
//                    if (mOnConnectionStateChangeListener != null) {
//                        mOnConnectionStateChangeListener.onConnectionStateChange(gatt, STATE_DISCONNECTED, STATE_CONNECTED);
//                    }
                }else {
                    isVerified = false;
                    Log.e(TAG, "=====验证失败 " );
                }
            }
            byte[] data = characteristic.getValue();
            if(isVerified && data[3] == 'M'){
                musicBleMac = BluetoothUtils.byteArray2HexStr(characteristic.getValue()).substring(8,20);
                Log.e(TAG, "onCharacteristicChanged:地址 "+musicBleMac );
                if (mOnConnectionStateChangeListener != null) {
                    mOnConnectionStateChangeListener.onConnectionStateChange(gatt, 0, 1);
                }
            }
        }

        //连接建立时。发送加密指令验证是否属于本公司设备
        private void send(byte message, int length, byte[] array){
            BleData cmd = new BleData();
            Log.e(TAG, "send: message : "+ message);
            Log.e(TAG, "send: length : "+ length);
            Log.e(TAG, "send: aaray : " + Arrays.toString(array));
            cmd.message = message;
            cmd.length = (byte)length;
            cmd.mData = array;
            encodeCommand(cmd);
        }
        public void encodeCommand(BleData buf)  {
            Log.e(TAG, "encodeCommand: "+buf );
 //           byte[] tou = enCodeBleData(buf,buf.length+2);
            byte[] tou ={75, 8, 17, -96, 10, 32, 9, -128, 13, -128, 15, -128, 17, -128, 9, 96, 9, 96};
//            for (int m=0; m<tou.length; m++){
//                Log.e(TAG, "encodeCommand: "+m+" :"+tou[m]);
//            }

        Log.e(TAG, "发送数据======="+tou.length+",==="+Arrays.toString(tou));
            writeCharacteristic(BUUID.BLESERVICE,BUUID.BLESENDCHARACTERISTIC, tou);
        }

        public String bytes2HexString(byte[] b) {
            String ret = "";
            for (int i = 0; i < b.length; i++) {
                String hex = Integer.toHexString(b[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }//from   w w w .  j a  v a 2s  .  c o m
                ret += hex.toUpperCase();
            }
            return ret;
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt,descriptor,status);
            if (mOnDataAvailableListener != null) {
                mOnDataAvailableListener.onDescriptorRead(gatt, descriptor, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "onDescriptorWrite: 这里写入成功啊" );
            if(!isVerified){
                int randomNumber = getRandomNumber(1,240);
                sendChar[0] =  (byte) (randomNumber&0xff) ;
                sendChar[1] =  (byte) (randomNumber&0xff) ;
                sendChar[2] =  (byte) (randomNumber&0xff) ;
                sendChar[3] =  (byte) (randomNumber&0xff) ;
                sendChar[0] =  0x01;
                sendChar[1] =  0x21;
                sendChar[2] =  0x31;
                sendChar[3] =  0x41;
                sendChar[4] =  0;
                sendChar[5] =  0;
                Log.e(TAG, "onDescriptorWrite: sendCharacter : "+sendChar );
                send((byte) 'B',6, sendChar);
            }
        }
        public int getRandomNumber(int min,int max){
            Random random = new Random();
            int num = random.nextInt(max)%(max-min+1) + min;
            return num;
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (mOnReadRemoteRssiListener != null) {
                mOnReadRemoteRssiListener.onReadRemoteRssi(gatt, rssi, status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (mOnMtuChangedListener != null) {
                mOnMtuChangedListener.onMtuChanged(gatt, 1, status);
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String address) {
        final Intent intent = new Intent(action);
        intent.putExtra("address", address);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, BluetoothDevice device) {
        final Intent intent = new Intent(action);
        Log.e(TAG, "broadcastUpdate: name: "+device.getName() );
        Log.e(TAG, "broadcastUpdate: address: "+device.getAddress() );
        intent.putExtra("name", device.getName());
        intent.putExtra("address", device.getAddress());
        sendBroadcast(intent);
    }

    public void setOnLeScanListener(OnLeScanListener l) {
        mOnLeScanListener = l;
    }

    public void setOnConnectListener(OnConnectionStateChangeListener l) {
        mOnConnectionStateChangeListener = l;
    }

    public void setOnServicesDiscoveredListener(OnServicesDiscoveredListener l) {
        mOnServicesDiscoveredListener = l;
    }

    public void setOnDataAvailableListener(OnDataAvailableListener l) {
        mOnDataAvailableListener = l;
    }

    public void setOnReadRemoteRssiListener(OnReadRemoteRssiListener l) {
        mOnReadRemoteRssiListener = l;
    }

    public void setOnMtuChangedListener(OnMtuChangedListener l) {
        mOnMtuChangedListener = l;
    }

    /* For listening the bluetooth state* changed by manmohan chauhan*/

//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//
//            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
//                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
//                        BluetoothAdapter.ERROR);
//                switch (state) {
//                    case BluetoothAdapter.STATE_OFF:
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(),"Please Turn on the Bluetooth",Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        break;
//                    case BluetoothAdapter.STATE_TURNING_OFF:
//                        break;
//                    case BluetoothAdapter.STATE_ON:
//                        Intent intent2 = new Intent(getApplicationContext(), ScanActivity.class);
//                        startActivity(intent2);
//                       // scanLeDevice(true);
//                        break;
//                    case BluetoothAdapter.STATE_TURNING_ON:
//                        break;
//                }
//            }
//        }
//    };
}