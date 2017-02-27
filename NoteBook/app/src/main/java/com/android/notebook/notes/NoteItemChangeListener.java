package com.android.notebook.notes;

import com.android.notebook.model.Note;

import java.util.ArrayList;

public interface NoteItemChangeListener {

    void onNoteClick(Note note, int index);

    void onNoteDelete(ArrayList<Note> notes, Note removedNote);
}
