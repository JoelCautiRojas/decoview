package dealbrand.forceclose.ejemplodecoview.DecoView.events;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import dealbrand.forceclose.ejemplodecoview.DecoView.charts.DecoDrawEffect;

public class DecoEvent {

    static public final long EVENT_ID_UNSPECIFIED = -1;
    private final String TAG = getClass().getSimpleName();
    private final EventType mType;
    private final long mEventID;
    private final long mDelay;
    private final DecoDrawEffect.EffectType mEffectType;
    private final long mFadeDuration;
    private final View[] mLinkedViews;
    private final long mEffectDuration;
    private final int mIndexPosition;
    private final int mEffectRotations;
    private final String mDisplayText;
    private final float mEndPosition;
    private final int mColor;
    private final Interpolator mInterpolator;
    private final ExecuteEventListener mListener;

    private DecoEvent(Builder builder){
        mType = builder.mType;
        mEventID = builder.mEventID;
        mDelay = builder.mDelay;
        mEffectType = builder.mEffectType;
        mFadeDuration = builder.mFadeDuration;
        mLinkedViews = builder.mLinkedViews;
        mEffectDuration = builder.mEffectDuration;
        mIndexPosition = builder.mIndex;
        mEffectRotations = builder.mEffectRotations;
        mDisplayText = builder.mDisplayText;
        mEndPosition = builder.mEndPosition;
        mColor = builder.mColor;
        mInterpolator = builder.mInterpolator;
        mListener = builder.mListener;
        if (mEventID != EVENT_ID_UNSPECIFIED && mListener == null) {
            Log.w(TAG, "EventID redundant without specifying an event listener");
        }
    }

    public EventType getEventType() {
        return mType;
    }

    public long getEventID() {
        return mEventID;
    }

    public long getDelay() {
        return mDelay;
    }

    public DecoDrawEffect.EffectType getEffectType() {
        return mEffectType;
    }

    public long getFadeDuration() {
        return mFadeDuration;
    }

    public View[] getLinkedViews() {
        return mLinkedViews;
    }

    public long getEffectDuration() {
        return mEffectDuration;
    }

    public int getIndexPosition() {
        return mIndexPosition;
    }

    public int getEffectRotations() {
        return mEffectRotations;
    }

    public String getDisplayText() {
        return mDisplayText;
    }

    public float getEndPosition() {
        return mEndPosition;
    }

    public int getColor() {
        return mColor;
    }

    public boolean isColorSet() {
        return Color.alpha(mColor) > 0;
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    public void notifyEndListener() {
        if (mListener != null) {
            mListener.onEventEnd(this);
        }
    }

    public void notifyStartListener() {
        if (mListener != null) {
            mListener.onEventStart(this);
        }
    }

    public enum EventType {
        EVENT_MOVE, /* Move the current position of the chart series */
        EVENT_SHOW, /* Show the chart series using reveal animation */
        EVENT_HIDE, /* Hide the chart series using an animation */
        EVENT_EFFECT, /* Apply effect animation on the series */
        EVENT_COLOR_CHANGE /* Change the color of the series over time */
    }

    public interface ExecuteEventListener {
        void onEventStart(DecoEvent event);

        void onEventEnd(DecoEvent event);
    }

    public static class Builder {
        private final EventType mType;
        private long mEventID = EVENT_ID_UNSPECIFIED;
        private long mDelay;
        private DecoDrawEffect.EffectType mEffectType;
        private long mFadeDuration = 1000;
        private View[] mLinkedViews;
        private long mEffectDuration = -1;
        private int mIndex = -1;
        private int mEffectRotations = 2;
        private String mDisplayText;
        private float mEndPosition;
        private int mColor = Color.parseColor("#00000000");
        private Interpolator mInterpolator;
        private ExecuteEventListener mListener;

        public Builder(float endPosition) {
            mType = EventType.EVENT_MOVE;
            mEndPosition = endPosition;
        }
        public Builder(@NonNull DecoDrawEffect.EffectType effectType) {
            mType = EventType.EVENT_EFFECT;
            mEffectType = effectType;
        }
        public Builder(EventType eventType, boolean showView) {
            if (EventType.EVENT_HIDE != eventType && EventType.EVENT_SHOW != eventType) {
                throw new IllegalArgumentException("Invalid arguments for EventType. Use Alternative constructor");
            }
            mType = showView ? EventType.EVENT_SHOW : EventType.EVENT_HIDE;
        }

        public Builder(EventType eventType, int color) {
            if (EventType.EVENT_COLOR_CHANGE != eventType) {
                throw new IllegalArgumentException("Must specify EVENT_COLOR_CHANGE when setting new color");
            }
            mType = eventType;
            mColor = color;
        }
        public Builder setEventID(long eventID) {
            mEventID = eventID;
            return this;
        }
        public Builder setIndex(int indexPosition) {
            mIndex = indexPosition;
            return this;
        }
        public Builder setDelay(long delay) {
            mDelay = delay;
            return this;
        }
        public Builder setDuration(long effectDuration) {
            mEffectDuration = effectDuration;
            return this;
        }
        public Builder setFadeDuration(long fadeDuration) {
            mFadeDuration = fadeDuration;
            return this;
        }
        public Builder setEffectRotations(int effectRotations) {
            mEffectRotations = effectRotations;
            return this;
        }
        public Builder setDisplayText(String displayText) {
            mDisplayText = displayText;
            return this;
        }
        public Builder setLinkedViews(View[] linkedViews) {
            mLinkedViews = linkedViews;
            return this;
        }
        public Builder setInterpolator(Interpolator interpolator) {
            mInterpolator = interpolator;
            return this;
        }
        public Builder setColor(int color) {
            mColor = color;
            return this;
        }
        public Builder setListener(ExecuteEventListener listener) {
            mListener = listener;
            return this;
        }

        public DecoEvent build() {
            return new DecoEvent(this);
        }
    }
}
