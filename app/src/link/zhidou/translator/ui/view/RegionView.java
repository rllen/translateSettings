package link.zhidou.translator.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class RegionView extends View {
    private static final int LEFT = 0;
    private static final int TOP = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 3;
    private static final int CENTER = 4;
    /**
     * 画字
     */
    private Rect mBoundLeft;
    private Rect mBoundUp;
    private Rect mBoundRight;
    private Rect mBoundDown;
    private Rect mBoundCenter;
    private Paint mTextPaint;
    private String mTextLeft = "8";
    private String mTextUp = "8";
    private String mTextRight = "8";
    private String mTextDown = "8";
    private String mTextCenter = "8";
    private int mTextColor;
    private float mTextSize;
    /**
     * 画键盘
     */
    private Paint mPaint;
    private Paint mClickPaint;
    private Paint mBackPaint;
    private RectF mRectFBig;
    private RectF mRectFLittle;

    private Path mPathLeft;
    private Path mPathTop;
    private Path mPathRight;
    private Path mPathBottom;
    private Path mPathCenter;
    private Path mPathBack;

    private float mInitSweepAngle = 0;
    private float mBigSweepAngle = 88;
    private float mLittleSweepAngle = 86;

    private float mBigMarginAngle;
    private float mLittleMarginAngle;

    private List<Region> mList;

    private Region mAllRegion;

    private Region mRegionTop;
    private Region mRegionRight;
    private Region mRegionLeft;
    private Region mRegionBottom;
    private Region mRegionCenter;

    private int mRadius;
    private int mHollRadius;

    private int mClickFlag = -1;
    private int mWidth;
    private int mCurX, mCurY;
    private RegionViewClickListener mListener;
    private int width;
    private int height;

    public RegionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public RegionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public RegionView(Context context) {
        super(context);
        initView(context, null);
    }

    public void setListener(RegionViewClickListener mListener) {
        this.mListener = mListener;
    }

    private void initView(Context mContext, AttributeSet attrs) {
        //键盘
        mPaint = new Paint();
        mPaint.setStyle(Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#0f84de"));

        mClickPaint = new Paint(mPaint);
        mClickPaint.setColor(Color.parseColor("#0e75c4"));

        mBackPaint = new Paint(mPaint);
        mBackPaint.setColor(Color.parseColor("#FFFFFF"));

        mPathLeft = new Path();
        mPathTop = new Path();
        mPathRight = new Path();
        mPathBottom = new Path();
        mPathCenter = new Path();
        mPathBack = new Path();

        mList = new ArrayList<>();

        mRegionLeft = new Region();
        mRegionTop = new Region();
        mRegionRight = new Region();
        mRegionBottom = new Region();
        mRegionCenter = new Region();

        mBigMarginAngle = 90 - mBigSweepAngle;
        mLittleMarginAngle = 90 - mLittleSweepAngle;

        //初始化画笔 用来画字
        mBoundLeft = new Rect();
        mBoundUp = new Rect();
        mBoundRight = new Rect();
        mBoundDown = new Rect();
        mBoundCenter = new Rect();
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setColor(Color.parseColor("#FFFFFF"));
    }

    private void initPath() {
        mList.clear();
        mPathRight.addArc(mRectFBig, mInitSweepAngle - mBigSweepAngle / 2,
                mBigSweepAngle);
        mPathRight.arcTo(mRectFLittle, mInitSweepAngle + mLittleSweepAngle / 2,
                -mLittleSweepAngle);
        mPathRight.close();

        mRegionRight.setPath(mPathRight, mAllRegion);
        mList.add(mRegionRight);

        mPathBottom.addArc(mRectFBig, mInitSweepAngle - mBigSweepAngle / 2
                + mBigMarginAngle + mBigSweepAngle, mBigSweepAngle);
        mPathBottom.arcTo(mRectFLittle, mInitSweepAngle + mLittleSweepAngle / 2
                + mLittleMarginAngle + mLittleSweepAngle, -mLittleSweepAngle);
        mPathBottom.close();

        mRegionBottom.setPath(mPathBottom, mAllRegion);
        mList.add(mRegionBottom);

        mPathLeft.addArc(mRectFBig, mInitSweepAngle - mBigSweepAngle / 2 + 2
                * (mBigMarginAngle + mBigSweepAngle), mBigSweepAngle);
        mPathLeft.arcTo(mRectFLittle, mInitSweepAngle + mLittleSweepAngle / 2
                        + 2 * (mLittleMarginAngle + mLittleSweepAngle),
                -mLittleSweepAngle);
        mPathLeft.close();

        mRegionLeft.setPath(mPathLeft, mAllRegion);
        mList.add(mRegionLeft);

        mPathTop.addArc(mRectFBig, mInitSweepAngle - mBigSweepAngle / 2 + 3
                * (mBigMarginAngle + mBigSweepAngle), mBigSweepAngle);
        mPathTop.arcTo(mRectFLittle, mInitSweepAngle + mLittleSweepAngle / 2
                        + 3 * (mLittleMarginAngle + mLittleSweepAngle),
                -mLittleSweepAngle);
        mPathTop.close();

        mRegionTop.setPath(mPathTop, mAllRegion);
        mList.add(mRegionTop);

        mPathCenter.addCircle(0, 0, mRadius, Path.Direction.CW);
        mPathCenter.close();

        mPathBack.addCircle(0, 0, mHollRadius, Path.Direction.CW);

        mRegionCenter.setPath(mPathCenter, mAllRegion);
        mList.add(mRegionCenter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        width = onMeasureR(0, widthMeasureSpec);
        height = onMeasureR(1, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画键盘
        canvas.save();
        canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        canvas.drawPath(mPathBack, mBackPaint);
        canvas.drawPath(mPathRight, mPaint);
        canvas.drawPath(mPathBottom, mPaint);
        canvas.drawPath(mPathLeft, mPaint);
        canvas.drawPath(mPathTop, mPaint);
        canvas.drawPath(mPathCenter, mPaint);
        switch (mClickFlag) {
            case RIGHT:
                canvas.drawPath(mPathRight, mClickPaint);
                break;
            case BOTTOM:
                canvas.drawPath(mPathBottom, mClickPaint);
                break;
            case LEFT:
                canvas.drawPath(mPathLeft, mClickPaint);
                break;
            case TOP:
                canvas.drawPath(mPathTop, mClickPaint);
                break;
            case CENTER:
                canvas.drawPath(mPathCenter, mClickPaint);
                break;
            default:
                break;
        }
        canvas.restore();
        //画字 控件高度/2 + 文字高度/2,绘制文字从文字左下角开始,因此"+"
        mTextPaint.getTextBounds(mTextLeft, 0, mTextLeft.length(), mBoundLeft);
        float leftStartX = (getWidth() * 3) / 20 - mBoundLeft.width() / 2;
        int leftStartY = getHeight() / 2 + mBoundLeft.height() / 2;

        canvas.drawText(mTextLeft, leftStartX, leftStartY, mTextPaint);


        mTextPaint.getTextBounds(mTextUp, 0, mTextUp.length(), mBoundUp);
        float upStartX = getWidth() / 2 - mBoundUp.width() / 2;
        int upStartY = (getHeight() * 3) / 20 + mBoundUp.height() / 2;

        canvas.drawText(mTextUp, upStartX, upStartY, mTextPaint);


        mTextPaint.getTextBounds(mTextRight, 0, mTextRight.length(), mBoundRight);
        float rightStartX = (getWidth() * 17) / 20 - mBoundRight.width() / 2;
        int rightStartY = getHeight() / 2 + mBoundRight.height() / 2;

        canvas.drawText(mTextRight, rightStartX, rightStartY, mTextPaint);


        mTextPaint.getTextBounds(mTextDown, 0, mTextDown.length(), mBoundDown);
        float downStartX = getWidth() / 2 - mBoundDown.width() / 2;
        int downStartY = (getHeight() * 17) / 20 + mBoundDown.height() / 2;

        canvas.drawText(mTextDown, downStartX, downStartY, mTextPaint);

        mTextPaint.getTextBounds("1", 0, 1, mBoundCenter);

        mTextPaint.getTextBounds(mTextCenter, 0, mTextCenter.length(), mBoundCenter);
        float centerStartX = "1".equals(mTextCenter) ? getWidth() / 2 -mBoundCenter.width():
                getWidth() / 2 - (mBoundCenter.width() / 2+1);
        int centerStartY = getHeight() / 2 + mBoundCenter.height() / 2;

        canvas.drawText(mTextCenter, centerStartX, centerStartY, mTextPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mAllRegion = new Region(-mWidth, -mWidth, mWidth, mWidth);
        mRectFBig = new RectF(-mWidth / 2, -mWidth / 2, mWidth / 2, mWidth / 2);
        mRectFLittle = new RectF(-mWidth / 5 - 2, -mWidth / 5 - 2, mWidth / 5 + 2, mWidth / 5 + 2);
        mRadius = mWidth / 5;
        mHollRadius = mWidth / 2;
        initPath();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCurX = (int) event.getX() - getMeasuredWidth() / 2;
        mCurY = (int) event.getY() - getMeasuredHeight() / 2;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                containRect(mCurX, mCurY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mClickFlag != -1) {
                    containRect(mCurX, mCurY);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mClickFlag != -1) {
                    switch (mClickFlag) {
                        case RIGHT:
                            if (mListener != null) {
                                mListener.clickRight();
                            }
                            break;
                        case BOTTOM:
                            if (mListener != null) {
                                mListener.clickBottom();
                            }
                            break;
                        case LEFT:
                            if (mListener != null) {
                                mListener.clickLeft();
                            }
                            break;
                        case TOP:
                            if (mListener != null) {
                                mListener.clickTop();
                            }
                            break;
                        case CENTER:
                            if (mListener != null) {
                                mListener.clickCenter();
                            }
                            break;
                        default:
                            break;
                    }
                    mClickFlag = -1;
                }
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    public void containRect(int x, int y) {
        int index = -1;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).contains(x, y)) {
                mClickFlag = switchRect(i);
                index = i;
                break;
            }
        }
        if (index == -1) {
            mClickFlag = -1;
        }
    }

    public int switchRect(int i) {
        switch (i) {
            case 0:
                return RIGHT;
            case 1:
                return BOTTOM;
            case 2:
                return LEFT;
            case 3:
                return TOP;
            case 4:
                return CENTER;
            default:
                return -1;
        }
    }

    public void setValue(String mTextLeft, String mTextUp, String mTextRight, String mTextDown, String mTextCenter, float mTextSize) {
        this.mTextLeft = mTextLeft;
        this.mTextUp = mTextUp;
        this.mTextRight = mTextRight;
        this.mTextDown = mTextDown;
        this.mTextCenter = mTextCenter;
        this.mTextSize = mTextSize;
        mTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public interface RegionViewClickListener {
        void clickLeft();

        void clickTop();

        void clickRight();

        void clickBottom();

        void clickCenter();
    }

    /**
     * 计算控件宽高
     * [0宽,1高]
     *
     * @param oldMeasure
     * @author Ruffian
     */
    public int onMeasureR(int attr, int oldMeasure) {
        int newSize = 0;
        int mode = MeasureSpec.getMode(oldMeasure);
        int oldSize = MeasureSpec.getSize(oldMeasure);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                newSize = oldSize;
                break;
            case MeasureSpec.AT_MOST:
                float value;
                if (attr == 0) {
                    value = mTextPaint.measureText(mTextLeft);
                    // 控件的宽度 + getPaddingLeft() + getPaddingRight()
                    newSize = (int) (getPaddingLeft() + value + getPaddingRight());
                } else if (attr == 1) {
                    Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                    value = Math.abs((fontMetrics.bottom - fontMetrics.top));
                    // 控件的高度 + getPaddingTop() + getPaddingBottom()
                    newSize = (int) (getPaddingTop() + value + getPaddingBottom());
                }
                break;
            default:
                break;
        }
        return newSize;
    }
}