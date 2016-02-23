package com.s99.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class BoxDrawingView extends View {

    private static final String TAG = "BoxDrawingView";
    private static final String BOXEN = "boxen";
    private static final String SUPER_STATE = "super_state";

    private Box mCurrentBox;
    private ArrayList<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;
    private Paint mTestPaint; //paint for test information
    private int mPointerId = -1;

    //Used when creating the view in code
    public BoxDrawingView(Context context) {
        this(context, null);
    }

    //Used when creating the view from xml
    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //paint the box a red
        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        //paint the background off-withe
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);

        //test paint blue
        mTestPaint = new Paint();
        mTestPaint.setColor(0xff283593);
        mTestPaint.setStrokeWidth(10);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";
        int pointerIdx = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIdx);

        if (mPointerId >= 0) {
            pointerIdx = event.findPointerIndex(mPointerId);
        }

        PointF pointer = new PointF(event.getX(pointerIdx), event.getY(pointerIdx));

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "ACTION_POINTER_DOWN id = " + pointerId;
                Log.i(TAG, action);
                if (mPointerId < 0 && mCurrentBox != null) {
                    mPointerId = pointerId;
                    mCurrentBox.setPointerStart(pointer);
                    mCurrentBox.setPointerEnd(pointer);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                action = "ACTION_POINTER_UP id = " + pointerId;
                Log.i(TAG, action);
                if (mPointerId == pointerId){
                    mPointerId = -1;
                }
                break;
        }

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                //reset drawing state
                mCurrentBox = new Box(current);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mCurrentBox != null){
                    mCurrentBox.setCurrent(current);
                    invalidate();

                    if (mPointerId >= 0){
                        mCurrentBox.setPointerEnd(pointer);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                mPointerId = -1;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                mPointerId = -1;
                break;
        }

        Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //fill the background

        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxen){
            float left = Math.min(box.getCurrent().x, box.getOrigin().x);
            float right = Math.max(box.getCurrent().x, box.getOrigin().x);
            float top = Math.min(box.getCurrent().y, box.getOrigin().y);
            float bottom = Math.max(box.getCurrent().y, box.getOrigin().y);

            PointF pivot = new PointF(
                    (float)((left + right) * 0.5), (float)((top + bottom) * 0.5));

            canvas.drawPoint(pivot.x, pivot.y, mTestPaint);

            if ( box.getPointerEnd() != null && box.getPointerStart() != null) {

                PointF pS = box.getPointerStart();
                PointF pE = box.getPointerEnd();

                canvas.drawPoint(pS.x, pS.y, mTestPaint);
                canvas.drawPoint(pE.x, pE.y, mTestPaint);

                double pivotPs = Math.abs(Math.sqrt(
                        (pivot.x - pS.x) * (pivot.x - pS.x) + (pivot.y - pS.y) * (pivot.y - pS.y)));
                double pivotPe = Math.abs(Math.sqrt(
                        (pivot.x - pE.x) * (pivot.x - pE.x) + (pivot.y - pE.y) * (pivot.y - pE.y)));
                double PePs = Math.abs(Math.sqrt(
                        (pE.x - pS.x) * (pE.x - pS.x) + (pE.y - pS.y) * (pE.y - pS.y)));
                double alfa =
                        (pivotPs * pivotPs + pivotPe * pivotPe - PePs * PePs)
                        / (2 * pivotPs * pivotPe);

                alfa = Math.toDegrees(Math.acos(alfa));

                canvas.drawText(String.valueOf(alfa), right, bottom, mTestPaint);

                canvas.save();

                canvas.rotate((float) alfa, pivot.x, pivot.y);

                canvas.drawRect(left, top, right, bottom, mBoxPaint);

                canvas.restore();

            }else {
                canvas.drawRect(left, top, right, bottom, mBoxPaint);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle savedState = new Bundle();
        savedState.putParcelableArrayList(BOXEN, mBoxen);
        savedState.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;
        mBoxen = savedState.getParcelableArrayList(BOXEN);
        super.onRestoreInstanceState(savedState.getParcelable(SUPER_STATE));
    }

}
