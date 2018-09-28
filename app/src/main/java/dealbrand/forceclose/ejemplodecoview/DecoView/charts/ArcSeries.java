package dealbrand.forceclose.ejemplodecoview.DecoView.charts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.NonNull;

public abstract class ArcSeries extends ChartSeries{
    protected float mArcAngleStart;
    protected float mArcAngleSweep;

    ArcSeries(@NonNull SeriesItem seriesItem, int totalAngle, int rotateAngle) {
        super(seriesItem, totalAngle, rotateAngle);
    }

    abstract void drawArc(Canvas canvas);

    @Override
    public boolean draw(Canvas canvas, RectF bounds) {
        if (super.draw(canvas, bounds)) {
            return true;
        }
        final float endPos = calcCurrentPosition(mPositionStart, mPositionEnd, mSeriesItem.getMinValue(), mSeriesItem.getMaxValue(), mPercentComplete);
        mArcAngleSweep = adjustSweepDirection(verifyMinSweepAngle(endPos * mAngleSweep));
        mArcAngleStart = mAngleStart;

        if (mSeriesItem.getDrawAsPoint()) {
            mArcAngleStart = adjustDrawPointAngle(mArcAngleSweep);
            mArcAngleSweep = adjustSweepDirection(getMinSweepAngle());
        } else if (mArcAngleSweep == 0) {
            return true;
        }
        return false;
    }

    protected void applyGradientToPaint() {
        if (Color.alpha(mSeriesItem.getSecondaryColor()) != 0) {
            SweepGradient gradient;
            if (mAngleSweep < 360) {
                final int[] colors = {mSeriesItem.getColor(), mSeriesItem.getSecondaryColor()};
                final float[] positions = {0, 1};
                gradient = new SweepGradient(mBounds.centerX(), mBounds.centerY(), colors, positions);
                Matrix gradientRotationMatrix = new Matrix();
                gradientRotationMatrix.preRotate(mAngleStart - ((360f - mAngleSweep) / 2), mBounds.centerX(), mBounds.centerY());
                gradient.setLocalMatrix(gradientRotationMatrix);
            } else {
                final int[] colors = {mSeriesItem.getSecondaryColor(), mSeriesItem.getColor(), mSeriesItem.getSecondaryColor()};
                final float[] positions = {0, 0.5f * (mAngleSweep / 360f), 1};
                gradient = new SweepGradient(mBounds.centerX(), mBounds.centerY(), colors, positions);
            }
            mPaint.setShader(gradient);
        }
    }
}
