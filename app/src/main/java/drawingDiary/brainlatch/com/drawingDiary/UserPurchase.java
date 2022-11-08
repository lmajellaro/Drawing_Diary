package drawingDiary.brainlatch.com.drawingDiary;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPurchase {
    Context context;

    private String inAppPurchasePref = "inAppPurchasePref";

    public UserPurchase(Context context) {
        this.context = context;
    }

    public void setUserPurchased(String skuProdId, boolean IsPurchase) {
        if (context != null) {
            SharedPreferences LoginPref = context.getSharedPreferences(inAppPurchasePref, context.MODE_PRIVATE);
            SharedPreferences.Editor editor = LoginPref.edit();
            editor.putBoolean(skuProdId, IsPurchase);
            editor.apply();
        }
    }

    public boolean isUserPurchased(String skuProdId) {
        if (context != null) {
            SharedPreferences LoginPref = context.getSharedPreferences(inAppPurchasePref,context.MODE_PRIVATE);
            return LoginPref.getBoolean(skuProdId, false);
        } else
            return false;
    }
}
