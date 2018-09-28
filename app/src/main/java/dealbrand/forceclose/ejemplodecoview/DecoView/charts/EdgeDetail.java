package dealbrand.forceclose.ejemplodecoview.DecoView.charts;

import android.graphics.Path;
import android.support.annotation.NonNull;

public class EdgeDetail {
    private final int mColor;
    private final float mRatio;
    private final EdgeType mEdgeType;
    private Path mClipPath;

    @SuppressWarnings("unused")
    public EdgeDetail(@NonNull EdgeType edgeType, int color, float percentRatio) {
        if (percentRatio > 1.0 || percentRatio < 0) {
            throw new IllegalArgumentException("Invalid ratio set for EdgeDetail");
        }
        mEdgeType = edgeType;
        mColor = color;
        mRatio = percentRatio;
    }

    EdgeDetail(@NonNull final EdgeDetail edgeDetail) {
        mEdgeType = edgeDetail.mEdgeType;
        mColor = edgeDetail.mColor;
        mRatio = edgeDetail.mRatio;
        mClipPath = null;
    }
    public int getColor() {
        return mColor;
    }

    public float getRatio() {
        return mRatio;
    }

    public EdgeType getEdgeType() {
        return mEdgeType;
    }

    Path getClipPath() {
        return mClipPath;
    }

    void setClipPath(Path clipPath) {
        mClipPath = clipPath;
    }

    @SuppressWarnings("unused")
    public enum EdgeType {
        EDGE_INNER,
        EDGE_OUTER
    }
}
