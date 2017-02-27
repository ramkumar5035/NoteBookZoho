package com.android.notebook.login;

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
import com.android.notebook.notes.NotesListActivity;
import com.android.notebook.utils.PasswordValidator;
import com.android.notebook.utils.TextUtils;

public class LoginActivity extends BaseActivity {

    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnLogin;
    private TextView btnSignUp;
    private UserDatabaseHelper userDatabaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        setupDefaults();
        setupEvents();
    }

    private void initViews() {
        txtEmail = (EditText) findViewById(R.id.email);
        txtPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.login);
        btnSignUp = (TextView) findViewById(R.id.link_signup);

        userDatabaseHelper = new UserDatabaseHelper(this);
    }

    private void setupDefaults() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle(getString(R.string.login));
        setSupportActionBar(myToolbar);
    }

    private void setupEvents() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    public void login() {
        if (!validate()) {
//            onLoginFailed();
            return;
        }

        btnLogin.setEnabled(false);

        onLoginSuccess();
    }

    public void onLoginSuccess() {
        btnLogin.setEnabled(true);

        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        if (userDatabaseHelper.checkUser(email, password)) {
            Toast.makeText(getBaseContext(), getString(R.string.login_success), Toast.LENGTH_LONG).show();
            getUserPreference().setUserName(email);
            getUserPreference().setPassword(password);
            getUserPreference().setUserLoginStatus(true);

            txtEmail.setText("");
            txtPassword.setText("");

            Intent intent = new Intent(getApplicationContext(), NotesListActivity.class);
            startActivity(intent);
        } else {
            onLoginFailed();
        }
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        btnLogin.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

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

        return valid;
    }
}