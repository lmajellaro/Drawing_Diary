package drawingDiary.brainlatch.com.drawingDiary;

import android.provider.BaseColumns;

/**
 * Created by Luigi on 02/09/2017.
 */

public class TaskConstants {
    public static final int DONE = 1;
    public static final int TODO = 0;

    public static final String ADD_NEW_PROMPT_CODE = "addnew";
    public static final String ADD_NEW_PROMPT_STRING = "Add new prompt...";
    public static final String INK_MENU_PROMPT_CODE = "inkmenu";
    public static final String INK_MENU_PROMPT_STRING = "(Ink)tober";
    public static final String NOCOMMENT = "";
    public static final String APP_NAME = "Drawing Diary";
    public static final String ADMOB_INTERSTITIAL_APP_ID="ca-app-pub-3940256099942544/1033173712";
    public static final String TEST_DEVICE_ID ="33BE2250B43518CCDA7DE426D04EE231";
    public static final String INTERSTITIAL_AD_COUNTER = "interstitalCnt";
    //Define the frequency of the Interstitial ad
    public static final Integer INTERSTITIAL_MAX_COUNT = 1; //Default 2

    public static final String TB_RESOURCE_TABLE_SUFFIX = "_res";

//Available prompts
    public static final String TB_INKTOBER_2018 = "inktober2018";
    public static final String TB_INKTOBER_2017 = "inktober2017";
    public static final String TB_INKTOBER_2016 = "inktober2016";
    public static final String DB_INKTOBER_SUGGESTIONS = "InktoberIdeas";
    public static final String TB_INKTOBER_SUGGESTION = "InktoberIdeas";
    public static final String DB_DRAWING_DIARY = "drawingDiary";

//Custom Prompt list table and parameters
    public static final String TB_CUSTOM_PROMPTS = "customprompts_list";
    public static final String CUSTOM_TABLE_ID = "Table";
    public static final String CUSTOM_TABLE_SEPARATOR = "_";
    public static final String CUSTOM_TABLE_PREFIX = CUSTOM_TABLE_ID + CUSTOM_TABLE_SEPARATOR;

//Database Open Helper Variables
    public static final int DATABASE_VERSION = 8;

//SHARED PREFERENCES CONSTANTS
    public static final String PROMPT_PREFERENCES = "drawingDiary.promptSelection";
    public static final String FIRST_RUN_PREFERENCE = "PREFERENCE";
    public static final String APP_STORED_VERSION = "appStoredVersion";
    public static final String IS_FIRST_RUN = "isFirstRun";
    public static final String IS_CALL_FROM_CHILDREN = "isCallFromChildren";
    public static final String SPINNER_SELECTION = "spinnerSelection";
    public static final String PREVIOUS_SPINNER_SELECTION ="prevSpinnerSelection";

    //Store Product ids
    public static final String REMOVE_ADS_PROD_ID = "1_remove_ads";

}
