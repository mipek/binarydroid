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
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;

public class HexEditView extends View {
    private static final int MSG_HOLD_TO_SCROLL = 1;
    private static final String HEXCHARS    = "0123456789ABCDEF";

    // variables w/ public access
    private long address;
    private ByteAccessor accessor;

    // variables w/ private access
    private int rowHeight;
    private HoldToScrollHandler handler;
    private long lastPositionUpdate;
    private float positionY;
    private float startY;
    private float currentY;
    private boolean isHolding = false;

    // properties
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

        handler = new HoldToScrollHandler(this);

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
    public boolean onTouchEvent(MotionEvent event) {
        final float y = event.getY();// / getHeight();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // remember touch starting position
                startY = y;
                isHolding = true;
                handler.sendEmptyMessage(MSG_HOLD_TO_SCROLL);
                break;
            case MotionEvent.ACTION_MOVE: // do scrolling
                currentY = y;
                doScroll(true);
                break;
            case MotionEvent.ACTION_UP:
                isHolding = false;
                handler.removeMessages(MSG_HOLD_TO_SCROLL);
            default:
                break;
        }

        // consume event
        return true;
    }

    private void doScroll(boolean updateTimestamp) {
        float deltaY = startY - currentY;
        if (deltaY != 0) {
            positionY += deltaY;
            if (positionY < 0) {
                positionY = 0;
            }
            if (updateTimestamp) {
                lastPositionUpdate = System.currentTimeMillis();
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawRect(0,0,getWidth(),getHeight(), paintString);
        if (getAccessor() == null) return;

        // get start address
        long address = getAddress();

        // figure out where we are at (in the file)
        long offset = (int)(positionY / rowHeight) * 8;
        Log.d("BinaryDroid", "delta: " + (positionY / rowHeight) + ", offset: " + offset);
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
        } while(drawY < maxY && address < getAccessor().getTotalBytes()); //(drawY < canvas.getHeight());
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

    private static class HoldToScrollHandler extends Handler {
        private static final long HOLD_TO_SCROLL_WAIT_TIME = 880;
        private static final long HOLD_TO_SCROLL_UPDATE_TIME = 88;
        private final WeakReference<HexEditView> refView;

        HoldToScrollHandler(HexEditView view) {
            refView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Check if user is still "holding the scroll"
                case MSG_HOLD_TO_SCROLL:
                    holdToScroll();
                    break;
                default:
                    throw new RuntimeException("handleMessage: unknown message " + msg);
            }
        }

        private void holdToScroll() {
            HexEditView view = refView.get();
            if (view != null && view.isHolding) {
                if (System.currentTimeMillis() - view.lastPositionUpdate >= HOLD_TO_SCROLL_WAIT_TIME) {
                    view.doScroll(false);
                }
                // re-schedule the holdToScroll-check
                sendEmptyMessageDelayed(MSG_HOLD_TO_SCROLL, HOLD_TO_SCROLL_UPDATE_TIME);
            }
        }
    };
}
