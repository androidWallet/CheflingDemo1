package com.mguru.cheflingtest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mguru.adapter.RecipeTypeAdapter;
import com.mguru.adapter.ServesAdapter;
import com.mguru.utils.BlurBuilder;
import com.mguru.utils.Constant;
import com.mguru.utils.CustomTimePickerDialog;
import com.mguru.utils.MethodUtils;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private CustomTimePickerDialog customTimePickerDialog;
    private ImageView recipeImageView, cameraIconImageView;
    private EditText recipeNameEditText, notesEditText;
    private TextView recipeTypeTextView, beginnerTextView, sousChefTextView, masterTextView;
    private LinearLayout cookingTimeGroupLayout, servesGroupLayout, nextButtonGroupLayout;
    private RelativeLayout viewGroupLayout;
    private Typeface typeface;
    private String[] recipeTypeArray;
    private String[] servesArray;
    private int position;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 101;
    private Uri fileUri;
    private boolean isAllPermissionGranted;
    private String imageLocation, recipeName, recipeType, servesValue, cookingTime;

    /**
     * Created by Sk Maniruddin on 23-10-2016.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Get Resource ID from XML
         */
        recipeImageView = (ImageView) findViewById(R.id.recipe_imageview);
        cameraIconImageView = (ImageView) findViewById(R.id.camera_icon_imageview);
        recipeNameEditText = (EditText) findViewById(R.id.recipe_name_edittext);
        notesEditText = (EditText) findViewById(R.id.notes_edittext);
        recipeTypeTextView = (TextView) findViewById(R.id.recipe_type_textview);
        beginnerTextView = (TextView) findViewById(R.id.beginner_textview);
        sousChefTextView = (TextView) findViewById(R.id.sous_chef_textview);
        masterTextView = (TextView) findViewById(R.id.master_textview);
        cookingTimeGroupLayout = (LinearLayout) findViewById(R.id.cooking_time_group_layout);
        servesGroupLayout = (LinearLayout) findViewById(R.id.serves_group_layout);
        nextButtonGroupLayout = (LinearLayout) findViewById(R.id.next_button_group_layout);
        viewGroupLayout = (RelativeLayout) findViewById(R.id.view_group_layout);

        /**
         * Apply font
         */
        typeface = Typeface.createFromAsset(getAssets(), "fonts/helvetica_neue_lt_com_55_roman.ttf");
        changeTypeface(viewGroupLayout);

        /**
         * Initialize Time Picker Dialog
         */
        customTimePickerDialog = new CustomTimePickerDialog(this, onTimeSetListener, 12, 15, false);
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_1);
        setRecipeImage(originalBitmap);

        /**
         * Click Listener
         */
        cameraIconImageView.setOnClickListener(this);
        recipeTypeTextView.setOnClickListener(this);
        beginnerTextView.setOnClickListener(this);
        sousChefTextView.setOnClickListener(this);
        masterTextView.setOnClickListener(this);
        servesGroupLayout.setOnClickListener(this);
        cookingTimeGroupLayout.setOnClickListener(this);
        nextButtonGroupLayout.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * Take the permission from the user
         */
        permissionTask();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.camera_icon_imageview:
                if (isAllPermissionGranted) {
                    cameraDialog();
                } else {
                    permissionTask();
                }
                break;
            case R.id.recipe_type_textview:
                recipeTypeListDialog();
                break;
            case R.id.beginner_textview:
                updateDifficultyLevel(true, false, false);
                break;
            case R.id.sous_chef_textview:
                updateDifficultyLevel(false, true, false);
                break;
            case R.id.master_textview:
                updateDifficultyLevel(false, false, true);
                break;
            case R.id.serves_group_layout:
                servesDialog();
                break;
            case R.id.cooking_time_group_layout:
                customTimePickerDialog.show();
                break;
            case R.id.next_button_group_layout:
                recipeName = recipeNameEditText.getText().toString();
                if (checkValidation()) {
                    feedbackDialog();
                }
                break;
        }
    }

    /**
     * Cooking timer
     */
    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            cookingTime = hourOfDay + ":" + minute;
            Toast.makeText(MainActivity.this, "You have selected the time " + hourOfDay + ":" + minute, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Receipe Type Dialog
     */
    private void recipeTypeListDialog() {
        final Dialog recipeTypeDialog = new Dialog(MainActivity.this);
        recipeTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        recipeTypeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        recipeTypeDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        recipeTypeDialog.setContentView(R.layout.recipe_type_dialog);
        recipeTypeDialog.show();

        ListView recipeTypeListView = (ListView) recipeTypeDialog.findViewById(R.id.recipe_type_listview);
        TextView cancelButton = (TextView) recipeTypeDialog.findViewById(R.id.cancel_button);
        TextView doneButton = (TextView) recipeTypeDialog.findViewById(R.id.done_button);

        cancelButton.setTypeface(typeface);
        doneButton.setTypeface(typeface);

        recipeTypeArray = getResources().getStringArray(R.array.recipe_type_item_array);

        RecipeTypeAdapter recipeTypeAdapter = new RecipeTypeAdapter(MainActivity.this, recipeTypeArray);
        recipeTypeListView.setAdapter(recipeTypeAdapter);

        recipeTypeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                position = i;
                for (int j = 0; j < adapterView.getChildCount(); j++)
                    adapterView.getChildAt(j).setBackgroundColor(Color.parseColor("#FFFFFF"));
                view.setBackgroundColor(Color.parseColor("#85bb38"));
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipeTypeDialog.dismiss();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipeTypeDialog.dismiss();
                recipeTypeTextView.setText(recipeTypeArray[position]);
                recipeType = recipeTypeArray[position];
            }
        });
    }

    /**
     * Serves Dialog
     */
    private void servesDialog() {
        final Dialog servesDialog = new Dialog(MainActivity.this);
        servesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        servesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        servesDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        servesDialog.setContentView(R.layout.serves_dialog);
        servesDialog.show();

        ListView serveTypeListView = (ListView) servesDialog.findViewById(R.id.serves_listview);
        TextView cancelButton = (TextView) servesDialog.findViewById(R.id.cancel_button);
        TextView doneButton = (TextView) servesDialog.findViewById(R.id.done_button);

        cancelButton.setTypeface(typeface);
        doneButton.setTypeface(typeface);

        servesArray = getResources().getStringArray(R.array.serves_item_array);

        ServesAdapter servesAdapter = new ServesAdapter(MainActivity.this, servesArray);
        serveTypeListView.setAdapter(servesAdapter);

        serveTypeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                position = i;
                for (int j = 0; j < adapterView.getChildCount(); j++)
                    adapterView.getChildAt(j).setBackgroundColor(Color.parseColor("#FFFFFF"));
                view.setBackgroundColor(Color.parseColor("#85bb38"));
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                servesDialog.dismiss();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                servesDialog.dismiss();
                servesValue = servesArray[position];
                Toast.makeText(MainActivity.this, "You have selected the value " + servesArray[position], Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Feedback Dialog
     */
    private void feedbackDialog() {
        final Dialog feedbackDialog = new Dialog(MainActivity.this);
        feedbackDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        feedbackDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        feedbackDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        feedbackDialog.setContentView(R.layout.feedback_dialog);
        feedbackDialog.show();

        TextView cancelButton = (TextView) feedbackDialog.findViewById(R.id.cancel_button);
        TextView sendButton = (TextView) feedbackDialog.findViewById(R.id.send_button);

        cancelButton.setTypeface(typeface);
        sendButton.setTypeface(typeface);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedbackDialog.dismiss();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedbackDialog.dismiss();
            }
        });

    }

    /**
     * Camera Dialog
     */
    private void cameraDialog() {
        final Dialog cameraDialog = new Dialog(MainActivity.this);
        cameraDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cameraDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cameraDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cameraDialog.setContentView(R.layout.camera_dialog);
        cameraDialog.show();

        TextView galleryButton = (TextView) cameraDialog.findViewById(R.id.gallery_button);
        TextView cameraButton = (TextView) cameraDialog.findViewById(R.id.camera_button);

        galleryButton.setTypeface(typeface);
        cameraButton.setTypeface(typeface);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraDialog.dismiss();
                takeFromGallery();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraDialog.dismiss();
                captureImage();
            }
        });

    }

    /**
     * Capturing Camera Image will lauch camera app requrest image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Creating file uri to store image/video
     */
    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private static File getOutputMediaFile() {
        File mediaFile = null;
        try {
            mediaFile = new File(Constant.picLocation + File.separator + "recipe.png");
            if (!mediaFile.exists()) {
                mediaFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaFile;
    }

    /*
   * Capturing Image from gallery
   */
    private void takeFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                previewCapturedImage();
            } else if (requestCode == GALLERY_IMAGE_REQUEST_CODE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    String path = getPathFromURI(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    setRecipeImage(bitmap);
                    imageLocation = path;
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(MainActivity.this, "User cancelled image capture.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Sorry! Failed to capture image.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    * Display image from a path to ImageView
    */
    private void previewCapturedImage() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
            setRecipeImage(bitmap);
            MethodUtils.saveToInternalStorage(bitmap);
            imageLocation = fileUri.getPath();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /* Get the real path from the URI */
    private String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    /**
     * Method used for set the recipe image taken by user
     *
     * @param bitmap
     */
    private void setRecipeImage(Bitmap bitmap) {
        if (bitmap != null) {
            Bitmap blurredBitmap = BlurBuilder.blur(MainActivity.this, bitmap);
            recipeImageView.setBackgroundDrawable(new BitmapDrawable(getResources(), blurredBitmap));
        }
    }

    /**
     * Method used for update the difficulty level
     *
     * @param flag1
     * @param flag2
     * @param flag3
     */
    private void updateDifficultyLevel(boolean flag1, boolean flag2, boolean flag3) {
        if (flag1) {
            beginnerTextView.setBackgroundResource(R.drawable.rounded_corner_selected);
        } else {
            beginnerTextView.setBackgroundResource(R.drawable.rounded_corner_unselected);
        }

        if (flag2) {
            sousChefTextView.setBackgroundResource(R.drawable.rounded_corner_selected);
        } else {
            sousChefTextView.setBackgroundResource(R.drawable.rounded_corner_unselected);
        }

        if (flag3) {
            masterTextView.setBackgroundResource(R.drawable.rounded_corner_selected);
        } else {
            masterTextView.setBackgroundResource(R.drawable.rounded_corner_unselected);
        }
    }


    /**
     * Method used for apply the font
     *
     * @param vGroup
     */
    private void changeTypeface(ViewGroup vGroup) {
        for (int i = 0; i < vGroup.getChildCount(); i++) {
            View v = vGroup.getChildAt(i);
            if (v instanceof ImageView || v instanceof ImageButton || v instanceof ListView)
                continue;
            if (v instanceof TextView) {
                ((TextView) v).setTypeface(typeface);
            } else if (v instanceof RadioButton) {
                ((RadioButton) v).setTypeface(typeface);
            } else if (v instanceof DrawerLayout || v instanceof FrameLayout
                    || v instanceof LinearLayout || v instanceof RelativeLayout
                    || v instanceof RadioGroup || v instanceof ViewGroup) {
                changeTypeface((ViewGroup) v);
            }
        }
    }

    private boolean checkValidation() {
        if (null == imageLocation) {
            Toast.makeText(MainActivity.this, "Please select recipe image.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (recipeName.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please type the recipe name.", Toast.LENGTH_SHORT).show();
            recipeNameEditText.requestFocus();
            return false;
        } else if (null == recipeType) {
            Toast.makeText(MainActivity.this, "Please choose the recipe type.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (null == servesValue) {
            Toast.makeText(MainActivity.this, "Please select serves value.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (null == cookingTime) {
            Toast.makeText(MainActivity.this, "Please select cooking time.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    /**
     * Taken permission from the user
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @AfterPermissionGranted(100)
    private void permissionTask() {
        isAllPermissionGranted = false;
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {
            isAllPermissionGranted = true;
        } else {
            EasyPermissions.requestPermissions(this, "Application needs permission to access your storage", 100, perms);
        }
    }
}
