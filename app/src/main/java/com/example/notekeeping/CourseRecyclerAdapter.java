package com.example.notekeeping;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder>{

    private final Context mContext;
    private final List<CourseInfo> mCourses;
    private final LayoutInflater mLayoutinflater;

    public CourseRecyclerAdapter(Context mContext, List<CourseInfo> courses) {
        this.mContext = mContext;
        mLayoutinflater = LayoutInflater.from(mContext);
        this.mCourses = courses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutinflater.inflate( R.layout.item_course_list, parent, false );
        return new ViewHolder( itemView );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseInfo course = mCourses.get( position );
        holder.mTextCourse.setText( course.getTitle() );
        holder.mCurPos = position;
    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public int mCurPos;

        public ViewHolder(@NonNull View itemView) {
            super( itemView );
            mTextCourse = (TextView) itemView.findViewById( R.id.text_course );

            itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make( v,  mCourses.get( mCurPos ).getTitle(), Snackbar.LENGTH_LONG).show();
                }
            } );
        }
    }
}
