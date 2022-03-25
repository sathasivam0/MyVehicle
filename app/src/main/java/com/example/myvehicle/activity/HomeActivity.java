package com.example.myvehicle.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvehicle.R;
import com.example.myvehicle.adapter.AssetsAdapter;
import com.example.myvehicle.adapter.SubCategoryAdapter;
import com.example.myvehicle.databinding.ActivityHomeBinding;
import com.example.myvehicle.model.AssetsModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.marcoscg.materialtoast.MaterialToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityHomeBinding binding;
    private ArrayList<AssetsModel> assetsModelArrayList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AssetsAdapter adapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(LayoutInflater.from(HomeActivity.this));
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setTitle("View Category List");
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            loadDataFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        try {
            Query query = db.collection("Vehicle").whereEqualTo("parentID", "0");
            progressDialog = ProgressDialog.show(HomeActivity.this,
                    "Please wait",
                    "Loading...");
            db.collection("Vehicle").orderBy("slug", Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
                try {
                    if (queryDocumentSnapshots.isEmpty()) {
                        progressDialog.dismiss();
                        getValuesFromAssets();
                    } else {
                        progressDialog.dismiss();
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
                            if (aParentId.equals("0")) {
                                assetsModelArrayList.add(model);
                                //                            getSubCategories(aCategoryId);
                            }
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
            adapter = new AssetsAdapter(HomeActivity.this, assetsModelArrayList, binding.tvCategoryNoRecord);
            binding.rcViewAssertList.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
            binding.rcViewAssertList.setHasFixedSize(true);
            binding.rcViewAssertList.setAdapter(adapter);
            setOnClickListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setOnClickListener() {
        try {
            binding.fabAddVehicles.setOnClickListener(this);
            binding.rcViewAssertList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    // if the recycler view is scrolled
                    // above hide the FAB
                    if (dy > 10 && binding.fabAddVehicles.isShown()) {
                        binding.fabAddVehicles.hide();
                    }

                    // if the recycler view is
                    // scrolled above show the FAB
                    if (dy < -10 && !binding.fabAddVehicles.isShown()) {
                        binding.fabAddVehicles.show();
                    }

                    // of the recycler view is at the first
                    // item always show the FAB
                    if (!recyclerView.canScrollVertically(-1)) {
                        binding.fabAddVehicles.show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_add_vehicles) {
            try {
                Intent intent = new Intent(HomeActivity.this, AddCategoryActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
        return true;
    }

    private void getValuesFromAssets() {
        try {
            JSONObject aDataObj = new JSONObject(readJSON());
            JSONArray jsonArray = aDataObj.getJSONArray("data");
            if (jsonArray.length() > 0) {
                assetsModelArrayList = new ArrayList<>();
                for (int a = 0; a < jsonArray.length(); a++) {
                    AssetsModel model = new AssetsModel();
                    JSONObject object = jsonArray.getJSONObject(a);

                    String aCategoryId = object.getString("categoryId");
                    JSONArray aNameAry = object.getJSONArray("name");
                    String aSlug = object.getString("slug");
                    String aDesc = object.getString("description");
                    String aParentId = object.getString("parentID");
                    int aType = object.getInt("type");
                    String aAttributeSet = object.getString("attributeSet");
                    int aCategoryNumber = object.getInt("categoryNumber");
                    int aLevel = object.getInt("level");
                    boolean aFeature = object.getBoolean("featured");
                    String aIcon = object.getString("icon");
                    boolean aStatus = object.getBoolean("status");
                    String aCreatedDate = object.getString("create_date");

                    model.setCategoryId(aCategoryId);
                    model.setName(aNameAry);
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

                    addDetailsToDatabase(model, a);
                }
//                setAdapter();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void addDetailsToDatabase(AssetsModel model, int a) {
        try {
            HashMap<String, Object> ledgerMap = new HashMap<>();
            ledgerMap.put("categoryId", model.getCategoryId());
            ledgerMap.put("name", model.getName().toString());
            ledgerMap.put("slug", model.getSlug());
            ledgerMap.put("description", model.getDescription());
            ledgerMap.put("parentID", model.getParentID());
            ledgerMap.put("type", model.getType());
            ledgerMap.put("attributeSet", model.getAttributeSet());
            ledgerMap.put("categoryNumber", model.getCategoryNumber());
            ledgerMap.put("level", model.getLevel());
            ledgerMap.put("featured", model.isFeatured());
            ledgerMap.put("icon", model.getIcon());
            ledgerMap.put("status", model.isStatus());
            ledgerMap.put("create_date", model.getCreate_date());

            db.collection("Vehicle").document().set(ledgerMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    System.out.println("VIEW ALL PROPERTIES OF LEDGER HAS BEEN ADDED: " + a);
                }
            }).addOnFailureListener(e -> {
                String aError = e.getMessage();
                System.out.println("VIEW ALL PROPERTIES OF LEDGER HAS BEEN ADDED ERROR: " + aError);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readJSON() {
        String json = null;
        try {
            // Opening data.json file
            InputStream inputStream = HomeActivity.this.getAssets().open("test_category.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            // read values in the byte array
            inputStream.read(buffer);
            inputStream.close();
            // convert byte to string
            json = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return json;
        }
        return json;
    }

    public void showSubCategoryList(String categoryId, AssetsAdapter.AssetsHolder holder) {
        try {
            Query query = db.collection("Vehicle").whereEqualTo("parentID", categoryId).whereEqualTo("level", 1);
            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                try {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        ArrayList<AssetsModel> assetsModelArrayListForSubCategory = new ArrayList<>();
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

                            assetsModelArrayListForSubCategory.add(model);

                        }
                        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(HomeActivity.this, assetsModelArrayListForSubCategory);
                        holder.rcViewHomeExpansion.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                        holder.rcViewHomeExpansion.setHasFixedSize(true);
                        holder.rcViewHomeExpansion.setAdapter(subCategoryAdapter);
                    } else {
                        holder.llSubCategory.setVisibility(View.GONE);
                        MaterialToast.makeText(HomeActivity.this,"No sub categories available...", Toast.LENGTH_SHORT).setBackgroundColor(Color.BLACK).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    public void showSuperSubCategoryList(String categoryId, SubCategoryAdapter.SubCategoryHolder holder) {
        try {
            holder.tvTitle.setText("Super Category");
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

                        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(HomeActivity.this, assetsModelArrayListSuperSub);
                        holder.rcViewHomeExpansion.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                        holder.rcViewHomeExpansion.setHasFixedSize(true);
                        holder.rcViewHomeExpansion.setAdapter(subCategoryAdapter);
                    } else {
                        holder.llSubCategory.setVisibility(View.GONE);
                        MaterialToast.makeText(HomeActivity.this,"No sub categories available...", Toast.LENGTH_SHORT).setBackgroundColor(Color.BLACK).show();
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