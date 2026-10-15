package it.unisa.taskcal;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unisa.taskcal.data.Event;
import it.unisa.taskcal.data.EventDao;
import it.unisa.taskcal.data.TaskCalDatabase;

// Shared state: survives screen rotation / theme change, and is shared by all views
public class CalendarViewModel extends AndroidViewModel {

    public YearMonth currentMonth = YearMonth.now();
    public LocalDate selectedDate = LocalDate.now();
    public int currentViewId = R.id.nav_month;

    private final EventDao eventDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Event>> dayEvents = new MutableLiveData<>();

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        eventDao = TaskCalDatabase.getInstance(application).eventDao();
    }

    // Le schermate osservano questa lista e si aggiornano da sole
    public LiveData<List<Event>> getDayEvents() {
        return dayEvents;
    }

    // Legge dal database gli eventi del giorno selezionato (in background)
    public void loadEventsForSelectedDate() {
        ZoneId zone = ZoneId.systemDefault();
        long dayStart = selectedDate.atStartOfDay(zone).toInstant().toEpochMilli();
        long dayEnd = selectedDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();

        executor.execute(() ->
                dayEvents.postValue(eventDao.getEventsForDay(dayStart, dayEnd)));
    }

    // Salva un evento e poi ricarica la lista del giorno
    public void saveEvent(Event event) {
        executor.execute(() -> {
            eventDao.insert(event);
            loadEventsForSelectedDate();
        });
    }

    public void deleteEvent(Event event) {
        executor.execute(() -> {
            eventDao.delete(event);
            loadEventsForSelectedDate();
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}