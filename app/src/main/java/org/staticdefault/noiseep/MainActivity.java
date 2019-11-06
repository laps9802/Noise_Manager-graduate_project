package org.staticdefault.noiseep;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ListView graphList;
    private GraphAdapter graphAdapter;
    private String[] lastestData = new String[1];

    public static int maxNoiseValue = 60;
    public static int maxVibrationValue = 5;

    private boolean isFirst = true;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
        LocalData.initialize(this);

        CheckBox checkBox = (CheckBox) findViewById(R.id.pushIgnore);

        checkBox.setChecked(LocalData.getPreferences().getBoolean("pushIgnore", false));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                LocalData.edit(new LocalData.LocalDataRunnable() {
                    @Override
                    public void run(SharedPreferences.Editor editor) {
                        editor.putBoolean("pushIgnore", isChecked);
                    }
                });
            }
        });

        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String data= getResponseFromUrl("http://guruem82.dothome.co.kr/All_Select.php?id=" + (LocalData.getPreferences().getInt("ID", 0) + 100));

                    String[] datas = data.split("<br/>");
                    boolean isChanged = datas.length != lastestData.length;
                    lastestData = datas;

                    if(lastestData.length > 1){
                        isFirst = true;
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String[] splitedData = lastestData[0].split("_");
                                Integer sound = Integer.valueOf(splitedData[1]);
                                Integer vibration = Integer.valueOf(splitedData[2]);

                                if((sound >= maxNoiseValue) || (vibration >= maxVibrationValue)){
                                    if(!currentDetectedNoise)
                                    setDetectedNoise(true);
                                }else {
                                    if(currentDetectedNoise)
                                        setDetectedNoise(false);
                                }
                                if((sound != currentNoiseValue) || (vibration != currentVibrationValue))
                                setStatusData(sound, vibration);
                            }
                        });

                        if(isChanged){
                            if(isFirst){

                            }
                            refreshGraph();

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

        //테스트용 코드

        this.graphList = (ListView) findViewById(R.id.graphList);
        this.graphAdapter = new GraphAdapter(this);

        this.graphList.setAdapter(graphAdapter);
        refreshGraph();
    }




    private double calculateAverage(List <Integer> marks) {
        Integer sum = 0;
        if(!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }

    public Long getDayOfStart(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    public Long getDayOfEnd(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        return calendar.getTimeInMillis();
    }

    public Long getWeekOfStart(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    public Long getWeekOfEnd(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        return calendar.getTimeInMillis();
    }
    public Long getMonthOfStart(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    public Long getMonthOfEnd(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        return calendar.getTimeInMillis();
    }

    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public void refreshGraph(){
        graphAdapter.getGraphObjectList().clear();

        List<Date> realDates = new ArrayList<Date>();
        List<Integer> realValues = new ArrayList<Integer>();

        for(String realDate : lastestData){
            try {
                String[] dates = realDate.split("_");

                try {
                    realDates.add(formatter.parse(dates[0]));
                    realValues.add(Integer.valueOf(dates[1]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){

            }
        }

        final Date date = new Date(System.currentTimeMillis());

        {
            final List<String> dates = new ArrayList<String>();
            final List<Integer> values = new ArrayList<Integer>();
            List<Integer> valueIndex = new ArrayList<Integer>();

            long startDate = getDayOfStart(date);
            long endDate = getDayOfEnd(date);

            for(int i = 0; i < 24; i++){
                dates.add(i + "시");
                values.add(0);
                valueIndex.add(0);
            }
            for(int i = 0; i < realDates.size(); i++){
                long time = realDates.get(i).getTime();
                if(startDate <= time && endDate >= time){
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(realDates.get(i));

                    int realIndex = calendar.get(Calendar.HOUR_OF_DAY);
                    values.set(realIndex, values.get(realIndex) + realValues.get(i));
                    valueIndex.set(realIndex, valueIndex.get(realIndex) + 1);
                }
            }
            for(int i = 0; i < 24; i++){
                values.set(i, (int)((float)values.get(i) / (float)valueIndex.get(i)));
            }

            graphAdapter.getGraphObjectList().add(new GraphAdapter.GraphObject("일일 통계", (int)calculateAverage(values)) {
                @Override
                public void injectGraph(LineChart lineChart) {
                    ArrayList<Entry> entries = new ArrayList<Entry>();

                    for(int index = 0; index < dates.size(); index++){
                        entries.add(new Entry(index , values.get(index)));
                    }
                    Collections.sort(entries, new EntryXComparator());

                    LineDataSet lineDataSet = new LineDataSet(entries, "소음");
                    lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                    dataSets.add(lineDataSet);

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);
                    xAxis.setValueFormatter(new IAxisValueFormatter() {
                        @Override
                        public String getFormattedValue(float value, AxisBase axis) {
                            return dates.get((int)value);
                        }
                    });

                    LineData lineData = new LineData(dataSets);

                    lineChart.setData(lineData);
                    lineChart.invalidate();
                }
            });
        }
        {
            final List<String> dates = new ArrayList<String>();
            final List<Integer> values = new ArrayList<Integer>();
            List<Integer> valueIndex = new ArrayList<Integer>();

            long startDate = getWeekOfStart(date);
            long endDate = getWeekOfEnd(date);

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(date);

            int maxTime = currentCalendar.getActualMaximum(Calendar.WEEK_OF_MONTH) + 1;
            for(int i = 0; i < maxTime; i++){
                dates.add((i + 1) + "주");
                values.add(0);
                valueIndex.add(0);
            }
            for(int i = 0; i < realDates.size(); i++){
                long time = realDates.get(i).getTime();
                if(startDate <= time && endDate >= time){
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(realDates.get(i));

                    int realIndex = calendar.get(Calendar.WEEK_OF_MONTH);
                    values.set(realIndex, values.get(realIndex) + realValues.get(i));
                    valueIndex.set(realIndex, valueIndex.get(realIndex) + 1);
                }
            }
            for(int i = 0; i < maxTime; i++){
                values.set(i, (int)((float)values.get(i) / (float)valueIndex.get(i)));
            }

            graphAdapter.getGraphObjectList().add(new GraphAdapter.GraphObject("주간 통계", (int)calculateAverage(values)) {
                @Override
                public void injectGraph(LineChart lineChart) {
                    ArrayList<Entry> entries = new ArrayList<Entry>();

                    for(int index = 0; index < dates.size(); index++){
                        entries.add(new Entry(index , values.get(index)));
                    }
                    Collections.sort(entries, new EntryXComparator());

                    LineDataSet lineDataSet = new LineDataSet(entries, "소음");
                    lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                    dataSets.add(lineDataSet);

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);
                    xAxis.setValueFormatter(new IAxisValueFormatter() {
                        @Override
                        public String getFormattedValue(float value, AxisBase axis) {
                            return dates.get((int)value);
                        }
                    });

                    LineData lineData = new LineData(dataSets);

                    lineChart.setData(lineData);
                    lineChart.invalidate();
                }
            });
        }
        {
            final List<String> dates = new ArrayList<String>();
            final List<Integer> values = new ArrayList<Integer>();
            List<Integer> valueIndex = new ArrayList<Integer>();

            long startDate = getMonthOfStart(date);
            long endDate = getMonthOfEnd(date);

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(date);

            int maxTime = currentCalendar.getActualMaximum(Calendar.MONTH) + 1;
            for(int i = 0; i < maxTime; i++){
                dates.add((i + 1) + "월");
                values.add(0);
                valueIndex.add(0);
            }
            for(int i = 0; i < realDates.size(); i++){
                long time = realDates.get(i).getTime();
                if(startDate <= time && endDate >= time){
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(realDates.get(i));

                    int realIndex = calendar.get(Calendar.MONTH);
                    values.set(realIndex, values.get(realIndex) + realValues.get(i));
                    valueIndex.set(realIndex, valueIndex.get(realIndex) + 1);
                }
            }
            for(int i = 0; i < maxTime; i++){
                values.set(i, (int)((float)values.get(i) / (float)valueIndex.get(i)));
            }

            graphAdapter.getGraphObjectList().add(new GraphAdapter.GraphObject("월간 통계", (int)calculateAverage(values)) {
                @Override
                public void injectGraph(LineChart lineChart) {
                    ArrayList<Entry> entries = new ArrayList<Entry>();

                    for(int index = 0; index < dates.size(); index++){
                        entries.add(new Entry(index , values.get(index)));
                    }
                    Collections.sort(entries, new EntryXComparator());

                    LineDataSet lineDataSet = new LineDataSet(entries, "소음");
                    lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                    dataSets.add(lineDataSet);

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);
                    xAxis.setValueFormatter(new IAxisValueFormatter() {
                        @Override
                        public String getFormattedValue(float value, AxisBase axis) {
                            return dates.get((int)value);
                        }
                    });

                    LineData lineData = new LineData(dataSets);

                    lineChart.setData(lineData);
                    lineChart.invalidate();
                }
            });
        }

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphList.invalidateViews();
            }
        });
    }
    public void setImageAnimation(final ImageView imageView, final Bitmap bitmap) {
        final Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        final Animation animationIn  = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animationOut.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                imageView.setImageBitmap(bitmap);
                animationIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                animationIn.setDuration(250);
                imageView.startAnimation(animationIn);
            }
        });
        animationOut.setDuration(250);
        imageView.startAnimation(animationOut);
    }
    public void setITextAnimation(final TextView textView, final String string) {
        final Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        final Animation animationIn  = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animationOut.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                textView.setText(string);
                animationIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                animationIn.setDuration(250);
                textView.startAnimation(animationIn);
            }
        });
        animationOut.setDuration(250);
        textView.startAnimation(animationOut);
    }
    public boolean currentDetectedNoise;

    public void setDetectedNoise(boolean detectedNoise){
        final ImageView imageView = (ImageView) findViewById(R.id.statusImage);
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.statusLayout);
        final TextView textView = (TextView) findViewById(R.id.statusText);
        currentDetectedNoise = detectedNoise;

        if(detectedNoise){
            int colorFrom = ((ColorDrawable)relativeLayout.getBackground()).getColor();
            int colorTo = Color.parseColor("#b32222");

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(500);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    relativeLayout.setBackgroundColor((int) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();
            setImageAnimation(imageView, BitmapFactory.decodeResource(getResources(), R.drawable.megaphone));
            setITextAnimation(textView, "소음이 감지 되었습니다.");
        }else{
            int colorFrom = ((ColorDrawable)relativeLayout.getBackground()).getColor();
            int colorTo = Color.parseColor("#689f38");

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(500);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    relativeLayout.setBackgroundColor((int) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();
            setImageAnimation(imageView, BitmapFactory.decodeResource(getResources(), R.drawable.success));
            setITextAnimation(textView, "소음이 감지 되지 않았습니다!");
        }
    }


    private  boolean currentNoise = false;
    private  boolean currentVibration = false;
    private  int currentNoiseValue = 0;
    private  int currentVibrationValue = 0;
    public void setStatusData(int noise, int vibration){
        final ImageView noiseValueImage = (ImageView) findViewById(R.id.noiseValueImage);
        final TextView noiseValueText = (TextView) findViewById(R.id.noiseValueText);
        final ImageView vibrationValueImage = (ImageView) findViewById(R.id.vibrationValueImage);
        final TextView vibrationValueText = (TextView) findViewById(R.id.vibrationValueText);

        currentNoiseValue = noise;
        currentVibrationValue = vibration;

        setITextAnimation(noiseValueText, noise + " db");
        setITextAnimation(vibrationValueText, vibration + " cm/s");

        if(noise > maxNoiseValue){
            if(!currentNoise){
                setImageAnimation(noiseValueImage, BitmapFactory.decodeResource(getResources(), R.drawable.bad));
                currentNoise = true;
            }
        }else{
            if(currentNoise){
                setImageAnimation(noiseValueImage, BitmapFactory.decodeResource(getResources(), R.drawable.good));
                currentNoise = false;
            }
        }
        if(vibration > maxVibrationValue){
            if(!currentVibration){
                setImageAnimation(vibrationValueImage, BitmapFactory.decodeResource(getResources(), R.drawable.bad));
                currentVibration = true;
            }
        }else{
            if(currentVibration){
                setImageAnimation(vibrationValueImage, BitmapFactory.decodeResource(getResources(), R.drawable.good));
                currentVibration = false;
            }
        }
    }
}
