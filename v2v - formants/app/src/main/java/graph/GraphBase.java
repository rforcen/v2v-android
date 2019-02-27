package graph;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import Signal.AsyncListener;

public class GraphBase extends View {

    public GraphBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void refresh() {
        postInvalidate();
    }

    public AsyncListener getListener() {
        return new AsyncListener() {
            @Override
            public void onDataReady() {
                refresh();
            }
        };
    }
}
