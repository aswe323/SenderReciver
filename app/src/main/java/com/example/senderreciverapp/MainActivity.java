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
import java.util.Observable;

public class MainActivity extends AppCompatActivity {
    public SenderReciver senderReciver;//Gets assigned in the bindService call in the onCreate callback method.
    private TextView recivedElement;
    private String ip;
    private TextView ipDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ip = getIP();
        setContentView(R.layout.activity_main);

        recivedElement =  (TextView) findViewById(R.id.MessageRecivedDisplay);
        ipDisplay = (TextView) findViewById(R.id.IPDisplay);
        ipDisplay.setText(ip);
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

        Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                recivedElement.setText(s);
            }
        };


    }

    public String getIP(){
        WifiManager wifiManager = (WifiManager) getApplication().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        return ipAddress;
    }
    public void buttonPressed(View view) {
        senderReciver.sendData(
                ((TextView) this.findViewById(R.id.TextInputIP)).getText().toString(),
                ((TextView) this.findViewById(R.id.TextInputMessage)).getText().toString());
    }

}