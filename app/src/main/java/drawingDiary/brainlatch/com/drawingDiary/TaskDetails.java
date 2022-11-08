package drawingDiary.brainlatch.com.drawingDiary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static drawingDiary.brainlatch.com.drawingDiary.DatabaseOpenHelper.*;
import static drawingDiary.brainlatch.com.drawingDiary.TaskConstants.*;

/**
 * Created by Luigi on 07/09/2017.
 */
public class TaskDetails extends AppCompatActivity {

    //public static final Integer NUMBER_RESULTS = 30;
    public static final Integer NUMBER_RESULTS_TO_DISPLAY = 10;
    //public static final Integer MAX_IMG_SIZE = 1200000;
    //public static final Integer MIN_IMG_WIDTH = 960;
   //public static final Integer MIN_IMG_HEIGHT = 720;
   //public static final Integer MIN_IMG_SIZE = 1;
    private static final String TAG = TaskDetails.class.getSimpleName();
    private static boolean isFirstLoadFailed = false;
  //static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMG_FROM_GALLERY = 2;



    //define the Typefaces used in the class
    public Typeface tfOpenSansSemiboldItalic;
    public Typeface tfwestchesterRegular;

    private static String urlString;
    private ProgressDialog pDialog;
    private Integer displayedPage = 0;
    private String[]imgUrlsList = new String[NUMBER_RESULTS_TO_DISPLAY];

    private DatabaseOpenHelper promptHelper;
    private Integer shownTips [];

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private ImageButton takePictureButton;
    private Uri file;
    private String mCurrentPhotoPath;
    private File mCurrentPhotoName;
    private ImageView imageView;
    private TextView photoOfTheDay;
    private TextView taskOfTheDay;
    private String receivedKey;
    private String tableName;
    private String taskIdKey;
    private UserPurchase purchaseStatus;


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        //Notify the parent activity of the children call
        SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
        editor.putBoolean(IS_CALL_FROM_CHILDREN, true);
        editor.commit();

        setContentView(R.layout.task_details);
        tfOpenSansSemiboldItalic = Typeface.createFromAsset(this.getAssets(), "fonts/opensanssemibolditalic.ttf");
        tfwestchesterRegular = Typeface.createFromAsset(this.getAssets(), "fonts/westchesterv2regular.otf");

        // Map all the layout elements
        photoOfTheDay = (TextView) findViewById(R.id.tip_of_the_day);
        photoOfTheDay.setTypeface(tfOpenSansSemiboldItalic);
        takePictureButton = (ImageButton) findViewById(R.id.button_image);
        taskOfTheDay = (TextView) findViewById(R.id.task_of_the_day);
        taskOfTheDay.setTypeface(tfOpenSansSemiboldItalic);
        imageView = (ImageView) findViewById(R.id.imageview);

        //Set the toolbar
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(APP_NAME);

        mTitle.setTypeface(tfwestchesterRegular);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = (AdView) findViewById(R.id.adIdeasSearch);
        //Configure the test devices for the ads
        List<String> testDeviceIds = Arrays.asList(AdRequest.DEVICE_ID_EMULATOR,TEST_DEVICE_ID);
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);

        //If the app is purchased, don't show the ads for both the types
        purchaseStatus = new UserPurchase(this);
        if(purchaseStatus.isUserPurchased(REMOVE_ADS_PROD_ID)){
            //Change the visibility property for the banner
            mAdView.setVisibility(View.GONE);
        }

        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

            // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        receivedKey = intent.getStringExtra("searchKey");
        tableName = intent.getStringExtra("tableName");
        taskIdKey = intent.getStringExtra("taskIdKey");

        //Set Layout

        if(tableName.equalsIgnoreCase("Xmas"))
        {
            // Change the background of the activity to image 2 (for example)
            RelativeLayout layout =(RelativeLayout)findViewById(R.id.taskDetails);
            layout.setBackgroundResource(R.drawable.ink_xmas_xxxhd);
        }

        taskOfTheDay.setText(receivedKey);

        Uri taskImg = getImgUriFromDb(receivedKey,taskIdKey,DB_DRAWING_DIARY,tableName);
        File imgFile = new File(taskImg.getPath());
        if(imgFile.exists())
        {
            imageView.setImageURI(taskImg);
            photoOfTheDay.setVisibility(View.INVISIBLE);
        }
                }

    public void selectImage(View view){
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Choose your picture");

                    builder.setItems(options, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            if (options[item].equals("Take Photo")) {
                                try {
                                    takePicture();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else if (options[item].equals("Choose from Gallery")) {
                                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto , REQUEST_IMG_FROM_GALLERY);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void takePicture() throws IOException {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Error occurred while creating the File");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri file = FileProvider.getUriForFile(this,
                        "com.drawingDiary.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, file, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        boolean checkInstagram = isInstagramInstalled();
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {
                // Do something with the picture
                galleryAddPic();

                //imageView.setImageURI(Uri.parse("file://" + mCurrentPhotoPath));
                imageView.setImageURI(FileProvider.getUriForFile(this,
                        "com.drawingDiary.fileprovider",
                        mCurrentPhotoName));
                photoOfTheDay.setVisibility(View.INVISIBLE);
                writeImgToDb(receivedKey, taskIdKey, mCurrentPhotoPath, DB_DRAWING_DIARY, tableName);

                if (checkInstagram) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/jpg");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                            "com.drawingDiary.fileprovider",
                            mCurrentPhotoName));
                    shareIntent.setPackage("com.instagram.android");
                    /* startActivity(shareIntent);*/
                    startActivity(Intent.createChooser(shareIntent, "Share Image to..."));
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Instagram is not Installed, cannot open the application to share this picture!",
                            Toast.LENGTH_LONG)
                            .show();

                }
            }
        }
        if (requestCode == REQUEST_IMG_FROM_GALLERY) {
            if (resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (selectedImage != null) {
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        File pictureFile = new File(picturePath);
                        try {
                            copyImageFile(selectedImage, this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        imageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
                        photoOfTheDay.setVisibility(View.INVISIBLE);
                        writeImgToDb(receivedKey, taskIdKey, mCurrentPhotoPath, DB_DRAWING_DIARY, tableName);
                        cursor.close();
                    }

                    if (checkInstagram) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/jpg");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                                "com.drawingDiary.fileprovider",
                                mCurrentPhotoName));
                        shareIntent.setPackage("com.instagram.android");
                        /* startActivity(shareIntent);*/
                        startActivity(Intent.createChooser(shareIntent, "Share Image to..."));
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Instagram is not Installed, cannot open the application to share this picture!",
                                Toast.LENGTH_LONG)
                                .show();

                    }
                }
            }
        }
    }

    private File copyImageFile(Uri sourceImageFile, Context myContext) throws IOException {
        //From Android 10 is not allowed to read files directly from the gallery
        //we need to create a copy to the app location
        //First we create the destination file
        Calendar calendar = Calendar.getInstance();

        String mm = String.valueOf(calendar.get(Calendar.MONTH) +1);
        String dd = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String yyyy = String.valueOf(calendar.get(Calendar.YEAR));
        String hh = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String mi = String.valueOf(calendar.get(Calendar.MINUTE));
        String ss = String.valueOf(calendar.get(Calendar.SECOND));
        String timeStamp = yyyy+mm+dd+"_"+hh+mi+ss;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir.getPath() + File.separator +
                "DD_"+ timeStamp + ".jpg");
        //We copy the file using the copy method

        ParcelFileDescriptor openFile = myContext.getContentResolver().openFileDescriptor(sourceImageFile,"r");
        InputStream in = new FileInputStream(openFile.getFileDescriptor());
        OutputStream out = new FileOutputStream(image);
        IOUtils.copy(in,out);

                // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentPhotoName = image;

        return image;
    }

    private File createImageFile() throws IOException {
        Calendar calendar = Calendar.getInstance();

        String mm = String.valueOf(calendar.get(Calendar.MONTH) +1);
        String dd = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String yyyy = String.valueOf(calendar.get(Calendar.YEAR));
        String hh = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String mi = String.valueOf(calendar.get(Calendar.MINUTE));
        String ss = String.valueOf(calendar.get(Calendar.SECOND));
        String timeStamp = yyyy+mm+dd+"_"+hh+mi+ss;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir.getPath() + File.separator +
                "DD_"+ timeStamp + ".jpg");


        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentPhotoName = image;

    return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private boolean isInstagramInstalled(){
        boolean installed = false;

        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.instagram.android", 0);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    private void writeImgToDb(String task, String taskId, String path, String dbName, String tableName){

        promptHelper = DatabaseOpenHelper.getInstance(this,dbName);
        promptHelper.setTableName(tableName);
        promptHelper.setDbName(dbName);


        try {
            // open the database
            promptHelper.open();
            promptHelper.getWritableDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        String qry = "UPDATE \n" +
                tableName +
                " SET " + COLUMN_IMG_LINK + " = \"" + path + "\" \n" +
                " WHERE " + COLUMN_TASK + " = \"" + task + "\" \n" +
                " AND " + COLUMN_ID + " = \"" + taskId + "\" \n";

        promptHelper.runQuery(qry);

        promptHelper.close();
    }


    private Uri getImgUriFromDb(String task, String taskId, String dbName, String tableName){

        Uri imgUri = null;
        String imgPath = null;

        String[] params = new String[]{ task, taskId};

        String query = "SELECT " + COLUMN_IMG_LINK + " \n" +
                " FROM "+ tableName + " \n" +
                " WHERE " + COLUMN_TASK + " = ? \n" +
                " AND " + COLUMN_ID + " = ? \n";


        promptHelper = DatabaseOpenHelper.getInstance(this,dbName);
        promptHelper.setTableName(tableName);
        promptHelper.setDbName(dbName);


        try {
            // open the database
            promptHelper.open();
            promptHelper.getWritableDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }

                /*Cursor mCursor = promptHelper.runQuery(query,params);*/
        Cursor mCursor = promptHelper.runQuery(query,params);

        if (mCursor != null) {
            mCursor.moveToFirst();
            imgPath = mCursor.getString(0);
        }
        promptHelper.close();

        imgUri = Uri.parse("file://" + imgPath);
        return imgUri;
    }
}
