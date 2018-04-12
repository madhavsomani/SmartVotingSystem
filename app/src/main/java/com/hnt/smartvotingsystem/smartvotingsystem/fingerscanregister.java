package com.hnt.smartvotingsystem.smartvotingsystem;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mantra.mfs100.DeviceInfo;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static android.R.attr.data;

/**
 * Created by madhav on 2/12/2017.
 */

public class fingerscanregister extends Activity implements MFS100Event {

    Button btnInit;
    Button btnUninit;
    Button btnStartCapture;
    Button upoadbutton;
    Button btnMatchISOTemplate;
    Button btnClearLog;
    TextView lblMessage;
    EditText txtEventLog;
    ImageView imgFinger;

    ProgressDialog uploading;
    ProgressDialog progress;

    byte[] Enroll_Template;
    byte[] Verify_Template;

    FingerData fingerData1;
    FingerData fingerData2;
    FingerData fingerData3;


    int mfsVer = 41;
    SharedPreferences settings;
    Context context;
    String usernamekey,pathimage,pathiso;
    CommonMethod.ScannerAction scannerAction = CommonMethod.ScannerAction.Capture;

    int minQuality = 60;
    int timeout = 10000;
    MFS100 mfs100 = null;

    public static String _testKey = "t7L8wTG/iv02t+pgYrMQ7tt8qvU1z42nXpJDfAfsW592N4sKUHLd8A0MEV0GRxH+f4RgefEaMZALj7mgm/Thc0jNhR2CW9BZCTgeDPjC6q0W";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerscanregister);
        context = fingerscanregister.this.getApplicationContext();
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        mfsVer = Integer.parseInt(settings.getString("MFSVer",
                String.valueOf(mfsVer)));

        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usernamekey = saved_values.getString("usernamekey", "null");

        FindFormControls();
        CommonMethod.DeleteDirectory();
        CommonMethod.CreateDirectory();


        pathiso = Environment.getExternalStorageDirectory()
                + "//FingerData/" + usernamekey +".iso";
        pathimage = Environment.getExternalStorageDirectory()
                + "//FingerData/" + usernamekey +".bmp";


        //Added by Milan Sheth on 19-Dec-2016
        PubVar.sharedPrefernceDeviceMode = context.getSharedPreferences(PubVar.strSpDeviceKey, Context.MODE_PRIVATE);

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
        if (uploading.isShowing()) {
            uploading.dismiss();
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
        imgFinger = (ImageView) findViewById(R.id.imgFinger);

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
            case R.id.btnStartCapture:
                scanfinger();
                break;
            case R.id.uploadbutton:
                uploadVideo(pathimage);
                uploadVideo(pathiso);
                break;
            case R.id.btnForLoop:
                Toast.makeText(fingerscanregister.this,
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



    private void uploadVideo(final String selectedPath) {
        class UploadVideo extends AsyncTask<Void, Void, String> {



            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uploading = ProgressDialog.show(fingerscanregister.this, "Uploading File", "Please wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                uploading.dismiss();
                Toast.makeText(fingerscanregister.this, "<b>Uploaded at <a href='" + s + "'>" + s + "</a></b>", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            protected String doInBackground(Void... params) {
                Upload u = new Upload();
                String msg = u.uploadVideo(selectedPath);
                return msg;
            }
        }
        UploadVideo uv = new UploadVideo();
        uv.execute();
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


    public void scanfinger(){

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
        WriteFile(usernamekey+".bmp", fingerData1.FingerImage());
        WriteFile(usernamekey+".iso", fingerData1.ISOTemplate());

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
                if (strDeviceMode.toLowerCase().equalsIgnoreCase("public")){
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

    private void showSuccessLog(){
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

    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

}
