package dealbrand.forceclose.ejemplodecoview.DecoView.charts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import dealbrand.forceclose.ejemplodecoview.DecoView.events.DecoEvent;

abstract public class ChartSeries {
    static final private float MIN_SWEEP_ANGLE = 0.1f;
    static final private float MIN_SWEEP_ANGLE_FLAT = 0.1f;
    static final private float MIN_SWEEP_ANGLE_NONE = 0f;
    static final private float MIN_SWEEP_ANGLE_PIE = MIN_SWEEP_ANGLE_NONE;

    @SuppressWarnings("unused")
    protected final String TAG = getClass().getSimpleName();
    protected final SeriesItem mSeriesItem;
    protected DecoEvent.EventType mDrawMode;
    protected DecoDrawEffect mEffect;
    protected float mPositionStart;
    protected float mPositionEnd;
    protected float mPositionCurrentEnd;
    protected float mPercentComplete = 1.0f;
    protected RectF mBounds;
    protected RectF mBoundsInset;
    protected int mAngleStart = 180;
    protected int mAngleSweep = 360;
    protected Paint mPaint;
    private boolean mVisible;
    private ValueAnimator mValueAnimator;
    private ColorAnimate mColorAnimate;
    private DecoEvent mEventCurrent;
    private boolean mIsPaused;

    ChartSeries(@NonNull SeriesItem seriesItem, int totalAngle, int rotateAngle) {
        mSeriesItem = seriesItem;
        mVisible = seriesItem.getInitialVisibility();
        setupView(totalAngle, rotateAngle);
        reset();
    }

    public void setupView(int totalAngle, int rotateAngle) {
        if (totalAngle < 0 || totalAngle > 360) {
            throw new IllegalArgumentException("Total angle of view must be in the range 0..360");
        }
        if (rotateAngle < 0 || rotateAngle > 360) {
            throw new IllegalArgumentException("Rotate angle of view must be in the range 0..360");
        }
        mAngleStart = rotateAngle;
        mAngleSweep = totalAngle;

        if (!mSeriesItem.getSpinClockwise()) {
            mAngleStart = (mAngleStart + mAngleSweep) % 360;
        }
        mBounds = null;
    }

    public SeriesItem getSeriesItem() {
        return mSeriesItem;
    }

    public void startAnimateMove(@NonNull final DecoEvent event) {
        mIsPaused = false;
        mDrawMode = event.getEventType();
        mVisible = true;
        cancelAnimation();
        mEventCurrent = event;
        final boolean changeColors = event.isColorSet();
        if (changeColors) {
            mColorAnimate = new ColorAnimate(mSeriesItem.getColor(), event.getColor());
            mSeriesItem.setColor(event.getColor());
        }
        float position = event.getEndPosition();
        event.notifyStartListener();
        mPositionStart = mPositionCurrentEnd;
        mPositionEnd = position;
        long animationDuration = event.getEffectDuration();
        if ((animationDuration == 0) || (Math.abs(mPositionEnd - mPositionStart) < 0.01)) {
            cancelAnimation();
            mPositionCurrentEnd = mPositionEnd;
            mEventCurrent = null;
            mPercentComplete = 1.0f;
            for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                seriesItemListener.onSeriesItemAnimationProgress(1.0f, mPositionEnd);
            }
            event.notifyEndListener();
            return;
        }
        if (animationDuration < 0) {
            animationDuration = Math.abs((int) (mSeriesItem.getSpinDuration() *
                    ((mPositionStart - mPositionEnd) / mSeriesItem.getMaxValue())));

        }
        mValueAnimator = ValueAnimator.ofFloat(mPositionStart, position);
        mValueAnimator.setDuration(animationDuration);
        if (event.getInterpolator() != null) {
            mValueAnimator.setInterpolator(event.getInterpolator());
        } else {
            if (mSeriesItem.getInterpolator() != null) {
                mValueAnimator.setInterpolator(mSeriesItem.getInterpolator());
            }
        }
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float current = Float.valueOf(valueAnimator.getAnimatedValue().toString());
                mPercentComplete = (current - mPositionStart) / (mPositionEnd - mPositionStart);
                mPositionCurrentEnd = current;

                for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                    seriesItemListener.onSeriesItemAnimationProgress(mPercentComplete, mPositionCurrentEnd);
                }
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (changeColors) {
                    mColorAnimate = null;
                }

                event.notifyEndListener();
            }
        });
        mValueAnimator.start();
    }

    public void cancelAnimation() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        mEventCurrent = null;
        if (mColorAnimate != null) {
            mPaint.setColor(mSeriesItem.getColor());
            mColorAnimate = null;
        }
    }

    public void startAnimateHideShow(@NonNull final DecoEvent event, final boolean showArc) {
        cancelAnimation();
        event.notifyStartListener();
        mDrawMode = event.getEventType();
        mPercentComplete = showArc ? 1.0f : 0f;
        mVisible = true;
        final float maxValue = 1.0f;
        mValueAnimator = ValueAnimator.ofFloat(0, maxValue);
        mValueAnimator.setDuration(event.getEffectDuration());
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float current = Float.valueOf(valueAnimator.getAnimatedValue().toString());
                mPercentComplete = showArc ? (maxValue - current) : current;

                for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                    seriesItemListener.onSeriesItemDisplayProgress(mPercentComplete);
                }
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (event.getEventType() != DecoEvent.EventType.EVENT_EFFECT) {
                    event.notifyEndListener();
                }
            }
        });

        mValueAnimator.start();
    }

    public void startAnimateColorChange(@NonNull final DecoEvent event) {
        cancelAnimation();
        event.notifyStartListener();
        mVisible = true;

        mDrawMode = event.getEventType();
        mPercentComplete = 0f;

        final boolean changeColors = event.isColorSet();
        if (changeColors) {
            mColorAnimate = new ColorAnimate(mSeriesItem.getColor(), event.getColor());
            mSeriesItem.setColor(event.getColor());
        } else {
            Log.w(TAG, "Must set new color to start CHANGE_COLOR event");
            return;
        }

        final float maxValue = 1.0f;
        mValueAnimator = ValueAnimator.ofFloat(0, maxValue);
        mValueAnimator.setDuration(event.getEffectDuration());
        if (event.getInterpolator() != null) {
            mValueAnimator.setInterpolator(event.getInterpolator());
        } else {
            mValueAnimator.setInterpolator(new LinearInterpolator());
        }
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mPercentComplete = Float.valueOf(valueAnimator.getAnimatedValue().toString());

                for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                    seriesItemListener.onSeriesItemDisplayProgress(mPercentComplete);
                }
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                event.notifyEndListener();
            }
        });
        mValueAnimator.start();
    }

    public void startAnimateEffect(@NonNull final DecoEvent event)
            throws IllegalStateException {
        if (event.getEffectType() == null) {
            throw new IllegalStateException("Unable to execute null effect type");
        }
        final float maxValue = 1.0f;
        cancelAnimation();
        event.notifyStartListener();
        mVisible = true;
        mDrawMode = event.getEventType();
        mEffect = new DecoDrawEffect(event.getEffectType(), mPaint, event.getDisplayText());
        mEffect.setRotationCount(event.getEffectRotations());
        mPercentComplete = 0f;
        mValueAnimator = ValueAnimator.ofFloat(0, maxValue);
        mValueAnimator.setDuration(event.getEffectDuration());
        Interpolator interpolator = (event.getInterpolator() != null) ? event.getInterpolator() : new LinearInterpolator();
        mValueAnimator.setInterpolator(interpolator);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mPercentComplete = Float.valueOf(valueAnimator.getAnimatedValue().toString());
                for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                    seriesItemListener.onSeriesItemDisplayProgress(mPercentComplete);
                }
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                event.notifyEndListener();
                mDrawMode = DecoEvent.EventType.EVENT_MOVE;
                mVisible = mEffect.postExecuteVisibility();
                mEffect = null;
            }
        });
        mValueAnimator.start();
    }

    public void reset() {
        mDrawMode = DecoEvent.EventType.EVENT_MOVE;
        mVisible = mSeriesItem.getInitialVisibility();
        cancelAnimation();
        mPositionStart = mSeriesItem.getMinValue();
        mPositionEnd = mSeriesItem.getInitialValue();
        mPositionCurrentEnd = mSeriesItem.getInitialValue();
        mPercentComplete = 1.0f;
        mPaint = new Paint();
        mPaint.setColor(mSeriesItem.getColor());
        mPaint.setStyle((mSeriesItem.getChartStyle() == SeriesItem.ChartStyle.STYLE_DONUT) ? Paint.Style.STROKE : Paint.Style.FILL);
        mPaint.setStrokeWidth(mSeriesItem.getLineWidth());
        mPaint.setStrokeCap(mSeriesItem.getRoundCap() ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        mPaint.setAntiAlias(true);
        if (mSeriesItem.getShadowSize() > 0) {
            mPaint.setShadowLayer(mSeriesItem.getShadowSize(), 0, 0, mSeriesItem.getShadowColor());
        }
        mBounds = null;
        for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
            seriesItemListener.onSeriesItemAnimationProgress(mPercentComplete, mPositionCurrentEnd);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public RectF drawLabel(Canvas canvas, RectF bounds, float anglePercent) {
        if (!mVisible) {
            return null;
        }
        if (bounds == null || bounds.isEmpty()) {
            throw new IllegalArgumentException("Drawing bounds can not be null or empty");
        }
        if (mSeriesItem.getSeriesLabel() != null) {
            return mSeriesItem.getSeriesLabel().draw(canvas, bounds, anglePercent, getPositionPercent(), mPositionCurrentEnd);
        }
        return null;
    }

    public boolean draw(Canvas canvas, RectF bounds) {
        if (!mVisible) {
            return true;
        }
        if (bounds == null || bounds.isEmpty()) {
            throw new IllegalArgumentException("Drawing bounds can not be null or empty");
        }
        processBoundsChange(bounds);
        if (mDrawMode == DecoEvent.EventType.EVENT_EFFECT) {
            // Delegate the drawing to the ArcEffect as required
            if (mEffect != null) {
                mEffect.draw(canvas, mBoundsInset, mPercentComplete, mAngleStart, mAngleSweep);
            }
            return true;
        }
        processRevealEffect();
        if (mColorAnimate != null) {
            mPaint.setColor(mColorAnimate.getColorCurrent(mPercentComplete));
        } else if (mPaint.getColor() != getSeriesItem().getColor()) {
            mPaint.setColor(getSeriesItem().getColor());
        }
        return false;
    }

    protected float adjustSweepDirection(float sweep) {
        return mSeriesItem.getSpinClockwise() ? sweep : -sweep;
    }

    protected float adjustDrawPointAngle(float sweep) {
        return (mAngleStart + (sweep - getMinSweepAngle())) % 360;

    }

    protected void processRevealEffect() {
        if ((mDrawMode != DecoEvent.EventType.EVENT_HIDE) &&
                (mDrawMode != DecoEvent.EventType.EVENT_SHOW)) {
            if (mSeriesItem.getLineWidth() != mPaint.getStrokeWidth()) {
                mPaint.setStrokeWidth(mSeriesItem.getLineWidth());
            }
            return;
        }

        float lineWidth = mSeriesItem.getLineWidth();
        if (mPercentComplete > 0) {
            lineWidth *= (1.0f - mPercentComplete);
            mPaint.setAlpha((int) (Color.alpha(mSeriesItem.getColor()) * (1.0f - mPercentComplete)));
        } else {
            mPaint.setAlpha(Color.alpha(mSeriesItem.getColor()));
        }

        mPaint.setStrokeWidth(lineWidth);
    }

    protected void processBoundsChange(final RectF bounds) {
        if (mBounds == null || !mBounds.equals(bounds)) {
            mBounds = new RectF(bounds);
            mBoundsInset = new RectF(bounds);
            if (mSeriesItem.getInset() != null) {
                mBoundsInset.inset(mSeriesItem.getInset().x, mSeriesItem.getInset().y);
            }
            applyGradientToPaint();
        }
    }

    abstract protected void applyGradientToPaint();

    protected float verifyMinSweepAngle(final float angle) {
        return (Math.abs(angle) < getMinSweepAngle() &&
                getSeriesItem().showPointWhenEmpty())
                ? getMinSweepAngle() : angle;
    }

    protected float calcCurrentPosition(float start, float end, float min, float max, float percent) {
        start -= min;
        end -= min;
        max -= min;
        if (Math.abs(start - end) < 0.01) {
            return start / max;
        }
        if ((mDrawMode == DecoEvent.EventType.EVENT_HIDE) ||
                (mDrawMode == DecoEvent.EventType.EVENT_SHOW) ||
                (mDrawMode == DecoEvent.EventType.EVENT_COLOR_CHANGE)) {
            percent = 1.0f;
        }
        if (Math.abs(end) < 0.01) {
            return (start / max) * (start - (start * percent)) / start;
        }
        return (end / max) * (start + (percent * (end - start))) / end;
    }

    protected float getMinSweepAngle() {
        if (!mSeriesItem.showPointWhenEmpty()) {
            return MIN_SWEEP_ANGLE_NONE;
        }
        if (mSeriesItem.getChartStyle() == SeriesItem.ChartStyle.STYLE_PIE) {
            return MIN_SWEEP_ANGLE_PIE;
        }
        if (mPaint.getStrokeCap() == Paint.Cap.ROUND) {
            return MIN_SWEEP_ANGLE;
        }
        return MIN_SWEEP_ANGLE_FLAT;
    }
    public float getPositionPercent() {
        return mPositionCurrentEnd / (mSeriesItem.getMaxValue() - mSeriesItem.getMinValue());
    }
    public boolean isVisible() {
        return mVisible;
    }

    public boolean pause() {
        if (mValueAnimator != null && mValueAnimator.isRunning() && !mIsPaused) {
            mValueAnimator.cancel();
            mIsPaused = true;
            return true;
        }
        return false;
    }

    public boolean resume() {
        if (isPaused()) {
            startAnimateMove(mEventCurrent);
            return true;
        }
        return false;
    }

    public boolean isPaused() {
        return mIsPaused;
    }

    public void setPosition(float position) {
        mPositionStart = position;
        mPositionEnd = position;
        mPositionCurrentEnd = position;
        mPercentComplete = 1.0f;
    }
}
