package uz.lumnaara.focusai;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Odatlar widgeti: chapda odat nomlari, o'ngda oxirgi 7 kunlik nuqta jadvali.
 * Ma'lumot ilova (WebView) tomonidan FocusWidgetPlugin orqali yoziladi.
 */
public class HabitsWidgetProvider extends AppWidgetProvider {

    private static final int MAX_ROWS = 8;
    private static final int DOT_EMPTY = 0x1F39FF8C;   /* juda xira neon — bajarilmagan kun */

    private static final int[] DOT_IDS = {
        R.id.wr_0, R.id.wr_1, R.id.wr_2, R.id.wr_3, R.id.wr_4, R.id.wr_5, R.id.wr_6
    };
    private static final int[] DAY_IDS = {
        R.id.wh_d0, R.id.wh_d1, R.id.wh_d2, R.id.wh_d3, R.id.wh_d4, R.id.wh_d5, R.id.wh_d6
    };

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        render(ctx, mgr, ids);
    }

    static void render(Context ctx, AppWidgetManager mgr, int[] ids) {
        if (ids == null || ids.length == 0) return;
        SharedPreferences sp = ctx.getSharedPreferences(FocusWidgetProvider.PREFS, Context.MODE_PRIVATE);
        String json = sp.getString("habits", "[]");
        String dayLetters = sp.getString("dayLetters", "D,S,C,P,J,S,Y");

        for (int id : ids) {
            RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_habits);
            v.removeAllViews(R.id.wh_rows);

            /* hafta kunlari harflari (til bo'yicha) */
            String[] L = dayLetters.split(",");
            for (int d = 0; d < 7 && d < L.length; d++) v.setTextViewText(DAY_IDS[d], L[d]);

            int count = 0;
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length() && count < MAX_ROWS; i++) {
                    JSONObject h = arr.getJSONObject(i);
                    RemoteViews row = new RemoteViews(ctx.getPackageName(), R.layout.widget_habits_row);
                    int color = parseColor(h.optString("color", "#39FF8C"));

                    row.setTextViewText(R.id.wr_name, h.optString("name", ""));
                    row.setTextViewText(R.id.wr_ic, "●");        /* ● odat rangida */
                    row.setTextColor(R.id.wr_ic, color);

                    JSONArray days = h.optJSONArray("days");
                    for (int d = 0; d < 7; d++) {
                        boolean done = days != null && d < days.length() && days.optInt(d, 0) == 1;
                        row.setInt(DOT_IDS[d], "setColorFilter", done ? color : DOT_EMPTY);
                    }
                    v.addView(R.id.wh_rows, row);
                    count++;
                }
            } catch (Exception e) { /* buzuq JSON — bo'sh holat ko'rsatamiz */ }

            v.setViewVisibility(R.id.wh_empty, count == 0 ? View.VISIBLE : View.GONE);
            attachOpen(ctx, v, R.id.wh_root);
            mgr.updateAppWidget(id, v);
        }
    }

    static int parseColor(String c) {
        try { return Color.parseColor(c); } catch (Exception e) { return Color.parseColor("#39FF8C"); }
    }

    /** Widget bosilsa — ilovani ochadi */
    static void attachOpen(Context ctx, RemoteViews v, int viewId) {
        Intent open = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (open == null) return;
        open.setAction(Intent.ACTION_MAIN);
        open.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, open,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(viewId, pi);
    }
}
