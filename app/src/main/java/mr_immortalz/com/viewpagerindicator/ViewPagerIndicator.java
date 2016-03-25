package mr_immortalz.com.viewpagerindicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by asus on 2016/3/22.
 */
public class ViewPagerIndicator extends LinearLayout {
    private ViewPager mViewPager;

    private int width;
    private int height;
    private int visibleItemCount = 3;
    private int itemCount = 3;

    //绘制框框
    private Paint paint;
    private float mWidth = 0;
    private float mHeight = 0;
    private float mLeft = 0;
    private float mTop = 0;
    private float radiusX = 10;
    private float radiusY = 10;
    private int mPadding = 8;

    private List<String> mDatas;
    private boolean isSetData = false;
    private Context context;
    private int currentPosition;
    private boolean isAutoSelect = false;//判断是否进行切换
    private float rebounceOffset;

    public ViewPagerIndicator(Context context) {
        super(context);
        this.context = context;
        init();
    }


    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();

    }

    private void init() {
        LogUtil.m();
        this.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.white));
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        mWidth = width / visibleItemCount;
        mHeight = height;
        LogUtil.m("width " + width + "  height " + height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        LogUtil.m();
        super.onSizeChanged(w, h, oldw, oldh);
        if (isSetData) {
            isSetData = false;
            this.removeAllViews();
            //添加TextView
            for (int i = 0; i < mDatas.size(); i++) {
                TextView tv = new TextView(context);
                tv.setPadding(mPadding, mPadding, mPadding, mPadding);
                tv.setText(mDatas.get(i));
                LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                lp.width = width / visibleItemCount;
                lp.height = height;
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(getResources().getColor(R.color.font_red));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tv.setLayoutParams(lp);
                final int finalI = i;
                tv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mViewPager != null) {
                            mViewPager.setCurrentItem(finalI);
                        }
                    }
                });
                this.addView(tv);
            }
            setTitleColor();
        }

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //LogUtil.m();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //drawRoundRect需要的最低API是21
            canvas.drawRoundRect(mLeft + mPadding, mTop + mPadding, mLeft + mWidth - mPadding, mTop + mHeight - mPadding, radiusX, radiusY, paint);
        } else {
            canvas.drawRoundRect(new RectF(mLeft + mPadding, mTop + mPadding, mLeft + mWidth - mPadding, mTop + mHeight - mPadding), radiusX, radiusX, paint);
            //canvas.drawRect(mLeft + mPadding, mTop + mPadding, mLeft + mWidth - mPadding, mTop + mHeight - mPadding, paint);
        }


    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //ogUtil.m();
        super.dispatchDraw(canvas);
    }

    public void setViewPager(ViewPager viewpager, int position) {
        LogUtil.m();
        this.mViewPager = viewpager;
        this.currentPosition = position;
        if (mViewPager != null) {
            viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    LogUtil.m();
                    //当移动的是最左边item
                    if (isAutoSelect && currentPosition == 0) {
                        //滑动手松开时，让最左边（即第一个）item滑动到左边缘位置
                        if (positionOffset > rebounceOffset / 2) {
                            mLeft = (position + (positionOffset - rebounceOffset / 2) * 2) * mWidth;
                        } else if (positionOffset > rebounceOffset / 3 && positionOffset < rebounceOffset / 2) {
                            //让最左边（即第一个）item 向右回弹一部分距离
                            mLeft = (position + (rebounceOffset / 2) - positionOffset) * mWidth * 6 / 12;
                        } else {
                            //让最左边（即最后一个）item 向左回弹到边缘位置
                            mLeft = (position + positionOffset) * mWidth * 6 / 12;
                        }
                        invalidate();
                    } else if (isAutoSelect && currentPosition == itemCount - 1) {
                        //当移动的是最右边（即最后一个）item

                        //滑动手松开时，让最右边（即最后一个）item滑动到右边缘位置
                        if (positionOffset >= rebounceOffset && positionOffset < (1 - (1 - rebounceOffset) / 2)) {
                            //
                            mLeft = (position + positionOffset / (1 - (1 - rebounceOffset) / 2)) * mWidth;
                            //当item数大于visibleItem可见数，本控件(本质LinearLayout)才滚动
                            if (visibleItemCount < itemCount) {
                                scrollTo((int) (mWidth * positionOffset / (1 - (1 - rebounceOffset) / 2) + (position - visibleItemCount + 1) * mWidth), 0);
                            }
                            if ((mLeft + mWidth) > (getChildCount() * mWidth)) {
                                //当(mLeft + mWidth)大于最边缘的宽度时，设置
                                mLeft = (itemCount - 1) * mWidth;
                            }
                        } else if (positionOffset > (1 - (1 - rebounceOffset) / 2) && positionOffset < (1 - (1 - rebounceOffset) / 4)) {
                            //让最右边（即最后一个）item 向左回弹一部分距离

                            //当item数大于visibleItem可见数，且本控件未滚动到指定位置，则设置控件滚动到指定位置
                            if (visibleItemCount < itemCount && getScrollX() != (itemCount - visibleItemCount) * mWidth) {
                                scrollTo((int) ((itemCount - visibleItemCount) * mWidth), 0);
                            }
                            mLeft = (position + 1) * mWidth - (positionOffset - (1 - (1 - rebounceOffset) / 2)) * mWidth * 7 / 12;
                        } else {
                            //让最右边（即最后一个）item 向右回弹到边缘位置

                            //因为onPageScrolled 最后positionOffset会变成0，所以这里需要判断一下
                            //当positionOffset = 0 时，设置mLeft位置
                            if (positionOffset != 0) {
                                mLeft = (position + 1) * mWidth - (1.0f - positionOffset) * mWidth * 7 / 12;
                                if (mLeft > (itemCount - 1) * mWidth) {
                                    mLeft = (itemCount - 1) * mWidth;
                                }
                            } else {
                                mLeft = (itemCount - 1) * mWidth;
                            }

                        }
                        invalidate();
                    } else {
                        //当移动的是中间item
                        scrollTo(position, positionOffset);
                        rebounceOffset = positionOffset;
                    }
                    setTitleColor();
                }

                @Override
                public void onPageSelected(int position) {
                    LogUtil.m("position " + position);
                    currentPosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    LogUtil.m("state " + state);
                    if (state == 2) {
                        //当state = 2时，表示手松开，viewpager开启自动滑动
                        isAutoSelect = true;
                    }
                    if (state == 0) {
                        //当state = 0时，表示viewpager滑动停止
                        isAutoSelect = false;
                    }
                }
            });
        }
    }


    public void setViewPager(ViewPager viewpager) {
        LogUtil.m();
        setViewPager(viewpager, 0);
    }

    /**
     * 正常滑动
     * @param position
     * @param positionOffset
     */
    private void scrollTo(int position, float positionOffset) {
        //item数量大于可见item，linearlayout才滑动
        if (visibleItemCount < itemCount) {
            if (positionOffset > 0 && position > (visibleItemCount - 2)) {
                this.scrollTo((int) (mWidth * positionOffset + (position - visibleItemCount + 1) * mWidth), 0);
            }
        }
        mLeft = (position + positionOffset) * mWidth;
        invalidate();
    }

    /**
     * 设置字体颜色
     */
    private void setTitleColor() {
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                if (i == currentPosition) {
                    ((TextView) getChildAt(currentPosition)).setTextColor(getResources().getColor(R.color.font_red));
                } else {
                    ((TextView) getChildAt(i)).setTextColor(getResources().getColor(R.color.font_white));
                }
            }
        }
    }

    /**
     * 设置内容数据
     *
     * @param mDatas
     */
    public void setDatas(List<String> mDatas) {
        LogUtil.m();
        this.isSetData = true;
        this.mDatas = mDatas;
        this.itemCount = mDatas.size();
        if (itemCount < visibleItemCount) {
            visibleItemCount = itemCount;
        }

    }
}
