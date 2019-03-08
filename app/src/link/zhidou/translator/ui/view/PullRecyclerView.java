package link.zhidou.translator.ui.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import link.zhidou.translator.R;
import link.zhidou.translator.utils.Log;

/**
 * Created by keetom on 2018/3/23.
 */
public class PullRecyclerView extends LinearLayout implements SwipeRefreshLayout.OnRefreshListener, View.OnTouchListener {
    private static final String TAG = PullRecyclerView.class.getSimpleName();
    private static final boolean DEBUG = Log.isLoggable();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout headView;
    private OnPullRefreshListener listener;
    //是否正在刷新
    private boolean isRefreshing = false;
    //是否正在加载
    private boolean isLoading = false;
    //是否有更多数据
    private boolean hasMore = true;

    public PullRecyclerView(Context context) {
        this(context, null);
    }

    public PullRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initListener();
        init();
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.pull_recycle_layout, this, true);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        headView = (LinearLayout) findViewById(R.id.headView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void initListener() {
        swipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.addOnScrollListener(new PullableScroll());
        //防止滚动的时候，滑动View
        mRecyclerView.setOnTouchListener(this);
    }

    private void init() {
        //隐藏垂直滚动条
        mRecyclerView.setVerticalScrollBarEnabled(true);
        //item高度固定时，设置该选项提高性能
        mRecyclerView.setHasFixedSize(true);
        /**
         * 关闭动画,出现item重叠现象,关屏幕->开屏幕
         * 首先打开互聊天画面,消息一下子进来.
         */
        mRecyclerView.setItemAnimator(null);
        //禁止下拉刷新
        setRefreshEnable(false);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layout) {
        mRecyclerView.setLayoutManager(layout);
    }

    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        mRecyclerView.setVerticalScrollBarEnabled(verticalScrollBarEnabled);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decor) {
        mRecyclerView.addItemDecoration(decor);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 设置监听下拉或上拉的事件
     *
     * @param listener
     */
    public void setOnPullRefreshListener(OnPullRefreshListener listener) {
        this.listener = listener;
    }

    /**
     * 设置是否有更多数据
     *
     * @param hasMore
     */
    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    /**
     * 设置是否允许下拉
     *
     * @param enable
     */
    public void setRefreshEnable(boolean enable) {
        swipeRefreshLayout.setEnabled(enable);
    }

    /**
     * 滚动时判断能否刷新
     *
     * @return
     */
    private boolean isRefreshEnable() {
        return !isRefreshing && !isLoading;
    }


    /**
     * 正在加载更多
     */
    public void doLoadMore() {
        if (DEBUG) {
            Log.d(TAG, "isLoading: " + isLoading + ", hasMore: " + hasMore + ", isRefreshing: " + isRefreshing);
        }
        if (!isLoading && hasMore && !isRefreshing) {
            headView.setVisibility(View.VISIBLE);
            isLoading = true;
            //禁止下拉
            if (listener != null) {
                listener.onLoadMore();
            }
        }
    }

    /**
     * 刷新或加载完成
     */
    public void refreshOrLoadComplete() {
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        headView.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return isRefreshing || isLoading;
    }

    @Override
    public void onRefresh() {

    }

    public interface OnPullRefreshListener {
        /**
         * 加载操作
         */
        void onLoadMore();
    }

    /**
     * 监听RecycleView滑动底部或顶部
     */
    class PullableScroll extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItem = 0;
            int firstVisibleItem = 0;
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            int totalItemCount = layoutManager.getItemCount();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                // since may lead to the final item has more than one StaggeredGridLayoutManager the particularity of the so here that is an array
                // this array into an array of position and then take the maximum value that is the last show the position value
                int[] lastPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisibleItem = findMax(lastPositions);
                firstVisibleItem = staggeredGridLayoutManager.findFirstVisibleItemPositions(lastPositions)[0];
            }
//            pullRefreshEnable(firstVisibleItem, totalItemCount);
//            if (isSlideToBottom(recyclerView)) {
            loadMore(dx, dy, firstVisibleItem, totalItemCount);
//            }
        }

        private int findMax(int[] lastPositions) {
            int max = lastPositions[0];
            for (int value : lastPositions) {
                if (value > max) {
                    max = value;
                }
            }
            return max;
        }
    }

    /**
     * 判断是否滑动到底部
     *
     * @param recyclerView
     * @return
     */
    public boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return false;
        }
        if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset()
                >= recyclerView.computeVerticalScrollRange()) {
            return true;
        }
        return false;
    }

    private void loadMore(int dx, int dy, int lastVisibleItem, int totalItemCount) {
        //滚动到底部时且有更多数据能够上拉加载
        if (lastVisibleItem == 0 && (dx < 0 || dy < 0)) {
            doLoadMore();
        }
    }

    public void scrollBottomToPosition(int position) {
        mRecyclerView.scrollToPosition(position);
    }

    public void scrollTopToPosition(int position) {
        if (position != -1) {
            mRecyclerView.scrollToPosition(position);
            LinearLayoutManager mLayoutManager =
                    (LinearLayoutManager) mRecyclerView.getLayoutManager();
            mLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    public void setHasFixedSize(boolean hasFixedSize) {
        mRecyclerView.setHasFixedSize(hasFixedSize);
    }

    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        mRecyclerView.setItemAnimator(animator);
    }

    /**
     * 滚动到顶部
     */
    public void scrollToTop() {
        mRecyclerView.scrollToPosition(0);
    }
}