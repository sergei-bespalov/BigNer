package com.s99.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class PhotoPageActivity extends SingleFragmentActivity {
    /**
     * implement it if you want to handle onBackPressed in fragment
     */
    public interface BackPressHandler {
        /**
         * @return true - handled, false - not handled
         */
        boolean onBackPressed();
    }

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.fragment_container);

        if (f instanceof BackPressHandler) {
            if (((BackPressHandler) f).onBackPressed()){
                return;
            }
        }

        super.onBackPressed();
    }
}
