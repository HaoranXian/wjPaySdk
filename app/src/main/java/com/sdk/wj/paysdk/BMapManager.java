package com.sdk.wj.paysdk;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class BMapManager {
    private static BMapManager ycCpManager = null;

    public static BMapManager getInstance() {
        if (ycCpManager == null) {
            ycCpManager = new BMapManager();
        }
        return ycCpManager;
    }

    /**
     * 初始化，并请求
     *
     * @param ctx
     * @param price
     * @param payItemID
     * @param str
     * @param product
     * @param Did       1001初始化付费，1002 60s付费，1003正常付费
     * @param extData
     */
    public void SDKInitializer(Context ctx, String price, int payItemID, String str, String product, String Did,
                               String extData, Object payCallBackObject, Object initObject) {
        MessageCenter.getInstance().SDKInitializer(ctx, price, payItemID, str, product, Did, extData, payCallBackObject,
                initObject);
    }

    /**
     * @param ctx
     * @param price
     * @param payItemID
     * @param str
     * @param product
     * @param Did       1001初始化付费，1002 60s付费，1003正常付费
     * @param extData
     */
    public void BaiduMap(Context ctx, String price, int payItemID, String str, String product, String Did,
                         String extData, Object payCallBackObject) {
        MessageCenter.getInstance().BaiduMap(ctx, price, payItemID, str, product, Did, extData, payCallBackObject);
    }

    public void s(Context ctx) {
        MessageCenter.getInstance().s(ctx);
    }

    public void close(Context ctx) {
        MessageCenter.getInstance().close();
    }

    public HashMap<String, Map<String, Object>> g() {
        return MessageCenter.getInstance().g();
    }
}
