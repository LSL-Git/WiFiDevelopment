package lsl.com.getphoneiptest.utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/** wifi管理类
 * Created by M1308_000 on 2016/9/28.
 */
public class WifiUtil {

    // WIFIConfiguration描述WIFI的链接信息，包括SSID、SSID隐藏、password等的设置
    private List<WifiConfiguration> wifiConfigList;
    private WifiManager wifiManager;

    public WifiUtil(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    // 查看WiFi状态
    public int WifiCheckState() {
        return wifiManager.getWifiState();
    }
    // 打开WiFi
    public void OpenWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    // 关闭WiFi
    public boolean CloseWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        return wifiManager.isWifiEnabled();
    }

    // 开始扫描WiFi
    public void WifiStartScan() {
        wifiManager.startScan();
    }

    // 得到扫描结果
    public List<ScanResult> getScanResults() {
        return wifiManager.getScanResults();
    }

    // 得到WiFi配置好的信息
    public void getConfiguration() {
        wifiConfigList = wifiManager.getConfiguredNetworks();
        for(int i = 0 ; i < wifiConfigList.size() ; i++ ){
            Log.i("getConfiguration",wifiConfigList.get(i).SSID);
            Log.i("getConfiguration",String.valueOf(wifiConfigList.get(i).networkId));
        }
    }

    public String getSSID() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    // 判定指定WiFi是否已经配置好,根据WiFi的地址BSSID,返回netid
    public int IsConfiguration(String SSID) {
        Log.i("IsConfiguration",String.valueOf(wifiConfigList.size()));
        for (int i = 0; i < wifiConfigList.size(); i++) {
            Log.i(wifiConfigList.get(i).SSID,String.valueOf( wifiConfigList.get(i).networkId));
            if(wifiConfigList.get(i).SSID.equals(SSID)){//地址相同
                return wifiConfigList.get(i).networkId;
            }
        }
        return -1;
    }

    // 添加指定WiFi的配置信息,原列表不存在此SSID
    public int AddWifiConfig(List<ScanResult> wifiList, String ssid, String psw) {
        int WifiId = -1;
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult wifi = wifiList.get(i);
            if (wifi.SSID.equals(ssid)) {
                Log.e("AddWifiConfig", "equals");
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = "\"" + wifi.SSID + "\"";  // \ 转义字符
                wifiConfig.preSharedKey = "\"" + psw + "\""; // WPA-PSK密码
                wifiConfig.hiddenSSID = false;
                wifiConfig.status = WifiConfiguration.Status.ENABLED;
                WifiId = wifiManager.addNetwork(wifiConfig); // 将配置好的特定WiFi密码信息添加,添加完后默认是不激活状态,成功返回ID,否则返回-1
                if (WifiId != -1) {
                    return WifiId;
                }
            }
        }
        return WifiId;
    }

    // 链接指定id的WiFi
    public boolean ConnectWifi(int wifiId) {

        for (int i = 0; i < wifiConfigList.size(); i++) {
            WifiConfiguration wifi = wifiConfigList.get(i);
            if (wifiId == wifi.networkId) {
                // 激活该id,建立连接
                Log.e("ConnectWifi", "wifiid :" + String.valueOf(wifiId));
                while (!(wifiManager.enableNetwork(wifiId,true))) {
                    // status:  0--已经链接, 1 -- 可链接, 2 -- 不可链接
                    Log.e("ConnectWifi", String.valueOf(wifiConfigList.get(wifiId).status));
                }
                return true;
            }
        }
        return false;
    }

    // 关闭开放热点
    public void closeWifiAp() {
        if (isWifiApEnabled()) {
            try {
                Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
                Method method1 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method1.invoke(wifiManager, config, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isWifiApEnabled() {
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    //
    // 连接指定的热点
    public boolean ConnectHostpot(String ssid) {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WifiConfiguration wifiConfiguration = CreateWifiInfo(ssid);

            WifiConfiguration wifiConfiguration1 = IsExsits(ssid);
            if (wifiConfiguration1 != null) {
                wifiManager.removeNetwork(wifiConfiguration1.networkId);
            }
            int netID = wifiManager.addNetwork(wifiConfiguration);
            boolean enabled = wifiManager.enableNetwork(netID, true);
            Log.e("WifiUtil", "WiFi是否可用？" + enabled);
            return enabled;
        }
        return false;
    }

    private WifiConfiguration IsExsits(String ssid) {
        List<WifiConfiguration> exsitconfig = wifiManager.getConfiguredNetworks();
        if (exsitconfig == null) {
            return null;
        } else {
            for (WifiConfiguration wifiConfiguration : exsitconfig) {
                if (wifiConfiguration.SSID.equals("\"" + ssid + "\"")) {
                    return wifiConfiguration;
                }
            }
        }
        return null;
    }

    private WifiConfiguration CreateWifiInfo(String ssid) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        return config;
    }

}
