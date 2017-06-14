package in.codelab.androidauthdemo;

import android.util.Log;

import timber.log.Timber;

class DefaultTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }
        Log.println(priority, tag, message);
    }
}
