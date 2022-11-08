package drawingDiary.brainlatch.com.drawingDiary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static drawingDiary.brainlatch.com.drawingDiary.TaskConstants.*;

/**
 * Created by Luigi on 07/09/2017.
 */
public class CreateCustomPrompt extends AppCompatActivity {

    Integer numberOfRecords = 0;

    private Integer getNumberOfRecords() {
        return this.numberOfRecords;
    }

    private void setNumberOfRecords(Integer recordsToProcess) {
        if (recordsToProcess != null)
            this.numberOfRecords = recordsToProcess;
    }

    Integer totalNumberOfRecords = 0;

    private void setTotalNumberOfRecords(Integer recordsInPrompt) {
        if (recordsInPrompt != null)
            this.totalNumberOfRecords = recordsInPrompt;
    }

    String promptElementText = null;

    private String getpPomptElementText() {
        return this.promptElementText;
    }

    private void setPromptElementText(String element) {
        this.promptElementText = element;
    }

    private ArrayList<String> newPromptElements = new ArrayList<String>();


    //define the Typefaces used in the class
    public Typeface tfOpenSansSemiboldItalic;
    public Typeface tfwestchesterRegular;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private TextView createNewPrompt;
    private DatabaseOpenHelper customPromptHelper;
    private UserPurchase purchaseStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Notify the parent activity of the children call
        SharedPreferences.Editor editor = getSharedPreferences(PROMPT_PREFERENCES, MODE_PRIVATE).edit();
        editor.putBoolean(IS_CALL_FROM_CHILDREN, true);
        editor.commit();

        setContentView(R.layout.create_custom_prompt);
        tfOpenSansSemiboldItalic = Typeface.createFromAsset(this.getAssets(), "fonts/opensanssemibolditalic.ttf");
        tfwestchesterRegular = Typeface.createFromAsset(this.getAssets(), "fonts/westchesterv2regular.otf");

        createNewPrompt = (TextView) findViewById(R.id.create_new_prompt);
        createNewPrompt.setTypeface(tfOpenSansSemiboldItalic);

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
        List<String> testDeviceIds = Arrays.asList(AdRequest.DEVICE_ID_EMULATOR, TEST_DEVICE_ID);
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);

        //If the app is purchased, don't show the ads for both the types
        purchaseStatus = new UserPurchase(this);
        if (purchaseStatus.isUserPurchased(REMOVE_ADS_PROD_ID)) {
            //Change the visibility property for the banner
            mAdView.setVisibility(View.GONE);
        }
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        createNewPrompt();
    }

    private void createNewPrompt() {

        Typeface tfOpenSansSemiboldItalic = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/opensanssemibolditalic.ttf");
        //default string for the Alert Dialog
        String txt = null;

        final EditText diagEditText = new EditText(CreateCustomPrompt.this);
        diagEditText.setText(txt, TextView.BufferType.EDITABLE);
        diagEditText.setTypeface(tfOpenSansSemiboldItalic);
        diagEditText.setGravity(Gravity.CENTER_HORIZONTAL);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create a new prompt...")
                .setMessage("How many different things you want to draw during this challenge?")
                .setView(diagEditText)
                .setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Save the number of elements selected
                        final Integer numberOfElements = tryParse(String.valueOf(diagEditText.getText()));
                        //This variable will be reset while the prompt list is created
                        setNumberOfRecords(numberOfElements);
                        //This variable will store the total number of records defined
                        setTotalNumberOfRecords(numberOfElements);
                        dialog.dismiss();
                        createNewPromptElements(numberOfRecords);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CreateCustomPrompt.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void createNewPromptElements(Integer elements) {

        // The first element is the title
        if (numberOfRecords == 0 && newPromptElements.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "No prompt to be created!",
                    Toast.LENGTH_LONG)
                    .show();
            Intent intent = new Intent(CreateCustomPrompt.this, MainActivity.class);
            startActivity(intent);
            return;
        } else if (numberOfRecords == 0 && !newPromptElements.isEmpty()) {
            // Save the elements to a proper table in the
            //Use the first element of the array as prompt Name

            savePromptToDB(newPromptElements);

            Toast.makeText(getApplicationContext(),
                    "Prompt Created!",
                    Toast.LENGTH_LONG)
                    .show();
            Intent intent = new Intent(CreateCustomPrompt.this, MainActivity.class);
            startActivity(intent);
            return;
        }

        Typeface tfOpenSansSemiboldItalic = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/opensanssemibolditalic.ttf");
        //default string for the Alert Dialog
        String txt = null;

        final EditText diagEditText = new EditText(CreateCustomPrompt.this);
        diagEditText.setText(txt, TextView.BufferType.EDITABLE);
        diagEditText.setTypeface(tfOpenSansSemiboldItalic);

        AlertDialog elementsNamesDialog = new AlertDialog.Builder(this)
                .setTitle("Create a new prompt...")
                .setMessage("Insert the name of the element " + newPromptElements.size())
                .setView(diagEditText)
                .setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Save the number of elements selected
                        final String elementName = String.valueOf(diagEditText.getText());
                        newPromptElements.add(elementName);

                        //Set the number of Records left to process
                        setNumberOfRecords(numberOfRecords - 1);
                        final Integer pmptElements = numberOfRecords;
                        //Call again the same method
                        createNewPromptElements(pmptElements);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CreateCustomPrompt.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .create();

        elementsNamesDialog.setCanceledOnTouchOutside(false);
        // Use this condition to define the name of the prompt
        if (newPromptElements.isEmpty()) {
            elementsNamesDialog.setMessage("Insert the name of the prompt");
            //add one record for the title
            numberOfRecords = numberOfRecords + 1;
        }

        elementsNamesDialog.show();
    }


    private Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {

            Toast.makeText(getApplicationContext(),
                    String.valueOf("You need to enter a valid number!"),
                    Toast.LENGTH_LONG)
                    .show();
            return null;
        }
    }

    private void savePromptToDB(ArrayList<String> promptElements) {

        customPromptHelper = DatabaseOpenHelper.getInstance(this, DB_DRAWING_DIARY);
        customPromptHelper.setDbName(DB_DRAWING_DIARY);

        try {
            // open the database
            customPromptHelper.open();
            customPromptHelper.getWritableDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        //Create the Custom Prompt table set from the title of the prompt, first element of the array
        String tableName = customPromptHelper.createCustomPromptTableSet(promptElements.get(0));

        if (tableName == null) {
            Toast.makeText(getApplicationContext(),
                    String.valueOf("The Prompt name is not valid!"),
                    Toast.LENGTH_LONG)
                    .show();
        } else {
            customPromptHelper.setTableName(tableName + TB_RESOURCE_TABLE_SUFFIX);

            //Insert records in the resource table
            //Skip the first one since it's the title

            for (int i = 1; i <= promptElements.size() - 1; i++) {
                String element = promptElements.get(i);
                customPromptHelper.insertPromptData(null, element, 0, "", "");
            }
        }
    }
}

