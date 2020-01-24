package com.example.gps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class MyCompassView extends View {

    public int direction = 0;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean firstDraw;

    private boolean typeDirectionNorth = true;

    public MyCompassView(Context context) {
        super(context);
        init();
    }

    public MyCompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyCompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(25);

        p1.setColor(0xFF3F8BBA);
        p1.setStyle(Paint.Style.FILL_AND_STROKE);
        p1.setStrokeWidth(3);
        firstDraw = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cxCompass = getMeasuredWidth()/2;
        int cyCompass = getMeasuredHeight()/2;
        float radiusCompass;

        float scaleRadius = 0.8f;
        if(cxCompass > cyCompass){
            radiusCompass = cyCompass * scaleRadius;
        }
        else{
            radiusCompass = cxCompass * scaleRadius;
        }

        if(!firstDraw){
            final RectF oval = new RectF();

            float x1 = radiusCompass+5;
            float y1 = radiusCompass+10;

            oval.set(x1 - radiusCompass, y1 - radiusCompass, x1 + radiusCompass, y1+ radiusCompass);
            int sweep = 45;
            float startPosition = 270 - direction - (sweep/2);

            canvas.drawArc(oval,startPosition, sweep,true,p1);

            canvas.drawText(String.valueOf(direction), cxCompass+5, cyCompass, paint);
        }

    }

    public void updateDirection(float dir)
    {
        firstDraw = false;
        direction = (int)dir;
        invalidate();
    }

}