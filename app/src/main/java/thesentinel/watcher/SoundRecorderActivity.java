package thesentinel.watcher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.round;

public class SoundRecorderActivity extends AppCompatActivity {

    private TextView amplitudeValue;
    private ConstraintLayout constraintLayout;
    private MediaRecorder mRecorder;
    private static double MAX_AMPLITUDE_THRESHOLD = 80.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DEBUG","SoundRecorderActivity.onCreate");
        setContentView(R.layout.activity_sound_recorder);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),DeviceListActivity.class));
                finish();
            }
        });

        amplitudeValue = (TextView) findViewById(R.id.amplitudeValue);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("DEBUG","SoundRecorderActivity.onStart");
        getPermission();
        record();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DEBUG","SoundRecorderActivity.onResume");
        getPermission();
        record();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("DEBUG","SoundRecorderActivity.onPause");
        if (mRecorder == null) {
            mRecorder.stop();
            mRecorder = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("DEBUG","SoundRecorderActivity.onStop");
        if (mRecorder == null) {
            mRecorder.stop();
            mRecorder = null;
        }
    }

    private void record() {
        Log.d("DEBUG","record()");
        if (mRecorder == null) {
            try {
                Log.d("DEBUG","mRecorder NULL");
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");

                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new RecorderTask(mRecorder), 0, 50);
                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e("ERROR",e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    0);
        }
    }

    private void updateAmplitude(double amplitudeDb) {
        if (amplitudeDb > 0 && amplitudeDb < 1000) {
            amplitudeValue.setText(String.valueOf(round(amplitudeDb)) + " dB");
            if (amplitudeDb > MAX_AMPLITUDE_THRESHOLD) {
                triggered();
            } else {
                constraintLayout.setBackground(getResources().getDrawable(R.drawable.bg));
                // ledControl.turnOffLed();
            }
        }
    }

    private void triggered() {
        // change trigger with your needs.
        constraintLayout.setBackground(getResources().getDrawable(R.drawable.bg_danger));
        // ledControl.turnOnLed();
    }

    private class RecorderTask extends TimerTask {
        private MediaRecorder mRecorder;

        public RecorderTask(MediaRecorder mRecorder) {
            this.mRecorder = mRecorder;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int amplitude = mRecorder.getMaxAmplitude();
                    double amplitudeDb = 20 * Math.log10((double) Math.abs(amplitude));
                    Log.d("DEBUG","amplitudeDb:" + amplitudeDb);
                    updateAmplitude(round(amplitudeDb));
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}