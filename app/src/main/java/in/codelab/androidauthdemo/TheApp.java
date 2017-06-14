package in.codelab.androidauthdemo;

import android.app.Application;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class TheApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        plantingTimber();
    }

    private void plantingTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            //Do Nothing for now..Helpful when used with Crashlytics like tool/lib.
            //Ref.: https://blog.xmartlabs.com/2015/07/09/Android-logging-with-Crashlytics-and-Timber/
            Timber.plant(new DefaultTree());
        }
    }
}
