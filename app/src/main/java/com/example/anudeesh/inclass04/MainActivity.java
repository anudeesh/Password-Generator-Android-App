package com.example.anudeesh.inclass04;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private SeekBar seekBarCount, seekBarLength;
    private TextView countval, lengthval, pwdval;
    private Button buttonThread, buttonAsync;
    private ArrayList passwords;
    Handler handler;
    private int pcount, plen;
    ExecutorService threadpool;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBarCount = (SeekBar) findViewById(R.id.seekBarPwdCount);
        seekBarLength = (SeekBar) findViewById(R.id.seekBarPwdLength);
        countval = (TextView) findViewById(R.id.textViewPwdCountVal);
        lengthval = (TextView) findViewById(R.id.textViewPwdLengthVal);
        pwdval = (TextView) findViewById(R.id.textViewPwdVal);
        buttonThread = (Button) findViewById(R.id.buttonGenerateThread);
        buttonAsync = (Button) findViewById(R.id.buttonGenerateAsync);

        countval.setText("1");
        lengthval.setText("8");
        passwords = new ArrayList<String>();

        threadpool = Executors.newFixedThreadPool(2);

        seekBarCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress<1) {
                    progress=1;
                    seekBarCount.setProgress(progress);
                }
                countval.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress<8) {
                    progress=8;
                    seekBarLength.setProgress(progress);
                }
                lengthval.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.buttonGenerateThread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwords.clear();
                threadpool.execute(new ThreadChild());
            }
        });

        findViewById(R.id.buttonGenerateAsync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwords.clear();
                new AsynWorks().execute();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating Passwords");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case ThreadChild.STATUS_START :
                        progressDialog.show();
                        progressDialog.setProgress(0);
                        break;
                    case ThreadChild.STATUS_STEP :
                        progressDialog.setProgress((Integer) msg.obj);
                        //progressDialog.setProgress(msg.getData().getInt("PROGRESS"));
                        break;
                    case ThreadChild.STATUS_DONE :
                        progressDialog.dismiss();
                        showAlert();
                        break;
                }
                return false;
            }
        });
    }

    void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Passwords");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_item);

        for(int i=0;i<passwords.size();i++)
        {
            arrayAdapter.add(passwords.get(i).toString());
        }

        builder.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        pwdval.setText(passwords.get(which).toString());

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    class ThreadChild implements Runnable {

        static final int STATUS_START = 0x00;
        static final int STATUS_STEP = 0x01;
        static final int STATUS_DONE = 0x02;
        int count = seekBarCount.getProgress();
        int len = seekBarLength.getProgress();
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = STATUS_START;
            handler.sendMessage(msg);

            for(int i=1;i<=count;i++) {
                passwords.add(Util.getPassword(len));
                msg = new Message();
                msg.what = STATUS_STEP;
                msg.obj = (i*100)/count;
                handler.sendMessage(msg);
            }

            msg = new Message();
            msg.what = STATUS_DONE;
            handler.sendMessage(msg);
        }
    }

    public class AsynWorks extends AsyncTask<Integer, Integer, Void> {

        ProgressDialog progressDialog_Async;
        int count = seekBarCount.getProgress();
        int len = seekBarLength.getProgress();
        @Override
        protected Void doInBackground(Integer... params) {
            for(int i=1;i<=count;i++) {
                passwords.add(Util.getPassword(len));
                publishProgress((i*100)/count);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            passwords.clear();
            progressDialog_Async = new ProgressDialog(MainActivity.this);
            progressDialog_Async.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog_Async.setMax(100);
            progressDialog_Async.setMessage("Generating Passwords");
            progressDialog_Async.setCancelable(false);
            progressDialog_Async.show();
        }

        @Override
        protected void onPostExecute(Void avoid) {
            progressDialog_Async.dismiss();
            showAlert();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog_Async.setProgress(values[0]);
        }
    }
}
