package com.toxoidandroid.restservice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.toxoidandroid.restservice.data.User;
import com.toxoidandroid.restservice.webservices.WebServiceTask;

public class MainActivity extends AppCompatActivity {

    private UserInfoTask mUserInfoTask = null;
    private UserEditTask mUserEditTask = null;
    private UserResetTask mUserResetTask = null;
    private UserDeleteTask mUserDeleteTask = null;
    private EditText mEmailText;
    private EditText mPasswordText;
    private EditText mNameText;
    private EditText mPhoneNumberText;
    private EditText mNoteText;

    private interface ConfirmationListener {
        void onConfirmation(boolean isConfirmed);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        showProgress(true);
        mUserInfoTask = new UserInfoTask();
        mUserInfoTask.execute();
    }

    private void initViews() {
        mEmailText = (EditText) findViewById(R.id.email);
        mPasswordText = (EditText) findViewById(R.id.password);
        mNameText = (EditText) findViewById(R.id.name);
        mPhoneNumberText = (EditText) findViewById(R.id.phoneNumber);
        mNoteText = (EditText) findViewById(R.id.note);
    }

    private void showProgress(final boolean isShow) {
        findViewById(R.id.progress).setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.info_form).setVisibility(isShow ? View.GONE : View.INVISIBLE);
    }

    private void populateText() {
        User user = RESTServiceApplication.getInstance().getUser();
        mEmailText.setText(user.getEmail());
        mPasswordText.setText(user.getPassword());
        mNameText.setText(user.getName() == null ? "" : user.getName());
        mPhoneNumberText.setText(user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
        mNoteText.setText(user.getNote() == null ? "" : user.getNote());
    }

    private abstract class ActivityWebServiceTask extends WebServiceTask {
        public ActivityWebServiceTask(WebServiceTask webServiceTask) {
            super(MainActivity.this);
        }

        @Override
        public void showProgress() {
            MainActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            MainActivity.this.showProgress(false);
        }

        @Override
        public void performSuccessfulOperation() {
            populateText();
        }
    }

    private class UserEditTask extends ActivityWebServiceTask {
        public UserEditTask() {
            super(mUserInfoTask);
        }

        @Override
        public boolean performRequest() {
            return false;
        }
    }

    private class UserResetTask extends ActivityWebServiceTask {
        public UserResetTask() {
            super(mUserInfoTask);
        }

        @Override
        public boolean performRequest() {
            return false;
        }
    }

    private class UserInfoTask extends ActivityWebServiceTask {
        public UserInfoTask() {
            super(mUserInfoTask);
        }

        @Override
        public boolean performRequest() {
            return false;
        }
    }

    private class UserDeleteTask extends ActivityWebServiceTask {
        public UserDeleteTask() {
            super(mUserInfoTask);
        }

        @Override
        public boolean performRequest() {
            return false;
        }
    }
}
