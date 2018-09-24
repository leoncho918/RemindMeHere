package com.mad.remindmehere.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mad.remindmehere.model.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {
    @Query("SELECT * FROM Reminder")
    List<Reminder> getAll();

    @Query("SELECT *FROM Reminder WHERE mId = :id")
    List<Reminder> getWithId(int id);

    @Update
    void updateReminder(Reminder reminder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addReminder(Reminder reminder);

    @Delete
    void deleteReminder(Reminder reminder);
}
