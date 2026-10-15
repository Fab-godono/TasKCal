package it.unisa.taskcal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

import android.graphics.drawable.GradientDrawable;
import java.util.HashSet;
import java.util.Set;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(LocalDate date);
    }

    private final List<LocalDate> days;          // null entries = empty cells
    private final int rowHeight;                 // pixels
    private final ThemeManager.AppTheme theme;
    private final OnDayClickListener listener;
    private LocalDate selectedDate;
    private Set<LocalDate> eventDays = new HashSet<>();
    public void setEventDays(Set<LocalDate> eventDays) {
        this.eventDays = eventDays;
        notifyDataSetChanged();
    }

    public DayAdapter(List<LocalDate> days, int rowHeight,
                      ThemeManager.AppTheme theme, OnDayClickListener listener) {
        this.days = days;
        this.rowHeight = rowHeight;
        this.theme = theme;
        this.listener = listener;
    }

    // Redraws only the two cells that change (old and new selection)
    public void setSelectedDate(LocalDate date) {
        LocalDate old = this.selectedDate;
        this.selectedDate = date;
        if (old == null) {
            notifyDataSetChanged();
            return;
        }
        int oldPos = days.indexOf(old);
        int newPos = days.indexOf(date);
        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (newPos >= 0 && newPos != oldPos) notifyItemChanged(newPos);
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day, parent, false);
        view.getLayoutParams().height = rowHeight;
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        LocalDate date = days.get(position);
        TextView tv = holder.tvDay;
        Context ctx = tv.getContext();

        if (date == null) {
            tv.setCompoundDrawables(null, null, null, null);
            tv.setText("");
            tv.setBackground(null);
            tv.setOnClickListener(null);
            tv.setClickable(false);
            return;
        }

        tv.setText(String.valueOf(date.getDayOfMonth()));
        tv.setOnClickListener(v -> listener.onDayClick(date));

        boolean selected = date.equals(selectedDate);
        boolean today = date.equals(LocalDate.now());

        if (selected || today) {
            int color = selected ? theme.selected : theme.container;
            tv.setBackground(ThemeManager.roundedRect(
                    color, ThemeManager.dp(ctx, 10), ThemeManager.dp(ctx, 2)));
            tv.setTextColor(selected ? theme.onSelected : theme.text);
        } else {
            tv.setBackground(null);
            tv.setTextColor(theme.text);
        }
        // puntino sotto il numero se il giorno ha eventi
        if (eventDays.contains(date)) {
            int size = ThemeManager.dp(ctx, 6);
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(selected ? theme.onSelected : theme.primary);
            dot.setBounds(0, 0, size, size);
            tv.setCompoundDrawables(null, null, null, dot);
            tv.setCompoundDrawablePadding(ThemeManager.dp(ctx, 3));
        } else {
            tv.setCompoundDrawables(null, null, null, null);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDay;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
        }
    }
}
