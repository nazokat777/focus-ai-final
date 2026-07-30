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

        Context ctx = getContext().getApplicationContext();
        SharedPreferences sp = ctx.getSharedPreferences(FocusWidgetProvider.PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putString("today", today)
                .putInt("pct", pct == null ? 0 : pct)
                .putInt("streak", streak == null ? 0 : streak)
                .putString("subtitle", subtitle)
                .putString("streakLabel", streakLabel)
                .apply();

        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        ComponentName cn = new ComponentName(ctx, FocusWidgetProvider.class);
        int[] ids = mgr.getAppWidgetIds(cn);
        FocusWidgetProvider.updateAll(ctx, mgr, ids);

        call.resolve();
    }
}
