package com.ctlev.app.persistence.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.RoomDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;
import androidx.annotation.NonNull;

import com.ctlev.app.persistence.AppExecutors;
import com.ctlev.app.persistence.db.dao.EmployeeDao;
import com.ctlev.app.persistence.db.tables.Employee;

@Database(entities = {Employee.class} ,exportSchema = true, version = 1)
public abstract class AppDatabase  extends RoomDatabase {

    private static AppDatabase sInstance;
    public abstract EmployeeDao productDao();

    @VisibleForTesting
    public static final String DATABASE_NAME = "ctlev.db";
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();
    public static AppDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                    sInstance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static AppDatabase buildDatabase(final Context appContext,
                                             final AppExecutors executors) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.diskIO().execute(() -> {

                            // Generate the data for pre-population
                            AppDatabase database = AppDatabase.getInstance(appContext, executors);

                            // notify that the database was created and it's ready to be used
                            database.setDatabaseCreated();
                        });
                    }
                })
                //.addMigrations(MIGRATION_1_2)
                .build();
    }

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }


    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

}
