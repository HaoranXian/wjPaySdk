package com.sdk.wj.paysdk.channel;

public interface IPayChannel {
	void addPayChannelListener(IPayChannelListener newPayChannelListener);
	void removePayChannelListener(IPayChannelListener newPayChannelListener);
	void pay(); 
}
