package com.hez.peter.rotating;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.laojiang.retrofithttp.weight.downfilesutils.FinalDownFiles;
import com.laojiang.retrofithttp.weight.downfilesutils.action.FinalDownFileResult;
import com.laojiang.retrofithttp.weight.downfilesutils.downfiles.DownInfo;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Peter on 2018/4/27.
 */

public class RotatingPresenter implements RotatingContract.Presenter {

    private RotatingContract.View mFrag;
    private int pageindex = 0, pagesize = 100;
    private FinalDownFiles finalDownFiles;

    @Override
    public void onStartDownload(String url, final String filename, final Context context) {
        finalDownFiles = new FinalDownFiles(false, context, url,
                getOutUrlStr(filename), new FinalDownFileResult() {
            @Override
            public void onLoading(long readLength, long countLength) {
                super.onLoading(readLength, countLength);
//                progressBar.setMax((int) countLength);
                BigDecimal x = BigDecimal.valueOf(countLength);
                BigDecimal d = BigDecimal.valueOf(readLength).divide(x, new MathContext(2));
                if (mFrag == null){
                    return;
                }
                //更新下载进度条
                if ((int) Math.floor(Float.parseFloat(d.toString()) * 100) != 0) {
                    mFrag.onProgress((int) Math.floor(Float.parseFloat(d.toString()) * 100));
                }
                if (readLength == 0) {
                    //下载成功
                    mFrag.onFinishDownload();
                    //解压到当前产品的文件夹
                        ZipExtractorTask task = new ZipExtractorTask(Constants.PICTHER_PRODUCT_PATH + filename + ".zip", Constants.PICTHER_PRODUCT_PATH + filename, context, true , 0);
                        task.execute();
                }
//                progressBar.setProgress((int) readLength);
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                finalDownFiles.setStop();
            }

            @Override
            public void onErroe(String message, int code) {
                super.onErroe(message, code);
                Log.i("出错==", message.toString() + "\n" + code);
                mFrag.onFail("Download Fail");
                //当网络连接断开的时候 code为-5
                if (code == -5) {
                    finalDownFiles.setPause();
                }
            }

        });
        DownInfo downInfo = finalDownFiles.getDownInfo();
        downInfo.setConnectionTime(3);

    }

    @Override
    public void stopDownload() {
        if (finalDownFiles != null){
            finalDownFiles.stopAll();
        }
    }

    @Override
    public void loadCache() {

    }

    @NonNull
    private String getOutUrlStr(String filename) {
        return Constants.PICTHER_PRODUCT_PATH + filename + ".zip";
    }

    @Override
    public void bindView(ImpBaseView view) {
        mFrag = (RotatingContract.View) view;
    }

    @Override
    public void unbindView() {
    }

    @Override
    public void onDestory() {
        if (finalDownFiles != null) {
            finalDownFiles.stopAll();
        }
        mFrag = null;
    }

}
