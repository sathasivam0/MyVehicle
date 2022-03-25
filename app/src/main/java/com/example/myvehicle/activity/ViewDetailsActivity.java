package com.example.myvehicle.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myvehicle.R;
import com.example.myvehicle.adapter.SubCategoryAdapter;
import com.example.myvehicle.databinding.ActivityViewDetailsBinding;
import com.example.myvehicle.model.AssetsModel;
import com.github.florent37.expansionpanel.ExpansionLayout;
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marcoscg.materialtoast.MaterialToast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class ViewDetailsActivity extends AppCompatActivity {

    private ActivityViewDetailsBinding binding;
    private String myDocumentId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<AssetsModel> assetsModelArrayList;
    final ExpansionLayoutCollection expansionLayoutCollection = new ExpansionLayoutCollection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewDetailsBinding.inflate(LayoutInflater.from(ViewDetailsActivity.this));
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setTitle("View Category Details");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        myDocumentId = getIntent().getStringExtra("document_id");

        getDocumentDetailsFromDb();
    }

    private void getDocumentDetailsFromDb() {
        try {
            db.collection("Vehicle").document(myDocumentId).get().addOnSuccessListener(documentSnapshot -> {
                try {
                    if (documentSnapshot.exists()) {

                        String aCategoryId = documentSnapshot.getString("categoryId");
                        JSONArray aNamesAry = new JSONArray(documentSnapshot.getString("name"));
                        String aSlug = documentSnapshot.getString("slug");
                        String aDesc = documentSnapshot.getString("description");
                        String aParentId = documentSnapshot.getString("parentID");
                        int aType = documentSnapshot.getLong("type").intValue();
                        String aAttributeSet = documentSnapshot.getString("attributeSet");
                        int aCategoryNumber = documentSnapshot.getLong("categoryNumber").intValue();
                        int aLevel = documentSnapshot.getLong("level").intValue();
                        boolean aFeature = documentSnapshot.getBoolean("featured");
                        String aIcon = documentSnapshot.getString("icon");
                        boolean aStatus = documentSnapshot.getBoolean("status");
                        String aCreatedDate = documentSnapshot.getString("create_date");

                        String aImage = documentSnapshot.getString("image");

                        if (aImage != null) {
                            Glide.with(ViewDetailsActivity.this).load(aImage).into(binding.ivViewNameImage);
                        }

                        String aNameEnglish = aNamesAry.getJSONObject(0).getString("value");
                        String aNameHindi = aNamesAry.getJSONObject(1).getString("value");

                        binding.tvViewNameEnglish.setText(aNameEnglish);
                        binding.tvViewNameHindi.setText(aNameHindi);
                        binding.tvViewNameDescription.setText(aDesc);

                        if (aLevel == 0) {
                            getSubCategories(aCategoryId);
                            binding.llViewSubCategory.setVisibility(View.VISIBLE);
                        } else {
                            binding.llViewSubCategory.setVisibility(View.GONE);
                        }

                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getSubCategories(String categoryId) {
        try {
            Query query = db.collection("Vehicle").whereEqualTo("parentID",categoryId).whereEqualTo("level",1);
            query.orderBy("slug", Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
                try {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        assetsModelArrayList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                            AssetsModel model = new AssetsModel();
                            String aCategoryId = documentSnapshot.getString("categoryId");
                            JSONArray aNamesAry = new JSONArray(documentSnapshot.getString("name"));
                            String aSlug = documentSnapshot.getString("slug");
                            String aDesc = documentSnapshot.getString("description");
                            String aParentId = documentSnapshot.getString("parentID");
                            int aType = documentSnapshot.getLong("type").intValue();
                            String aAttributeSet = documentSnapshot.getString("attributeSet");
                            int aCategoryNumber = documentSnapshot.getLong("categoryNumber").intValue();
                            int aLevel = documentSnapshot.getLong("level").intValue();
                            boolean aFeature = documentSnapshot.getBoolean("featured");
                            String aIcon = documentSnapshot.getString("icon");
                            boolean aStatus = documentSnapshot.getBoolean("status");
                            String aCreatedDate = documentSnapshot.getString("create_date");

                            String aNameEnglish = aNamesAry.getJSONObject(0).getString("value");
                            String aNameHindi = aNamesAry.getJSONObject(1).getString("value");

                            model.setCategoryId(aCategoryId);
                            model.setName(aNamesAry);
                            model.setSlug(aSlug);
                            model.setDescription(aDesc);
                            model.setParentID(aParentId);
                            model.setType(aType);
                            model.setAttributeSet(aAttributeSet);
                            model.setCategoryNumber(aCategoryNumber);
                            model.setLevel(aLevel);
                            model.setFeatured(aFeature);
                            model.setIcon(aIcon);
                            model.setStatus(aStatus);
                            model.setCreate_date(aCreatedDate);
                            model.setDoc_id(documentSnapshot.getId());

                            assetsModelArrayList.add(model);

                        }
                        setAdapter();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAdapter() {
        try {

            expansionLayoutCollection.add(binding.todayExpansion);
            expansionLayoutCollection.openOnlyOne(true);

            SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(ViewDetailsActivity.this,assetsModelArrayList);
            binding.rcViewSubCategoryList.setLayoutManager(new LinearLayoutManager(ViewDetailsActivity.this));
            binding.rcViewSubCategoryList.setHasFixedSize(true);
            binding.rcViewSubCategoryList.setAdapter(subCategoryAdapter);

            binding.todayExpansion.addListener((expansionLayout, expanded) -> {
                if (expanded) {
                    binding.rcViewSubCategoryList.setVisibility(View.VISIBLE);
                } else {
                    binding.rcViewSubCategoryList.setVisibility(View.GONE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showSubCategoryList(String categoryId, SubCategoryAdapter.SubCategoryHolder holder) {
        try {
            Query query = db.collection("Vehicle").whereEqualTo("parentID", categoryId).whereEqualTo("level", 2);
            query.orderBy("slug", Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
                try {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        ArrayList<AssetsModel> assetsModelArrayListSuperSub = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            AssetsModel model = new AssetsModel();
                            String aCategoryId = documentSnapshot.getString("categoryId");
                            JSONArray aNamesAry = new JSONArray(documentSnapshot.getString("name"));
                            String aSlug = documentSnapshot.getString("slug");
                            String aDesc = documentSnapshot.getString("description");
                            String aParentId = documentSnapshot.getString("parentID");
                            int aType = documentSnapshot.getLong("type").intValue();
                            String aAttributeSet = documentSnapshot.getString("attributeSet");
                            int aCategoryNumber = documentSnapshot.getLong("categoryNumber").intValue();
                            int aLevel = documentSnapshot.getLong("level").intValue();
                            boolean aFeature = documentSnapshot.getBoolean("featured");
                            String aIcon = documentSnapshot.getString("icon");
                            boolean aStatus = documentSnapshot.getBoolean("status");
                            String aCreatedDate = documentSnapshot.getString("create_date");

                            String aNameEnglish = aNamesAry.getJSONObject(0).getString("value");
                            String aNameHindi = aNamesAry.getJSONObject(1).getString("value");

                            System.out.println("VIEW DOCUMENT SUB CATEGORY LIST NAME: " + aNameEnglish);

                            model.setCategoryId(aCategoryId);
                            model.setName(aNamesAry);
                            model.setSlug(aSlug);
                            model.setDescription(aDesc);
                            model.setParentID(aParentId);
                            model.setType(aType);
                            model.setAttributeSet(aAttributeSet);
                            model.setCategoryNumber(aCategoryNumber);
                            model.setLevel(aLevel);
                            model.setFeatured(aFeature);
                            model.setIcon(aIcon);
                            model.setStatus(aStatus);
                            model.setCreate_date(aCreatedDate);
                            model.setDoc_id(documentSnapshot.getId());

                            assetsModelArrayListSuperSub.add(model);
                        }

                        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(ViewDetailsActivity.this, assetsModelArrayListSuperSub);
                        holder.rcViewHomeExpansion.setLayoutManager(new LinearLayoutManager(ViewDetailsActivity.this));
                        holder.rcViewHomeExpansion.setHasFixedSize(true);
                        holder.rcViewHomeExpansion.setAdapter(subCategoryAdapter);
                    } else {
                        holder.llSubCategory.setVisibility(View.GONE);
                        MaterialToast.makeText(ViewDetailsActivity.this,"No sub categories available...", Toast.LENGTH_SHORT).setBackgroundColor(Color.BLACK).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}