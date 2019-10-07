package pcap.project.pcap;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;



public class PictureDraw extends View implements TimeAnimator.TimeListener
{
    private Bitmap picture;
    private Bitmap temp;
    private transient Canvas canvasSave;
    private TextOnImage caption;
    private int height;
    private int width;
    private boolean finished;

    private TimeAnimator mTimer;

    public PictureDraw(Context context) {
        super(context);
        commonInit();
    }

    public PictureDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        commonInit();
    }

    public PictureDraw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        commonInit();
    }

    public void commonInit()
    {
        finished = false;
        canvasSave = new Canvas();
        caption = new TextOnImage(0, 0);
        caption.setCaption("Welcome to the Captioner!");

        mTimer = new TimeAnimator();
        mTimer.setTimeListener(this);
        mTimer.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*height = h;
        width = w;*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        height = View.MeasureSpec.getSize(heightMeasureSpec);
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void setCaption(String aCaption)
    {
        this.caption.setCaption(aCaption);
    }

    public void setPicture(Bitmap picture)
    {
        this.picture = picture;
        canvasSave.setBitmap(temp);
    }

    public void setCaptionPosition(int width, int height)
    {
        caption.setCoords(width / 2, height / 2);
    }

    public void setSettings(String path, String color, String borderColor, String textSize, boolean borderOnOff, String borderSize)
    {
        if (path != null)
        {
            AssetManager assetManager = getResources().getAssets();
            Typeface tf = Typeface.createFromAsset(assetManager, path);

            caption.setFont(tf);
        }
        caption.setColor(color);
        caption.setBorderColor(borderColor);
        caption.setTextSize(textSize);
        caption.setBorder(borderOnOff);
        caption.setBorderSize(borderSize);

    }

    public void moveLeft(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            caption.setDirectionX(1);
        else if (event.getAction() == MotionEvent.ACTION_UP)
            caption.setDirectionX(0);
    }
    public void moveRight(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            caption.setDirectionX(2);
        else if (event.getAction() == MotionEvent.ACTION_UP)
            caption.setDirectionX(0);
    }
    public void moveUp(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            caption.setDirectionY(2);
        else if (event.getAction() == MotionEvent.ACTION_UP)
            caption.setDirectionY(0);
    }
    public void moveDown(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            caption.setDirectionY(1);
        else if (event.getAction() == MotionEvent.ACTION_UP)
            caption.setDirectionY(0);
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        temp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        //TRY SCALEDBITMAP FOR PICTURE
        canvasSave.setBitmap(this.temp);

        Path path = new Path();
        path.moveTo(0,0);
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth(), getHeight());
        path.lineTo(0, getHeight());
        path.close();

        int cx = (width - picture.getWidth()) >> 1;  // (Stuff) / 2;
        int cy = (height - picture.getHeight()) >> 1; // (Stuff) / 2;

        //Rect rect = new Rect(cx, cy, getWidth() - cx, getHeight() - cy);
        //canvas.drawBitmap(this.picture, null, rect, null);
        canvas.drawBitmap(this.picture, cx, cy, null);

        canvas.drawPath(path, paint);
        if (finished) {
            canvasSave.drawBitmap(this.picture, cx, cy, null);
            caption.drawMe(canvasSave);
        }

        caption.drawMe(canvas);

    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Bitmap getPicture() {
        return this.temp;
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {

        if (caption.getDirectionX() == 1)
            caption.setCoords(caption.getCoords()[0] - 3, caption.getCoords()[1]);
        else if (caption.getDirectionX() == 2)
            caption.setCoords(caption.getCoords()[0] + 3, caption.getCoords()[1]);

        if (caption.getDirectionY() == 1)
            caption.setCoords(caption.getCoords()[0], caption.getCoords()[1] + 3);
        else if (caption.getDirectionY() == 2)
            caption.setCoords(caption.getCoords()[0], caption.getCoords()[1] - 3);


        invalidate();
    }
}
