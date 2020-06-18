package com.example.smsreader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ExpenseActivity extends AppCompatActivity {

    private TextView mDisplayDateFrom;
    private TextView mDisplayDateTo;
    private DatePickerDialog.OnDateSetListener mDateSetListener1;
    private DatePickerDialog.OnDateSetListener mDateSetListener2;
    private   String dateFrom;
    private   String dateTo;
    private Double[][][][] arr;
    private Button findButton;

    PieChart pieChart;
    PieData pieData;
    PieDataSet pieDataSet;
    ArrayList<PieEntry> pieEntries;
    public   double expenseMonth;
    public   double incomeMonth;
    public   double expenseYear;
    public   double incomeYear;
    public    List<Double>  listSend,yearNetList,yearExpenseList,yearIncomeList,monthExpenseList,monthNetList,monthIncomeList;
    private   List<Integer> months;
    private List<String> postingList;
    private String rs= "₹ ";
    private ListView monthListView;
    private    int monthsBetween,daysBetween,yearsBetween;

    private String netIncomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        Bundle bundle = getIntent().getExtras();
        mDisplayDateFrom = findViewById(R.id.tvDateFrom);
        mDisplayDateTo = findViewById(R.id.tvDateTo);
        findButton = findViewById(R.id.buttonFind);
        pieChart = findViewById(R.id.pieChart);
        monthListView = findViewById(R.id.monthList);



        arr   = (Double[][][][]) Objects.requireNonNull(bundle).getSerializable("array");

        mDisplayDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ExpenseActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener1,
                        year,month,day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                dateFrom = year + "-" + String.format("%02d", month)  + "-" +String.format("%02d", day);
                mDisplayDateFrom.setText(dateFrom);
            }
        };


        mDisplayDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ExpenseActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener2,
                        year,month,day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                dateTo = year + "-" + String.format("%02d", month)  + "-" +String.format("%02d", day);
                mDisplayDateTo.setText(dateTo);

            }
        };

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dateFrom == null ||dateTo == null)
                    Toast.makeText(ExpenseActivity.this,"select proper valid dates ",Toast.LENGTH_LONG).show();
               else {
                    calculate(arr, dateFrom, dateTo);
                    findButton.setText("Click on GRAPH!");
                    ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(ExpenseActivity.this,android.R.layout.simple_list_item_1,setList());
                    monthListView.setAdapter(arrayAdapter);
                }
            }
        });



        }
    //PieChart Ends Here
    private List<String> setList(){
        List<String> setList ;
        List<Double> setListDI= new ArrayList<>();
        List<Double> setListDD = new ArrayList<>() ;
        setListDD = monthExpenseList;
        setListDI = monthIncomeList;
        setList = setData(setListDD,setListDI);


        return setList;
    }

    private List<String> setData(List<Double> listd,List<Double> listi){
        List<String> stringList = new ArrayList<>();
        String data= "";
        int month = Integer.parseInt(dateFrom.substring(5,7));
        int i=0;
        while (i < listd.size() && listd != null){
            data ="Month : "+getMonthName(months.get(i)-2)+"\nExpense Amount : " +rs+listd.get(i) + "\nIncome Amount : "+rs+listi.get(i);
            stringList.add(data);
        i++;
        }
        return stringList;
    }

    //@org.jetbrains.annotations.Contract(pure = true)
    @RequiresApi(api = Build.VERSION_CODES.O)
    private   void calculate(Double arr[][][][], String dateFrom, String dateTo) {

        expenseMonth = incomeMonth = 0;
        monthNetList = new ArrayList<>();
        monthExpenseList = new ArrayList<>();
        monthIncomeList = new ArrayList<>();
        yearExpenseList = new ArrayList<>();
        yearIncomeList = new ArrayList<>();
        yearNetList = new ArrayList<>();
        months = new ArrayList<>();
        int year, month, day, prevMonth, prevYear, i;
        i = month =  0;

        LocalDate start = LocalDate.parse(dateFrom);
        LocalDate end = LocalDate.parse(dateTo);
        String[] date = String.valueOf(start).split("-");
        prevYear = Integer.parseInt(date[0].substring(2));
        prevMonth = Integer.parseInt(date[1]);
        Period age = Period.between(start, end);
        yearsBetween = age.getYears();
        monthsBetween = age.getMonths();
        daysBetween = age.getDays();



        while (!start.isAfter(end)) {
            //do code here
            date = String.valueOf(start).split("-");
            year = Integer.parseInt(date[0].substring(2));
            month = Integer.parseInt(date[1]);
            day = Integer.parseInt(date[2]);
            expenseMonth += arr[year][month][day][0];
            incomeMonth += arr[year][month][day][1];


            if (prevMonth != month)//month end cod here
            {
                months.add(month);
                monthExpenseList.add(expenseMonth);
                monthIncomeList.add(incomeMonth);
                monthNetList.add(incomeMonth + expenseMonth);
                expenseYear += monthExpenseList.get(i);
                incomeYear += monthIncomeList.get(i++);
                incomeMonth = 0;
                expenseMonth = 0;
                System.out.println("month :"+ String.valueOf(month));

            }

            if (prevYear != year) {
                yearIncomeList.add(incomeYear);
                yearExpenseList.add(expenseYear);
                yearNetList.add(incomeYear + expenseYear);
                incomeYear = 0;
                expenseYear = 0;
            }
            prevMonth = month;
            prevYear = year;

            start = start.plusDays(1);
        }
        monthExpenseList.add(expenseMonth);
        monthIncomeList.add(incomeMonth);
        monthNetList.add(incomeMonth + expenseMonth);
        expenseYear += monthExpenseList.get(i);
        incomeYear += monthIncomeList.get(i);
        yearIncomeList.add(incomeYear);
        yearExpenseList.add(expenseYear);
        yearNetList.add(incomeYear + expenseYear);
        incomeYear = 0;
        expenseYear = 0;
        months.add(month+1);



        listSend = monthNetList;
        graphing(pieChart);

    }




    public   void  graphing(PieChart pieChart){
        getEntries();
        pieDataSet = new PieDataSet(pieEntries, netIncomeText);
        pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        pieDataSet.setSliceSpace(0.1f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setSliceSpace(0.1f);
        pieChart.setHoleRadius(2f);
        pieChart.setTransparentCircleRadius(3);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if(e==null)
                    return;
                Toast.makeText(ExpenseActivity.this, String.valueOf( e.getY()),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

    }

    private   void getEntries() {
         float value,value2,valueSingle,value2Single;
         value=value2=0f;


        pieEntries = new ArrayList<PieEntry>();
        int i = 0;
        while (i < yearExpenseList.size()) {
             valueSingle= Float.parseFloat(String.valueOf(yearIncomeList.get(i)));
             value2Single= Float.parseFloat(String.valueOf(yearExpenseList.get(i)));
             value += valueSingle;
             value2 += value2Single;

        i++;
        }
        Log.i("values of pie :",value+" "+value2);
        value2 = Math.abs(value2);
        pieEntries.add(new PieEntry(value,"Income"));
        pieEntries.add(new PieEntry(value2,"Expense"));
        netIncomeText = " Net : "+rs+(value-value2);
    }

    public   String getMonthName(int month) {
        if(month==-1)
            month =month +1;
        return new DateFormatSymbols().getMonths()[month]; // debug month name here
    }






}
