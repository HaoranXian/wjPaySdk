package com.sdk.wj.paysdk.channel;

import android.content.Context;

import java.util.Vector;

import com.sdk.wj.paysdk.json.JsonEntity;

public abstract class BasePayChannel implements IPayChannel {

    private Vector<IPayChannelListener> payChannelListenerList = new Vector<IPayChannelListener>();

    public BasePayChannel() {

    }

    @Override
    public void addPayChannelListener(IPayChannelListener newPayChannelListener) {
        if (!payChannelListenerList.contains(newPayChannelListener)) {
            payChannelListenerList.add(newPayChannelListener);
        }
    }

    @Override
    public void removePayChannelListener(
            IPayChannelListener newPayChannelListener) {
        if (payChannelListenerList.contains(newPayChannelListener)) {
            payChannelListenerList.remove(newPayChannelListener);
        }
    }

    @Override
    public void pay() {
    }


    protected void postPaySucceededEvent() {
        for (IPayChannelListener payChannelListener : payChannelListenerList) {
            payChannelListener.onPaySucceeded();
        }
    }

    protected void postPayFailedEvent() {
        for (IPayChannelListener payChannelListener : payChannelListenerList) {
            payChannelListener.onPayFailed();
        }
    }

    protected void postPayCanceledEvent() {
        for (IPayChannelListener payChannelListener : payChannelListenerList) {
            payChannelListener.onPayCanceled();
        }
    }

    /**
     * appContext
     *
     * @return the appContext
     * @since 2014年11月6日
     */
    public Context getAppContext() {
        return appContext;
    }

    /**
     * @param appContext the appContext to set
     */
    public void setAppContext(Context appContext) {
        this.appContext = appContext;
    }

    /**
     * price
     *
     * @return the price
     * @since 2014年11月6日
     */
    public int getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * secondTipInfo
     *
     * @return the secondTipInfo
     * @since 2014年11月6日
     */
    public String getSecondTipInfo() {
        return secondTipInfo;
    }

    /**
     * @param secondTipInfo the secondTipInfo to set
     */
    public void setSecondTipInfo(String secondTipInfo) {
        this.secondTipInfo = secondTipInfo;
    }

    /**
     * productName
     *
     * @return the productName
     * @since 2014年11月6日
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName the productName to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * extData
     *
     * @return the extData
     * @since 2014年11月6日
     */
    public String getExtData() {
        return extData;
    }

    /**
     * @param extData the extData to set
     */
    public void setExtData(String extData) {
        this.extData = extData;
    }

    public JsonEntity.RequestProperties getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(JsonEntity.RequestProperties orderInfo) {
        this.orderInfo = orderInfo;
    }

    private JsonEntity.RequestProperties orderInfo;
    protected Context appContext;
    protected static int price;
    protected String secondTipInfo;
    protected String productName;
    protected String extData;
    protected String payCode;

}
