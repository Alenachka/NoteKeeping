package com.example.notekeeping;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>{

    private final Context mContext;
    private final List<NoteInfo> mNotes;
    private final LayoutInflater mLayoutinflater;

    public NoteRecyclerAdapter(Context mContext, List<NoteInfo> mNotes) {
        this.mContext = mContext;
        mLayoutinflater = LayoutInflater.from(mContext);
        this.mNotes = mNotes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutinflater.inflate( R.layout.item_note_list, parent, false );
        return new ViewHolder( itemView );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoteInfo note = mNotes.get( position );
        holder.mTextCourse.setText( note.getCourse().getTitle() );
        holder.mTextTitle.setText( note.getTitle() );
        holder.mCurPos = position;
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mCurPos;

        public ViewHolder(@NonNull View itemView) {
            super( itemView );
            mTextCourse = (TextView) itemView.findViewById( R.id.text_course );
            mTextTitle = (TextView) itemView.findViewById( R.id.text_title );

            itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent (mContext, MainActivity.class);
                    intent.putExtra(MainActivity.NOTE_POSITION, mCurPos);
                    mContext.startActivity( intent );
                }
            } );
        }
    }
}
