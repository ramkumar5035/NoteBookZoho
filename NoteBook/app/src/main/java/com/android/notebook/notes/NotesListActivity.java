package com.android.notebook.notes;

import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.notebook.R;
import com.android.notebook.base.BaseActivity;
import com.android.notebook.database.NoteDatabaseHelper;
import com.android.notebook.database.UserDatabaseHelper;
import com.android.notebook.dropbox.DropBoxAccountManager;
import com.android.notebook.login.LoginActivity;
import com.android.notebook.model.Note;
import com.android.notebook.model.User;
import com.android.notebook.notes.swipe_delete.SimpleTouchHelperCallback;
import com.android.notebook.profile.ProfileActivity;
import com.android.notebook.splash.SplashActivity;
import com.android.notebook.utils.TextUtils;
import com.android.notebook.views.RecyclerViewItemOffsetDecoration;
import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class NotesListActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, NoteItemChangeListener {

    public static int REQUEST_PROFILE = 10001;
    public static int REQUEST_NOTE = 10002;

    public static String NOTE_ITEM = "com.android.notebook.notes.NOTE_ITEM";
    public static String NOTE_EDIT = "com.android.notebook.notes.NOTE_EDIT";

    private UserDatabaseHelper userDatabaseHelper;
    private LinearLayout headerView;
    private TextView txtUserName;
    private TextView txtUserEmail;
    private SearchView searchView;
    private boolean isFromSplash = false;

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private ArrayList<Note> noteArrayList;
    private NoteDatabaseHelper noteDatabaseHelper;
    private DbxAccountManager mDbxAcctMgr;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PROFILE) {
                if (isFromSplash) {
                    Intent intent = new Intent(NotesListActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish();
            } else if (requestCode == REQUEST_NOTE && data != null) {
                if (data.getExtras() != null) {
                    Note note = (Note) data.getSerializableExtra(NOTE_ITEM);
                    boolean isEdit = data.getBooleanExtra(NOTE_EDIT, false);

                    if (isEdit) {
                        noteDatabaseHelper.updateNoteItem(note);
                        noteArrayList = getNoteList();
                        noteAdapter.updateNotes(noteArrayList);
                        syncUpdateNote(note);
                    } else {
                        noteDatabaseHelper.addNoteItem(note);
                        noteArrayList = getNoteList();
                        noteAdapter.updateNotes(noteArrayList);
                        recyclerView.scrollToPosition(0);
                        new SyncAddNoteTask(note).execute();
                    }
                    checkForEmptyList();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
        initViews();
        setupDefaults();
        setupEvents();
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        headerView = (LinearLayout) header.findViewById(R.id.header_view);
        txtUserName = (TextView) header.findViewById(R.id.user_name);
        txtUserEmail = (TextView) header.findViewById(R.id.user_email);
        userDatabaseHelper = new UserDatabaseHelper(this);

        mDbxAcctMgr = DropBoxAccountManager.getAccountManager(this);
    }

    private void setupDefaults() {
        String email = getUserPreference().getUserName();
        String password = getUserPreference().getUserpwd();

        if (userDatabaseHelper.checkUser(email, password)) {
            User user = userDatabaseHelper.getUser(email, password);
            if (user != null) {
                txtUserName.setText(user.getName());
                txtUserEmail.setText(user.getEmail());
            }
        }

        if (getIntent().hasExtra(SplashActivity.IS_FROM_SPLASH)) {
            isFromSplash = getIntent().getBooleanExtra(SplashActivity.IS_FROM_SPLASH, false);
        }

        noteArrayList = getNoteList();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerViewItemOffsetDecoration itemDecoration = new RecyclerViewItemOffsetDecoration(this, R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        noteAdapter = new NoteAdapter(noteArrayList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(noteAdapter);
        ItemTouchHelper.Callback callback = new SimpleTouchHelperCallback(noteAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    private void setupEvents() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }

                Intent intent = new Intent(NotesListActivity.this, AddNoteActivity.class);
                intent.putExtra(NOTE_EDIT, false);
                startActivityForResult(intent, REQUEST_NOTE);
            }
        });

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivityForResult(intent, REQUEST_PROFILE);
            }
        });
    }

    private ArrayList<Note> getNoteList() {
        noteArrayList = new ArrayList<>();
        noteDatabaseHelper = NoteDatabaseHelper.getNoteItemDatabase(this);
        noteArrayList = noteDatabaseHelper.getNoteFromDatabase();
        Collections.reverse(noteArrayList);

        if (searchView != null && !searchView.isIconified()) {
            EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            if (searchPlate != null) {
                String key = searchPlate.getText().toString().trim();
                if (!TextUtils.isNullOrEmpty(key) && noteArrayList.size() > 0) {
                    key = key.toLowerCase();
                    ArrayList<Note> searchResult = new ArrayList<>();
                    for (Note note : noteArrayList) {
                        String title = note.getTitle().toLowerCase();
                        if (title.startsWith(key)) {
                            searchResult.add(note);
                        }
                    }
                    return searchResult;
                }
            }
        }

        return noteArrayList;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            finishAffinity();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notes_list, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    return false;
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //some operation
                }
            });
            EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchPlate.setHint(getString(R.string.search));
            View searchPlateView = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
            searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    noteArrayList = getNoteList();
                    noteAdapter.updateNotes(noteArrayList);
                    checkForEmptyList();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    noteArrayList = getNoteList();
                    noteAdapter.updateNotes(noteArrayList);
                    checkForEmptyList();
                    return false;
                }
            });
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivityForResult(intent, REQUEST_PROFILE);
        } else if (id == R.id.nav_logout) {
            getUserPreference().logout();
            if (isFromSplash) {
                Intent intent = new Intent(NotesListActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForEmptyList();
//        new SyncRefreshNowTask().execute();
    }

    @Override
    public void onNoteClick(Note note, int index) {
        Intent intent = new Intent(NotesListActivity.this, AddNoteActivity.class);
        intent.putExtra(NOTE_ITEM, note);
        intent.putExtra(NOTE_EDIT, true);
        startActivityForResult(intent, REQUEST_NOTE);
    }

    @Override
    public void onNoteDelete(ArrayList<Note> notes, Note removedNote) {
        Snackbar.make(findViewById(R.id.fab), "Note Deleted", Snackbar.LENGTH_SHORT).show();
        checkForEmptyList();
        new SyncRemoveNoteTask(removedNote, true).execute();
    }

    public void checkForEmptyList() {
        TextView empty = (TextView) findViewById(R.id.note_empty);
        TextView hint = (TextView) findViewById(R.id.note_hint);
        empty.setVisibility(noteArrayList.size() == 0 ? View.VISIBLE : View.GONE);
        hint.setVisibility(noteArrayList.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private class SyncRefreshNowTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getUserPreference().isSyncEnabled()) {
                    if (mDbxAcctMgr == null) {
                        mDbxAcctMgr = DropBoxAccountManager.getAccountManager(NotesListActivity.this);
                    }
                    if (mDbxAcctMgr != null) {
                        DbxAccount acct = mDbxAcctMgr.getLinkedAccount();
                        if (acct != null) {
                            DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount()).syncNowAndWait();
                        }
                    }
                }
            } catch (DbxException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class SyncRemoveNoteTask extends AsyncTask<Void, Integer, Void> {
        private Note note;
        private boolean removeCurrentNote;

        public SyncRemoveNoteTask(Note removedNote, boolean removeCurrentNote) {
            this.note = removedNote;
            this.removeCurrentNote = removeCurrentNote;
        }

        @Override
        protected Void doInBackground(Void... params) {
            syncRemoveNote(note, removeCurrentNote);
            return null;
        }
    }

    private class SyncAddNoteTask extends AsyncTask<Void, Integer, Void> {
        private Note note;

        public SyncAddNoteTask(Note note) {
            this.note = note;
        }

        @Override
        protected Void doInBackground(Void... params) {
            syncAddNote(note);
            return null;
        }
    }

    private void syncAddNote(Note note) {
        if (!getUserPreference().isSyncEnabled()) return;
        if (note == null) return;

        if (mDbxAcctMgr == null) {
            mDbxAcctMgr = DropBoxAccountManager.getAccountManager(this);
        }

        DbxAccount acct = mDbxAcctMgr.getLinkedAccount();
        if (null == acct) {
            Log.e("DbxAccount", "No linked account.");
            return;
        }

        DbxFileSystem dbxFs = null;
        try {
            dbxFs = DbxFileSystem.forAccount(acct);
        } catch (DbxException.Unauthorized unauthorized) {
            unauthorized.printStackTrace();
        }

        if (dbxFs == null) return;

        DbxFile testFile = null;
        String filePath = note.getTitle();
        if (!filePath.endsWith(".txt")) {
            filePath += ".txt";
        }

        try {
            testFile = dbxFs.create(new DbxPath(filePath));
            if (testFile != null)
                testFile.writeString(note.getContent());
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (testFile != null) {
                testFile.close();
            }
        }
    }

    private void syncRemoveNote(Note note, boolean removeCurrentNote) {
        if (!getUserPreference().isSyncEnabled()) return;
        if (note == null) return;

        if (mDbxAcctMgr == null) {
            mDbxAcctMgr = DropBoxAccountManager.getAccountManager(this);
        }

        String filePath = removeCurrentNote ? note.getTitle() : note.getOldTitle();
        if (!filePath.endsWith(".txt")) {
            filePath += ".txt";
        }

        DbxAccount acct = mDbxAcctMgr.getLinkedAccount();
        if (null == acct) {
            Log.e("DbxAccount", "No linked account.");
            return;
        }

        DbxPath p = null;
        try {
            p = new DbxPath("/" + filePath);
        } catch (DbxPath.InvalidPathException e) {
            e.printStackTrace();
        }

        try {
            if (p != null)
                DbxFileSystem.forAccount(acct).delete(p);
        } catch (DbxException.Exists e) {
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    private void syncUpdateNote(Note note) {
        if (note.getTitle().equals(note.getOldTitle())
                && note.getContent().equals(note.getOldContent())) {
            return;
        }

        new SyncRemoveNoteTask(note, false).execute();
        new SyncAddNoteTask(note).execute();
    }
}
