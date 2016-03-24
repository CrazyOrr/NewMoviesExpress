package com.github.crazyorr.newmoviesexpress;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.github.crazyorr.newmoviesexpress.activity.MainActivity;
import com.github.crazyorr.newmoviesexpress.activity.NotificationsActivity;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.util.NewMoviesExpressService;
import com.github.crazyorr.newmoviesexpress.util.Util;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.rollinglogger.RollingLogger;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class BackgroundService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final String TAG = BackgroundService.class.getSimpleName();
    private static final String INTENT_EXTRA_NAME_ALARM = "INTENT_EXTRA_NAME_ALARM";

    private static final int MAX_NOTIFICATIONS_COUNT = 5;
    private static final int MAX_RETRY_TIMES = 3;

    private MyBinder mBinder;
    private RollingLogger mLogger;

    private int mNotificationId = 1;

    private int mRetryTimes;

    private String mToken;
    private boolean isNotificationEnabled;
    private int mDaysBefore;
    private int mDaysAfter;

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new MyBinder();

        //日志文件大小(byte)
        final long LOG_FILE_SIZE = 1 * 1024 * 1024;
        //日志文件个数
        final int LOG_FILE_COUNT = 5;
        mLogger = new RollingLogger(Util.getSavingPath(this) + "log", "log",
                LOG_FILE_SIZE, LOG_FILE_COUNT);

        writeLogLine(TAG + "启动");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        mToken = sharedPref.getString(Const.SHARED_PREFERENCES_TOKEN, null);
        isNotificationEnabled = sharedPref.getBoolean(getString(R.string.pref_key_notification), true);
        Resources res = getResources();
        mDaysBefore = sharedPref.getInt(getString(R.string.pref_key_notify_days_before),
                res.getInteger(R.integer.default_days_before));
        mDaysAfter = sharedPref.getInt(getString(R.string.pref_key_notify_days_after),
                res.getInteger(R.integer.default_days_after));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isAlarm = false;
        if (intent != null) {
            isAlarm = intent.getBooleanExtra(INTENT_EXTRA_NAME_ALARM, false);
        }
        if (isAlarm) {
            if (isNotificationEnabled && !TextUtils.isEmpty(mToken)) {
                loadNotifications();
            }
        } else {
            setAlarm();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        writeLogLine(TAG + "关闭");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Const.SHARED_PREFERENCES_TOKEN.equals(key)) {
            mToken = sharedPreferences.getString(key, null);
        } else if (getString(R.string.pref_key_notify_days_before).equals(key)) {
            mDaysBefore = sharedPreferences.getInt(key, mDaysBefore);
        } else if (getString(R.string.pref_key_notify_days_after).equals(key)) {
            mDaysAfter = sharedPreferences.getInt(key, mDaysAfter);
        }
    }

    private void writeLogLine(String log) {
        if (mLogger != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
                mLogger.writeLogLine(sdf.format(new Date()) + " " + log);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadNotifications() {
        writeLogLine("开始获取新片通知");
        Map<String, String> params = new ArrayMap<>();
        params.put(NewMoviesExpressService.QUERY_PARAM_BEFORE, String.valueOf(mDaysBefore));
        params.put(NewMoviesExpressService.QUERY_PARAM_AFTER, String.valueOf(mDaysAfter));
        HttpHelper.mNewMoviesExpressService.notifications(mToken, 0, MAX_NOTIFICATIONS_COUNT,
                NewMoviesExpressService.NOTIFICATIONS_DUE, params
        ).enqueue(new HttpCallback<PagedList<MovieSimple>>() {
            @Override
            public void onSuccess(retrofit2.Call<PagedList<MovieSimple>> call, retrofit2.Response<PagedList<MovieSimple>> response) {
                writeLogLine("获取新片上映提醒成功");
                final PagedList<MovieSimple> movieList = response.body();
                buildNotification(mNotificationId, movieList.getTotal(), movieList.getSubjects());
            }

            @Override
            public void onError(Call<PagedList<MovieSimple>> call, Response<PagedList<MovieSimple>> response, ApiError error) {
                Log.e(TAG, error.getMsg());
                writeLogLine("获取新片上映提醒失败：" + error.getMsg());
            }

            @Override
            public void onFailure(retrofit2.Call<PagedList<MovieSimple>> call, Throwable t) {
                super.onFailure(call, t);
                writeLogLine("获取新片上映提醒失败：" + t.toString());
                if (mRetryTimes < MAX_RETRY_TIMES) {
                    mRetryTimes++;
                    writeLogLine(String.format("重试第%d次", mRetryTimes));
                    loadNotifications();
                }
            }
        });
    }

    private void buildNotification(int id, int total, Collection<MovieSimple> collection) {
        if (collection == null) {
            return;
        }
        if (total == 0 || collection.size() == 0) {
            return;
        }
        String title = String.format(getString(R.string.notification_title), total);
        Collection<String> movieTitles = Collections2.transform(collection, new Function<MovieSimple, String>() {
            @Override
            public String apply(MovieSimple input) {
                return input.getTitle();
            }
        });
        String content = TextUtils.join(", ", movieTitles);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setSmallIcon(getNotificationIcon())
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(title)
                        .setContentText(content);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);
        inboxStyle.setSummaryText(getString(R.string.coming_soon));
        for (MovieSimple movie : collection) {
            inboxStyle.addLine(movie.getTitle());
        }
        mBuilder.setStyle(inboxStyle);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, NotificationsActivity.class);
        resultIntent.putExtra(NotificationsActivity.INTENT_EXTRA_NOTIFICATIONS_FILTER,
                NewMoviesExpressService.NOTIFICATIONS_DUE);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mNotificationId allows you to update the notification later on.
        mNotificationManager.notify(id, mBuilder.build());
    }

    private int getNotificationIcon() {
        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return whiteIcon ? R.drawable.notification_icon_silhouette : R.drawable.notification_icon;
    }

    private void setAlarm() {
        Intent intent = new Intent(this, BackgroundService.class);
        intent.putExtra(INTENT_EXTRA_NAME_ALARM, true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
        AlarmManager alarmManager = (AlarmManager)
                getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(alarmType, SystemClock.elapsedRealtime() + 1000, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    public class MyBinder extends Binder {
        // TODO 暂时不用
    }
}
