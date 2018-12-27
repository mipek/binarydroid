package de.thwildau.mpekar.binarydroid.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Implements a view that is scrollable.
 * Please note that currently only vertical scrolling (y-axis) is implemented:
 */
public class ScrollableView extends View {
    private static final int MSG_HOLD_TO_SCROLL = 1;
    private HoldToScrollHandler handler;
    private long lastPositionUpdate;
    private float positionY;
    private float startY;
    private float currentY;
    private boolean isHolding;
    private boolean scrollableX;
    private boolean scrollableY;

    public ScrollableView(Context context, AttributeSet attrs,
                          boolean scrollableX, boolean scrollableY) {
        super(context, attrs);

        this.handler = new HoldToScrollHandler(this);
        this.isHolding = false;
        this.scrollableX = scrollableX;
        this.scrollableY = scrollableY;

        if (scrollableX) {
            throw new RuntimeException("horizontal scrolling not implemented");
        }
    }

    /**
     * Returns the current position / scroll offset.
     * @return  current scroll offset
     */
    public float getPositionY() {
        return positionY;
    }

    protected void setPositionY(float positionY) {
        this.positionY = positionY;
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
        final float minimumScrollHeight = getHeight() / 6;
        if (Math.abs(deltaY) >= minimumScrollHeight) {
            float newPosY = positionY + deltaY;
            if (newPosY < 0) {
                newPosY = 0;
            }
            setPositionY(newPosY);
            if (updateTimestamp) {
                lastPositionUpdate = System.currentTimeMillis();
            }
            invalidate();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            isHolding = false;
        }
    }

    // This handler enables us to "auto-scroll" when touching the same spot for some time..
    // Please note that auto-scroll currently only works for vertical scrolling.
    private static class HoldToScrollHandler extends Handler {
        private static final long HOLD_TO_SCROLL_WAIT_TIME = 880;
        private static final long HOLD_TO_SCROLL_UPDATE_TIME = 88;
        private final WeakReference<ScrollableView> refView;

        HoldToScrollHandler(ScrollableView view) {
            refView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HOLD_TO_SCROLL:
                    holdToScroll();
                    break;
                default:
                    throw new RuntimeException("handleMessage: unknown message " + msg);
            }
        }

        private void holdToScroll() {
            ScrollableView view = refView.get();
            if (view != null && view.isHolding) {
                if (view.isEnabled()) {
                    if (System.currentTimeMillis() - view.lastPositionUpdate >= HOLD_TO_SCROLL_WAIT_TIME) {
                        view.doScroll(false);
                    }
                    // re-schedule the holdToScroll-check
                    sendEmptyMessageDelayed(MSG_HOLD_TO_SCROLL, HOLD_TO_SCROLL_UPDATE_TIME);
                } else {
                    // forcefully release when view is disabled
                    view.isHolding = false;
                }
            }
        }
    }
}
