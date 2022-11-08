package drawingDiary.brainlatch.com.drawingDiary;

/**
 * Created by Luigi on 24/10/2017.
 * Singleton class
 */

public class ActivityState {

    private static ActivityState instance = new ActivityState();

    /* Static 'instance' method */
    public static ActivityState getInstance( ) {
        return instance;
    }

    private  boolean isActivityRestarted;

    private ActivityState(){
        isActivityRestarted = false;
    }

    public boolean getIsActivityRestarted(){
        return isActivityRestarted;
    }
    public void setIsActivityRestarted(boolean isRestarted){
        this.isActivityRestarted = isRestarted;
    }
}
