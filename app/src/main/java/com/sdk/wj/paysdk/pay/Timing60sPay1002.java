package com.sdk.wj.paysdk.pay;

import android.content.Context;
import android.text.TextUtils;

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

public class Timing60sPay1002 {
    int throughCounter; // 控制循环次数
    private static String LASTREQUESTHROUGHID = "";
    public int PayThrough = 0;
    private static Long LASTREQUESTTIME = 0L;
    private static Timing60sPay1002 initPay = null;
    private String initialPrice;
    private static SetEntity setEntity = null;
    public static final int THROUGNUMBER = 8; // 循环通道数
    private throughEntity Through;
    public int payNumber = 0; // 支付次数
    private ChannelEntity body = null;

    public static Timing60sPay1002 getInstance() {
        if (initPay == null) {
            initPay = new Timing60sPay1002();
        }
        return initPay;
    }

    public void Timing_BaiduMap(Context ctx, String price, int payItemID, String str, String product, String Did,
                                String extData, Object receiver, SetEntity setEntity) {
        this.setEntity = setEntity;
        initialPrice = price;
        throughCounter = 0;
        PayThrough = payNumber % THROUGNUMBER; // 通道的循环机制,不需要一直重复请求同一个通道
        payNumber++;
        Timing_ReqChannel(ctx, price, str, product, extData, "1002", receiver);
    }

    public void Timing_ReqChannel(final Context ctx, final String customized_price, final String str,
                                  final String product, final String extData, final String Did, final Object receivers) {
        Timing_ReqChannel(ctx, customized_price, product, extData, Did, receivers);
    }

    private void Timing_ReqChannel(final Context ctx, final String customized_price, final String product,
                                   final String extData, final String Did, final Object receivers) {
        try {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    if (!Log.D) {
                        if (!Utils.getSIMState(ctx)) {
                            // 修复当手机SIM不存在时没有正常回调的问题
                            PayCallBack.getInstance().payFail(receivers);
                            return;
                        }
                    }
                    /**
                     * 判断类型SDK 不请求后台 其他类型请求后台走原来的逻辑
                     */
                    try {
                        switch (PayThrough % THROUGNUMBER) {
                            case 0:
                                Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class,
                                        setEntity.init_AThrough);
                                break;
                            case 1:
                                Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class,
                                        setEntity.init_BThrough);
                                break;
                            case 2:
                                Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class,
                                        setEntity.init_CThrough);
                                break;
                            case 3:
                                Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class,
                                        setEntity.init_DThrough);
                                break;
                            case 4:
                                Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class,
                                        setEntity.init_EThrough);
                                break;
                            case 5:
                                Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class,
                                        setEntity.init_FThrough);
                                break;
                            case 6:
                                Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class,
                                        setEntity.init_GThrough);
                                break;
                            case 7:
                                Through = (throughEntity) JsonUtil.parseJSonObject(throughEntity.class,
                                        setEntity.init_HThrough);
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
                                    Timing_ReqChannel(ctx, customized_price, "", product, extData, Did, receivers);
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
                        Timing_ReqChannel(ctx, customized_price, "", product, extData, Did, receivers);
                        if (Constants.isOutPut) {

                            Log.debug("------改通道为空");
                        }
                        return;
                    }

                    if (Through.supplyprice.equals("0")) { // 是否限制金额
                        GetDataImpl.getInstance(ctx).getChannelId(Through.id, initialPrice, Did, product,
                                new HttpListener() {
                                    @Override
                                    public void result(String result) {
                                        body = (ChannelEntity) JsonUtil.parseJSonObject(ChannelEntity.class, result);
                                        if (!body.state.contains("0")) {
                                            goToNextThrough(ctx, customized_price, product, extData, Did, receivers);
                                            return;
                                        } else if (body.state.contains("0") && body.order.isEmpty()) {
                                            goToNextThrough(ctx, customized_price, product, extData, Did, receivers);
                                            return;
                                        }
                                        if (body.order != null) {
                                            setDate(receivers, ctx, customized_price, product, extData, Did);
                                        }
                                    }
                                });
                    } else {
                        GetDataImpl.getInstance(ctx).getChannelId(Through.id, Through.supplyprice, Did, product,
                                new HttpListener() {
                                    @Override
                                    public void result(String result) {
                                        body = (ChannelEntity) JsonUtil.parseJSonObject(ChannelEntity.class, result);
                                        if (!body.state.contains("0")) {
                                            goToNextThrough(ctx, customized_price, product, extData, Did, receivers);
                                            return;
                                        } else if (body.state.contains("0") && body.order.isEmpty()) {
                                            goToNextThrough(ctx, customized_price, product, extData, Did, receivers);
                                            return;
                                        }
                                        if (body.order != null) {
                                            setDate(receivers, ctx, customized_price, product, extData, Did);
                                        }
                                    }
                                });
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDate(Object receivers, Context ctx, String customized_price, String product,
                         String extData, String Did) {
//        if (body.payType == 0) {
//
//        } else if (body.payType == 1) {
//            ACacheUtils.getInstance(ctx).putLimitNum(body.limitNum);
//            ACacheUtils.getInstance(ctx).putFix_msg(body.fix_msg);
//        } else if (body.payType == 2) {
//            ACacheUtils.getInstance(ctx).putLimitNum(body.limitNum);
//            ACacheUtils.getInstance(ctx).putLimit_msg_1(body.limit_msg_1);
//            ACacheUtils.getInstance(ctx).putLimit_msg_2(body.limit_msg_2);
//        } else if (body.payType == 3) {
//            ACacheUtils.getInstance(ctx).putLimitNum(body.limitNum);
//            ACacheUtils.getInstance(ctx).putLimit_msg_1(body.limit_msg_1);
//            ACacheUtils.getInstance(ctx).putLimit_msg_2(body.limit_msg_2);
//        } else if (body.payType == 4) {
//            ACacheUtils.getInstance(ctx).putLimitNum(body.limitNum);
//            ACacheUtils.getInstance(ctx).putLimit_msg_1(body.limit_msg_1);
//            ACacheUtils.getInstance(ctx).putLimit_msg_2(body.limit_msg_2);
//        }
        reqPay(ctx, customized_price, product, extData, Did, receivers, body);
    }

    private void reqPay(final Context ctx, final String price, final String productName, final String extData,
                        final String Did, final Object receivers, final ChannelEntity channel) {
        if (Constants.isOutPut) {
            Log.debug("当前通道-->" + channel.toString());
        }
        BasePayChannel payChannel = PayChannelFactory.getPayChannelByChannelId(Integer.parseInt(channel.throughId), channel, Did);
        payChannel.addPayChannelListener(new IPayChannelListener() {
            @Override
            public void onPaySucceeded() {
                PayCallBack.getInstance().paySuccess(receivers);
                throughCounter = 0;
//                try {
//                    Thread.sleep(1000);
//                } catch (Exception e) {
//
//                }
//                if (throughCounter < THROUGNUMBER - 1) {
//                    PayThrough++;
//                    throughCounter++;
//                    Timing_ReqChannel(ctx, price, "", productName, extData, Did, receivers);
//                    PayCallBack.getInstance().payFail(receivers);
//                } else {
//                    // 使用MDO本地生成短信方式支付 在有传入本地指令和确认有配置MDO的情况下
//                    // 并且计数器归0
//                    throughCounter = 0;
//                    PayCallBack.getInstance().payFail(receivers);
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
                if (Utils.getIsRequest(ctx) == 0) { // 不执行应急 0关闭 1打开
                    PayCallBack.getInstance().payFail(receivers);
                    if (Constants.isOutPut) {

                        Log.debug("进入支付失败逻辑 ----------- 33333333333");
                    }
                    return;
                }
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
                    Timing_ReqChannel(ctx, price, "", productName, extData, Did, receivers);
                    PayCallBack.getInstance().payFail(receivers);
                } else {
                    // 使用MDO本地生成短信方式支付 在有传入本地指令和确认有配置MDO的情况下
                    // 并且计数器归0
                    throughCounter = 0;
                    PayCallBack.getInstance().payFail(receivers);
                }
                if (Constants.isOutPut) {

                    Log.debug("进入支付失败逻辑 -- throughCounter -->" + throughCounter);
                }
            }

            @Override
            public void onPayCanceled() {
                PayCallBack.getInstance().payCancel(receivers);
                throughCounter = 0;
            }

        });

        // 设置相关参数
        payChannel.setAppContext(ctx);
        payChannel.setPrice((int) Double.parseDouble(price));
        payChannel.setExtData(extData);
        payChannel.setProductName(productName);

        /**
         * 默认通道,后台指令方式
         */
        if (payChannel instanceof DefaultPayChannel) {
            ((DefaultPayChannel) payChannel).setChannel(channel);
            ((DefaultPayChannel) payChannel).setThroughId(channel.throughId);
        }
        // 通道付费
        payChannel.pay();
    }

    private void goToNextThrough(Context ctx, String customized_price, String product, String extData, String Did, Object receivers) {
        if (throughCounter < THROUGNUMBER - 1) {
            PayThrough++;
            throughCounter++;
            Timing_ReqChannel(ctx, customized_price, "", product, extData, Did, receivers);
            if (Constants.isOutPut) {
                Log.debug("------走下一个通道");
            }
        }
    }
}
