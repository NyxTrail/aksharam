package in.digistorm.aksharam;

/*
 * Copyright (c) 2022 Alan M Varghese <alan@digistorm.in>
 *
 * This files is part of Aksharam, a script teaching app for Indic
 * languages.
 *
 * Aksharam is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aksharam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even teh implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GlobalSettings {
    private static GlobalSettings globalSettings;

    private String logTag = "GlobalSettings";

    // Dark/Light mode
    private boolean darkMode;

    // Remember, URLs must end in a '/' or Retrofit rebels
    private final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();
    // max idle time a thread is kept alive
    private final int KEEP_ALIVE_TIME = 20;
    private final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private final String threadFactoryname = "GlobalThreadFactory";

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            NUMBER_OF_CORES,
            NUMBER_OF_CORES,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            workQueue);

    private class SimpleThreadFactory implements ThreadFactory {
        private final String name;

        SimpleThreadFactory() {
            this.name = threadFactoryname;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }

    // We will keep the listeners in a named map so that their owners can
    // replace existing listeners (not so straight forward if this is an ArrayList)
    private final Map<String, DataFileListChanged> dataFileListChangedListeners;

    private GlobalSettings(Context context) {
        threadPoolExecutor.setThreadFactory(new SimpleThreadFactory());
        dataFileListChangedListeners = new HashMap<>();
        darkMode = context.getSharedPreferences("dark_mode", Context.MODE_PRIVATE).getBoolean("dark_mode", false);
    }

    // this must be called at the point where there is a change in the data file list
    // usually, the Settings activity
    public void invokeDataFileListChangedListeners() {
        Iterator<String> keys = dataFileListChangedListeners.keySet().iterator();
        for(String key: dataFileListChangedListeners.keySet()) {
            Log.d(logTag, "Invoking " + key);
            Objects.requireNonNull(dataFileListChangedListeners.get(key)).onDataFileListChanged();
        }
    }

    public void addDataFileListChangedListener(String name, DataFileListChanged l) {
        dataFileListChangedListeners.put(name, l);
    }


    public void setDarkMode(boolean v, Context context) {
        darkMode = v;
        context.getSharedPreferences("dark_mode", Context.MODE_PRIVATE).edit()
                .putBoolean("dark_mode", v).apply();
    }

    public boolean getDarkMode() {
        return darkMode;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    // this method should be called only once in the entire project
    public static GlobalSettings createInstance(Context context) {
        globalSettings = new GlobalSettings(context);
        return globalSettings;
    }

    public static GlobalSettings getInstance() {
        return globalSettings;
    }
}
