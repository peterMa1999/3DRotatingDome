package com.hez.peter.rotating;

import android.content.Context;

/**
 * Created by Peter on 2018/4/28.
 */

public interface RotatingContract {

    interface View extends ImpBaseView {

        void Fail();

        void onStartDownload(String url, String filename, Context context);

        void onProgress(int progress);

        void onFinishDownload();

        void onFail(String errorInfo);

    }

    interface Presenter extends ImpBasePresenter {

        void onStartDownload(String url, String filename, Context context);

        void stopDownload();

        void loadCache();
    }

}
