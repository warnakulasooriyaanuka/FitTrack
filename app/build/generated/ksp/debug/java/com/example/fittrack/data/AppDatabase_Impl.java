package com.example.fittrack.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile WorkoutDao _workoutDao;

  private volatile TrackerDao _trackerDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `workouts` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `type` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `calories` INTEGER NOT NULL, `date` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_activity` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `steps` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `meals` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `mealType` TEXT NOT NULL, `name` TEXT NOT NULL, `calories` INTEGER NOT NULL, `protein` INTEGER NOT NULL, `carbs` INTEGER NOT NULL, `fat` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_logs` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `quality` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `planned_workouts` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `dayOfWeek` INTEGER NOT NULL, `type` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `note` TEXT NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `water_logs` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `ml` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `body_metrics` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `weightKg` REAL NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '617b7d262e324ac8af3079370165fe68')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `workouts`");
        db.execSQL("DROP TABLE IF EXISTS `daily_activity`");
        db.execSQL("DROP TABLE IF EXISTS `meals`");
        db.execSQL("DROP TABLE IF EXISTS `sleep_logs`");
        db.execSQL("DROP TABLE IF EXISTS `planned_workouts`");
        db.execSQL("DROP TABLE IF EXISTS `water_logs`");
        db.execSQL("DROP TABLE IF EXISTS `body_metrics`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsWorkouts = new HashMap<String, TableInfo.Column>(7);
        _columnsWorkouts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("durationMin", new TableInfo.Column("durationMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("calories", new TableInfo.Column("calories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWorkouts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWorkouts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWorkouts = new TableInfo("workouts", _columnsWorkouts, _foreignKeysWorkouts, _indicesWorkouts);
        final TableInfo _existingWorkouts = TableInfo.read(db, "workouts");
        if (!_infoWorkouts.equals(_existingWorkouts)) {
          return new RoomOpenHelper.ValidationResult(false, "workouts(com.example.fittrack.data.Workout).\n"
                  + " Expected:\n" + _infoWorkouts + "\n"
                  + " Found:\n" + _existingWorkouts);
        }
        final HashMap<String, TableInfo.Column> _columnsDailyActivity = new HashMap<String, TableInfo.Column>(5);
        _columnsDailyActivity.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("day", new TableInfo.Column("day", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("steps", new TableInfo.Column("steps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDailyActivity = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDailyActivity = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDailyActivity = new TableInfo("daily_activity", _columnsDailyActivity, _foreignKeysDailyActivity, _indicesDailyActivity);
        final TableInfo _existingDailyActivity = TableInfo.read(db, "daily_activity");
        if (!_infoDailyActivity.equals(_existingDailyActivity)) {
          return new RoomOpenHelper.ValidationResult(false, "daily_activity(com.example.fittrack.data.DailyActivity).\n"
                  + " Expected:\n" + _infoDailyActivity + "\n"
                  + " Found:\n" + _existingDailyActivity);
        }
        final HashMap<String, TableInfo.Column> _columnsMeals = new HashMap<String, TableInfo.Column>(11);
        _columnsMeals.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("day", new TableInfo.Column("day", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("mealType", new TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("calories", new TableInfo.Column("calories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("protein", new TableInfo.Column("protein", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("carbs", new TableInfo.Column("carbs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("fat", new TableInfo.Column("fat", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMeals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMeals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMeals = new TableInfo("meals", _columnsMeals, _foreignKeysMeals, _indicesMeals);
        final TableInfo _existingMeals = TableInfo.read(db, "meals");
        if (!_infoMeals.equals(_existingMeals)) {
          return new RoomOpenHelper.ValidationResult(false, "meals(com.example.fittrack.data.Meal).\n"
                  + " Expected:\n" + _infoMeals + "\n"
                  + " Found:\n" + _existingMeals);
        }
        final HashMap<String, TableInfo.Column> _columnsSleepLogs = new HashMap<String, TableInfo.Column>(7);
        _columnsSleepLogs.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepLogs.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepLogs.put("day", new TableInfo.Column("day", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepLogs.put("durationMin", new TableInfo.Column("durationMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepLogs.put("quality", new TableInfo.Column("quality", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepLogs.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepLogs.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSleepLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSleepLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSleepLogs = new TableInfo("sleep_logs", _columnsSleepLogs, _foreignKeysSleepLogs, _indicesSleepLogs);
        final TableInfo _existingSleepLogs = TableInfo.read(db, "sleep_logs");
        if (!_infoSleepLogs.equals(_existingSleepLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "sleep_logs(com.example.fittrack.data.SleepLog).\n"
                  + " Expected:\n" + _infoSleepLogs + "\n"
                  + " Found:\n" + _existingSleepLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsPlannedWorkouts = new HashMap<String, TableInfo.Column>(7);
        _columnsPlannedWorkouts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlannedWorkouts.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlannedWorkouts.put("dayOfWeek", new TableInfo.Column("dayOfWeek", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlannedWorkouts.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlannedWorkouts.put("durationMin", new TableInfo.Column("durationMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlannedWorkouts.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlannedWorkouts.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlannedWorkouts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlannedWorkouts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlannedWorkouts = new TableInfo("planned_workouts", _columnsPlannedWorkouts, _foreignKeysPlannedWorkouts, _indicesPlannedWorkouts);
        final TableInfo _existingPlannedWorkouts = TableInfo.read(db, "planned_workouts");
        if (!_infoPlannedWorkouts.equals(_existingPlannedWorkouts)) {
          return new RoomOpenHelper.ValidationResult(false, "planned_workouts(com.example.fittrack.data.PlannedWorkout).\n"
                  + " Expected:\n" + _infoPlannedWorkouts + "\n"
                  + " Found:\n" + _existingPlannedWorkouts);
        }
        final HashMap<String, TableInfo.Column> _columnsWaterLogs = new HashMap<String, TableInfo.Column>(5);
        _columnsWaterLogs.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterLogs.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterLogs.put("day", new TableInfo.Column("day", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterLogs.put("ml", new TableInfo.Column("ml", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterLogs.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWaterLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWaterLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWaterLogs = new TableInfo("water_logs", _columnsWaterLogs, _foreignKeysWaterLogs, _indicesWaterLogs);
        final TableInfo _existingWaterLogs = TableInfo.read(db, "water_logs");
        if (!_infoWaterLogs.equals(_existingWaterLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "water_logs(com.example.fittrack.data.WaterLog).\n"
                  + " Expected:\n" + _infoWaterLogs + "\n"
                  + " Found:\n" + _existingWaterLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsBodyMetrics = new HashMap<String, TableInfo.Column>(5);
        _columnsBodyMetrics.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBodyMetrics.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBodyMetrics.put("day", new TableInfo.Column("day", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBodyMetrics.put("weightKg", new TableInfo.Column("weightKg", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBodyMetrics.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBodyMetrics = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBodyMetrics = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBodyMetrics = new TableInfo("body_metrics", _columnsBodyMetrics, _foreignKeysBodyMetrics, _indicesBodyMetrics);
        final TableInfo _existingBodyMetrics = TableInfo.read(db, "body_metrics");
        if (!_infoBodyMetrics.equals(_existingBodyMetrics)) {
          return new RoomOpenHelper.ValidationResult(false, "body_metrics(com.example.fittrack.data.BodyMetric).\n"
                  + " Expected:\n" + _infoBodyMetrics + "\n"
                  + " Found:\n" + _existingBodyMetrics);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "617b7d262e324ac8af3079370165fe68", "25c4ed14353e8ecc4be36bd71b97343d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "workouts","daily_activity","meals","sleep_logs","planned_workouts","water_logs","body_metrics");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `workouts`");
      _db.execSQL("DELETE FROM `daily_activity`");
      _db.execSQL("DELETE FROM `meals`");
      _db.execSQL("DELETE FROM `sleep_logs`");
      _db.execSQL("DELETE FROM `planned_workouts`");
      _db.execSQL("DELETE FROM `water_logs`");
      _db.execSQL("DELETE FROM `body_metrics`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(WorkoutDao.class, WorkoutDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TrackerDao.class, TrackerDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public WorkoutDao workoutDao() {
    if (_workoutDao != null) {
      return _workoutDao;
    } else {
      synchronized(this) {
        if(_workoutDao == null) {
          _workoutDao = new WorkoutDao_Impl(this);
        }
        return _workoutDao;
      }
    }
  }

  @Override
  public TrackerDao trackerDao() {
    if (_trackerDao != null) {
      return _trackerDao;
    } else {
      synchronized(this) {
        if(_trackerDao == null) {
          _trackerDao = new TrackerDao_Impl(this);
        }
        return _trackerDao;
      }
    }
  }
}
