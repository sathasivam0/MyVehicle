package com.example.myvehicle.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvehicle.R;
import com.example.myvehicle.activity.HomeActivity;
import com.example.myvehicle.activity.ViewDetailsActivity;
import com.example.myvehicle.model.AssetsModel;
import com.github.florent37.expansionpanel.ExpansionLayout;
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection;
import com.github.florent37.expansionpanel.viewgroup.ExpansionsViewGroupLinearLayout;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.SubCategoryHolder> {
    private Context context;
    private ArrayList<AssetsModel> assetsModelArrayList;

    public SubCategoryAdapter(Context context, ArrayList<AssetsModel> assetsModelArrayList) {
        this.context = context;
        this.assetsModelArrayList = assetsModelArrayList;

    }

    @NonNull
    @Override
    public SubCategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sub_category_layout, parent, false);
        return new SubCategoryHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SubCategoryHolder holder, int position) {
        try {
            AssetsModel model = assetsModelArrayList.get(position);
            holder.tvNameEng.setText(model.getName().getJSONObject(0).getString("value"));
            holder.tvNameHind.setText(model.getName().getJSONObject(1).getString("value"));
            holder.tvDesc.setText(model.getDescription());

            if (position == 0) {
                holder.view.setVisibility(View.GONE);
            } else {
                holder.view.setVisibility(View.VISIBLE);
            }

            holder.rlSubCatHeader.setOnClickListener(view -> {
                if (holder.llSubCategory.getVisibility() == View.GONE) {
                    holder.llSubCategory.setVisibility(View.VISIBLE);

                    if (context instanceof ViewDetailsActivity) {
                        ((ViewDetailsActivity) context).showSubCategoryList(model.getCategoryId(), holder);
                    } else {
                        ((HomeActivity) context).showSuperSubCategoryList(model.getCategoryId(), holder);
                    }

                } else {
                    holder.llSubCategory.setVisibility(View.GONE);
                }
            });

            if (context instanceof ViewDetailsActivity) {
                holder.tvTitle.setText("Sub Category");
                holder.llSubCategoryContainer.setBackgroundColor(ContextCompat.getColor(context,R.color.sub_color));
            } else {
                holder.tvTitle.setText("Super Category");
                holder.llSubCategoryContainer.setBackgroundColor(ContextCompat.getColor(context,R.color.super_color));
            }

            holder.tvTitle.setOnClickListener(view -> {
                if (holder.llSubCategory.getVisibility() == View.GONE) {

                    holder.llSubCategory.setVisibility(View.VISIBLE);
                    if (context instanceof ViewDetailsActivity) {
                        ((ViewDetailsActivity) context).showSubCategoryList(model.getCategoryId(), holder);
                    } else {
                        ((HomeActivity) context).showSuperSubCategoryList(model.getCategoryId(), holder);
                    }
                } else {
                    holder.llSubCategory.setVisibility(View.GONE);
                }
            });

            holder.imageView.setOnClickListener(view -> {
                if (holder.llSubCategory.getVisibility() == View.GONE) {
                    holder.llSubCategory.setVisibility(View.VISIBLE);
                    if (context instanceof ViewDetailsActivity) {
                        ((ViewDetailsActivity) context).showSubCategoryList(model.getCategoryId(), holder);
                    } else {
                        ((HomeActivity) context).showSuperSubCategoryList(model.getCategoryId(), holder);
                    }
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

    public class SubCategoryHolder extends RecyclerView.ViewHolder {

        private TextView tvNameEng, tvNameHind, tvDesc;
        private View view;

        public final TextView tvTitle;
        private RelativeLayout rlSubCatHeader;
        public LinearLayout llSubCategory;
        public LinearLayout llSubCategoryContainer;
        public RecyclerView rcViewHomeExpansion;
        private AppCompatImageView imageView;

        public SubCategoryHolder(@NonNull View itemView) {
            super(itemView);
            tvNameEng = itemView.findViewById(R.id.tv_holder_sub_name_one);
            tvNameHind = itemView.findViewById(R.id.tv_holder_sub_name_two);
            tvDesc = itemView.findViewById(R.id.tv_holder_sub_desc);
            view = itemView.findViewById(R.id.view_holder_bar);

            tvTitle = itemView.findViewById(R.id.tv_holder_sub_title);
            rlSubCatHeader = itemView.findViewById(R.id.rl_holder_sub_sub);
            llSubCategory = itemView.findViewById(R.id.subExpansion);
            llSubCategoryContainer = itemView.findViewById(R.id.ll_holder_sub_category);

            imageView = itemView.findViewById(R.id.headerIndicator3);

            rcViewHomeExpansion = itemView.findViewById(R.id.rc_view_sub_category_list1);

        }
    }
}
