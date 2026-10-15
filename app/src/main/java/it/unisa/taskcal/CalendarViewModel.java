package it.unisa.taskcal;

import android.app.Application;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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
    private final MutableLiveData<Set<LocalDate>> daysWithEvents = new MutableLiveData<>();

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

    // I giorni del mese che hanno almeno un evento (per i puntini nel calendario)
    public LiveData<Set<LocalDate>> getDaysWithEvents() {
        return daysWithEvents;
    }

    public void loadEventsForMonth(YearMonth month) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate first = month.atDay(1);
        LocalDate last = month.atEndOfMonth();
        long from = first.atStartOfDay(zone).toInstant().toEpochMilli();
        long to = last.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();

        executor.execute(() -> {
            Set<LocalDate> result = new HashSet<>();
            for (Event e : eventDao.getEventsForDay(from, to)) {
                LocalDate s = Instant.ofEpochMilli(e.startMillis).atZone(zone).toLocalDate();
                LocalDate en = Instant.ofEpochMilli(e.endMillis).atZone(zone).toLocalDate();
                if (s.isBefore(first)) s = first;
                if (en.isAfter(last)) en = last;
                // un evento su più giorni segna tutti i giorni coperti
                for (LocalDate d = s; !d.isAfter(en); d = d.plusDays(1)) {
                    result.add(d);
                }
            }
            daysWithEvents.postValue(result);
        });
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