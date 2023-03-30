package com.wansnn.csc.wsbulb.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wansnn.csc.wsbulb.BluetoothUtils;

/**
 * Created by csc on 2017/11/10.
 */

public class myBLEDataSenderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("myBLEDataSenderReceiver", "onReceive: 收到数据发送通知"+ BluetoothUtils.byteArray2HexStr(intent.getByteArrayExtra("bleData")));
    }
}
