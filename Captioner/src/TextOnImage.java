package pcap.project.pcap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;

public class TextOnImage
{
    private Paint paintFill;
    private Paint paintBorder;
    private String caption;
    private float[] coords;
    private int directionX;
    private int directionY;
    private boolean borderOnOff;

    public TextOnImage(int x, int y)
    {
        paintFill = new Paint();
        paintBorder = new Paint();
        paintFill.setTextSize(100);
        paintBorder.setTextSize(100);
        paintFill.setColor(Color.WHITE);
        paintBorder.setColor(Color.DKGRAY);
        borderOnOff = true;

        caption = "";

        coords = new float[2];
        coords[0] = x;
        coords[1] = y;

        directionX = 0;
        directionY = 0;

    }

    public void setColor(String color)
    {
        paintFill.setColor(Color.parseColor(color));
    }
    public void setBorderColor(String color)
    {
        paintBorder.setColor(Color.parseColor(color));
    }

    public void setDirectionX(int directionX) {
        this.directionX = directionX;
    }

    public void setDirectionY(int directionY) {
        this.directionY = directionY;
    }

    public int getDirectionX() {
        return directionX;
    }

    public int getDirectionY() {
        return directionY;
    }

    public void setBorder(boolean border)
    {
        borderOnOff = border;
    }

    public void setCaption(String caption)
    {
        this.caption = caption;
    }

    public void setFont(Typeface typeface)
    {
        paintFill.setTypeface(typeface);
        paintBorder.setTypeface(typeface);
    }
    public void setTextSize(String size)
    {
        paintFill.setTextSize(Float.parseFloat(size));
        paintBorder.setTextSize(Float.parseFloat(size));
    }
    public void setBorderSize(String borderSize)
    {
        paintBorder.setStrokeWidth(Integer.parseInt(borderSize));
    }

    public void setCoords(float x, float y)
    {
        coords[0] = x;
        coords[1] = y;
    }

    public float[] getCoords() {
        return coords;
    }

    public void drawMe(Canvas canvas)
    {
        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setStrokeWidth(10);
        paintBorder.setAntiAlias(true);
        //paintFill.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(this.caption, coords[0], coords[1], paintFill);

        if (borderOnOff) {
            paintBorder.setStyle(Paint.Style.STROKE);
            paintBorder.setAntiAlias(true);
            //paintBorder.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(this.caption, coords[0], coords[1], paintBorder);
        }
    }
}
