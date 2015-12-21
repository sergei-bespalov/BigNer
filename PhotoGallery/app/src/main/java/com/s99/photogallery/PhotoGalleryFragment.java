package com.s99.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private static int mColCount;

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int mLastPageNumber;
    private boolean mFetching;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mLastPageNumber = 1;
        new FetchItemTask().execute(mLastPageNumber);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_photo_gallery_recycler_view);


        mPhotoRecyclerView
                .setLayoutManager(
                        new StaggeredGridLayoutManager(
                                1,
                                StaggeredGridLayoutManager.VERTICAL));


        v.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int viewWidth = pxToDp(mPhotoRecyclerView.getWidth());
                        mColCount = viewWidth/130;
                        ((StaggeredGridLayoutManager) mPhotoRecyclerView
                                .getLayoutManager()).setSpanCount(mColCount);

                    }
                });

        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager)
                        mPhotoRecyclerView.getLayoutManager();
                int[] lastVisiblePositions = lm.findLastVisibleItemPositions(null);
                for (int lastItem : lastVisiblePositions) {
                    if (!mFetching && lastItem == mItems.size() - 1) {
                        mLastPageNumber++;
                        new FetchItemTask().execute(mLastPageNumber);
                    }
                }
            }
        });

        setupAdapter();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.d(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    private class FetchItemTask extends AsyncTask<Integer, Void, List<GalleryItem>> {
        @Override
        protected void onPreExecute() {
            mFetching = true;
        }

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            String query = "robot";

            if (query == null){
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(query);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems.addAll(galleryItems);
            PhotoAdapter adapter = (PhotoAdapter) mPhotoRecyclerView.getAdapter();
            if (adapter == null) {
                setupAdapter();
            } else {
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            }
            mFetching = false;
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            photoHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());

            int preloadedPosition = position;
            while (preloadedPosition >= 0 && preloadedPosition > position - 10){
                galleryItem = mGalleryItems.get(preloadedPosition);
                mThumbnailDownloader.queueThumbnail(null, galleryItem.getUrl());
                preloadedPosition--;
            }

            preloadedPosition = position;
            while (preloadedPosition < getItemCount() & preloadedPosition < position + 10){
                galleryItem = mGalleryItems.get(preloadedPosition);
                mThumbnailDownloader.queueThumbnail(null, galleryItem.getUrl());
                preloadedPosition++;
            }
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
}
