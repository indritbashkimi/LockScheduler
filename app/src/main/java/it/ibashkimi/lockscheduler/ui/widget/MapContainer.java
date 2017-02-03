package it.ibashkimi.lockscheduler.ui.widget;

import android.content.Context;
import android.support.percent.PercentFrameLayout;
import android.util.AttributeSet;
import android.view.View;

import it.ibashkimi.lockscheduler.R;


public class MapContainer extends PercentFrameLayout {
    public MapContainer(Context context) {
        this(context, null);
    }

    public MapContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.map_container, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}
