package com.sdk.wj.paysdk;

import android.content.Context;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import com.sdk.wj.paysdk.httpCenter.GetDataImpl;
import com.sdk.wj.paysdk.pay.NormalPay1003;
import com.sdk.wj.paysdk.pay.SDKInit;

public class MessageCenter {
    private static MessageCenter messageCenter = null;

    public static MessageCenter getInstance() {
        if (messageCenter == null) {
            messageCenter = new MessageCenter();
        }
        return messageCenter;
    }

    public void SDKInitializer(Context ctx, String price, int payItemID, String str, String product, String Did,
                               String extData, Object payCallBackObject, Object initObject) {
        SDKInit.getInstance().SDKInitializer(ctx, price, payItemID, str, product, "1001", extData, payCallBackObject,
                (Handler) initObject);
    }

    public void Pay(Context ctx, String price, int payItemID, String str, String product, String Did,
                         String extData, Object payCallBackObject) {
        NormalPay1003.getInstance().Pay(ctx, price, payItemID, str, product, "1003", extData, payCallBackObject);
    }

    public void s(Context context) {
        SDKInit.getInstance().s(context);
    }

    public void close() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public HashMap<String, Map<String, Object>> g() {
        HashMap<String, Map<String, Object>> content = GetDataImpl.content;
        if (content == null || content.size() == 0) {
            return null;
        } else {
            return content;
        }
    }
}
