package de.thwildau.mpekar.binarydroid.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import java.nio.ByteBuffer;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
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
        Container c = getVm().getBinary().getValue();
        if  (c == null) return "ffffffff";
        if  (c == null) return "ffffffff";

        switch (c.getArch()) {
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

        int padding = 200;
        int usedWidth = width / 6;
        ViewHelper.setTextSizeForWidth(paintAddr, width / 6, getMaxAddress());

        // enable ASCII character view in landscape mode
        int orientation = getResources().getConfiguration().orientation;
        String sampleBytes;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rowHeight = (int)(height / 10);
            showCharacters = true;
            sampleBytes = "11 11 11 11 11 11 11 11 11 11 11 11 11 11 11 11";

            int textWidth = width / 6;
            ViewHelper.setTextSizeForWidth(paintString, textWidth, "MMMMMMMM");
            usedWidth += textWidth;
        } else {
            rowHeight = (int)(height / 20);// * 0.05f);
            showCharacters = false;
            sampleBytes = "11 11 11 11 11 11 11 11";
        }
        ViewHelper.setTextSizeForWidth(paintBytes, width - usedWidth, sampleBytes);

        setMeasuredDimension(width, height);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawRect(0,0,getWidth(),getHeight(), paintString);
        if (!isReady()) return;
        if (getAccessor() == null) return;

        // get start address
        long address = getAddress();

        // figure out where we are at (in the file)
        long offset = (int)(getPositionY() / rowHeight) * 8;
        Log.d("BinaryDroid", "scroll delta: " + (getPositionY() / rowHeight) + ", offset: " + offset);
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

        byte wordSize = getVm().getBinary().getValue().getWordSize();
        int bytesPerLine = 8;
        if (showCharacters) {
            bytesPerLine = 16;
        }

        // Draw address
        String addrStr = Utils.l2s(offset, wordSize);
        canvas.drawText(addrStr, 0, rowHeight, paintAddr);

        StringBuilder displayBytes = new StringBuilder(bytesPerLine * 3);
        ByteBuffer bytes = access.getBytes(offset, bytesPerLine);
        for (int i=0; i<bytes.limit(); ++i) {
            if (i != 0) {
                displayBytes.append(' ');
            }

            byte value = bytes.get(i);
            Utils.b2s(displayBytes, value);
        }

        // Draw bytes
        canvas.drawText(
                displayBytes.toString(),
                paintAddr.measureText(addrStr), rowHeight, paintBytes);

        // Draw ASCII characters (when required to do so)
        if (showCharacters) {
            StringBuilder displayChars = new StringBuilder(bytesPerLine * 2);
            for (int i=0; i<bytes.limit(); ++i) {
                // we can use "getChar" because it reads two characters
                displayChars.append((char)bytes.get(i));
            }

            canvas.drawText(displayChars.toString(), getWidth() - width / 6, rowHeight, paintString);
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
