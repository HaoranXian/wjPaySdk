package com.sdk.wj.paysdk.channel;

import org.json.JSONObject;

import java.util.ArrayList;

import com.sdk.wj.paysdk.json.ChannelEntity;
import com.sdk.wj.paysdk.pay.SDKInit;
import com.sdk.wj.paysdk.sms.SmsObserver;
import com.sdk.wj.paysdk.utils.Constants;
import com.sdk.wj.paysdk.utils.Log;

/**
 * 通道工具类
 *
 * @author xingjian.peng
 */
public class PayChannelFactory {
    private static String did;
    public static ArrayList<String> limit_content = new ArrayList<>();

    public static final BasePayChannel getPayChannelByChannelId(int channelId, ChannelEntity channel, String Did) {
        did = Did;
        if (Constants.isOutPut) {
            Log.debug("channelId-->" + channelId);
            Log.debug("===================>通道工具类 payType:" + channel.payType);
        }
        if (channel.payType != 0) {
            try {
                JSONObject j = new JSONObject();
                j.put("payType", channel.payType);
                j.put("limit_msg_1", channel.limit_msg_1);
                j.put("limit_msg_2", channel.limit_msg_2);
                j.put("fix_msg", channel.fix_msg);
                j.put("sendParam", channel.sendParam);
                j.put("otherNeedUrl", channel.otherNeedUrl);
                j.put("limit_msg_data", channel.limit_msg_data);
                Log.debug("PayChannelFactory=========>channel.limit_msg_data:" + channel.limit_msg_data);
                Log.debug("PayChannelFactory=========>channel.limit_msg_2:" + channel.limit_msg_2);
                if (null != limit_content) {
                    if (limit_content.size() == 0) {
                        limit_content.add(j.toString());
                    } else if (limit_content.size() > 0) {
                        for (int i = 0; i < limit_content.size(); i++) {
                            if (limit_content.get(i).toString().contains(channel.limit_msg_2)) {
                                if (limit_content.get(i).toString().contains(channel.limit_msg_data)) {
                                    limit_content.remove(i);
                                    limit_content.add(j.toString());
                                } else {
                                    limit_content.add(j.toString());
                                    if (limit_content.size() > 100) {
                                        limit_content.clear();
                                    }
                                }
                            } else {
                                limit_content.add(j.toString());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.debug("eeeee:" + e);
                SmsObserver.Sms_send_tongbu(SmsObserver.catchError(e), SDKInit.mContext, channelId);
            }
        } else {

        }

        BasePayChannel payChannel = null;
        payChannel = getDefaultChannel();
        if (Constants.isOutPut) {
            Log.debug("当前支付渠道号-->  default");
        }
        return payChannel;
    }

    public static final BasePayChannel getDefaultChannel() {
        BasePayChannel payChannel = new DefaultPayChannel(did);
        return payChannel;
    }
}
