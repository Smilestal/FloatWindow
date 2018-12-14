package com.crystal.floatwindow;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

public class FloatWindowUtils {

    public static Boolean isShown = false;
    private static WindowManager windowManager;
    private static int mSlop;
    private static boolean isMove = false;  //是否在移动
    private static View mView = null;

    public static void showWindow(Context context, View.OnClickListener listener) {
        if (isShown) hideWindow();
        isShown = true;

        windowManager = (WindowManager) context.getSystemService(Application.WINDOW_SERVICE);
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        boolean permission = PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission("android.permission.SYSTEM_ALERT_WINDOW", context.getPackageName());
        if (permission) {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            wmParams.token = ((Activity) context).getWindow().getDecorView().getWindowToken();
        }

        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.START | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = Util.getScreenWidth(context);
        wmParams.y = (int) (Util.getScreenHeight(context) * 0.7);

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mView = setupView(context, wmParams, listener);
        windowManager.addView(mView, wmParams);
    }

    public static void visible() {
        if (mView != null) {
            mView.setVisibility(View.VISIBLE);
        }
    }

    public static void invisible() {
        if (mView != null) {
            mView.setVisibility(View.INVISIBLE);
        }
    }

    private static void hideWindow() {
        if (isShown && null != mView) {
            windowManager.removeView(mView);
            isShown = false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private static View setupView(final Context context, WindowManager.LayoutParams wmParams, View.OnClickListener listener) {
        View view = View.inflate(context, R.layout.float_layout, null);
        view.setOnClickListener(listener);

        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        view.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;  //起始点
            float downX, downY;
            int finalMoveX;  //最后通过动画将mView的X轴坐标移动到finalMoveX

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        downX = event.getRawX();
                        downY = event.getRawY();
                        isMove = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        wmParams.x = (int) (event.getRawX() - startX);
                        //这里修复了刚开始移动的时候，悬浮窗的y坐标是不正确的，要减去状态栏的高度，可以将这个去掉运行体验一下
                        wmParams.y = (int) (event.getRawY() - startY - getStatusBarHeight(context));

                        //更新mView 的位置
                        if (view.getParent() != null) {
                            windowManager.updateViewLayout(view, wmParams);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        isMove = (Math.abs(event.getRawX() - downX) > mSlop) || (Math.abs(event.getRawY() - downY) > mSlop);

                        //判断mView是在Window中的位置，以中间为界
                        if (wmParams.x + view.getMeasuredWidth() / 2 >= windowManager.getDefaultDisplay().getWidth() / 2) {
                            finalMoveX = windowManager.getDefaultDisplay().getWidth() - view.getMeasuredWidth();
                        } else {
                            finalMoveX = 0;
                        }

                        //使用动画移动mView
                        ValueAnimator animator = ValueAnimator.ofInt(wmParams.x, finalMoveX).setDuration(Math.abs(wmParams.x - finalMoveX));
                        animator.setInterpolator(new AccelerateDecelerateInterpolator());
                        animator.addUpdateListener((ValueAnimator animation) -> {
                            wmParams.x = (int) animation.getAnimatedValue();
                            if (view.getParent() != null) {
                                windowManager.updateViewLayout(view, wmParams);
                            }
                        });
                        animator.start();
                        break;
                }
                return isMove;
            }
        });
        return view;
    }

    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
