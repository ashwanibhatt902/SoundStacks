package com.startup.soundstack.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;
import com.startup.soundstack.R;
import com.startup.soundstack.SoundStackApplication;
import com.startup.soundstack.activities.SoundActivity;
import com.startup.soundstack.customclass.CustomText;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PersonalFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PersonalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View mBaseView;
    private ImageView profilePic;
    private ImageView coverPic;
    private View profilePB = null;
    private View coverPB = null;
    private TextView mUserName = null;
    private int requsetCode;
    String mCurrentPhotoPath;

    private final int REQ_COVER_GALLERY = 1001;
    private final int REQ_PROFILE_GALLERY = 1002;
    private final int REQ_COVER_CAMERA = 1003;
    private final int REQ_PROFILE_CAMERA = 1004;


    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalFragment newInstance(String param1, String param2) {
        PersonalFragment fragment = new PersonalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PersonalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.fragment_personal, container, false);

        NavigationView navigationView = (NavigationView) (mBaseView.findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(mMenuListener);


        View headerView = navigationView.getHeaderView(0);
        profilePic = (ImageView) headerView.findViewById(R.id.avatar);
        coverPic = (ImageView) headerView.findViewById(R.id.coverPic);
        mUserName = (TextView) headerView.findViewById(R.id.userName);

        coverPic.setOnClickListener(mImageChangeListener);
        profilePic.setOnClickListener(mImageChangeListener);
        mUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseUser currentUser= ParseUser.getCurrentUser();
                if (currentUser != null) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);

                    LayoutInflater inflater = (getActivity()).getLayoutInflater();
                    final View dialogLayout = inflater.inflate(R.layout.dialog_user_info, null);


                    builder.setTitle("Account Info");
                    builder.setView(dialogLayout);

                    CircleImageView userAvatar = (CircleImageView) dialogLayout.findViewById(R.id.avatar);
                    setProfilePic(userAvatar, currentUser);

                    CustomText email = (CustomText)dialogLayout.findViewById(R.id.emailid);
                    if(currentUser.getString(Constants.UserProperty.EMAIL_ADD) !=  null){
                        email.setText(currentUser.getString(Constants.UserProperty.EMAIL_ADD));
                    }
                    CustomText username = (CustomText) dialogLayout.findViewById(R.id.user_name);
                    username.setText(currentUser.getString("name"));
                    final View logout = dialogLayout.findViewById(R.id.logoutParent);
                    final Dialog alertDialog =  builder.create();
                    logout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            logout.setOnClickListener(null);
                            dialogLayout.findViewById(R.id.progrss).setVisibility(View.VISIBLE);
                            ParseUser.logOutInBackground(new LogOutCallback() {
                                @Override
                                public void done(ParseException e) {

                                    UpdateCenter.postUpdateEvent(getActivity(), UpdateCenter.EventType.Session);
                                    alertDialog.dismiss();
                                }
                            });
                        }
                    });
                    alertDialog.show();
                }
                else {
                    Utility.openLoginActivity(getActivity());
                }
            }
        });

        profilePB = headerView.findViewById(R.id.profilePB);
        coverPB = headerView.findViewById(R.id.coverPB);

        this.updateUI(UpdateCenter.generateUpdateEvent(this, UpdateCenter.EventType.Session));
        return mBaseView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        UpdateCenter.getEventBus().register(this);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        UpdateCenter.getEventBus().unregister(this);
        mListener = null;
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == Activity.RESULT_OK) {

            Bitmap bitmap = null;
            String propertyName = null;

            if(requestCode == REQ_COVER_GALLERY){
                Uri selectedImage = imageReturnedIntent.getData();
                coverPB.setVisibility(View.VISIBLE);
                propertyName = Constants.UserProperty.COVER_PIC_FILE;
                bitmap = Utility.getBitmapFromUri(getActivity(), Uri.parse(selectedImage.toString()), 1024);
            }else if(requestCode == REQ_PROFILE_GALLERY){
                Uri selectedImage = imageReturnedIntent.getData();
                profilePB.setVisibility(View.VISIBLE);
                bitmap = Utility.getBitmapFromUri(getActivity(), Uri.parse(selectedImage.toString()), 512);
                propertyName = Constants.UserProperty.PROFILE_PIC_FILE;
            }else if(requestCode == REQ_PROFILE_CAMERA){
                Uri selectedImage = Uri.parse(mCurrentPhotoPath);
                profilePB.setVisibility(View.VISIBLE);
                bitmap = Utility.getBitmapFromUri(getActivity(), Uri.parse(selectedImage.toString()), 512);
                propertyName = Constants.UserProperty.PROFILE_PIC_FILE;
//                galleryAddPic();
            }else if(requestCode == REQ_COVER_CAMERA){
                Uri selectedImage = Uri.parse(mCurrentPhotoPath);
                coverPB.setVisibility(View.VISIBLE);
                bitmap = Utility.getBitmapFromUri(getActivity(), Uri.parse(selectedImage.toString()), 1024);
                propertyName = Constants.UserProperty.COVER_PIC_FILE;
//                galleryAddPic();
            }

            if(bitmap == null){
                return;
            }

            final String prop = propertyName;
            final Bitmap bmp = bitmap;


            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            final ParseUser  user = ParseUser.getCurrentUser();
            String filename = propertyName;
            ParseFile file = new ParseFile(filename, byteArray);
            user.put(propertyName, file);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    String msg = "nothing";
                    coverPic.setOnClickListener(mImageChangeListener);
                    profilePic.setOnClickListener(mImageChangeListener);

                    if (e != null) {
                        Toast.makeText(getActivity(), "Exception", Toast.LENGTH_LONG).show();
                        msg = "Fail to upload new Image";
                    } else {
                        msg = "successfully uploaded your new Image";
                        if (prop.equalsIgnoreCase(Constants.UserProperty.COVER_PIC_FILE)){
                            coverPic.setImageBitmap(bmp);
                        }else{
                            profilePic.setImageBitmap(bmp);
                        }
                    }

                    Snackbar.make(mBaseView, msg, Snackbar.LENGTH_LONG).show();

                    coverPic.setOnClickListener(mImageChangeListener);
                    profilePic.setOnClickListener(mImageChangeListener);

                    profilePB.setVisibility(View.GONE);
                    coverPB.setVisibility(View.GONE);
                }
            });
        }

        else {
            profilePic.setOnClickListener(mImageChangeListener);
            coverPic.setOnClickListener(mImageChangeListener);
        }
    }

    @Subscribe
    public void updateUI(UpdateCenter.UpdateEvent event) {

        if (!event.mType.equals(UpdateCenter.EventType.Session)) return;

        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            mUserName.setText("@"+currentUser.getString(Constants.UserProperty.NAME));
            ParseFile coverParseImage = currentUser.getParseFile(Constants.UserProperty.COVER_PIC_FILE);
            if (coverParseImage != null) {
                coverParseImage.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if (e == null && bytes != null) {
                            Bitmap imgBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            coverPic.setImageBitmap(imgBitmap);
                        }
                    }
                });
            }
                setProfilePic(profilePic, currentUser);
        } else {
            profilePic.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
            coverPic.setImageResource(R.drawable.cover);
            mUserName.setText("Guest");
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    NavigationView.OnNavigationItemSelectedListener mMenuListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            int menuID = menuItem.getItemId();
            Context context = getActivity();

            ParseUser currentUser = ParseUser.getCurrentUser();

            if (context == null) return true;
            if (currentUser == null) {
                Utility.openLoginActivity(context);
                return false;
            }

            switch (menuID) {
                case R.id.nav_my_sound: {

                    Tracker t = ((SoundStackApplication)getActivity().getApplication()).getTracker();
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("Screen Opened")
                            .setAction("My Sound")
                            .build());


                    Intent intent = new Intent(context, SoundActivity.class);
                    JSONObject jsonObject = Utility.createJsonQuery
                            (getString(R.string.activity_my_sounds), SoundItem.COLUMN_UPLOADED_BY_USER, new JSONArray(Arrays.asList(currentUser.getObjectId())),
                                    SoundItem.class.toString(),R.drawable.ic_my_library_music_white_24dp);
                    try {
                        jsonObject.put("showFAB", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("obj", jsonObject.toString());
                    context.startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.righttoleft,
                            R.anim.lefttoright);
                    break;
                }case R.id.nav_my_category: {
                    Tracker t = ((SoundStackApplication)getActivity().getApplication()).getTracker();
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("Screen Opened")
                            .setAction("My Category")
                            .build());
                    Intent intent = new Intent(context, SoundActivity.class);
                    JSONObject jsonObject = Utility.createJsonQuery
                            (getString(R.string.activity_my_category), SoundItem.COLUMN_UPLOADED_BY_USER, new JSONArray(Arrays.asList(currentUser.getObjectId())),
                                    Category.class.toString(),
                                    R.drawable.ic_my_library_books_white_24dp);
                    try {
                        jsonObject.put("showFAB", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("obj", jsonObject.toString());
                    context.startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.righttoleft,
                            R.anim.lefttoright);
                    break;
                }
                case R.id.nav_fav_sound: {

                    Tracker t = ((SoundStackApplication)getActivity().getApplication()).getTracker();
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("Screen Opened")
                            .setAction("Favourite Sound")
                            .build());

                    Intent intent = new Intent(context, SoundActivity.class);
                    JSONArray array = currentUser.getJSONArray(Constants.UserProperty.FAV_SOUND_ID);
                    JSONObject jsonObject = Utility.createJsonQuery
                            (getString(R.string.activity_fav_sounds), "objectId", array, SoundItem.class.toString(),
                                    R.drawable.ic_favorite_white_24dp);
                    intent.putExtra("obj", jsonObject.toString());
                    context.startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.righttoleft,
                            R.anim.lefttoright);
                    break;
                }case R.id.nav_fav_category: {
                    Tracker t = ((SoundStackApplication)getActivity().getApplication()).getTracker();
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("Screen Opened")
                            .setAction("Favourite Category")
                            .build());
                    Intent intent = new Intent(context, SoundActivity.class);
                    JSONArray array = currentUser.getJSONArray(Constants.UserProperty.FAV_CATEGORIES_ID);
                    JSONObject jsonObject = Utility.createJsonQuery
                            (getString(R.string.activity_fav_category), "objectId", array, Category.class.toString(), R.drawable.ic_favorite_white_24dp);
                    intent.putExtra("obj", jsonObject.toString());
                    context.startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.righttoleft,
                            R.anim.lefttoright);
                    break;
                }
                case R.id.nav_liked_sound: {
                    Tracker t = ((SoundStackApplication)getActivity().getApplication()).getTracker();
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("Screen Opened")
                            .setAction("Liked Sound")
                            .build());
                    Intent intent = new Intent(context, SoundActivity.class);
                    JSONArray array = currentUser.getJSONArray(Constants.UserProperty.LIKE_SOUND_ID);
                    JSONObject jsonObject = Utility.createJsonQuery
                            (getString(R.string.activity_Like_sounds), "objectId", array, SoundItem.class.toString(),R.drawable.ic_thumb_up_white_24dp);
                    intent.putExtra("obj", jsonObject.toString());
                    context.startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.righttoleft,
                            R.anim.lefttoright);
                    break;
                }
            }
            return false;
        }
    };

    private View.OnClickListener mImageChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (ParseUser.getCurrentUser() == null) {
                Utility.openLoginActivity(getActivity());
                return;
            }

            requsetCode = -1;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogLayout = inflater.inflate(R.layout.activity_select_pic, null);

            View cameraButton = dialogLayout.findViewById(R.id.camera);
            View galleryButton = dialogLayout.findViewById(R.id.gallery);

            builder.setView(dialogLayout);
            final AlertDialog customDialog =  builder.create();

            customDialog.show();

            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                    view.setOnClickListener(null);
                    if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
                        Toast.makeText(getActivity(), "OOPS!Sorry,you don't have a camera on this device.", Toast.LENGTH_LONG).show();
                    } else {
                        mCurrentPhotoPath = null;
                        if (view.getId() == R.id.avatar) {
                            requsetCode = REQ_PROFILE_CAMERA;
                        } else {
                            requsetCode = REQ_COVER_CAMERA;
                        }
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, requsetCode);
                        }
                    }
                }
            });
            galleryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                    view.setOnClickListener(null);
                    if (view.getId() == R.id.avatar) {
                        requsetCode = REQ_PROFILE_GALLERY;
                    } else {
                        requsetCode = REQ_COVER_GALLERY;
                    }
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, requsetCode);
                }
            });
        }
    };


    private void setProfilePic(final ImageView imageView, ParseUser currentUser ) {
        ParseFile profileParseFile = currentUser.getParseFile(Constants.UserProperty.PROFILE_PIC_FILE_LQ);
        if (profileParseFile == null) {
            profileParseFile = currentUser.getParseFile(Constants.UserProperty.PROFILE_PIC_FILE);
        }
        if (profileParseFile != null) {
            profileParseFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null && bytes != null) {
                        Bitmap imgBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(imgBitmap);
                    }else{
                        Toast.makeText(getActivity(),"Exception",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.getActivity().sendBroadcast(mediaScanIntent);
    }

}
