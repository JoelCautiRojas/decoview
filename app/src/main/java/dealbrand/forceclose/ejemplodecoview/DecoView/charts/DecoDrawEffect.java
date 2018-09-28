package dealbrand.forceclose.ejemplodecoview.DecoView.charts;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DecoDrawEffect {

    static private final int MAX_ALPHA = 255;
    static private final float EXPLODE_LINE_MIN = 0.01f;
    static private final float EXPLODE_LINE_MAX = 0.1f;
    static private final float EXPLODE_CIRCLE_MIN = 0.01f;
    static private final float EXPLODE_CIRCLE_MAX = 0.1f;
    static private final int EXPLODE_LINE_COUNT = 9;
    static private final float MIN_LINE_WIDTH = 10f;
    static private final float MAX_LINE_WIDTH = 100f;
    private final EffectType mEffectType;
    private Paint mPaint;
    private Paint mPaintExplode;
    private Paint mPaintText;
    private String mText;
    private final RectF mSpinBounds = new RectF();
    private int mCircuits = 6;

    DecoDrawEffect(@NonNull EffectType effectType, @NonNull Paint paint, @Nullable String text) {
        mEffectType = effectType;
        setPaint(paint);
        setText(text, paint.getColor());
    }

    DecoDrawEffect(@NonNull EffectType effectType, @NonNull Paint paint) {
        mEffectType = effectType;
        setPaint(paint);
    }

    public boolean postExecuteVisibility() {
        return (mEffectType == EffectType.EFFECT_SPIRAL_OUT) ||
                (mEffectType == EffectType.EFFECT_SPIRAL_OUT_FILL);
    }

    private void setPaint(@NonNull Paint paint) {
        mPaint = new Paint(paint);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(determineLineWidth(paint, 1f));
        mPaintExplode = new Paint(paint);
        mPaintExplode.setStrokeCap(Paint.Cap.ROUND);
        mPaintExplode.setStyle(Paint.Style.FILL);
        mPaintExplode.setStrokeWidth(determineLineWidth(paint, 0.66f));
    }

    private float determineLineWidth(@NonNull Paint paint, float factor) {
        float width = paint.getStrokeWidth();
        width = Math.min(width, MAX_LINE_WIDTH);
        width = Math.max(width, MIN_LINE_WIDTH);
        return width * factor;
    }

    public void setText(@Nullable String text, int color) {
        mText = text;
        mPaintText = new Paint();
        mPaintText.setColor(color);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setAntiAlias(true);
    }

    public void setRotationCount(int circuits) {
        mCircuits = circuits;
    }

    public void draw(@NonNull Canvas canvas, @NonNull RectF bounds, float percentComplete, float startAngle, float sweepAngle) {
        switch (mEffectType) {
            case EFFECT_SPIRAL_EXPLODE:
                final float step = 0.6f;
                if (percentComplete <= step) {
                    drawMoveToCenter(canvas, bounds, percentComplete * (1f / step), startAngle, sweepAngle);
                } else {
                    final float remain = 1.0f - step;
                    drawExplode(canvas, bounds, (percentComplete - step) / remain);
                    drawText(canvas, bounds, (percentComplete - step) / remain);
                }

                break;
            case EFFECT_EXPLODE:
                drawExplode(canvas, bounds, percentComplete);
                drawText(canvas, bounds, percentComplete);
                break;
            case EFFECT_SPIRAL_IN:
            case EFFECT_SPIRAL_OUT:
            case EFFECT_SPIRAL_OUT_FILL:
                drawMoveToCenter(canvas, bounds, percentComplete, startAngle, sweepAngle);
                break;
        }
    }

    public void drawMoveToCenter(@NonNull Canvas canvas, RectF bounds,
                                 float percentComplete, float startAngle, float sweepAngle) {

        final boolean moveOutward = mEffectType == EffectType.EFFECT_SPIRAL_OUT ||
                mEffectType == EffectType.EFFECT_SPIRAL_OUT_FILL;

        final boolean spinClockwise = mEffectType != EffectType.EFFECT_SPIRAL_IN &&
                mEffectType != EffectType.EFFECT_SPIRAL_EXPLODE;

        final float buffer = 10f;
        final float halfWidth = (bounds.width() / 2) - buffer;
        final float halfHeight = (bounds.height() / 2) - buffer;
        final float baseRotateAngle = mCircuits * 360f;

        float rotateAmount = (mEffectType == EffectType.EFFECT_SPIRAL_OUT_FILL) ? baseRotateAngle + 360f : baseRotateAngle;
        float rotateOffset = rotateAmount * percentComplete;
        float newAngle = (startAngle + (spinClockwise ? rotateOffset : -rotateOffset)) % 360;
        float sweep = getSweepAngle(percentComplete);

        mSpinBounds.set(bounds);

        float percent = percentComplete;

        if (moveOutward) {
            percent = 1.0f - percentComplete;
        }

        if (mEffectType == EffectType.EFFECT_SPIRAL_OUT_FILL) {
            if ((rotateAmount * percentComplete) > (rotateAmount - 360f)) {
                mPaint.setStyle(Paint.Style.STROKE);
                sweep = (rotateAmount * percentComplete) % 360;
                if (sweep <= 0) {
                    sweep = 360;
                }

                if (sweep > sweepAngle) {
                    sweep = sweepAngle;
                }
                newAngle = startAngle;
            } else {
                float min = 1.0f - (baseRotateAngle / rotateAmount);
                if (percent > min) {
                    float adjustedPercentage = (percent - min) / (1.0f - min);
                    mSpinBounds.inset(halfWidth * adjustedPercentage,
                            halfHeight * adjustedPercentage);
                }
            }
        } else {
            mSpinBounds.inset(halfWidth * percent, halfHeight * percent);
        }

        canvas.drawArc(mSpinBounds,
                newAngle,
                sweep,
                false,
                mPaint);
    }

    private float getSweepAngle(float percentComplete) {
        final float sweepMax = 30f;
        final float sweepMin = 0.1f;

        if (percentComplete < 0.5) {
            return sweepMin + (sweepMax - sweepMin) * (percentComplete * 2);
        }
        return sweepMax - (sweepMax - sweepMin) * ((percentComplete - 0.5f) * 2);
    }

    public void drawText(@NonNull Canvas canvas, RectF bounds, float percentComplete) {
        if (mText != null && mText.length() > 0) {
            mPaintText.setTextSize(100 * percentComplete);
            mPaintText.setAlpha(MAX_ALPHA);

            final float startFadePercent = 0.7f;
            if (percentComplete > startFadePercent) {
                int alphaText = (int) (MAX_ALPHA - (MAX_ALPHA * ((percentComplete - startFadePercent) / (1.0f - startFadePercent))));
                mPaintText.setAlpha(alphaText);
            }

            final float xPos = bounds.left + (bounds.width() / 2);
            final float yPos = (bounds.top + (bounds.height() / 2)) - ((mPaintText.descent() + mPaintText.ascent()) / 2);
            canvas.drawText(mText, xPos, yPos, mPaintText);
        }
    }

    public void drawExplode(@NonNull Canvas canvas, RectF bounds, float percentComplete) {
        boolean drawCircles = Build.VERSION.SDK_INT <= 17;
        final float maxLength = bounds.width() * EXPLODE_LINE_MAX;
        final float minLength = bounds.width() * EXPLODE_LINE_MIN;
        final float startPosition = bounds.width() * EXPLODE_LINE_MAX;
        int alpha = MAX_ALPHA;

        float length;
        if (percentComplete > 0.5f) {
            float completed = (percentComplete - 0.5f) * 2;
            length = maxLength - (completed * (maxLength - minLength));
            alpha = MAX_ALPHA - (int) (MAX_ALPHA * completed);
        } else {
            length = minLength + ((percentComplete * 2) * (maxLength - minLength));
        }

        final int initialAlpha = mPaint.getAlpha();
        if (alpha < MAX_ALPHA) {
            mPaintExplode.setAlpha((int) (initialAlpha * (alpha / (float) MAX_ALPHA)));
        }

        float radiusEnd = startPosition + (int) (((bounds.width() / 2) - startPosition) * percentComplete);
        float radiusStart = radiusEnd - length;

        float angleInDegrees = 0;
        for (int i = 0; i < EXPLODE_LINE_COUNT; i++) {
            drawExplodeLine(canvas, bounds, radiusStart, radiusEnd, angleInDegrees, percentComplete, drawCircles);
            angleInDegrees += (360f / EXPLODE_LINE_COUNT);
        }

        if (alpha < MAX_ALPHA) {
            mPaint.setAlpha(initialAlpha);
        }
    }

    private void drawExplodeLine(@NonNull Canvas canvas, RectF bounds,
                                 float radiusStart, float radiusEnd, float angleInDegrees,
                                 float percentComplete, boolean compatMode) {
        float startX = (radiusStart * (float) Math.cos(angleInDegrees * Math.PI / 180F)) + bounds.centerX();
        float startY = (radiusStart * (float) Math.sin(angleInDegrees * Math.PI / 180F)) + bounds.centerY();
        float endX = (radiusEnd * (float) Math.cos(angleInDegrees * Math.PI / 180F)) + bounds.centerX();
        float endY = (radiusEnd * (float) Math.sin(angleInDegrees * Math.PI / 180F)) + bounds.centerY();

        if (!compatMode) {
            canvas.drawLine(startX, startY, endX, endY, mPaintExplode);
        } else {
            float radius = (bounds.width() * EXPLODE_CIRCLE_MIN) + ((bounds.width() * EXPLODE_CIRCLE_MAX - bounds.width() * EXPLODE_CIRCLE_MIN) * percentComplete);
            canvas.drawCircle(endX, endY, radius, mPaintExplode);
        }
    }

    public enum EffectType{
        EFFECT_SPIRAL_OUT_FILL, /* Fill track after outward spiral animation */
        EFFECT_SPIRAL_OUT, /* Animation from center to outside in spiral motion */
        EFFECT_SPIRAL_IN, /* Animation from outside to center in spiral motion */
        EFFECT_EXPLODE, /* Explode animation where several lines are produced from center */
        EFFECT_SPIRAL_EXPLODE /* Combines EFFECT_SPIRAL_IN and EFFECT_EXPLODE */
    }
}
