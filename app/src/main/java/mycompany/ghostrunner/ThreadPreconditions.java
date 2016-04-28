package mycompany.ghostrunner;

import android.os.Looper;

/**
 * Created by rolofzon on 2016-04-28.
 */
public class ThreadPreconditions {
    public static void checkOnMainThread() {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                throw new IllegalStateException("This method should be called from the Main Thread");
            }
        }
    }
}
