package org.staticdefault.noiseep;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ultim on 2017-09-04.
 */

public class GraphAdapter extends BaseAdapter {
    public static abstract class GraphObject {
        private String title;
        private int noise;

        public GraphObject(String title, int noise) {
            this.title = title;
            this.noise = noise;
        }

        abstract public void injectGraph(LineChart lineChart);

        public int getNoise() {
            return noise;
        }

        public void setNoise(int noise) {
            this.noise = noise;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
    private Context context;
    private LayoutInflater inflater = null;

    private List<GraphObject> graphObjectList;
    public GraphAdapter(Context context){
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.graphObjectList = new ArrayList<GraphObject>();
    }

    public List<GraphObject> getGraphObjectList() {
        return graphObjectList;
    }

    @Override
    public int getCount() {
        return graphObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return graphObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.item_graph, null);

        TextView textTitle = (TextView) view.findViewById(R.id.textTitle);
        TextView textNoise = (TextView) view.findViewById(R.id.textNoise);
        LineChart chartGraph = (LineChart) view.findViewById(R.id.chartGraph);

        GraphObject graphObject = graphObjectList.get(position);
        textTitle.setText(graphObject.getTitle());
        textNoise.setText("평균 소음 : " + graphObject.getNoise() + " db");
        graphObject.injectGraph(chartGraph);

        chartGraph.getAxisLeft().setDrawGridLines(false);
        chartGraph.getAxisLeft().setDrawAxisLine(false);
        chartGraph.setBackgroundColor(Color.parseColor("#ffffff"));
        chartGraph.setDescription(null);
        chartGraph.getAxisLeft().setDrawLabels(false);
        chartGraph.getAxisRight().setDrawLabels(false);
        chartGraph.getLegend().setEnabled(false);

        chartGraph.getLineData().getDataSets().get(0).setDrawFilled(true);
        return view;
    }
}
