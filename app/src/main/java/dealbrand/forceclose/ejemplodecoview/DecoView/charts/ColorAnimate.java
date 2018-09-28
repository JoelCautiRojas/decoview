package dealbrand.forceclose.ejemplodecoview.DecoView.charts;

import android.graphics.Color;

public class ColorAnimate {
    static public final int ANIMATE_ALPHA = 0x01;
    static public final int ANIMATE_RED = 0x02;
    static public final int ANIMATE_GREEN = 0x04;
    static public final int ANIMATE_BLUE = 0x08;
    static public final int ANIMATE_ALL = ANIMATE_ALPHA | ANIMATE_RED | ANIMATE_GREEN | ANIMATE_BLUE;

    private int mMask;
    private final int mColorStart;
    private final int mColorEnd;
    private int mColorCurrent;

    public ColorAnimate(int colorStart, int colorEnd) {
        mColorStart = colorStart;
        mColorEnd = colorEnd;
        mColorCurrent = mColorStart;
        setMask(ANIMATE_ALL);
    }

    public void setMask(int mask) {
        mMask = mask;
    }

    private int getValue(int mask, int start, int end, float percent) {
        if ((mask & mMask) == 0) {
            return start;
        }

        return start + (int) ((end - start) * percent);
    }

    public int getColorCurrent(float percentComplete) {
        mColorCurrent = Color.argb(
                getValue(ANIMATE_ALPHA, Color.alpha(mColorStart), Color.alpha(mColorEnd), percentComplete),
                getValue(ANIMATE_RED, Color.red(mColorStart), Color.red(mColorEnd), percentComplete),
                getValue(ANIMATE_GREEN, Color.green(mColorStart), Color.green(mColorEnd), percentComplete),
                getValue(ANIMATE_BLUE, Color.blue(mColorStart), Color.blue(mColorEnd), percentComplete));
        return mColorCurrent;
    }
}
