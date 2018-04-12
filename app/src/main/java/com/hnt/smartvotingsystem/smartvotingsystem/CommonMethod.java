package com.hnt.smartvotingsystem.smartvotingsystem;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonMethod {

	public static String Storagepath = Environment
			.getExternalStorageDirectory().getPath() + "/MFS100/";

	public static boolean DeleteDirectory() {
		try {
			File file = new File(Storagepath);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	public static boolean CreateDirectory() {
		try {
			File file = new File(Storagepath);

			if (!file.exists()) {
				if (!file.mkdirs()) {
					return false;
				}
			}
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	@SuppressLint("SimpleDateFormat")
	public static void writeLog(String strLog) {

		try {
			boolean isNewFile = false;

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
			String FileNameFormate = dateFormat.format(new Date());
			String FileName = Storagepath + "/Log_" + FileNameFormate + ".txt";

			SimpleDateFormat logTimeFormat = new SimpleDateFormat(
					"yyyy-MM-dd-HH-mm-ss");
			String logTime = logTimeFormat.format(new Date());
			File dir = new File(Storagepath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File logFile = new File(FileName);
			if (!logFile.exists()) {
				isNewFile = true;
				logFile.createNewFile();
			}
			FileOutputStream fOut;
			OutputStreamWriter myOutWriter;
			fOut = new FileOutputStream(logFile, true);
			myOutWriter = new OutputStreamWriter(fOut);
			if (isNewFile) {
				myOutWriter.append(logTime + "\n" + strLog);
			} else {
				myOutWriter.append("\n" + logTime + "\n" + strLog);
			}
			myOutWriter.flush();
			myOutWriter.close();

		} catch (Exception ex) {

		}

	}

	public enum ScannerAction {
		Capture, Verify
	}
}
