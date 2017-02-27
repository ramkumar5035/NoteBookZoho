package com.android.notebook.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.notebook.R;
import com.android.notebook.base.BaseActivity;
import com.android.notebook.model.Note;
import com.android.notebook.utils.AlertUtils;
import com.android.notebook.utils.TextUtils;
import com.android.notebook.views.EditorView;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.onegravity.rteditor.api.format.RTFormat;

import java.util.Calendar;

public class AddNoteActivity extends BaseActivity {

    static long noteCreationTime = 1;

    private EditorView content;
    private EditorView title;
    private Button btnDone;
    private Calendar calendar;
    private boolean isEditNote = false;
    private RTManager editorViewManager;
    private Note currentNote;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (editorViewManager != null) {
            editorViewManager.onDestroy(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        initEditor(savedInstanceState);
        initViews();
        setupDefaults();
        setupEvents();
    }

    private void initEditor(Bundle savedInstanceState) {
        RTApi rtApi = new RTApi(this, new RTProxyImpl(this), new RTMediaFactoryImpl(this, true));
        editorViewManager = new RTManager(rtApi, savedInstanceState);

        ViewGroup toolbarContainer = (ViewGroup) findViewById(R.id.rte_toolbar_container);
        RTToolbar rtToolbar0 = (RTToolbar) findViewById(R.id.rte_toolbar);
        if (rtToolbar0 != null) {
            editorViewManager.registerToolbar(toolbarContainer, rtToolbar0);
        }
    }

    private void initViews() {
        title = (EditorView) findViewById(R.id.title_editText);
        content = (EditorView) findViewById(R.id.content_edittext);
        editorViewManager.registerEditor(title, true);
        editorViewManager.registerEditor(content, true);
        btnDone = (Button) findViewById(R.id.done);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        noteCreationTime = calendar.getTimeInMillis();
    }

    private void setupDefaults() {
        if (getIntent().hasExtra(NotesListActivity.NOTE_ITEM)) {
            currentNote = (Note) getIntent().getSerializableExtra(NotesListActivity.NOTE_ITEM);
            if (currentNote != null) {
                title.setRichTextEditing(true, currentNote.getTitle());
                content.setRichTextEditing(true, currentNote.getContent());
                noteCreationTime = currentNote.getTimeOfAddition();
                content.requestFocus();
            }
        }


        if (getIntent().hasExtra(NotesListActivity.NOTE_EDIT)) {
            isEditNote = getIntent().getBooleanExtra(NotesListActivity.NOTE_EDIT, false);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditNote ? getString(R.string.edit_note) : getString(R.string.add_note));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupEvents() {
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(title)) {
                    Note note = new Note(title.getText(RTFormat.HTML), content.getText(RTFormat.HTML), noteCreationTime);
                    if (currentNote != null) {
                        note.setOldTitle(currentNote.getTitle());
                        note.setOldContent(currentNote.getContent());
                    }
                    Intent intent = new Intent();
                    intent.putExtra(NotesListActivity.NOTE_ITEM, note);
                    intent.putExtra(NotesListActivity.NOTE_EDIT, isEditNote);
                    setResult(RESULT_OK, intent);
                    finish();
                } else
                    AlertUtils.showToast(AddNoteActivity.this, "Title Should not be empty");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
