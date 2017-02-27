package com.android.notebook.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.notebook.R;
import com.android.notebook.base.BaseActivity;
import com.android.notebook.database.UserDatabaseHelper;
import com.android.notebook.dropbox.DropBoxAccountManager;
import com.android.notebook.model.User;
import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;

public class ProfileActivity extends BaseActivity {

    public static int REQUEST_SYNC = 10009;

    private UserDatabaseHelper userDatabaseHelper;
    private TextView txtUserName;
    private TextView txtUserEmail;
    private Button btnLogOut;
    private Toolbar toolbar;
    private CheckBox chkSync;
    private DbxAccountManager mDbxAcctMgr;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // result code is always 0 here. Dropbox issue
        if (requestCode == REQUEST_SYNC) {
            if (mDbxAcctMgr != null) {
                DbxAccount acct = mDbxAcctMgr.getLinkedAccount();
                if (acct != null) {
                    getUserPreference().setSyncEnabled(true);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        setupDefaults();
        setupEvents();
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtUserName = (TextView) findViewById(R.id.user_name);
        txtUserEmail = (TextView) findViewById(R.id.user_email);
        btnLogOut = (Button) findViewById(R.id.logout);
        chkSync = (CheckBox) findViewById(R.id.checkbox_sync);

        userDatabaseHelper = new UserDatabaseHelper(this);
        mDbxAcctMgr = DropBoxAccountManager.getAccountManager(this);
    }

    private void setupDefaults() {
        chkSync.setChecked(getUserPreference().isSyncEnabled());
        String email = getUserPreference().getUserName();
        String password = getUserPreference().getUserpwd();

        if (userDatabaseHelper.checkUser(email, password)) {
            User user = userDatabaseHelper.getUser(email, password);
            if (user != null) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(user.getName());
                }
                txtUserName.setText(user.getName());
                txtUserEmail.setText(user.getEmail());
            }
        }
    }

    private void setupEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserPreference().logout();
                setResult(RESULT_OK);
                finish();
            }
        });

        chkSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getUserPreference().setSyncEnabled(false);
                if (isChecked && mDbxAcctMgr != null) {
                    mDbxAcctMgr.startLink(ProfileActivity.this, REQUEST_SYNC);
                }
            }
        });
    }
}
