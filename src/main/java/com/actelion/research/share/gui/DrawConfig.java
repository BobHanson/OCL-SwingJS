package com.actelion.research.share.gui;

/**
 * User: rufenec
 * Creation Date: 8/24/2016
 */
public abstract class DrawConfig
{
    public final int createColor(double r, double g, double b, double alpha)
    {
        int col =  ((int)(r * 255) << 24) | ((int)(g * 255) << 16) | ((int)(b * 255) << 8) |(int) (alpha*255);
        return col;
    }

    public abstract int getHighLightColor();
    public abstract int getMapToolColor();
    public abstract int getSelectionColor();
    public abstract int getForegroundColor();
    public abstract int getBackgroundColor();
}
