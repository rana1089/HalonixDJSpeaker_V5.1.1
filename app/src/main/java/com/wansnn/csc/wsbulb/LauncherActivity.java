package com.wansnn.csc.wsbulb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.wansnn.csc.wsbulb.jl_dialog.Jl_Dialog;
import com.wansnn.csc.wsbulb.jl_dialog.interfaces.OnViewClickListener;

public class LauncherActivity extends AppCompatActivity {

    static final private String TAG = "LauncherActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private PermissionManager mPermissionManager;
    private String[] mPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,

    };
    private Jl_Dialog notifyDialog;
    private Jl_Dialog notifyGpsDialog;
    private int CHECK_GPS_CODE = 8119;
    private final static int LAUNCHER_TIME = 30;

    private static final int MSG_TO_MAIN_ACTIVITY = 0x2222;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message != null) {
                switch (message.what) {
                    case MSG_TO_MAIN_ACTIVITY:
//                        if (JL_MediaPlayerServiceManager.getInstance().getJl_mediaPlayer() == null) {
//                            handler.sendEmptyMessageDelayed(MSG_TO_MAIN_ACTIVITY, 1000);
//                        } else {
                        startActivity(new Intent(LauncherActivity.this, ScanActivity.class));
                        finish();
//                        }
                        break;
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.activity_launcher);



        mPermissionManager = new PermissionManager(mPermissions, this);
        mPermissionManager.setOnPermissionStateCallback(new PermissionManager.OnPermissionStateCallback() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "onSuccess ");
                LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locManager != null && !locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showNotifyGPSDialog();
                    return;
                }
                //todo 授权成功处理初始化

                //  int type = PreferencesHelper.getSharedPreferences(getApplicationContext()).getInt(Constant.KEY_PRODUCT_TYPE, -1);
                MyApplication.getApplication().init();

                handler.sendEmptyMessageDelayed(MSG_TO_MAIN_ACTIVITY, LAUNCHER_TIME);

            }

            @Override
            public void onFailed(String permission, Intent intent) {
                Log.e(TAG, "showToPermissionSettingDialog ");
                showToPermissionSettingDialog(permission, intent);
            }
        });
    }

    /**
     * 显示打开定位服务(gps)提示框
     */
    private void showNotifyGPSDialog() {
        if (notifyGpsDialog == null) {
            notifyGpsDialog = Jl_Dialog.builder()

                    .title(getString(R.string.open_gpg_tip))
//                    .content()
                    .left(getString(R.string.exit))
                    .right(getString(R.string.ok))
                    .titleColor(0xFF777777)
                    .backgroundColor(Color.WHITE)
                    .leftClickListener(new OnViewClickListener() {
                        @Override
                        public void onClick(View v, DialogFragment dialogFragment) {
                            dismissNotifyGPSDialog();
                            finish();
                            System.exit(0);
                        }
                    })
                    .rightClickListener(new OnViewClickListener() {
                        @Override
                        public void onClick(View v, DialogFragment dialogFragment) {
                            dismissNotifyGPSDialog();
                            displayLocationSettingsRequest(LauncherActivity.this);
                           // startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), CHECK_GPS_CODE);
                        }
                    })
                    .build();
        }
        if (!notifyGpsDialog.isShow()) {
            notifyGpsDialog.show(getSupportFragmentManager(), "notify_gps_dialog");
        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(LauncherActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    private void dismissNotifyGPSDialog() {
        if (notifyGpsDialog != null) {
            if (notifyGpsDialog.isShow() && !isDestroyed()) {
                notifyGpsDialog.dismiss();
            }
            notifyGpsDialog = null;
        }
    }

    private void showToPermissionSettingDialog(final String permission, final Intent intent) {
        StringBuilder sb = new StringBuilder();
        sb.append("为了能正常的使用软件，请");

        if (permission.equals(Manifest.permission.READ_CONTACTS)) {
            sb.append("允许读写联系人");
        } else if (permission.equals(Manifest.permission.WRITE_CONTACTS)) {
            sb.append("允许读写联系人");
        } else if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
            sb.append("允许使用麦克风");
        } else if (permission.equals(Manifest.permission.READ_PHONE_STATE)) {
            sb.append("允许读取手机信息");
        } else if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            sb.append("允许获取定位信息");
        } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            sb.append("允许获取定位信息");
        } else if (permission.equals(Manifest.permission.BLUETOOTH)) {
            sb.append("允许使用蓝牙");
        } else if (permission.equals(Manifest.permission.BLUETOOTH_ADMIN)) {
            sb.append("允许使用蓝牙");
        } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            sb.append("允许文件读写");
        } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            sb.append("允许文件读写");
        } else if (permission.equals(Manifest.permission.WRITE_SETTINGS)) {
            sb.append("允许使用修改手机设置");
        } else {
            return;
        }
        sb.append("操作路径为：");
        sb.append("设置>应用管理>" + getString(R.string.app_name) + ">权限");
        //String content = String.format(sb.toString(), permission);
        if (notifyDialog == null) {
            notifyDialog = Jl_Dialog.builder()
                    .title(getString(R.string.tips))
                    .content(sb.toString())
                    .left(getString(R.string.cancel))
                    .right(getString(R.string.to_setting))
                    .leftClickListener(new OnViewClickListener() {
                        @Override
                        public void onClick(View v, DialogFragment dialogFragment) {
                            finish();
                        }
                    })
                    .rightClickListener(new OnViewClickListener() {
                        @Override
                        public void onClick(View v, DialogFragment dialogFragment) {
                            startActivity(intent);
                            if (notifyDialog != null) {
                                notifyDialog.dismiss();
                            }
                        }
                    })
                    .build();
        } else {
            Log.e(TAG, sb.toString());
            if (notifyDialog.getBuilder() != null) {
                notifyDialog.getBuilder().content(sb.toString());
            }
        }
        if (!notifyDialog.isShow()) {
            Log.e(TAG, sb.toString());
            notifyDialog.show(getSupportFragmentManager(), "request_permission");
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (notifyDialog != null && notifyDialog.isShow()) {
            return;
        }
        mPermissionManager.onResume();
    }

    private void dismissNotifyDialog() {
        if (notifyDialog != null) {
            if (notifyDialog.isShow() && !isDestroyed()) {
                notifyDialog.dismiss();
            }
            notifyDialog = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("onStop", "onStop 1");
        if (notifyDialog != null && notifyDialog.isShow()) {
            notifyDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {

        dismissNotifyDialog();
        notifyDialog = null;
        super.onDestroy();
    }
}