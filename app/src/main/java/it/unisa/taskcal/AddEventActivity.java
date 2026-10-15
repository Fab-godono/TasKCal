package it.unisa.taskcal;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

import it.unisa.taskcal.data.Event;
import it.unisa.taskcal.data.TaskCalDatabase;

public class AddEventActivity extends AppCompatActivity {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE d MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private TextInputEditText etTitle, etLocation, etDescription;
    private MaterialSwitch switchAllDay;
    private Button btnStartDate, btnStartTime, btnEndDate, btnEndTime;

    private LocalDateTime start;
    private LocalDateTime end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        setTitle("Nuovo evento");

        etTitle = findViewById(R.id.etTitle);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        switchAllDay = findViewById(R.id.switchAllDay);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnEndTime = findViewById(R.id.btnEndTime);

        // Data di partenza: il giorno aperto nel calendario (passato da MainActivity),
        // altrimenti oggi. Ora di inizio: prossima ora piena.
        LocalDate day = LocalDate.now();
        String dayExtra = getIntent().getStringExtra("date");
        if (dayExtra != null) day = LocalDate.parse(dayExtra);

        start = LocalDateTime.of(day, LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1));
        end = start.plusHours(1);

        btnStartDate.setOnClickListener(v -> pickDate(true));
        btnEndDate.setOnClickListener(v -> pickDate(false));
        btnStartTime.setOnClickListener(v -> pickTime(true));
        btnEndTime.setOnClickListener(v -> pickTime(false));

        switchAllDay.setOnCheckedChangeListener((b, checked) -> refreshButtons());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());

        refreshButtons();
    }

    private void refreshButtons() {
        btnStartDate.setText(start.format(DATE_FMT));
        btnEndDate.setText(end.format(DATE_FMT));
        btnStartTime.setText(start.format(TIME_FMT));
        btnEndTime.setText(end.format(TIME_FMT));

        int timeVisibility = switchAllDay.isChecked() ? View.GONE : View.VISIBLE;
        btnStartTime.setVisibility(timeVisibility);
        btnEndTime.setVisibility(timeVisibility);
    }

    private void pickDate(boolean isStart) {
        LocalDateTime base = isStart ? start : end;
        new DatePickerDialog(this, (view, y, m, d) -> {
            // i mesi del DatePicker partono da 0, LocalDate da 1
            LocalDateTime chosen = LocalDateTime.of(LocalDate.of(y, m + 1, d), base.toLocalTime());
            if (isStart) {
                long duration = java.time.Duration.between(start, end).toMinutes();
                start = chosen;
                end = start.plusMinutes(duration); // la fine si sposta insieme all'inizio
            } else {
                end = chosen;
            }
            refreshButtons();
        }, base.getYear(), base.getMonthValue() - 1, base.getDayOfMonth()).show();
    }

    private void pickTime(boolean isStart) {
        LocalDateTime base = isStart ? start : end;
        new TimePickerDialog(this, (view, h, min) -> {
            LocalDateTime chosen = LocalDateTime.of(base.toLocalDate(), LocalTime.of(h, min));
            if (isStart) {
                long duration = java.time.Duration.between(start, end).toMinutes();
                start = chosen;
                end = start.plusMinutes(duration);
            } else {
                end = chosen;
            }
            refreshButtons();
        }, base.getHour(), base.getMinute(), true).show();
    }

    private void save() {
        String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Inserisci un titolo");
            return;
        }

        boolean allDay = switchAllDay.isChecked();
        ZoneId zone = ZoneId.systemDefault();

        LocalDateTime s = allDay ? start.toLocalDate().atStartOfDay() : start;
        LocalDateTime e = allDay ? end.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1_000_000) : end;

        if (e.isBefore(s)) {
            Toast.makeText(this, "La fine non può essere prima dell'inizio", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        event.title = title;
        event.startMillis = s.atZone(zone).toInstant().toEpochMilli();
        event.endMillis = e.atZone(zone).toInstant().toEpochMilli();
        event.allDay = allDay;
        event.reminderMinutes = -1;
        event.repeatRule = "NONE";
        event.calendarName = "Personale";
        event.location = etLocation.getText() == null ? "" : etLocation.getText().toString().trim();
        event.description = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();

        // Salvataggio in background, poi si chiude la schermata
        Executors.newSingleThreadExecutor().execute(() -> {
            TaskCalDatabase.getInstance(getApplicationContext()).eventDao().insert(event);
            runOnUiThread(this::finish);
        });
    }
}