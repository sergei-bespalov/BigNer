package com.s99.photogallery;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PollServiceLollipop extends JobService {
    public static final String TAG = "PollServiceLollipop";

    private PollTask mCurrentTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "has new job");
        mCurrentTask = new PollTask();
        mCurrentTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return true;
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void> {

        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParameters = params[0];

            if (!isNetworkAvailableAndConnected()) {
                jobFinished(jobParameters, false);
                return null;
            }

            String query = QueryPreferences.getStoredQuery(PollServiceLollipop.this);
            String lastResultId = QueryPreferences.getLastResultId(PollServiceLollipop.this);
            List<GalleryItem> items;

            if (query == null) {
                items = new FlickrFetchr().fetchRecentPhotos();
            } else {
                items = new FlickrFetchr().searchPhotos(query);
            }

            if (items.size() == 0) {
                jobFinished(jobParameters, false);
                return null;
            }

            String resultId = items.get(0).getId();
            if (resultId.equals(lastResultId)) {
                Log.i(TAG, "Got an old result: " + resultId);
            } else {
                Log.i(TAG, "Got a new result: " + resultId);

                Resources resources = getResources();
                Intent i = PhotoGalleryActivity.newIntent(PollServiceLollipop.this);
                PendingIntent pi = PendingIntent.getActivity(PollServiceLollipop.this, 0, i, 0);

                Notification notification =
                        new NotificationCompat.Builder(PollServiceLollipop.this)
                                .setTicker(resources.getString(R.string.new_pictures_title))
                                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                                .setContentTitle(resources.getString(R.string.new_pictures_title))
                                .setContentText(resources.getString(R.string.new_pictures_text))
                                .setContentIntent(pi)
                                .setAutoCancel(true)
                                .build();

                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(PollServiceLollipop.this);
                notificationManager.notify(0, notification);

                sendBroadcast(
                        new Intent(PollService.ACTION_SHOW_NOTIFICATION),
                        PollService.PERM_PRIVATE);
            }

            QueryPreferences.setLastResultId(PollServiceLollipop.this, resultId);

            jobFinished(jobParameters, false);
            return null;
        }
    }
}
