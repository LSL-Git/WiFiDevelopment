package lsl.com.getphoneiptest.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import lsl.com.getphoneiptest.R;
import lsl.com.getphoneiptest.utils.WifiUtil;

public class SendActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tv, tv2;
    private Button but;
    private Button but_sel_file;
    private Button but_send;

    public String AcceptIP = null;
    private Uri fileUri =null;
    private String filePath = null;
    private File file = null;
    private int LocalListenPort = 4567;
    private Thread sendThread = null;
    private ListView lv_ipList;
    private List<String> isconnectIPList;
    private ArrayAdapter<String> adapter;

    private Thread thread;
    private Button but_cancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        tv = (TextView) findViewById(R.id.tv_show);
        tv2 = (TextView) findViewById(R.id.tv2);
        but = (Button) findViewById(R.id.but_cls);
        but_sel_file = (Button) findViewById(R.id.but_select);
        but_send = (Button) findViewById(R.id.but_send);
        lv_ipList = (ListView) findViewById(R.id.lv_isconnect);
        but_cancel = (Button) findViewById(R.id.but_cancel);

        but.setOnClickListener(this);
        but_sel_file.setOnClickListener(this);
        but_send.setOnClickListener(this);
        ListOnItemClickListener listener = new ListOnItemClickListener();
        lv_ipList.setOnItemClickListener(listener);
        but_cancel.setOnClickListener(this);


    }

    // 获取连接本机热点的ip
    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIp = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String [] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIp.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIp;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_cls:

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                Thread.sleep(2000);
                                Message msg = new Message();
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                break;
            case R.id.but_select:
                // 隐式启动系统内置的相册Activity
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 10);
                break;
            case R.id.but_send:
                AcceptIP = tv2.getText().toString();
                Log.e("SendActivity", tv2.getText().toString());
                if (!TextUtils.isEmpty(AcceptIP)){
                    // 启动发送线程
                    sendThread = new Thread(sendRunnable);
                    sendThread.start();
                } else {
                    Toast.makeText(SendActivity.this, "Ip不可用", 1).show();
                }
                break;
            case R.id.but_cancel:
                finish();
                break;
        }
    }
    private Handler handler = new Handler() {
        int i = 0;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    isconnectIPList = new ArrayList<>();
                    if (getConnectedIP().toString() != null) {
                        isconnectIPList = getConnectedIP();
                    }
                    adapter = new ArrayAdapter<>(SendActivity.this, android.R.layout.simple_list_item_1, isconnectIPList);
                    lv_ipList.setAdapter(adapter);
                    Log.e("SendActivity", ">>>>>>>>>");
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiUtil wifiUtil = new WifiUtil(SendActivity.this);
        wifiUtil.closeWifiAp();
        Toast.makeText(this,"热点已关闭", 1).show();
        if (thread != null) {
            thread.interrupt();
        }
    }

    private class ListOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String sel_Ip = adapter.getItem(position);
            tv2.setText(sel_Ip);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 10) {
                // 根据Activity 的返回值， 得到文件名与文件的路径
                fileUri = data.getData();
                // 根据文件的uri得到文件的路径地址
                filePath = SendActivity.getRealFilePath(this, fileUri);
    //            Log.e("FilePath", filePath);
                System.out.println(filePath);
                file = new File(filePath);
                tv.setText("已选文件：" + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //根据文件的Uri得到文件的路径地址（不用看懂）
    public static String getRealFilePath(Context context,final Uri uri) {
        if ( null == uri )
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        }
        else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ){
                if ( cursor.moveToFirst() ){
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ){
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    public Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            Socket socket;
            try {
                AcceptIP = tv2.getText().toString().trim();
                Log.d("SendActivity", AcceptIP);
                // 创建套接字，指定接收端IP地址与端口号
                socket = new Socket(AcceptIP, LocalListenPort);
                // 得到输出流
                OutputStream outputStream = socket.getOutputStream();
                FileInputStream fis = new FileInputStream(file);
                int count = fis.available();
                byte[] filedata = new byte[count];
                // 将选中的文件存储到内存中
                fis.read(filedata);
                // 组合文件名与文件大小，之间用“：”隔开
                String str = file.getName().toString() + ":" + filedata.length + ":";
                byte[] str2 = str.getBytes();
                // 将存储文件信息的数组和存储文件内容的数组组合成新数组
                byte[] data = new byte[count + str.length()];
                System.arraycopy(str2, 0, data, 0, str2.length);
                System.arraycopy(filedata, 0, data, str2.length, filedata.length);
                // 发送信息 格式为  文件名：文件大小（字节数）：文件内容
                outputStream.write(data);
                outputStream.flush();
                fis.close();
                outputStream.close();
                Looper.prepare();
                Toast.makeText(SendActivity.this, "发送成功", 1).show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}


