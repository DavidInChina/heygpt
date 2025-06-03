package com.common.basesdk.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.Objects;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;


public class BaseFragDialog extends DialogFragment {

    public static final Handler HANDLER = new Handler();
    private Object mView;
    private View mRootView;
    private float mAlpha;
    private boolean mAutoDismiss;
    private boolean mCancelable;
    private Window window;
    private int mAnimation;
    private int mGravity;
    private SparseArray<OnListener> mClickArray;
    private SparseArray<String> mSetText;

    public BaseFragDialog() {
    }

    BaseFragDialog(Object view, float alpha, boolean autoDismiss, boolean cancelable, int animation, int gravity) {
        this.mView = view;
        this.mAlpha = alpha;
        this.mAutoDismiss = autoDismiss;
        this.mCancelable = cancelable;
        this.mAnimation = animation;
        this.mGravity = gravity;
        mClickArray = new SparseArray<>();
        mSetText = new SparseArray<>();
    }

    public static BaseFragDialog newInstance(Object view, float alpha,
                                             boolean mAutoDismiss, boolean cancelable,
                                             int animation, int gravity) {
        return new BaseFragDialog(view, alpha, mAutoDismiss, cancelable, animation, gravity);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        window = Objects.requireNonNull(getDialog()).getWindow();
        if (window != null) {
            if (mView instanceof Integer) {
                this.mRootView = inflater.inflate((Integer) mView, window.findViewById(android.R.id.content), false);
            } else if (mView instanceof View) {
                this.mRootView = (View) mView;
            } else {
                throw new NullPointerException("Not Layout File ");
            }
            create();
        }
        return mRootView;
    }

    public static DialogBuilder<BaseFragDialog> Builder() {
        return new DialogBuilder();
    }

    /**
     * 设置背景遮盖层开关
     */
    public void setBackgroundDimEnabled(boolean enabled) {
        if (window != null) {
            if (enabled) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        }
    }

    /**
     * 设置背景遮盖层的透明度（前提条件是背景遮盖层开关必须是为开启状态）
     */
    public void setBackgroundDimAmount(float dimAmount) {
        if (window != null) {
            window.setDimAmount(dimAmount);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSetText != null) {
            mSetText.clear();
            mSetText = null;
        }
        if (mClickArray != null) {
            mClickArray.clear();
            mClickArray = null;
        }
        if (mRootView != null) {
            mRootView = null;
        }
    }

    /**
     * 对点击事件进行处理
     */
    private final class ViewOnClick implements View.OnClickListener {
        private final BaseFragDialog dialog;
        private final OnListener listener;

        ViewOnClick(BaseFragDialog dialog, OnListener listener) {
            this.dialog = dialog;
            this.listener = listener;
        }

        @Override
        public void onClick(View view) {
            if (!mAutoDismiss) {
                listener.onClick(dialog, view);
            }
        }
    }

    /**
     * 对事件进行监听，
     */
    public interface OnListener {
        void onClick(BaseFragDialog dialog, View view);
    }

    /**
     * 设置 文本
     *
     * @param Id      id
     * @param strings 内容
     */
    public BaseFragDialog setText(@IdRes int Id, String strings) {
        mSetText.put(Id, strings);
        return this;
    }

    /**
     * 监听事件
     */
    public BaseFragDialog setListener(int id, OnListener listener) {
        mClickArray.put(id, listener);
        return this;
    }


    private void setLocation() {
        // 隐藏标题栏, 不加弹窗上方会一个透明的标题栏占着空间
        if (null != getDialog() && null != getDialog().getWindow()) {
            try {
                getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            } catch (Exception ignored) {

            }
            // 必须设置这两个,才能设置宽度
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
            // 设置宽度
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
            attributes.alpha = mAlpha;
            attributes.gravity = mGravity;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setAttributes(attributes);
        }

    }

    /**
     * 延时发送，在指定的时间执行
     */
    public void postAtTime(long uptimeMillis, Runnable run) {
        HANDLER.postDelayed(run, uptimeMillis);
    }

    private void create() {
        setLocation();
        initView(mRootView);
        setCancelable(mCancelable);
        window.setWindowAnimations(mAnimation);
        for (int i = 0; i < mSetText.size(); i++) {
            View viewById = mRootView.findViewById(mSetText.keyAt(i));
            if (viewById instanceof AppCompatTextView) {
                ((AppCompatTextView) viewById).setText(mSetText.valueAt(i));
            } else if (viewById instanceof AppCompatButton) {
                ((AppCompatButton) viewById).setText(mSetText.valueAt(i));
            }
        }

        for (int i = 0; i < mClickArray.size(); i++) {
            mRootView.findViewById(mClickArray.keyAt(i))
                    .setOnClickListener(new ViewOnClick(this, mClickArray.valueAt(i)));
        }
    }

    /**
     * 空实现，如果dialog的逻辑过于复杂，则可以继承此类，实现此方法。
     * 这个方法可用于绑定 view 进行一些初始化等操作
     */
    public void initView(View view) {

    }
}
