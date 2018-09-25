package com.mad.remindmehere.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.database.DatabaseErrorHandler;

import com.mad.remindmehere.database.dao.ReminderDao;
import com.mad.remindmehere.model.Reminder;

import static com.mad.remindmehere.database.ReminderDatabase.DATABASE_VERSION;

@Database(entities = Reminder.class, version = DATABASE_VERSION, exportSchema = false)
public abstract class ReminderDatabase extends RoomDatabase {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "reminders_db";

    public abstract ReminderDao reminderDao();

    private static ReminderDatabase mReminderDatabase = null;

    public static ReminderDatabase getReminderDatabase(Context context) {
        if (mReminderDatabase == null) {
            mReminderDatabase = Room.databaseBuilder(context, ReminderDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return mReminderDatabase;
    }
}
