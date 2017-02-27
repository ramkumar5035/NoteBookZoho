package com.android.notebook.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.notebook.R;
import com.android.notebook.base.BaseActivity;
import com.android.notebook.database.UserDatabaseHelper;
import com.android.notebook.model.User;
import com.android.notebook.notes.NotesListActivity;
import com.android.notebook.utils.PasswordValidator;
import com.android.notebook.utils.TextUtils;

public class SignUpActivity extends BaseActivity {

    private EditText txtName;
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private Button btnSignUp;
    private TextView btnLogin;
    private UserDatabaseHelper userDatabaseHelper;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initViews();
        setupDefaults();
        setupEvents();
    }

    private void initViews() {
        txtName = (EditText) findViewById(R.id.name);
        txtEmail = (EditText) findViewById(R.id.email);
        txtPassword = (EditText) findViewById(R.id.password);
        txtConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        btnLogin = (Button) findViewById(R.id.login);
        btnSignUp = (Button) findViewById(R.id.signup);
        btnLogin = (TextView) findViewById(R.id.link_login);

        userDatabaseHelper = new UserDatabaseHelper(this);
        user = new User();
    }

    private void setupDefaults() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle(getString(R.string.register));
        setSupportActionBar(myToolbar);
    }

    private void setupEvents() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }

        btnSignUp.setEnabled(false);

        onSignupSuccess();
    }


    public void onSignupSuccess() {
        btnSignUp.setEnabled(true);

        String name = txtName.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String reEnterPassword = txtConfirmPassword.getText().toString();

        if (!userDatabaseHelper.checkUser(email)) {
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            userDatabaseHelper.addUser(user);
            Toast.makeText(getBaseContext(), getString(R.string.registration_successful), Toast.LENGTH_LONG).show();

            getUserPreference().setUserName(email);
            getUserPreference().setPassword(password);
            getUserPreference().setUserLoginStatus(true);

            Intent intent = new Intent(getApplicationContext(), NotesListActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
        }
    }

    public void onSignupFailed() {
        btnSignUp.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = txtName.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String reEnterPassword = txtConfirmPassword.getText().toString();

        if (TextUtils.isNullOrEmpty(name)) {
            txtName.setError(getString(R.string.alert_enter_name));
            valid = false;
        } else {
            txtName.setError(null);
        }

        if (TextUtils.isNullOrEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError(getString(R.string.alert_email_not_valid));
            valid = false;
        } else {
            txtEmail.setError(null);
        }

        if (!PasswordValidator.isPasswordLengthValid(password)) {
            txtPassword.setError(getString(R.string.alert_password_length_error));
            valid = false;
        } else if (!PasswordValidator.isPasswordContainsDigits(password)) {
            txtPassword.setError(getString(R.string.alert_password_digits_error));
            valid = false;
        } else if (!PasswordValidator.isPasswordContainsLowercase(password)) {
            txtPassword.setError(getString(R.string.alert_password_lowercase_error));
            valid = false;
        } else if (!PasswordValidator.isPasswordContainsUppercase(password)) {
            txtPassword.setError(getString(R.string.alert_password_uppercase_error));
            valid = false;
        } else if (!PasswordValidator.isPasswordContainsSpecials(password)) {
            txtPassword.setError(getString(R.string.alert_password_specials_error));
            valid = false;
        } else {
            txtPassword.setError(null);
        }

        if (TextUtils.isNullOrEmpty(reEnterPassword) || !(reEnterPassword.equals(password))) {
            txtConfirmPassword.setError(getString(R.string.alert_password_mismatch));
            valid = false;
        } else {
            txtConfirmPassword.setError(null);
        }

        return valid;
    }
}