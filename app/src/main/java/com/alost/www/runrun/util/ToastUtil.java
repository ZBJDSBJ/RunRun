package com.alost.www.runrun.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Alost on 17/6/27.
 */

public class ToastUtil {
    private static Toast toast;

    public static void showToast(Context context, String showString) {
        if (toast == null) {
            toast = Toast.makeText(context, showString, Toast.LENGTH_SHORT);
        } else {
            toast.setText(showString);
        }
        toast.show();
    }
}
