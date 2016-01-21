package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.widget.TodayWidgetProvider;

public class TodayWidgetIntentService extends IntentService {

    private static final String[] FOOTBALL_COLUMNS = {
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.MATCH_DAY,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };

    //For matching the projection
    private static final int INDEX_LEAGUE = 0;
    private static final int INDEX_MATCH_TIME = 1;
    private static final int INDEX_HOME = 2;
    private static final int INDEX_AWAY = 3;
    private static final int INDEX_HOME_GOAL = 4;
    private static final int INDEX_AWAY_GOAL = 5;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodayWidgetProvider.class));

        Uri scoreWithDate = DatabaseContract.scores_table.buildScoreWithDate();

        Date now = new Date(System.currentTimeMillis() - 86400000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String[] today = new String[1];
        today[0] = simpleDateFormat.format(now);

        Cursor data = getContentResolver()
                .query( scoreWithDate,
                        FOOTBALL_COLUMNS, //projections
                        null, //selection
                        today, //selection args
                        null //sort order
                        );

        if (data == null) {
            data.close(); //Should I put close here?
            return;
        }

        if(!data.moveToFirst()) {
            data.close();
            return;
        }

        String match_date = data.getString(INDEX_MATCH_TIME);
        String home_team = data.getString(INDEX_HOME);
        String away_team = data.getString(INDEX_AWAY);
        String score = Utilies.getScores(data.getInt(INDEX_HOME_GOAL), data.getInt(INDEX_AWAY_GOAL));

        data.close();

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_today;
            RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), layoutId);

            views.setTextViewText(R.id.widget_data_textview, match_date);
            views.setTextViewText(R.id.widget_home_name, home_team);
            views.setTextViewText(R.id.widget_away_name, away_team);
            views.setTextViewText(R.id.widget_score_textview, score);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                views.setContentDescription(R.id.widget_data_textview, match_date);
                views.setContentDescription(R.id.widget_home_name, home_team);
                views.setContentDescription(R.id.widget_away_name, away_team);
                views.setContentDescription(R.id.widget_score_textview, score);
            }

            Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widjet, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }

        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if(options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp, displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }
}
