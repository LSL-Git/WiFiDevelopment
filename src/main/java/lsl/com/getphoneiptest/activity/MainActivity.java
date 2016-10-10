package lsl.com.getphoneiptest.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import lsl.com.getphoneiptest.R;

/** 选择操作
 * Created by M1308_000 on 2016/9/18.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private TextView but_send;
    private TextView but_accept;

    private boolean flag = false;
    private WifiManager wifiManager;
    private String isOk;
    private List<ScanResult> wifiList;
    private List<String> passableHotsPot;
    private WifiReceiver wifiReceiver;
    private boolean isConnected = false;
    private TextView but_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        but_send = (TextView) findViewById(R.id.but_sel_send);
        but_accept = (TextView) findViewById(R.id.but_sel_accept);
        but_exit = (TextView) findViewById(R.id.but_exit);

        but_send.setOnClickListener(this);
        but_accept.setOnClickListener(this);
        but_exit.setOnClickListener(this);
        // 获取WiFi管理服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_sel_send:
                // 如果是打开就关闭,如果是关闭就打开
                flag = !flag;
                if (setWifiApEnabled(flag)) {
                    isOk = "热点开启成功";
                } else {
                    isOk = "热点开启失败";
                }
                Intent intent = new Intent(this, SendActivity.class);
                startActivity(intent);
                break;
            case R.id.but_sel_accept:
                Intent intent1 = new Intent(this, AcceptActivity.class);
                startActivity(intent1);
                wifiManager.startScan();
//                Toast.makeText(this, "????????", 1).show();
                break;
            case R.id.but_exit:
                finish();
                break;
        }
    }

    // WiFi热点开关
    public boolean setWifiApEnabled(boolean enable) {
        if (enable) {
            // WiFi与热点不能同时打开，所以打开热点要先关掉WiFi
            wifiManager.setWifiEnabled(false);
        }
        try {
            // 热点配置类
            WifiConfiguration apconfig = new WifiConfiguration();
            // 设置热点名称
            apconfig.SSID = "LSL";
            // 设置热点密码
            apconfig.preSharedKey = null;
            // 通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            // 返回热点打开状态
            return (boolean) method.invoke(wifiManager, apconfig, enable);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private final class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            wifiList = wifiManager.getScanResults();
            Log.e("MainActivity", "setWifiApEnabled" + String.valueOf(wifiList));
            if (wifiList == null || wifiList.size() == 0 || isConnected) {
                return;
            }
            onReceiveNewNetworks(wifiList);
        }
    }

    private void onReceiveNewNetworks(List<ScanResult> wifiList) {
        Log.e("MainActivity", "onReceiveNewNetworks");
        passableHotsPot = new ArrayList<>();
        for (ScanResult result : wifiList) {
            Log.e("MainActivity", result.SSID);
            if ((result.SSID).contains("LSL")) {
                passableHotsPot.add(result.SSID);
            }
        }
        synchronized (this) {
            connectToHotpot();
        }
    }

    private void connectToHotpot() {
        Log.e("MainActivity", "connectToHotpot");
        if (passableHotsPot == null || passableHotsPot.size() == 0) {
            return;
        }
        WifiConfiguration wifiConfiguration = this.setWifiParams(passableHotsPot.get(0));
        int wcgID = wifiManager.addNetwork(wifiConfiguration);
        boolean falg2 = wifiManager.enableNetwork(wcgID, true);
        isConnected = falg2;
        Log.e("connect seccess ?", String.valueOf(falg2));
    }

    private WifiConfiguration setWifiParams(String ssId) {
        Log.e("MainActivity", "setWifiParams  " + ssId);
        WifiConfiguration wificonfig = new WifiConfiguration();
        wificonfig.SSID = "\""+ssId+"\"";
        wificonfig.preSharedKey = null;
        wificonfig.hiddenSSID = true;
        wificonfig.status = WifiConfiguration.Status.ENABLED;
        wificonfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wificonfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wificonfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wificonfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wificonfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wificonfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return wificonfig;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
    }
}
