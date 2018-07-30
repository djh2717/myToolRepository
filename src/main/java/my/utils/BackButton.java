package my.utils;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

/**
 * Used to exit activity.
 *
 * @author 15445
 */
public class BackButton {

    public static void back(Button button, final Activity activity) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }
}
