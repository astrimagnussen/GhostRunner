package mycompany.ghostrunner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class ListRun extends AppCompatActivity implements Serializable {
    private ListView listView;
    private RunListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_run);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("List");

        listView = (ListView) findViewById(R.id.listView);

        adapter = new RunListAdapter(this/*, R.layout.row, runList*/);
        listView.setAdapter(adapter);
        if(!read()) {
            //inte bra
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final Run item = (Run) parent.getItemAtPosition(position);

                Intent intent = new Intent(view.getContext(), GhostCompete.class);
                intent.putExtra("Run" , item);
                startActivity(intent);

                /*view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                runList.remove(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });*/
            }
        });
    }

    private class RunListAdapter extends BaseAdapter {
        private ArrayList<Run> runList;

        private final Context context;

        public RunListAdapter(Context context/*, int textViewResourceId,
                              ArrayList<Run> runList*/) {
            this.context = context;
            runList = new ArrayList<>();
        }

        public void updateRuns(ArrayList<Run> runs) {
            ThreadPreconditions.checkOnMainThread();
            runList = runs;
            notifyDataSetChanged();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getCount() {
            return runList.size();
        }

        // getItem(int) in Adapter returns Object but we can override
        // it to Run thanks to Java return type covariance
        @Override
        public Run getItem(int position) {
            return runList.get(position);
        }

        // getItemId() is often useless, I think this should be the default
        // implementation in BaseAdapter
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
            }

            TextView dateText = (TextView) convertView.findViewById(R.id.dateTextGhost);
            TextView distText = (TextView) convertView.findViewById(R.id.distTextGhost);
            TextView timeText = (TextView) convertView.findViewById(R.id.timeTextGhost);

            Run run = getItem(position);
            dateText.setText(run.getDate().substring(0, 10));
            float distance = run.getDistance();
            int tenMeters = (int) (distance/10)%100;
            int km = (int) distance/1000;
            distText.setText(String.format("%d.%02d %s", km, tenMeters, " km"));
            if (run.getHours() > 0){
                timeText.setText(String.format("%d:%02d:%02d", run.getHours(), run.getMinutes(), run.getSeconds()));
            }
            else {
                timeText.setText(String.format("%d:%02d", run.getMinutes(), run.getSeconds()));
            }

            return convertView;
        }
    }

    public boolean read() {
        try {
            String input;
            FileInputStream fileInputStream = openFileInput("runs");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            int counter = 0;
            int hour = 0;
            int min = 0;
            int sec = 0;
            int distance = 0;
            String date = "";
            ArrayList<Run> readRunList = new ArrayList<>();

            while ((input = bufferedReader.readLine()) != null) {
                switch (counter % 5) {
                    case 0:
                        try {
                            hour = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return false;
                        }
                        break;
                    case 1:
                        try {
                            min = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return false;
                        }
                        break;
                    case 2:
                        try {
                            sec = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return false;
                        }
                        break;
                    case 3:
                        try {
                            distance = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return false;
                        }
                        break;
                    case 4:
                        byte[] bytes = input.getBytes("UTF-8");
                        date = new String(bytes, "UTF-8");
                        break;
                }
                counter++;
                if (counter % 5 == 0 && counter != 0) {
                    /*System.out.println("hour = " + hour);
                    System.out.println("min = " + min);
                    System.out.println("sec = " + sec);
                    System.out.println("distance = " + distance);
                    System.out.println("date = " + date);*/
                    readRunList.add(new Run(hour, min, sec, distance, date));
                }
            }
            Collections.reverse(readRunList);
            adapter.updateRuns(readRunList);
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
