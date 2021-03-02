package com.weikun.androidutils.utils;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.RequiresPermission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 移动数据 工具类
 * @author linweikun
 * @date 2021/1/18
 */
public class MobileManager {
    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;

    public MobileManager(Context context) {
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mContext = context;
    }

    /**
     * 数据流量开关是否打开
     */
    public boolean isMobileDataEnabled() {
        try {
            Method method = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mConnectivityManager);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 是否联网
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public boolean isConnected() {
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * 获取网络类型
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public int getNetworkType(){
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo==null){
            return -1;
        }
        return networkInfo.getType();
    }

    /**
     *  是否插上SIM卡
     */
    public boolean hasSim() {
        String operator = mTelephonyManager.getSimOperator();
        if (TextUtils.isEmpty(operator)) {
            return false;
        }
        return true;
    }

    /**
     * 获取当前SIM卡数量
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public int getAvailableSimSlotCount() {
        return getActiveSubscriptionInfoList().size();
    }

    /**
     * 获取当前可用SIM服务
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public List<SubscriptionInfo> getActiveSubscriptionInfoList() {
        return mSubscriptionManager.getActiveSubscriptionInfoList();
    }

    /**
     * 获取当前使用的SIM卡槽序号
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public int getEnabledSimSlotIndex() {
        int targetId = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            targetId = SubscriptionManager.getDefaultDataSubscriptionId();
        }else {
            try {
                Method method = SubscriptionManager.class.getMethod("getDefaultDataSubId");
                targetId = (int)method.invoke(mSubscriptionManager);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        List<SubscriptionInfo> list = getActiveSubscriptionInfoList();
        for (SubscriptionInfo info : list) {
            if (info.getSubscriptionId() == targetId) {
                return info.getSimSlotIndex();
            }
        }
        return -1;
    }

    /**
     * 获取网络运营商名称
     * 中国移动、如中国联通、中国电信
     */
    public String getOperatorName() {
        String opeType = "unknown";
        if (!hasSim()) {
            return opeType;
        }
        String operator = mTelephonyManager.getSimOperator();
        if ("46001".equals(operator) || "46006".equals(operator) || "46009".equals(operator)) {
            opeType = "中国联通";
        } else if ("46000".equals(operator) || "46002".equals(operator) || "46004".equals(operator) || "46007".equals(operator)) {
            opeType = "中国移动";

        } else if ("46003".equals(operator) || "46005".equals(operator) || "46011".equals(operator)) {
            opeType = "中国电信";
        } else {
            opeType = "unknown";
        }
        return opeType;
    }

    /**
     * 网络连通测试
     * @param address 测试地址
     */
    public static boolean ping(String address) throws IOException {
        StringBuilder pingResult = new StringBuilder();
        StringBuilder pingStr = new StringBuilder("ping -c 5 -i 0.5 -s 32 ");
        pingStr.append(address);
        Process process = Runtime.getRuntime().exec(pingStr.toString());
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
            pingResult.append(line).append("\n");
            SystemClock.sleep(100);
        }
        br.close();
        process.destroy();
        return pingResult.indexOf("ttl") > 0;
    }

    /**
     * 根据SIM卡槽序号来获取SIM服务信息
     * @param slotIndex SIM卡槽序号
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public SubscriptionInfo getSubscriptionInfoWithSimSlotIndex(int slotIndex) {
        return mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slotIndex);
    }

    private static final String SIM_STATE = "getSimState";

    /**
     * 获取相应卡的状态
     * @param slotIdx:0(sim1),1(sim2)
     * @return true:使用中；false:未使用中
     */
    public boolean isSimEnabled(int slotIdx) {
        boolean isReady = false;
        try {
            Method method = TelephonyManager.class.getMethod("getSimState");

        }catch (Exception e){
            e.printStackTrace();
        }
        Object getSimState = getSimByMethod(mContext, SIM_STATE, slotIdx);
        if (getSimState != null) {
            int simState = Integer.parseInt(getSimState.toString());
            if ((simState != TelephonyManager.SIM_STATE_ABSENT) && (simState != TelephonyManager.SIM_STATE_UNKNOWN)) {
                isReady = true;
            }
        }
        return isReady;
    }

    /**
     * 通过反射调用相应的方法
     */
    private Object getSimByMethod(Context context, String method, int param) {
        try {
            Class<?> telephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimState = telephonyClass.getMethod(method, parameter);
            Object[] obParameter = new Object[1];
            obParameter[0] = param;
            Object ob_phone = getSimState.invoke(mTelephonyManager, obParameter);
            if (ob_phone != null) {
                return ob_phone;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 切换流量数据卡
     * @param index 卡槽序号
     * 需要添加权限android.permission.WRITE_APN_SETTINGS
     * 原生系统不支持第三方应用切换SIM卡，需要system uid签名(推荐)或者修改系统代码去开放该权限(做坏事，不推荐)
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public static void switchDataSim(Context context,int index) throws Exception {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        int targetSubscriptionId = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(index).getSubscriptionId();
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1){
            Method method = SubscriptionManager.class.getMethod("setDefaultDataSubId", int.class);
            method.invoke(subscriptionManager, targetSubscriptionId);
        }else {
            Method method = TelephonyManager.class.getMethod("setDataEnabled",int.class,boolean.class);
            method.invoke(subscriptionManager,targetSubscriptionId,true);
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public NetworkInfo getNetworkInfo(){
        return mConnectivityManager.getActiveNetworkInfo();
    }
}
