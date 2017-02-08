package com.kisen.personalizedslideindicator.indicatorview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kisen.personalizedslideindicator.R;

/**
 * 带有指示器的自定义RecyclerView，目前只支持垂直列表
 * Created by huang on 2017/2/8.
 */
public class IndicatorView extends RecyclerView {

    private static final int DEFAULT_IN_ANIMATION = -1;
    private static final int DEFAULT_OUT_ANIMATION = -1;
    private View mScrollPanel;

    private int mWidthMeasureWSpec;
    private int mHeightMeasureWSpec;

    private OnPositionChangedListener positionChangedListener;

    public float mScrollBarPanelPosition = 0;

    public Animation mInAnimation = null;
    public Animation mOutAnimation = null;
    //定义指示器在RecyclerView中y轴的高度
    public float thumbOffset;
    private int mLastPosition = -1;
    private int layoutId;

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /**
         * 1.监听滑动 addOnScrollListener
         *  回调onScroll()
         */
        OnScrollListener indicatorScrollListener = new OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //监听滑动
                if (mScrollPanel == null || positionChangedListener == null)
                    return;
                //不断修改指示器的Y坐标
                /**
                 * computeVerticalScrollRange()--纵向滚动条滑动范围（0-10000）
                 * computeVerticalScrollOffset()--滑动条的纵向幅度位置，比如屏幕中间5000
                 * computeVerticalScrollExtent()--滑动条纵向滚动范围内它自身的厚度占用的幅度
                 */
                //1滑块厚度，思路：滑块的厚度/IndicatorView的高度=extent/range;
                float height = computeVerticalScrollExtent() * getMeasuredHeight() * 1f / computeVerticalScrollRange();
                //2得到系统滑块正中间的Y坐标
                thumbOffset = computeVerticalScrollOffset() * height * 1f / computeVerticalScrollExtent() + height / 2;
                mScrollBarPanelPosition = thumbOffset - mScrollPanel.getMeasuredHeight() / 2;
                //不断修改摆放位置
                int left = getMeasuredWidth() - mScrollPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
                mScrollPanel.layout(
                        left,
                        (int) mScrollBarPanelPosition,
                        left + mScrollPanel.getMeasuredWidth(),
                        (int) mScrollBarPanelPosition + mScrollPanel.getMeasuredHeight());

                int firstPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                //监听指示器在哪个条目位置
                for (int i = 0; i < getChildCount(); i++) {
                    View childView = getChildAt(i);
                    //判断当前滑块是否在i对应的位置
                    if (childView != null) {
                        if (thumbOffset > childView.getTop() && thumbOffset < childView.getBottom()) {
                            if (mLastPosition != firstPosition + i) {
                                mLastPosition = firstPosition + i;
                                //回调
                                positionChangedListener.onPositionChanged(IndicatorView.this, mLastPosition, mScrollPanel);
                                //因为指示器内容可能发生变化，宽高会发生变化，重新计算
                                measureChild(mScrollPanel, mWidthMeasureWSpec, mHeightMeasureWSpec);
                            }
                            break;
                        }
                    }
                }
            }
        };
        addOnScrollListener(indicatorScrollListener);
        initIndicator(context, attrs);
    }

    private void initIndicator(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        layoutId = a.getResourceId(R.styleable.IndicatorView_scrollBarPanel, -1);
        int inAnimator = a.getResourceId(R.styleable.IndicatorView_scrollBarPanelInAnimation, DEFAULT_IN_ANIMATION);
        int outAnimator = a.getResourceId(R.styleable.IndicatorView_scrollBarPanelOutAnimation, DEFAULT_OUT_ANIMATION);
        a.recycle();

        mInAnimation = AnimationUtils.loadAnimation(context, inAnimator);
        mOutAnimation = AnimationUtils.loadAnimation(context, outAnimator);
        long durationMillis = ViewConfiguration.getScrollBarFadeDuration();
        mInAnimation.setDuration(durationMillis);
        mOutAnimation.setDuration(durationMillis);
        mOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //动画结束时隐藏指示器
                if (mScrollPanel != null) {
                    mScrollPanel.setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
//        if (layout instanceof LinearLayoutManager) {
//            mOrientation = ((LinearLayoutManager) layout).getOrientation();
//        } else {
//            mOrientation = ((StaggeredGridLayoutManager) layout).getOrientation();
//        }
        setScrollBarPanel(layoutId);
    }

    private void setScrollBarPanel(int layoutId) {
        mScrollPanel = LayoutInflater.from(getContext()).inflate(layoutId, this, false);
        mScrollPanel.setVisibility(GONE);
        //提醒自定义的View重新测量、摆放、绘制
        requestLayout();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //绘制自己的试图
        super.dispatchDraw(canvas);
        //绘制自定义指示器---在原来的容器最上层绘制自定义指示器
        if (mScrollPanel != null) {
            drawChild(canvas, mScrollPanel, getDrawingTime());
        }
    }

    @Override
    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        //监听系统的滑动条显示情况
        boolean awakenScrollBars = super.awakenScrollBars(startDelay, invalidate);
        if (awakenScrollBars && mScrollPanel != null) {
            if (mScrollPanel.getVisibility() == View.GONE) {
                mScrollPanel.setVisibility(VISIBLE);
                if (mInAnimation != null)
                    mScrollPanel.startAnimation(mInAnimation);
            }
            //过一小段时间就消失
            handler.removeCallbacks(runnable);
            handler.postAtTime(runnable, startDelay + AnimationUtils.currentAnimationTimeMillis());
        }
        return awakenScrollBars;
    }

    Handler handler = new Handler();

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //执行消失动画
            if (mOutAnimation != null) {
                mScrollPanel.startAnimation(mOutAnimation);
            }
        }
    };

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        //测试RecyclerView所有子控件
        super.onMeasure(widthSpec, heightSpec);
        mWidthMeasureWSpec = widthSpec;
        mHeightMeasureWSpec = heightSpec;
        //测试自定义指示器
        if (mScrollPanel != null)
            measureChild(mScrollPanel, widthSpec, heightSpec);//父类测量子控件方法
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //摆放RecyclerView中所有子控件
        super.onLayout(changed, l, t, r, b);
        if (mScrollPanel == null)
            return;
        int left = getMeasuredWidth() - mScrollPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
        //摆放自定义指示器
        mScrollPanel.layout(
                left,
                (int) mScrollBarPanelPosition,
                left + mScrollPanel.getMeasuredWidth(),
                (int) mScrollBarPanelPosition + mScrollPanel.getMeasuredHeight());

    }

    /**
     * 指示器位置变化监听
     */
    public interface OnPositionChangedListener {

        /**
         * 指示器位置变化回调
         */
        void onPositionChanged(IndicatorView indicatorView, int position, View scrollBarPanel);
    }

    public void setPositionChangedListener(OnPositionChangedListener positionChangedListener) {
        this.positionChangedListener = positionChangedListener;
    }
}
