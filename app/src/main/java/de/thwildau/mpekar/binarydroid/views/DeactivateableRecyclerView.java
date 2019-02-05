package de.thwildau.mpekar.binarydroid.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class DeactivateableRecyclerView extends RecyclerView {
    private Paint overlayPaint;

    public DeactivateableRecyclerView(Context context) {
        super(context);
    }

    public DeactivateableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DeactivateableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        overlayPaint = new Paint();
        overlayPaint.setColor(Color.GRAY);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        if (!isEnabled()) {
            c.drawRect(getPaddingLeft(), getPaddingTop(), getWidth(), getHeight(), overlayPaint);
        }
    }
}
