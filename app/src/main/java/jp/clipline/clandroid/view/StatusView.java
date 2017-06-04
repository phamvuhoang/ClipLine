package jp.clipline.clandroid.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.clipline.clandroid.R;

public class StatusView extends LinearLayout implements View.OnClickListener {


    public enum STATUS_VIEW {
        SELECT, SUBMISS, COMPARE
    }

    private TextView mTextViewStatus;
    private TextView mTextViewStatusContent;
    private TextView mTextViewLineViewReport;

    private TextView mTextViewReportStatus;
    private TextView mTextViewReportContent;
    private TextView mTextViewLineReportCheck;

    private TextView mTextViewCheckStatus;
    private TextView mTextViewCheckContent;

    private ClickListener mClickListener;


    public StatusView(Context context) {
        super(context);
        initView();
    }

    public StatusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        HandlerTypedArray(context, attrs);
    }

    public StatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        HandlerTypedArray(context, attrs);
    }

    private void HandlerTypedArray(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StatusView);
        array.recycle();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.layout_status_view, this);
        mTextViewStatus = (TextView) view.findViewById(R.id.textViewStatus);
        mTextViewStatusContent = (TextView) view.findViewById(R.id.textViewStatusContent);
        mTextViewLineViewReport = (TextView) view.findViewById(R.id.textViewLineViewReport);

        mTextViewReportStatus = (TextView) view.findViewById(R.id.textViewReportStatus);
        mTextViewReportContent = (TextView) view.findViewById(R.id.textViewReportContent);
        mTextViewLineReportCheck = (TextView) view.findViewById(R.id.textViewLineReportCheck);

        mTextViewCheckStatus = (TextView) view.findViewById(R.id.textViewCheckStatus);
        mTextViewCheckContent = (TextView) view.findViewById(R.id.textViewContentCheck);

        mTextViewStatus.setOnClickListener(this);
        mTextViewStatusContent.setOnClickListener(this);

        mTextViewReportStatus.setOnClickListener(this);
        mTextViewReportContent.setOnClickListener(this);

    }

    public TextView getTextViewStatus() {
        return mTextViewStatus;
    }

    public void setTextViewStatus(TextView textViewStatus) {
        this.mTextViewStatus = textViewStatus;
    }

    public TextView getTextViewStatusContent() {
        return mTextViewStatusContent;
    }

    public void setTextViewStatusContent(TextView textViewStatusContent) {
        this.mTextViewStatusContent = textViewStatusContent;
    }

    public TextView getTextViewLineViewReport() {
        return mTextViewLineViewReport;
    }

    public void setTextViewLineViewReport(TextView textViewLineViewReport) {
        this.mTextViewLineViewReport = textViewLineViewReport;
    }

    public TextView getTextViewReportStatus() {
        return mTextViewReportStatus;
    }

    public void setTextViewReportStatus(TextView textViewReportStatus) {
        this.mTextViewReportStatus = textViewReportStatus;
    }

    public TextView getTextViewReportContent() {
        return mTextViewReportContent;
    }

    public void setTextViewReportContent(TextView textViewReportContent) {
        this.mTextViewReportContent = textViewReportContent;
    }

    public TextView getTextViewLineReportCheck() {
        return mTextViewLineReportCheck;
    }

    public void setTextViewLineReportCheck(TextView textViewLineReportCheck) {
        this.mTextViewLineReportCheck = textViewLineReportCheck;
    }

    public TextView getTextViewCheckStatus() {
        return mTextViewCheckStatus;
    }

    public void setTextViewCheckStatus(TextView textViewCheckStatus) {
        this.mTextViewCheckStatus = textViewCheckStatus;
    }

    public TextView getTextViewCheckContent() {
        return mTextViewCheckContent;
    }

    public void setTextViewCheckContent(TextView textViewContentCheck) {
        this.mTextViewCheckContent = textViewContentCheck;
    }

    public ClickListener getClickListener() {
        return mClickListener;
    }

    public void setClickListener(ClickListener listener) {
        this.mClickListener = listener;
    }


    public void setTypeView(STATUS_VIEW typeView) {

        mTextViewStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.icon_status_complete));
        mTextViewLineViewReport.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));

        mTextViewStatusContent.setText(getContext().getText(R.string.footer_view));
        mTextViewReportContent.setText(getContext().getText(R.string.footer_shoot));
        mTextViewCheckContent.setText(getContext().getText(R.string.footer_compare));

        if (typeView.equals(STATUS_VIEW.SELECT) || typeView.equals(STATUS_VIEW.SUBMISS)) {
            mTextViewReportStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_green));
            mTextViewReportStatus.setText("2");

            mTextViewLineReportCheck.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_gray_header_color));

            mTextViewCheckStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_gray));
            mTextViewCheckStatus.setText("3");


        } else if (typeView.equals(STATUS_VIEW.COMPARE)) {

            mTextViewReportStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.icon_status_complete));

            mTextViewLineReportCheck.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));

            mTextViewCheckStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_green));
            mTextViewCheckStatus.setText("3");

        }
    }

    public interface ClickListener {
        void onListenerView();

        void onListenerReport();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textViewStatus:
            case R.id.textViewStatusContent:
                if (mClickListener != null) {
                    mClickListener.onListenerView();
                }
                break;

            case R.id.textViewReportStatus:
            case R.id.textViewReportContent:
                if (mClickListener != null) {
                    mClickListener.onListenerReport();
                }
                break;
            default:
                break;
        }
    }

}
