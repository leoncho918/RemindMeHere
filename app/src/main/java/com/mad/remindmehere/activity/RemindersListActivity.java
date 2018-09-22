package com.mad.remindmehere.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.google.android.gms.maps.model.LatLng;
import com.mad.remindmehere.R;
import com.mad.remindmehere.adapter.ReminderAdapter;
import com.mad.remindmehere.model.Reminder;

import java.util.ArrayList;

public class RemindersListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<Reminder> mReminders = new ArrayList<Reminder>();
    private ArrayList<Reminder> mRemindersSearch;
    private EditText mSearchViewEt;

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

        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(1, "Milk", "Get milk", new LatLng(-32.915609, 150.040804), 30));
        mReminders.add(new Reminder(0, "Eggs", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Lettuce", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Cheese", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Cheese Cake", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Eggplant", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Tomato", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Potato", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Bread", "Get groceries", new LatLng(-33.915609, 151.040804), 10));


        ReminderAdapter adapter = new ReminderAdapter(getApplicationContext(), mReminders, this);
        mRecyclerView.setAdapter(adapter);
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
                mRemindersSearch = resultList;
                changeList();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void changeList() {
        ReminderAdapter adapter = new ReminderAdapter(getApplicationContext(), mRemindersSearch, this);
        mRecyclerView.setAdapter(adapter);
    }
}
