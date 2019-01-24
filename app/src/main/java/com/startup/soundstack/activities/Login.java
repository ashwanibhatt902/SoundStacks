package com.startup.soundstack.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.AccessToken;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.UpdateCenter.EventType;
import com.startup.soundstack.utils.Utility;

import org.json.JSONObject;

import java.util.Arrays;

import bolts.Continuation;
import bolts.Task;


public class Login extends AppCompatActivity {

    TextInputLayout mEmailInput = null;
    TextInputLayout mPasswordInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Answers.getInstance().logCustom(new CustomEvent("Login Activity"));

        mEmailInput = (TextInputLayout) findViewById(R.id.emailid);
        mPasswordInput = (TextInputLayout) findViewById(R.id.passwordP);

        String id = Utility.getPreference(this).getString(Constants.Preference.SYSTEM_LOGIN_ID, "undefined");
        if (!id.equalsIgnoreCase("undefined")) {
            mEmailInput.getEditText().setText(id);
        }
    }

   public void onFBLoginButtonClicked(final View view){

       findViewById(R.id.progrss).setVisibility(View.VISIBLE);
       ParseFacebookUtils.logInWithReadPermissionsInBackground(this, Arrays.asList("public_profile, email"), new LogInCallback() {
           @Override
           public void done(ParseUser user, final ParseException err) {

               if (err != null) {
                   int errCode = err.getCode();
                   switch (errCode) {
                       case ParseException.INVALID_LINKED_SESSION:
                       case ParseException.INVALID_SESSION_TOKEN:
                       case ParseException.MISSING_OBJECT_ID:
                           logout();
                       default:
                           Snackbar.make(findViewById(R.id.parentRL), R.string.login_fail, Snackbar.LENGTH_LONG)
                                   .setAction("Retry", new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Login.this.onFBLoginButtonClicked(view);
                                       }
                                   })
                                   .show();
                           findViewById(R.id.progrss).setVisibility(View.GONE);
                           break;
                   }
               } else if (user == null) {
                   Snackbar.make(findViewById(R.id.parentRL), R.string.login_fail, Snackbar.LENGTH_LONG)
                           .setAction("Retry", new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   Login.this.onFBLoginButtonClicked(view);
                               }
                           })
                           .show();
                   findViewById(R.id.progrss).setVisibility(View.GONE);

               } else if (user.isNew()) {
                   saveUserFbInfo();
                   Log.e("MyApp", "User signed up and logged in through Facebook!");
               } else {
                   if (user != null) {
                       String userName = user.getString(Constants.UserProperty.NAME);
                       if (TextUtils.isEmpty(userName)) {
                           saveUserFbInfo();
                       }
                   }
                   UpdateCenter.postUpdateEvent(Login.this, EventType.Session);
                   Login.this.finish();
                   Log.e("MyApp", "User logged in through Facebook!");
               }

           }
       });
   }
    public void logout() {
        if (ParseUser.getCurrentUser() != null) ParseUser.logOut();
    }

    private void saveUserFbInfo() {

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject obj, GraphResponse response) {
                        FacebookRequestError error = response.getError();
                        if (error == null) {
                            final ParseUser user = ParseUser.getCurrentUser();
                            String name = obj.optString(Constants.UserProperty.NAME);
                            String email = obj.optString(Constants.UserProperty.EMAIL);

                            String id = obj.optString("id");
                            final String profileImg = obj.optJSONObject("picture").optJSONObject("data").optString("url");//("http://graph.facebook.com/" + id + "/picture");
                            String coverImg = obj.optJSONObject("cover").optString("source");

//                            Utility.uploadUserImageAsync(Login.this, profileImg, Constants.UserProperty.COVER_PIC_FILE);

                            user.put(Constants.UserProperty.NAME, name); // this should not be here, as we would ask user to confirm the name to be used in welcome_screen and should be uploaded to parse over there
                            user.put("id", id); // Instead of facebook name we should use Id for our records, as we are planning to prove flexibility with username. moreover the username might not be unique, but the ID would be
                            if(email != null){
                                user.put(Constants.UserProperty.EMAIL_ADD, email); // e-mail record would be useful if we ever in future decide to send e-mails to our users.
                            }
                            user.put(Constants.UserProperty.PROFILE_PIC_URL, profileImg);
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {

                                        Snackbar.make(findViewById(R.id.parentRL), e.getMessage(), Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Retry", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Login.this.onFBLoginButtonClicked(null);
                                                    }
                                                })
                                                .show();

                                        findViewById(R.id.progrss).setVisibility(View.GONE);
                                        return;
                                    }

                                    if (user.getString(Constants.UserProperty.NAME).equals("")) {
                                        user.put(Constants.UserProperty.NAME, user.getObjectId());
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    startWelcomeScreen("facebook");
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        startWelcomeScreen("facebook");
                                    }

                                    Task<ParseObject> parseObjectTask = Utility.uploadUserImageAsync(Login.this, profileImg, Constants.UserProperty.PROFILE_PIC_FILE);

                                    parseObjectTask.continueWith(new Continuation<ParseObject, Object>() {
                                        @Override
                                        public Object then(Task<ParseObject> task) throws Exception {
                                            String msg = "nothing";
                                            if (task.isCompleted()) {
                                                UpdateCenter.postUpdateEvent(Login.this, UpdateCenter.EventType.Session);
                                            }
                                            return null;
                                        }
                                    });

                                }
                            });
                        }

                        else {
                            logout();
                            Snackbar.make(findViewById(R.id.parentRL), R.string.login_fail, Snackbar.LENGTH_LONG)
                                    .setAction("Retry", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Login.this.onFBLoginButtonClicked(null);
                                        }
                                    })
                                    .show();
                            findViewById(R.id.progrss).setVisibility(View.GONE);
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,cover,picture.type(large){url}");
        request.setParameters(parameters);
        request.executeAsync();
    }


    private boolean validationHandling(String email, String Password, int viewID) {

        boolean validationState = true;
        if (!Utility.isValidEmail(email)) {
            mEmailInput.setError(getString(R.string.invalid_email));
            if (viewID == R.id.register) {
                Snackbar hj = Snackbar.make(findViewById(R.id.parentRL), R.string.invalid_email_for_register, Snackbar.LENGTH_LONG);
                hj.show();
            }
            validationState = false;
        }
        else if(!Utility.isValidPassword(Password)) {
            mPasswordInput.setError(getString(R.string.invalid_pass));
            if (viewID == R.id.register) {
                Snackbar.make(findViewById(R.id.parentRL), R.string.enter_pass_for_register, Snackbar.LENGTH_LONG).show();
            }
            validationState = false;
        }
        return validationState;
    }

    private boolean handleError(ParseException e,final View view, boolean r) {
        int errCode = e.getCode();
        boolean handled = false;

        switch (errCode) {
            case ParseException.EMAIL_TAKEN :
                mEmailInput.setError(e.getMessage());
                handled = true;
                break;
            case ParseException.USERNAME_TAKEN :
                mEmailInput.setError(getString(R.string.email_Already_Regiter));
                Snackbar.make(findViewById(R.id.parentRL), R.string.email_Already_Regiter, Snackbar.LENGTH_LONG).show();
                handled = true;
                break;

            case ParseException.INVALID_SESSION_TOKEN:
            case ParseException.INVALID_LINKED_SESSION:
                logout();
                Snackbar.make(findViewById(R.id.parentRL), r?R.string.register_fail:R.string.login_fail, Snackbar.LENGTH_LONG)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Login.this.onClick(view);
                            }
                        })
                        .show();
                handled = true;
                break;

            case ParseException.OBJECT_NOT_FOUND:{
                mPasswordInput.setError(getString(R.string.email_pass_incorret));
                handled = true;
                break;
            }

            default: {
                Snackbar.make(findViewById(R.id.parentRL), r?R.string.register_fail:R.string.login_fail, Snackbar.LENGTH_LONG)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Login.this.onClick(view);
                            }
                        })
                        .show();
            }

        }

        return handled;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    public void onClick(final View view) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        final String email = mEmailInput.getEditText().getText().toString();
        final String password = mPasswordInput.getEditText().getText().toString();
        mEmailInput.setErrorEnabled(false);
        mPasswordInput.setErrorEnabled(false);

        switch (view.getId()) {
            case R.id.register: {

                if (!validationHandling(email, password, view.getId()));

                else {
                    findViewById(R.id.progrss).setVisibility(View.VISIBLE);

                    String username = email.substring(0, email.indexOf('@'));
                    final ParseUser user = new ParseUser();
                    user.setUsername(email);
                    user.setPassword(password);
                    user.setEmail(email);
                    user.put(Constants.UserProperty.EMAIL_ADD, email);
                    user.put(Constants.UserProperty.NAME, username);
                    user.signUpInBackground(new SignUpCallback() {

                        public void done(ParseException e) {
                            if (e == null) {
                                if (user.getString(Constants.UserProperty.NAME).equals("")) {
                                    user.put(Constants.UserProperty.NAME, user.getObjectId());
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                findViewById(R.id.progrss).setVisibility(View.GONE);
                                                startWelcomeScreen("");
                                            }
                                        }
                                    });
                                }
                                else{
                                    findViewById(R.id.progrss).setVisibility(View.GONE);
                                    startWelcomeScreen("");
                                }
                            } else {
                                findViewById(R.id.progrss).setVisibility(View.GONE);
                                if (handleError(e, view, true));
                            }
                        }
                    });

                }
                break;
            }
            case R.id.forgotPass: {
                if (!Utility.isValidEmail(email)) {
                    mEmailInput.setError(getString(R.string.invalid_email));
                }

                else {
                    findViewById(R.id.progrss).setVisibility(View.VISIBLE);
                    ParseUser.requestPasswordResetInBackground(mEmailInput.getEditText().getText().toString(), new RequestPasswordResetCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(findViewById(R.id.parentRL), R.string.pass_reset_success, Snackbar.LENGTH_LONG)
                                        .show();
                            } else {
                                int errCode = e.getCode();
                                switch (errCode) {
                                    case ParseException.EMAIL_NOT_FOUND:
                                        mEmailInput.setError(e.getMessage());
                                        break;
                                    default:
                                        Snackbar.make(findViewById(R.id.parentRL), R.string.pass_reset_fail, Snackbar.LENGTH_LONG)
                                                .setAction("Retry", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Login.this.onClick(view);
                                                    }
                                                })
                                                .show();
                                        break;
                                }

                            }
                            findViewById(R.id.progrss).setVisibility(View.GONE);
                        }
                    });
                }
                break;
            }

            case R.id.loginbtn: {

                if (!validationHandling(email, password, view.getId()));

                else {
                    findViewById(R.id.progrss).setVisibility(View.VISIBLE);

                    ParseUser.logInInBackground(email, password, new LogInCallback() {
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {
                                UpdateCenter.postUpdateEvent(Login.this, EventType.Session);
                                Login.this.finish();
                            } else {
                                if (handleError(e, view, false)) ;
                            }
                            findViewById(R.id.progrss).setVisibility(View.GONE);
                        }
                    });
                }
                break;
            }
        }
    }

    private void startWelcomeScreen(String createdBy) {
        Intent intent = new Intent(Login.this, WelcomeScreen.class);
        Login.this.startActivity(intent);
        this.overridePendingTransition(R.anim.righttoleft,
                R.anim.lefttoright);
        UpdateCenter.postUpdateEvent(Login.this, EventType.Session);
        intent.putExtra("createdBy", createdBy);
//        findViewById(R.id.progrss).setVisibility(View.GONE);
        Login.this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nagative100to0, R.anim.oto100);
    }
}
