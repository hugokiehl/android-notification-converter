package com.example.myapplication;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.versionedparcelable.ParcelUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NotificationListener extends NotificationListenerService {

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        System.out.println("");
        System.out.println("------------------------------------------------------------------------------------------");
        Notification notification = sbn.getNotification();
        Bundle bundle = notification.extras;
        printBundleContent("extra", bundle);
//        notification.contentView
        String pack = sbn.getPackageName();

        Notification.Builder nb = Notification.Builder.recoverBuilder(getApplicationContext(), notification);
        RemoteViews contentView = nb.createContentView();
        RemoteViews bigContentView = nb.createBigContentView();
        if (contentView != null && bigContentView != null) {
            List<String> texts = getText(contentView);
            for (String text : texts) {
                System.out.println("CONTENT TEXT: "+text);
            }
            texts = getText(bigContentView);
            for (String text : texts) {
                System.out.println("BIG CONTENT TEXT: "+text);
            }
        }

        Bundle extras = sbn.getNotification().extras;

        int iconId = extras.getInt(Notification.EXTRA_SMALL_ICON);

        try {
            PackageManager manager = getPackageManager();
            Resources resources = manager.getResourcesForApplication(pack);

//            Drawable icon = resources.getDrawable(iconId);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (extras.containsKey(Notification.EXTRA_PICTURE)) {
            // this bitmap contain the picture attachment
            Bitmap bmp = (Bitmap) extras.get(Notification.EXTRA_PICTURE);
        }
    }

    void printBundleContent(String name, Bundle bundle) {
        System.out.println(">>>>> start BUNDLE ["+name+"] <<<<<<");
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            Object obj = bundle.get(key);
            if (obj instanceof String) {
                System.out.println("KEY: " + key + " - STRING CONTENT: " + ((String) obj));
            } else if (obj instanceof Boolean) {
                System.out.println("KEY: " + key + " - BOOLEAN CONTENT: " + ((Boolean) obj));
            } else if (obj instanceof Bundle) {
                printBundleContent(key, (Bundle) obj);
            } else {
                String type = "n/a";
                if (obj != null) {
                    type = obj.getClass().getSimpleName();
                }
                System.out.println("KEY: " + key + " - CONTENT TYPE: "+ type);
            }
            int i=0;
            i++;
//            if (obj instanceof ApplicationInfo) {
//                ApplicationInfo info = (ApplicationInfo) obj;
//                System.out.println("DATA_DIR");
//                printPathContent(info.dataDir);
//                System.out.println("SOURCE_DIR");
//                printPathContent(info.sourceDir);
//            }
            if (key.toLowerCase().contains("style")) {
                System.out.println("style found");
            }
            if (key.equals("android.messagingStyleUser")) {

            }
        }
        System.out.println(">>>>> end BUNDLE ["+name+"] <<<<<<");
    }

    void printPathContent(String baseDir) {
        if (baseDir != null) {
            File file = new File(baseDir);
            if (file.exists()) {
                if (file.isDirectory()) {
                    String path = file.getPath();
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                    System.out.println(path);
                    for (String f : Objects.requireNonNull(file.list())) {
                        printPathContent(Paths.get(baseDir, f).toString());
                    }
                } else {
                    System.out.println(file.getPath());
                }
            }
        }
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    public static List<String> getText(RemoteViews views)
    {

        if (views == null) {
            return new ArrayList<>();
        }

        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        List<String> text = new ArrayList<String>();
        try
        {
            Field field = views.getClass().getSuperclass().getDeclaredField("mActions");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

            // Find the setText() and setTime() reflection actions
            for (Parcelable p : actions)
            {
                if (!p.getClass().getSimpleName().equals("ReflectionAction")) {
                    continue;
                }

                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);

                int viewId = parcel.readInt();
//                ReadWriteHelper DEFAULT = new ReadWriteHelper();
//                Parcel.obtain().readString()





                Class secretClass = views.getClass();

                try {
                    Map<Integer, String> text1 = new HashMap<Integer, String>();

                    Field outerField = secretClass.getSuperclass().getDeclaredField("mActions");
                    outerField.setAccessible(true);
                    ArrayList<Object> actions1 = (ArrayList<Object>) outerField.get(views);

                    for (Object action : actions1) {
                        Field innerFields[] = action.getClass().getDeclaredFields();
                        Field innerFieldsSuper[] = action.getClass().getSuperclass().getDeclaredFields();

                        Object value = null;
                        String type = null;
                        Integer viewId1 = null;
                        for (Field field1 : innerFields) {
                            field1.setAccessible(true);
                            if (field1.getName().equals("value")) {
                                value = field1.get(action);
                            } else if (field1.getName().equals("methodName")) {
                                type = (String) field1.get(action);
                            }
                        }
                        for (Field field1 : innerFieldsSuper) {
                            field1.setAccessible(true);
                            if (field1.getName().equals("viewId")) {
                                viewId1 = field1.getInt(action);
                            }
                        }

                        if (value != null && type != null && viewId1 != null) { // && (type == 9 || type == 10)) {
                            text1.put(viewId1, value.toString());
                        }
                    }

                    System.out.println("title is: " + text1.get(16908310));
                    System.out.println("info is: " + text1.get(16909082));
                    System.out.println("text is: " + text1.get(16908358));
                } catch (Exception e) {
                    e.printStackTrace();
                }








                Class c;
                c = Class.forName("android.os.Parcel");
                String aaa = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                Field m = c.getField("mReadWriteHelper");
//                Object o = m.invoke(null);





                String methodName = parcel.readString();
                if (methodName == null) continue;
                int type = parcel.readInt();

                // Save strings
                if (methodName.equals("setText"))
                {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();

                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    text.add(t);
                }

                // Save times. Comment this section out if the notification time isn't important
                else if (methodName.equals("setTime"))
                {
                    // Parameter type (5 = Long)
                    parcel.readInt();

                    String t = new SimpleDateFormat("h:mm a").format(new Date(parcel.readLong()));
                    text.add(t);
                }

                parcel.recycle();
            }
        }

        // It's not usually good style to do this, but then again, neither is the use of reflection...
        catch (Exception e)
        {
            Log.e("NotificationClassifier", e.toString());
        }

        return text;
    }

}
