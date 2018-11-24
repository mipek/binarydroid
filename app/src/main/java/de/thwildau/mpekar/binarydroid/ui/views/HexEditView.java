package de.thwildau.mpekar.binarydroid.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.nio.ByteBuffer;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.disasm.ByteAccessor;

public class HexEditView extends View {
    private long address;
    private ByteAccessor accessor;

    // TODO: add properties / auto detect somehow
    private final int numberRows = 15;
    private static final String HEXCHARS    = "0123456789ABCDEF";

    private int rowHeight;

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
            //textSize = a.getInt(R.styleable.HexEditView_textSize, 18);
            textColor = a.getColor(R.styleable.HexEditView_textColor, getResources().getColor(R.color.hexbytes));
            addressColor = a.getColor(R.styleable.HexEditView_addressColor, getResources().getColor(R.color.hexaddr));
            showCharacters = a.getBoolean(R.styleable.HexEditView_showCharacters, true);
        } finally {
            a.recycle();
        }

        invalidate();
    }


    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public ByteAccessor getAccessor() {
        return accessor;
    }

    public void setAccessor(ByteAccessor accessor) {
        this.accessor = accessor;
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

        int usedWidth = width / 5;
        ViewHelper.setTextSizeForWidth(paintAddr, width / 6, getMaxAddress());

        // enable character view in landscape mode
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewHelper.setTextSizeForWidth(paintString, width / 4, "MMMMMMMM");
            usedWidth += width / 4;
            showCharacters = false;//true;
        } else {
            showCharacters = false;
        }

        final String sampleBytes = "11 11 11 11 11 11 11 11";
        ViewHelper.setTextSizeForWidth(paintBytes, width - usedWidth - getPaddingLeft() - getPaddingRight(), sampleBytes);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getAccessor() == null) return;

        long address = getAddress();
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        for (int i=0; i<numberRows; ++i) {
            drawRow(canvas, address);
            canvas.translate(0, rowHeight + 1);
            address += 8; // 8 bytes per line
        }
        canvas.restore();
    }

    /// Draws a single hexview row
   // private byte [] buffer = new byte[8]; // 8 bytes per row
    private void drawRow(Canvas canvas, long offset) {
        final ByteAccessor access = getAccessor();
        final int width = getWidth();

        StringBuilder displayBytes = new StringBuilder(8 * 3);
        ByteBuffer bytes = access.getBytes(offset, 8);
        for (int i=0; i<bytes.limit(); ++i) {
            if (i != 0) {
                displayBytes.append(' ');
            }

            byte value = bytes.get(i);
            displayBytes.append(HEXCHARS.charAt((value & 0xF0) >> 4))
                        .append(HEXCHARS.charAt(value & 0x0F));
        }

        String addrStr = String.format("%08X", offset);
        canvas.drawText(addrStr, 0, rowHeight, paintAddr);
        canvas.drawText(displayBytes.toString(), paintAddr.measureText(addrStr), rowHeight, paintBytes);

        if (showCharacters) {
            StringBuilder displayChars = new StringBuilder(8 * 2);
            for (int i=0; i<bytes.limit(); ++i) {
                // we can use "getChar" because it reads two characters
                displayChars.append((char)bytes.get(i));
            }

            canvas.drawText(displayChars.toString(), getWidth() - width / 4, rowHeight, paintString);
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
        paintBytes.setTextSize(rowHeight);
        paintBytes.setTextAlign(Paint.Align.LEFT);

        paintString.setColor(Color.GRAY);
        paintString.setTextSize(rowHeight);
        paintString.setTextAlign(Paint.Align.LEFT);
    }
}
