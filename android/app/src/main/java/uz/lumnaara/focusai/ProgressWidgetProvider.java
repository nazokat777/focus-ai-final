package uz.lumnaara.focusai;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Progress widgeti: chapda bugungi foiz halqasi (Canvas bitmap),
 * o'ngda bugungi 3 ta odat — rangli pill + bajarilgan/bajarilmagan holati.
 */
public class ProgressWidgetProvider extends AppWidgetProvider {

    private static final int MAX_ROWS = 3;
    private static final int RING_PX = 168;                 /* kichik bitmap — xotira xavfsiz */
    private static final int NEON = 0xFF39FF8C;
    private static final int TRACK = 0x2239FF8C;

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        render(ctx, mgr, ids);
    }

    static void render(Context ctx, AppWidgetManager mgr, int[] ids) {
        if (ids == null || ids.length == 0) return;
        SharedPreferences sp = ctx.getSharedPreferences(FocusWidgetProvider.PREFS, Context.MODE_PRIVATE);
        int pct = Math.max(0, Math.min(100, sp.getInt("pct", 0)));
        String json = sp.getString("habits", "[]");
        String sub = sp.getString("subtitle", "Bugun");

        Bitmap ring = drawRing(pct);

        for (int id : ids) {
            RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_progress);
            v.setImageViewBitmap(R.id.wp_ring, ring);
            v.setTextViewText(R.id.wp_sub, sub);
            v.removeAllViews(R.id.wp_rows);

            try {
                JSONArray arr = new JSONArray(json);
                int n = 0;
                for (int i = 0; i < arr.length() && n < MAX_ROWS; i++) {
                    JSONObject h = arr.getJSONObject(i);
                    if (h.optInt("today", 1) != 1) continue;      /* faqat bugungi odatlar */
                    RemoteViews row = new RemoteViews(ctx.getPackageName(), R.layout.widget_progress_row);
                    int color = HabitsWidgetProvider.parseColor(h.optString("color", "#39FF8C"));
                    boolean done = h.optInt("done", 0) == 1;

                    row.setTextViewText(R.id.wpr_name, h.optString("name", ""));
                    row.setTextColor(R.id.wpr_ic, color);
                    row.setInt(R.id.wpr_state, "setTextColor", done ? color : 0x66EAF3EE);
                    row.setTextViewText(R.id.wpr_state, done ? "✓" : "○");
                    /* pill foni — odat rangining juda xira varianti */
                    row.setInt(R.id.wpr_name, "setBackgroundColor", Color.TRANSPARENT);
                    row.setInt(R.id.wpr_ic, "setBackgroundColor", Color.TRANSPARENT);
                    v.addView(R.id.wp_rows, row);
                    n++;
                }
            } catch (Exception e) { /* jim */ }

            HabitsWidgetProvider.attachOpen(ctx, v, R.id.wp_root);
            mgr.updateAppWidget(id, v);
        }
    }

    /** Bugungi foiz halqasi — Canvas'da chiziladi */
    private static Bitmap drawRing(int pct) {
        Bitmap bm = Bitmap.createBitmap(RING_PX, RING_PX, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        float stroke = RING_PX * 0.11f;
        RectF r = new RectF(stroke / 2f + 2, stroke / 2f + 2, RING_PX - stroke / 2f - 2, RING_PX - stroke / 2f - 2);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(stroke);
        p.setStrokeCap(Paint.Cap.ROUND);

        p.setColor(TRACK);
        c.drawArc(r, 0, 360, false, p);              /* yo'l */

        if (pct > 0) {
            p.setColor(NEON);
            c.drawArc(r, -90, 360f * pct / 100f, false, p);   /* progress */
        }

        Paint t = new Paint(Paint.ANTI_ALIAS_FLAG);
        t.setColor(NEON);
        t.setTextAlign(Paint.Align.CENTER);
        t.setFakeBoldText(true);
        t.setTextSize(RING_PX * 0.28f);
        Paint.FontMetrics fm = t.getFontMetrics();
        float cy = RING_PX / 2f - (fm.ascent + fm.descent) / 2f;
        c.drawText(pct + "%", RING_PX / 2f, cy, t);

        return bm;
    }
}
