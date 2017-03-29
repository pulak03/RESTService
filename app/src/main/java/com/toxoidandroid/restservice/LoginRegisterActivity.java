package com.toxoidandroid.restservice;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.toxoidandroid.restservice.data.User;
import com.toxoidandroid.restservice.webservices.WebServiceTask;
import com.toxoidandroid.restservice.webservices.WebServiceUtils;

import org.json.JSONObject;

public class LoginRegisterActivity extends AppCompatActivity {

    private UserLoginRegisterTask mUserLoginRegisterTask = null;
    private EditText mEmailView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        initViews();
    }

    private void initViews() {
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
    }

    public void attemptLoginRegister(View view) {
        if (mUserLoginRegisterTask != null) {

            return;

        }
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mUserLoginRegisterTask = new UserLoginRegisterTask(email, password, view.getId() == R.id.email_sign_in_button);
            mUserLoginRegisterTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showProgress(final boolean isShow) {
        findViewById(R.id.login_progress).setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.login_form).setVisibility(isShow ? View.GONE : View.INVISIBLE);
    }

    private class UserLoginRegisterTask extends WebServiceTask {

        private final ContentValues mContentValues = new ContentValues();
        private boolean mIsLogin;

        UserLoginRegisterTask(String email, String password, boolean isLogin) {
            super(LoginRegisterActivity.this);
            mContentValues.put(Constants.EMAIL, email);
            mContentValues.put(Constants.PASSWORD, password);
            mContentValues.put(Constants.GRANT_TYPE, Constants.CLIENT_CREDENTIALS);
            mIsLogin = isLogin;
        }

        @Override
        public void showProgress() {
            LoginRegisterActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            LoginRegisterActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            JSONObject obj = WebServiceUtils.requestJSONObject(mIsLogin ? Constants.LOGIN_URL : Constants.SIGNUP_URL,
                    WebServiceUtils.METHOD.POST, mContentValues, true);
            mUserLoginRegisterTask = null;
            if (!hasError(obj)) {
                if (mIsLogin) {
                    User user = new User();
                    user.setId(obj.optLong(Constants.ID));
                    user.setEmail(mContentValues.getAsString(Constants.EMAIL));
                    user.setPassword(mContentValues.getAsString(Constants.PASSWORD));
                    RESTServiceApplication.getInstance().setUser(user);
                    RESTServiceApplication.getInstance().setAccessToken(obj.optJSONObject(Constants.ACCESS)
                            .optString(Constants.ACCESS_TOKEN));
                    return true;
                } else {
                    mIsLogin = true;
                    performRequest();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
