package org.staticdefault.noiseep;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ultim on 2017-02-15.
 */

public class LocalData {
    private static SharedPreferences preferences;

    public interface  LocalDataRunnable {
        public void run (SharedPreferences.Editor editor);
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static void initialize(Context context){
        preferences = context.getSharedPreferences("org.staticdefault.noiseep.MainActivity", Context.MODE_PRIVATE);
    }

    public static void edit(LocalDataRunnable runnable){
        SharedPreferences.Editor editor = getPreferences().edit();

        runnable.run(editor);

        editor.commit();
    }
}
