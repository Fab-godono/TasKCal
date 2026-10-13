package it.unisa.taskcal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

// Full-screen month
public class MonthFragment extends Fragment {

    private CalendarViewModel state;
    private ThemeManager.AppTheme theme;

    private TextView tvMonthTitle;
    private RecyclerView rvDays;
    private DayAdapter adapter;

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

        ThemeManager.applyToViews(view, theme);
        rvDays.setLayoutManager(new GridLayoutManager(requireContext(), 7));

        btnPrev.setOnClickListener(v -> {
            state.currentMonth = state.currentMonth.minusMonths(1);
            showMonth();
        });
        btnNext.setOnClickListener(v -> {
            state.currentMonth = state.currentMonth.plusMonths(1);
            showMonth();
        });

        showMonth();
    }

    private void showMonth() {
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
            adapter.setSelectedDate(state.selectedDate);
            rvDays.setAdapter(adapter);
        });
    }
}
