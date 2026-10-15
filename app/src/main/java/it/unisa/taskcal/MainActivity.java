package it.unisa.taskcal;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.time.YearMonth;

import it.unisa.taskcal.data.Event;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private MaterialToolbar toolbar;
    private LinearLayout navList;       // holds the rows of the burger menu
    private LinearLayout themeList;     // the Themes dropdown (inside navList)
    private TextView navMonth, navYear, navThemes;

    private CalendarViewModel state;
    private ThemeManager.AppTheme theme;
    private boolean themesExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        state = new ViewModelProvider(this).get(CalendarViewModel.class);
        theme = ThemeManager.getCurrent(this);

        drawer = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navList = findViewById(R.id.navList);

        // ---- apply the theme to the toolbar, burger menu and background ----
        findViewById(R.id.contentRoot).setBackgroundColor(theme.background);
        toolbar.setBackgroundColor(theme.primary);
        toolbar.setTitleTextColor(theme.onPrimary);
        toolbar.setNavigationIconTint(theme.onPrimary);
        findViewById(R.id.navPanel).setBackgroundColor(theme.surface);
        ((TextView) findViewById(R.id.navTitle)).setTextColor(theme.text);

        // ---- burger button opens the left menu ----
        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        // ---- "Today" button (right side of the toolbar) ----
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.getMenu().findItem(R.id.action_today)
                .setIconTintList(ColorStateList.valueOf(theme.onPrimary));
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_today) {
                goToToday();
                return true;
            }
            return false;
        });

        buildNavMenu();

        // Back button closes the left menu first
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (savedInstanceState == null) {
            showView(state.currentViewId);
        } else {
            updateChrome(state.currentViewId); // fragment is restored automatically
        }
    }

    // ---------------- burger menu ----------------

    private void buildNavMenu() {
        navMonth = addNavRow(navList, getString(R.string.title_month), v -> switchTo(R.id.nav_month));
        navYear = addNavRow(navList, getString(R.string.title_year), v -> switchTo(R.id.nav_year));

        navThemes = addNavRow(navList, "", v -> {
            themesExpanded = !themesExpanded;
            refreshThemeList();
        });

        themeList = new LinearLayout(this);
        themeList.setOrientation(LinearLayout.VERTICAL);
        navList.addView(themeList);

        refreshThemeList();
    }

    // Creates one menu row
    private TextView addNavRow(LinearLayout parent, String text, View.OnClickListener onClick) {
        TextView tv = (TextView) getLayoutInflater().inflate(R.layout.item_nav, parent, false);
        tv.setText(text);
        tv.setTextColor(theme.text);
        tv.setOnClickListener(onClick);
        parent.addView(tv);
        return tv;
    }

    // Opens / closes the dropdown under "Themes"
    private void refreshThemeList() {
        navThemes.setText(getString(R.string.menu_themes) + (themesExpanded ? "  \u25B4" : "  \u25BE"));
        themeList.removeAllViews();
        if (!themesExpanded) return;

        int current = ThemeManager.getCurrentIndex(this);
        for (int i = 0; i < ThemeManager.THEMES.length; i++) {
            final int index = i;
            ThemeManager.AppTheme t = ThemeManager.THEMES[i];

            String label = t.name + (i == current ? "   \u2713" : "");
            TextView row = addNavRow(themeList, label, v -> {
                ThemeManager.setCurrentIndex(this, index);
                recreate(); // rebuild the screen with the new colors
            });

            // little color dot in front of the name
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(t.primary);
            dot.setStroke(ThemeManager.dp(this, 1), 0x33000000);
            dot.setSize(ThemeManager.dp(this, 20), ThemeManager.dp(this, 20));
            row.setCompoundDrawablesWithIntrinsicBounds(dot, null, null, null);
            row.setCompoundDrawablePadding(ThemeManager.dp(this, 14));
            row.setPaddingRelative(ThemeManager.dp(this, 32), row.getPaddingTop(),
                    row.getPaddingEnd(), row.getPaddingBottom());
        }
    }

    // Highlights the active row (Month or Year) in the theme color
    private void styleNavRow(TextView tv, boolean active) {
        if (active) {
            tv.setBackground(ThemeManager.roundedRect(theme.selected, ThemeManager.dp(this, 12), 0));
            tv.setTextColor(theme.onSelected);
        } else {
            tv.setBackground(null);
            tv.setTextColor(theme.text);
        }
    }

    private void switchTo(int id) {
        state.currentViewId = id;
        showView(id);
        drawer.closeDrawer(GravityCompat.START);
    }

    // ---------------- views ----------------

    private void showView(int id) {
        Fragment f = (id == R.id.nav_year) ? new YearFragment() : new MonthFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .commit();
        updateChrome(id);
    }

    // Toolbar title + highlighted menu row
    private void updateChrome(int id) {
        toolbar.setTitle(id == R.id.nav_year ? R.string.title_year : R.string.title_month);
        styleNavRow(navMonth, id == R.id.nav_month);
        styleNavRow(navYear, id == R.id.nav_year);
    }

    // Jumps to the current day / month / year, depending on the open view
    private void goToToday() {
        state.currentMonth = YearMonth.now();
        state.selectedDate = LocalDate.now();
        showView(state.currentViewId); // reloads the open view with today's date
    }

    // Called by the Year view when you tap a mini month
    public void openMonth(YearMonth month) {
        state.currentMonth = month;
        state.currentViewId = R.id.nav_month;
        showView(R.id.nav_month);
    }
}
