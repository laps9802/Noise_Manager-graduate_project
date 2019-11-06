package org.staticdefault.noiseep;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class MyService extends Service {
    private Handler handler = new Handler();

    public MyService() {
    }

    public static String getResponseFromUrl(String urlPath) {
        String fullString = "";
        try {
            URL url = new URL(urlPath);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    fullString += line;
                }
            }catch (Exception e){

            }
            reader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullString;
    }
    private String[] lastestData = new String[1];
    private boolean noiseMyHouse = false;
    private boolean noiseUpHouse = false;

    @Override
    public void onCreate() {
        super.onCreate();

        (new Thread(new Runnable() {
            @Override
            public void run() {
                LocalData.initialize(getApplicationContext());
                while (true){
                    {
                        String data= getResponseFromUrl("http://guruem82.dothome.co.kr/All_Select.php?id=" + ((LocalData.getPreferences().getInt("ID", 0)) + 100));

                        String[] datas = data.split("<br/>");
                        lastestData = datas;

                        if(lastestData.length > 1 && (!LocalData.getPreferences().getBoolean("pushIgnore", false))){
                            String[] splitedData = lastestData[0].split("_");
                            Integer sound = Integer.valueOf(splitedData[1]);
                            Integer vibration = Integer.valueOf(splitedData[2]);

                            if((sound >= MainActivity.maxNoiseValue) || (vibration >= MainActivity.maxVibrationValue)){
                                if(!noiseUpHouse){
                                    noiseUpHouse = true;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                                            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.megaphone).setContentTitle("소음이 발생했습니다!").setContentText((LocalData.getPreferences().getInt("ID", 0) + 100) + "호에서 소음이 발생했습니다!").setContentIntent(pendingIntent).setSound(alarmSound);

                                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                            notificationManager.notify(0, builder.build());
                                        }
                                    });
                                }
                            }else{
                                noiseUpHouse = false;
                            }
                        }
                    }

                    {
                        String data= getResponseFromUrl("http://guruem82.dothome.co.kr/All_Select.php?id=" + LocalData.getPreferences().getInt("ID", 0));

                        String[] datas = data.split("<br/>");
                        lastestData = datas;

                        if(lastestData.length > 1 && (!LocalData.getPreferences().getBoolean("pushIgnore", false))){
                            String[] splitedData = lastestData[0].split("_");
                            Integer sound = Integer.valueOf(splitedData[1]);
                            Integer vibration = Integer.valueOf(splitedData[2]);

                            if((sound >= MainActivity.maxNoiseValue) || (vibration >= MainActivity.maxVibrationValue)){
                                if(!noiseMyHouse){
                                    noiseMyHouse = true;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                                            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.megaphone).setContentTitle("소음이 발생했습니다!").setContentText("내 집에서 소음이 발생했습니다!").setContentIntent(pendingIntent).setSound(alarmSound);

                                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                            notificationManager.notify(0, builder.build());
                                        }
                                    });
                                }
                            }else{
                                noiseMyHouse = false;
                            }
                        }
                    }

                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
