package uz.lumnaara.focusai;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.os.Bundle;
import android.util.TypedValue;
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

    /** Foydalanuvchi widget o'lchamini o'zgartirganda chaqiriladi.
        Ilgari bu YO'Q edi — ramka kattalashardi, lekin matn/halqa qattiq
        o'lchamda qolardi va "kattalashmayapti" bo'lib ko'rinardi. */
    @Override
    public void onAppWidgetOptionsChanged(Context ctx, AppWidgetManager mgr, int id, Bundle newOptions) {
        updateAll(ctx, mgr, new int[]{ id });
    }

    /** Widgetning joriy kengligi (dp). Topilmasa 180 qaytaradi. */
    static int widthDp(AppWidgetManager mgr, int id) {
        try {
            Bundle o = mgr.getAppWidgetOptions(id);
            int w = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
            return w > 0 ? w : 180;
        } catch (Exception e) { return 180; }
    }
    static int heightDp(AppWidgetManager mgr, int id) {
        try {
            Bundle o = mgr.getAppWidgetOptions(id);
            int h = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0);
            return h > 0 ? h : 110;
        } catch (Exception e) { return 110; }
    }
    /** dp o'lchamiga qarab shkala: kichik 1.0 -> katta 1.8 gacha */
    static float scaleOf(int w, int h) {
        float sw = w / 180f, sh = h / 110f;
        float s = Math.min(sw, sh);
        if (s < 1f) s = 1f;
        if (s > 1.8f) s = 1.8f;
        return s;
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
            /* o'lchamga moslashish: widget kattalashsa matn ham o'sadi */
            float sc = scaleOf(widthDp(mgr, id), heightDp(mgr, id));
            v.setTextViewTextSize(R.id.w_streak,    TypedValue.COMPLEX_UNIT_SP, 36f * sc);
            v.setTextViewTextSize(R.id.w_today,     TypedValue.COMPLEX_UNIT_SP, 22f * sc);
            v.setTextViewTextSize(R.id.w_pct,       TypedValue.COMPLEX_UNIT_SP, 13f * sc);
            v.setTextViewTextSize(R.id.w_streak_lbl,TypedValue.COMPLEX_UNIT_SP, 11f * sc);
            v.setTextViewTextSize(R.id.w_sub,       TypedValue.COMPLEX_UNIT_SP, 11f * sc);
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
