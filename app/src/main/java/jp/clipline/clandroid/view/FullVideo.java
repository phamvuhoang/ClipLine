package jp.clipline.clandroid.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by nguyentu on 5/14/17.
 */
///// 20170514 MODIFY START
public class FullVideo extends VideoView {

    protected int defaultWidth = 1920;
    protected int defaultHeight = 1080;

    public FullVideo(Context context) {
        super(context);
    }

    public FullVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullVideo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(defaultWidth, widthMeasureSpec);
        int height = getDefaultSize(defaultHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
///// 20170514 MODIFY START
