package xdu.edu.cn.airbreath;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import xdu.edu.cn.airbreath.entity.Constans;



public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener, View.OnClickListener {
    private Handler myhandler;
    private Socket socket;
    private String str = "";
    boolean running = false;
    private HorizontalBarChart barchart;
    private ReceiveThread rt;
    private StartThread st;
    private LinearLayout mLl_Refresh;
    private ImageView iv_fresh;
    private TextView mBtnSend;
    private TextView mTvCity;
    private TextView mTvUpdateTime;
    private TextView mTvWeek;
    private TextView mTvTemp;
    private TextView mTvWet;
    private PieChart mChart2_5;
    private PieChart mChartvoc;
    private ProgressDialog dialog;
    private Animation anim;
    public LocationClient mLocationClient = null;
    private BarData data;
    private Spinner spinner;
    private List<String> list = new ArrayList<String>();
    private TextView co;
    private TextView o3;
    public BDLocationListener myListener = new MyLocationListener();
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();
    }

    private void initView() {
        myhandler = new MyHandler();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        initLocation();
        st = new StartThread();
        //initDialog();
        mLocationClient.start();
        //st.start();

        mLl_Refresh = (LinearLayout) findViewById(R.id.ll_Refresh);
        iv_fresh = (ImageView) findViewById(R.id.iv_fresh);
        mBtnSend = (TextView) findViewById(R.id.btnSend);
        mTvCity = (TextView) findViewById(R.id.tvCity);
        mTvUpdateTime = (TextView) findViewById(R.id.tvUpdateTime);
        mTvWeek = (TextView) findViewById(R.id.tvWeek);
        mLl_Refresh.setOnClickListener(this);
        co = (TextView) findViewById(R.id.tv_CO);
        o3 = (TextView) findViewById(R.id.tv_O3);
        spinner = (Spinner) findViewById(R.id.spinner);
        list.add("低速");list.add("中速");list.add("快速");
        ArrayAdapter<String>  adapter = new ArrayAdapter<String>(this,R.layout.spinner_item, list);
        adapter.setDropDownViewResource(R.layout.spinner_dropdowm_item);
        spinner.setAdapter(adapter);


        initChart();
        init2_5Chart();
        initVocChart();
        }



    private SpannableString generateCenterSpannableText(String value,String name) {
        SpannableString s = new SpannableString(value+"\n"+name);
        s.setSpan(new RelativeSizeSpan(2.2f), 0, value.length(), 0);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, value.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.DKGRAY), value.length(), value.length()+name.length()+1, 0);
        s.setSpan(new RelativeSizeSpan(1f), value.length(), value.length()+name.length()+1, 0);
        return s;

    }
    private void initVocChart() {
        String Vocvalue = "150";
        String Vocname = "TVOC";
        mChartvoc = (PieChart) findViewById(R.id.chartVOC);
        mChartvoc.setUsePercentValues(false);
        mChartvoc.getDescription().setEnabled(false);
        mChartvoc.setExtraOffsets(5, 10, 5, 5);

        mChartvoc.setDragDecelerationFrictionCoef(0.95f);
        //绘制中间文字
        mChartvoc.setCenterText(generateCenterSpannableText(Vocvalue,Vocname));
        mChartvoc.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        mChartvoc.setDrawHoleEnabled(true);
        mChartvoc.setHoleColor(Color.WHITE);

        mChartvoc.setTransparentCircleColor(Color.WHITE);
        mChartvoc.setTransparentCircleAlpha(88);

        mChartvoc.setHoleRadius(87f);
        mChartvoc.setTransparentCircleRadius(90f);

        mChartvoc.setDrawCenterText(true);

        mChartvoc.setRotationAngle(-90);
        // 触摸旋转
        mChartvoc.setRotationEnabled(false);
        mChartvoc.setHighlightPerTapEnabled(true);
        mChartvoc.setDrawSliceText(false);


        // 添加一个选择监听器
        mChartvoc.setOnChartValueSelectedListener(this);

        //模拟数据
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        entries.add(new PieEntry(60, "TVOC"));
        entries.add(new PieEntry(40));


        //设置数据
        vocsetData(entries);

        //默认动画
        mChartvoc.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChartvoc.getLegend();
        l.setEnabled(false);
    }



    private void init2_5Chart() {
        String PMvalue = "160";
        String PMname = "PM2.5";
        mChart2_5 = (PieChart) findViewById(R.id.chart2_5);
        mChart2_5.setUsePercentValues(false);
        mChart2_5.getDescription().setEnabled(false);
        mChart2_5.setExtraOffsets(5, 10, 5, 5);

        mChart2_5.setDragDecelerationFrictionCoef(0.95f);
        //绘制中间文字
        mChart2_5.setCenterText(generateCenterSpannableText(PMvalue,PMname));
        mChart2_5.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        mChart2_5.setDrawHoleEnabled(true);
        mChart2_5.setHoleColor(Color.WHITE);

        mChart2_5.setTransparentCircleColor(Color.WHITE);
        mChart2_5.setTransparentCircleAlpha(88);

        mChart2_5.setHoleRadius(87f);
        mChart2_5.setTransparentCircleRadius(90f);

        mChart2_5.setDrawCenterText(true);

        mChart2_5.setRotationAngle(-90);
        // 触摸旋转
        mChart2_5.setRotationEnabled(false);
        mChart2_5.setHighlightPerTapEnabled(true);
        mChart2_5.setDrawSliceText(false);


        // 添加一个选择监听器
        mChart2_5.setOnChartValueSelectedListener(this);

        //模拟数据
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        entries.add(new PieEntry(40, "PM2.5"));
        entries.add(new PieEntry(60));


        //设置数据
        dotsetData(entries);

        //默认动画
        mChart2_5.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart2_5.getLegend();
        l.setEnabled(false);
    }




    private void initDialog() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("正在连接服务器");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(true);
        dialog.show();


    }

    private void initChart() {
        Typeface mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Bold.ttf");
        barchart = (HorizontalBarChart) findViewById(R.id.barchart);

        barchart.setDrawBarShadow(false);
        barchart.setDrawValueAboveBar(true);
        barchart.setNoDataText("没有数据");
        barchart.setMaxVisibleValueCount(60);
        barchart.getDescription().setEnabled(false);
        barchart.setTouchEnabled(false);
        barchart.setDragEnabled(true);
        barchart.setScaleEnabled(true);
        barchart.setPinchZoom(false);
        barchart.setDrawGridBackground(false);
        barchart.setGridBackgroundColor(Color.GRAY);


        Legend l = barchart.getLegend();
        /*l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);*/
        l.setEnabled(false);

        XAxis xAxis = barchart.getXAxis();
        xAxis.setAxisMinimum(8);
        xAxis.setAxisMaximum(45);
        xAxis.setEnabled(false);



        YAxis leftAxis = barchart.getAxisLeft();
        leftAxis.setEnabled(false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(10f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = barchart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(mTf);
        rightAxis.setSpaceTop(10f);
        rightAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
        rightAxis.addLimitLine(new LimitLine(25,"超标线"));

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(10,(float)15.3,"NO2"));
        entries.add(new BarEntry(22,(float)22,"NO"));
        entries.add(new BarEntry(32,16,"CH4"));
        entries.add(new BarEntry(42,(float)28.5,"PM10"));

        barsetData(entries);
    }

    private void barsetData(ArrayList entries) {
        float barWidth = 4f;
        BarDataSet set1;
        if (barchart.getData() != null &&
                barchart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) barchart.getData().getDataSetByIndex(0);
            set1.setValues(entries);
            barchart.getData().notifyDataChanged();
            barchart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(entries,"空气污染物");
            set1.setColors(new int[]{Color.rgb(0, 238, 118), Color.rgb(155, 241, 226), Color.rgb(250, 128, 114), Color.rgb(255, 241, 26)});
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setDrawValues(true);
            data.setBarWidth(barWidth);
            barchart.setData(data);
        }
    }

    private void dotsetData(ArrayList<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "PM2.5");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        ArrayList<Integer> colors = new ArrayList<Integer>();

            colors.add(Color.rgb(51,204,204));
            colors.add(Color.rgb(220,220,220));

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        mChart2_5.setData(data);

        // 撤销所有的亮点
        mChart2_5.highlightValues(null);
        mChart2_5.invalidate();
    }

    private void vocsetData(ArrayList<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "TVOC");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        ArrayList<Integer> colors = new ArrayList<Integer>();

        colors.add(Color.rgb(51,204,204));
        colors.add(Color.rgb(220,220,220));

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        mChartvoc.setData(data);

        // 撤销所有的亮点
        mChartvoc.highlightValues(null);
        mChartvoc.invalidate();
    }
    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_Refresh:
                atartAnimation();
                MyHandler mhandler = new MyHandler();
                mhandler.sendEmptyMessageDelayed(1000, 3000);
                OutputStream os = null;
                try {

                    os = socket.getOutputStream();
                    os.write(("123456\n").getBytes("utf-8"));
                    Log.i("Start", "发送成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void atartAnimation() {
        anim = AnimationUtils.loadAnimation(this, R.anim.iv_rotate);
        LinearInterpolator ll = new LinearInterpolator();
        anim.setInterpolator(ll);
        if (anim != null) {
            iv_fresh.startAnimation(anim);
        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        int span = 1000;
        option.setScanSpan(span);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
    }

    public void settv(String tv) {
        final String location = tv;
        if(mTvCity != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mTvCity.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvCity.setText(location);
                        }
                    });

                }
            }).start();
        }

    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            switch (location.getLocType()) {
                case BDLocation.TypeGpsLocation:
                case BDLocation.TypeNetWorkLocation:
                case BDLocation.TypeOffLineLocation:
                     city = location.getDistrict();
                    Log.i(Constans.TAG, "定位成功" + city);
                    break;
                case BDLocation.TypeServerError:
                case BDLocation.TypeNetWorkException:
                case BDLocation.TypeCriteriaException:
                    Log.i(Constans.TAG, "定位失败");

                    break;
                            }
            settv(city);
            }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }



    private class StartThread extends Thread {
    @Override
    public void run() {
        try {
            socket = new Socket("192.168.191.1", 40002);//连接服务端的IP
            Log.i("Start", String.valueOf(socket.isConnected()));
            rt = new ReceiveThread(socket);
            rt.start();
            running = true;

            //启动接收数据的线程
            if (socket.isConnected()) {//成功连接获取socket对象则发送成功消息
                Message msg0 = myhandler.obtainMessage();
                msg0.what = 0;
                myhandler.sendMessage(msg0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

private class ReceiveThread extends Thread {
    private InputStream is;

    //建立构造函数来获取socket对象的输入流
    public ReceiveThread(Socket socket) throws IOException {
        is = socket.getInputStream();
    }

    @Override
    public void run() {
        while (running) {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            try {
                //读服务器端发来的数据，阻塞直到收到结束符\n或\r
                System.out.println(str = br.readLine());

            } catch (NullPointerException e) {
                running = false;//防止服务器端关闭导致客户端读到空指针而导致程序崩溃
                Message msg2 = myhandler.obtainMessage();
                msg2.what = 2;
                myhandler.sendMessage(msg2);//发送信息通知用户客户端已关闭
                e.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message msg = myhandler.obtainMessage();
            msg.what = 1;
            msg.obj = str;
            myhandler.sendMessage(msg);
            try {
                sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        Message msg2 = myhandler.obtainMessage();
        msg2.what = 2;
        myhandler.sendMessage(msg2);//发送信息通知用户客户端已关闭

    }
}

    private void displayToast(String s)//Toast方法
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


class MyHandler extends Handler {//在主线程处理Handler传回来的message

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                String str = (String) msg.obj;
                displayToast(str);
                break;
            case 0:
                displayToast("连接服务器");
                dialog.dismiss();
                break;
            case 2:
                displayToast("服务器端已断开");
                st = new StartThread();
                initDialog();
                mLocationClient.start();
                break;
            case 1000:
                if (anim != null) {
                    iv_fresh.clearAnimation();
                }
                break;
            }

    }
}

}

