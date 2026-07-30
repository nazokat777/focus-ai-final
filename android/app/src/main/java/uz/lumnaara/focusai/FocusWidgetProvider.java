package uz.lumnaara.focusai;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

/**
 * Focus AI bosh-ekran widgeti.
 * Ma'lumotni ilova (WebView) `FocusWidgetPlugin` orqali "focusai_widget"
 * SharedPreferences fayliga yozadi; bu provider o'shani o'qib ko'rsatadi.
 * Widget bosilса — ilova ochiladi.
 */
public class FocusWidgetProvider extends AppWidgetProvider {

    public static final String PREFS = "focusai_widget";

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        updateAll(ctx, mgr, ids);
    }

    /** Ilova yoki tizim chaqirganda barcha joylashtirilgan widgetlarni yangilaydi. */
    static void updateAll(Context ctx, AppWidgetManager mgr, int[] ids) {
        if (ids == null || ids.length == 0) return;
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String today = sp.getString("today", "0m");
        int pct = clamp(sp.getInt("pct", 0));
        int streak = sp.getInt("streak", 0);
        String subtitle = sp.getString("subtitle", "Focus");

        for (int id : ids) {
            String streakLbl = sp.getString("streakLabel", "kunlik seriya");
            RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.focus_widget);
            v.setTextViewText(R.id.w_today, today);
            v.setTextViewText(R.id.w_sub, subtitle);
            v.setTextViewText(R.id.w_streak, "🔥 " + streak);
            v.setTextViewText(R.id.w_streak_lbl, streakLbl);
            v.setTextViewText(R.id.w_pct, pct + "%");
            v.setProgressBar(R.id.w_prog, 100, pct, false);

            Intent open = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
            if (open != null) {
                open.setAction(Intent.ACTION_MAIN);
                open.addCategory(Intent.CATEGORY_LAUNCHER);
                PendingIntent pi = PendingIntent.getActivity(
                        ctx, 0, open,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                v.setOnClickPendingIntent(R.id.w_root, pi);
            }
            mgr.updateAppWidget(id, v);
        }
    }

    private static int clamp(int p) {
        return p < 0 ? 0 : (p > 100 ? 100 : p);
    }
}
