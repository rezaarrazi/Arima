package com.harrysoft.androidbluetoothserial.demoapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.arata.reza.bluetoothplugin.BluetoothManager;

import java.io.IOException;

public class SerialActivity extends AppCompatActivity {

    TextView myLabel;
    BluetoothManager bluetoothManager = new BluetoothManager();

    boolean connected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial);

        Handler handler = new Handler();
        int delay = 3000; //milliseconds

        Button closeButton = (Button)findViewById(R.id.close);
        myLabel = (TextView)findViewById(R.id.label);

        String deviceName;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                deviceName= null;
            } else {
                deviceName= extras.getString("device_name");
            }
        } else {
            deviceName= (String) savedInstanceState.getSerializable("device_name");
        }

        myLabel.setText(deviceName);
        try
        {
            if(bluetoothManager.findBT(deviceName)){
//                myLabel.setText("Bluetooth Device Found");
                bluetoothManager.openBT();
                connected = true;
            }
            else {
//                myLabel.setText("No bluetooth adapter available");
            }
        }
        catch (IOException ex) { }

        //Close button
        closeButton.setOnClickListener(v -> {
            try
            {
                bluetoothManager.closeBT();
            }
            catch (IOException ex) { }

            connected = false;
            handler.removeCallbacksAndMessages(null);
            finish();
        });

        //initial message
        try {
            bluetoothManager.sendData("a");
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something

                if(connected){
                    if(bluetoothManager.isReady()){
//                        myLabel.setText("ready");
                        try
                        {
                            bluetoothManager.sendData("a");
                        }
                        catch (IOException ex) { }
                    }
                    else {
                        if(!bluetoothManager.isReady()){
//                            myLabel.setText("bluetooth not ready");
                            try
                            {
                                bluetoothManager.closeBT();
                            }
                            catch (IOException ex) { }

                            addNotification();

                            connected = false;
                            handler.removeCallbacksAndMessages(null);
                            finish();
                        }
                    }
                }

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void addNotification(){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.bluetooth_logo)
                .setContentTitle("Device Lost")
                .setContentText("Your device lost!!!")
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED, 3000, 3000)
                .setSound(alarmSound);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
