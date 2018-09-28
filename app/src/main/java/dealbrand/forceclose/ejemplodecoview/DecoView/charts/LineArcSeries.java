package dealbrand.forceclose.ejemplodecoview.DecoView.charts;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

public class LineArcSeries extends ArcSeries{
    public LineArcSeries(@NonNull SeriesItem seriesItem, int totalAngle, int rotateAngle) {
        super(seriesItem, totalAngle, rotateAngle);
    }
    @Override
    public boolean draw(Canvas canvas, RectF bounds) {
        if (super.draw(canvas, bounds)) {
            return true;
        }
        drawArc(canvas);
        drawArcEdgeDetail(canvas);
        return true;
    }

    protected void drawArc(@NonNull Canvas canvas) {
        if (mArcAngleSweep == 0) {
            return;
        }
        canvas.drawArc(mBoundsInset,
                mArcAngleStart,
                mArcAngleSweep,
                false,
                mPaint);
    }

    private void drawArcEdgeDetail(@NonNull Canvas canvas) {
        ArrayList<EdgeDetail> edgeDetailList = getSeriesItem().getEdgeDetail();
        if (edgeDetailList == null) {
            return;
        }
        for (EdgeDetail edgeDetail : edgeDetailList) {
            final boolean drawInner = edgeDetail.getEdgeType() == EdgeDetail.EdgeType.EDGE_INNER;
            if (edgeDetail.getClipPath() == null) {
                float inset = (edgeDetail.getRatio() - 0.5f) * mSeriesItem.getLineWidth();
                if (drawInner) {
                    inset = -inset;
                }
                Path clipPath = new Path();
                RectF clipRect = new RectF(mBoundsInset);
                clipRect.inset(inset, inset);
                clipPath.addOval(clipRect, Path.Direction.CW);
                edgeDetail.setClipPath(clipPath);
            }
            drawClippedArc(canvas, edgeDetail.getClipPath(), edgeDetail.getColor(),
                    drawInner ? Region.Op.INTERSECT : Region.Op.DIFFERENCE);
        }
    }

    protected void drawClippedArc(@NonNull Canvas canvas, @NonNull Path path, int color, @NonNull Region.Op combine) {
        canvas.save();
        try {
            canvas.clipPath(path, combine);
        } catch (UnsupportedOperationException e) {
            Log.w(TAG, "clipPath unavailable on API 11 - 17 without disabling hardware acceleration. (EdgeDetail functionality requires clipPath). Call DecoView.enableCompatibilityMode() to enable");
            canvas.restore();
            return;
        }
        int colorOld = mPaint.getColor();
        mPaint.setColor(color);
        drawArc(canvas);
        mPaint.setColor(colorOld);
        canvas.restore();
    }
}
