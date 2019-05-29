package com.pro.tmio.app.joinroom.presenter;

import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.view.AVRootView;
import com.pro.tmio.app.joinroom.viewinterface.IGuestRoomView;


/**
 * Created by valexhuang on 2018/3/27.
 */

public class RoomHelper implements ILiveRoomOption.onExceptionListener, ILiveRoomOption.onRoomDisconnectListener {
    private IGuestRoomView roomView;


    public RoomHelper(IGuestRoomView view) {
        roomView = view;
    }

    // 设置渲染控件
    public void setRootView(AVRootView avRootView) {
        ILiveRoomManager.getInstance().initAvRootView(avRootView);
    }


    // 对应GuestRoomActivity中的加入房间
    public int joinRoom(int roomId) {
        ILiveRoomOption option = new ILiveRoomOption()
                .imsupport(false)       // 不需要IM功能
                .exceptionListener(this)  // 监听异常事件处理
                .roomDisconnectListener(this)   // 监听房间中断事件
                .controlRole("Guest")  // 使用Guest角色
                .autoCamera(false)       // 进房间后不需要打开摄像头
                .autoMic(false);         // 进房间后不需打开Mic

        return ILiveRoomManager.getInstance().joinRoom(roomId, option, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                roomView.onEnterRoom();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                roomView.onEnterRoomFailed(module, errCode, errMsg);
            }
        });
    }


    // 控制摄像头
    public int enableCamera(int cameraId, boolean enable) {
        return ILiveRoomManager.getInstance().enableCamera(cameraId, enable);
    }

    // 控制麦克风
    public int enableMic(boolean enable) {
        return ILiveRoomManager.getInstance().enableMic(enable);
    }


    // 退出房间
    public int quitRoom() {
        return ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                roomView.onQuitRoomSuccess();


            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                roomView.onQuitRoomFailed(errCode, errMsg);
            }
        });
    }


    @Override
    public void onException(int exceptionId, int errCode, String errMsg) {
        System.out.println(errMsg);
        //处理异常事件
    }


    @Override
    public void onRoomDisconnect(int errCode, String errMsg) {
        // 处理房间中断(一般为断网或长时间无长行后台回收房间)
    }


    // 处理Activity事件
    public void onPause() {
        ILiveRoomManager.getInstance().onPause();
    }

    public void onResume() {
        ILiveRoomManager.getInstance().onResume();
    }

}