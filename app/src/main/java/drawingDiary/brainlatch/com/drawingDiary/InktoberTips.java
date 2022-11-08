package drawingDiary.brainlatch.com.drawingDiary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static drawingDiary.brainlatch.com.drawingDiary.TaskConstants.*;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


/**
 * Created by Luigi on 07/09/2017.
 */
public class InktoberTips extends AppCompatActivity {

    public static final Integer NUMBER_RESULTS = 30;
    public static final Integer NUMBER_RESULTS_TO_DISPLAY = 10;
    public static final Integer MAX_IMG_SIZE = 1200000;
    public static final Integer MIN_IMG_WIDTH = 960;
    public static final Integer MIN_IMG_HEIGHT = 720;
    public static final Integer MIN_IMG_SIZE = 1;
    private static final String TAG = InktoberTips.class.getSimpleName();
    private static boolean isFirstLoadFailed = false;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;



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
    private UserPurchase purchaseStatus;


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        //Notify the parent activity of the children call
        SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
        editor.putBoolean(IS_CALL_FROM_CHILDREN, true);
        editor.commit();

         setContentView(R.layout.activity_ideas_search);
        tfOpenSansSemiboldItalic = Typeface.createFromAsset(this.getAssets(), "fonts/opensanssemibolditalic.ttf");
        tfwestchesterRegular = Typeface.createFromAsset(this.getAssets(), "fonts/westchesterv2regular.otf");

        //Disable the Instagram Take a Picture button if we don't have Instagram installed
        takePictureButton = (ImageButton) findViewById(R.id.button_image);

        boolean weHaveInstagram = isInstagramInstalled();
        if (!weHaveInstagram) {
            takePictureButton.setVisibility(View.INVISIBLE);
            takePictureButton.setEnabled(false);
        }


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

        String receivedKey = intent.getStringExtra("searchKey");

        String tip = " \"... " + getTip(receivedKey,DB_INKTOBER_SUGGESTIONS,TB_INKTOBER_SUGGESTION) + " ...\" ";

        TextView tipOfTheDay = (TextView) findViewById(R.id.tip_of_the_day);


        tipOfTheDay.setTypeface(tfOpenSansSemiboldItalic);
        tipOfTheDay.setText(tip);

        TextView taskOfTheDay = (TextView) findViewById(R.id.task_of_the_day);

        taskOfTheDay.setTypeface(tfOpenSansSemiboldItalic);
        taskOfTheDay.setText(receivedKey);


        /*Toast.makeText(getApplicationContext(),
                "DRAW: " + tip,
                Toast.LENGTH_LONG)
                .show();*/
        }


    private String getTip(String task, String dbName, String tableName){
        String res = "No more ideas";

        String[] params = new String[]{ task};

        String query = "SELECT Idea \n" +
                " FROM "+ tableName + " \n" +
                " WHERE TASK =? " +
                " ORDER BY RANDOM() LIMIT 1";


        promptHelper = DatabaseOpenHelper.getInstance(this,dbName);
        promptHelper.setTableName(tableName);
        promptHelper.setDbName(dbName);

        try {
            //Check if the db has been updated
            // check if database exists in app path, if not copy it from assets
            promptHelper.create();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

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
            res = mCursor.getString(0);

        }
        promptHelper.close();

        return res;
    }


    public void takePicture(View view) throws IOException {

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
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
            // Do something with the picture
                galleryAddPic();
                boolean checkInstagram = isInstagramInstalled();

                if (checkInstagram) {
                    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("image/jpg");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + mCurrentPhotoPath));
                    shareIntent.setPackage("com.instagram.android");
                   /* startActivity(shareIntent);*/
                    startActivity(Intent.createChooser(shareIntent, "Share Image to..."));
                } else
                {
                    Toast.makeText(getApplicationContext(),
                            "Instagram is not Installed, cannot open the application to share this picture!",
                            Toast.LENGTH_LONG)
                            .show();

                }

            }
        }
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
}
