package com.mad.remindmehere.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.Update;

import com.mad.remindmehere.model.Reminder;

import java.util.List;

/**
 * Data Access Object for Reminder
 */
@Dao
public interface ReminderDao {
    /**
     * Returns a list of all reminders in database
     * @return
     */
    @Query("SELECT * FROM reminders")
    List<Reminder> getAll();

    /**
     * Returns a list of reminders with the specified id
     * @param id
     * @return
     */
    @Query("SELECT *FROM reminders WHERE id = :id")
    List<Reminder> getWithId(int id);

    /**
     * Inserts the given reminder into the database
     * @param reminder
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addReminder(Reminder reminder);

    /**
     * Deleted the given reminder from the database
     * @param reminder
     */
    @Delete
    void deleteReminder(Reminder reminder);
}
