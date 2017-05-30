package com.delaroystudios.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static android.R.attr.name;
import static com.delaroystudios.camera.R.id.nextBtn;

public class ReportActivity extends ActionBarActivity {



    private static String TAG = "ReportActivity";
    private Button nextBtn;
    private Button backBtn;
    private float[] A = {25.06f, 60.24f, 89.23f, 56.23f, 100.92f};
    private float[] yData = {25.06f, 60.24f, 89.23f, 56.23f, 100.92f};
    private String[] xData = {"Food", "Rent", "Bills", "Entertainment", "Misc."};
    PieChart pieChart;
    private TextView monthText;
    private String monthString;
    Calendar c = Calendar.getInstance();
    int month = c.get(Calendar.MONTH) + 1;
    int t = c.get(Calendar.MONTH) + 1;
    private ArrayList<String[]> localStore;
    private Context context;
    private ArrayList<ArrayList<String[]>> storeMonth = new ArrayList<>();
    private StorageReference saveRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private static DecimalFormat df2 = new DecimalFormat(".##");
    private boolean loaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = storage.getReferenceFromUrl("gs://accordian-c7137.appspot.com");
        saveRef = mStorageRef.child(mAuth.getCurrentUser().getUid()+"/save.txt");

        downloadFirebaseStore();


          /*ArrayList<String[]> january = monthStore.get(0)
          for(line x : january){
          x[0] is downloadlink
          x[1] is business name
          x[2] is price (dollar signs stripped)
          x[3] is category
          }*/


        nextBtn = (Button) findViewById(R.id.nextBtn);
        backBtn = (Button) findViewById(R.id.backBtn);

        monthText = (TextView) findViewById(R.id.textMonth);
        monthText.setText(setMonth(t));

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t = t + 1;
                if(t > 12){
                    t = 1;
                }

                if(t < 1){
                    t = 12;
                }
                updateViews();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t = t - 1;
                if(t > 12){
                    t = 1;
                }
                if(t < 1){
                    t = 12;
                }
                updateViews();
            }
        });

        Log.d(TAG, "onCreate: creating the chart");
        pieChart = (PieChart) findViewById(R.id.idPieChart);


        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Monthly Expenses");
        pieChart.setCenterTextSize(13);


        addDataSet();
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValueSelected: Value select from chart");
                Log.d(TAG, "onValueSelected: " + e.toString());
                Log.d(TAG, "onValueSelected: " + h.toString());

                int pos1 = e.toString().indexOf("y: ");
                String expenses = e.toString().substring(pos1 + 2);
                Log.d(TAG, "String Pos: " + expenses);

               for (int i = 0; i < yData.length; i++) {
                    if (yData[i] == Float.parseFloat(expenses)) {
                        pos1 = i;
                        break;
                    }
                }
                String catagory = xData[pos1];
                Log.d(TAG, "String Pos: " + catagory);
                float A = Float.parseFloat(expenses);
                String AA = String.format("%.2f", A);

                Toast.makeText(ReportActivity.this, " " + catagory + ": $" + AA, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

    }
    private void replaceLocalStore(byte[] bytes) {
        ArrayList<String[]> temp = new ArrayList<>();
        File file = new File(getApplicationContext().getFilesDir(), "localStore.txt");
        file.delete();
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream (new File(file.getAbsolutePath().toString()), true);

            outputStream.write(bytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                temp.add(line.split(",###,"));
            }
            br.close();

        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        localStore = temp;

        for(int i=0;i<12;i++) {
            storeMonth.add(new ArrayList<String[]>());
        }
        Random r = new Random();
        for(String[] line : localStore) {
            int i = r.nextInt(12);
            storeMonth.get(i).add(line);
        }
        loaded=true;
    }
    private void downloadFirebaseStore() {

        final long ONE_MEGABYTE = 1024 * 1024;
        saveRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                replaceLocalStore(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("ASDASD","fail");
            }
        });
    /*    saveRef.getFile(Uri.fromFile(file)).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                initLocalStore(pass);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });*/



    }
    private void addDataSet(){
        Log.d(TAG, "addDataSet started");
        if(loaded){
            yData = genMonthArr(t-1);
        }else {
            yData = getData(t);// this is where to use the genMonthArr function}
        }
       // Log.d(TAG, yData.toString());
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntry= new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , xData[i]));
        }

        for(int i = 0; i < xData.length; i++){
            xEntry.add(xData[i]);
        }

        PieDataSet pieDataSet = new PieDataSet( yEntrys, "Expenses");
        pieDataSet.setSliceSpace(4);
        pieDataSet.setValueTextSize(20);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);

        pieDataSet.setColors(colors);

        //Legend legend = pieChart.getLegend();
        //legend.setForm(Legend.LegendForm.CIRCLE);
        //legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

    }

    public String setMonth(int a){
        switch (a) {
            case 1:  monthString = "   January   ";
                break;
            case 2:  monthString = "   February   ";
                break;
            case 3:  monthString = "    March     ";
                break;
            case 4:  monthString = "     April    ";
                break;
            case 5:  monthString = "      May     ";
                break;
            case 6:  monthString = "     June     ";
                break;
            case 7:  monthString = "     July     ";
                break;
            case 8:  monthString = "    August    ";
                break;
            case 9:  monthString = "   September";
                break;
            case 10: monthString = "   October     ";
                break;
            case 11: monthString = "   November ";
                break;
            case 12: monthString = "  December";
                break;
            default: monthString = "Invalid month";
                break;
        }
        return monthString;
    }

    public static float randomFill(){
        float minX = 1.0f;
        float maxX = 4000.0f;

        Random rand = new Random();
        float randomNum = rand.nextFloat() * (maxX - minX) + minX;
        return randomNum;
    }

    public float[] getData(int a) {
            A = new float[5];
            for (int i = 0; i < A.length; i++) {
                A[i] = randomFill();
            }
            return A;
        }

    private void updateViews() {
        monthText = (TextView) findViewById(R.id.textMonth);
        monthText.setText(setMonth(t));
        addDataSet();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent i = new Intent(ReportActivity.this, LoginActivity.class);
            startActivity(i);
            return true;
        }

        else if (id == R.id.action_home) {
            //mAuth.signOut();
            Intent i = new Intent(ReportActivity.this, PickActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private float[] genMonthArr(int month) {
        float[] yData = {100.0f, 100.0f, 100.0f, 100.0f, 100.0f};
        //String[] xData = {"Food", "Rent", "Bills", "Entertainment", "Misc."};

        for(String[] x : storeMonth.get(month)) {
            int t;
            if(x.length>=4){
            if(x[3].equalsIgnoreCase("Food")){
                t=0;
            } else if(x[3].equalsIgnoreCase("Rent")){
                t=1;
            } else if(x[3].equalsIgnoreCase("Bills")){
                t=2;
            } else if(x[3].equalsIgnoreCase("Entertainment")){
                t=3;
            } else if(x[3].equalsIgnoreCase("Misc")){
                t=4;
            } else {
                t=4;
            }
            try {
                float f = Float.parseFloat(x[2]);
                yData[t] += f;
            } catch(Exception e){
                Log.d("Report","invalid cost");
            }}
            else{
                yData[4] += 5;
            }
        }
        return yData;
    }
}
