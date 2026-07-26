package uz.lumnaara.focusai;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Widget ko'prigi — super.onCreate'dan OLDIN ro'yxatga olinishi shart (Capacitor talabi)
        registerPlugin(FocusWidgetPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
