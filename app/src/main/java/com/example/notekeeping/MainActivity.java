package com.example.notekeeping;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String NOTE_POSITION  = "com.example.notekeeping.NOTE_POSITION";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeping.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeping.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeping.ORIGINAL_NOTE_TEXT";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );


        mSpinnerCourses = (Spinner) findViewById( R.id.spinner_courses );

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>( this, android.R.layout.simple_spinner_item, courses );
        adapterCourses.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mSpinnerCourses.setAdapter( adapterCourses );
        
        readDisplayStateValues();
        if(savedInstanceState == null){
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }


        mTextNoteTitle = (EditText) findViewById( R.id.text_note_title );
        mTextNoteText = (EditText) findViewById( R.id.text_note_text );

        if(mIsNewNote){
            createNewNote();
        } else {
            displayNote( mSpinnerCourses, mTextNoteTitle, mTextNoteText );
        }
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString( ORIGINAL_NOTE_COURSE_ID );
        mOriginalNoteTitle = savedInstanceState.getString( ORIGINAL_NOTE_TITLE );
        mOriginalNoteText = savedInstanceState.getString( ORIGINAL_NOTE_TEXT );
    }


    private void saveOriginalNoteValues() {
        if(mIsNewNote)
            return;
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        mNote = dm.getNotes().get( mNotePosition );
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf( mNote.getCourse() );
        spinnerCourses.setSelection( courseIndex );
        textNoteTitle.setText( mNote.getTitle() );
        textNoteText.setText( mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int position = intent.getIntExtra( NOTE_POSITION , POSITION_NOT_SET );
        mIsNewNote = position == POSITION_NOT_SET;
        if(!mIsNewNote)
            mNote = DataManager.getInstance().getNotes().get( position );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel){
            mIsCancelling = true;
            finish();//activity will exit
        } else if (id == R.id.action_next){
            moveNext();
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem( R.id.action_next );
        int lastNoteIndex = DataManager.getInstance().getNotes().size()-1;
        item.setEnabled( mNotePosition < lastNoteIndex );
        return super.onPrepareOptionsMenu( menu );
    }

    private void moveNext() {
        saveNote();

        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get( mNotePosition );

        saveOriginalNoteValues();
        displayNote( mSpinnerCourses, mTextNoteTitle, mTextNoteText );
        invalidateOptionsMenu(); //calls the method onPrepareOptionsMenu
    }

    @Override
    //we save our content here
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewNote){
                DataManager.getInstance().removeNote( mNotePosition );
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState( outState );
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString( ORIGINAL_NOTE_TITLE, mOriginalNoteTitle );
        outState.putString( ORIGINAL_NOTE_TEXT,mOriginalNoteText );
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse( mOriginalNoteCourseId );
        mNote.setCourse( course );
        mNote.setTitle( mOriginalNoteTitle );
        mNote.setText( mOriginalNoteText );

    }

    private void saveNote() {
        mNote.setCourse( (CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle( mTextNoteTitle.getText().toString() );
        mNote.setText( mTextNoteText.getText().toString() );
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check out there \"" + course.getTitle() + "\"\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent( Intent.ACTION_SEND );
        intent.setType( "message/rfc2822" );
        intent.putExtra( Intent.EXTRA_SUBJECT, subject );
        intent.putExtra( Intent.EXTRA_TEXT, text );
        startActivity( intent );
    }
}
