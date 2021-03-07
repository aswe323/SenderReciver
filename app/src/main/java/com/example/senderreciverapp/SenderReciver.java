package com.example.senderreciverapp;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EventListener;

import static android.content.ContentValues.TAG;

public class SenderReciver extends Service {
    static boolean reciverRuning;
    static boolean senderRuning;
    static private Socket socket;
    final ServerSocket listener = new ServerSocket(7000);
    static MutableLiveData<String> recived = new MutableLiveData<>();
    private final IBinder binder = new LocalBinder();
    /**
     * https://developer.android.com/guide/components/bound-services#Binding
     */
    public class LocalBinder extends Binder {
        SenderReciver getService(){
            return SenderReciver.this;
        }
    }

    static class Socketed implements AutoCloseable, EventListener {

        private final BufferedReader reader;
        private final OutputStream writer;
        private final Socket socket;

        public Socketed(Socket socket) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = socket.getOutputStream();
        }

        public OutputStream getWriter() {
            return writer;
        }

        public BufferedReader getReader() {
            return reader;
        }

        public Socket getSocket() {
            return socket;
        }

        public void send(String toSend) throws IOException {
            writer.write(toSend.getBytes());
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }
    }

    public SenderReciver() throws IOException {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;

    }

    @Override
    public void onCreate(){
        super.onCreate();
        try {
            startReciver(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Begin listening
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Stop listening
    }

    public void startReciver(ServerSocket serverSocket) throws IOException {
        if(!reciverRuning){
            new Thread(() -> {//android dosn't allow doing network operations over the main thread.
                Log.d("senderReceiver,Receiver", "startReceiver: Thread Started Successfully");
                reciverRuning = true;
                while(reciverRuning) {
                    try (Socketed socketed = new Socketed(serverSocket.accept())) {//Blocks until the socket receives a connection.
                        BufferedReader reader = socketed.getReader();
                        String line = reader.readLine();
                        //Log.d("senderReceiver,Receiver", "startReceiver: received ->\n" + line);
                        recived.postValue(line);
                        socketed.send("ack");
                    } catch (Exception e) {
                        e.printStackTrace();
                        reciverRuning = false;
                        Log.d("senderReceiver,Receiver", "startReceiver:Thread Ended Due to Exception");
                    }
                }
            }
            ).start();
        }
    }

    public void sendData(String IP, String toSend) {
        if (!senderRuning) {
            senderRuning=true;
            new Thread(() -> {
                try {
                    socket = new Socket(IP, 7000);
                    OutputStream writer = socket.getOutputStream();
                    writer.write(toSend.getBytes());
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                senderRuning=false;
            }).start();
        }
    }

    public void sendData(Socket socket ,String toSend)  {
        if (!senderRuning) {
            senderRuning=true;
            new Thread(() -> {
                try {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    writer.print(toSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        senderRuning=false;

    }

}