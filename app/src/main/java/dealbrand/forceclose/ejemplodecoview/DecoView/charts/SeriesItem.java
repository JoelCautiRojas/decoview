package dealbrand.forceclose.ejemplodecoview.DecoView.charts;

import android.graphics.Color;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import java.util.ArrayList;

public class SeriesItem {
    private int mColor;
    private int mColorSecondary;
    private float mLineWidth;
    private final long mSpinDuration;
    private final float mMinValue;
    private final float mMaxValue;
    private final float mInitialValue;
    private final boolean mInitialVisibility;
    private final boolean mSpinClockwise;
    private final boolean mRoundCap;
    private final boolean mDrawAsPoint;
    private final ChartStyle mChartStyle;
    private final Interpolator mInterpolator;
    private final boolean mShowPointWhenEmpty;
    private PointF mInset;
    private ArrayList<EdgeDetail> mEdgeDetail;
    private SeriesLabel mSeriesLabel;
    private float mShadowSize;
    private int mShadowColor;
    private ArrayList<SeriesItemListener> mListeners;

    private SeriesItem(Builder builder) {
        mColor = builder.mColor;
        mColorSecondary = builder.mColorSecondary;
        mLineWidth = builder.mLineWidth;
        mSpinDuration = builder.mSpinDuration;
        mMinValue = builder.mMinValue;
        mMaxValue = builder.mMaxValue;
        mInitialValue = builder.mInitialValue;
        mInitialVisibility = builder.mInitialVisibility;
        mSpinClockwise = builder.mSpinClockwise;
        mRoundCap = builder.mRoundCap;
        mDrawAsPoint = builder.mDrawAsPoint;
        mChartStyle = builder.mChartStyle;
        mInterpolator = builder.mInterpolator;
        mShowPointWhenEmpty = builder.mShowPointWhenEmpty;
        mInset = builder.mInset;
        mEdgeDetail = builder.mEdgeDetail;
        mSeriesLabel = builder.mSeriesLabel;
        mShadowSize = builder.mShadowSize;
        mShadowColor = builder.mShadowColor;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getSecondaryColor() {
        return mColorSecondary;
    }

    public void setSecondaryColor(int color) {
        mColorSecondary = color;
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
    }

    public long getSpinDuration() {
        return mSpinDuration;
    }

    public float getMinValue() {
        return mMinValue;
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public float getInitialValue() {
        return mInitialValue;
    }

    public boolean getInitialVisibility() {
        return mInitialVisibility;
    }

    public boolean getSpinClockwise() {
        return mSpinClockwise;
    }

    public boolean getRoundCap() {
        return mRoundCap;
    }

    public boolean getDrawAsPoint() {
        return mDrawAsPoint;
    }

    public ChartStyle getChartStyle() {
        return mChartStyle;
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    public boolean showPointWhenEmpty() {
        return mShowPointWhenEmpty;
    }

    public PointF getInset() {
        if (mInset == null) {
            mInset = new PointF(0, 0);
        }
        return mInset;
    }

    public ArrayList<EdgeDetail> getEdgeDetail() {
        return mEdgeDetail;
    }

    public void addEdgeDetail(@Nullable EdgeDetail edgeDetail) {
        if (edgeDetail == null) {
            mEdgeDetail = null;
            return;
        }
        if (mEdgeDetail == null) {
            mEdgeDetail = new ArrayList<>();
        }
        mEdgeDetail.add(new EdgeDetail(edgeDetail));
    }

    public void setSeriesLabel(SeriesLabel label) {
        mSeriesLabel = label;
    }

    public SeriesLabel getSeriesLabel() {
        return mSeriesLabel;
    }

    public void setShadowSize(float shadowSize) {
        mShadowSize = shadowSize;
    }

    public float getShadowSize() {
        return mShadowSize;
    }

    public void setShadowColor(int shadowColor) {
        mShadowColor = shadowColor;
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    /**
     * Set a listener to get notification of completion of animation
     *
     * @param listener OrbSeriesItemListener to be used for callbacks
     */
    public void addArcSeriesItemListener(@NonNull SeriesItemListener listener) {

        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    ArrayList<SeriesItemListener> getListeners() {
        return mListeners;
    }

    public enum ChartStyle {
        STYLE_DONUT, /* Default: Hole in middle */
        STYLE_PIE, /* Drawn from center point to outer limit */
        STYLE_LINE_HORIZONTAL, /* Drawn as a horizontal straight line */
        STYLE_LINE_VERTICAL /* Drawn as a horizontal straight line */
    }

    /**
     * Callback interface for notification of animation end
     */
    public interface SeriesItemListener {
        void onSeriesItemAnimationProgress(float percentComplete, float currentPosition);

        void onSeriesItemDisplayProgress(float percentComplete);
    }

    public static class Builder {
        private int mColor = Color.argb(255, 32, 32, 32);
        private int mColorSecondary = Color.argb(0, 0, 0, 0);
        private float mLineWidth = -1;
        private long mSpinDuration = 5000;
        private float mMinValue;
        private float mMaxValue = 100f;
        private float mInitialValue;
        private boolean mInitialVisibility = true;
        private boolean mSpinClockwise = true;
        private boolean mRoundCap = true;
        private boolean mDrawAsPoint;
        private ChartStyle mChartStyle = ChartStyle.STYLE_DONUT;
        private Interpolator mInterpolator;
        private boolean mShowPointWhenEmpty = true;
        private PointF mInset;
        private ArrayList<EdgeDetail> mEdgeDetail;
        private SeriesLabel mSeriesLabel;
        private float mShadowSize = 0f;
        private int mShadowColor = Color.BLACK;

        public Builder(int color) {
            mColor = color;
        }

        public Builder(int color, int colorSecondary) {
            mColor = color;
            mColorSecondary = colorSecondary;
        }

        public Builder setLineWidth(final float lineWidth) {
            mLineWidth = lineWidth;
            return this;
        }

        public Builder setSpinDuration(final long spinDuration) {
            if (spinDuration <= 100) {
                throw new IllegalArgumentException("SpinDuration must be > 100 (value is in ms)");
            }
            mSpinDuration = spinDuration;
            return this;
        }

        public Builder setInitialVisibility(final boolean visibility) {
            mInitialVisibility = visibility;
            return this;
        }

        public Builder setSpinClockwise(final boolean spinClockwise) {
            mSpinClockwise = spinClockwise;
            return this;
        }

        public Builder setCapRounded(final boolean roundCap) {
            mRoundCap = roundCap;
            return this;
        }

        public Builder setDrawAsPoint(final boolean drawAsPoint) {
            mDrawAsPoint = drawAsPoint;
            return this;
        }

        public Builder setChartStyle(@NonNull final ChartStyle chartStyle) {
            mChartStyle = chartStyle;
            return this;
        }

        public Builder setRange(final float minValue, final float maxValue, final float initialValue) {
            if (minValue >= maxValue) {
                throw new IllegalArgumentException("minimum value must be less that maximum value");
            }
            if (minValue > initialValue || maxValue < initialValue) {
                throw new IllegalArgumentException("Initial value must be in the range of min .. max");
            }
            mMinValue = minValue;
            mMaxValue = maxValue;
            mInitialValue = initialValue;

            return this;
        }

        public Builder setInterpolator(@Nullable Interpolator interpolator) {
            mInterpolator = interpolator;
            return this;
        }

        public Builder setShowPointWhenEmpty(boolean showPointWhenEmpty) {
            mShowPointWhenEmpty = showPointWhenEmpty;
            return this;
        }

        public Builder setInset(@Nullable PointF inset) {
            mInset = inset;
            return this;
        }

        public Builder addEdgeDetail(@Nullable EdgeDetail edgeDetail) {
            if (edgeDetail == null) {
                mEdgeDetail = null;
                return this;
            }
            if (mEdgeDetail == null) {
                mEdgeDetail = new ArrayList<>();
            }
            mEdgeDetail.add(new EdgeDetail(edgeDetail));
            return this;
        }

        public Builder setSeriesLabel(@Nullable SeriesLabel seriesLabel) {
            mSeriesLabel = seriesLabel;
            return this;
        }

        public Builder setShadowSize(float shadowSize) {
            mShadowSize = shadowSize;
            return this;
        }

        public Builder setShadowColor(int shadowColor) {
            mShadowColor = shadowColor;
            return this;
        }
        public SeriesItem build() {
            return new SeriesItem(this);
        }

    }
}
