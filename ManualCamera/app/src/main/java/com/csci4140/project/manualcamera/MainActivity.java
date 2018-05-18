package com.csci4140.project.manualcamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    int captureMode = 0; // 0 for auto, 1 for M, 2 for S, 3 for I
    int iso = 100;
    long ss = 312500;
    private mCamera mcamera = new mCamera();
    int scene = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set Textview invisible
        TextView textISO = (TextView) findViewById(R.id.textISO);
        TextView textSS = (TextView) findViewById(R.id.textSS);
        textISO.setVisibility(View.INVISIBLE);
        textSS.setVisibility(View.INVISIBLE);

        Button btnConfig = (Button) findViewById(R.id.btnConfig);
        btnConfig.setVisibility(View.INVISIBLE);
        RadioButton Imode = (RadioButton) findViewById(R.id.Imode);
        RadioButton Smode = (RadioButton) findViewById(R.id.Smode);
        RadioButton Mmode = (RadioButton) findViewById(R.id.Mmode);
        RadioButton Amode = (RadioButton) findViewById(R.id.Amode);
        Imode.setOnCheckedChangeListener(captureModeSwitchListener);
        Smode.setOnCheckedChangeListener(captureModeSwitchListener);
        Mmode.setOnCheckedChangeListener(captureModeSwitchListener);
        Amode.setOnCheckedChangeListener(captureModeSwitchListener);
        RadioButton stageMode = (RadioButton) findViewById(R.id.stageMode);
        RadioButton monoMode = (RadioButton) findViewById(R.id.monoMode);
        RadioButton Nscene = (RadioButton) findViewById(R.id.Nscene);
        stageMode.setOnCheckedChangeListener(sceneMode);
        monoMode.setOnCheckedChangeListener(sceneMode);
        Nscene.setOnCheckedChangeListener(sceneMode);

        //Set btnConfig
        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mcamera.takePicture(captureMode+6,iso,ss,scene);
                //mcamera.createCameraPreview(captureMode,iso,ss);
            }
        });

        //set listener on ISO value
        //set listener on SS value

        SeekBar sbarISO = (SeekBar) findViewById(R.id.seekBarIso);
        SeekBar sbarSS = (SeekBar) findViewById(R.id.seekBarSS);
        sbarISO.setVisibility(View.INVISIBLE);
        sbarSS.setVisibility(View.INVISIBLE);
        sbarISO.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double temp = new Double(100.0*Math.pow(2,progress));
                iso = temp.intValue();
                Integer t1 = temp.intValue();
                TextView textView = (TextView) findViewById(R.id.textISO);
                textView.setText(t1.toString());
                Log.e("Main",t1.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbarSS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double temp = new Double(312500.0*Math.pow(2,progress));
                ss = temp.longValue();
                Double showSpeed = new Double(3200.0/Math.pow(2,progress));
                Long t1 = showSpeed.longValue();
                TextView textView = (TextView) findViewById(R.id.textSS);
                textView.setText("1/"+t1.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //////////////////////////////////////////////////////////////////////////////////
        //set btnISO and btnSS listener
        Button btnISO = (Button) findViewById(R.id.btnISO);
        Button btnSS = (Button) findViewById(R.id.btnSS);
        btnISO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar sbarISO = (SeekBar) findViewById(R.id.seekBarIso);
                SeekBar sbarSS = (SeekBar) findViewById(R.id.seekBarSS);
                sbarSS.setVisibility(View.INVISIBLE);
                sbarISO.setVisibility(View.VISIBLE);
                TextView textISO = (TextView) findViewById(R.id.textISO);
                TextView textSS = (TextView) findViewById(R.id.textSS);
                textISO.setVisibility(View.VISIBLE);
                textSS.setVisibility(View.INVISIBLE);
                Button btnConfig = (Button) findViewById(R.id.btnConfig);
                btnConfig.setVisibility(View.VISIBLE);
            }
        });
        btnSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar sbarISO = (SeekBar) findViewById(R.id.seekBarIso);
                SeekBar sbarSS = (SeekBar) findViewById(R.id.seekBarSS);
                sbarISO.setVisibility(View.INVISIBLE);
                sbarSS.setVisibility(View.VISIBLE);
                TextView textISO = (TextView) findViewById(R.id.textISO);
                TextView textSS = (TextView) findViewById(R.id.textSS);
                textISO.setVisibility(View.INVISIBLE);
                textSS.setVisibility(View.VISIBLE);
                Button btnConfig = (Button) findViewById(R.id.btnConfig);
                btnConfig.setVisibility(View.VISIBLE);
            }
        });

        //Set camera button
        Button btnCapture = (Button) findViewById(R.id.capture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mcamera.takePicture(captureMode,iso,ss,scene);
                Integer temp = scene;
                Log.e("main scene:",temp.toString());
            }
        });

        ///////////////////////////////////////////////////////////////////////////
        //Set Camera
        TextureView textureView = (TextureView) findViewById(R.id.textureView);
        assert textureView != null;
        mcamera.initCame(this,textureView);

    }

    private CompoundButton.OnCheckedChangeListener captureModeSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Button btnISO = (Button) findViewById(R.id.btnISO);
            Button btnSS = (Button) findViewById(R.id.btnSS);
            Button btnConfig = (Button) findViewById(R.id.btnConfig);
            btnConfig.setVisibility(View.INVISIBLE);
            if (isChecked) {
                SeekBar sbarISO = (SeekBar) findViewById(R.id.seekBarIso);
                SeekBar sbarSS = (SeekBar) findViewById(R.id.seekBarSS);
                sbarISO.setVisibility(View.INVISIBLE);
                sbarSS.setVisibility(View.INVISIBLE);
                TextView textISO = (TextView) findViewById(R.id.textISO);
                TextView textSS = (TextView) findViewById(R.id.textSS);
                textISO.setVisibility(View.INVISIBLE);
                textSS.setVisibility(View.INVISIBLE);
                switch (buttonView.getId()) {
                    case R.id.Amode:
                        captureMode = 0;
                        btnISO.setVisibility(View.INVISIBLE);
                        btnSS.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.Imode:
                        Log.e("main", "select I mode");
                        captureMode = 3;
                        btnISO.setVisibility(View.VISIBLE);
                        btnSS.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.Smode:
                        Log.e("main", "select S mode");
                        captureMode = 2;
                        btnISO.setVisibility(View.INVISIBLE);
                        btnSS.setVisibility(View.VISIBLE);
                        break;
                    case R.id.Mmode:
                        Log.e("main", "select M mode");
                        captureMode = 1;
                        btnISO.setVisibility(View.VISIBLE);
                        btnSS.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    };

    //The listener for scene mode
    private CompoundButton.OnCheckedChangeListener sceneMode = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                Button btnConfig = (Button) findViewById(R.id.btnConfig);
                btnConfig.setVisibility(View.INVISIBLE);
                Button btnISO = (Button) findViewById(R.id.btnISO);
                Button btnSS = (Button) findViewById(R.id.btnSS);
                SeekBar sbarISO = (SeekBar) findViewById(R.id.seekBarIso);
                SeekBar sbarSS = (SeekBar) findViewById(R.id.seekBarSS);
                TextView textISO = (TextView) findViewById(R.id.textISO);
                TextView textSS = (TextView) findViewById(R.id.textSS);
                btnISO.setVisibility(View.INVISIBLE);
                btnSS.setVisibility(View.INVISIBLE);
                sbarISO.setVisibility(View.INVISIBLE);
                sbarSS.setVisibility(View.INVISIBLE);
                textISO.setVisibility(View.INVISIBLE);
                textSS.setVisibility(View.INVISIBLE);
                switch(buttonView.getId()) {
                    case R.id.stageMode:
                        captureMode = 4;
                        break;
                    case R.id.monoMode:
                        scene = 2;
                        mcamera.takePicture(captureMode+6,iso,ss,scene);
                        break;
                    case R.id.Nscene:
                        scene = 1;

                        mcamera.takePicture(captureMode+6,iso,ss,scene);
                }
            }
        }
    };
}
