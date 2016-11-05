package lsl.com.getphoneiptest.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.WriterException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import lsl.com.getphoneiptest.R;
import lsl.com.getphoneiptest.tool.SendRunnable;
import lsl.com.getphoneiptest.zxing.encoding.EncodingHandler;

/** 二维码显示
 * Created by M1308_000 on 2016/11/1.
 */

public class QRcodeActivity extends Activity implements View.OnClickListener{

    private ImageView iv_crimageview;
    private boolean OK = true;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        init();

        findViewById(R.id.but_back).setOnClickListener(this);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (OK) {
                    try {
                        Thread.sleep(1000);
                        Log.e("QRcodeActivity","now is run " + getConnectedIP());
                        if (!TextUtils.isEmpty(getConnectedIP())) {
                            sendThread();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (thread != null) {
            thread.interrupt();
        }
    }

    // 获取连接本机热点的ip
    private String getConnectedIP() {
        String connectedIp = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String [] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIp = ip;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIp.substring(2,connectedIp.length());
    }

    // 初始化数据
    private void init() {
        iv_crimageview = (ImageView) findViewById(R.id.iv_qrcode);
        Intent intent = getIntent();
        String qr = intent.getStringExtra("filePath");

        try {
            Bitmap qrCodeBitmap = EncodingHandler.createQRCode(qr, 400);
            iv_crimageview.setImageBitmap(qrCodeBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void sendThread() {
        // 启动发送线程
        SendRunnable runnable = new SendRunnable(QRcodeActivity.this, getConnectedIP());
        runnable.run();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_back:
                finish();
                break;
        }
    }
}
