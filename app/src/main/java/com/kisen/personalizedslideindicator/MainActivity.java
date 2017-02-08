package com.kisen.personalizedslideindicator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kisen.personalizedslideindicator.indicatorview.IndicatorView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IndicatorView.OnPositionChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IndicatorView indicatorView = (IndicatorView) findViewById(R.id.recycler_view);
        indicatorView.setLayoutManager(new LinearLayoutManager(this));
        indicatorView.setAdapter(new DemoAdapter());
        indicatorView.setPositionChangedListener(this);
    }

    @Override
    public void onPositionChanged(IndicatorView indicatorView, int position, View scrollBarPanel) {
        TextView title = (TextView) scrollBarPanel.findViewById(R.id.title);
        title.setText("位置 : " + position);
    }

    class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.DemoViewHolder> {

        private List<String> list = new ArrayList<>();

        DemoAdapter() {
            for (int i = 0; i < 20; i++) {
                list.add("title" + i);
            }
        }

        @Override
        public DemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
            return new DemoViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onBindViewHolder(DemoViewHolder holder, int position) {
            holder.titleView.setText(list.get(position));
        }

        class DemoViewHolder extends RecyclerView.ViewHolder {

            public TextView titleView;

            public DemoViewHolder(View itemView) {
                super(itemView);
                titleView = (TextView) itemView.findViewById(R.id.title);
            }
        }
    }
}
