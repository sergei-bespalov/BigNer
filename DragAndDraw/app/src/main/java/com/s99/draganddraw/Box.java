package com.s99.draganddraw;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

public class Box implements Parcelable {
    private PointF mOrigin;
    private PointF mCurrent;

    public Box(PointF origin){
        mCurrent = origin;
        mOrigin = origin;
    }

    public Box(Parcel source){
        mOrigin = source.readParcelable(null);
        mCurrent = source.readParcelable(null);
    }

    public PointF getOrigin() {
        return mOrigin;
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public void setCurrent(PointF current) {
        mCurrent = current;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mOrigin, 0);
        dest.writeParcelable(mCurrent, 0);
    }

    public static final Parcelable.Creator<Box> CREATOR = new Parcelable.Creator<Box>(){

        @Override
        public Box createFromParcel(Parcel source) {
            return new Box(source);
        }

        @Override
        public Box[] newArray(int size) {
            return new Box[0];
        }
    };
}
