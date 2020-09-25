package eu.proexe.android.hellowolrd.ff;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import eu.proexe.android.hellowolrd.R;

public class AnimationView extends View {

    private RefreshLayout.State currentState;
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
        post(() -> onViewAniDone.viewAniDone());
    }

    void setCurrentState(RefreshLayout.State state){
        currentState = state;
        if(state == RefreshLayout.State.REFRESHING
                || state == RefreshLayout.State.REFRESHING_BY_HAND)
            invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(currentState == RefreshLayout.State.START_REFRESHING_BY_HAND)
            drawText(canvas, "down");
    }

    void drawText(Canvas canvas, String text){
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

    public void setOnViewAniDone(OnViewAniDone onViewAniDone) {
        this.onViewAniDone = onViewAniDone;
    }

    interface OnViewAniDone {
        void viewAniDone();
    }
}