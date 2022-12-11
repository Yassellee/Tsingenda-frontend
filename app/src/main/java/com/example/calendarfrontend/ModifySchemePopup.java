package com.example.calendarfrontend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lxj.xpopup.core.CenterPopupView;

@SuppressLint("ViewConstructor")
public class ModifySchemePopup extends CenterPopupView {
    private final Scheme mScheme;
    private final int position;
    private EditText mTitle, mDate, mLocation, mStartTime, mEndTime;

    public ModifySchemePopup(Context context, int position) {
        super(context);
        this.position = position;
        this.mScheme = MainActivity.schemeList.get(position);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.scheme_manager;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate() {
        super.onCreate();
        mTitle = findViewById(R.id.add_tv_title);
        mDate = findViewById(R.id.add_tv_date);
        mLocation = findViewById(R.id.add_tv_location);
        mStartTime = findViewById(R.id.add_tv_startTime);
        mEndTime = findViewById(R.id.add_tv_endTime);
        TextView title = findViewById(R.id.tv_title);

        title.setText("修改日程");
        mTitle.setText(mScheme.getTitle());
        mDate.setText(mScheme.getYear() + "-" + mScheme.getMonth() + "-" + mScheme.getDay());
        mLocation.setText(mScheme.getLocation());
        mStartTime.setText(mScheme.getStartTime());
        mEndTime.setText(mScheme.getEndTime());
        findViewById(R.id.add_btn_confirm).setOnClickListener(v -> {
            // 加入日程
            Scheme newScheme = new Scheme();
            newScheme.setTitle(mTitle.getText().toString());
            newScheme.setLocation(mLocation.getText().toString());
            newScheme.setStartTime(mStartTime.getText().toString());
            newScheme.setEndTime(mEndTime.getText().toString());
            String[] date = mDate.getText().toString().split("-");
            if(newScheme.getTitle().equals("")) {
                Toast.makeText(getContext(), "标题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if(date.length != 3){
                Toast.makeText(getContext(), "日期格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
            int year = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]);
            int day = Integer.parseInt(date[2]);
            if(!checkdate(year, month, day)){
                Toast.makeText(getContext(), "日期格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!checktime(newScheme.getStartTime(), newScheme.getEndTime())){
                Toast.makeText(getContext(), "时间格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
            newScheme.setYear(year);
            newScheme.setMonth(month);
            newScheme.setDay(day);
            newScheme.setConf_id(mScheme.getConf_id());
            newScheme.setId(mScheme.getId());
            DbHandler.updateScheme(MainActivity.schemeDB, "schemes", mScheme, newScheme);
            MainActivity.schemeList.set(position, newScheme);
            MainActivity.adapter.notifyDataSetChanged();
            dismiss();
        });
        findViewById(R.id.add_btn_cancel).setOnClickListener(v -> {
            // 忽略日程
            dismiss();
        });
    }

    private boolean checkdate(int year, int month, int day) {
        int[] maxdays = {31,29,31,30,31,30,31,31,30,31,30,31};
        if(year < 0 || month < 0 || month > 12 || day < 0 || day > maxdays[month - 1]) {
            return false;
        }
        if(month == 2 && day == 29) {
            return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        }
        return true;
    }
    private boolean checktime(String time1, String time2) {
        String[] time1s = time1.split(":");
        String[] time2s = time2.split(":");
        if(time1s.length != 2 || time2s.length != 2) {
            return false;
        }
        int hour1 = Integer.parseInt(time1s[0]);
        int minute1 = Integer.parseInt(time1s[1]);
        int hour2 = Integer.parseInt(time2s[0]);
        int minute2 = Integer.parseInt(time2s[1]);
        if(hour1 < 0 || hour1 > 23 || hour2 < 0 || hour2 > 23 || minute1 < 0 || minute1 > 59 || minute2 < 0 || minute2 > 59) {
            return false;
        }
        return hour1 <= hour2 && (hour1 != hour2 || minute1 <= minute2);
    }
}
