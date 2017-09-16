package com.sdk.wj.paysdk.channel;

public interface IPayChannelListener {
	void onPaySucceeded();
	void onPayFailed();
	void onPayCanceled();
}
