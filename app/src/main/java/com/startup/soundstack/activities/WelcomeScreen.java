package com.startup.soundstack.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class WelcomeScreen extends AppCompatActivity {

    ImageView userImage = null;
    ProfilePictureView profilePictureView = null;
    View progreesBar = null;
    View checkMark =  null;
    String mCurrentPhotoPath;


    private final int REQ_PROFILE_GALLERY = 1001;
    private final int REQ_PROFILE_CAMERA = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String createdBy = getIntent().getStringExtra("createdBy");
        createdBy = createdBy != null ? createdBy :"";

        setContentView(R.layout.welcome_screen);

        final String userName  = ParseUser.getCurrentUser().getString(Constants.UserProperty.NAME);
        String profilePic  = ParseUser.getCurrentUser().getString(Constants.UserProperty.PROFILE_PIC_URL);
        final EditText usernameET = (EditText)findViewById(R.id.user_name);

        progreesBar = findViewById(R.id.progressBar);
        checkMark = findViewById(R.id.checkmark);

        final Timer[] timer = {new Timer()};
         final long DELAY = 1000;
        usernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }
            @Override
            public void onTextChanged(final CharSequence s, int start, int before,
                                      int count) {
                if(timer[0] != null)
                    timer[0].cancel();
            }
            @Override
            public void afterTextChanged(final Editable s) {
                //avoid triggering event when text is too short
                if (s.length() >= 3) {

                    timer[0] = new Timer();

//                    timer[0].
                    timer[0].schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    progreesBar.setVisibility(View.VISIBLE);
                                    checkMark.setVisibility(View.GONE);
                                    ParseQuery userNameQuery = ParseQuery.getQuery(ParseUser.class);
                                    userNameQuery.whereEqualTo(Constants.UserProperty.NAME, s.toString());
                                    userNameQuery.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                                    userNameQuery.countInBackground(new CountCallback() {
                                        @Override
                                        public void done(int i, ParseException e) {
                                            if (e != null || i > 0) {
                                                usernameET.setError("Already Taken, Try again");
                                            } else if (i == 0) {
                                                findViewById(R.id.welcome_submit).setEnabled(true);
                                                checkMark.setVisibility(View.VISIBLE);
                                            }
                                            progreesBar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });

                        }

                    }, DELAY);
                }

                else if (s.length() < 3) {
                    usernameET.setError("Minimum 3 characters");
                    checkMark.setVisibility(View.GONE);
                }

                else {
                    usernameET.setError(null);
                }
            }
        });
//        usernameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
//                    ParseQuery userNameQuery = new ParseQuery(ParseUser.class);
//                    userNameQuery.whereContains(Constants.UserProperty.NAME, userName);
//                    userNameQuery.countInBackground(new CountCallback() {
//                        @Override
//                        public void done(int i, ParseException e) {
//                            if (e != null) {
//                                usernameET.setError("Already Taken, Try again");
//                            }
//                            else if (i == 0) {
//                                findViewById(R.id.welcome_submit).setEnabled(true);
//                                findViewById(R.id.checkmark).setVisibility(View.VISIBLE);
//                            }
//                            findViewById(R.id.progressBar).setVisibility(View.GONE);
//                        }
//                    });
//                }
//            }
//        });
        usernameET.setText(userName);


        profilePictureView = (ProfilePictureView)findViewById(R.id.user_imageF);
        userImage = (ImageView) findViewById(R.id.user_image);

        if (!createdBy.equals("facebook") && ( profilePic != null && profilePic.length() >0)) {
            userImage.setVisibility(View.VISIBLE);
            profilePictureView.setVisibility(View.INVISIBLE);

            ImageLoader imageLoader = ImageLoader.getInstance();

            //download and display image from url

            imageLoader.displayImage(profilePic,
                    userImage, HomeActivity.sDisplayImageOptions);
//            RequestCreator imageRequestCallback = Picasso.with(WelcomeScreen.this).load(profilePic);
//            imageRequestCallback.into(userImage);
        }

        else {
            userImage.setVisibility(View.INVISIBLE);
            profilePictureView.setVisibility(View.VISIBLE);
            profilePictureView.setProfileId(ParseUser.getCurrentUser().getString("id"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        ParseUser curentParseUser = ParseUser.getCurrentUser();

        int id = v.getId();
        if (id == R.id.welcome_submit && checkMark.getVisibility() == View.VISIBLE) {

            String finalName = ((EditText)findViewById(R.id.user_name)).getText().toString();
            if (!curentParseUser.getString(Constants.UserProperty.NAME).equals(finalName)) {
                curentParseUser.put(Constants.UserProperty.NAME, finalName);
                curentParseUser.saveEventually();
                UpdateCenter.postUpdateEvent(WelcomeScreen.this, UpdateCenter.EventType.Session);
            }
            WelcomeScreen.this.finish();
        }
        else if (id == R.id.user_image || id == R.id.user_imageF) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Get the layout inflater
            LayoutInflater inflater = this.getLayoutInflater();
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
                    if (WelcomeScreen.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
                        Toast.makeText(WelcomeScreen.this, "OOPS!Sorry,you don't have a camera on this device.", Toast.LENGTH_LONG).show();
                    } else {
                        mCurrentPhotoPath = null;
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
                            startActivityForResult(takePictureIntent, REQ_PROFILE_CAMERA);
                        }
                    }
                }
            });
            galleryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, REQ_PROFILE_GALLERY);
                }
            });
//            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(pickPhoto, 1);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == Activity.RESULT_OK) {
            final ProgressDialog progressDialog = new ProgressDialog(WelcomeScreen.this);
            progressDialog.setMessage("Uploading Image...");
            progressDialog.show();

            Bitmap bitmap = null;
            String propertyName = null;

            if(requestCode == REQ_PROFILE_GALLERY){
                Uri selectedImage = imageReturnedIntent.getData();
                bitmap = Utility.getBitmapFromUri(this, Uri.parse(selectedImage.toString()), 512);
                propertyName = Constants.UserProperty.PROFILE_PIC_FILE;
            }else if(requestCode == REQ_PROFILE_CAMERA){
                Uri selectedImage = Uri.parse(mCurrentPhotoPath);
                bitmap = Utility.getBitmapFromUri(this, Uri.parse(selectedImage.toString()), 512);
                propertyName = Constants.UserProperty.PROFILE_PIC_FILE;
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

                    if (e != null) {
                        msg = "Fail to upload new Image";
                    } else {
                        msg = "successfully uploaded your new Image";
                        if (prop.equalsIgnoreCase(Constants.UserProperty.PROFILE_PIC_FILE)){
                            profilePictureView.setVisibility(View.GONE);
                            userImage.setVisibility(View.VISIBLE);
                            userImage.setImageBitmap(bmp);
                            UpdateCenter.postUpdateEvent(WelcomeScreen.this, UpdateCenter.EventType.Session);
                        }
                    }

                    Snackbar.make(findViewById(R.id.parent), msg, Snackbar.LENGTH_LONG).show();
                    progressDialog.dismiss();
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
}
