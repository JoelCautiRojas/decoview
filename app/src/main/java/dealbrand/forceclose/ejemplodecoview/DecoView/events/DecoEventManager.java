package dealbrand.forceclose.ejemplodecoview.DecoView.events;

import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import dealbrand.forceclose.ejemplodecoview.DecoView.charts.DecoDrawEffect;

public class DecoEventManager {

    private final Handler mHandler = new Handler();

    private final ArcEventManagerListener mListener;

    public DecoEventManager(ArcEventManagerListener mListener) {
        this.mListener = mListener;
    }

    public void add(final DecoEvent event){
        final boolean show = (event.getEventType() == DecoEvent.EventType.EVENT_SHOW) ||
                (event.getEffectType() == DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT) ||
                (event.getEffectType() == DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT_FILL);
        final boolean ignore = event.getEventType() == DecoEvent.EventType.EVENT_MOVE;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (show && event.getLinkedViews() != null) {
                    for (View view : event.getLinkedViews()) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && view instanceof TextView) {
                            TextView textView = (TextView) view;
                            if (textView.getText().length() <= 0) {
                                textView.setText(" ");
                            }
                        }
                        view.setVisibility(View.VISIBLE);
                    }
                }
                if (!ignore && event.getLinkedViews() != null) {
                    for (final View view : event.getLinkedViews()) {
                        AlphaAnimation anim = new AlphaAnimation(show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
                        anim.setDuration(event.getFadeDuration());
                        anim.setFillAfter(true);
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                view.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        view.startAnimation(anim);
                    }
                }
                if (mListener != null) {
                    mListener.onExecuteEventStart(event);
                }
            }
        }, event.getDelay());
    }

    public void resetEvents() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public interface ArcEventManagerListener {
        void onExecuteEventStart(@NonNull DecoEvent event);
    }
}
