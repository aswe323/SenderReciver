package com.example.senderreciverapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Observable;
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;

public class MainActivity extends AppCompatActivity {
    public SenderReciver senderReciver;//Gets assigned in the bindService call in the onCreate callback method.
    private TextView recivedElement;
    static private String ip;
    private TextView ipDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initIP();
        setContentView(R.layout.activity_main);

        recivedElement = findViewById(R.id.MessageRecivedDisplay);
        ipDisplay = findViewById(R.id.IPDisplay);

        Intent intent = new Intent(this,SenderReciver.class);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SenderReciver.LocalBinder binder = (SenderReciver.LocalBinder) service;
                senderReciver = binder.getService();
                Toast.makeText(senderReciver, "Service Bound", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        startService(intent);
        bindService(intent,connection,0);

        SenderReciver.recived.observe(this, s -> recivedElement.setText(s));
    }

    public void initIP(){
        new Thread( () ->   {
            try (Scanner s = new Scanner(new URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A")) {
                ip = "" + s.next();
                ipDisplay.setText("" + ip);

                Log.d("test", "initIP: " + ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void buttonPressed(View view) {
        String ip = ((TextView) this.findViewById(R.id.TextInputIP)).getText().toString();
        String message = ((TextView) this.findViewById(R.id.TextInputMessage)).getText().toString();
        senderReciver.sendData(ip, message);
        Log.d("networkTraffic", "buttonPressed with data: " + message + " to ip: " + ip);
    }

}