package com.s99.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallToSuspect;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private CheckBox mSolvedCheckBox;
    private File mPhotoFile;
    private Callbacks mCallbacks;


    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_ZOOM = "DialogZoom";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;
    private static final int REQUEST_ZOOM = 4;

    public static final String EXTRA_CHANGED_CRIME_ID
            = "com.s99.criminalintent.changed_crime_id";

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
        void onCrimeRemoved(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                setResult();
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBigTablet()) {
                    startDatePickerAsDialog();
                } else {
                    startDatePickerAsActivity();
                }
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(fragmentManager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                setResult();
                updateCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);*/

                //challenge
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(R.string.send_report)
                        .startChooser();
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mCallToSuspect = (Button) v.findViewById(R.id.call_to_suspect);
        checkCanCallToSuspect();

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        if (canTakePhoto){
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_camera_photo);

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile == null || !mPhotoFile.exists()) return;

                FragmentManager fm = getActivity().getSupportFragmentManager();
                ZoomPhotoFragment zoomDialog = ZoomPhotoFragment
                        .newInstance(mPhotoFile.getAbsolutePath());
                zoomDialog.setTargetFragment(CrimeFragment.this, REQUEST_ZOOM);
                zoomDialog.show(fm, DIALOG_ZOOM);
            }
        });

        ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhotoView();
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_remove_crime:
                removeThisCrime();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkCanCallToSuspect(){

        if (mCrime.getSuspectsPhone() == null){
            mCallToSuspect.setEnabled(false);
            return;
        }

        PackageManager packageManager = getActivity().getPackageManager();

        final Intent callToSuspect = new Intent(Intent.ACTION_DIAL);
        Uri number = Uri.parse("tel:" + mCrime.getSuspectsPhone());
        callToSuspect.setData(number);

        mCallToSuspect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(callToSuspect);
            }
        });

        if (packageManager.resolveActivity(callToSuspect,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mCallToSuspect.setEnabled(false);
        }else {
            mCallToSuspect.setEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        if (requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            removeFragment(DIALOG_DATE);
            updateCrime();
        }

        if (requestCode == REQUEST_TIME){
            int hour = data.getIntExtra(TimePickerFragment.EXTRA_HOUR, 0);
            int minute = data.getIntExtra(TimePickerFragment.EXTRA_MINUTE, 0);

            Calendar calendar = new GregorianCalendar();
            calendar.setTime(mCrime.getDate());

            int year = calendar.get(GregorianCalendar.YEAR);
            int month = calendar.get(GregorianCalendar.MONTH);
            int day = calendar.get(GregorianCalendar.DAY_OF_MONTH);

            Date date = new GregorianCalendar(year, month, day, hour, minute).getTime();

            mCrime.setDate(date);
            updateTime();
            updateCrime();
        } else if (requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try{
                if (c.getCount() == 0){
                    return;
                }

                c.moveToFirst();
                String suspect = c.getString(0);
                String id = c.getString(1);

                Uri phonesUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                queryFields = new String[]{
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                String[] selectionArgs = new String[]{ id };
                c = getActivity().getContentResolver()
                        .query(phonesUri, queryFields, selection, selectionArgs, null);

                if (c.getCount() != 0){
                    c.moveToFirst();
                    String phone = c.getString(0);
                    if (phone != null){
                        mCrime.setSuspectsPhone(phone);
                        checkCanCallToSuspect();
                    }
                }

                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
                updateCrime();
            }finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO){
            updatePhotoView();
            updateCrime();
        }
    }

   private void updateCrime() {
       CrimeLab.get(getActivity()).updateCrime(mCrime);
       mCallbacks.onCrimeUpdated(mCrime);
   }

    private void updateTime() {
        CharSequence time = DateFormat.format("hh:mm a", mCrime.getDate());
        mTimeButton.setText(time);
    }

    private void updateDate() {
        CharSequence date = DateFormat.format("EEEE, MMM d, yyyy", mCrime.getDate());
        mDateButton.setText(date);
    }

    private String getCrimeReport(){
        String solvedString = null;
        if (mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView(){
        if (mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
        }else {
            Bitmap bitmap = null;
            try {
                int x = mPhotoView.getWidth();
                int y = mPhotoView.getHeight();
                bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath() ,x, y);

            }catch (NullPointerException e){
                bitmap = PictureUtils.getScaledBitmap(
                        mPhotoFile.getPath(), getActivity());
            }

            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private void removeFragment(String fragmentTag){
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }

    }

    private void removeThisCrime(){
        CrimeLab.get(getActivity()).removeCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
        mCallbacks.onCrimeRemoved(mCrime);
    }

    private void setResult(){
        Intent data = new Intent();
        data.putExtra(EXTRA_CHANGED_CRIME_ID, mCrime.getId());
        getActivity().setResult(Activity.RESULT_OK, data);
    }

    private void startDatePickerAsDialog(){
        FragmentManager fragmentManager = getFragmentManager();
        DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
        dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
        dialog.show(fragmentManager, DIALOG_DATE);
    }

    private void startDatePickerAsActivity(){
        Intent intent = new Intent(getActivity(), DatePickerActivity.class);
        intent.putExtra(DatePickerActivity.EXTRA_DATE, mCrime.getDate());
        startActivityForResult(intent, REQUEST_DATE);
    }

    private boolean isBigTablet(){
        return getResources().getInteger(R.integer.is_tablet) > 0;
    }
}
