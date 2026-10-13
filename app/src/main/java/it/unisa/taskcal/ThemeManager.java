package it.unisa.taskcal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

public class ThemeManager {

    // A theme is built from ONE hex color. All the other colors are calculated from it.
    public static class AppTheme {
        public final String name;
        public final int primary;     // toolbar + buttons (your hex)
        public final int onPrimary;   // text/icons on the toolbar and buttons
        public final int selected;    // selected day (a bit darker than primary)
        public final int onSelected;  // text on the selected day
        public final int container;   // "today" highlight
        public final int background;  // screen background
        public final int surface;     // drawer + year cards
        public final int text;        // normal text

        AppTheme(String name, String hex) {
            this.name = name;
            int base = Color.parseColor(hex);
            primary = base;
            onPrimary = readableOn(base);
            selected = ColorUtils.blendARGB(base, Color.BLACK, 0.2f);
            onSelected = readableOn(selected);
            container = ColorUtils.blendARGB(base, Color.WHITE, 0.5f);
            background = ColorUtils.blendARGB(base, Color.WHITE, 0.85f);
            surface = ColorUtils.blendARGB(base, Color.WHITE, 0.7f);
            text = ColorUtils.blendARGB(base, Color.BLACK, 0.8f);
        }

        // White or dark text, whichever is easier to read on this color
        private static int readableOn(int bg) {
            int dark = 0xFF2B2B2B;
            return ColorUtils.calculateContrast(Color.WHITE, bg) >= ColorUtils.calculateContrast(dark, bg)
                    ? Color.WHITE : dark;
        }
    }

    // >>> Add / change themes here: new AppTheme("Name", "#HEX") <<<
    public static final AppTheme[] THEMES = {
            new AppTheme("Rose", "#D88C9A"),
            new AppTheme("Peach", "#F2D0A9"),
            new AppTheme("Cream", "#F1E3D3"),
            new AppTheme("Sage", "#99C1B9"),
            new AppTheme("Lavender", "#8E7DBE"),
    };

    private static final String PREFS = "calendar_prefs";
    private static final String KEY_THEME = "theme_index";

    public static int getCurrentIndex(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int i = p.getInt(KEY_THEME, 0);
        return (i >= 0 && i < THEMES.length) ? i : 0;
    }

    public static void setCurrentIndex(Context c, int index) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_THEME, index).apply();
    }

    public static AppTheme getCurrent(Context c) {
        return THEMES[getCurrentIndex(c)];
    }

    // ---- helpers ----

    public static int dp(Context c, float dp) {
        return Math.round(dp * c.getResources().getDisplayMetrics().density);
    }

    public static Drawable roundedRect(int color, float radiusPx, int insetPx) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        return new InsetDrawable(g, insetPx);
    }

    public static Drawable circle(int color, int insetPx) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        return new InsetDrawable(g, insetPx);
    }

    // Colors every TextView / Button inside a layout (RecyclerView items are
    // colored by DayAdapter)
    public static void applyToViews(View v, AppTheme t) {
        if (v instanceof Button) {
            Button b = (Button) v;
            b.setBackgroundTintList(ColorStateList.valueOf(t.primary));
            b.setTextColor(t.onPrimary);
        } else if (v instanceof TextView) {
            ((TextView) v).setTextColor(t.text);
        }
        if (v instanceof ViewGroup && !(v instanceof RecyclerView)) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                applyToViews(g.getChildAt(i), t);
            }
        }
    }
}
