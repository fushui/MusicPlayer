package feiyu.com.musicplayer_view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;


/**
 * Created by feiyu on 2015/10/5.
 */
public class SlidingMenu extends HorizontalScrollView {
    private LinearLayout mWrapper;
    private ViewGroup mMenu;
    private ViewGroup mContent;

    private int mScreenWidth;
    private int mMenuWidth;
    private int mMenuRightPadding = 80;

    private boolean isOpen;
    private boolean once;

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /*TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlidingMenu,
                defStyleAttr,0);
        int count = a.getIndexCount();
        for(int i = 0; i < count; i ++){
            int attr = a.getIndex(i);
            switch (attr){
                case R.styleable.SlidingMenu_rightPadding:
                    mMenuRightPAdding = a.getDimensionPixelSize(attr,(int)TypedValue
                        .applyDimension(TypedValue
                                    .COMPLEX_UNIT_DIP,50,
                            getResources().getDisplayMetrics()));
                    break;
                default:
                    break;
            }
        }

        a.recycle();*/

        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetris = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetris);
        mScreenWidth = outMetris.widthPixels;
    }

    public SlidingMenu(Context context) {
        this(context,null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (!once) {
            mWrapper = (LinearLayout)getChildAt(0);
            mMenu = (ViewGroup)mWrapper.getChildAt(0);
            mContent = (ViewGroup)mWrapper.getChildAt(1);

            mMenuRightPadding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, mMenuRightPadding, mContent
                            .getResources().getDisplayMetrics());
            mMenuWidth = mMenu.getLayoutParams().width = mScreenWidth - mMenuRightPadding;
            mContent.getLayoutParams().width = mScreenWidth;
            once = true;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed){
           this.scrollTo(mMenuWidth,0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action){
            case MotionEvent.ACTION_UP:
                int scrollX = this.getScrollX();
                if(scrollX >= mMenuWidth / 2){
                    this.smoothScrollTo(mMenuWidth,0);
                    isOpen = false;
                }else{
                    this.smoothScrollTo(0,0);
                    isOpen = true;
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    public void openMenu() {
        if(isOpen) return;
        this.smoothScrollTo(0,0);
        isOpen = true;
    }

    public void closeMenu() {
       if(!isOpen) return;
        this.smoothScrollTo(mMenuWidth,0);
        isOpen = false;
    }

    public void toggle() {
        if (!isOpen){
            openMenu();
        }else {
            closeMenu();
        }
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        float scale = l * 1.0f/mMenuWidth;
        ViewHelper.setTranslationX(mMenu, scale * mMenuWidth * 0.8f );
        float rightScale = 0.7f + 0.3f * scale;
        float leftScale = 1.0f - 0.9f * scale;
        float leftAlpha = 1.0f - 0.9f * scale;



        ViewHelper.setScaleX(mMenu, leftScale);
        ViewHelper.setScaleY(mMenu, leftScale);
        ViewHelper.setAlpha(mMenu, leftAlpha);


        ViewHelper.setPivotX(mContent, 0);
        ViewHelper.setPivotY(mContent, mContent.getHeight() / 2);
        ViewHelper.setScaleX(mContent, rightScale);
        ViewHelper.setScaleY(mContent, rightScale);

    }
}
