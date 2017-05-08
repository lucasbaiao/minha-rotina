package br.com.lucasbaiao.minharotina.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.com.lucasbaiao.minharotina.R;
import br.com.lucasbaiao.minharotina.databinding.ItemCategoryBinding;
import br.com.lucasbaiao.minharotina.persistence.AppDatabaseHelper;
import br.com.lucasbaiao.minharotina.persistence.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private final Activity mActivity;
    private List<Category> mCategories;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onClick(View view, int position, boolean isCounting);
    }

    public CategoryAdapter(Activity activity) {
        mActivity = activity;
        mLayoutInflater = LayoutInflater.from(activity.getApplicationContext());
        updateCategories(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemCategoryBinding view = DataBindingUtil.inflate(mLayoutInflater, R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ItemCategoryBinding binding = holder.getBinding();
        final Category category = mCategories.get(position);
        binding.setCategory(category);
        binding.executePendingBindings();

        final boolean isCounting = category.getEvents().size() > 0
                && category.getEvents().valueAt(category.getEvents().size() - 1).getStop() == null
                && category.getEvents().valueAt(category.getEvents().size() - 1).getStart() != null;

        holder.itemView.setBackgroundColor(getColor(category.getTheme().getWindowBackgroundColor()));
        binding.categoryTitle.setTextColor(getColor(category.getTheme().getTextPrimaryColor()));
        binding.categoryTitle.setBackgroundColor(getColor(category.getTheme().getPrimaryColor()));
        binding.buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClick(v, holder.getAdapterPosition(), isCounting);
            }
        });
        if (category.getEvents().size() > 0) {
            String start = category.getEvents().valueAt(category.getEvents().size() - 1).getStart();
            String stop = category.getEvents().valueAt(category.getEvents().size() - 1).getStop();
            binding.categoryIcon.setText(String.format("Eventos: %s\n\nInício: %s\n\nFim: %s\n",
                    category.getEvents().size(), getFormattedDate(start), getFormattedDate(stop)));
        }

        if (isCounting) {
            binding.buttonStatus.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            binding.buttonStatus.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getFormattedDate(String timeMillis) {
        if (timeMillis != null && !timeMillis.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return sdf.format(new Date(Long.parseLong(timeMillis)));
        }
        return timeMillis;
    }

    @Override
    public long getItemId(int position) {
        return mCategories.get(position).getName().hashCode();
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public Category getItem(int position) {
        return mCategories.get(position);
    }

    /**
     * @see android.support.v7.widget.RecyclerView.Adapter#notifyItemChanged(int)
     * @param id Id of changed category.
     */
    public final void notifyItemChanged(String id) {
        this.updateCategories(mActivity);
        notifyItemChanged(getItemPositionById(id));
    }

    public final void notifyDataChanged() {
        this.updateCategories(mActivity);
        super.notifyDataSetChanged();
    }

    private int getItemPositionById(String id) {
        for (int i = 0; i < mCategories.size(); i++) {
            if (mCategories.get(i).getName().equals(id)) {
                return i;
            }

        }
        return -1;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private void updateCategories(Activity activity) {
        mCategories = AppDatabaseHelper.getCategories(activity, true);
    }

    /**
     * Convenience method for color loading.
     *
     * @param colorRes The resource id of the color to load.
     * @return The loaded color.
     */
    private int getColor(@ColorRes int colorRes) {
        return ContextCompat.getColor(mActivity, colorRes);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemCategoryBinding mBinding;

        ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        ItemCategoryBinding getBinding() {
            return mBinding;
        }
    }
}
