package com.sdk.wj.paysdk.pay;

import android.os.Handler;

import com.sdk.wj.paysdk.utils.Constants;

/**
 * Created by Administrator on 2017/8/23.
 */

public class PayCallBack {
    private static PayCallBack payCallBack = null;

    public static PayCallBack getInstance() {
        if (payCallBack == null) {
            payCallBack = new PayCallBack();
        }
        return payCallBack;
    }

    public void paySuccess(Object receiver) {
        Handler mHandler = (Handler) receiver;
        mHandler.sendEmptyMessage(Constants.PayState_SUCCESS);
    }

    public void payFail(Object receiver) {
        Handler mHandler = (Handler) receiver;
        mHandler.sendEmptyMessage(Constants.PayState_FAILURE);
    }

    public void payCancel(Object receiver) {
        Handler mHandler = (Handler) receiver;
        mHandler.sendEmptyMessage(Constants.PayState_CANCEL);
    }
}
