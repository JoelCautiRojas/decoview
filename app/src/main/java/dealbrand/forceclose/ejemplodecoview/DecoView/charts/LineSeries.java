package dealbrand.forceclose.ejemplodecoview.DecoView.charts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.Log;

import dealbrand.forceclose.ejemplodecoview.DecoView.DecoView;

public class LineSeries extends ChartSeries{
    private final String TAG = getClass().getSimpleName();
    private final Path mDrawPath = new Path();
    private DecoView.HorizGravity mHorizGravity = DecoView.HorizGravity.GRAVITY_HORIZONTAL_CENTER;
    private DecoView.VertGravity mVertGravity = DecoView.VertGravity.GRAVITY_VERTICAL_CENTER;

    public LineSeries(@NonNull SeriesItem seriesItem, int totalAngle, int rotateAngle) {
        super(seriesItem, totalAngle, rotateAngle);
        Log.e(TAG, "LineSeries is experimental. Not all functionality is implemented.");
    }

    @Override
    public boolean draw(Canvas canvas, RectF bounds) {
        if (super.draw(canvas, bounds)) {
            return true;
        }

        final boolean reverse = !mSeriesItem.getSpinClockwise();
        float insetX = mSeriesItem.getInset() != null ? mSeriesItem.getInset().x : 0;
        float insetY = mSeriesItem.getInset() != null ? mSeriesItem.getInset().y : 0;
        float lineWidth = getSeriesItem().getLineWidth() / 2;
        float posNow = mPositionCurrentEnd / (getSeriesItem().getMaxValue() - getSeriesItem().getMinValue());

        /* Adjust to show point even when empty */
        if (this.getSeriesItem().showPointWhenEmpty() && Math.abs(posNow) < 0.01f) {
            posNow = 0.01f;
        }

        final float totalWidth = posNow * (canvas.getWidth() - (2 * lineWidth));
        final float totalHeight = posNow * (canvas.getHeight() - (2 * lineWidth));
        float xVal1 = !reverse ? lineWidth : canvas.getWidth() - lineWidth;
        float yVal1 = !reverse ? lineWidth : canvas.getHeight() - lineWidth;
        float xVal2 = !reverse ? lineWidth + totalWidth : xVal1 - totalWidth;
        float yVal2 = !reverse ? lineWidth + totalHeight : yVal1 - totalHeight;

        if (isHorizontal()) {
            switch (mVertGravity) {
                case GRAVITY_VERTICAL_TOP:
                    yVal1 = yVal2 = lineWidth / 2;
                    yVal1 += insetY;
                    yVal2 += insetY;
                    break;
                case GRAVITY_VERTICAL_BOTTOM:
                    yVal1 = yVal2 = canvas.getHeight() - lineWidth;
                    yVal1 -= insetY;
                    yVal2 -= insetY;
                    break;
                default:
                    Log.w(TAG, "Invalid Gravity set, VERTICAL_CENTER set (" + mVertGravity + ")");
                case GRAVITY_VERTICAL_CENTER:
                    yVal1 = yVal2 = canvas.getHeight() / 2;
                    yVal1 += insetY;
                    yVal2 += insetY;
            }
        } else {
            switch (mHorizGravity) {
                case GRAVITY_HORIZONTAL_LEFT:
                    xVal1 = xVal2 = lineWidth;
                    xVal1 += insetX;
                    xVal2 += insetX;
                    break;
                case GRAVITY_HORIZONTAL_RIGHT:
                    xVal1 = xVal2 = canvas.getWidth() - lineWidth;
                    xVal1 -= insetX;
                    xVal2 -= insetX;
                    break;
                default:
                    Log.w(TAG, "Invalid Gravity set, HORIZONTAL_CENTER set (" + mHorizGravity + ")");
                case GRAVITY_HORIZONTAL_CENTER:
                    xVal1 = xVal2 = canvas.getWidth() / 2;
                    xVal1 += insetX;
                    xVal2 += insetX;
            }
        }
        mDrawPath.reset();
        mDrawPath.moveTo(xVal1, yVal1);
        mDrawPath.lineTo(xVal2, yVal2);

        canvas.drawPath(mDrawPath, mPaint);
        return true;
    }

    protected void applyGradientToPaint() {
        if (Color.alpha(mSeriesItem.getSecondaryColor()) != 0) {
            /**
             * Linear gradient is used for straight lines
             */
            int colorOne = mSeriesItem.getSpinClockwise() ? mSeriesItem.getColor() : mSeriesItem.getSecondaryColor();
            int colorTwo = mSeriesItem.getSpinClockwise() ? mSeriesItem.getSecondaryColor() : mSeriesItem.getColor();
            LinearGradient gradient = new LinearGradient(mBounds.left, mBounds.top, mBounds.right, mBounds.bottom, colorOne, colorTwo, Shader.TileMode.CLAMP);
            mPaint.setShader(gradient);
        }
    }

    public void setHorizGravity(DecoView.HorizGravity horizGravity) {
        mHorizGravity = horizGravity;
    }

    public void setVertGravity(DecoView.VertGravity vertGravity) {
        mVertGravity = vertGravity;
    }

    private boolean isHorizontal() {
        return mSeriesItem.getChartStyle() == SeriesItem.ChartStyle.STYLE_LINE_HORIZONTAL;
    }
}
