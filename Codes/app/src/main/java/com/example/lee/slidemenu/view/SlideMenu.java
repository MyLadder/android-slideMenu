package com.example.lee.slidemenu.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.example.lee.slidemenu.R;

/**
 * TODO: document your custom view class.
 */
public class SlideMenu extends HorizontalScrollView {


    private LinearLayout mWapper;   //外层的横向LinearLayout
    private ViewGroup mMenu;        //左侧菜单栏
    private ViewGroup mContent;     //内容区域
    private int mScreenWidth;       //屏幕宽度
    private int mMenuRightPadding;  //menu距离右侧的距离，单位为px
    private boolean mMeasureOnce = false;   //是否已经测量过
    private boolean mIsMenuOpen = false;    //菜单是否打开
    private int mMenuWidth;         //菜单栏的宽度
    //右边距的默认值150dp,转换为px
    private int mDefaultRightPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,150,
                    getContext().getResources().getDisplayMetrics());


    public SlideMenu(Context context) {
        this(context,null);
    }

    /**
     * 未使用自定义属性时调用
     * @param context
     * @param attrs
     */
    public SlideMenu(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    /**
     * 使用自定义属性时直接调用
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SlideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //得到屏幕宽度
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
         wm.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {


        //加载自定义属性
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SlideMenu, defStyle, 0);

        mMenuRightPadding = a.getDimensionPixelSize(R.styleable.SlideMenu_rightPadding,
                            mDefaultRightPadding);
        a.recycle();
    }

    /**
     * 设置子View和自己的宽和高
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mMeasureOnce) {
            //初始化控件
            mWapper =(LinearLayout)getChildAt(0);
            mMenu = (ViewGroup)mWapper.getChildAt(0);
            mContent =(ViewGroup)findViewById(R.id.content);
            mMenuWidth = mScreenWidth - mMenuRightPadding;
            mMenu.getLayoutParams().width = mMenuWidth;
            mContent.getLayoutParams().width = mScreenWidth;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    /**
     * 通过设置偏移量将menu隐藏
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed) {
            this.scrollTo(mMenuWidth, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action){
            case MotionEvent.ACTION_UP:
                //隐藏在左边的宽度
                int scrollX = getScrollX();
                if (scrollX>=mMenuWidth/2){
                    this.smoothScrollTo(mMenuWidth,0);
                    mIsMenuOpen = false;
                }else{
                    this.smoothScrollTo(0,0);
                    mIsMenuOpen = true;
                }
                return true;

        }
        return super.onTouchEvent(ev);
    }

    /**
     *设置缩放动画
     * @param l Current horizontal scroll origin.
     * @param t Current vertical scroll origin.
     * @param oldl Previous horizontal scroll origin.
     * @param oldt Previous vertical scroll origin.
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //调用属性动画，设置menu的TranslationX使其一直与屏幕紧贴,乘一个系数使其初始时一部分在屏幕左侧
        mMenu.setTranslationX(l*0.8f);
        //l的变化范围是MenuWidth ~ 0，所以l/MenuWidth变化范围是1 ~ 0
        float scale = l*1.0f/mMenuWidth;
        //我们的目标是右侧内容区实现1 ~ 0.8的缩放，所以对scale进行变换
        float contentScale = 0.2f*scale + 0.8f;
        //设置Content缩放的中心点为左侧竖直边的中点,使其更加跟手
        mContent.setPivotX(0);
        mContent.setPivotY(mContent.getHeight()/2);
        mContent.setScaleX(contentScale);
        mContent.setScaleY(contentScale);

        //menu有透明度和缩放的变化，缩放为0.8 ~ 1:-0.2*scale + 1
        //透明度为0.4 ~ 1.0 : -0.6*scale + 1
        float menuScale = -0.2f * scale + 1f;
        float alphaScale = -0.6f * scale + 1f;

        mMenu.setScaleX(menuScale);
        mMenu.setScaleY(menuScale);
        mMenu.setAlpha(alphaScale);
        //this.getBackground().setAlpha((int)(alphaScale*255.0f));



    }

    /**
     * 打开菜单
     */
    public void openMenu(){
        if (mIsMenuOpen)    return;
        this.smoothScrollTo(0,0);
        mIsMenuOpen = true;
    }

    /**
     * 关闭菜单
     */
    public void closeMenu(){
        if (!mIsMenuOpen)   return;
        this.smoothScrollTo(mMenuWidth,0);
        mIsMenuOpen = false;
    }

    /**
     * 切换菜单状态
     */
    public void toggleMenu(){
        if (mIsMenuOpen){
            closeMenu();
        }else{
            openMenu();
        }
    }
}