package com.example.gps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MyCompassView extends View {

    public int direction = 0;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean firstDraw;

    private int type = 1;

    public MyCompassView(Context context) {
        super(context);
// TODO Auto-generated constructor stub
        init();
    }

    public MyCompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
// TODO Auto-generated constructor stub
        init();
    }

    public MyCompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
// TODO Auto-generated constructor stub
        init();
    }

    private void init(){

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(25);

        p1.setColor(Color.GREEN);
        p1.setStyle(Paint.Style.FILL_AND_STROKE);
        p1.setStrokeWidth(3);
        firstDraw = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
// TODO Auto-generated method stub
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
// TODO Auto-generated method stub
        Log.e("width",""+getMeasuredWidth());
        Log.e("hright",""+getMeasuredHeight());
        int cxCompass = getMeasuredWidth()/2;
        int cyCompass = getMeasuredHeight()/2;
        float radiusCompass;

        if(cxCompass > cyCompass){
            radiusCompass = (float) (cyCompass * 0.9);
        }
        else{
            radiusCompass = (float) (cxCompass * 0.9);
        }
        //canvas.drawCircle(cxCompass, cyCompass, radiusCompass, paint);

        if(!firstDraw){
            /*
            canvas.drawLine(cxCompass, cyCompass,
                    (float)(cxCompass + radiusCompass * Math.sin((double)(-direction) * 3.14/180)),
                    (float)(cyCompass - radiusCompass * Math.cos((double)(-direction) * 3.14/180)),
                    paint);
            */

            final RectF oval = new RectF();

            float x1 = radiusCompass+5;
            float y1 = radiusCompass+10;

            oval.set(x1 - radiusCompass, y1 - radiusCompass, x1 + radiusCompass, y1+ radiusCompass);
            //oval.set(cxCompass, cyCompass, (float)(cxCompass + radiusCompass * Math.sin((double)(-direction) * 3.14/180)), (float)(cyCompass - radiusCompass * Math.cos((double)(-direction) * 3.14/180)));
            int sweep = 45;
            float startPosition = 270 - direction - (sweep/2);

            //int startAngle = (int) (180 / Math.PI * Math.atan2(start, start));

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