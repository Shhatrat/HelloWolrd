package eu.proexe.android.hellowolrd.ff;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import eu.proexe.android.hellowolrd.CircleRefreshLayout;
import eu.proexe.android.hellowolrd.R;

public class AnimationView extends View {

    enum AnimatorStatus {
        PULL_DOWN,
        DRAG_DOWN,
        REL_DRAG,
        SPRING_UP, // rebound to up, the position is less than PULL_HEIGHT
        POP_BALL,
        OUTER_CIR,
        REFRESHING,
        DONE,
        STOP;
    }


    private int PULL_HEIGHT;
    private int PULL_DELTA;
    private float mWidthOffset;

    private MotionEvent currentEvent;

    private AnimatorStatus mAniStatus = AnimatorStatus.PULL_DOWN;
    private OnViewAniDone onViewAniDone;

    public AnimationView(Context context) {
        this(context, null, 0);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        PULL_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
        PULL_DELTA = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height > PULL_DELTA + PULL_HEIGHT) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(PULL_DELTA + PULL_HEIGHT, MeasureSpec.getMode(heightMeasureSpec));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int mHeight;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mHeight = getHeight();

            if (mHeight < PULL_HEIGHT) {
                mAniStatus = AnimatorStatus.PULL_DOWN;
            }

            switch (mAniStatus) {
                case PULL_DOWN:
                    if (mHeight >= PULL_HEIGHT) {
                        mAniStatus = AnimatorStatus.DRAG_DOWN;
                    }
                    break;
                case REL_DRAG:
                    break;
            }
        }
    }

    private void drawListener(Canvas canvas, CircleRefreshLayout.DrawRefreshType type){
        LinearLayout layout = new LinearLayout(getContext());
        layout.measure(canvas.getWidth(), canvas.getHeight());
        layout.layout(0, 0, canvas.getWidth(), canvas.getHeight());
        int x = canvas.getWidth()/2;
        int y = canvas.getHeight()/2;
        canvas.translate(x,y);
        layout.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas, "down");
    }

    void drawText(Canvas canvas, String text){
        if(currentEvent!=null && currentEvent.getAction() == MotionEvent.ACTION_MOVE){
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(getContext());
            textView.setVisibility(View.VISIBLE);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            textView.setText(text);
            layout.addView(textView);

            ImageView imageView = new ImageView(getContext());
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_launcher_background));
            layout.addView(imageView);

            layout.measure(canvas.getWidth(), canvas.getHeight());
            layout.layout(0, 0, canvas.getWidth(), canvas.getHeight());

            canvas.translate(canvas.getWidth()/2 - textView.getWidth()/2,canvas.getHeight()/3);
            layout.draw(canvas);
        }
    }


    public void currentTouchEvent(MotionEvent event) {
        currentEvent = event;
    }

    public void setOnViewAniDone(OnViewAniDone onViewAniDone) {
        this.onViewAniDone = onViewAniDone;
    }

    interface OnViewAniDone {
        void viewAniDone();
    }

}