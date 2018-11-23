package de.thwildau.mpekar.binarydroid.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.disasm.BinaryAccessor;

public class HexEditView extends View {
    private long cursor;
    private BinaryAccessor accessor;

    // TODO: add properties / auto detect somehow
    private final int numberRows = 12;

    private int rowHeight;

    private int textSize;
    private int textColor;
    private int addressColor;
    private boolean showCharacters;
    private Paint paintAddr;
    private Paint paintBytes;
    private Paint paintString;

    public HexEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HexEditView,
                0, 0);

        try {
            textSize = a.getInt(R.styleable.HexEditView_textSize, 18);
            textColor = a.getColor(R.styleable.HexEditView_textColor, getResources().getColor(R.color.hexbytes));
            addressColor = a.getColor(R.styleable.HexEditView_addressColor, getResources().getColor(R.color.hexaddr));
            showCharacters = a.getBoolean(R.styleable.HexEditView_showCharacters, true);
        } finally {
            a.recycle();
        }

        invalidate();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getAddressColor() {
        return addressColor;
    }

    public void setAddressColor(int addressColor) {
        this.addressColor = addressColor;
        invalidate();
    }

    public boolean isShowCharacters() {
        return showCharacters;
    }

    public void setShowCharacters(boolean showCharacters) {
        this.showCharacters = showCharacters;
        invalidate();
        requestLayout();
    }

    private String getMaxAddress() {
        // TODO: support 64bit architectures
        return "ffffffff";
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int desiredWidth = getResources().getDisplayMetrics().widthPixels;
        int desiredHeight = getResources().getDisplayMetrics().heightPixels/10;
        int width, height;

        width = ViewHelper.handleSize(widthMode, desiredWidth, widthSize);
        height = ViewHelper.handleSize(heightMode, desiredHeight, heightSize);

        rowHeight = height / numberRows;

        int usedWidth = width / 4;
        ViewHelper.setTextSizeForWidth(paintAddr, width / 4, getMaxAddress());

        // enable character view in landscape mode
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewHelper.setTextSizeForWidth(paintString, width / 4, "MMMMMMMM");
            usedWidth *= 2;
        }

        final String sampleBytes = "42 42 42 42 42 42 42 42";
        ViewHelper.setTextSizeForWidth(paintBytes, width - usedWidth, sampleBytes);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        for (int i=0; i<numberRows; ++i) {
            drawRow(canvas);
            canvas.translate(0, rowHeight + 1);
        }
        canvas.restore();
    }

    /// Draws a single hexview row
    private void drawRow(Canvas canvas) {
        int width = getWidth();

        canvas.drawText(getMaxAddress(), 1, rowHeight, paintAddr);
        canvas.drawText("42 42 42 42 42 42 42 42", getWidth() / 4, rowHeight, paintBytes);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            canvas.drawText("MZ......", getWidth() - width / 4, rowHeight, paintString);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        paintAddr = new Paint();
        paintBytes = new Paint();
        paintString = new Paint();

        paintAddr.setColor(getAddressColor());
        paintAddr.setTextSize(rowHeight);
        paintAddr.setTextAlign(Paint.Align.LEFT);

        paintBytes.setColor(getTextColor());
        paintBytes.setTextSize(getTextSize());
        paintBytes.setTextAlign(Paint.Align.LEFT);

        paintString.setColor(Color.GRAY);
        paintString.setTextSize(getTextSize());
        paintString.setTextAlign(Paint.Align.LEFT);
    }
}
