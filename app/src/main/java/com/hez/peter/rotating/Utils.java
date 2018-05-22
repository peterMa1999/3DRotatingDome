package com.hez.peter.rotating;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Utils {


    private static final DecimalFormat df = new DecimalFormat("######0.00");
    private static final DecimalFormat df2 = new DecimalFormat("######0.000");

    public static String toJSON(String[] keys, Object[] values)
    {
        Gson g = new GsonBuilder().disableHtmlEscaping().create();
        LinkedHashMap<String, Object> map=new LinkedHashMap<String, Object>();
        if(keys.length==values.length)
        {
            for(int i=0;i<keys.length;i++)
            {
                map.put(keys[i], values[i]);
            }
        }
        String json = g.toJson(map);
        return json;
    }

    public static byte[] hexStringToBytes(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        int len=str.length()/2;
        byte[] buf=new byte[len];
        for(int i=0;i<len;i++){
            String sub = str.substring(2*i, 2*i+2);
            buf[i] = (byte) Integer.parseInt(sub, 16);
        }
        return buf;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int i = 0; i < byteNum.length; i++) {
            num <<= 8;
            num |= (byteNum[i] & 0xff);
        }
        return num;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static String bytesToString(byte[] buf){
        StringBuffer sb = new StringBuffer();
        for(int i=1;i<buf.length;i++){
            String s = Integer.toHexString(buf[i]& 0xff);
            if(s.length()==1){
                s="0"+s;
            }
            sb.append(s);
        }
        return sb.toString();
    }


    public static void openGPSSettings(Context context) {
        //获取GPS现在的状态（打开或是关闭状态）
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled( context.getContentResolver(), LocationManager.GPS_PROVIDER );

        if(gpsEnabled)
        {
            //关闭GPS
            Settings.Secure.setLocationProviderEnabled( context.getContentResolver(), LocationManager.GPS_PROVIDER, false );
        }
        else
        {
            //打开GPS  www.2cto.com
            Settings.Secure.setLocationProviderEnabled( context.getContentResolver(), LocationManager.GPS_PROVIDER, true);

        }
    }


    public static String conver(double d) {
        String format = df.format(d);
        return format;
    }

    public static String conver2(double d) {
        String format = df2.format(d);
        return format;
    }

    public static void hideInputMethod(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isConnected(Context c) {
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connMgr.getActiveNetworkInfo();
        return (null != info && info.isAvailable());
    }

    public static final boolean ping() {
        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }

    public static boolean isBackground(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses)
        {
            if (appProcess.processName.equals(context.getPackageName()))
            {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        return false;
    }

    public static void hideKeyBoard(Activity context) {
        if (context != null && context.getCurrentFocus() != null) {
            ((InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(context.getCurrentFocus()
                                    .getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static String readAssert(Context context, String fileName){
        String jsonString="";
        String resultString="";
        try {
            InputStream inputStream=context.getResources().getAssets().open(fileName);
            byte[] buffer=new byte[inputStream.available()];
            inputStream.read(buffer);
            resultString=new String(buffer,"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }



    /**
     * final Activity activity  ：调用该方法的Activity实例
     * long milliseconds ：震动的时长，单位是毫秒
     */
    public static void Vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    /**
     * 从sd卡获取图片资源
     * @return
     */
    public static List<String> getImagePathFromSD(String fileurl) {
        // 图片列表
        List<String> imagePathList = new ArrayList<String>();
        // 得到sd卡内image文件夹的路径   File.separator(/)
        String filePath = fileurl;
        // 得到该路径文件夹下所有的文件
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            // 得到该路径子文件夹下所有的文件
            File fileAll_chid = new File(file.getPath());
            File[] files_chid = fileAll_chid.listFiles();
            for (int j = 0; j < files_chid.length; j++) {
                File file_chid = files_chid[j];
                if (checkIsImageFile(file_chid.getPath())) {
                    imagePathList.add(file_chid.getPath());
                }

            }
//            if (checkIsImageFile(file.getPath())) {
//                imagePathList.add(file.getPath());
//            }
        }
        // 返回得到的图片列表
        return imagePathList;
    }

    /**
     * 检查扩展名，得到图片格式的文件
     * @param fName  文件名
     * @return
     */
    @SuppressLint("DefaultLocale")
    private static boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png")
                || FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
