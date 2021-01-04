package com.example.smsreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.smsreader.adapter.MyAdapter;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private List<String> messages;
    private Button readSmsButton;
    private  String StorageString,time;
    private double amountD;
    private String StorageStringUnformatted;
    private int i =0;
    public Double[][][][] dailyExpenseArray;
    private Button nextAct ;
    ProgressBar progressBarHolder;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    List<String>formattedMessages;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MyAdapter mAdapter;
    boolean processDone= false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_SMS} , PackageManager.PERMISSION_GRANTED);

        readSmsButton = findViewById(R.id.button);
        nextAct = findViewById(R.id.next);
        progressBarHolder = (ProgressBar) findViewById(R.id.progressBarHolder);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to
        // improve performance if you know that changes
        // in content do not change the layout size
        // of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(new ArrayList<String>(), this);
        recyclerView.setAdapter(mAdapter);

        readSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyTask().execute();

            }
        });


        nextAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ExpenseActivity.class);
                //nDialog.dismiss();
                Bundle bundle = new Bundle();
              //  bundle.put
                bundle.putSerializable("array",dailyExpenseArray);
                intent.putExtras(bundle);
                if(processDone)
                startActivity(intent);
                else {
                    readSmsButton.setVisibility(View.VISIBLE);
                    nextAct.setVisibility(View.INVISIBLE);
                    Snackbar.make(v, "Please Load the Messages first!", Snackbar.LENGTH_LONG).show();

                }
            }
        });
    }


    private class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            readSmsButton.setEnabled(false);
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setVisibility(View.GONE);
            readSmsButton.setEnabled(true);
            processDone= true;
            readSmsButton.setVisibility(View.INVISIBLE);
            nextAct.setVisibility(View.VISIBLE);
            mAdapter.setValues(formattedMessages);
            mAdapter.notifyDataSetChanged();


        }

        @Override
        protected Void doInBackground(Void... params) {
           readSms();
           return null;
        }
    }





    public void readSms(){
            messages = new ArrayList<>();
        formattedMessages= new ArrayList<>();
            String[] reqCols = new String[]{Telephony.Sms.Inbox.DATE, Telephony.Sms.BODY,Telephony.Sms.Inbox.ADDRESS};
            Cursor cursor = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI, reqCols, null,null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);
            String timed;
            if (cursor.moveToFirst()) {
                do {

                    String msg = cursor.getString(1);
                     time = cursor.getString(0);
                    String address = cursor.getString(2);
                    String lowerCaseMsg=msg.toLowerCase();


                    //DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(time));
                    time =formatter.format(calendar.getTime());
                    timed = time.substring(0,10);

                    
                    boolean fakeMessages =( lowerCaseMsg.contains("cashback")||lowerCaseMsg.contains("offer")||lowerCaseMsg.contains("only")||
                            lowerCaseMsg.contains("cash"))||lowerCaseMsg.contains("redeem");

                    boolean credited = lowerCaseMsg.contains("credited") || lowerCaseMsg.contains("received") ;

                    boolean debited = lowerCaseMsg.contains("debited") || lowerCaseMsg.contains("paid")||lowerCaseMsg.contains("sent");

                    boolean pending = lowerCaseMsg.contains("pending")|| lowerCaseMsg.contains("due");
                     //boolean       |;

                    Pattern money = Pattern.compile("Rs..?\\d+.\\d+");
                    Matcher m = money.matcher(msg);

                    if(m.find() && (credited ||debited || pending)&& !fakeMessages) {
                        String amount=m.group();
                        
                        try {
                             amount = amount.replaceAll("[^\\d.]", "").trim();
                             if (amount.charAt(0) == '.')
                                amount = amount.substring(1);
                            String amountType = debited?"-"+amount:amount;
                            amountD = Double.valueOf(amountType);
                        }
                        catch (Exception e)
                        {
                            Log.i("parseing",amount+" msg : "+msg);
                            e.printStackTrace();
                        }

                        StorageString ="Amount : ₹ "+amountD+ "\n"+"Time :"+time+"\n"+"Address :"+address+"\n"+"Message :"+msg+"\tcount :"+(i++);

                        StorageStringUnformatted = amountD + ";" + timed;


                        formattedMessages.add(StorageString);
                        messages.add(StorageStringUnformatted);
                    }
                } while (cursor.moveToNext());

            }cursor.close();
            takeOutDetails();



    }

    private void takeOutDetails() {
        dailyExpenseArray = new Double[22][13][32][2];
        for (int i=0 ;i<22 ;i++ ) {
            for (int j=0;j<13 ;j++ ) {
                for (int k=0;k<32;k++ ) {

                    dailyExpenseArray[i][j][k][0]=0.0;
                    dailyExpenseArray[i][j][k][1]=0.0;
                }

            }

        }
        int type=0; // 0  for negative and 1 for positive
        for (String s: messages) {
            String[] ar= s.split(";");
            String[] time =ar[1].split("/");

            int day = Integer.parseInt(time[0]);
            int month = Integer.parseInt(time[1]);
            int year = Integer.parseInt(time[2].substring(2));
            double amount  = Double.parseDouble(ar[0]);
            type = amount<0?0:1;

            dailyExpenseArray[year][month][day][type]+=amount;


        }
    }




}
