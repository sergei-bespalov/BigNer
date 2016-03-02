package com.s99.sunset;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

public class SunsetFragment extends Fragment {

    private View mSceneView;
    private View mSunsetView;
    private View mSkyView;

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;
    private int mCurrentColor = 0;
    private int mCurrentNightColor = 0;

    private float mSunCurrentAnimatedPosition = -1;

    private AnimatorSet mCurrentAnimatorSet;

    private boolean mReverse;

    public static SunsetFragment newInstance() {
        return new SunsetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sunset, container, false);

        mSceneView = view;
        mSkyView = view.findViewById(R.id.sky);
        mSunsetView = view.findViewById(R.id.sun);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);

        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mReverse) startAnimationReverse();
                else startAnimation();
            }
        });

        return view;
    }

    private void startAnimation() {
        float sunYStart = mSunsetView.getTop();
        final float sunYEnd = mSkyView.getHeight();

        if (mSunCurrentAnimatedPosition < 0) mSunCurrentAnimatedPosition = sunYStart;
        if (mCurrentColor == 0) mCurrentColor = mBlueSkyColor;
        if (mCurrentNightColor == 0) mCurrentNightColor = mSunsetSkyColor;

        //heatingTheSun
        float baseScale = 1;
        float heatScale = (float) 1.01;
        ObjectAnimator heatWAnimator = ObjectAnimator
                .ofFloat(mSunsetView, "scaleX", heatScale, baseScale)
                .setDuration(750);
        heatWAnimator.setRepeatCount(4);


        ObjectAnimator heatHAnimator = ObjectAnimator
                .ofFloat(mSunsetView, "scaleY", heatScale, baseScale)
                .setDuration(750);
        heatHAnimator.setRepeatCount(4);

        //sun set animation
        final ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunsetView, "y", mSunCurrentAnimatedPosition, sunYEnd)
                .setDuration(3000);
        heightAnimator.setInterpolator(new AccelerateInterpolator());

        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSunCurrentAnimatedPosition = (float) animation.getAnimatedValue();
            }
        });

        //sky color
        ObjectAnimator sunsetSkyAnimation = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mCurrentColor, mSunsetSkyColor)
                .setDuration(3000);
        sunsetSkyAnimation.setEvaluator(new ArgbEvaluator());

        sunsetSkyAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentColor = (int) animation.getAnimatedValue();
            }
        });

        //night sky
        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mCurrentNightColor, mNightSkyColor)
                .setDuration(1500);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());

        nightSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentNightColor = (int) animation.getAnimatedValue();
            }
        });

        //set
        if (mCurrentAnimatorSet != null) {
            mCurrentAnimatorSet.cancel();
        }

        mCurrentAnimatorSet = new AnimatorSet();
        mCurrentAnimatorSet
                .play(heightAnimator)
                .with(sunsetSkyAnimation)
                .with(heatHAnimator)
                .with(heatWAnimator)
                .before(nightSkyAnimator);
        mCurrentAnimatorSet.start();

        mReverse = true;
    }

    private void startAnimationReverse() {
        float sunYStart = mSunsetView.getTop();
        float sunYEnd = mSkyView.getHeight();

        if (mSunCurrentAnimatedPosition < 0) mSunCurrentAnimatedPosition = sunYEnd;
        if (mCurrentColor == 0) mCurrentColor = mSunsetSkyColor;
        if (mCurrentNightColor == 0) mCurrentNightColor = mNightSkyColor;

        //heatingTheSun
        float baseScale = 1;
        float heatScale = (float) 1.01;
        ObjectAnimator heatWAnimator = ObjectAnimator
                .ofFloat(mSunsetView, "scaleX", heatScale, baseScale)
                .setDuration(750);
        heatWAnimator.setRepeatCount(4);


        ObjectAnimator heatHAnimator = ObjectAnimator
                .ofFloat(mSunsetView, "scaleY", heatScale, baseScale)
                .setDuration(750);
        heatHAnimator.setRepeatCount(4);

        //sun set
        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunsetView, "y", mSunCurrentAnimatedPosition, sunYStart)
                .setDuration(3000);
        heightAnimator.setInterpolator(new AccelerateInterpolator());

        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSunCurrentAnimatedPosition = (float) animation.getAnimatedValue();
            }
        });

        //sky color
        ObjectAnimator sunsetSkyAnimation = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mCurrentColor, mBlueSkyColor)
                .setDuration(3000);
        sunsetSkyAnimation.setEvaluator(new ArgbEvaluator());

        sunsetSkyAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentColor = (int) animation.getAnimatedValue();
            }
        });

        //night sky after sunset
        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mCurrentNightColor, mSunsetSkyColor)
                .setDuration(1500);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());

        nightSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentNightColor = (int) animation.getAnimatedValue();
            }
        });


        //animator set
        boolean needAnimateNight = mSunCurrentAnimatedPosition == sunYEnd;

        if (mCurrentAnimatorSet != null) {
            mCurrentAnimatorSet.cancel();
        }

        mCurrentAnimatorSet = new AnimatorSet();

        if (needAnimateNight) {
            mCurrentAnimatorSet
                    .play(heightAnimator)
                    .with(sunsetSkyAnimation)
                    .with(heatHAnimator)
                    .with(heatWAnimator)
                    .after(nightSkyAnimator);
        }else {
            mCurrentAnimatorSet
                    .play(heightAnimator)
                    .with(sunsetSkyAnimation);
        }

        mCurrentAnimatorSet.start();


        mReverse = false;
    }
}
