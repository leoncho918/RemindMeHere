package com.mad.remindmehere.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.mad.remindmehere.model.Reminder;

import java.util.ArrayList;

public class RemindersListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<Reminder> mReminders = new ArrayList<Reminder>();
    private ArrayList<Reminder> mRemindersList;
    private EditText mSearchViewEt;
    private ReminderDatabase mReminderDatabase;
    private ReminderAdapter mAdapter;
    public static final String NAME = "com.mad.remindmehere.RemindersListActivity.NAME";
    public static final String DESC = "com.mad.remindmehere.RemindersListActivity.DESC";
    public static final String LAT = "com.mad.remindmehere.RemindersListActivity.LAT";
    public static final String LNG = "com.mad.remindmehere.RemindersListActivity.LNG";
    public static final String RADIUS = "com.mad.remindmehere.RemindersListActivity.RADIUS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        startItemTouchHelper();
    }

    private void startItemTouchHelper() {
        ItemTouchHelper.SimpleCallback touchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT) {
                    DeleteRemindersAsyncTask task = new DeleteRemindersAsyncTask();
                    task.execute(position);
                }
                if (direction == ItemTouchHelper.LEFT) {
                    Reminder selectedReminder = mRemindersList.get(position);
                    Intent intent = new Intent(RemindersListActivity.this, EditRemindersActivity.class);
                    intent.putExtra(NAME, selectedReminder.getName());
                    intent.putExtra(DESC, selectedReminder.getDescription());
                    intent.putExtra(LAT, selectedReminder.getLat());
                    intent.putExtra(LNG, selectedReminder.getLng());
                    intent.putExtra(RADIUS, selectedReminder.getRadius());
                    startActivity(intent);
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initialiseDatabase();

        getReminders();
    }

    private void initialiseDatabase() {
        mReminderDatabase = ReminderDatabase.getReminderDatabase(getApplicationContext());
    }

    private void getReminders() {
        RefreshRemindersAsyncTask task = new RefreshRemindersAsyncTask();
        task.execute();
    }

    public void addReminder(View view) {
        Intent intent = new Intent(RemindersListActivity.this, AddReminderActivity.class);
        startActivityForResult(intent, RemindersMapsActivity.ADD_REMINDER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating searchview layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view, menu);
        //Getting reference to searchview
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
                mSearchViewEt.requestFocus();
                return false;
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        //Expanding searchview when icon is pressed.
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        //Removing search icon
        int magnifyId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView magnifyIcon = (ImageView) searchView.findViewById(magnifyId);
        magnifyIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        int editTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        mSearchViewEt = (EditText) searchView.findViewById(editTextId);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mSearchViewEt.getWindowToken(), 0);
            }
        });
        searchReminder();
        return true;
    }

    private void searchReminder() {
        mSearchViewEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Reminder> resultList = new ArrayList<Reminder>();
                for (Reminder reminder : mReminders) {
                    String sName = s.toString().toLowerCase();
                    String rName = reminder.getName().toLowerCase();
                    if (rName.contains(sName)) {
                        resultList.add(reminder);
                    }
                }
                mRemindersList = resultList;
                populateRecyclerView();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void populateRecyclerView() {
        mAdapter = new ReminderAdapter(getApplicationContext(), mRemindersList, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private class RefreshRemindersAsyncTask extends AsyncTask<Void, Void, ArrayList<Reminder>> {
        @Override
        protected ArrayList<Reminder> doInBackground(Void... voids) {
            ArrayList<Reminder> reminders = new ArrayList<Reminder>();
            reminders = (ArrayList<Reminder>)mReminderDatabase.reminderDao().getAll();
            return reminders;
        }

        @Override
        protected void onPostExecute(ArrayList<Reminder> reminders) {
            super.onPostExecute(reminders);
            mReminders = reminders;
            mRemindersList = reminders;
            populateRecyclerView();
        }
    }

    private class DeleteRemindersAsyncTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            mReminderDatabase.reminderDao().deleteReminder(mRemindersList.get(integers[0]));
            return integers[0];
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            Reminder deletedReminder = mRemindersList.get(i);
            mAdapter.removeReminder(i);
            mReminders.remove(deletedReminder);
        }
    }
}
