package com.android.notebook.notes;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.notebook.R;
import com.android.notebook.database.NoteDatabaseHelper;
import com.android.notebook.model.Note;
import com.android.notebook.notes.swipe_delete.ItemTouchHelperAdapter;
import com.android.notebook.views.EditorView;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private ArrayList<Note> noteArrayList = new ArrayList<>();
    private NoteItemChangeListener noteItemChangeListener;
    private Activity context;
    private RTManager editorViewManager;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_item, parent, false);
        return new ViewHolder(view);
    }

    public NoteAdapter(ArrayList<Note> noteArrayList, Activity context) {
        this.noteArrayList = noteArrayList;
        this.context = context;
        noteItemChangeListener = (NoteItemChangeListener) context;
        RTApi rtApi = new RTApi(context, new RTProxyImpl(context), new RTMediaFactoryImpl(context, true));
        editorViewManager = new RTManager(rtApi, null);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        editorViewManager.registerEditor(holder.title, true);
        editorViewManager.registerEditor(holder.content, true);

        final Note note = noteArrayList.get(position);
        holder.title.setRichTextEditing(true, note.getTitle());
        holder.content.setRichTextEditing(true, note.getContent());
        holder.title.clearFocus();
        holder.content.clearFocus();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(note.getTimeOfAddition());
        holder.date.setText(new SimpleDateFormat("dd-MM-yyy").format(calendar.getTime()));
//        holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_blue));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteItemChangeListener.onNoteClick(note, position);
            }
        });

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteItemChangeListener.onNoteClick(note, position);
            }
        });

        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteItemChangeListener.onNoteClick(note, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteArrayList.size();
    }

    @Override
    public void onDismiss(final int position) {
        Note removedNote = noteArrayList.get(position);
        NoteDatabaseHelper.getNoteItemDatabase(context).deleteNoteFromDatabase(removedNote);
        noteArrayList.remove(position);
        notifyItemRemoved(position);
        noteItemChangeListener.onNoteDelete(noteArrayList, removedNote);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView date;
        EditorView title;
        EditorView content;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.single_item_card);
            content = (EditorView) itemView.findViewById(R.id.content);
            title = (EditorView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            cardView.setRadius(4);
        }
    }

    public void updateNotes(ArrayList<Note> noteList) {
        this.noteArrayList = noteList;
        notifyDataSetChanged();
    }
}
