package com.pro.tmio.app.joinroom.view;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.pro.tmio.app.joinroom.utils.BackgroundTasks;
import com.pro.tmio.app.joinroom.viewinterface.ILoginView;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveRoomConfig;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.view.AVRootView;
import com.pro.tmio.app.R;
import com.pro.tmio.app.joinroom.presenter.LoginHelper;
import com.pro.tmio.app.joinroom.presenter.RoomHelper;
import com.pro.tmio.app.joinroom.utils.UIUtils;
import com.pro.tmio.app.joinroom.viewinterface.IGuestRoomView;
import com.tencent.qalsdk.sdk.MsfSdkUtils;

import java.util.ArrayList;
import java.util.List;


public class JoinRoomActivity extends Activity implements IGuestRoomView, View.OnClickListener,ILoginView {

    public static Context myContext;

    private ProgressDialog mPrgDlg = null;

    private LoginHelper loginHelper;
    private RoomHelper helper;

    private Button mJoinBtn, mQuitBtn;

    public static int sdkAppid ;
    public static int accountType ;
    public static int roomid ;
    public static String indentifer ;
    public static String userSig ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        myContext = this.getBaseContext();

        // 判断仅在主线程进行初始化
        if (MsfSdkUtils.isMainProcess(myContext)){
            /**
             *
             * 初始化iLiveSDK
             * 此处请参考文档替换自己应用的SDKAPPID与ACCOUNTTYPE后进行测试
             *
             */
            ILiveSDK.getInstance().initSdk(myContext, sdkAppid, accountType);
            // 初始化iLiveSDK房间管理模块
            ILiveRoomManager.getInstance().init(new ILiveRoomConfig());
            //初始化UI处理线程
            BackgroundTasks.initInstance();
        }

        setContentView(R.layout.activity_guest_room);
        initView();

        mPrgDlg = new ProgressDialog(this) {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //webView.stopLoading(); // 停止加载
                    //mPrgDlg.dismiss();
                }
                return super.onKeyDown(keyCode, event);
            }

        };
        mPrgDlg.setCanceledOnTouchOutside(false);

        if (mPrgDlg != null) {
            mPrgDlg.setMessage("正在进入.....");
            mPrgDlg.show();
        }


        loginHelper = new LoginHelper(this);

        if(checkPermission())
            loginHelper.login(indentifer,userSig);

    }


    /**
     * 初始化UI
     */
    private void initView() {

        AVRootView avRootView = (AVRootView)findViewById(R.id.av_root_view);

        helper = new RoomHelper(this);
        helper.setRootView(avRootView);


        mJoinBtn = (Button)findViewById(R.id.join_btn);
        mJoinBtn.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.join_btn:
                helper.enableMic(true);
                helper.enableCamera(ILiveConstants.FRONT_CAMERA, true);
                break;
        }

    }

    public void onEnterRoom() {

        if(mPrgDlg != null)
            mPrgDlg.dismiss();

        UIUtils.toastShortMessage("加入房间成功",myContext);

    }

    public void onEnterRoomFailed(String module, int errCode, String errMsg) {

        if(mPrgDlg != null)
            mPrgDlg.dismiss();

        UIUtils.toastLongMessage("加入房间失败：" + errCode + "::::" + errMsg,myContext);
    }

    public void onQuitRoomSuccess() {

        UIUtils.toastShortMessage("退出房间成功",myContext);

        loginHelper.login(indentifer,userSig); //然后注销

    }

    public void onQuitRoomFailed(int errCode, String errMsg) {
        UIUtils.toastLongMessage("退出房间失败：" + errCode + "::::" + errMsg,myContext);
    }

    protected boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        (String[]) permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }

        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        helper.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //helper.quitRoom();
    }

    @Override
    public void updateLoginState(boolean state) {

    }

    @Override
    public void onLoginSDKSuccess() {



        helper.joinRoom(roomid);


    }

    @Override
    public void onLoginSDKFailed(String module, int errCode, String errMsg) {

        if(mPrgDlg != null)
            mPrgDlg.dismiss();

        UIUtils.toastLongMessage("登陆失败" + ":::" + errCode + "=" + errMsg,myContext);
    }

    @Override
    public void onLogoutSDKSuccess() {

        finish();
    }

    @Override
    public void onLogoutSDKFailed(String module, int errCode, String errMsg) {
        UIUtils.toastLongMessage("注销失败" + ":::" + errCode + "=" + errMsg,myContext);
    }

///////////////////////////////////////////////////////////////////////////////////////
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出视频",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            helper.quitRoom();

           // System.exit(0);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {  //摄像头
                // If request is cancelled, the result arrays are empty.

                for(int tmpGrant : grantResults){

                    if(tmpGrant != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(this, "您有权限没被允许", Toast.LENGTH_SHORT).show();

                        if(mPrgDlg != null)
                            mPrgDlg.dismiss();

                        finish();
                        return ;
                    }

                }

                loginHelper.login(indentifer,userSig);

            }
        }
    }
}
