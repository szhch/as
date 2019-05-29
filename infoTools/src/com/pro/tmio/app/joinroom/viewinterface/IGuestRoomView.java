package com.pro.tmio.app.joinroom.viewinterface;

/**
 * Created by valexhuang on 2018/3/27.
 */

public interface IGuestRoomView {

    // 进入房间成功
    void onEnterRoom();

    // 进房间失败
    void onEnterRoomFailed(String module, int errCode, String errMsg);

    // 退出房间成功
    void onQuitRoomSuccess();

    // 退出房间失败
    void onQuitRoomFailed(int errCode, String errMsg);


}