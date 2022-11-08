package drawingDiary.brainlatch.com.drawingDiary;


import static android.content.Context.MODE_PRIVATE;

import static drawingDiary.brainlatch.com.drawingDiary.TaskConstants.*;
import static drawingDiary.brainlatch.com.drawingDiary.DatabaseOpenHelper.COLUMN_ID;
import static drawingDiary.brainlatch.com.drawingDiary.DatabaseOpenHelper.COLUMN_TASK;
import static drawingDiary.brainlatch.com.drawingDiary.DatabaseOpenHelper.COLUMN_DONE;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;

import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;


class ListViewAdapter extends BaseAdapter {

    // boolean array for storing
    //the state of each CheckBox
    boolean[] checkBoxState;
    private Context mContext;

    //define the Typefaces used in the class
    public Typeface tfOpenSansSemiboldItalic;

    //check if the user already saw the help guide
    private boolean isHelpShown = false;

    static class InktoberViewHolder {
        TextView txtFirst;
        TextView txtSecond;
        ImageButton imgBtnThree;
        CheckBox ckbFourth;
    }

    public ArrayList<HashMap<String, String>> list;
    Activity activity;

    public ListViewAdapter(Activity activity, ArrayList<HashMap<String, String>> list, Context context) {
        super();
        this.activity = activity;
        this.list = list;
        this.mContext = context;

        //create the boolean array with
        //initial state as in the database

        checkBoxState = new boolean[list.size()];

        for (int i = 0; i < list.size();
             i++) {
            HashMap<String, String> tmp = list.get(i);
            switch (Integer.parseInt(tmp.get(COLUMN_DONE))) {
                case 1:
                    checkBoxState[i] = true;
                    break;
                default:
                    checkBoxState[i] = false;
            }
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public void notifyDataSetChanged() {
        // TODO Auto-generated method stub
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        InktoberViewHolder viewHolder;

        if (convertView == null) {

            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.item_todo, null);

            viewHolder = new InktoberViewHolder();

            //Cache the views
            viewHolder.txtFirst = (TextView) convertView.findViewById(R.id.task_day);
            viewHolder.txtSecond = (TextView) convertView.findViewById(R.id.task_title);
            viewHolder.imgBtnThree = (ImageButton) convertView.findViewById(R.id.comment);
            viewHolder.ckbFourth = (CheckBox) convertView.findViewById(R.id.task_checked);

            //link the cached views to the convertview
            convertView.setTag(viewHolder);

            tfOpenSansSemiboldItalic = Typeface.createFromAsset(mContext.getAssets(), "fonts/opensanssemibolditalic.ttf");
            viewHolder.txtFirst.setTypeface(tfOpenSansSemiboldItalic);
            viewHolder.txtSecond.setTypeface(tfOpenSansSemiboldItalic);
            viewHolder.ckbFourth.setTypeface(tfOpenSansSemiboldItalic);

        } else {
            viewHolder = (InktoberViewHolder) convertView.getTag();
        }


        //set the data to be displayed
        HashMap<String, String> map = list.get(position);
        viewHolder.txtFirst.setText(map.get(COLUMN_ID));
        viewHolder.txtSecond.setText(map.get(COLUMN_TASK));

        //Mark the daily task
        ((MainActivity) mContext).markCurrentDay(convertView, position);

        //set CheckBox using the boolean array
        viewHolder.ckbFourth.setChecked(checkBoxState[position]);

        viewHolder.imgBtnThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Retrieve the last value for the comment from database
                final String comment = ((MainActivity) mContext).getCommentDB(v);
                final EditText taskEditText = new EditText(activity);
                taskEditText.setText(comment, TextView.BufferType.EDITABLE);
                taskEditText.setTypeface(tfOpenSansSemiboldItalic);

                final View viewComm = v;

                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setTitle("Add an idea")
                        .setMessage("What are we gonna draw?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // String task = String.valueOf(taskEditText.getText());
                                //  Log.d(TAG, "Task to add: " + task);
                                String comment = String.valueOf(taskEditText.getText());
                                if (mContext instanceof MainActivity) {
                                    ((MainActivity) mContext).updateCommentDB(viewComm, comment);
                                }

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });

        viewHolder.ckbFourth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    checkBoxState[position] = true;
                } else {
                    checkBoxState[position] = false;
                }

                if (mContext instanceof MainActivity) {
                    ((MainActivity) mContext).updateCheckBoxDB(v);
                }
            }
        });

        if (position == 0 && isHelpShown == false) {
            showCaseUserTutorial(convertView);
            isHelpShown = true;
        }

        //Create onLongClick Listener to edit the single tasks afterwards
        TextView taskText = (TextView) convertView.findViewById(R.id.task_title);
        taskText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //Ask the user to edit the task
                final EditText taskEditText = new EditText(activity);
                taskEditText.setTypeface(tfOpenSansSemiboldItalic);

                //Only custom prompts can be edited
                if (((MainActivity) mContext).isCurrentPromptCustom()) {
                    final View taskView = v;
                    AlertDialog dialog = new AlertDialog.Builder(activity)
                            .setTitle("Edit this task")
                            .setMessage("What are we gonna draw?")
                            .setView(taskEditText)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String newTask = String.valueOf(taskEditText.getText());
                                    if (mContext instanceof MainActivity) {
                                        ((MainActivity) mContext).editPromptTask(taskView, newTask);
                                    }

                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                    dialog.show();
                } else
                    Toast.makeText((MainActivity) mContext,
                            String.valueOf("Sorry, only custom prompts can be edited."),
                            Toast.LENGTH_LONG)
                            .show();
                return true;
            }
        });

        return convertView;
    }

    public boolean isEnabled(int position) {
        return false;
    }

    public void showCaseUserTutorial(View v) {
        boolean isFirstRun = mContext.getSharedPreferences(FIRST_RUN_PREFERENCE, MODE_PRIVATE).getBoolean(IS_FIRST_RUN, true);

        if (isFirstRun) {
            final ShowcaseView showcaseview;

            TextView task = (TextView) v.findViewById(R.id.task_title);
            final CheckBox chbox = (CheckBox) v.findViewById(R.id.task_checked);
            final ImageButton imgBtnComment = (ImageButton) v.findViewById(R.id.comment);

            showcaseview = new ShowcaseView.Builder(this.activity)
                    .withMaterialShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setTarget(new ViewTarget(task))
                    .setContentText("Tap on the tasks for more details!")
                    .blockAllTouches()
                    .build();

            showcaseview.overrideButtonClick(new View.OnClickListener() {

                int cnt = 0;

                @Override
                public void onClick(View v) {
                    cnt++;

                    switch (cnt) {
                        case 1:
                            showcaseview.setContentText("Use the checkboxes to keep track of your progress!");
                            showcaseview.setTarget(new ViewTarget(chbox));
                            break;
                        case 2:
                            showcaseview.setContentText("Write notes and ideas on the go!");
                            showcaseview.setTarget(new ViewTarget(imgBtnComment));
                            break;
                        case 3:
                            showcaseview.setContentText("Tap longer on a task to edit your custom prompts!");
                            showcaseview.setTarget(new ViewTarget(task));
                            break;
                        case 4:
                            showcaseview.hide();
                            break;
                    }
                }
            });


            mContext.getSharedPreferences(FIRST_RUN_PREFERENCE, MODE_PRIVATE)
                    .edit()
                    .putBoolean(IS_FIRST_RUN, false)
                    .apply();
        }
    }
}



