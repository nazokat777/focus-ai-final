package uz.lumnaara.focusai;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * WebView -> native ko'prik. Ilova (JS) chaqiradi:
 *   Capacitor.Plugins.FocusWidget.update({today:'1h 20m', pct:42, streak:5, subtitle:'Bugungi fokus'})
 * Qiymatlarni SharedPreferences'ga yozadi va widgetni darhol yangilaydi.
 */
@CapacitorPlugin(name = "FocusWidget")
public class FocusWidgetPlugin extends Plugin {

    @PluginMethod
    public void update(PluginCall call) {
        String today = call.getString("today", "0m");
        Integer pct = call.getInt("pct", 0);
        Integer streak = call.getInt("streak", 0);
        String subtitle = call.getString("subtitle", "Focus");
        String streakLabel = call.getString("streakLabel", "kunlik seriya");
        /* boy ma'lumot: odatlar ro'yxati (JSON) + hafta kunlari harflari */
        String habits = call.getString("habits", "[]");
        String dayLetters = call.getString("dayLetters", "D,S,C,P,J,S,Y");

        Context ctx = getContext().getApplicationContext();
        SharedPreferences sp = ctx.getSharedPreferences(FocusWidgetProvider.PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putString("today", today)
                .putInt("pct", pct == null ? 0 : pct)
                .putInt("streak", streak == null ? 0 : streak)
                .putString("subtitle", subtitle)
                .putString("streakLabel", streakLabel)
                .putString("habits", habits)
                .putString("dayLetters", dayLetters)
                .apply();

        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        /* uchala widget turini ham yangilaymiz */
        FocusWidgetProvider.updateAll(ctx, mgr,
            mgr.getAppWidgetIds(new ComponentName(ctx, FocusWidgetProvider.class)));
        HabitsWidgetProvider.render(ctx, mgr,
            mgr.getAppWidgetIds(new ComponentName(ctx, HabitsWidgetProvider.class)));
        ProgressWidgetProvider.render(ctx, mgr,
            mgr.getAppWidgetIds(new ComponentName(ctx, ProgressWidgetProvider.class)));

        call.resolve();
    }
}
