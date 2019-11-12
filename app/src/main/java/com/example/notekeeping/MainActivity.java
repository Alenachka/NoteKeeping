package com.example.notekeeping;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.notekeeping.NotekeepingDatabaseContract.CourseInfoEntry;
import com.example.notekeeping.NotekeepingDatabaseContract.NoteInfoEntry;

import static com.example.notekeeping.NoteKeepingProviderContract.*;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String NOTE_ID = "com.example.notekeeping.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeping.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeping.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeping.ORIGINAL_NOTE_TEXT";
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private NoteInfo mNote = new NoteInfo( DataManager.getInstance().getCourses().get( 0 ), "", "" );
    ;
    boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteId;
    private boolean mIsCancelling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeepingOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCoursIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapaterCourses;
    private boolean mCoursesQueryFnished;
    private boolean mNotesQueryFinished;
    private Uri mNoteUri;
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mDbOpenHelper = new NoteKeepingOpenHelper( this );

        mSpinnerCourses = (Spinner) findViewById( R.id.spinner_courses );

        // List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mAdapaterCourses = new SimpleCursorAdapter( this, android.R.layout.simple_spinner_item, null,
                //taking the values which id is Column course title
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                //and display them in the view whose id is text1
                new int[]{android.R.id.text1}, 0 );
        mAdapaterCourses.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mSpinnerCourses.setAdapter( mAdapaterCourses );

        getSupportLoaderManager().initLoader( LOADER_COURSES, null, this );

        readDisplayStateValues();
        if (savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues( savedInstanceState );
        }

        mTextNoteTitle = (EditText) findViewById( R.id.text_note_title );
        mTextNoteText = (EditText) findViewById( R.id.text_note_text );

        if (!mIsNewNote) {
            getSupportLoaderManager().initLoader( LOADER_NOTES, null, this );
        }
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query( CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE );
        mAdapaterCourses.changeCursor( cursor );
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString( mNoteId )};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        mNoteCursor = db.query( NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null );
        mCoursIdPos = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_COURSE_ID );
        mNoteTitlePos = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TITLE );
        mNoteTextPos = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TEXT );
        mNoteCursor.moveToNext();
        displayNote();

    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString( ORIGINAL_NOTE_COURSE_ID );
        mOriginalNoteTitle = savedInstanceState.getString( ORIGINAL_NOTE_TITLE );
        mOriginalNoteText = savedInstanceState.getString( ORIGINAL_NOTE_TEXT );
    }


    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    private void createNewNote() {
        ContentValues values = new ContentValues();
        values.put( NoteInfoEntry.COLUMN_COURSE_ID, "" );
        values.put( NoteInfoEntry.COLUMN_NOTE_TITLE, "" );
        values.put( NoteInfoEntry.COLUMN_NOTE_TEXT, "" );

        mNoteUri = getContentResolver().insert( Notes.CONTENT_URI, values );
    }

    private void displayNote() {
        String courseId = mNoteCursor.getString( mCoursIdPos );
        String noteTitle = mNoteCursor.getString( mNoteTitlePos );
        String noteText = mNoteCursor.getString( mNoteTextPos );

        //use courseIndex to get selection on spinner
        int courseIndex = getIndexOfCourseId( courseId );

        mSpinnerCourses.setSelection( courseIndex );
        mTextNoteTitle.setText( noteTitle );
        mTextNoteText.setText( noteText );
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapaterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex( CourseInfoEntry.COLUMN_COURSE_ID );
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more) {
            String cursorCourseId = cursor.getString( courseIdPos );
            if (courseId.equals( cursorCourseId ))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra( NOTE_ID, ID_NOT_SET );
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }
        //mNote = DataManager.getInstance().getNotes().get( mNoteId );
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
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();//activity will exit
        } else if (id == R.id.action_next) {
            moveNext();
        } else if (id == R.id.action_set_reminder) {
            showReminderNotification();
        }

        return super.onOptionsItemSelected( item );
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void showReminderNotification() {
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        NoteReminderNotification.notify( this, noteTitle, noteText);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem( R.id.action_next );
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled( mNoteId < lastNoteIndex );
        return super.onPrepareOptionsMenu( menu );
    }

    private void moveNext() {
        saveNote();

        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get( mNoteId );

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu(); //calls the method onPrepareOptionsMenu
    }

    @Override
    //we save our content here
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            Log.i( TAG, "Cancelling note at position: " + mNoteId );
            if (mIsNewNote) {
                deleteNoteFromDatabase();
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
    }

    private void deleteNoteFromDatabase() {

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                getContentResolver().delete( mNoteUri, null, null );
                return null;
            }
        };
        task.execute();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState( outState );
        outState.putString( ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId );
        outState.putString( ORIGINAL_NOTE_TITLE, mOriginalNoteTitle );
        outState.putString( ORIGINAL_NOTE_TEXT, mOriginalNoteText );
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse( mOriginalNoteCourseId );
        mNote.setCourse( course );
        mNote.setTitle( mOriginalNoteTitle );
        mNote.setText( mOriginalNoteText );

    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase( courseId, noteTitle, noteText );
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapaterCourses.getCursor();
        cursor.moveToPosition( selectedPosition );
        int courseIdPos = cursor.getColumnIndex( CourseInfoEntry.COLUMN_COURSE_ID );
        String courseId = cursor.getString( courseIdPos );
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        ContentValues values = new ContentValues();
        values.put( Notes.COLUMN_COURSE_ID, courseId );
        values.put( Notes.COLUMN_NOTE_TITLE, noteTitle );
        values.put( Notes.COLUMN_NOTE_TEXT, noteText );

        getContentResolver().update( mNoteUri, values, null, null );
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFnished = false;
        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };
        return new CursorLoader( this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE );

    }

    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;
        String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };
        mNoteUri = ContentUris.withAppendedId( Notes.CONTENT_URI, mNoteId );
        return new CursorLoader( this, mNoteUri, noteColumns, null, null, null );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes( data );
        else if (loader.getId() == LOADER_COURSES) {
            mAdapaterCourses.changeCursor( data );
            mCoursesQueryFnished = true;
            displayNoteWhenQueriesFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCoursIdPos = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_COURSE_ID );
        mNoteTitlePos = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TITLE );
        mNoteTextPos = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TEXT );

        mNoteCursor.moveToFirst();

        mNotesQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if (mNotesQueryFinished && mCoursesQueryFnished)
            displayNote();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES) {
            if (mNoteCursor != null && !mNoteCursor.isClosed())
                mNoteCursor.close();
        } else if (loader.getId() == LOADER_COURSES) {
            mAdapaterCourses.changeCursor( null );
        }
    }
}
