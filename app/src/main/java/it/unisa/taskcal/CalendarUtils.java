package it.unisa.taskcal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarUtils {

    // Cells of a month grid: null = empty cell. Always a multiple of 7.
    public static List<LocalDate> buildDays(YearMonth month) {
        List<LocalDate> cells = new ArrayList<>();

        // Monday = 1 ... Sunday = 7. For a Sunday-first week, use: getValue() % 7
        int offset = month.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < offset; i++) {
            cells.add(null);
        }
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            cells.add(month.atDay(day));
        }
        while (cells.size() % 7 != 0) {
            cells.add(null);
        }
        return cells;
    }

    // Short weekday label ("Mon" / "Lun"), in the device language like the month names
    public static String weekdayShort(DayOfWeek d) {
        return capitalize(d.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
    }

    public static String monthName(YearMonth m) {
        return capitalize(m.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
    }

    public static String monthTitle(YearMonth m) {
        return monthName(m) + " " + m.getYear();
    }

    public static String dayTitle(LocalDate d) {
        return capitalize(d.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1);
    }
}
