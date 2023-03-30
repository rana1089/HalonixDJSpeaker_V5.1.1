
package com.wansnn.csc.wsbulb;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wansnn.csc.wsbulb.BlueTooth.BUUID;
import com.wansnn.csc.wsbulb.BlueTooth.BleService;
import com.wansnn.csc.wsbulb.customView.PaletteView;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_image;
    private Bitmap bitmap;
    private String TAG = "MainActivity";
    private View selectView;
    private SeekBar seekBar;
    GradientDrawable btnPreDrawable;
    private BleService mBleService;
    private int lastBright = 100;
    private TextView percentTV;
    PaletteView paletteView;

    boolean isOpenLight;
    private int lastRed = 255;
    private int lastGreen = 255;
    private int lastBlue = 255;

    private static Boolean connected = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        paletteView= findViewById(R.id.palette_view);
        paletteView.setListener(new PaletteView.PaletteListener() {
            @Override
            public void onColorSelected(int color) {
                Log.i(TAG, "onColorSelected: color rgb"+ Color.red(color)+" , "+Color.green(color)+" , "+Color.blue(color));
                int r,g,b;
                r=Color.red(color);
                g=Color.green(color);
                b=Color.blue(color);
                sendRGB(r,g,b);
            }

            @Override
            public void onSelectXY(double x, double y) {

            }
        });
        paletteView.setCENTER_IMAGE_DRAWABLE(ContextCompat.getDrawable(this,R.drawable.oneimageselect));


        iv_image =  findViewById(R.id.imageView);
        bitmap = ((BitmapDrawable)iv_image.getDrawable())
                .getBitmap();// 获取圆盘图片
        selectView = findViewById(R.id.select_color);
        btnPreDrawable = (GradientDrawable) selectView.getBackground();

        mBleService = BleService.getInstance();
        percentTV = findViewById(R.id.percentTV);
        doBindService();


        iv_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int x = (int) event.getX();
                int y = (int) event.getY();

                int picker_radius = iv_image.getWidth() / 2;// 选择器半径
                int centreX = iv_image.getWidth() / 2;
                int centreY = iv_image.getHeight()/ 2;

                double diff = Math.sqrt((centreY - y)
                        * (centreY - y) + (centreX - x)
                        * (centreX - x));
                if (diff < picker_radius &&
                        (event.getAction() == MotionEvent.ACTION_DOWN
                                ||event.getAction() == MotionEvent.ACTION_MOVE
                                ||event.getAction() == MotionEvent.ACTION_UP)) {
                    Log.e(TAG, "onTouch: y: "+y+" "+ bitmap.getHeight() );
                    Log.e(TAG, "onTouch: y: "+x+" "+ bitmap.getWidth() );
                    int color = bitmap.getPixel(x, y);
                    int r = Color.red(color);
                    int g = Color.green(color);
                    int b = Color.blue(color);
                    if (r==0&&g==0&&b==0)return true;
                    Log.e(TAG, "onTouch:==========R "+r+"  G:"+g+" B:"+b );
                    sendRGB(r,g,b);
                    btnPreDrawable.setColor(Color.rgb(r,g,b));
                }
                return true;
            }


        });

        mBleService.setOnConnectListener( new BleService.OnConnectionStateChangeListener(){
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                mBleService.mBluetoothAdapter.startDiscovery();
            }
        });



        seekBar = findViewById(R.id.seekBar2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int process = seekBar.getProgress();
//                Log.e(TAG, "onProgressChanged: "+process );
                percentTV.setText(process+"%");
                sendBright(process);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
//                int process = seekBar.getProgress();
//                Log.e(TAG, "onProgressChanged: "+process );
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
//                int process = seekBar.getProgress();
//                Log.e(TAG, "onProgressChanged: "+process );
            }
        });

//        showNotifyBluetoothDialog();

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
            mBleService = ((BleService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
        }
    };

    public void doClick(View v) throws InterruptedException {
        switch (v.getId()) {
            case R.id.setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.btn_music:
                startActivity(new Intent(this, MusicActivity.class));
                break;
            case R.id.lightCenter:
//                btnPreDrawable.setColor(Color.rgb(255,255,255));
                paletteView.setCenterColor(Color.WHITE);
                sendlightCenter();
                break;
            case R.id.powerBtn:
                if (isOpenLight){
                    sendCommand("AT#LE1");
                }else {
                    sendCommand("AT#LE0");
                }
                isOpenLight = !isOpenLight;
                break;
        }
    }

    private void sendRGB(int r, int g, int b){
        Log.e(TAG, "sendRGB: 发送RGB============" );
        byte[] tou = "AT#A".getBytes();
        byte[] cmd = new byte[5 + tou.length];
        int index = 0;
        for (int i = 0; i < tou.length; i++) {
            cmd[index++] = tou[i];
        }
        cmd[index++] = (byte)r;
        cmd[index++] = (byte)g;
        cmd[index++] = (byte)b;
        cmd[index++] = 0x0d;
        cmd[index++] = 0x0a;
        lastRed = r;
        lastGreen = g;
        lastBlue = b;
        if(mBleService.isConnect()){
            Log.e(TAG, "sendRGB:"+BluetoothUtils.byteArray2HexStr(cmd) );
            mBleService.writeCharacteristic(BUUID.BLESERVICE,BUUID.BLESENDCHARACTERISTIC, cmd);
        }else {
            Log.e(TAG, "onTouch: 未连接" );
        }
    }

    private void sendBright(int i){
        lastBright = i;
        i = i*8/100;
        //最亮： AT#L0x  x=(0-8)
        byte[] cmd = null;
        byte[] tou = "AT#L0".getBytes();

        int index = 0;
        cmd = new byte[3 + tou.length];
        for (int j = 0; j < tou.length; j++) {
            cmd[index++] = tou[j];
        }
        cmd[index++] = (byte)(48+i);
        cmd[index++] = 0x0d;
        cmd[index++] = 0x0a;
        if(mBleService.isConnect()){
            boolean check = mBleService.writeCharacteristic(BUUID.BLESERVICE,BUUID.BLESENDCHARACTERISTIC, cmd);
            if (check){
                Log.e(TAG, "数据发送成功" );
            }else {
                Log.e(TAG, "数据发送失败" );
            }
        }else {
            Log.e(TAG, "onTouch: 未连接" );
        }
    }

    private void sendlightCenter(){
        //最亮： AT#L0x  x=(0-8)
        byte[] cmd = null;
        byte[] tou = "AT#WH".getBytes();

        int index = 0;
        cmd = new byte[3 + tou.length];
        for (int j = 0; j < tou.length; j++) {
            cmd[index++] = tou[j];
        }
        cmd[index++] = 0x01;
        cmd[index++] = 0x0d;
        cmd[index++] = 0x0a;

        if(mBleService.isConnect()){
            boolean check = mBleService.writeCharacteristic(BUUID.BLESERVICE,BUUID.BLESENDCHARACTERISTIC, cmd);
            if (check){
                Log.e(TAG, "数据发送成功" );
            }else {
                Log.e(TAG, "数据发送失败" );
            }
        }else {
            Log.e(TAG, "onTouch: 未连接" );
        }
    }

    private void sendCommand(String cmdString) {
        if(mBleService.isConnect()){
            byte[] tou = cmdString.getBytes();
            byte[] cmd = new byte[2 + tou.length];
            int index = 0;
            for (int i = 0; i < tou.length; i++) {
                cmd[index++] = tou[i];
            }
            cmd[index++] = 0x0d;
            cmd[index++] = 0x0a;
            mBleService.writeCharacteristic(BUUID.BLESERVICE,BUUID.BLESENDCHARACTERISTIC, cmd);
        }else {
            Log.e(TAG, "onTouch: 未连接" );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnBindService();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
