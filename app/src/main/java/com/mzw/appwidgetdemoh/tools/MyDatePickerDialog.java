package com.mzw.appwidgetdemoh.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by think on 2019/1/20.
 */

public class MyDatePickerDialog  extends AlertDialog implements DialogInterface.OnClickListener,
        DatePicker.OnDateChangedListener {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private static boolean showYear = true;
    private static boolean showMonth = true;
    private static boolean showDay = true;

    TextView lunarView;
    Lunar lunar;
    private DatePicker mDatePicker;
    private OnDateSetListener mDateSetListener;

    protected MyDatePickerDialog(Context context) {
        this(context, 0, null, Calendar.getInstance(),showYear,showMonth,showDay);
    }

    protected MyDatePickerDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        this(context, 0, null, Calendar.getInstance(),showYear,showMonth,showDay);
    }

    protected MyDatePickerDialog(Context context, int themeResId) {
        this(context, 0, null, Calendar.getInstance(),showYear,showMonth,showDay);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mDateSetListener != null) {
                    // Clearing focus forces the dialog to commit any pending
                    // changes, e.g. typed text in a NumberPicker.
                    mDatePicker.clearFocus();

                    mDateSetListener.onDateSet(mDatePicker, mDatePicker.getYear(),mDatePicker.getMonth() + 1, mDatePicker.getDayOfMonth()
                            ,lunar.cyclical() + lunar.animalsYear()+"年"+lunar.toString(),lunar.getMonthStr()+lunar.getDayStr());
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        try {
            Date date = sdf.parse(year+"-"+(monthOfYear+1)+"-"+dayOfMonth);
            Calendar calendar= Calendar.getInstance();
            calendar.setTime(date);
            lunar = new Lunar(calendar);
            //大于2050年 不显示农历
            if(year >= 2050){
                lunarView.setVisibility(View.INVISIBLE);
            }else{
                lunarView.setVisibility(View.VISIBLE);
            }
            if(lunarView != null){
                lunarView.setText("【"+lunar.cyclical()+lunar.animalsYear()+"年"+lunar.toString()+"】");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
    }


    public MyDatePickerDialog(Context context, int themeResId,
                              OnDateSetListener listener, Calendar calendar
            , boolean showYear, boolean showMonth, boolean showDay) {
        super(context, resolveDialogTheme(context, themeResId));

        MyDatePickerDialog.showYear = showYear;
        MyDatePickerDialog.showMonth = showMonth;
        MyDatePickerDialog.showDay = showDay;

        int year = -1,monthOfYear = -1,dayOfMonth = -1;

        final Context themeContext = getContext();
        final LayoutInflater inflater = LayoutInflater.from(themeContext);

        final View view = inflater.inflate(
//                com.android.internal.R.layout.date_picker_dialog
                Resources.getSystem().getIdentifier("date_picker_dialog","layout","android"), null);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout laout = new RelativeLayout(context);
        lunarView = new TextView(context);
        lunarView.setPadding(5,5,5,5);
        lunarView.setTextSize(16);
        laout.addView(view,params);
        laout.addView(lunarView);
        setView(laout);

        setButton(BUTTON_POSITIVE, themeContext.getString(
//                com.android.internal.R.string.ok
                Resources.getSystem().getIdentifier("ok","string","android")
        ), this);
        setButton(BUTTON_NEGATIVE, themeContext.getString(
//                com.android.internal.R.string.cancel
                Resources.getSystem().getIdentifier("cancel","string","android")
        ), this);
//        setButtonPanelLayoutHint(LAYOUT_HINT_SIDE);

        if (calendar != null) {
            year = calendar.get(Calendar.YEAR);
            monthOfYear = calendar.get(Calendar.MONTH);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }

        mDatePicker = (DatePicker) view.findViewById(
//                com.android.internal.R.id.datePicker
                Resources.getSystem().getIdentifier("datePicker","id","android")
        );
        lunar = new Lunar(calendar);
        if(lunarView != null){
            lunarView.setText("【"+lunar.animalsYear()+"年"+lunar.toString()+"】");
        }
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        //隐藏布局
        if(!showYear){
            ((ViewGroup)((ViewGroup) mDatePicker.getChildAt(0)).getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
        }
        if(!showMonth){
            ((ViewGroup)((ViewGroup) mDatePicker.getChildAt(0)).getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
        }
        if(!showDay){
            ((ViewGroup)((ViewGroup) mDatePicker.getChildAt(0)).getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
        }
        mDateSetListener = listener;
    }

    static int resolveDialogTheme(Context context, int themeResId) {
        if (themeResId == 0) {
            final TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(
//                    com.android.internal.R.attr.datePickerDialogTheme
                    Resources.getSystem().getIdentifier("datePickerDialogTheme","attr","android")
                    , outValue, true);
            return outValue.resourceId;
        } else {
            return themeResId;
        }
    }

    public interface OnDateSetListener {
        /**
         * @param view the picker associated with the dialog
         * @param year the selected year
         * @param month the selected month (0-11 for compatibility with
         *              {@link Calendar#MONTH})
         * @param dayOfMonth th selected day of the month (1-31, depending on
         *                   month)
         */
//        void onDateSet(DatePicker view, int year, int month, int dayOfMonth, String lunarStr, String str);
        void onDateSet(DatePicker view, int year, int month, int dayOfMonth,String lunarYMD, String lunarMD);
    }




}
