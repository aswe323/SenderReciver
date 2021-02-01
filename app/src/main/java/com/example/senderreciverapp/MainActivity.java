package com.example.senderreciverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;

public class MainActivity extends AppCompatActivity {
    public SenderReciver senderReciver;//Gets assigned in the bindService call in the onCreate callback method.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }
    
    public void buttonPressed(View view) {
        senderReciver.sendData("127.0.0.1","Hi!\n");
    }
}