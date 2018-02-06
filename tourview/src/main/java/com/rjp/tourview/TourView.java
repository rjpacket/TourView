package com.rjp.tourview;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Gimpo create on 2018/2/6 15:16
 * email  : jimbo922@163.com
 */

public class TourView extends View {
    private Context mContext;
    private Paint mPaint;
    private List<TourModel> tourModels;
    private int position = -1;
    private int width;
    private int height;
    private BlurMaskFilter blurMaskFilter;
    private float blurWidth = 20;

    public TourView(@NonNull Context context) {
        this(context, null);
    }

    public TourView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        tourModels = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        blurMaskFilter = new BlurMaskFilter(blurWidth, BlurMaskFilter.Blur.OUTER);
        disableHardwareRendering(this);
    }

    /**
     * 禁止硬件加速
     * @param v
     */
    public static void disableHardwareRendering(View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (position != -1) {
            TourModel tourModel = tourModels.get(position);

            //画高亮区域
            mPaint.setMaskFilter(null);
            int saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);
            canvas.drawColor(Color.parseColor("#cc000000"));
            drawHole(canvas, tourModel);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
            drawHole(canvas, tourModel);
            mPaint.setXfermode(null);
            canvas.restoreToCount(saved);

            //画引导的信息
            canvas.drawBitmap(tourModel.getBitmap(), tourModel.getBitmapLeft(), tourModel.getBitmapTop(), mPaint);

            //虚化
            if (blurWidth > 0) {
                mPaint.setMaskFilter(blurMaskFilter);
                drawHole(canvas, tourModel);
            }
        }
    }

    /**
     * 画高亮区域
     *
     * @param canvas
     * @param tourModel
     */
    private void drawHole(Canvas canvas, TourModel tourModel) {
        switch (tourModel.getType()) {
            case Type.TYPE_CIRCLE:
                int holeWidth = tourModel.getHoleRight() - tourModel.getHoleLeft();
                int holeHeight = tourModel.getHoleBottom() - tourModel.getHoleTop();
                int radius = holeWidth > holeHeight ? holeWidth / 2 : holeHeight / 2;
                canvas.drawCircle((tourModel.getHoleLeft() + tourModel.getHoleRight()) / 2, (tourModel.getHoleTop() + tourModel.getHoleBottom()) / 2, radius, mPaint);
                break;
            case Type.TYPE_RECT:
                canvas.drawRect(new RectF(tourModel.getHoleLeft(), tourModel.getHoleTop(), tourModel.getHoleRight(), tourModel.getHoleBottom()), mPaint);
                break;
            case Type.TYPE_OVAL:
            default:
                canvas.drawOval(new RectF(tourModel.getHoleLeft(), tourModel.getHoleTop(), tourModel.getHoleRight(), tourModel.getHoleBottom()), mPaint);
                break;
        }
    }

    /**
     * 在view的location画一个resId
     */
    public void add(TourModel tourModel) {
        tourModels.add(tourModel);
    }

    /**
     * 调用这个方法就可以展示
     */
    public void next() {
        if (tourModels != null && position < tourModels.size()) {
            position++;
            invalidate();
        } else {
            setVisibility(GONE);
        }
    }
}
