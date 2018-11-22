package de.thwildau.mpekar.binarydroid.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import de.thwildau.mpekar.binarydroid.R;

public class HexEditView extends View {
    private long address;

    private int textSize;
    private int textColor;
    private int addressColor;
    private boolean showCharacters;
    private Paint paintAddr;
    private Paint paintBytes;

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

        init();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int desiredWidth = getResources().getDisplayMetrics().widthPixels;
        int desiredHeight = getResources().getDisplayMetrics().heightPixels/10;
        int width, height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawRect(0f, 0f, getWidth(), getHeight(), paintAddr);
        //Long.toHexString(address)
        //canvas.drawText("00000000", getPaddingLeft(), getPaddingTop(), paintAddr);
        //canvas.drawText("00000000", 0, 0, paintAddr);
        final String mText = "00000000";
        canvas.drawText(mText, 0, mText.length() - 1, 0, canvas.getHeight(), paintAddr);
    }

    private void init() {
        paintAddr = new Paint();
        paintBytes = new Paint();

        paintAddr.setColor(getAddressColor());
        paintAddr.setTextSize(getTextSize());
        paintAddr.setTextAlign(Paint.Align.LEFT);

        paintBytes.setColor(getTextColor());
        paintBytes.setTextSize(getTextSize());
        paintBytes.setTextAlign(Paint.Align.LEFT);
    }
}
