package lsl.com.getphoneiptest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lsl.com.getphoneiptest.R;
import lsl.com.getphoneiptest.service.ListenService;
import lsl.com.getphoneiptest.utils.WifiUtil;
import lsl.com.getphoneiptest.zxing.activity.CaptureActivity;

/** 接收文件
 * Created by M1308_000 on 2016/9/18.
 */
public class AcceptActivity extends Activity implements View.OnClickListener{

    public Intent listenIntent = null;           //启动后台服务的Intent
    public static Handler handler = new Handler();
    public static String filename;              //接收到的文件的文件名
    public static TextView textView;
    private Button but_search;
    private ListView lv_hostPost;
    private List<String> hotspotList;
    private ArrayAdapter hotspotAdapter;
    private WifiUtil wifiUtil;
    private List<ScanResult> hostpotResultList;
    private Thread thread;
    private TextView tv_mIp;
    private TextView tv_isconnected;
    private String hostpotSSID;
    private Button but_exit;
    private boolean bool;
    private Button but_scan;

    public static void GetFlieName(String str){  //得到接收文件的文件名，并将其显示在TextView中
        filename = str;
        handler.post(new Runnable() {
            public void run() {
                textView.setText("已接受文件：" + filename);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);
        textView = (TextView)findViewById(R.id.tv_accept);
        but_search = (Button) findViewById(R.id.but_search);
        lv_hostPost = (ListView) findViewById(R.id.lv_hostpot);
        tv_mIp = (TextView) findViewById(R.id.tv_mIp);
        tv_isconnected = (TextView) findViewById(R.id.tv_isconnecthostpot);
        but_exit = (Button) findViewById(R.id.but_cancel1);
        but_scan = (Button) findViewById(R.id.but_scan);

        hotspotList = new ArrayList<>();
        hotspotAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, hotspotList);
        lv_hostPost.setAdapter(hotspotAdapter);
        ListOnItemClickListener wifiListListener = new ListOnItemClickListener();
        lv_hostPost.setOnItemClickListener(wifiListListener);

        but_scan.setOnClickListener(this);
        but_search.setOnClickListener(this);
        but_exit.setOnClickListener(this);
        wifiUtil = new WifiUtil(AcceptActivity.this);

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //判断SDCard是否存在并且可以读写
            listenIntent = new Intent(this, ListenService.class);
            startService(listenIntent);                            //启动后台服务
        }else{
            textView.setText("请插入SD卡！");             //提示SD卡不存在
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_search:
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                Thread.sleep(2000);
                                Message mag = new Message();
                                mag.what = 1;
                                mhandler.sendMessage(mag);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                wifiUtil.OpenWifi();
                Log.e("AcceptActivity", wifiUtil.getSSID());
                break;
            case R.id.but_scan:
                // 启动二维码扫描
                Intent openCamera = new Intent(AcceptActivity.this, CaptureActivity.class);
                startActivityForResult(openCamera, 0);
                break;
            case R.id.but_cancel1:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            tv_mIp.setText(scanResult);
        }
    }

    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.e("AcceptActivity", String.valueOf(wifiUtil.WifiCheckState()));
                    hotspotList.clear();
                    // 0正在关闭, 1 WiFi不可用, 2 正在打开, 3 可用, 4状态不可用
                    if (wifiUtil.WifiCheckState() == 3) {
                        tv_mIp.setText("本机IP：" + initLocalIp());
                        tv_isconnected.setText("你已连接：" + wifiUtil.getSSID());
                        hostpotResultList = wifiUtil.getScanResults();
                        wifiUtil.getConfiguration();
                        if (hostpotResultList != null) {
                            scanResultToString(hostpotResultList, hotspotList);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (thread != null) {
            thread.interrupt();
        }
        if (wifiUtil.CloseWifi()) {
            Toast.makeText(AcceptActivity.this, "WiFi已关闭", 1).show();
        }

    }

    //ScanResult类型转为String
    private void scanResultToString(List<ScanResult> hostpotResultList, List<String> hotspotList) {
        for (int i = 0; i < hostpotResultList.size(); i++) {
            ScanResult strScanResult = hostpotResultList.get(i);
            int lev = WifiManager.calculateSignalLevel(strScanResult.level, 5);
            String str = strScanResult.SSID + "---(信号强度：" + lev + " )";
            Toast.makeText(this, "正在扫描...", 10).show();
            Log.e("AcceptActivity", str.substring(0,3));
            if (str.substring(0, 3).equals("LSL")) {
                bool = hotspotList.add(str.substring(3,str.length()));
            }
            if (bool) {
                hotspotAdapter.notifyDataSetChanged();
            } else {
                Log.e("scanResultToString", "fial");
            }
        }
    }

    private class ListOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String hostpotItem = (String) hotspotAdapter.getItem(position);
            String [] ItemValus = hostpotItem.split("---");
            hostpotSSID = ItemValus[0];
            Log.e("AcceptActivity", hostpotSSID);
            if (wifiUtil.ConnectHostpot(hostpotSSID)) {
                Toast.makeText(AcceptActivity.this, "网络可用", 1).show();
            } else {
                Toast.makeText(AcceptActivity.this, "网络不可用", 1).show();
            }
        }
    }


    private String initLocalIp() {
        // 获取本机IP
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }


    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);

    }
}

