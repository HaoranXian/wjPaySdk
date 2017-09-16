package com.sdk.wj.paysdk.sms;

public interface ISendMessageListener {
	void onSendSucceed();
	void onSendFailed();
}
