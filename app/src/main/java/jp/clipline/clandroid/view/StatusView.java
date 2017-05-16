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

public class StatusView extends LinearLayout {


    public enum STATUS_VIEW {
        VIEW, REPORT, CHECK
    }

    private TextView mTextViewLine;
    private TextView mTextViewStatus;
    private TextView mTextViewContent;


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
        mTextViewLine = (TextView) view.findViewById(R.id.textViewLine);
        mTextViewStatus = (TextView) view.findViewById(R.id.textViewStatus);
        mTextViewContent = (TextView) view.findViewById(R.id.textViewContent);

    }

    public TextView getTextViewLine() {
        return mTextViewLine;
    }

    public void setTextViewLine(TextView line) {
        this.mTextViewLine = line;
    }

    public TextView getTextViewStatus() {
        return mTextViewStatus;
    }

    public void setTextViewStatus(TextView status) {
        this.mTextViewStatus = status;
    }

    public TextView getTextViewContent() {
        return mTextViewContent;
    }

    public void setTextViewContent(TextView content) {
        this.mTextViewContent = content;
    }

    public void setTypeView(STATUS_VIEW typeView, boolean isScreenSelect) {
        if (typeView.equals(STATUS_VIEW.VIEW)) {

            mTextViewLine.setVisibility(GONE);
            mTextViewStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.icon_status_complete));
            mTextViewContent.setText(getContext().getText(R.string.footer_view));
        } else if (isScreenSelect) {  //Screen select

            if (typeView.equals(STATUS_VIEW.REPORT)) {
                mTextViewLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
                mTextViewStatus.setText("2");
                mTextViewStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_green));
                mTextViewContent.setText(getContext().getText(R.string.footer_shoot));
            } else if (typeView.equals(STATUS_VIEW.CHECK)) {
                mTextViewLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_gray_header_color));
                mTextViewStatus.setText("3");
                mTextViewStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_gray));
                mTextViewContent.setText(getContext().getText(R.string.footer_compare));
            }
        } else { ////Screen compare

            mTextViewLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
            if (typeView.equals(STATUS_VIEW.REPORT)) {
                mTextViewStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.icon_status_complete));
            } else if (typeView.equals(STATUS_VIEW.CHECK)) {
                mTextViewStatus.setText("3");
                mTextViewStatus.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_green));
            }
        }

    }


}
