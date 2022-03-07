package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NestNotificationListener extends NotificationListenerService {
    private static final String TAG = NestNotificationListener.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

//        StatusBarNotification[] notifications = getActiveNotifications();

        if (!sbn.getPackageName().equals(getString(R.string.nest_package))) {
            Log.d(TAG, "skipping notification, it's not from '"
                    +getString(R.string.nest_package)+"', actually from '"+sbn.getNotification()+"'");

            // TODO remove
//            StatusBarNotification[] notifications = getActiveNotifications();
//            for (StatusBarNotification notification : notifications) {
//                if (notification.getPackageName().equals(TARGET_PACKAGE)) {
//                    onNotificationPosted(notification);
//                }
//            }

            return;
        }

        Bitmap picture = null;
        String title = null;
        String text = null;
        boolean good = false;

        Notification notification = sbn.getNotification();
        String template = notification.extras.getString(Notification.EXTRA_TEMPLATE);
        if (template != null && template.equals("android.app.Notification$BigPictureStyle")) {
            picture = (Bitmap) notification.extras.get(Notification.EXTRA_PICTURE);
            title = notification.extras.getString(Notification.EXTRA_TITLE);
            text = notification.extras.getString(Notification.EXTRA_TEXT);
            good = true;
        } else {
            // Looks like the wearableExtender is a good options
//        new Notification.WearableExtender(((Notification)notifications[10].notification))
            Notification.WearableExtender wearableExtender = new Notification.WearableExtender(sbn.getNotification());
            // TODO figure out "getPages" alternative, it's deprecated
            List<Notification> pages = wearableExtender.getPages();
            if (pages != null) {
                for (Notification page : pages) {
                    template = page.extras.getString(Notification.EXTRA_TEMPLATE);
                    if (template == null || !template.equals("android.app.Notification$BigPictureStyle")) {
                        continue;
                    }
                    picture = (Bitmap) page.extras.get(Notification.EXTRA_PICTURE);
                    title = page.extras.getString(Notification.EXTRA_TITLE);
                    text = page.extras.getString(Notification.EXTRA_TEXT);
                    good = true;
                    break;
                }
            }
        }
        // TODO get other types (text style, etc...) e.g. filter reminders, smoke alarm checking

        if (good) {
            Notification noti = new NotificationCompat.Builder(this, getString(R.string.channel_id))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.elmo_gif)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setLargeIcon(picture)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(picture)
                            .bigLargeIcon(null))
                    .build();

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.notify((int) System.currentTimeMillis(), noti);

            notificationManager.cancel(sbn.getId());

            /*
             RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.decnotification);
             contentView.setImageViewBitmap(R.id.imageView2, picture);
             RemoteViews bigContentView = new RemoteViews(getPackageName(), R.layout.decbignotification);
             bigContentView.setImageViewBitmap(R.id.aqq, picture);
             //        contentView.setImageViewResource(R.id.);
             Notification noti = new NotificationCompat.Builder(this, "HUGO_APP")
             .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
             .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
             .setSmallIcon(R.drawable.elmo_gif)
             .setLargeIcon(picture)
             .setContent(contentView)
             .setCustomContentView(contentView)
             //                .setCustomHeadsUpContentView(contentView)
             .setCustomBigContentView(bigContentView)
             //                .setCustomBigContentView(bigContentView)
             .setContentTitle(title)
             .setContentText(text)
             .setPriority(NotificationCompat.PRIORITY_DEFAULT)
             //                .setOngoing(true)
             //                            .setAutoCancel(true)
             .build();
             //        Notification.Builder nb = Notification.Builder.recoverBuilder(getApplicationContext(), noti);
             //        (new NotificationCompat.DecoratedCustomViewStyle()).makeContentView(nb);
             */
        }


        // TODO check extras for android.template = android.app.Notification$DecoratedCustomViewStyle

        // TODO go through extras and check android.contains.customView

        // TODO maybe inspect extras to find texts matches we care about before proceeding.
        //      I'm not sure if extra has it, maybe it'll be required to go to contentView...

        // TODO indeed, extras has android.title and android.text set.

        // the target notification has set the contentView, bigContentView and headsUpContentView
        Notification.Builder nb = Notification.Builder
                .recoverBuilder(getApplicationContext(), sbn.getNotification());
        RemoteViews contentView = nb.createContentView();
        if (contentView == null) {
            Log.e(TAG, "onNotificationPosted: contentView is null");
            return;
        }
        RemoteViews bigContentView = nb.createBigContentView();
        if (bigContentView == null) {
            Log.e(TAG, "onNotificationPosted: bigContentView is null");
            return;
        }
        RemoteViews headsUpContentView = nb.createHeadsUpContentView();
        if (headsUpContentView == null) {
            Log.e(TAG, "onNotificationPosted: headsUpContentView is null");
            return;
        }

        /*
         What are we looking for?
         - Title: From extras? If not, contentView should have it, but need to find viewID+ReflectionAction.
         - Text: same comments as Title.
         - actions (intentions?): e.g. reply, on touch action. So far, no idea on how to get it.
         - Images: bigContentView should have it in a ReflectionAction with methodName set to "setImageURI",
                   but that ReflectionAction will be deep inside ViewGroupActionAdd->mNestedViews->mActions...
                   This notification has multiple images (each in a ViewGroupActionAdd), my guess is that
                   they are close frames used to make it animated (looks like animated gifs are not supported).
                   Interesting that there are multiple ViewGroupActionRemove too, is that to run in loop in
                   conjunction to the ViewGroupActionAdd ones?
         */

        deep(bigContentView);
    }

    void deep(RemoteViews views) {
        Class targetClass;
        if (views.getClass().getSimpleName().equals("RemoteViews")) {
            targetClass = views.getClass();
        } else if (views.getClass().getSuperclass() != null &&
                views.getClass().getSuperclass().getSimpleName().equals("RemoteViews")) {
            targetClass = views.getClass().getSuperclass();
        } else {
            Log.e(TAG, "deep: cannot use the provided remote view");
            return;
        }
        try {
            Field outerField = targetClass.getDeclaredField("mActions");
            outerField.setAccessible(true);
            ArrayList<Object> actions = (ArrayList<Object>) outerField.get(views);

            for (Object action : actions) {
                String name = action.getClass().getSimpleName();
                Field innerFields[] = action.getClass().getDeclaredFields();
                Field innerFieldsSuper[] = action.getClass().getSuperclass().getDeclaredFields();

                if (name.equals("ViewGroupActionAdd")) {

                } else {

                }
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "deep: ", e);
            return;
        }

    }
}
