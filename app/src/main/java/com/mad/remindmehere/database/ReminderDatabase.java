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
/**
 * Class for reminder room database
 */
public abstract class ReminderDatabase extends RoomDatabase {
    //Constants
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "reminders_db";
    private static ReminderDatabase mReminderDatabase = null;

    /**
     * Abstract method to return reminder data access object
     * @return
     */
    public abstract ReminderDao reminderDao();

    /**
     * Method to get instance of reminder database
     * @param context
     * @return
     */
    public static ReminderDatabase getReminderDatabase(Context context) {
        //If mReminderDatabase is null then build the reminder database
        if (mReminderDatabase == null) {
            mReminderDatabase = Room.databaseBuilder(context, ReminderDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        //Return instance of reminder database
        return mReminderDatabase;
    }
}
