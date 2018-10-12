package com.mad.remindmehere.activity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mad.remindmehere.R;
import com.mad.remindmehere.adapter.ReminderAdapter;
import com.mad.remindmehere.database.ReminderDatabase;
import com.mad.remindmehere.geofence.Geofencing;
import com.mad.remindmehere.model.Reminder;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

/**
 * This activity handles all the functions and behaviour displayed in the activity to display all reminders in a list
 */
public class RemindersListActivity extends AppCompatActivity {

    //Variables to store data
    private RecyclerView mRecyclerView;
    private ArrayList<Reminder> mReminders = new ArrayList<Reminder>();
    private ArrayList<Reminder> mRemindersList;
    private EditText mSearchViewEt;
    private Boolean firstRun = true;

    //Variables to store objects
    private ReminderDatabase mReminderDatabase;
    private ReminderAdapter mAdapter;
    private Geofencing mGeofencing;

    //Constants
    public static final String NAME = "com.mad.remindmehere.RemindersListActivity.NAME";
    public static final String DESC = "com.mad.remindmehere.RemindersListActivity.DESC";
    public static final String LAT = "com.mad.remindmehere.RemindersListActivity.LAT";
    public static final String LNG = "com.mad.remindmehere.RemindersListActivity.LNG";
    public static final String RADIUS = "com.mad.remindmehere.RemindersListActivity.RADIUS";
    public static final String POSITION = "com.mad.remindmehere.RemindersListActivity.POSITION";
    public static final int UPDATE_REMINDER = 5;

    /**
     * Method called when activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Link xml layout to activity
        setContentView(R.layout.activity_reminders_list);
        //Set the statusbar colour if Android Version is Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        //Linking toolbar from xml layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Setting toolbar as the support action bar
        setSupportActionBar(toolbar);
        //Set navigation icon to custom drawable
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        //Set on click listener for navigation icon to close activity when pressed
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //Linking recycler view widget from xml layout
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //Notifies the recycler view that it's size isn't affected by adapter content
        mRecyclerView.setHasFixedSize(true);
        //Creating layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //Setting the layout manager for the recycler view
        mRecyclerView.setLayoutManager(linearLayoutManager);

        //Method call to create an itemtouchhelper
        startItemTouchHelper();

        //Method call to create object of geofencer
        initaliseGeofencer();
    }

    /**
     * Method to create a item touch helper and add colour and icons when a item in recycler view is swiped.
     * If item is swiped right reminder is deleted from database via async task, if swiped right EditReminderActivity is launched
     */
    private void startItemTouchHelper() {
        //Create new callback
        ItemTouchHelper.SimpleCallback touchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            //Method called when dragged item is moved from old position to a new position
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            //Method called when item in recyclerview is swiped
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //Create final variable to save position of swiped reminder
                final int position = viewHolder.getAdapterPosition();
                //If direction is right then create asynctask to delete reminder from the list.
                if (direction == ItemTouchHelper.RIGHT) {
                    DeleteRemindersAsyncTask task = new DeleteRemindersAsyncTask();
                    task.execute(position);
                }
                //If direction is left
                if (direction == ItemTouchHelper.LEFT) {
                    //Save the reminder as a new object
                    Reminder selectedReminder = mRemindersList.get(position);
                    //Create a new intent to start EditRemindersActivity
                    Intent intent = new Intent(RemindersListActivity.this, EditRemindersActivity.class);
                    //Pass reminder variables into intent
                    intent.putExtra(NAME, selectedReminder.getName());
                    intent.putExtra(DESC, selectedReminder.getDescription());
                    intent.putExtra(LAT, selectedReminder.getLat());
                    intent.putExtra(LNG, selectedReminder.getLng());
                    intent.putExtra(RADIUS, selectedReminder.getRadius());
                    intent.putExtra(POSITION, position);
                    //Start activity and wait for a result
                    startActivityForResult(intent, UPDATE_REMINDER);
                }
            }

            //Called by itemtouchhelper on recyclerview's ondraw callback
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                //Create new RecyclerViewSwipeDecorator builder to set the color and icon when recyclerview item is swiped left or right
                new RecyclerViewSwipeDecorator.Builder(RemindersListActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(getColor(R.color.delete)).addSwipeRightActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftBackgroundColor(getColor(R.color.edit)).addSwipeLeftActionIcon(R.drawable.ic_edit)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        //Create new itemtouchhelper with itemtouchhelper callback
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchCallback);
        //Attach itemtouchhelper to recyclerview
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * Method to create an instance of geofencer class
     */
    private void initaliseGeofencer() {
        mGeofencing = Geofencing.getInstance(getApplicationContext());
    }

    /**
     * Method called when activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();

        //Method call to get instance of reminder databse
        initialiseDatabase();

        //Method call to start asynctask to get reminders from database
        getReminders();
    }

    /**
     * Method to get instance of reminder database
     */
    private void initialiseDatabase() {
        mReminderDatabase = ReminderDatabase.getReminderDatabase(getApplicationContext());
    }

    /**
     * Method to start async task and get reminders from database via asynctask
     */
    private void getReminders() {
        RefreshRemindersAsyncTask task = new RefreshRemindersAsyncTask();
        task.execute();
    }

    /**
     * Method called when add reminder button is clicked and starts the AddReminderActivity
     * @param view
     */
    public void addReminder(View view) {
        //Create a new intent to start AddReminderActivity
        Intent intent = new Intent(RemindersListActivity.this, AddReminderActivity.class);
        //Start the activity and wait for a result
        startActivityForResult(intent, RemindersMapsActivity.ADD_REMINDER);
    }

    /**
     * Method called menu items are created and sets a searchicon which has a onclicklistener
     * which opens a edittext for searching through the list of reminders
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating searchview layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view, menu);
        //Getting search menuitem
        MenuItem searchItem = menu.findItem(R.id.search);
        //Setting onmenuitemclicklistener
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            //Method called when menuitem is clicked
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Creating a new inputmethodmanager and making soft keyboard appear
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
                //Set typing cursor into SearchView EditText
                mSearchViewEt.requestFocus();
                return false;
            }
        });
        //Expanding searchview when icon is pressed.
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        //Removing search icon
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        //Getting id of magnifying glass icon
        int magnifyId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        //Creating imageview of magnifying glass icon from searchview
        ImageView magnifyIcon = (ImageView) searchView.findViewById(magnifyId);
        //Set layout of magnifying icon
        magnifyIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        //Getting id of searchview edit text
        int editTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        //Linking ui searchview edit text widget
        mSearchViewEt = (EditText) searchView.findViewById(editTextId);
        //Setting listener for when edittext no longer has focus
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            //Called when focus changes
            public void onFocusChange(View v, boolean hasFocus) {
                //Hide keyboard when edittext no longer has focus
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mSearchViewEt.getWindowToken(), 0);
            }
        });
        //Method call to start textchangedlistener
        searchReminder();
        return true;
    }

    /**
     * Method to create listener for when text in edittext of searchview changes
     */
    private void searchReminder() {
        //Adding textchangedlistner
        mSearchViewEt.addTextChangedListener(new TextWatcher() {
            //Method called before text changes
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            //Method called when text changes
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Create arraylist to store result reminders when searching for reminders
                ArrayList<Reminder> resultList = new ArrayList<Reminder>();
                //For each loop to compare each reminder name to text in edittext
                for (Reminder reminder : mReminders) {
                    //Saving text in edittext to string in lowercase
                    String sName = s.toString().toLowerCase();
                    //Saving reminder name to string in lowercase
                    String rName = reminder.getName().toLowerCase();
                    //If searchedtext is found in reminder name
                    if (rName.contains(sName)) {
                        //Add reminder to result arraylist.
                        resultList.add(reminder);
                    }
                }
                //Save results to mRemindersList
                mRemindersList = resultList;
                //Refresh recyclerview
                populateRecyclerView();
            }
            //Method called after text changes
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Method to populate recyclerview with reminders in list
     */
    private void populateRecyclerView() {
        //If this is the first time running this method
        if (firstRun) {
            //Get instance of ReminderAdapter and set adapter to recyclerview
            mAdapter = ReminderAdapter.getInstance(getApplicationContext(), mRemindersList, this);
            mRecyclerView.setAdapter(mAdapter);
            firstRun = false;
        }
        //Else update dataset in adapter and notify that dataset as changed
        else {
            mAdapter.updateReminders(mRemindersList);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Method called when result is received
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check that request and result codes are equal to request code sent with intent
        if (requestCode == UPDATE_REMINDER) {
            if (resultCode == UPDATE_REMINDER) {
                //Get position from intent
                int pos = data.getIntExtra(POSITION, EditRemindersActivity.DEFAULT_POSITION);
                //Create new asynctask to delete reminder from databased and pass in position of reminder
                DeleteRemindersAsyncTask task = new DeleteRemindersAsyncTask();
                task.execute(pos);
            }
        }
    }

    /**
     * Class to retrieve all reminders from room database
     */
    private class RefreshRemindersAsyncTask extends AsyncTask<Void, Void, ArrayList<Reminder>> {
        //Method called when asynctask starts
        @Override
        protected ArrayList<Reminder> doInBackground(Void... voids) {
            //Create new arraylist for reminders
            ArrayList<Reminder> reminders = new ArrayList<Reminder>();
            //Populate new arraylist with reminders in room database
            reminders = (ArrayList<Reminder>)mReminderDatabase.reminderDao().getAll();
            //Return reminders list
            return reminders;
        }
        //Method called when asynctask finishes running
        @Override
        protected void onPostExecute(ArrayList<Reminder> reminders) {
            super.onPostExecute(reminders);
            //Update reminders lists with reminders from database
            mReminders = reminders;
            mRemindersList = reminders;
            //Method call to popular recyclerview with reminders
            populateRecyclerView();
            //Unregister all geofences, rebuild geofences with new reminders and register new geofences
            mGeofencing.unRegisterGeofences();
            mGeofencing.updateGeofences(reminders);
            mGeofencing.registerGeofences();
        }
    }

    /**
     * Class to delete reminder from room database
     */
    private class DeleteRemindersAsyncTask extends AsyncTask<Integer, Void, Integer> {
        //Method called when asynctask starts
        @Override
        protected Integer doInBackground(Integer... integers) {
            //Get reminder from reminderlist with position and delete it from room database
            mReminderDatabase.reminderDao().deleteReminder(mRemindersList.get(integers[0]));
            //Return deleted reminder position in recyclerview
            return integers[0];
        }
        //Method called when asynctask finishes running
        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            //Create new reminder object of deleted reminder
            Reminder deletedReminder = mRemindersList.get(i);
            //Remove deleted reminder from adapter with position
            mAdapter.removeReminder(i);
            //Remove deleted reminder from reminder list
            mReminders.remove(deletedReminder);
            //Method call to retrieve reminders from database
            getReminders();
        }
    }
}
