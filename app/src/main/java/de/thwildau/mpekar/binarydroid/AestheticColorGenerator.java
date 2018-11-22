package de.thwildau.mpekar.binarydroid;


import android.graphics.Color;

import java.util.Random;

public class AestheticColorGenerator {
    private Random random = new Random();

    public int generateRandomColor() {
        return mixColor(255, 255, 255);
    }

    // https://stackoverflow.com/a/43235
    private int mixColor(int r, int g, int b) {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        red = (red + r) / 2;
        green = (green + g) / 2;
        blue = (blue + b) / 2;

        return Color.rgb(red, green, blue);
    }

}
