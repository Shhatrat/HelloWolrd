package eu.proexe.android.hellowolrd.ff;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import eu.proexe.android.hellowolrd.CircleRefreshLayout;

public class RefreshLayout extends FrameLayout {

    enum State{
        INIT,
        START_REFRESHING_BY_HAND,
        START_REFRESHING,
        REFRESHING_BY_HAND,
        REFRESHING,
        CLOSING
    }

    private State currentState = State.INIT;
    private static final long BACK_TOP_DUR = 600;
    private static final long REL_DRAG_DUR = 200;

    private float pullHeight;

    private View childView;
    private AnimationView mHeader;

    private float mTouchStartY;
    private float mTouchCurY;

    private ValueAnimator mUpBackAnimator;
    private ValueAnimator mUpTopAnimator;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(10);

    public RefreshLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    ValueAnimator startAnimator;
    ValueAnimator backAnimator;
    ValueAnimator backImmediatelyAnimator;

    public void startRefreshing(){
        if(currentState == State.CLOSING){
            if (backAnimator!=null )backAnimator.cancel();
        }

        currentState = State.START_REFRESHING;
        startAnimator = ValueAnimator.ofFloat(0, pullHeight);
        startAnimator.setDuration(BACK_TOP_DUR);
        startAnimator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            val = decelerateInterpolator.getInterpolation(val / pullHeight) * val;
            if (childView != null) {
                childView.setTranslationY(val);
            }
            mHeader.getLayoutParams().height = (int) val;
            mHeader.requestLayout();
        });
        startAnimator.start();
    }

    public void stopRefreshing(){
        if(currentState == State.START_REFRESHING) {
            startAnimator.cancel();
        }else if(currentState == State.START_REFRESHING_BY_HAND){
            return;
        }
        currentState = State.CLOSING;
        backAnimator = ValueAnimator.ofFloat(childView.getTranslationY(), 0);
        float height = childView.getTranslationY();
        backAnimator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            val = decelerateInterpolator.getInterpolation(val / pullHeight) * val;
            if (childView != null) {
                childView.setTranslationY(val);
            }
            mHeader.getLayoutParams().height = (int) val;
            mHeader.requestLayout();
            currentState = State.INIT;
        });
        backAnimator.setDuration((long) (height * BACK_TOP_DUR / pullHeight));
        backAnimator.start();
    }

    public void stopRefreshingImmediately(){
        if(currentState == State.START_REFRESHING) {
            startAnimator.cancel();
        }else if(currentState == State.START_REFRESHING_BY_HAND) {
            return;
        }
        currentState = State.CLOSING;
        float height = childView.getTranslationY();
        backImmediatelyAnimator = ValueAnimator.ofFloat(height, 0);
        backImmediatelyAnimator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            val = decelerateInterpolator.getInterpolation(val / pullHeight) * val;
            if (childView != null) {
                childView.setTranslationY(val);
            }
            mHeader.getLayoutParams().height = (int) val;
            mHeader.requestLayout();
            currentState = State.INIT;
        });
        backImmediatelyAnimator.setDuration(0);
        backImmediatelyAnimator.start();

    }

    private void init(Context context){
        currentState = State.INIT;
        pullHeight = Util.dpToPx(context, 100);
        this.post(() -> {
            childView = getChildAt(0);
            addHeaderView();
        });
    }

    private void addHeaderView(){
        mHeader = new AnimationView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.gravity = Gravity.TOP;
        mHeader.setLayoutParams(params);
//        mHeader.setOnDrawListener(onDrawListener);
        super.addView(mHeader);
        setUpChildAnimation();
    }

    private void setUpChildAnimation() {
        if (childView == null) {
            return;
        }
        mUpBackAnimator = ValueAnimator.ofFloat(pullHeight, pullHeight);
        mUpBackAnimator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            if (childView != null) {
                childView.setTranslationY(val);
            }
        });
        mUpBackAnimator.setDuration(REL_DRAG_DUR);
        mUpTopAnimator = ValueAnimator.ofFloat(pullHeight, 0);
        mUpTopAnimator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            val = decelerateInterpolator.getInterpolation(val / pullHeight) * val;
            if (childView != null) {
                childView.setTranslationY(val);
            }
            mHeader.getLayoutParams().height = (int) val;
            mHeader.requestLayout();
        });
        mUpTopAnimator.setDuration(BACK_TOP_DUR);
        mHeader.setOnViewAniDone(() -> mUpTopAnimator.start());
    }

    @Override
    public void addView(View child) {
        if (getChildCount() >= 1) {
            throw new RuntimeException("you can only attach one child");
        }
        childView = child;
        super.addView(child);
        setUpChildAnimation();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (currentState!=State.INIT) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartY = ev.getY();
                mTouchCurY = mTouchStartY;
                break;
            case MotionEvent.ACTION_MOVE:
                float curY = ev.getY();
                float dy = curY - mTouchStartY;
                if (dy > 0 && !ViewCompat.canScrollVertically(childView, -1)) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentState == State.REFRESHING || currentState == State.REFRESHING_BY_HAND) {
            return super.onTouchEvent(event);
        }
        mHeader.currentTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mTouchCurY = event.getY();
                currentState = State.START_REFRESHING_BY_HAND;
                float dy = mTouchCurY - mTouchStartY;
                dy = Math.min(pullHeight * 2, dy);
                dy = Math.max(0, dy);


                if (childView != null) {
                    float offsetY = decelerateInterpolator.getInterpolation(dy / 2 / pullHeight) * dy / 2;
                    childView.setTranslationY(offsetY);

                    mHeader.getLayoutParams().height = (int) offsetY;
                    mHeader.requestLayout();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (childView != null) {
                    if (childView.getTranslationY() >= pullHeight) {
                        mUpBackAnimator.start();
//                        mHeader.releaseDrag();
                        currentState = State.REFRESHING_BY_HAND;
                        if(onStartRefreshingListener!=null){
                            onStartRefreshingListener.onStartRefreshing();
                        }

                    } else {
                        currentState = State.CLOSING;
                        float height = childView.getTranslationY();
                        ValueAnimator backTopAni = ValueAnimator.ofFloat(height, 0);
                        backTopAni.addUpdateListener(animation -> {
                            float val = (float) animation.getAnimatedValue();
                            val = decelerateInterpolator.getInterpolation(val / pullHeight) * val;
                            if (childView != null) {
                                childView.setTranslationY(val);
                            }
                            mHeader.getLayoutParams().height = (int) val;
                            mHeader.requestLayout();
                            currentState = State.INIT;
                        });
                        backTopAni.setDuration((long) (height * BACK_TOP_DUR / pullHeight));
                        backTopAni.start();
                    }
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private CircleRefreshLayout.OnStartRefreshingListener onStartRefreshingListener;

    public void setOnStartRefreshingListener(CircleRefreshLayout.OnStartRefreshingListener onStartRefreshingListener) {
        this.onStartRefreshingListener = onStartRefreshingListener;
    }
}
