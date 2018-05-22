package com.hez.peter.rotating;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bigkoo.svprogresshud.SVProgressHUD;

import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RotatingActivity extends AppCompatActivity implements RotatingContract.View{

    @BindView(R.id.layout_produce_show)
    LinearLayout mLayout_produce_show;
    @BindView(R.id.img_produce)
    ImageView mImg_produce;

    // 当前显示的bitmap对象
    private static Bitmap bitmap;
    // 开始按下位置
    private int startX;
    // 当前位置
    private int currentX;
    // 当前图片的编号,初始为1
    private int scrNum = 1;
    // 图片的总数,初始为0
    private static int maxNum = 0;
    // 资源图片集合
    private String[] srcs = null;
    private SVProgressHUD mSVProgressHUD;
    //自动3D旋转消息标识
    private static final int MSG_WHIRLIGIG = 2;
    //开始自动旋转的消息标识
    private static final int MSG_START_WHIRLIGIG = 1;
    private RotatingContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotating);
        ButterKnife.bind(this);
        bindPresenter();
        mSVProgressHUD = new SVProgressHUD(this);
        //先重设了进度再显示，避免下次再show会先显示上一次的进度位置所以要先将进度归0
        mSVProgressHUD.getProgressBar().setProgress(0);

        /*
        接收解压完成的广播，更新图片显示
         */
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.sl.unzip");
        BroadcastReceiver mItemViewListClickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mSVProgressHUD.dismiss();
                show3Dphoto();
            }
        };
        broadcastManager.registerReceiver(mItemViewListClickReceiver, intentFilter);

        //查找该产品的图片资源是否下载到了手机本地
        File file = new File(Constants.PICTHER_PRODUCT_PATH + "tupian");
        //未找到图片文件夹就重新下载资源压缩包
        if (!file.exists()) {
            onStartDownload("http://adel.ifs.waltzcn.com/upload/201805/10/V27275.zip", "tupian", RotatingActivity.this);
        }
        //找到了图片文件夹就搜索该路径下的所有图片
        else {
            show3Dphoto();
        }


        //3D图布局触摸监听
        mLayout_produce_show.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                resetTipsTimer();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        currentX = (int) event.getX();
                        // 判断手势滑动方向，并切换图片
                        if (currentX - startX > 3) {
                            for (int i = 0; i < (currentX - startX) / 3; i++) {
                                modifySrcR();
                            }
                        } else if (currentX - startX < -3) {
                            for (int i = 0; i < (currentX - startX) / -3; i++) {
                                modifySrcL();
                            }
                        }
                        // 重置起始位置
                        startX = (int) event.getX();

                        break;

                }

                return true;
            }

        });
    }

    @Override
    public void bindPresenter() {
        if (mPresenter == null) {
            mPresenter = new RotatingPresenter();
        }
        mPresenter.bindView(this);
    }

    @Override
    public void unbindPresenter() {
        mPresenter.unbindView();
    }

    @Override
    public void destoryPresenter() {
        mPresenter.onDestory();
    }

    // 向右滑动修改资源
    private void modifySrcR() {
        if (srcs == null) {
            return;
        }
        if (scrNum > maxNum) {
            scrNum = 1;
        }
        if (scrNum > 0) {
            bitmap = Utils.getLoacalBitmap(srcs[scrNum - 1]); //从本地取图片(在cdcard中获取)
            mImg_produce.setImageBitmap(bitmap);
            scrNum++;
        }

    }

    // 向左滑动修改资源
    private void modifySrcL() {
        if (srcs == null) {
            return;
        }
        if (scrNum <= 0) {
            scrNum = maxNum;
        }
        if (scrNum <= maxNum) {
//            bitmap = BitmapFactory.decodeResource(getResources(),
//                    srcs[scrNum - 1]);
//            mImg_produce.setImageBitmap(bitmap);
            bitmap = Utils.getLoacalBitmap(srcs[scrNum - 1]); //从本地取图片(在cdcard中获取)
            mImg_produce.setImageBitmap(bitmap);
            scrNum--;
        }
    }

    @Override
    public void onStartDownload(String url, String filename, Context context) {
        mSVProgressHUD.showWithProgress("进度 " + 0 + "%", SVProgressHUD.SVProgressHUDMaskType.Black);
        mPresenter.onStartDownload(url, filename, context);
    }

    @Override
    public void onProgress(int progress) {
        mSVProgressHUD.getProgressBar().setProgress(progress);
        mSVProgressHUD.setText("下载资源" + progress + "%");
    }

    @Override
    public void onFinishDownload() {
        mSVProgressHUD.dismiss();
        mSVProgressHUD.showWithStatus("解压中...");
    }

    @Override
    public void onFail(String errorInfo) {
        show3Dphoto();
    }

    @Override
    public void Fail() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHandler.hasMessages(MSG_WHIRLIGIG)) {
            mHandler.removeMessages(MSG_WHIRLIGIG);
        }
        //如果要展示的3D产品图片数组为空，则不启动开始计时旋转
        if (srcs == null) {
            return;
        }
        //启动默认开始计时
        startTipsTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        //有其他操作时结束计时
        endTipsTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.stopDownload();
        if (mHandler.hasMessages(MSG_WHIRLIGIG)) {
            mHandler.removeMessages(MSG_WHIRLIGIG);
        }
    }

    //获取sd卡中的3D图片
    private void show3Dphoto() {
        try {
            //根据路径查找手机SD卡中该路径下的所有图片
            List<String> list = Utils.getImagePathFromSD(Constants.PICTHER_PRODUCT_PATH + "tupian");
            if (list.size() != 0) {
                //对图片路径列表进行排序，升序
                Collections.sort(list, Collator.getInstance(java.util.Locale.CHINA));
                //图片总数
                maxNum = list.size();
                //SD卡本地要展示的图片路径数组
                srcs = list.toArray(new String[list.size()]);
                //从本地取图片(在cdcard中获取)
                bitmap = Utils.getLoacalBitmap(srcs[scrNum]);
                mImg_produce.setImageBitmap(bitmap);
                startTipsTimer();
            } else {
                //如果搜索到的图片列表大小为0则重新下载资源
                onStartDownload("http://adel.ifs.waltzcn.com/upload/201805/10/V27275.zip", "tupian", RotatingActivity.this);
                Log.i("TAG","未找到3D图片");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mSVProgressHUD != null) {
                if (mSVProgressHUD.isShowing()) {
                    mSVProgressHUD.dismiss();
                }
            }
            Log.i("TAG","搜索图片失败");
        }
    }


    //handler中接受定时消息来更新展示的3D图片
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHIRLIGIG:
                    modifySrcR();
                    mHandler.sendEmptyMessageDelayed(MSG_WHIRLIGIG, 100);
                    break;
                case MSG_START_WHIRLIGIG:
                    mHandler.sendEmptyMessageDelayed(MSG_WHIRLIGIG, 100);
                    break;
            }

        }
    };

    private Runnable tipsShowRunable = new Runnable() {

        @Override
        public void run() {
            mHandler.obtainMessage(MSG_START_WHIRLIGIG).sendToTarget();
        }
    };

    //重置计时
    public void resetTipsTimer() {
        if (mHandler.hasMessages(MSG_WHIRLIGIG)) {
            mHandler.removeMessages(MSG_WHIRLIGIG);
        }
        mHandler.removeCallbacks(tipsShowRunable);
        mHandler.postDelayed(tipsShowRunable, 2000);
    }

    /**
     * <无操作时开始计时>
     * <功能详细描述>
     *
     * @see [类、类#方法、类#成员]
     */
    public void startTipsTimer() {
        if (mHandler.hasMessages(MSG_WHIRLIGIG)) {
            mHandler.removeMessages(MSG_WHIRLIGIG);
        }
        mHandler.postDelayed(tipsShowRunable, 2000);
    }

    /**
     * <结束当前计时,重置计时>
     * <功能详细描述>
     *
     * @see [类、类#方法、类#成员]
     */
    public void endTipsTimer() {
        if (mHandler.hasMessages(MSG_WHIRLIGIG)) {
            mHandler.removeMessages(MSG_WHIRLIGIG);
        }
        mHandler.removeCallbacks(tipsShowRunable);
    }

}
