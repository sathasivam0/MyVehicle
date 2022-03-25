package com.example.myvehicle.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvehicle.R;
import com.example.myvehicle.activity.AddCategoryActivity;
import com.example.myvehicle.activity.HomeActivity;
import com.example.myvehicle.activity.ViewDetailsActivity;
import com.example.myvehicle.model.AssetsModel;
import com.github.florent37.expansionpanel.ExpansionLayout;
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection;
import com.marcoscg.materialtoast.MaterialToast;

import java.util.ArrayList;

public class AssetsAdapter extends RecyclerView.Adapter<AssetsAdapter.AssetsHolder> implements Filterable {
    private final Context context;
    private ArrayList<AssetsModel> assetsModelArrayList;
    private final ArrayList<AssetsModel> mArrayList;
    private final TextView tvNoRecord;

    public AssetsAdapter(Context context, ArrayList<AssetsModel> assetsModelArrayList, TextView tvCategoryNoRecord) {
        this.context = context;
        this.assetsModelArrayList = assetsModelArrayList;
        this.mArrayList = assetsModelArrayList;
        this.tvNoRecord = tvCategoryNoRecord;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                try {
                    String charString = constraint.toString();
                    if (charString.isEmpty()) {
                        assetsModelArrayList = mArrayList;
                    } else {
                        ArrayList<AssetsModel> filteredList = new ArrayList<>();
                        for (AssetsModel inventory : mArrayList) {
                            if (inventory.getName().getJSONObject(0).getString("value").toLowerCase().contains(charString)) {
                                // to filter doctors with name
                                filteredList.add(inventory);
                            } else if (inventory.getName().getJSONObject(1).getString("value").toLowerCase().contains(charString)) {
                                // to filter doctors with mobile number
                                filteredList.add(inventory);
                            }
                        }
                        assetsModelArrayList = filteredList;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = assetsModelArrayList;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                try {
                    assetsModelArrayList = (ArrayList<AssetsModel>) results.values;
                    if (assetsModelArrayList.size() > 0) {
                        tvNoRecord.setVisibility(View.GONE);
                    } else {
                        tvNoRecord.setVisibility(View.VISIBLE);
                    }
                    notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @NonNull
    @Override
    public AssetsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_home_item_layout, parent, false);
        return new AssetsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetsHolder holder, int position) {
        try {

            AssetsModel model = assetsModelArrayList.get(position);
            String aNameOne = model.getName().getJSONObject(0).getString("value");
            String aNameTwo = model.getName().getJSONObject(1).getString("value");
            String aDesc = model.getDescription();

            holder.tvNameOne.setText(aNameOne);
            holder.tvNameTwo.setText(aNameTwo);
            holder.tvDesc.setText(aDesc);

            holder.itemView.setOnClickListener(view -> {
                try {

                    Intent intent = new Intent(context, ViewDetailsActivity.class);
                    intent.putExtra("document_id", model.getDoc_id());
                    context.startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            holder.rlSubCatHeader.setOnClickListener(view -> {
                if (holder.llSubCategory.getVisibility() == View.GONE) {
                    holder.llSubCategory.setVisibility(View.VISIBLE);

                    ((HomeActivity) context).showSubCategoryList(model.getCategoryId(), holder);

                } else {
                    holder.llSubCategory.setVisibility(View.GONE);
                }
            });

            holder.tvTitle.setOnClickListener(view -> {
                if (holder.llSubCategory.getVisibility() == View.GONE) {
                    holder.llSubCategory.setVisibility(View.VISIBLE);

                    ((HomeActivity) context).showSubCategoryList(model.getCategoryId(), holder);
                } else {
                    holder.llSubCategory.setVisibility(View.GONE);
                }
            });

            holder.imageView.setOnClickListener(view -> {
                if (holder.llSubCategory.getVisibility() == View.GONE) {
                    holder.llSubCategory.setVisibility(View.VISIBLE);
                    ((HomeActivity) context).showSubCategoryList(model.getCategoryId(), holder);
                } else {
                    holder.llSubCategory.setVisibility(View.GONE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return assetsModelArrayList.size();
    }

    public class AssetsHolder extends RecyclerView.ViewHolder {
        private final TextView tvNameOne;
        private final TextView tvNameTwo;
        private final TextView tvDesc;

        public final TextView tvTitle;
        private RelativeLayout rlSubCatHeader;
        public LinearLayout llSubCategory;
        public RecyclerView rcViewHomeExpansion;
        private AppCompatImageView imageView;


        public AssetsHolder(@NonNull View itemView) {
            super(itemView);
            tvNameOne = itemView.findViewById(R.id.tv_holder_name_one);
            tvNameTwo = itemView.findViewById(R.id.tv_holder_name_two);
            tvDesc = itemView.findViewById(R.id.tv_holder_description);

            tvTitle = itemView.findViewById(R.id.tv_holder_home_title);
            rlSubCatHeader = itemView.findViewById(R.id.rl_holder_home_sub);
            llSubCategory = itemView.findViewById(R.id.mainExpansion);

            imageView = itemView.findViewById(R.id.headerIndicator2);

            rcViewHomeExpansion = itemView.findViewById(R.id.rc_view_home_category_list);

        }
    }
}
