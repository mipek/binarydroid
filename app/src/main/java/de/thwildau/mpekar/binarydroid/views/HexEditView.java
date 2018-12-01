package de.thwildau.mpekar.binarydroid.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
import de.thwildau.mpekar.binarydroid.model.Architectures;
import de.thwildau.mpekar.binarydroid.model.Container;
import de.thwildau.mpekar.binarydroid.ui.disasm.DisassemblerViewModel;

public class HexEditView extends ScrollableView {
    // variables w/ public access
    private long address;
    private DisassemblerViewModel vm;

    // variables w/ private access
    private int rowHeight;
    private boolean showCharacters;

    // properties
    private int textColor;
    private int addressColor;
    private Paint paintAddr;
    private Paint paintBytes;
    private Paint paintString;

    public HexEditView(Context context, AttributeSet attrs) {
        super(context, attrs, false, true);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HexEditView,
                0, 0);

        try {
            //textSize = a.getInt(R.styleable.HexEditView_textSize, 18);
            textColor = a.getColor(R.styleable.HexEditView_textColor, getResources().getColor(R.color.hexbytes));
            addressColor = a.getColor(R.styleable.HexEditView_addressColor, getResources().getColor(R.color.hexaddr));
            showCharacters = a.getBoolean(R.styleable.HexEditView_showCharacters, false);
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

    public DisassemblerViewModel getVm() {
        return vm;
    }

    public void setVm(DisassemblerViewModel vm) {
        this.vm = vm;
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

    // Return max address for this architecture.
    private String getMaxAddress() {
        switch (getVm().getBinary().getValue().getArch()) {
            case ARM64:
            case AMD64:
                return "ffffffffffffffff";
            case ARM:
            case X86:
                return "ffffffff";
            default:
                throw new RuntimeException("unknown arch");
        }
    }

    // Tests whether or not the view has a viewmodel & accessor
    private boolean isReady() {
        return (getVm() != null && getVm().getAccessor() != null);
    }

    private ByteAccessor getAccessor() {
        return getVm().getAccessor().getValue();
    }

    private long getTotalBytes() {
        return getAccessor().getTotalBytes();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int desiredWidth = getResources().getDisplayMetrics().widthPixels;
        int desiredHeight = getResources().getDisplayMetrics().heightPixels;
        int width, height;

        width = ViewHelper.handleSize(widthMode, desiredWidth, widthSize);
        height = ViewHelper.handleSize(heightMode, desiredHeight, heightSize);

        rowHeight = (int)(height * 0.05f);

        int usedWidth = width / 5;
        ViewHelper.setTextSizeForWidth(paintAddr, width / 6, getMaxAddress());

        // enable character view in landscape mode
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewHelper.setTextSizeForWidth(paintString, width / 4, "MMMMMMMM");
            usedWidth += width / 4;
            showCharacters = true;
        } else {
            showCharacters = false;
        }

        final String sampleBytes = "11 11 11 11 11 11 11 11";
        ViewHelper.setTextSizeForWidth(paintBytes, width - usedWidth - getPaddingLeft() - getPaddingRight(), sampleBytes);

        setMeasuredDimension(width, height);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawRect(0,0,getWidth(),getHeight(), paintString);
        if (!isReady()) return;

        // get start address
        long address = getAddress();

        // figure out where we are at (in the file)
        long offset = (int)(getPositionY() / rowHeight) * 8;
        Log.d("BinaryDroid", "delta: " + (getPositionY() / rowHeight) + ", offset: " + offset);
        address += offset;

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        float drawY = getPaddingTop();
        float maxY = getHeight() - getPaddingTop();
        do {
            drawRow(canvas, address);
            canvas.translate(0, rowHeight);
            address += 8; // 8 bytes per line
            drawY += rowHeight;
        } while(drawY < maxY && address < getTotalBytes()); //(drawY < canvas.getHeight());
        canvas.restore();
    }

    /// Draws a single hexview row
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
            Utils.b2s(displayBytes, value);
        }

        byte wordSize = getVm().getBinary().getValue().getWordSize();
        String addrStr = Utils.l2s(offset, wordSize);//String.format("%08X", offset);
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
