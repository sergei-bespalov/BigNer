package com.s99.sunset;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
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

    private boolean mReverse;

    public static SunsetFragment newInstance(){
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

    private void startAnimation(){
        float sunYStart = mSunsetView.getTop();
        float sunYEnd = mSkyView.getHeight();
        int sunWidth = mSunsetView.getWidth();
        int sunHeight = mSunsetView.getHeight();

        ObjectAnimator sunHeightAnimator = ObjectAnimator
                .ofInt(mSunsetView, "scaleY", sunHeight, sunHeight + 1000)
                .setDuration(600);
        sunHeightAnimator.setRepeatCount(5);

        ObjectAnimator sunWidthAnimator = ObjectAnimator
                .ofInt(mSunsetView, "scaleX", sunWidth, sunWidth + 1000)
                .setDuration(600);
        sunWidthAnimator.setRepeatCount(5);

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunsetView, "y", sunYStart, sunYEnd)
                .setDuration(3000);
        heightAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator sunsetSkyAnimation = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mBlueSkyColor, mSunsetSkyColor)
                .setDuration(3000);
        sunsetSkyAnimation.setEvaluator(new ArgbEvaluator());

        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mNightSkyColor)
                .setDuration(1500);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(heightAnimator)
                .with(sunsetSkyAnimation)
                .with(sunWidthAnimator)
                .with(sunHeightAnimator)
                .before(nightSkyAnimator);
        animatorSet.start();

        mReverse = true;
    }

    private void startAnimationReverse(){
        float sunYStart = mSunsetView.getTop();
        float sunYEnd = mSkyView.getHeight();

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunsetView, "y", sunYEnd, sunYStart)
                .setDuration(3000);
        heightAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator sunsetSkyAnimation = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mBlueSkyColor)
                .setDuration(3000);
        sunsetSkyAnimation.setEvaluator(new ArgbEvaluator());

        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mNightSkyColor,  mSunsetSkyColor)
                .setDuration(1500);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(heightAnimator)
                .with(sunsetSkyAnimation)
                .after(nightSkyAnimator);
        animatorSet.start();

        mReverse = false;
    }
}
