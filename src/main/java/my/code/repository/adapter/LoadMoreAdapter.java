package my.code.repository.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Collection;

import advanced.nioDemo.R;
/**
 * A recyclerView adapter, use to show load more layout.
 *
 * @author djh
 */
public class LoadMoreAdapter<T> extends RecyclerView.Adapter<LoadMoreAdapter.LoadMoreViewHolder> {

    /**
     * Whether is loading.
     */
    public boolean isLoadIng = false;

    /**
     * Whether is load all.
     */
    public boolean loadAll = false;

    /**
     * Use to get item num.
     */
    private Collection<T> mItems;

    /**
     * Load more viewHolder.
     */
    private LoadMoreViewHolder mLoadMoreViewHolder;

    /**
     * Callback interface
     */
    private LoadMoreListener mLoadMoreListener;

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_LOAD_MORE = 1;

    protected LoadMoreAdapter(Collection<T> items) {
        mItems = items;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        // Get the layout manager.
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        // Set the recyclerView scroll listener, use to call back loading more.
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //If the last visible item is the last one in the data collection.
                //Start loading more.
                if (linearLayoutManager.findLastVisibleItemPosition() == mItems.size()) {
                    if (mLoadMoreListener != null && !isLoadIng && !loadAll) {
                        isLoadIng = true;
                        mLoadMoreListener.startLoad();
                    }
                }
            }
        });
    }

    /**
     * Child class should call this method, if the item is load more item,
     * will return it, else return a invalid item,child class should use
     * {@link LoadMoreViewHolder#isLoadMoreViewHolder} to judge whether should
     * use the parent method return value.
     */
    @NonNull
    @Override
    public LoadMoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOAD_MORE) {
            FrameLayout loadMoreFrameLayout;
            loadMoreFrameLayout = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_layout, parent, false);
            mLoadMoreViewHolder = new LoadMoreViewHolder(loadMoreFrameLayout);
            return mLoadMoreViewHolder;
        }
        // Use this to avoid compile warning.
        LoadMoreViewHolder loadMoreViewHolder = new LoadMoreViewHolder(new View(parent.getContext()));
        loadMoreViewHolder.isLoadMoreViewHolder = false;
        return loadMoreViewHolder;
    }

    /**
     * Child class should simple call this method.
     */
    @Override
    public void onBindViewHolder(@NonNull LoadMoreViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_LOAD_MORE) {
            //Determine if all data is loaded, show end text.
            if (loadAll) {
                holder.mLoadingView.setVisibility(View.GONE);
                holder.mEndText.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Plus one is because it loads more layout treats it as a item.
     * Just the layout is different.
     */
    @Override
    public final int getItemCount() {
        // If items size is zero, we should not show the load more item.
        if (mItems.size() == 0) {
            return 0;
        }
        return mItems.size() + 1;
    }

    @Override
    public final int getItemViewType(int position) {
        // If the position is the last item, show the load more item.
        if (position == mItems.size()) {
            return TYPE_LOAD_MORE;
        } else {
            return TYPE_NORMAL;
        }
    }

    /**
     * use to set load more interface call back.
     */
    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
    }

    /**
     * Call this when load more is failure.
     */
    public void setFailureFootLayout() {
        if (mLoadMoreViewHolder != null) {
            // Gone the loading view.
            mLoadMoreViewHolder.mLoadingView.setVisibility(View.GONE);
            // Show the failure text.
            mLoadMoreViewHolder.mFailureText.setVisibility(View.VISIBLE);
            //Set up monitoring,click retry to load.
            mLoadMoreViewHolder.mFailureText.setOnClickListener(v -> {
                if (mLoadMoreListener != null) {
                    //Start load.
                    mLoadMoreListener.startLoad();
                    mLoadMoreViewHolder.mFailureText.setVisibility(View.GONE);
                    //Show loading status.
                    mLoadMoreViewHolder.mLoadingView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        /**
         * Use to judge this view holder whether is load more viewHolder.
         */
        public boolean isLoadMoreViewHolder = true;

        /**
         * Load more, load end and load failure view.
         */
        private TextView mEndText;
        private TextView mFailureText;
        private LottieAnimationView mLoadingView;

        /**
         * This is default construct.
         */
        public LoadMoreViewHolder(View itemView) {
            super(itemView);
        }

        /**
         * This construct use to init the load more layout.
         */
        LoadMoreViewHolder(FrameLayout loadMoreLayout) {
            super(loadMoreLayout);
            // Init the load more layout.
            mEndText = loadMoreLayout.findViewById(R.id.end);
            mFailureText = loadMoreLayout.findViewById(R.id.loadFailure);
            mLoadingView = loadMoreLayout.findViewById(R.id.lottie_loading);
        }

    }

    public interface LoadMoreListener {
        /**
         * Start loading data.
         */
        void startLoad();
    }

}
