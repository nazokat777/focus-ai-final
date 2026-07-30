package uz.lumnaara.focusai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Suzuvchi taymer: WindowManager orqali boshqa ilovalar ustida pill chizadi,
 * barmoq bilan ko'chiriladi, foreground service sifatida ilova yopiq bo'lsa ham yashaydi.
 * Layout kodda quriladi — XML shart emas.
 */
public class FloatingTimerService extends Service {

    public static final String ACTION_START = "focus.timer.START";
    public static final String ACTION_STOP  = "focus.timer.STOP";
    public static final String EXTRA_TEXT   = "text";

    private static final String CHANNEL_ID = "focus_timer_overlay";
    private static final int NOTIF_ID = 4201;

    /* jonli instansiya — plugin update() bevosita matnni yangilashi uchun */
    private static FloatingTimerService instance;
    private static final Handler UI = new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private View overlayView;
    private TextView label;
    private WindowManager.LayoutParams params;

    /* ikki-barmoq bilan o'lcham (pinch-to-resize) */
    private static final float BASE_SP = 16f;
    private float scale = 1f;
    private ScaleGestureDetector scaleDetector;
    private GradientDrawable pillBg;

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : ACTION_START;
        if (ACTION_STOP.equals(action)) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        startAsForeground();
        if (overlayView == null) addOverlay();
        String text = intent != null ? intent.getStringExtra(EXTRA_TEXT) : null;
        if (text != null && label != null) label.setText(text);
        instance = this;
        return START_STICKY;   /* tizim o'ldirsa qayta tiklaydi */
    }

    /* Foreground bildirishnoma — ilova yopiqda yashash sharti */
    private void startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Suzuvchi taymer", NotificationManager.IMPORTANCE_LOW);
            ch.setShowBadge(false);
            if (nm != null) nm.createNotificationChannel(ch);
        }
        Intent open = new Intent(this, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int pf = PendingIntent.FLAG_UPDATE_CURRENT
            | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, pf);

        Notification n = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this))
            .setContentTitle("Focus AI")
            .setContentText("Suzuvchi taymer ishlayapti")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pi)
            .setOngoing(true)
            .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {  /* Android 14+ */
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, n);
        }
    }

    /* Overlay pill + barmoq bilan ko'chirish */
    private void addOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        /* saqlangan o'lchamni tiklaymiz */
        scale = getSharedPreferences(FocusWidgetProvider.PREFS, MODE_PRIVATE).getFloat("ft_scale", 1f);

        label = new TextView(this);
        label.setText("00:00");
        label.setTextColor(Color.parseColor("#39FF8C"));      /* ilova neoni */

        pillBg = new GradientDrawable();
        pillBg.setColor(Color.parseColor("#E6030806"));       /* yarim-shaffof qora */
        pillBg.setStroke(dp(1), Color.parseColor("#3339FF8C"));
        label.setBackground(pillBg);
        overlayView = label;
        applyScale();                                          /* matn/padding/burchakni o'lchamga moslaydi */

        /* ikki barmoq bilan chimchilab kattalashtirish/kichraytirish */
        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector det) {
                scale *= det.getScaleFactor();
                if (scale < 0.7f) scale = 0.7f;
                if (scale > 2.8f) scale = 2.8f;
                applyScale();
                return true;
            }
            @Override public void onScaleEnd(ScaleGestureDetector det) {
                getSharedPreferences(FocusWidgetProvider.PREFS, MODE_PRIVATE)
                    .edit().putFloat("ft_scale", scale).apply();
            }
        });

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,    /* klaviatura fokusini o'g'irlamaydi */
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(16);
        params.y = dp(120);

        overlayView.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY;
            float touchX, touchY;
            boolean moved;
            @Override public boolean onTouch(View v, MotionEvent e) {
                scaleDetector.onTouchEvent(e);                 /* pinch'ni birinchi tekshiramiz */
                if (scaleDetector.isInProgress() || e.getPointerCount() > 1) {
                    moved = true;                              /* chimchilash = siljitish/ochish emas */
                    return true;
                }
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x; initialY = params.y;
                        touchX = e.getRawX(); touchY = e.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (e.getRawX() - touchX);
                        int dy = (int) (e.getRawY() - touchY);
                        if (Math.abs(dx) > dp(4) || Math.abs(dy) > dp(4)) moved = true;
                        params.x = initialX + dx;
                        params.y = initialY + dy;
                        try { windowManager.updateViewLayout(overlayView, params); } catch (Exception ex) {}
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) {                          /* tegish = ilovani ochish */
                            Intent open = new Intent(FloatingTimerService.this, MainActivity.class);
                            open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(open);
                        }
                        return true;
                }
                return false;
            }
        });

        try { windowManager.addView(overlayView, params); } catch (Exception e) { overlayView = null; }
    }

    /** Plugin.update() shu yerga keladi (istalgan thread'dan xavfsiz) */
    public static void updateText(final String text) {
        final FloatingTimerService s = instance;
        if (s == null || s.label == null) return;
        UI.post(new Runnable() { @Override public void run() {
            if (s.label != null) s.label.setText(text);
        }});
    }

    /* matn o'lchami + padding + burchakni joriy scale'ga moslaydi */
    private void applyScale() {
        if (label == null) return;
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASE_SP * scale);
        int pH = (int) (dp(14) * scale), pV = (int) (dp(8) * scale);
        label.setPadding(pH, pV, pH, pV);
        if (pillBg != null) pillBg.setCornerRadius(dp(24) * scale);
        if (overlayView != null && windowManager != null && params != null) {
            try { windowManager.updateViewLayout(overlayView, params); } catch (Exception e) {}
        }
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroy() {
        if (overlayView != null && windowManager != null) {
            try { windowManager.removeView(overlayView); } catch (Exception e) {}
            overlayView = null;
        }
        instance = null;
        super.onDestroy();
    }
}
