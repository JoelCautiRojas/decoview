package dealbrand.forceclose.ejemplodecoview.DecoView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import dealbrand.forceclose.ejemplodecoview.DecoView.charts.ChartSeries;
import dealbrand.forceclose.ejemplodecoview.DecoView.charts.DecoDrawEffect;
import dealbrand.forceclose.ejemplodecoview.DecoView.charts.LineArcSeries;
import dealbrand.forceclose.ejemplodecoview.DecoView.charts.LineSeries;
import dealbrand.forceclose.ejemplodecoview.DecoView.charts.PieSeries;
import dealbrand.forceclose.ejemplodecoview.DecoView.charts.SeriesItem;
import dealbrand.forceclose.ejemplodecoview.DecoView.events.DecoEvent;
import dealbrand.forceclose.ejemplodecoview.DecoView.events.DecoEventManager;
import dealbrand.forceclose.ejemplodecoview.DecoView.util.FuncionesGenericas;
import dealbrand.forceclose.ejemplodecoview.R;

public class DecoView extends View implements DecoEventManager.ArcEventManagerListener {
    private final String TAG = getClass().getSimpleName();
    private VertGravity mVertGravity = VertGravity.GRAVITY_VERTICAL_CENTER;
    private HorizGravity mHorizGravity = HorizGravity.GRAVITY_HORIZONTAL_CENTER;
    private ArrayList<ChartSeries> mChartSeries;
    private int mCanvasWidth = -1;
    private int mCanvasHeight = -1;
    private RectF mArcBounds;
    private float mDefaultLineWidth = 30;
    private int mRotateAngle;
    private int mTotalAngle = 360;
    private DecoEventManager mDecoEventManager;
    private float[] mMeasureViewableArea;
    public DecoView(Context context) {
        super(context);
        initView();
    }
    public DecoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,R.styleable.DecoView,
                0, 0);

        int rotateAngle = 0;
        try {
            mDefaultLineWidth = a.getDimension(R.styleable.DecoView_dv_lineWidth, 30f);
            rotateAngle = a.getInt(R.styleable.DecoView_dv_rotateAngle, 0);
            mTotalAngle = a.getInt(R.styleable.DecoView_dv_totalAngle, 360);
            mVertGravity = VertGravity.values()[a.getInt(R.styleable.DecoView_dv_arc_gravity_vertical, VertGravity.GRAVITY_VERTICAL_CENTER.ordinal())];
            mHorizGravity = HorizGravity.values()[a.getInt(R.styleable.DecoView_dv_arc_gravity_horizontal, HorizGravity.GRAVITY_HORIZONTAL_CENTER.ordinal())];
        } finally {
            a.recycle();
        }

        configureAngles(mTotalAngle, rotateAngle);

        initView();
    }
    public DecoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void configureAngles(int totalAngle, int rotateAngle) {
        if (totalAngle <= 0) {
            throw new IllegalArgumentException("Total angle of the arc must be > 0");
        }
        final int circleStartPosition = 270;
        final int arcStartPosition = 90;
        final int degreesInCircle = 360;

        mTotalAngle = totalAngle;
        mRotateAngle = (circleStartPosition + rotateAngle) % degreesInCircle;

        if (mTotalAngle < degreesInCircle) {
            mRotateAngle = ((arcStartPosition + (degreesInCircle - totalAngle) / 2) + rotateAngle) % degreesInCircle;
        }

        if (mChartSeries != null) {
            for (ChartSeries chartSeries : mChartSeries) {
                chartSeries.setupView(mTotalAngle, mRotateAngle);
            }
        }
    }

    private void initView() {
        FuncionesGenericas.initialize(getContext());
        enableCompatibilityMode();
        createVisualEditorTrack();
    }

    private DecoEventManager getEventManager() {
        if (mDecoEventManager == null) {
            mDecoEventManager = new DecoEventManager(this);
        }
        return mDecoEventManager;
    }

    public boolean isEmpty() {
        return mChartSeries == null || mChartSeries.isEmpty();
    }

    public int addSeries(@NonNull SeriesItem seriesItem) {
        if (mChartSeries == null) {
            mChartSeries = new ArrayList<>();
        }

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {

            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                invalidate();
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {
                invalidate();
            }
        });

        if (seriesItem.getLineWidth() < 0) {
            seriesItem.setLineWidth(mDefaultLineWidth);
        }

        ChartSeries chartSeries;
        switch (seriesItem.getChartStyle()) {
            case STYLE_DONUT:
                chartSeries = new LineArcSeries(seriesItem, mTotalAngle, mRotateAngle);
                break;
            case STYLE_PIE:
                chartSeries = new PieSeries(seriesItem, mTotalAngle, mRotateAngle);
                break;
            case STYLE_LINE_HORIZONTAL:
            case STYLE_LINE_VERTICAL:
                Log.w(TAG, "STYLE_LINE_* is currently experimental");
                LineSeries lineSeries = new LineSeries(seriesItem, mTotalAngle, mRotateAngle);
                lineSeries.setHorizGravity(mHorizGravity);
                lineSeries.setVertGravity(mVertGravity);
                chartSeries = lineSeries;
                break;
            default:
                throw new IllegalStateException("Chart Style not implemented");
        }
        mChartSeries.add(mChartSeries.size(), chartSeries);
        mMeasureViewableArea = new float[mChartSeries.size()];

        recalcLayout();
        return mChartSeries.size() - 1;
    }

    private void createVisualEditorTrack() {
        if (isInEditMode()) {
            addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                    .setRange(0, 100, 100)
                    .setLineWidth(mDefaultLineWidth)
                    .build());
            addSeries(new SeriesItem.Builder(Color.argb(255, 255, 64, 64))
                    .setRange(0, 100, 25)
                    .setLineWidth(mDefaultLineWidth)
                    .build());

        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mCanvasWidth = width;
        mCanvasHeight = height;
        recalcLayout();
    }

    private void recalcLayout() {
        if (mCanvasWidth <= 0 || mCanvasHeight <= 0) {
            return;
        }
        float offsetLineWidth = getWidestLine() / 2;
        float offsetX = 0;
        float offsetY = 0;
        if (mCanvasWidth != mCanvasHeight) {
            if (mCanvasWidth > mCanvasHeight) {
                offsetX = (mCanvasWidth - mCanvasHeight) / 2;
            } else {
                offsetY = (mCanvasHeight - mCanvasWidth) / 2;
            }
        }
        if (mVertGravity == VertGravity.GRAVITY_VERTICAL_FILL) {
            offsetY = 0;
        }
        if (mHorizGravity == HorizGravity.GRAVITY_HORIZONTAL_FILL) {
            offsetX = 0;
        }
        float paddingLeft = offsetX + getPaddingLeft();
        float paddingTop = offsetY + getPaddingTop();
        float paddingRight = offsetX + getPaddingRight();
        float paddingBottom = offsetY + getPaddingBottom();
        mArcBounds = new RectF(offsetLineWidth + paddingLeft,
                offsetLineWidth + paddingTop,
                mCanvasWidth - offsetLineWidth - paddingRight,
                mCanvasHeight - offsetLineWidth - paddingBottom);
        if (mVertGravity == VertGravity.GRAVITY_VERTICAL_TOP) {
            mArcBounds.offset(0, -offsetY);
        } else if (mVertGravity == VertGravity.GRAVITY_VERTICAL_BOTTOM) {
            mArcBounds.offset(0, offsetY);
        }
        if (mHorizGravity == HorizGravity.GRAVITY_HORIZONTAL_LEFT) {
            mArcBounds.offset(-offsetX, 0);
        } else if (mHorizGravity == HorizGravity.GRAVITY_HORIZONTAL_RIGHT) {
            mArcBounds.offset(offsetX, 0);
        }
    }

    private float getWidestLine() {
        if (mChartSeries == null) {
            return 0;
        }
        float widest = 0;
        for (ChartSeries chartSeries : mChartSeries) {
            widest = Math.max(chartSeries.getSeriesItem().getLineWidth(), widest);
        }
        return widest;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mArcBounds == null || mArcBounds.isEmpty()) {
            return;
        }
        if (mChartSeries != null) {
            boolean labelsSupported = true;
            for (int i = 0; i < mChartSeries.size(); i++) {
                ChartSeries chartSeries = mChartSeries.get(i);
                chartSeries.draw(canvas, mArcBounds);
                labelsSupported &= (!chartSeries.isVisible() || chartSeries.getSeriesItem().getSpinClockwise());
                mMeasureViewableArea[i] = getLabelPosition(i);
            }
            if (labelsSupported) {
                for (int i = 0; i < mMeasureViewableArea.length; i++) {
                    if (mMeasureViewableArea[i] >= 0f) {
                        ChartSeries chartSeries = mChartSeries.get(i);
                        chartSeries.drawLabel(canvas, mArcBounds, mMeasureViewableArea[i]);
                        //TODO: Keep bounds of all labels and don't allow overlap
                    }
                }
            }
        }
    }

    private float getLabelPosition(final int index) {
        float max = 0.0f;
        ChartSeries chartSeries = mChartSeries.get(index);
        for (int i = index + 1; i < mChartSeries.size(); i++) {
            ChartSeries innerSeries = mChartSeries.get(i);
            if (innerSeries.isVisible() && max < innerSeries.getPositionPercent()) {
                max = innerSeries.getPositionPercent();
            }
        }
        if (max < chartSeries.getPositionPercent()) {
            float adjusted = ((chartSeries.getPositionPercent() + max) / 2) * ((float) mTotalAngle / 360f);
            float adjust = adjusted + (((float) mRotateAngle + 90f) / 360f);
            while (adjust > 1.0f) {
                adjust -= 1.0f;
            }
            return adjust;
        }
        return -1f;
    }

    private void executeMove(@NonNull DecoEvent event) {
        if ((event.getEventType() != DecoEvent.EventType.EVENT_MOVE) &&
                (event.getEventType() != DecoEvent.EventType.EVENT_COLOR_CHANGE)) {
            return;
        }
        if (mChartSeries != null) {
            if (mChartSeries.size() <= event.getIndexPosition()) {
                throw new IllegalArgumentException("Invalid index: Position out of range (Index: " + event.getIndexPosition() + " Series Count: " + mChartSeries.size() + ")");
            }
            final int index = event.getIndexPosition();
            if (index >= 0 && index < mChartSeries.size()) {
                ChartSeries item = mChartSeries.get(event.getIndexPosition());
                if (event.getEventType() == DecoEvent.EventType.EVENT_COLOR_CHANGE) {
                    item.startAnimateColorChange(event);
                } else {
                    item.startAnimateMove(event);
                }
            } else {
                Log.e(TAG, "Ignoring move request: Invalid array index. Index: " + index + " Size: " + mChartSeries.size());
            }
        }
    }

    public void addEvent(@NonNull DecoEvent event) {
        getEventManager().add(event);
    }

    public void moveTo(int index, float position) {
        addEvent(new DecoEvent.Builder(position).setIndex(index).build());
    }

    public void moveTo(int index, float position, int duration) {
        if (duration == 0) {
            getChartSeries(index).setPosition(position);
            invalidate();
            return;
        }
        addEvent(new DecoEvent.Builder(position).setIndex(index).setDuration(duration).build());
    }

    public void executeReset() {
        if (mDecoEventManager != null) {
            mDecoEventManager.resetEvents();
        }
        if (mChartSeries != null) {
            for (ChartSeries chartSeries : mChartSeries) {
                chartSeries.reset();
            }
        }
    }

    public void deleteAll() {
        if (mDecoEventManager != null) {
            mDecoEventManager.resetEvents();
        }
        mChartSeries = null;
    }

    private boolean executeReveal(@NonNull DecoEvent event) {
        if ((event.getEventType() != DecoEvent.EventType.EVENT_SHOW) &&
                (event.getEventType() != DecoEvent.EventType.EVENT_HIDE)) {
            return false;
        }
        if (event.getEventType() == DecoEvent.EventType.EVENT_SHOW) {
            setVisibility(View.VISIBLE);
        }
        if (mChartSeries != null) {
            for (int i = 0; i < mChartSeries.size(); i++) {
                if ((event.getIndexPosition() == i) || (event.getIndexPosition() < 0)) {
                    ChartSeries chartSeries = mChartSeries.get(i);
                    chartSeries.startAnimateHideShow(event, event.getEventType() == DecoEvent.EventType.EVENT_SHOW);
                }
            }
        }
        return true;
    }

    private boolean executeEffect(@NonNull DecoEvent event) {
        if (event.getEventType() != DecoEvent.EventType.EVENT_EFFECT) {
            return false;
        }
        if (mChartSeries == null) {
            return false;
        }
        if (event.getIndexPosition() < 0) {
            Log.e(TAG, "EffectType " + event.getEventType().toString() + " must specify valid data series index");
            return false;
        }
        if (event.getEffectType() == DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE) {
            // hide all series, except the one to apply the effect
            for (int i = 0; i < mChartSeries.size(); i++) {
                ChartSeries chartSeries = mChartSeries.get(i);
                if (i != event.getIndexPosition()) {
                    chartSeries.startAnimateHideShow(event, false);
                } else {
                    chartSeries.startAnimateEffect(event);
                }
            }
            return true;
        }
        for (int i = 0; i < mChartSeries.size(); i++) {
            if ((event.getIndexPosition() == i) || event.getIndexPosition() < 0) {
                ChartSeries chartSeries = mChartSeries.get(i);
                chartSeries.startAnimateEffect(event);
            }
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDecoEventManager != null) {
            mDecoEventManager.resetEvents();
        }
    }

    @Override
    public void onExecuteEventStart(@NonNull DecoEvent event) {
        executeMove(event);
        executeReveal(event);
        executeEffect(event);
    }

    public void setVertGravity(VertGravity vertGravity) {
        mVertGravity = vertGravity;
    }

    public void setHorizGravity(HorizGravity horizGravity) {
        mHorizGravity = horizGravity;
    }

    public void enableCompatibilityMode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void disableHardwareAccelerationForDecoView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }
    @Deprecated
    public SeriesItem getSeriesItem(int index) {
        if (index >= 0 && index < mChartSeries.size()) {
            return mChartSeries.get(index).getSeriesItem();
        }
        return null;
    }
    public ChartSeries getChartSeries(int index) {
        if (index >= 0 && index < mChartSeries.size()) {
            return mChartSeries.get(index);
        }
        return null;
    }
    public enum VertGravity {
        GRAVITY_VERTICAL_TOP,
        GRAVITY_VERTICAL_CENTER,
        GRAVITY_VERTICAL_BOTTOM,
        GRAVITY_VERTICAL_FILL
    }
    public enum HorizGravity {
        GRAVITY_HORIZONTAL_LEFT,
        GRAVITY_HORIZONTAL_CENTER,
        GRAVITY_HORIZONTAL_RIGHT,
        GRAVITY_HORIZONTAL_FILL
    }
}
