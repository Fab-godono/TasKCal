package it.unisa.taskcal;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

// 12 mini months. Tap one to open it in the Month view.
public class YearFragment extends Fragment {

    private CalendarViewModel state;
    private ThemeManager.AppTheme theme;
    private TextView tvYearTitle;
    private GridLayout yearGrid;

    public YearFragment() {
        super(R.layout.fragment_year);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        state = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
        theme = ThemeManager.getCurrent(requireContext());

        tvYearTitle = view.findViewById(R.id.tvYearTitle);
        yearGrid = view.findViewById(R.id.yearGrid);
        Button btnPrev = view.findViewById(R.id.btnPrev);
        Button btnNext = view.findViewById(R.id.btnNext);

        ThemeManager.applyToViews(view, theme);

        btnPrev.setOnClickListener(v -> {
            state.currentMonth = state.currentMonth.minusYears(1);
            showYear();
        });
        btnNext.setOnClickListener(v -> {
            state.currentMonth = state.currentMonth.plusYears(1);
            showYear();
        });

        showYear();
    }

    private void showYear() {
        int year = state.currentMonth.getYear();
        tvYearTitle.setText(String.valueOf(year));

        yearGrid.removeAllViews();
        for (int m = 1; m <= 12; m++) {
            View card = buildMiniMonth(YearMonth.of(year, m));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            int margin = ThemeManager.dp(requireContext(), 4);
            lp.setMargins(margin, margin, margin, margin);
            yearGrid.addView(card, lp);
        }
    }

    private View buildMiniMonth(YearMonth ym) {
        Context ctx = requireContext();
        int pad = ThemeManager.dp(ctx, 6);

        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(pad, pad, pad, pad);
        card.setBackground(ThemeManager.roundedRect(theme.surface, ThemeManager.dp(ctx, 12), 0));
        card.setOnClickListener(v -> ((MainActivity) requireActivity()).openMonth(ym));

        // Month name (colored if it's the current real month)
        TextView title = new TextView(ctx);
        title.setText(CalendarUtils.monthName(ym));
        title.setTextSize(13);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(ym.equals(YearMonth.now()) ? theme.selected : theme.text);
        title.setPadding(0, 0, 0, ThemeManager.dp(ctx, 4));
        card.addView(title);

        // Tiny 7-column grid of day numbers
        GridLayout grid = new GridLayout(ctx);
        grid.setColumnCount(7);
        List<LocalDate> days = CalendarUtils.buildDays(ym);
        LocalDate today = LocalDate.now();

        for (LocalDate d : days) {
            TextView tv = new TextView(ctx);
            tv.setTextSize(9);
            tv.setGravity(Gravity.CENTER);
            tv.setIncludeFontPadding(false);
            tv.setText(d == null ? "" : String.valueOf(d.getDayOfMonth()));

            if (d != null && d.equals(today)) {
                tv.setBackground(ThemeManager.circle(theme.selected, 0));
                tv.setTextColor(theme.onSelected);
            } else {
                tv.setTextColor(theme.text);
            }

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.height = ThemeManager.dp(ctx, 15);
            grid.addView(tv, lp);
        }
        card.addView(grid, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return card;
    }
}
