package com.voicesync.v2v;

import graph.FreqRespGraph;
import graph.GraphRadial;
import graph.Recurrence;
import Signal.Conf;
import Signal.Signal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    private ImageButton ibRecord, ibPlay;
    private GraphRadial graphRadial1;
    private Recurrence recurrence1;
    private FreqRespGraph freqRespGraph1;

    // Requesting permission to RECORD_AUDIO
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        init();
    }

    private void init() {
        ibRecord = (ImageButton) findViewById(R.id.ibRecord);
        ibPlay = (ImageButton) findViewById(R.id.ibPlay);
        ibPlay.setEnabled(false);
        graphRadial1 = (GraphRadial) findViewById(R.id.graphRadial1);
        recurrence1 = (Recurrence) findViewById(R.id.recurrence1);
        freqRespGraph1 = (FreqRespGraph) findViewById(R.id.freqRespGraph1);
        addListeners();
    }

    private void addListeners() { // sequence of listener called when rec chuck is ready: fft, graphs
        Signal.addListener(); // recFFT
        Signal.addListener(graphRadial1.getListener());
        Signal.addListener(recurrence1.getListener());
        Signal.addListener(freqRespGraph1.getListener());
    }

    public void onClickRecord(View v) {
        if (Signal.rec.isRecording) {
            Signal.stopAll();
            ibRecord.setImageResource(R.drawable.record_off);
            on(ibPlay);
        } else {
            Signal.startRecording();
            ibRecord.setImageResource(R.drawable.record);
            off(ibPlay);
        }
    }

    public void onClickPlay(View v) {
        if (Signal.isPlaying()) {
            Signal.stopAll();
            on(ibRecord);
        } else {
            Signal.startPlaying();
            off(ibRecord);
        }
    }

    private void on(ImageButton ib) {
        ib.setEnabled(true);
    }

    private void off(ImageButton ib) {
        ib.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        on(ibRecord);
    }

    @Override
    protected void onPause() {
        super.onPause();
        on(ibRecord);
        off(ibPlay);
        Signal.stopAll();
        Signal.reset();
    }

    @Override
    protected void onStop() { // ap stop, maybe not called
        super.onStop();
        Signal.stopAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
