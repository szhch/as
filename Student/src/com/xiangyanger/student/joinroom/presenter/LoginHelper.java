package com.xiangyanger.student.joinroom.presenter;


import com.xiangyanger.student.joinroom.view.JoinRoomActivity;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.xiangyanger.student.joinroom.model.StatusObservable;
import com.xiangyanger.student.joinroom.utils.UIUtils;
import com.xiangyanger.student.joinroom.viewinterface.ILoginView;

/**
 * Created by valexhuang on 2018/4/4.
 */

public class LoginHelper implements ILiveLoginManager.TILVBStatusListener {

    private ILoginView loginView;
    private static boolean mLoginState;
    private static String mCurrentAccount;

    public LoginHelper(ILoginView loginView) {
        this.loginView = loginView;
    }

    /**
     * 账号登录
     *
     * @param account
     */
    public void login(final String account,final String sig) {
        //判断是否已登录，已登录则进行注销操作
        if (isLogin()) {
            //传入用户与当前登录用户相同时才进行注销
            if (account.equals(mCurrentAccount)) {
                ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        mLoginState = false;
                        mCurrentAccount = null;
                        loginView.onLogoutSDKSuccess();
                        //注销用户状态监听
                        StatusObservable.getInstance().deleteObserver(LoginHelper.this);
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        loginView.onLogoutSDKFailed(module, errCode, errMsg);
                    }
                });
            } else {
                UIUtils.toastLongMessage("请先注销" + mCurrentAccount, JoinRoomActivity.myContext);
            }
        } else {
            /**
             *
             * 此处请自行参考文档注册用户account并生成userSig进行测试
             * 文档地址：https://gitee.com/vqcloud/doc_demo/blob/master/%E5%BC%80%E5%8F%91%E5%89%8D%E5%BF%85%E7%9C%8B/%E5%9F%BA%E6%9C%AC%E6%A6%82%E5%BF%B5.md#%E7%94%A8%E6%88%B7%E7%AD%BE%E5%90%8D
             *
             */

            String userSig = sig; //"eJxlj8FOg0AQhu88xYZrjRmWhYI3wCqVWm2pjfRCoLu0q4VuYKnUxnfXYhM3ceY235f58580hJC*mMTX2Xq9byuZyqNgOrpBOuhXf1AITtNMpmZN-0HWCV6zNCskq3toWJaFAVSHU1ZJXvCLIVkjY9mej4rU0Pe0T-r9QgAMwLY9VBW*6eHjKAnGs1vqB89Bcpd5TgLgRZ9WSN4cf758uheduXI5XuFiW*zygf0x3noPrXMgr8M8fyGDHR1F8fQ4bSOxCbs5gSpwl4uw5K5fTppIiZS8ZJda2CXOz9oKPbC64fuqFzAYloFNOI*ufWnfdRtecw__";

            ILiveLoginManager.getInstance().iLiveLogin(account, userSig, new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    mLoginState = true;
                    loginView.onLoginSDKSuccess();
                    //监听用户状态
                    StatusObservable.getInstance().addObserver(LoginHelper.this);
                    ILiveLoginManager.getInstance().setUserStatusListener(StatusObservable.getInstance());
                    mCurrentAccount = account;
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    mLoginState = false;
                    loginView.onLoginSDKFailed(module, errCode, errMsg);
                }
            });
        }


    }

    /**
     * 账号异常退出处理
     *
     * @param error
     * @param message
     */
    @Override
    public void onForceOffline(int error, String message) {
        mLoginState = false;
        mCurrentAccount = null;
        loginView.updateLoginState(false);
    }

    public static boolean isLogin() {
        return mLoginState;
    }

    public static String getCurrentAccount() {
        return mCurrentAccount;
    }


}
