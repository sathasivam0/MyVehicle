package com.example.myvehicle.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.myvehicle.Constant.Constant;
import com.example.myvehicle.R;
import com.example.myvehicle.databinding.ActivityAddCategoryBinding;
import com.example.myvehicle.model.AssetsModel;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marcoscg.materialtoast.MaterialToast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class AddCategoryActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityAddCategoryBinding binding;

    private final int PICK_IMAGE_REQUEST_FOR_PROFILE = 71;
    private String downloadProfileImageUrl;
    private StorageReference storageReference;
    private Uri profileFilePath;
    private String myNameEnglish, myNameHindi, myDescription, myCategoryId = "", myParentId, myAttributeSet, mySubCategoryId = "";
    private int myType, myCategoryNumber, myLevel;
    private boolean myStatus = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<AssetsModel> assetsModelArrayListForCategory, assetsModelArrayList;
    private ArrayList<String> myCategoryList;
    private ArrayList<String> mySubCategoryList;
    private ArrayList<String> mySubCategoryIdList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCategoryBinding.inflate(LayoutInflater.from(AddCategoryActivity.this));
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Category");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        storageReference = Constant.storage.getReference();
        getDocumentDetailsFromDb();
    }

    private void getDocumentDetailsFromDb() {
        try {
//            Query query = db.collection("Vehicle").whereEqualTo("parentID","0");
            db.collection("Vehicle").get().addOnSuccessListener(queryDocumentSnapshots -> {
                try {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        hideKeyboard();
                        assetsModelArrayList = new ArrayList<>();
                        assetsModelArrayListForCategory = new ArrayList<>();
                        myCategoryList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String aParentId = documentSnapshot.getString("parentID");
                            String aCategoryId = documentSnapshot.getString("categoryId");

                            AssetsModel model = new AssetsModel();
                            JSONArray aNamesAry = new JSONArray(documentSnapshot.getString("name"));
                            String aSlug = documentSnapshot.getString("slug");
                            String aDesc = documentSnapshot.getString("description");
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

                            assetsModelArrayList.add(model);

                            if (aParentId.equals("0")) {
                                myCategoryList.add(aNamesAry.getJSONObject(0).getString("value"));
                                assetsModelArrayListForCategory.add(model);
                            }
                        }
                        setUpListener();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpListener() {
        try {
            binding.ivAddNameImage.setOnClickListener(this);
            binding.btnVehiclesAdd.setOnClickListener(this);
            binding.etAddCategory.setOnClickListener(this);
            binding.etAddSubCategory.setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_add_name_image) {
            try {
                requestPermissions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (view.getId() == R.id.et_add_category) {
            try {
                openSpinnerDialog(myCategoryList, "Select Category");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (view.getId() == R.id.et_add_sub_category) {
            try {
                if (myCategoryList.size() > 0) {
                    openSpinnerDialog(mySubCategoryList, "Select Sub Category");
                } else {
                    MaterialToast.makeText(AddCategoryActivity.this, "Please select category", Toast.LENGTH_SHORT).setBackgroundColor(Color.BLACK).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (view.getId() == R.id.btn_vehicles_add) {
            try {
                hideKeyboard();
                checkValidation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openSpinnerDialog(ArrayList<String> arrayList, String title) {
        try {
            SpinnerDialog spinnerDialog = new SpinnerDialog(AddCategoryActivity.this, arrayList, title, "Close ");// With No Animation
            spinnerDialog.setCancellable(true); // for cancellable
            spinnerDialog.setShowKeyboard(false);// for open keyboard by default
            spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                @Override
                public void onClick(String item, int position) {
                    try {
                        if (title.equals("Select Category")) {

                            binding.etAddCategory.setText(item);
                            showSubCategoryItems(assetsModelArrayListForCategory.get(position).getCategoryId());
                            myCategoryId = assetsModelArrayListForCategory.get(position).getCategoryId();

                        } else if (title.equals("Select Sub Category")) {
                            binding.etAddSubCategory.setText(item);
                            mySubCategoryId = mySubCategoryIdList.get(position);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            spinnerDialog.showSpinerDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSubCategoryItems(String categoryId) {
        try {
            mySubCategoryList = new ArrayList<>();
            mySubCategoryIdList = new ArrayList<>();
            for (int a = 0; a < assetsModelArrayList.size(); a++) {
                AssetsModel model = assetsModelArrayList.get(a);
                String aParentId = model.getParentID();
                if (aParentId.equals(categoryId)) {
                    mySubCategoryList.add(model.getName().getJSONObject(0).getString("value"));
                    mySubCategoryIdList.add(model.getCategoryId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPermissions() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Allow Permission to Select Image", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST_FOR_PROFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == PICK_IMAGE_REQUEST_FOR_PROFILE) {
                    showImagePickerOptions();
                }
            } else {
                Toast.makeText(this, "Permission Canceled..!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showImagePickerOptions() {
        try {
            ImagePicker.with(this)
                    .crop()                    //Crop image(Optional), Check Customization for more option
                    .compress(1024)            //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                Uri uri = Objects.requireNonNull(data).getData();
                // Use Uri object instead of File to avoid storage permissions
                profileFilePath = uri;
                binding.ivAddNameImage.setImageURI(uri);
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkValidation() {
        try {
            myNameEnglish = Objects.requireNonNull(binding.etAddNameEnglish.getText()).toString();
            myNameHindi = Objects.requireNonNull(binding.etAddNameHindi.getText()).toString();
            myDescription = Objects.requireNonNull(binding.etAddDescription.getText()).toString();
            myStatus = true;

            if (myNameEnglish.length() < 3) {
                binding.etAddNameEnglish.setError("Name must have atleast 3 characters");
                binding.etAddNameEnglish.requestFocus();
            } else if (myNameHindi.length() < 3) {
                binding.etAddNameHindi.setError("Name must have atleast 3 characters");
                binding.etAddNameHindi.requestFocus();
            } else if (myDescription.length() < 4) {
                binding.etAddDescription.setError("Name must have atleast 3 characters");
                binding.etAddDescription.requestFocus();
            } else {
                uploadProfileImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadProfileImage() {
        try {
            if (profileFilePath != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();
                StorageReference ref = storageReference.child("employee_profile_images/" + UUID.randomUUID().toString().substring(0, 6) + ".jpg");
                final UploadTask uploadTask = ref.putFile(profileFilePath);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw Objects.requireNonNull(task.getException());
                                }
                                downloadProfileImageUrl = ref.getDownloadUrl().toString();
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    downloadProfileImageUrl = Objects.requireNonNull(task.getResult()).toString();
                                    progressDialog.dismiss();
                                    Toast.makeText(AddCategoryActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                    addRecordToDb();
                                }
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    }
                });
            } else {
                addRecordToDb();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRecordToDb() {
        try {

            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String aCreatedDate = fmt.format(calendar.getTime()).toString();
            HashMap<String, Object> ledgerMap = new HashMap<>();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("_id", generateRandomPassword(24));
            jsonObject.put("language", "en");
            jsonObject.put("value", myNameEnglish);

            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("_id", generateRandomPassword(24));
            jsonObject1.put("language", "hi");
            jsonObject1.put("value", myNameHindi);

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            jsonArray.put(jsonObject1);

            ledgerMap.put("categoryId", generateRandomPassword(24));
            ledgerMap.put("name", jsonArray.toString());
            ledgerMap.put("image", downloadProfileImageUrl);
            ledgerMap.put("slug", myNameEnglish.toLowerCase());
            ledgerMap.put("description", myDescription);
            ledgerMap.put("attributeSet", generateRandomPassword(24));
            ledgerMap.put("categoryNumber", generateRandomNumber(4));
            ledgerMap.put("featured", binding.chkAddFeatured.isChecked());
            ledgerMap.put("icon", "");
            ledgerMap.put("type", 1);
            ledgerMap.put("status", myStatus);
            ledgerMap.put("create_date", aCreatedDate);

            if (myCategoryId.isEmpty()) {
                ledgerMap.put("parentID", "0");
                ledgerMap.put("level", 0);
            } else if (!myCategoryId.isEmpty() && mySubCategoryId.isEmpty()) {
                ledgerMap.put("parentID", myCategoryId);
                ledgerMap.put("level", 1);
            } else {
                ledgerMap.put("parentID", mySubCategoryId);
                ledgerMap.put("level", 2);
            }


            db.collection("Vehicle").document().set(ledgerMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    System.out.println("VIEW ALL PROPERTIES OF LEDGER HAS BEEN ADDED ");
                    MaterialToast.makeText(AddCategoryActivity.this, "Category is added", Toast.LENGTH_SHORT).setBackgroundColor(Color.BLACK).show();
                    finish();
                }
            }).addOnFailureListener(e -> {
                String aError = e.getMessage();
                System.out.println("VIEW ALL PROPERTIES OF LEDGER HAS BEEN ADDED ERROR: " + aError);
                MaterialToast.makeText(AddCategoryActivity.this, aError, Toast.LENGTH_SHORT).setBackgroundColor(Color.BLACK).show();
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

    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            if (view != null) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String generateRandomPassword(int len) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public static int generateRandomNumber(int len) {
        String chars = "0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return Integer.parseInt(sb.toString());
    }

}