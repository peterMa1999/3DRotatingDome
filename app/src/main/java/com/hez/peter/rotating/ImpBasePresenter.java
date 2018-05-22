package com.hez.peter.rotating;

/**
 * Presenter 基础接口，用于初始化和释放资源避免引起内存泄漏
 */

public interface ImpBasePresenter {
    void bindView(ImpBaseView view);

    void unbindView();

    void onDestory();
}
