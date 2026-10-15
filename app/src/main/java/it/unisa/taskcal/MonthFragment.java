package it.unisa.taskcal;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import java.util.Set;

// Full-screen month
public class MonthFragment extends Fragment {

    private CalendarViewModel state;
    private ThemeManager.AppTheme theme;

    private TextView tvMonthTitle;
    private RecyclerView rvDays;
    private DayAdapter adapter;
    private Set<LocalDate> eventDays = new java.util.HashSet<>();

    public MonthFragment() {
        super(R.layout.fragment_month);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        state = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
        theme = ThemeManager.getCurrent(requireContext());

        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        rvDays = view.findViewById(R.id.rvDays);
        Button btnPrev = view.findViewById(R.id.btnPrev);
        Button btnNext = view.findViewById(R.id.btnNext);

        // Weekday labels, Monday first (same order used by CalendarUtils.buildDays)
        LinearLayout weekdayHeader = view.findViewById(R.id.weekdayHeader);
        for (int i = 0; i < 7; i++) {
            TextView label = new TextView(requireContext());
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            label.setGravity(Gravity.CENTER);
            label.setText(CalendarUtils.weekdayShort(DayOfWeek.MONDAY.plus(i)));
            weekdayHeader.addView(label);
        }

        ThemeManager.applyToViews(view, theme);
        rvDays.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        rvDays.setItemAnimator(null); // niente sfumatura quando cambia la selezione

        btnPrev.setOnClickListener(v -> {
            state.currentMonth = state.currentMonth.minusMonths(1);
            showMonth();
        });
        btnNext.setOnClickListener(v -> {
            state.currentMonth = state.currentMonth.plusMonths(1);
            showMonth();
        });

        state.getDaysWithEvents().observe(getViewLifecycleOwner(), days -> {
            eventDays = days;
            if (adapter != null) adapter.setEventDays(days);
        });

        showMonth();
    }

    private void showMonth() {
        state.loadEventsForMonth(state.currentMonth);
        tvMonthTitle.setText(CalendarUtils.monthTitle(state.currentMonth));
        List<LocalDate> days = CalendarUtils.buildDays(state.currentMonth);

        // post() waits until the RecyclerView has a height
        rvDays.post(() -> {
            if (!isAdded()) return;
            int rows = days.size() / 7;
            int rowHeight = rvDays.getHeight() / rows;

            adapter = new DayAdapter(days, rowHeight, theme, date -> {
                state.selectedDate = date;
                adapter.setSelectedDate(date);
            });
            adapter.setEventDays(eventDays);
            adapter.setSelectedDate(state.selectedDate);
            rvDays.setAdapter(adapter);
        });
    }

    // Quando torni da "Nuovo evento", ricarica i puntini
    @Override
    public void onResume() {
        super.onResume();
        if (state != null) state.loadEventsForMonth(state.currentMonth);
    }
}
