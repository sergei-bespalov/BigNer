package com.s99.criminalintent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;


public class ZoomPhotoFragment extends DialogFragment {

    private static String ARG_PHOTO_FILEPATH = "com.s99.criminalintent.extra_photo_filepath";

    private ImageView mPhotoView;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String photoPath = getArguments().getString(ARG_PHOTO_FILEPATH);
        Bitmap photo = PictureUtils.getScaledBitmap(photoPath, getActivity());

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo_zoom, null);
        mPhotoView = (ImageView) v.findViewById(R.id.crime_camera_photo_zoom);

        mPhotoView.setImageBitmap(photo);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

    }

    public static ZoomPhotoFragment newInstance(String photoFilepath){
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_FILEPATH, photoFilepath);

        ZoomPhotoFragment fragment= new ZoomPhotoFragment();
        fragment.setArguments(args);

        return fragment;
    }

}
