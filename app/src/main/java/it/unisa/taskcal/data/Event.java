package it.unisa.taskcal.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class Event {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public int color;

    // Date e ore salvate come millisecondi (UTC) dal 1970
    public long startMillis;
    public long endMillis;
    public boolean allDay;

    // Promemoria: minuti prima dell'evento (-1 = nessun promemoria)
    public int reminderMinutes;

    // Ripetizione: "NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    public String repeatRule;

    public String calendarName;
    public String location;
    public String description;

    // Conto alla rovescia attivo sì/no
    public boolean countdown;
}