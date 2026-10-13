package it.unisa.taskcal; // <-- change to your own package name

import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.YearMonth;

// Shared state: survives screen rotation / theme change, and is shared by all views
public class CalendarViewModel extends ViewModel {
    public YearMonth currentMonth = YearMonth.now();
    public LocalDate selectedDate = LocalDate.now();
    public int currentViewId = R.id.nav_month;
}
