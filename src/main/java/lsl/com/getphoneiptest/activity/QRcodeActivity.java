package lsl.com.getphoneiptest.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.WriterException;

import lsl.com.getphoneiptest.R;
import lsl.com.getphoneiptest.zxing.encoding.EncodingHandler;

/**
 * Created by M1308_000 on 2016/11/1.
 */

public class QRcodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        ImageView iv_crimageview = (ImageView) findViewById(R.id.iv_qrcode);

        Intent intent = getIntent();
        String qr = intent.getStringExtra("filePath");

        try {
            Bitmap qrCodeBitmap = EncodingHandler.createQRCode(qr, 400);
            iv_crimageview.setImageBitmap(qrCodeBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        findViewById(R.id.but_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
