package drawingDiary.brainlatch.com.drawingDiary;

        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.graphics.Color;
        import android.graphics.Typeface;
        import android.net.Uri;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.LinearLayout;
        import android.widget.TextView;

public class RateUs {

    // Insert your Application Title
    private final static String TITLE = "Drawing Diary";

    // Insert your Application Package Name
    private final static String PACKAGE_NAME = "drawingDiary.brainlatch.com.drawingDiary";

    // Day until the Rate Us Dialog Prompt(Default 2 Days)
    private final static int DAYS_UNTIL_PROMPT = 5;

    // App launches until Rate Us Dialog Prompt(Default 5 Launches)
    private final static int LAUNCHES_UNTIL_PROMPT = 15;

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("rateus", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch
                    + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context mContext,
                                      final SharedPreferences.Editor editor) {
        Typeface tfOpenSansSemiboldItalic = Typeface.createFromAsset(mContext.getAssets(), "fonts/opensanssemibolditalic.ttf");
        Typeface tfwestchesterRegular = Typeface.createFromAsset(mContext.getAssets(), "fonts/westchesterv2regular.otf");

        final Dialog dialog = new Dialog(mContext,R.style.Dialog);
        // Set Dialog Title
        dialog.setTitle("Rate " + TITLE);

        TextView dialogTextView = (TextView) dialog.findViewById(android.R.id.title);
        if(dialogTextView != null)
            dialogTextView.setTypeface(tfOpenSansSemiboldItalic);

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(mContext);
        tv.setTypeface(tfOpenSansSemiboldItalic);
        tv.setText("If you like " + TITLE
                + ", please leave us a feedback!");
        tv.setWidth(240);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(30, 30, 30, 10);
        ll.addView(tv);

        // First Button
        Button b1 = new Button(mContext);
        b1.setText("Rate " + TITLE);
        b1.setTypeface(tfOpenSansSemiboldItalic);
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("market://details?id=" + PACKAGE_NAME)));
                dialog.dismiss();
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
            }
        });
        ll.addView(b1);

        // Second Button
        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setTypeface(tfOpenSansSemiboldItalic);
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                if (editor != null) {
                    editor.putLong("date_firstlaunch", 0);
                    editor.commit();
                    editor.putLong("launch_count", 0);
                    editor.commit();
                }
            }
        });
        ll.addView(b2);

        // Third Button
        Button b3 = new Button(mContext);
        b3.setText("Stop Bugging me");
        b3.setTypeface(tfOpenSansSemiboldItalic);
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);

        // Show Dialog
        dialog.show();
    }
}