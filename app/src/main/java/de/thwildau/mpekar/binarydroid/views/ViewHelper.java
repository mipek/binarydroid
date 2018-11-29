package de.thwildau.mpekar.binarydroid.views;

import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * A collection of helper functions related to {@link android.view.View}s.
 * Contains core from:
 * <ul>
 *     <li>https://stackoverflow.com/a/21895626</li>
 * </ul>
 */
class ViewHelper {
    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredWidth
     *            the desired width
     * @param text
     *            the text that should be that width
     */
    static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                    String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    /**
     * Handle measure spec and return size accordingly. Should be used in onMeasure
     * @param measureSpec   measure spec (from onMeasure)
     * @param desired       desired size
     * @param max           maximum size
     * @return new size value
     */
    static int handleSize(int measureSpec, int desired, int max) {
        int size;
        if (measureSpec == View.MeasureSpec.EXACTLY) {
            size = max;
        } else if (measureSpec == View.MeasureSpec.AT_MOST) {
            size = Math.min(desired, max);
        } else {
            size = desired;
        }
        return size;
    }
}
