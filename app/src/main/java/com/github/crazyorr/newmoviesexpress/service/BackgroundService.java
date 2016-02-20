package com.github.crazyorr.newmoviesexpress.service;

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
import android.text.TextUtils;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.activity.MainActivity;
import com.github.crazyorr.newmoviesexpress.activity.NotificationsActivity;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.util.GlobalVar;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.util.Util;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.rollinglogger.RollingLogger;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BackgroundService extends Service {

    protected static final String TAG = BackgroundService.class.getSimpleName();
    private static final String INTENT_EXTRA_NAME_ALARM = "INTENT_EXTRA_NAME_ALARM";

    private static final int MAX_RETRY_TIMES = 3;

    private MyBinder mBinder;
    private RollingLogger mLogger;

    private int mNotificationId = 1;

    private int mRetryTimes;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isAlarm = intent.getBooleanExtra(INTENT_EXTRA_NAME_ALARM, false);
        if (isAlarm) {
            getNotifications();
        } else {
            setAlarm();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void getNotifications() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNotificationEnabled = sharedPref.getBoolean(getString(R.string.pref_key_notification), true);
        if (isNotificationEnabled) {
            writeLogLine("开始获取新片通知");

            HttpHelper.mNewMoviesExpressService.notifications().enqueue(new HttpCallback<List<MovieSimple>>() {
                @Override
                public void onSuccess(retrofit2.Call<List<MovieSimple>> call, retrofit2.Response<List<MovieSimple>> response) {
                    writeLogLine("获取新片通知成功");
                    final List<MovieSimple> list = response.body();
                    if (list != null) {
                        buildNotification(mNotificationId, Collections2.filter(list, new Predicate<MovieSimple>() {
                            @Override
                            public boolean apply(MovieSimple input) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                Date pubDate = null;
                                try {
                                    pubDate = sdf.parse(input.getMainland_pubdate());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (pubDate != null) {
                                    Date todayDate = new Date();
                                    long diff = pubDate.getTime() - todayDate.getTime();
                                    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                                    Resources res = getResources();
                                    int daysBefore = sharedPref.getInt(getString(R.string.pref_key_notify_since),
                                            res.getInteger(R.integer.default_days_before));
                                    int daysAfter = sharedPref.getInt(getString(R.string.pref_key_notify_until),
                                            res.getInteger(R.integer.default_days_after));
                                    return days >= (0 - daysAfter) && days <= daysBefore;
                                }
                                return false;
                            }
                        }));
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<List<MovieSimple>> call, Throwable t) {
                    super.onFailure(call, t);
                    writeLogLine("获取新片通知失败：" + t.toString());
                    if (mRetryTimes < MAX_RETRY_TIMES) {
                        mRetryTimes++;
                        writeLogLine(String.format("重试第%d次", mRetryTimes));
                        getNotifications();
                    }
                }
            });
        }
    }

    private void buildNotification(int id, Collection<MovieSimple> collection) {
        if (collection == null) {
            return;
        }
        int count = collection.size();
        if (count == 0) {
            return;
        }
        String title = String.format(getString(R.string.notification_title), count);
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
        GlobalVar.notificationMovies = new ArrayList<>(collection);

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
