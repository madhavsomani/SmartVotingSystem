package com.hnt.smartvotingsystem.smartvotingsystem;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import it.sephiroth.android.library.picasso.Picasso;

/**
 * Created by madhav on 2/12/2017.
 */

public class fingerscanverification extends Activity implements MFS100Event {

    Button btnInit;
    Button btnUninit;
    Button btnStartCapture;
    Button btnStopCapture;
    Button btnSyncCapture;
    Button btnExtractISOTemplate;
    Button btnExtractWSQ;
    Button btnMatchISOTemplate;
    Button btnClearLog;
    TextView lblMessage;
    EditText txtEventLog;
    ImageView imgFinger,imgorignalFinger;

    byte[] Enroll_Template;
    byte[] Verify_Template;

    FingerData fingerData1;
    FingerData fingerData2;
    FingerData fingerData3;


    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private Button startBtn;

    private ProgressDialog mProgressDialog;
    int mfsVer = 41;
    SharedPreferences settings;
    Context context;
    String usernamekey;
    CommonMethod.ScannerAction scannerAction = CommonMethod.ScannerAction.Capture;

    int minQuality = 60;
    int timeout = 10000;
    MFS100 mfs100 = null;

    ProgressBar pb;
    Dialog dialog;
    int downloadedSize = 0;
    int totalSize = 0;
    TextView cur_val;
    String dwnload_file_path;


    public static String _testKey = "t7L8wTG/iv02t+pgYrMQ7tt8qvU1z42nXpJDfAfsW592N4sKUHLd8A0MEV0GRxH+f4RgefEaMZALj7mgm/Thc0jNhR2CW9BZCTgeDPjC6q0W";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerscanverfication);
        context = fingerscanverification.this.getApplicationContext();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        imgorignalFinger = (ImageView) findViewById(R.id.imgFingerview);

        mfsVer = Integer.parseInt(settings.getString("MFSVer",
                String.valueOf(mfsVer)));

        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usernamekey = saved_values.getString("usernamekey", "null");

        dwnload_file_path = "http://hntdatabase.16mb.com/fingeruploads/" + usernamekey + ".iso";

        FindFormControls();
        CommonMethod.DeleteDirectory();
        CommonMethod.CreateDirectory();

        //Added by Milan Sheth on 19-Dec-2016
        PubVar.sharedPrefernceDeviceMode = context.getSharedPreferences(PubVar.strSpDeviceKey, Context.MODE_PRIVATE);

        startDownload();
        Picasso.with(context).load("http://hntdatabase.16mb.com/fingeruploads/"+usernamekey+".bmp").into(imgorignalFinger);

        // mfs100 = new MFS100(this, mfsVer, Base64.encodeToString(si(),
        // Base64.DEFAULT));
        mfs100 = new MFS100(this, mfsVer);
        mfs100.SetApplicationContext(this);

    }

    protected void onStop() {
        UnInitScanner();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mfs100 != null) {
            mfs100.Dispose();
        }
        super.onDestroy();
    }

    public void FindFormControls() {
        btnInit = (Button) findViewById(R.id.btnInit);
        btnUninit = (Button) findViewById(R.id.btnUninit);
        btnStartCapture = (Button) findViewById(R.id.btnStartCapture);

        // btnExtractISOTemplate = (Button) findViewById(R.id.btnExtractISOTemplate);
        // btnExtractWSQ = (Button) findViewById(R.id.btnExtractWSQ);
        btnMatchISOTemplate = (Button) findViewById(R.id.btnMatchISOTemplate);
        btnClearLog = (Button) findViewById(R.id.btnClearLog);
        lblMessage = (TextView) findViewById(R.id.lblMessage);
        txtEventLog = (EditText) findViewById(R.id.txtEventLog);
        imgFinger = (ImageView) findViewById(R.id.imgFinger2);

    }

    Handler handler2;
    Runnable runnable;
    int i = 0;

    public void onControlClicked(View v) {

        switch (v.getId()) {
            case R.id.btnInit:
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        InitScanner();
                    }
                }).start();
                break;
            case R.id.btnUninit:
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        UnInitScanner();
                    }
                }).start();
                break;
            case R.id.btnClearLog:
                matchthefingers();
                break;
            case R.id.btnMatchISOTemplate:
                scanfinger();
                break;

            case R.id.btnForLoop:
                Toast.makeText(fingerscanverification.this,
                        "Loop for init->uninit->init... 500 times",
                        Toast.LENGTH_LONG).show();
                i = 0;
                handler2 = new Handler();
                runnable = new Runnable() {

                    @Override
                    public void run() {
                        // Log.e("1", ""+ (i+1));
                        if (i >= 500) {
                            handler2.removeCallbacks(runnable);
                        } else {
                            if (i % 2 == 0) {
                                InitScanner();
                            } else {
                                UnInitScanner();
                            }
                            i++;
                            handler2.postDelayed(runnable, 100);
                        }
                    }
                };
                handler2.post(runnable);
                break;
            default:

                break;
        }
    }


    private void InitScanner() {
        try {
            int ret = mfs100.Init();
            if (ret != 0) {
                SetTextonuiThread(mfs100.GetErrorMsg(ret));
            } else {
                SetTextonuiThread("Init success");
                String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo()
                        + " Make: " + mfs100.GetDeviceInfo().Make()
                        + " Model: " + mfs100.GetDeviceInfo().Model()
                        + "\nCertificate: " + mfs100.GetCertification();
                SetLogOnUIThread(info);
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Init failed, unhandled exception",
                    Toast.LENGTH_LONG).show();
            SetTextonuiThread("Init failed, unhandled exception");
        }
    }


    private void UnInitScanner() {
        try {
            int ret = mfs100.UnInit();
            if (ret != 0) {
                SetTextonuiThread(mfs100.GetErrorMsg(ret));
            } else {
                SetLogOnUIThread("Uninit Success");
                SetTextonuiThread("Uninit Success");
            }
        } catch (Exception e) {
            Log.e("UnInitScanner.EX", e.toString());
        }
    }

    private void WriteFile(String filename, byte[] bytes) {
        try {
            String path = Environment.getExternalStorageDirectory()
                    + "//FingerData";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = path + "//" + filename;
            file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(bytes);
            stream.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


	/*
	 * private void DisplayFinger(final Bitmap bitmap) { imgFinger.post(new
	 * Runnable() {
	 *
	 * @Override public void run() { imgFinger.setImageBitmap(bitmap); } }); }
	 */

    private void SetTextonuiThread(final String str) {

        lblMessage.post(new Runnable() {
            public void run() {
                lblMessage.setText(str);
            }
        });
    }

    private void SetLogOnUIThread(final String str) {

        txtEventLog.post(new Runnable() {
            public void run() {
                // txtEventLog.setText(txtEventLog.getText().toString() + "\n"
                // + str, BufferType.EDITABLE);
                txtEventLog.append("\n" + str);
            }
        });
    }


    public void scanfinger() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SetTextonuiThread("");
                try {
                    fingerData1 = new FingerData();
                    int ret = mfs100.AutoCapture(fingerData1, timeout, true, true);
                    if (ret != 0) {
                        SetTextonuiThread(mfs100.GetErrorMsg(ret));
                    } else {
                        SetTextonuiThread("Quality: " + fingerData1.Quality()
                                + " NFIQ: " + fingerData1.Nfiq());
                        SetData1(fingerData1);
                    }
                } catch (Exception ex) {
                    SetTextonuiThread("Error");
                }
            }
        }).start();


    }



    public void SetData1(FingerData fingerData1) {
        WriteFile(usernamekey + ".bmp", fingerData1.FingerImage());
        WriteFile(usernamekey + ".iso", fingerData1.ISOTemplate());


    }





    public void matchthefingers()

    {
        //logic to retrive file to byte
        String path = Environment.getExternalStorageDirectory()
                + "//verifyData/"+usernamekey+".iso";
        File file = new File(path);
        //init array with file length
        byte[] bytesArray = new byte[(int) file.length()];

        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytesArray);
            fis.close();
            //read file into bytes[]
        } catch (IOException e) {
            e.printStackTrace();
        }

        matchiso(fingerData1, bytesArray);

    }

    public void matchiso(FingerData f1, byte[] array) {
        int ret = mfs100.MatchISO(f1.ISOTemplate(), array);
        if (ret >= 0) {
            if (ret >= 600) {
                SetTextonuiThread("Finger matched with score: " + ret);
                startActivity(new Intent(fingerscanverification.this,vote.class));
                finish();
            } else {
                SetTextonuiThread("Finger not matched, score: " + ret + " is too low");
            }
        } else {
            SetTextonuiThread(mfs100.GetErrorMsg(ret));
        }

    }


    @Override
    public void OnPreview(FingerData fingerData) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(
                fingerData.FingerImage(), 0, fingerData.FingerImage().length);
        imgFinger.post(new Runnable() {
            @Override
            public void run() {
                imgFinger.setImageBitmap(bitmap);
                imgFinger.refreshDrawableState();

            }
        });
        // Log.e("OnPreview.Quality", String.valueOf(fingerData.Quality()));
        SetTextonuiThread("Quality: " + fingerData.Quality());
    }

    @Override
    public void OnCaptureCompleted(boolean b, int i, String s, FingerData fingerData) {

    }


    @Override
    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        int ret = 0;
        if (!hasPermission) {
            SetTextonuiThread("Permission denied");
            return;
        }
        if (vid == 1204 || vid == 11279) {
            if (pid == 34323) {
                ret = mfs100.LoadFirmware();
                if (ret != 0) {
                    SetTextonuiThread(mfs100.GetErrorMsg(ret));
                } else {
                    SetTextonuiThread("Loadfirmware success");
                }
            } else if (pid == 4101) {
                //Added by Milan Sheth on 19-Dec-2016
                String strDeviceMode = PubVar.sharedPrefernceDeviceMode.getString(PubVar.strSpDeviceKey, "public");
                if (strDeviceMode.toLowerCase().equalsIgnoreCase("public")) {
                    ret = mfs100.Init("");
                    if (ret == -1322) {
                        ret = mfs100.Init(_testKey);
                        if (ret == 0) {
                            PubVar.sharedPrefernceDeviceMode.edit().putString(PubVar.strSpDeviceKey, "protected").apply();
                            showSuccessLog();
                        }
                    } else if (ret == 0) {
                        PubVar.sharedPrefernceDeviceMode.edit().putString(PubVar.strSpDeviceKey, "public").apply();
                        showSuccessLog();
                    }
                } else {
                    ret = mfs100.Init(_testKey);
                    if (ret == -1322) {
                        ret = mfs100.Init("");
                        if (ret == 0) {
                            PubVar.sharedPrefernceDeviceMode.edit().putString(PubVar.strSpDeviceKey, "public").apply();
                            showSuccessLog();
                        }
                    } else if (ret == 0) {
                        PubVar.sharedPrefernceDeviceMode.edit().putString(PubVar.strSpDeviceKey, "protected").apply();
                        showSuccessLog();
                    }
                }

                if (ret != 0) {
                    SetTextonuiThread(mfs100.GetErrorMsg(ret));
                }
            }
        }
    }

    private void showSuccessLog() {
        SetTextonuiThread("Init success");
        String info = "Serial: "
                + mfs100.GetDeviceInfo().SerialNo() + " Make: "
                + mfs100.GetDeviceInfo().Make() + " Model: "
                + mfs100.GetDeviceInfo().Model()
                + "\nCertificate: " + mfs100.GetCertification();
        SetLogOnUIThread(info);
    }

    @Override
    public void OnDeviceDetached() {
        UnInitScanner();
        SetTextonuiThread("Device removed");
    }

    @Override
    public void OnHostCheckFailed(String err) {
        try {
            SetLogOnUIThread(err);
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {

        }
    }


    private void startDownload() {
        String url = dwnload_file_path;
        new DownloadFileAsync().execute(url);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                String path = Environment.getExternalStorageDirectory()
                        + "//verifyData";
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }

                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                InputStream input = new BufferedInputStream(url.openStream());
                String downloadpath = Environment.getExternalStorageDirectory()
                        + "//verifyData/"+usernamekey+".iso";
                OutputStream output = new FileOutputStream(downloadpath);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            }
            return null;

        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

    }

}