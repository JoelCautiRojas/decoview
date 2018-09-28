package dealbrand.forceclose.ejemplodecoview.DecoView.util;

import android.content.Context;

public class FuncionesGenericas {

    static private boolean mInitialized;
    static private float mScaledDensity = 3.0f;

    static public void initialize(Context context){
        mInitialized = true;
        mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    }

    static public float pixelsToSp(final float px) {
        verifyInitialized();
        return px / mScaledDensity;
    }

    static public float spToPixels(final float sp) {
        verifyInitialized();
        return sp * mScaledDensity;
    }

    static public void verifyInitialized()
            throws IllegalStateException {
        if (!mInitialized) {
            throw new IllegalStateException("Missing call to GenericFunctions::initialize()");
        }
    }
}
