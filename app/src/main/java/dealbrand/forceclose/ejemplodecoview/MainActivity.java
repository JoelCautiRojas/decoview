package dealbrand.forceclose.ejemplodecoview;

import android.graphics.Color;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import dealbrand.forceclose.ejemplodecoview.DecoView.DecoView;
import dealbrand.forceclose.ejemplodecoview.DecoView.charts.EdgeDetail;
import dealbrand.forceclose.ejemplodecoview.DecoView.charts.SeriesItem;
import dealbrand.forceclose.ejemplodecoview.DecoView.charts.SeriesLabel;
import dealbrand.forceclose.ejemplodecoview.DecoView.events.DecoEvent;

public class MainActivity extends AppCompatActivity {

    DecoView deco;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deco = findViewById(R.id.decor);
        tv = findViewById(R.id.texto);

        int indice = 0;

        SeriesItem item = new SeriesItem.Builder(Color.argb(200,255,0,0))
                .setRange(0,100,0)
                .setInitialVisibility(false)
                .setLineWidth(20)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_OUTER,Color.parseColor("#22000000"),0.4f))
                .setSeriesLabel(new SeriesLabel.Builder("Valor %.0f%%").build())
                .setInterpolator(new DecelerateInterpolator())
                .setShowPointWhenEmpty(true)
                .setCapRounded(true)
                .setInset(new PointF(20f,20f))
                .setDrawAsPoint(false)
                .setSpinClockwise(true)
                .setSpinDuration(2000)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        indice = deco.addSeries(item);
        deco.addEvent(
                new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW,true)
                .setDelay(1000)
                .setDuration(2000)
                .build()
        );
        deco.addEvent(
                new DecoEvent.Builder(25).setIndex(indice).setDelay(4000).build()
        );
        deco.addEvent(
                new DecoEvent.Builder(100).setIndex(indice).setDelay(8000).build()
        );
        deco.addEvent(
                new DecoEvent.Builder(10).setIndex(indice).setDelay(12000).build()
        );
    }
}
