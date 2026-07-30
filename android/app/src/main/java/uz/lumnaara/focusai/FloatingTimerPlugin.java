package uz.lumnaara.focusai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Suzuvchi taymer — WebView(JS) -> native ko'prik.
 *   Capacitor.Plugins.FocusTimer.hasPermission()        -> {granted:boolean}
 *   Capacitor.Plugins.FocusTimer.requestPermission()    -> "Ustidan chizish" sozlamasini ochadi
 *   Capacitor.Plugins.FocusTimer.start({text:'25:00'})  -> pillni chiqaradi
 *   Capacitor.Plugins.FocusTimer.update({text:'24:59'}) -> matnni yangilaydi (restart yo'q)
 *   Capacitor.Plugins.FocusTimer.stop()                 -> pillni yopadi
 */
@CapacitorPlugin(name = "FocusTimer")
public class FloatingTimerPlugin extends Plugin {

    private boolean canDraw() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        return Settings.canDrawOverlays(getContext());
    }

    @PluginMethod
    public void hasPermission(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("granted", canDraw());
        call.resolve(ret);
    }

    /** Sozlamalar ekranini ochadi: "Boshqa ilovalar ustida chizish" */
    @PluginMethod
    public void requestPermission(PluginCall call) {
        if (canDraw()) { call.resolve(); return; }
        try {
            Intent i = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        } catch (Exception e) { /* ba'zi OEM'larda ekran boshqacha — jim o'tamiz */ }
        call.resolve();   // foydalanuvchi qaytgach JS qayta hasPermission() bilan tekshiradi
    }

    @PluginMethod
    public void start(PluginCall call) {
        if (!canDraw()) { call.reject("NO_OVERLAY_PERMISSION"); return; }
        String text = call.getString("text", "00:00");
        Context ctx = getContext();
        Intent svc = new Intent(ctx, FloatingTimerService.class);
        svc.setAction(FloatingTimerService.ACTION_START);
        svc.putExtra(FloatingTimerService.EXTRA_TEXT, text);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(svc);
            else ctx.startService(svc);
        } catch (Exception e) { call.reject("START_FAILED"); return; }
        call.resolve();
    }

    /** Servisni qayta ishga tushirmaydi — jonli instansiyaga matn beradi */
    @PluginMethod
    public void update(PluginCall call) {
        FloatingTimerService.updateText(call.getString("text", "00:00"));
        call.resolve();
    }

    @PluginMethod
    public void stop(PluginCall call) {
        Context ctx = getContext();
        Intent svc = new Intent(ctx, FloatingTimerService.class);
        svc.setAction(FloatingTimerService.ACTION_STOP);
        try { ctx.startService(svc); } catch (Exception e) { /* ishlamayotgan bo'lsa — mayli */ }
        call.resolve();
    }
}
