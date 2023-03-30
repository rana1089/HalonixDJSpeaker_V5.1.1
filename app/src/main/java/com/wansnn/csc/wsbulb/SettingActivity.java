package com.wansnn.csc.wsbulb;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.wansnn.csc.wsbulb.BlueTooth.BUUID;
import com.wansnn.csc.wsbulb.BlueTooth.BleService;

/**
 * Created by csc on 2017/12/6.
 */

public class SettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    static String TAG = "SettingActivity";
    private BleService mBleService;
    private Switch mSwitch1;
    private Switch mSwitch2;
    private Switch mSwitch3;
    private Switch mSwitch4;
    private Switch mSwitch5;
    static int lastSelect;
    private static boolean clearing = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mBleService = BleService.getInstance();
        mSwitch1 = findViewById(R.id.switch1);
        mSwitch2 = findViewById(R.id.switch2);
        mSwitch3 = findViewById(R.id.switch3);
        mSwitch4 = findViewById(R.id.switch4);
        mSwitch5 = findViewById(R.id.switch5);
        setSwitchChangeListener();

    }

    private void setSwitchChangeListener() {
        mSwitch1.setOnCheckedChangeListener(this);
        mSwitch2.setOnCheckedChangeListener(this);
        mSwitch3.setOnCheckedChangeListener(this);
        mSwitch4.setOnCheckedChangeListener(this);
        mSwitch5.setOnCheckedChangeListener(this);

    }


    private void sendScene(int i, int isOn) {
        //最亮： AT#L0x  x=(0-8)
        byte[] cmd = null;
        byte[] tou = "AT#CB".getBytes();

        int index = 0;
        cmd = new byte[4 + tou.length];
        for (int j = 0; j < tou.length; j++) {
            cmd[index++] = tou[j];
        }
        cmd[index++] = (byte) i;
        cmd[index++] = (byte) (isOn);
        cmd[index++] = 0x0d;
        cmd[index++] = 0x0a;

        if (mBleService.isConnect()) {
            mBleService.writeCharacteristic(BUUID.BLESERVICE, BUUID.BLESENDCHARACTERISTIC, cmd);
        } else {
            Log.e(TAG, "onTouch: 未连接");
        }
    }


    public void settingOnClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                this.finish();
                break;
//            case R.id.switch1:
//                sendScene(1,mSwitch1.isChecked()?1:0);
//                clear(1);
//                break;
//            case R.id.switch2:
//                sendScene(2,mSwitch2.isChecked()?1:0);
//                clear(2);
//                break;
//            case R.id.switch3:
//                sendScene(3,mSwitch3.isChecked()?1:0);
//                clear(3);
//                break;
//            case R.id.switch4:
//                sendScene(4,mSwitch4.isChecked()?1:0);
//                clear(4);
//                break;
//            case R.id.switch5:
//                sendScene(5,mSwitch5.isChecked()?1:0);
//                clear(5);
//                break;
        }
    }

    private void clear(int i) {
        Log.e(TAG, "clear: " + i + "  last:" + lastSelect);
        if (lastSelect < 1) {
            lastSelect = i;
        } else if (lastSelect == i) {

        } else {
            switch (lastSelect) {
                case 1:
                    clearing = true;
                    mSwitch1.setChecked(false);
                    break;
                case 2:
                    clearing = true;
                    mSwitch2.setChecked(false);
                    break;
                case 3:
                    clearing = true;
                    mSwitch3.setChecked(false);
                    break;
                case 4:
                    clearing = true;
                    mSwitch4.setChecked(false);
                    break;
                case 5:
                    clearing = true;
                    mSwitch5.setChecked(false);
                    break;
            }
            lastSelect = i;
        }


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!clearing) {
            switch (buttonView.getId()) {
                case R.id.switch1:
                    Log.e(TAG, "onCheckedChanged: s1 " + (mSwitch1.isChecked() ? 1 : 0));
                    sendScene(1, mSwitch1.isChecked() ? 1 : 0);
                    clear(1);
                    break;
                case R.id.switch2:
                    sendScene(2, mSwitch2.isChecked() ? 1 : 0);
                    Log.e(TAG, "onCheckedChanged: s2 " + (mSwitch2.isChecked() ? 1 : 0));
                    clear(2);
                    break;
                case R.id.switch3:
                    sendScene(3, mSwitch3.isChecked() ? 1 : 0);
                    Log.e(TAG, "onCheckedChanged: s3 " + (mSwitch3.isChecked() ? 1 : 0));
                    clear(3);
                    break;
                case R.id.switch4:
                    sendScene(4, mSwitch4.isChecked() ? 1 : 0);
                    Log.e(TAG, "onCheckedChanged: s4 " + (mSwitch4.isChecked() ? 1 : 0));
                    clear(4);
                    break;
                case R.id.switch5:
                    sendScene(5, mSwitch5.isChecked() ? 1 : 0);
                    Log.e(TAG, "onCheckedChanged: s5 " + (mSwitch5.isChecked() ? 1 : 0));
                    clear(5);
                    break;
            }
            clearing = false;
        }
    }
}
