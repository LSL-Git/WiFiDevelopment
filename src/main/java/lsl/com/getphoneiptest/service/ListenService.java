package lsl.com.getphoneiptest.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

import lsl.com.getphoneiptest.activity.AcceptActivity;

/** 监听发送情况
 * Created by M1308_000 on 2016/9/18.
 */
public class ListenService extends Service{

    ServerSocket Serversocket = null;     //监听套接字
    int LocalListenPort = 4567;            //本地监听端口
    Thread ListenThread = null;           //监听线程
    public int m_fileLen = 0;              //接收文件长度
    public String m_filename = null;      //接收文件名
    FileOutputStream fos = null;          //文件输出流
    File SDpath = null;                    //默认文件目录
    File newFile = null;                  //接收到的文件
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try{
            Serversocket.close(); //关闭监听套接字
            ListenThread.interrupt(); //终止线程
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        SDpath = Environment.getExternalStorageDirectory();   //得到SD卡默认目录
        ListenThread = new Thread(null, listener, "ListenThread");
        ListenThread.start();                        //启动监听线程
    }

    public Runnable listener=new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                //实例化监听套接字，使它监听指定端口
                Serversocket=new ServerSocket(LocalListenPort);

                //循环监听
                while(!Thread.interrupted())  {
                    //调用ServerSocket的accept()方法，接受客户端所发送的请求
                    Socket socket = Serversocket.accept();
                    //创建一个接收该客户端发来数据的线程
                    receiveDataRunnable recRunnable = new receiveDataRunnable();
                    recRunnable.setSocket(socket);
                    Thread receiveThread = new Thread(recRunnable);
                    receiveThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("NewApi")
    public class receiveDataRunnable implements Runnable
    {
        private boolean ReceiveEnd = false;  // 判断是否接受结束的变量
        private Socket m_socket;

        public void setSocket(Socket socket){ //得到已经连接上的Socket
            m_socket = socket;
        }

        @Override
        public void run() {
            InputStream inputStream = null;
            try {
                //从Socket当中得到InputStream对象
                inputStream = m_socket.getInputStream();
            } catch (IOException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            while (ReceiveEnd == false){
                try {
                    int count = 0;
                    //返回的实际可读字节数，也就是当前消息的总大小
                    while (count == 0){
                        count = inputStream.available();
                    }
                    byte readBuffer [] = new byte[count];
                    int temp = 0;
                    //从InputStream当中读取客户端所发送的数据，并存到readBuffer中
                    temp = inputStream.read(readBuffer, 0, readBuffer.length);
                    if (temp == -1)
                        continue;   //没有读取成功，继续读下一条
                    //判断是否为第一次接受到数据
                    if (m_fileLen == 0 && m_filename == null){
                        String revText = new String(readBuffer, Charset.forName("UTF-8"));//编码成字符串
                        //根据接收的内容得到接受文件名和文件大小， 接受内容格式->  文件名：文件大小：文件内容
                        String[] sep = revText.split(":");
                        m_filename = sep[0];      //得到文件名
                        m_fileLen = Integer.parseInt(sep[1]);  //得到文件长度

                        if (sep.length>2 && !sep[2].equals("")){
                            //接收的消息中含有文件内容
                            //统计非文件内容所占的字节
                            String infoStr = sep[0]+":"+sep[1]+":";
                            int infoByteLen = infoStr.getBytes(Charset.forName("UTF-8")).length;
                            //得到文件内容所占的字节
                            int fileLen = readBuffer.length - infoByteLen;
                            //接收的文件内容存到内部存储器中
                            newFile = new File(SDpath, m_filename);
                            newFile.createNewFile();
                            fos = new FileOutputStream(newFile);
                            if (m_fileLen <= fileLen){
                                //这次所接收的消息包含全部的文件内容
                                fos.write(readBuffer, infoByteLen, m_fileLen);
                                fos.flush();
                                fos.close();
                                fos = null;
                                m_filename = null;
                                m_fileLen = 0;
                                ReceiveEnd = true;
                                //文件接收完成，将文件名传到Activity中
                                AcceptActivity.GetFlieName(newFile.getPath().toString());
                            }
                            else{
                                //表示这次接收的消息并未包含全部的文件内容
                                fos.write(readBuffer, infoByteLen, fileLen);
                                m_fileLen -= fileLen;
                            }
                        }
                    }
                    else{  //非第一次接受，继续接收对方发过来的剩余的内容
                        //判断当前消息是否包含全部的剩余文件内容
                        if(readBuffer.length < m_fileLen){
                            fos.write(readBuffer, 0, readBuffer.length);
                        }
                        else {
                            fos.write(readBuffer, 0, m_fileLen);
                        }
                        m_fileLen -= temp;
                        //判断是否接收完成
                        if (m_fileLen <= 0){ //文件接收完成
                            fos.flush();
                            fos.close();
                            fos = null;
                            m_filename = null;
                            m_fileLen = 0;
                            ReceiveEnd = true;
                            //将文件名传到Activity中
                            AcceptActivity.GetFlieName(newFile.getPath().toString());
                        }
                    }
                }
                catch (IOException e) {
                    try {
                        m_socket.shutdownInput();  //关闭套接字
                        m_socket.close();
                    }
                    catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        return;
                    }
                    e.printStackTrace();
                }
            }
            //关闭输入流及客户端套接字
            try {
                inputStream.close();
                m_socket.close();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        }
    }
}