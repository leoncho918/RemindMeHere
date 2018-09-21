package com.mad.remindmehere.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.mad.remindmehere.R;
import com.mad.remindmehere.adapter.ReminderAdapter;
import com.mad.remindmehere.model.Reminder;

import java.util.ArrayList;

public class RemindersListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<Reminder> mReminders = new ArrayList<Reminder>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_list);

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
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 10));


        ReminderAdapter adapter = new ReminderAdapter(getApplicationContext(), mReminders, this);
        mRecyclerView.setAdapter(adapter);
    }

    public void addReminder(View view) {
        Intent intent = new Intent(RemindersListActivity.this, AddReminderActivity.class);
        startActivityForResult(intent, RemindersMapsActivity.ADD_REMINDER);
    }
}
