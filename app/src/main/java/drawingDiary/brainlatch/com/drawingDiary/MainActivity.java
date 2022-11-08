package drawingDiary.brainlatch.com.drawingDiary;

import static drawingDiary.brainlatch.com.drawingDiary.TaskConstants.*;

import static drawingDiary.brainlatch.com.drawingDiary.DatabaseOpenHelper.COLUMN_ID;
import static drawingDiary.brainlatch.com.drawingDiary.DatabaseOpenHelper.COLUMN_TASK;
import static drawingDiary.brainlatch.com.drawingDiary.DatabaseOpenHelper.COLUMN_DONE;
import static drawingDiary.brainlatch.com.drawingDiary.DatabaseOpenHelper.COLUMN_COMMENTS;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    Calendar calendar = Calendar.getInstance();
    //Months Start with 0
    int thisMonth = calendar.get(Calendar.MONTH);
    int thisDay = calendar.get(Calendar.DAY_OF_MONTH);
    int thisYear = calendar.get(Calendar.YEAR);
    int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

    String todayBackground = "#26000000";
    String defaultBackground = "#FFFFFF";

    private static final String TAG ="MainActivity";
    private ListView mTaskListView;
    private ListViewAdapter mAdapter;
    private DatabaseOpenHelper drawingDiaryDbHelper;
    private Spinner mSpinner;
    public Typeface tfwestchesterRegular;
    private String currentPromptSelected;
    private String previousPromptSelected;
    private String newSelectedItem;
    private String currentPromptTbName;
    private String previousPromptTbName;
    private String[] promptNames;
    private String[] promptTables;
    private String[] inkPromptNames;
    private String[] inkPromptTables;
    HashMap<String, String> customPrompts;
    private ArrayList<String> promptNamesList;
    private ArrayList<String> inkPromptNamesList;
    private boolean isActivityRecreated;
    private Integer appStoredVersion;
    private boolean isCallFromChildren = false;
    private Integer interstitialCount = 0;


    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private UserPurchase purchaseStatus;
    private BillingClient billingClient;
    private List skuList = new ArrayList();
    private ImageButton buttonBuyProduct;


    //Actions on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Launch RateUs.java class
        RateUs.app_launched(this);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = (AdView) findViewById(R.id.adView);

        //Configure the test devices for the ads
        List<String> testDeviceIds = Arrays.asList(AdRequest.DEVICE_ID_EMULATOR,TEST_DEVICE_ID);
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);

        //Manage in-app purchases. Added SKUs to the SKU list
        skuList.add(REMOVE_ADS_PROD_ID);
        purchaseStatus = new UserPurchase(this);
        setupBillingClient();
        
        //If the app is purchased, don't show the ads for both the types
        if(purchaseStatus.isUserPurchased(REMOVE_ADS_PROD_ID)){
            //Change the visibility property for the banner
            mAdView.setVisibility(View.GONE);
        }

        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        //Initialization
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(ADMOB_INTERSTITIAL_APP_ID);
        AdRequest InterstitialReq = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(InterstitialReq);

        /*   You should not initialize your helper object using with new DatabaseHelper(context).
     Instead, always use DatabaseHelper.getInstance(context), as it guarantees that only
     one database helper will exist across the entire application's lifecycle.*/
         drawingDiaryDbHelper = DatabaseOpenHelper.getInstance(this,DB_DRAWING_DIARY);
         drawingDiaryDbHelper.setDbName(DB_DRAWING_DIARY);

        try {
            // check if database exists in app path, if not copy it from assets
            drawingDiaryDbHelper.create();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        //Create mapping between Prompt names and Prompt tables
        promptNames = getResources().getStringArray(R.array.prompts);
        promptTables = getResources().getStringArray(R.array.tableNames);

        //The list will be used to add custom elements dinamically to the Spinner
        //First we add the default prompts of the application
        //We will add later the custom ones
        promptNamesList = new ArrayList<String>(Arrays.asList(promptNames));

        Map<String, String> promptMap = new HashMap<String, String>();
        for (int i = 0; i < promptTables.length; i++) {
            promptMap.put(promptNames[i], promptTables[i]);
        }

        promptMap.put(INK_MENU_PROMPT_STRING, INK_MENU_PROMPT_CODE);
        promptNamesList.add(INK_MENU_PROMPT_STRING);

        //Check for additional custom Prompt lists to add
        customPrompts = addCustomPrompts();
        promptMap.putAll(customPrompts);
        // Finished adding custom Prompt List

        //Here we read from Shared Preferences the current selected prompt (New Prompt selection
        //is never included in shared preferences
        getBasicSharedPreferences();
        newVersionsWelcomeMsgs();

        if(!currentPromptSelected.equalsIgnoreCase(ADD_NEW_PROMPT_STRING) && !currentPromptSelected.equalsIgnoreCase(INK_MENU_PROMPT_STRING))
            Toast.makeText(getApplicationContext(),
                    "Your current prompt theme is: " + currentPromptSelected,
                    Toast.LENGTH_LONG)
                    .show();

        //Create mapping between Prompt names and Prompt tables for Ink(tober) promps
        inkPromptNames = getResources().getStringArray(R.array.ink_prompts);
        inkPromptTables = getResources().getStringArray(R.array.ink_tableNames);
        inkPromptNamesList = new ArrayList<String>(Arrays.asList(inkPromptNames));
        Map<String, String> inkPromptMap = new HashMap<String, String>();
        for (int i = 0; i < inkPromptTables.length; i++) {
            promptMap.put(inkPromptNames[i], inkPromptTables[i]);
        }

        // End of the first initialization of the basic parameters

/*        if(currentPromptSelected.equalsIgnoreCase(INK_MENU_PROMPT_STRING) && inkPromptMap.get(previousPromptSelected) != null) {
            currentPromptTbName = inkPromptMap.get(previousPromptSelected);
            previousPromptTbName = inkPromptMap.get(previousPromptSelected);
        } else if (currentPromptSelected.equalsIgnoreCase(INK_MENU_PROMPT_STRING) && inkPromptMap.get(previousPromptSelected) == null)
        {
            currentPromptTbName = promptMap.get(previousPromptSelected);
            previousPromptTbName = promptMap.get(previousPromptSelected);}
        else if (!currentPromptSelected.equalsIgnoreCase(INK_MENU_PROMPT_STRING) && currentPromptSelected != null)
            {
            currentPromptTbName = promptMap.get(currentPromptSelected);
            previousPromptTbName = promptMap.get(previousPromptSelected);}

        if (currentPromptTbName == null)
            currentPromptTbName = "Random";

        if (previousPromptSelected == null)
            previousPromptTbName = "Random";*/

        if(promptMap.get(currentPromptSelected) != null)
            currentPromptTbName = promptMap.get(currentPromptSelected);
        else currentPromptTbName = "Random";

        if(promptMap.get(previousPromptSelected) != null)
            previousPromptTbName = promptMap.get(previousPromptSelected);
        else previousPromptTbName = "Random";

        drawingDiaryDbHelper.setTableName(currentPromptTbName);

        mTaskListView = (ListView) findViewById(R.id.list_todo);

        setUpToolbar();

        // If it's a call for the creation of a new Custom Prompt start the related activity
        // otherwise update the UI for the creation of an existing prompt
        if (currentPromptTbName.equalsIgnoreCase(ADD_NEW_PROMPT_CODE))
            customPromptUI();
        else if (currentPromptTbName.equalsIgnoreCase(INK_MENU_PROMPT_CODE)) {
            // Check if it's a call from the back button
            if (isCallFromChildren == true)
                //If the call comes from the Child Activity for the Prompot creation,
                //set the spinner selection to the previous prompt
                mSpinner.setSelection(getSpinnerIndex(mSpinner, previousPromptSelected));
            else {
                //Otherwise start the activity for selecting the Ink(tober) prompt
                selectInkPrompt();
            }
        } else
            updateUI();

        //Reset the children call shared variable, this variable defines if the subactivity called the main
        SharedPreferences prefs = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE);
        if (prefs.getBoolean(IS_CALL_FROM_CHILDREN,false) == true)
        {
            SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(IS_CALL_FROM_CHILDREN, false);
            editor.commit();
        }
    }

    private void selectInkPrompt() {

        // Set spinner selection shared preference
        SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
        editor.putString(SPINNER_SELECTION, newSelectedItem);
        editor.commit();

        final CharSequence[] options = { "(Ink)tober 2016", "(Ink)tober 2017", "(Ink)tober 2018", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a prompt from the list");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("(Ink)tober 2016")) {
                    currentPromptSelected = "(Ink)tober 2016";
                    currentPromptTbName = "Inktober2016";
                    //I need to reset the default table for the DbHelper
                    drawingDiaryDbHelper.setTableName(currentPromptTbName);

                    //Remember the current selection for the spinner
                    editor.putString(PREVIOUS_SPINNER_SELECTION, currentPromptSelected);
                    editor.commit();

                    updateUI();

                } else if (options[item].equals("(Ink)tober 2017")) {
                    currentPromptSelected = "(Ink)tober 2017";
                    currentPromptTbName = "Inktober2017";
                    //I need to reset the default table for the DbHelper
                    drawingDiaryDbHelper.setTableName(currentPromptTbName);

                    //Remember the current selection for the spinner
                    editor.putString(PREVIOUS_SPINNER_SELECTION, currentPromptSelected);
                    editor.commit();

                    updateUI();

                } else if (options[item].equals("(Ink)tober 2018")) {
                    currentPromptSelected = "(Ink)tober 2018";
                    currentPromptTbName = "Inktober2018";
                    //I need to reset the default table for the DbHelper
                    drawingDiaryDbHelper.setTableName(currentPromptTbName);

                    //Remember the current selection for the spinner
                    editor.putString(PREVIOUS_SPINNER_SELECTION, currentPromptSelected);
                    editor.commit();

                    updateUI();

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is setup succesfully
                    Log.i(TAG, "Billing client successfully set up");
                    loadAllSkus();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //Try to restart the connection on the next request to
                //Google Play by calling the startConnection() method

            }
        });
    }

    private void loadAllSkus() {
        if (billingClient.isReady()) {
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.INAPP)
                    .build();
            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> skuDetailsList) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Object skuDetailsObject : skuDetailsList) {
                            final SkuDetails skuDetails = (SkuDetails) skuDetailsObject;
                            if (skuDetails.getSku().equals(REMOVE_ADS_PROD_ID)){
                                //Now that the Billing Client is ready I can enable the button
                                buttonBuyProduct.setEnabled(true);
                                buttonBuyProduct.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        BillingFlowParams params = BillingFlowParams
                                                .newBuilder()
                                                .setSkuDetails(skuDetails)
                                                .build();
                                        billingClient.launchBillingFlow(MainActivity.this, params);
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
        else
            Toast.makeText(MainActivity.this, "billing client is not ready", Toast.LENGTH_SHORT).show();
    }


    private void updateUI() {

        try {
            // open the database
            drawingDiaryDbHelper.open();
            drawingDiaryDbHelper.getWritableDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }
        ArrayList<HashMap<String, String>> taskList = new ArrayList<HashMap<String, String>>();

        //Check if the Activity has been recreated because a new prompt has been selected
        ActivityState recreatedActivity = ActivityState.getInstance();
        isActivityRecreated = recreatedActivity.getIsActivityRestarted();

        //Check if any of the Inktober tables need to be fulfilled
        checkInktoberTables();

        if (drawingDiaryDbHelper.isTableEmpty())
            drawingDiaryDbHelper.createPrompt(daysInMonth);
        else if (isActivityRecreated == true
                && isCallFromChildren == false
                    && !previousPromptTbName.equalsIgnoreCase(currentPromptTbName)) {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Challenge in progress")
                    .setMessage("Do you want to clean it up?")
                    .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // If we are using Inktober prompt we cannot change the order of tasks
                            if (!currentPromptTbName.equalsIgnoreCase(TB_INKTOBER_2016)
                                    && !currentPromptTbName.equalsIgnoreCase(TB_INKTOBER_2017)
                                        && !currentPromptTbName.equalsIgnoreCase(TB_INKTOBER_2018)) {
                                drawingDiaryDbHelper.open();
                                drawingDiaryDbHelper.createPrompt(daysInMonth);
                                drawingDiaryDbHelper.close();

                                restartActivity(false);
                            } else {
                                drawingDiaryDbHelper.open();
                                drawingDiaryDbHelper.cleanUpChallengeTable();
                                drawingDiaryDbHelper.close();

                                restartActivity(false);
                            }
                        }
                    })
                    .setNegativeButton("No!", null)
                    .create();
            dialog.show();

            //Update the counter for the Interstitial add everytime a prompt is changed
            getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE)
                    .edit()
                    .putInt(INTERSTITIAL_AD_COUNTER, interstitialCount + 1)
                    .apply();
        }

        // fetch all prompt data for the month
        Cursor inkursor = drawingDiaryDbHelper.getAllPromptData();
        inkursor.moveToFirst();

        while (!inkursor.isAfterLast()) {

            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put(COLUMN_ID, inkursor.getString(0));
            temp.put(COLUMN_TASK, inkursor.getString(1));
            temp.put(COLUMN_DONE, inkursor.getString(2));
            temp.put(COLUMN_COMMENTS, inkursor.getString(3));
            taskList.add(temp);

            inkursor.moveToNext();
        }

        inkursor.close();
        // close the database
        drawingDiaryDbHelper.close();

        // Since I changed the input parameters for the view adapter I need to pass also the context
        // the activity context can be picked up as ActivityName.this
        mAdapter = new ListViewAdapter(this, taskList, MainActivity.this);
        mTaskListView.setAdapter(mAdapter);
    }


    public void updateCheckBoxDB(View v) {
        //code to check if this checkbox is checked!
        CheckBox checkBox = (CheckBox)v;
        View parent = (View) v.getParent();
        TextView rowId  = (TextView) parent.findViewById(R.id.task_day);

        try {
            // open the database
            drawingDiaryDbHelper.open();
            drawingDiaryDbHelper.getWritableDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        if(checkBox.isChecked()){

            drawingDiaryDbHelper.updatePromptData(Integer.parseInt(rowId.getText().toString()),DONE,null,null);

        }
        else
        {
            drawingDiaryDbHelper.updatePromptData(Integer.parseInt(rowId.getText().toString()),TODO,null,null);
        }

        drawingDiaryDbHelper.close();
    }

    public void updateCommentDB(View v, String comment) {
        //code to update comment in device database!
        View parent = (View) v.getParent();
        TextView rowId  = (TextView) parent.findViewById(R.id.task_day);

        try {
            // open the database
            drawingDiaryDbHelper.open();
            drawingDiaryDbHelper.getWritableDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }
            drawingDiaryDbHelper.updatePromptData(Integer.parseInt(rowId.getText().toString()),(Integer)null,comment,null);

        drawingDiaryDbHelper.close();
    }


    public String getCommentDB(View v) {
        //code to retrieve the last comment value from database

        String retrievedComment = null;

        ImageButton imgBtnComment = (ImageButton)v;
        View parent = (View) v.getParent();
        TextView rowId  = (TextView) parent.findViewById(R.id.task_day);

        try {
            // open the database
            drawingDiaryDbHelper.open();
            drawingDiaryDbHelper.getWritableDatabase();

        } catch (SQLException sqle) {
            throw sqle;
        }
        Cursor inktoberDay = drawingDiaryDbHelper.getPromptDay(Integer.parseInt(rowId.getText().toString()));
        inktoberDay.moveToFirst();

        // The third element is the comment
        retrievedComment = inktoberDay.getString(3);
        drawingDiaryDbHelper.close();
        inktoberDay.close();

        return retrievedComment;
    }

    public boolean isCurrentPromptCustom() {

        if (customPrompts.get(currentPromptSelected) != null)
            return true;

        else
           return false;
    }

    public void editPromptTask(View v, String newTask) {
        //code to edit a task in the database after user long tap event

        //First we need to retrieve the element id

        View parent = (View) v.getParent();
        TextView rowId  = (TextView) parent.findViewById(R.id.task_day);
        Integer taskId = Integer.parseInt(rowId.getText().toString());

        try {
            // open the database
            drawingDiaryDbHelper.open();
            drawingDiaryDbHelper.getWritableDatabase();

        } catch (SQLException sqle) {
            throw sqle;
        }

        //Then we call the function to edit the task
        boolean taskUpdated = drawingDiaryDbHelper.updateTaskDB(taskId, newTask);

        if (taskUpdated)
            restartActivity(true);
     }

    private void customPromptUI() {

        // If the user wants to create a new prompt we need to start a new activity
        // Check if it's a call from the back button
        if (isCallFromChildren == true)
            //If the call comes from the Child Activity for the Prompot creation,
            //set the spinner selection to the previous prompt
            mSpinner.setSelection(getSpinnerIndex(mSpinner, previousPromptSelected));
        else {
            //Otherwise start the activity for the prompt creation
            Intent intent = new Intent(this, CreateCustomPrompt.class);
            startActivity(intent);
        }
    }

    /** Called when the user taps the Task Name*/
    /** Add the check if the app is purchased*/
    public void executeTaskDetails(View view) {

        Integer intAdsCount = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).getInt(INTERSTITIAL_AD_COUNTER,0);
        TextView taskText = (TextView)view.findViewById(R.id.task_title);

        if (mInterstitialAd.isLoaded() && !purchaseStatus.isUserPurchased(REMOVE_ADS_PROD_ID) && intAdsCount >= INTERSTITIAL_MAX_COUNT ) {
            mInterstitialAd.show();

            //Reset the counter value
            getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE)
                    .edit()
                    .putInt(INTERSTITIAL_AD_COUNTER, 0)
                    .apply();
        }else {
            //Add one to the counter for the next interstitial display event
            getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE)
                    .edit()
                    .putInt(INTERSTITIAL_AD_COUNTER, intAdsCount + 1)
                    .apply();
            Intent intent = null;

            //** Only for Inktober 2017 we created a Inktober Tips table
            if(currentPromptTbName.equalsIgnoreCase(TB_INKTOBER_2017))
                intent = new Intent(this, InktoberTips.class);
            else
                intent = new Intent(this, TaskDetails.class);

            String searchKey = taskText.getText().toString();

            //This block allows to retrieve a view of the list view based on
            //the clicked element
            View parentRow = (View) view.getParent();
            ListView listView = (ListView) parentRow.getParent();
            final int position = listView.getPositionForView(parentRow);
            TextView taskId = (TextView)parentRow.findViewById(R.id.task_day);
            String taskIdKey = taskId.getText().toString();
            //end of block

            intent.putExtra("searchKey", searchKey);
            intent.putExtra("taskIdKey", taskIdKey);
            intent.putExtra("tableName", currentPromptTbName);

            startActivity(intent);
        }
    }

    public void markCurrentDay(View v, Integer id){

        if(thisDay == id + 1)
            v.setBackgroundColor(Color.parseColor(todayBackground));
        else
            v.setBackgroundColor(Color.parseColor(defaultBackground));
    }


public void setUpToolbar() {

    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
    mTitle.setText(APP_NAME);
    tfwestchesterRegular = Typeface.createFromAsset(this.getAssets(), "fonts/westchesterv2regular.otf");
    mTitle.setTypeface(tfwestchesterRegular);

    //Setup the icon for the App Store
    buttonBuyProduct = (ImageButton) findViewById(R.id.shopping);
    //I need to disable the button until the billing client is ready
    buttonBuyProduct.setEnabled(false);

    //Disable the Bin icon if the Prompt is not a custom prompt

    ImageButton trashBin = (ImageButton) findViewById(R.id.delete_custom_prompt);
    if (customPrompts.get(currentPromptSelected) != null) {
        trashBin.setVisibility(View.VISIBLE);
        trashBin.setEnabled(true);
    }
    else{
        trashBin.setVisibility(View.INVISIBLE);
        trashBin.setEnabled(false);
    }

    mSpinner = (Spinner) findViewById(R.id.spinner_settings);

    //ArrayAdapter<String> spinnerPromptsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, promptNames){
    ArrayAdapter<String> spinnerPromptsAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, promptNamesList){
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // this part is needed for hiding the original view
            View view = super.getView(position, convertView, parent);
            view.setVisibility(View.GONE);

            return view;
        }
    };

    mSpinner.setAdapter(spinnerPromptsAdapter);

    //Set Spinner position
        mSpinner.setSelection(getSpinnerIndex(mSpinner, currentPromptSelected));


    mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            newSelectedItem = parent.getItemAtPosition(position).toString();

            // Set spinner selection shared preference
            SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
            editor.putString(SPINNER_SELECTION, newSelectedItem);
            editor.commit();

            //Remember the current selection only if it was not a call for a new prompt creation
            //or a call to the Ink(tober) Menu
            if (!currentPromptSelected.equalsIgnoreCase(ADD_NEW_PROMPT_STRING) && !currentPromptSelected.equalsIgnoreCase(INK_MENU_PROMPT_STRING)) {
                editor.putString(PREVIOUS_SPINNER_SELECTION, currentPromptSelected);
                editor.commit();
            }

            //Recreate the activity with the new Prompt
                if (!newSelectedItem.equalsIgnoreCase(currentPromptSelected))
            {
                restartActivity(true);
            }
        } // to close the onItemSelected
        public void onNothingSelected(AdapterView<?> parent)
        {
            Toast.makeText(getApplicationContext(),
                    "No prompt selected",
                    Toast.LENGTH_LONG)
                    .show();
        }
    });
}


    private int getSpinnerIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

    private void restartActivity(boolean restartActivityStatus){

        ActivityState recreatedActivity = ActivityState.getInstance();
        recreatedActivity.setIsActivityRestarted(restartActivityStatus);
        isActivityRecreated = recreatedActivity.getIsActivityRestarted();
        Intent intent = getIntent();
        finish();
        startActivity(intent);

    }

    private void getBasicSharedPreferences()
    {
        SharedPreferences prefs = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE);

        if(prefs.contains(SPINNER_SELECTION)) {

            currentPromptSelected = prefs.getString(SPINNER_SELECTION,"Random");
            previousPromptSelected = prefs.getString(PREVIOUS_SPINNER_SELECTION,"Random");
           /* isActivityRecreated = prefs.getBoolean("isActivityRecreated",false);*/
        }
        else {

            SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
            editor.putString(SPINNER_SELECTION, "Random");
            editor.putString(PREVIOUS_SPINNER_SELECTION, "Random");
            editor.commit();
            currentPromptSelected = prefs.getString(SPINNER_SELECTION,"Random");
            previousPromptSelected = prefs.getString(PREVIOUS_SPINNER_SELECTION,"Random");
        }

        if(prefs.contains(IS_CALL_FROM_CHILDREN)) {

            isCallFromChildren = prefs.getBoolean(IS_CALL_FROM_CHILDREN,false);
        }
        else {
            SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(IS_CALL_FROM_CHILDREN, false);
            editor.commit();
            isCallFromChildren = prefs.getBoolean(IS_CALL_FROM_CHILDREN,false);
        }

        if(prefs.contains(APP_STORED_VERSION)) {

            appStoredVersion = prefs.getInt(APP_STORED_VERSION,0);
        }
        else {
            SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
            Integer appVersion = getAppCurrentVersion();
            editor.putInt(APP_STORED_VERSION, appVersion);
            editor.commit();
            appStoredVersion = prefs.getInt(APP_STORED_VERSION,0);
        }

        if(prefs.contains(INTERSTITIAL_AD_COUNTER)) {

            interstitialCount = prefs.getInt(INTERSTITIAL_AD_COUNTER,0);
        }
        else {
            SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
            editor.putInt(INTERSTITIAL_AD_COUNTER, 0);
            editor.commit();
            interstitialCount = prefs.getInt(INTERSTITIAL_AD_COUNTER,0);
        }
    }

    private HashMap<String,String> addCustomPrompts(){

        DatabaseOpenHelper customPromptsDbHelper;

        /*   You should not initialize your helper object using with new DatabaseHelper(context).
     Instead, always use DatabaseHelper.getInstance(context), as it guarantees that only
     one database helper will exist across the entire application's lifecycle.*/
        customPromptsDbHelper = DatabaseOpenHelper.getInstance(this,DB_DRAWING_DIARY);
        //customPromptsDbHelper.setDbName(DB_DRAWING_DIARY);

        String promptName;
        String promptTableName;
        HashMap<String,String> customPromptsMap = new HashMap<String,String>();

        try {
            // open the database
            customPromptsDbHelper.open();
            customPromptsDbHelper.getWritableDatabase();

        } catch (SQLException sqle) {
            throw sqle;
        }

        if(!customPromptsDbHelper.isTableEmpty(TB_CUSTOM_PROMPTS))
        {
            String qry = "SELECT * FROM " + TB_CUSTOM_PROMPTS;
            Cursor prmts = customPromptsDbHelper.runQuery(qry,null);
            prmts.moveToFirst();

            while (!prmts.isAfterLast()) {
                promptName = prmts.getString(0);
                promptTableName = CUSTOM_TABLE_PREFIX + prmts.getString(1);
                customPromptsMap.put(promptName, promptTableName);
                promptNamesList.add(promptName);
                prmts.moveToNext();
            }

        }

        customPromptsMap.put(ADD_NEW_PROMPT_STRING, ADD_NEW_PROMPT_CODE);
        promptNamesList.add(ADD_NEW_PROMPT_STRING);

        return customPromptsMap;
    }

    public void onClickdeleteCustomPrompt(View view) throws IOException {

        delPromptFromDB(currentPromptSelected);
    }

    public void delPromptFromDB(String promptName) {

        drawingDiaryDbHelper = DatabaseOpenHelper.getInstance(this, DB_DRAWING_DIARY);
        drawingDiaryDbHelper.setDbName(DB_DRAWING_DIARY);

        try {
            // open the database
            drawingDiaryDbHelper.open();
            drawingDiaryDbHelper.getWritableDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }
        //Parametrized query to find the prompt in the list of the custom prompts
        String[] params = new String[]{ promptName};
        String findPromptQry = "SELECT tablename \n" +
                " FROM "+ TB_CUSTOM_PROMPTS + " \n" +
                " WHERE promptName = ?";

        Cursor findTableNumber = drawingDiaryDbHelper.runQuery(findPromptQry,params);

        if (!drawingDiaryDbHelper.isCursorEmpty(findTableNumber)) {
            findTableNumber.moveToFirst();
            String tableName = CUSTOM_TABLE_PREFIX + findTableNumber.getString(0);

            drawingDiaryDbHelper.deleteTable(tableName);
            drawingDiaryDbHelper.deleteTable(tableName + TB_RESOURCE_TABLE_SUFFIX);


            drawingDiaryDbHelper.deleteFromTable(TB_CUSTOM_PROMPTS,"promptName = ?",new String[] {promptName});

                Toast.makeText(getApplicationContext(),
                        "The prompt: " + promptName + " has been deleted! ",
                        Toast.LENGTH_LONG)
                        .show();

        }

        //Since the prompt does't exist anymore I need to repoint the spinner
        if (!previousPromptSelected.equalsIgnoreCase(promptName))
            mSpinner.setSelection(getSpinnerIndex(mSpinner, previousPromptSelected));
        else
            mSpinner.setSelection(getSpinnerIndex(mSpinner, "Random"));
    }

    private void newVersionsWelcomeMsgs() {

        SharedPreferences prefs = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE);
        Integer appVersion = getAppCurrentVersion();

        //Check if the SharedPreference isFirstRun exists. If does not exit then don't show the
        //Update message since it's a fresh installation
        boolean isFreshInstallation = !getSharedPreferences(FIRST_RUN_PREFERENCE, MODE_PRIVATE).contains(IS_FIRST_RUN);

        if (isFreshInstallation == true)
            return;

        if (appVersion > appStoredVersion && appVersion == 13) {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Application Upgrade!")
                    .setMessage("Now you can create your own custom prompts!")
                    .setPositiveButton("Got it!", null)
                    .create();
            dialog.show();

            SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
            editor.putInt(APP_STORED_VERSION, appVersion);
            editor.commit();

        }
    }

    private void checkInktoberTables() {
        if (drawingDiaryDbHelper.isTableEmpty(currentPromptTbName) &&
                (currentPromptTbName.equalsIgnoreCase(TB_INKTOBER_2016)
                        || currentPromptTbName.equalsIgnoreCase(TB_INKTOBER_2017)
                            || currentPromptTbName.equalsIgnoreCase(TB_INKTOBER_2018)))
            drawingDiaryDbHelper.fillResourceTableFromAsset(currentPromptTbName);
    }

    private Integer getAppCurrentVersion() {

        Integer versionNumber = 0;

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            versionNumber = pInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error reading versionCode");
            e.printStackTrace();
        }
        return versionNumber;
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();

        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            //OK
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
        else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            // already owned
            //I need to be sure that the application is aware that the product has been already bought
            purchaseStatus.setUserPurchased(REMOVE_ADS_PROD_ID,true);
            mAdView.setVisibility(View.GONE);
        }
        else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Nothing to do
        }

    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals(REMOVE_ADS_PROD_ID)) {
            Toast.makeText(this, "Purchase done. You got rid of interstitial and banner ads!", Toast.LENGTH_SHORT).show();
            //I need to be sure that the application is aware that the product has been bought
            purchaseStatus.setUserPurchased(REMOVE_ADS_PROD_ID,true);
            mAdView.setVisibility(View.GONE);

        }
    }
}
