package mycompany.ghostrunner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class ListRun extends AppCompatActivity implements Serializable {
    private ListView listView;
    private RunListAdapter adapter;
    private ArrayList<String> listOfRuns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_run);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.listView);

        adapter = new RunListAdapter(this/*, R.layout.row, runList*/);
        listView.setAdapter(adapter);
        if (!read()) {
            //inte bra
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final Run item = (Run) parent.getItemAtPosition(position);

                Intent intent = new Intent(view.getContext(), GhostCompete.class);
                intent.putExtra("Run", item);
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

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                final Run item = (Run) parent.getItemAtPosition(position);
                boolean deleted = true;

                AlertDialog.Builder builder = new AlertDialog.Builder(ListRun.this);

                builder.setTitle("Do you want to delete '" + item.getName() + "'?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(deleteRun(item.getName())){
                            Toast.makeText(getApplicationContext(), item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                            read();
                        }
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), item.getName()+" NOT deleted", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
                return deleted;
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
            TextView nameText = (TextView) convertView.findViewById(R.id.nameTextGhost);
            nameText.setTextSize(25);
            TextView dateText = (TextView) convertView.findViewById(R.id.dateTextGhost);
            TextView distText = (TextView) convertView.findViewById(R.id.distTextGhost);
            TextView timeText = (TextView) convertView.findViewById(R.id.timeTextGhost);

            Run run = getItem(position);
            nameText.setText(run.getName());
            dateText.setText(run.getDate().substring(0, 10));
            float distance = run.getDistance();
            int tenMeters = (int) (distance / 10) % 100;
            int km = (int) distance / 1000;
            distText.setText(String.format("%d.%02d %s", km, tenMeters, " km"));
            if (run.getHours() > 0) {
                timeText.setText(String.format("%d:%02d:%02d", (int)run.getHours(), (int)run.getMinutes(), (int)run.getSeconds()));
            } else {
                timeText.setText(String.format("%d:%02d", (int)run.getMinutes(), (int)run.getSeconds()));
            }

            return convertView;
        }
    }

    public boolean read() {
        listOfRuns = new ArrayList<>();
        ArrayList<Run> readRunList = new ArrayList<>();
        try {
            String input;
            FileInputStream fileInputStream = openFileInput("runs");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            //Reads all the names of files to read
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((input = bufferedReader.readLine()) != null) {
                listOfRuns.add(input);
            }
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!listOfRuns.isEmpty()) {
            for (String name : listOfRuns) {
                String input2 = "";
                try {
                    FileInputStream fileInputStream2 = openFileInput(name);
                    InputStreamReader inputStreamReader2 = new InputStreamReader(fileInputStream2);
                    BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader2);

                    int hour = 0;
                    int min = 0;
                    int sec = 0;
                    int distance = 0;
                    String date = "";

                    try {
                        hour = Integer.parseInt(bufferedReader2.readLine());
                        min = Integer.parseInt(bufferedReader2.readLine());
                        sec = Integer.parseInt(bufferedReader2.readLine());
                        distance = Integer.parseInt(bufferedReader2.readLine());
                        byte[] bytes = bufferedReader2.readLine().getBytes("UTF-8");
                        date = new String(bytes, "UTF-8");

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return false;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }

                    readRunList.add(new Run(name, hour, min, sec, distance, date));
                    fileInputStream2.close();
                    Collections.reverse(readRunList);
                    adapter.updateRuns(readRunList);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        adapter.updateRuns(readRunList);
        return false;
    }

    public Boolean deleteRun(String name) {
        if (listOfRuns.contains(name)) {
            listOfRuns.remove(name);

            try {
                //Skriver om filen med alla aktiva rundor med den deletade borttagen
                FileOutputStream fileOutputStream;

                fileOutputStream = openFileOutput("runs", MODE_PRIVATE);
                for (String s : listOfRuns) {
                    fileOutputStream.write(s.getBytes());
                    fileOutputStream.write("\n".getBytes());
                }

                fileOutputStream.close();

                File dir = getFilesDir();
                File file = new File(dir, name);
                boolean deleted = file.delete();
                return deleted;
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
        return false;
    }

    public void menu (View view){
        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(500);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
