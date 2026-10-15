package it.unisa.taskcal.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {

    @Insert
    long insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("SELECT * FROM events WHERE id = :id")
    Event getById(long id);

    @Query("SELECT * FROM events " +
            "WHERE startMillis < :dayEnd AND endMillis >= :dayStart " +
            "ORDER BY allDay DESC, startMillis ASC")
    List<Event> getEventsForDay(long dayStart, long dayEnd);

    @Query("SELECT * FROM events ORDER BY startMillis ASC")
    List<Event> getAll();
}