package lsl.com.getphoneiptest.tool;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

import static lsl.com.getphoneiptest.activity.MainActivity.file;

/** 用于发送文件
 * Created by M1308_000 on 2016/11/3.
 */

public class SendRunnable implements Runnable {

    private int LocalListenPort = 4567;
    public Context mcontext;
    private String AcceptIP;

    public SendRunnable(Context mcontext, String AcceptIP) {
        this.mcontext = mcontext;
        this.AcceptIP = AcceptIP;
    }

    @Override
    public void run() {
        Log.e("QRcodeActivity", AcceptIP);
        Socket socket;
        try {
//            AcceptIP = tv2.getText().toString().trim();
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
            Toast.makeText(mcontext, "发送成功", 1).show();
            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
