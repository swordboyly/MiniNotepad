package liuyang.drawable_notepad;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Array used to backup data before using search function
    private ArrayList<Note> allNotesSearchArray;

    // Database Handler
    private DatabaseHandler dbHandler;

    // Alert dialogs for back button and delete all notes button
    private AlertDialog alertDialogDeleteAll;
    private AlertDialog alertDialogDeleteSingleNote;

    // Note selected on menu
    private Note selectedNote;

    // Variables used to handle note list
    public static NoteRecyclerAapter noteRecyclerAapter;
    private static RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create DatabaseHandler
        dbHandler = new DatabaseHandler(getApplicationContext());
        recyclerView= (RecyclerView) findViewById(R.id.recyclerview);
        StaggeredGridLayoutManager manager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new SpaceItemDecoration(16,16));
        noteRecyclerAapter=new NoteRecyclerAapter(this,R.layout.listview_item_row,dbHandler.getAllNotesAsArrayList());
        recyclerView.setAdapter(noteRecyclerAapter);
        // Setup AlertDialogs
        alertDialogDeleteAll = initAlertDialogDeleteAllNotes();

        // Floating Action Button listener used to adding new notes
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("id", "-1");
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creating menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        android.widget.SearchView searchView = (android.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint(searchView.getContext().getResources().getString(R.string.search_hint));

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allNotesSearchArray = (ArrayList<Note>) noteRecyclerAapter.getData();
            }
        });

        final android.widget.SearchView.OnQueryTextListener queryTextListener = new android.widget.SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextChange(String newText) {

                ArrayList<Note> filteredNotesArrayList = new ArrayList<>();
                for (Note note : allNotesSearchArray) {
                    if (note.getRawText().contains(newText)) {
                        filteredNotesArrayList.add(note);
                    }
                }
                noteRecyclerAapter.setFilter(filteredNotesArrayList);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Do nothing
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return true;
    }

    /**
     * Method used for first setup of delete all notes button AlertDialog
     */
    private AlertDialog initAlertDialogDeleteAllNotes() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.confirmation)).setTitle(this.getString(R.string.delete_notes_title));
        builder.setPositiveButton(this.getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllNotes();
                Toast.makeText(MainActivity.this, getString(R.string.delete_notes_success),
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(this.getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    private AlertDialog setupAlertDialogDeleteSingleNote0() {
        final Note slcNote=noteRecyclerAapter.getPositionItem();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.confirmation)).setTitle(String.format(this.getString(R.string.delete_note_number), slcNote.getId()));
        builder.setPositiveButton(this.getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHandler.deleteNote(slcNote);
                noteRecyclerAapter.setFilter(dbHandler.getAllNotesAsArrayList());
                Toast.makeText(MainActivity.this, String.format(getString(R.string.note_deleted), slcNote.getId()),
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(this.getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }


    /**
     * Method used to show AlertDialog when delete all notes button is clicked
     */
    public void showAlertDialogDeleteAllNotes(MenuItem menuItem) {
        alertDialogDeleteAll.show();
    }

    public void showAbout(MenuItem menuItem){
        Intent intent=new Intent(this,AboutActivity.class);
        startActivity(intent);
    }

    /**
     * Method used to show AlertDialog when delete note button is clicked
     */
    private void showAlertDialogDeleteSingleNote() {
        alertDialogDeleteSingleNote.show();
    }
    /**
     * Method used to share the chosen note
     */
    private void shareNote(){
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, noteRecyclerAapter.getPositionItem().getRawText());
        startActivity(Intent.createChooser(textIntent, "分享"));
    }


    /**
     * Method used to delete all notes via DatabaseHandler
     */
    public void deleteAllNotes() {
        dbHandler.clearAllNotes();
        noteRecyclerAapter.setFilter(dbHandler.getAllNotesAsArrayList());
    }

    /**
     * Method used to enter note edition mode
     *
     * @param noteId ID number of the Note entry in the SQLite database
     */
    private void editNote(int noteId) {
        hideSoftKeyboard();
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("id", String.valueOf(noteId));
        startActivity(intent);
    }

    /**
     * Method used to hide keyboard
     */
    private void hideSoftKeyboard() {
        if (this.getCurrentFocus() != null) {
            try {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
            } catch (RuntimeException e) {
                //ignore
            }
        }
    }



    public void setListViewData(ArrayList<Note> allNotes, Note newNote) {
        if (noteRecyclerAapter != null) {
            if (newNote != null){

            }
            noteRecyclerAapter.setFilter(allNotes);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                hideSoftKeyboard();
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("id", "-1");
                startActivity(intent);
                break;
            case 1:
                hideSoftKeyboard();
                alertDialogDeleteSingleNote = setupAlertDialogDeleteSingleNote0();
                showAlertDialogDeleteSingleNote();
                break;
            case 2:
                editNote(noteRecyclerAapter.getPositionItem().getId());
                break;
            case 3:
                shareNote();
            case 4:
                break;

        }
        return super.onContextItemSelected(item);


    }

    public void showHelp(MenuItem menuItem){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.action_help));
        builder.setMessage(getString(R.string.help));
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        noteRecyclerAapter.setFilter(dbHandler.getAllNotesAsArrayList());
        super.onResume();
    }
}