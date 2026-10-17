package com.example.fittrack.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TrackerDao_Impl implements TrackerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DailyActivity> __insertionAdapterOfDailyActivity;

  private final EntityInsertionAdapter<Meal> __insertionAdapterOfMeal;

  private final EntityInsertionAdapter<SleepLog> __insertionAdapterOfSleepLog;

  private final EntityInsertionAdapter<PlannedWorkout> __insertionAdapterOfPlannedWorkout;

  private final EntityInsertionAdapter<WaterLog> __insertionAdapterOfWaterLog;

  private final EntityInsertionAdapter<BodyMetric> __insertionAdapterOfBodyMetric;

  private final EntityDeletionOrUpdateAdapter<Meal> __deletionAdapterOfMeal;

  private final EntityDeletionOrUpdateAdapter<SleepLog> __deletionAdapterOfSleepLog;

  private final EntityDeletionOrUpdateAdapter<PlannedWorkout> __deletionAdapterOfPlannedWorkout;

  private final EntityDeletionOrUpdateAdapter<BodyMetric> __deletionAdapterOfBodyMetric;

  public TrackerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDailyActivity = new EntityInsertionAdapter<DailyActivity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `daily_activity` (`id`,`userId`,`day`,`steps`,`synced`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DailyActivity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getDay());
        statement.bindLong(4, entity.getSteps());
        final int _tmp = entity.getSynced() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
    this.__insertionAdapterOfMeal = new EntityInsertionAdapter<Meal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `meals` (`id`,`userId`,`day`,`mealType`,`name`,`calories`,`protein`,`carbs`,`fat`,`createdAt`,`synced`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Meal entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getDay());
        statement.bindString(4, entity.getMealType());
        statement.bindString(5, entity.getName());
        statement.bindLong(6, entity.getCalories());
        statement.bindLong(7, entity.getProtein());
        statement.bindLong(8, entity.getCarbs());
        statement.bindLong(9, entity.getFat());
        statement.bindLong(10, entity.getCreatedAt());
        final int _tmp = entity.getSynced() ? 1 : 0;
        statement.bindLong(11, _tmp);
      }
    };
    this.__insertionAdapterOfSleepLog = new EntityInsertionAdapter<SleepLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sleep_logs` (`id`,`userId`,`day`,`durationMin`,`quality`,`createdAt`,`synced`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SleepLog entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getDay());
        statement.bindLong(4, entity.getDurationMin());
        statement.bindLong(5, entity.getQuality());
        statement.bindLong(6, entity.getCreatedAt());
        final int _tmp = entity.getSynced() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__insertionAdapterOfPlannedWorkout = new EntityInsertionAdapter<PlannedWorkout>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `planned_workouts` (`id`,`userId`,`dayOfWeek`,`type`,`durationMin`,`note`,`synced`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlannedWorkout entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindLong(3, entity.getDayOfWeek());
        statement.bindString(4, entity.getType());
        statement.bindLong(5, entity.getDurationMin());
        statement.bindString(6, entity.getNote());
        final int _tmp = entity.getSynced() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__insertionAdapterOfWaterLog = new EntityInsertionAdapter<WaterLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `water_logs` (`id`,`userId`,`day`,`ml`,`synced`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WaterLog entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getDay());
        statement.bindLong(4, entity.getMl());
        final int _tmp = entity.getSynced() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
    this.__insertionAdapterOfBodyMetric = new EntityInsertionAdapter<BodyMetric>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `body_metrics` (`id`,`userId`,`day`,`weightKg`,`synced`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BodyMetric entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getDay());
        statement.bindDouble(4, entity.getWeightKg());
        final int _tmp = entity.getSynced() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
    this.__deletionAdapterOfMeal = new EntityDeletionOrUpdateAdapter<Meal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `meals` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Meal entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfSleepLog = new EntityDeletionOrUpdateAdapter<SleepLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sleep_logs` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SleepLog entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfPlannedWorkout = new EntityDeletionOrUpdateAdapter<PlannedWorkout>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `planned_workouts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlannedWorkout entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__deletionAdapterOfBodyMetric = new EntityDeletionOrUpdateAdapter<BodyMetric>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `body_metrics` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BodyMetric entity) {
        statement.bindString(1, entity.getId());
      }
    };
  }

  @Override
  public Object upsertActivity(final DailyActivity a,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDailyActivity.insert(a);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertMeal(final Meal m, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMeal.insert(m);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertMeals(final List<Meal> list, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMeal.insert(list);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertSleep(final SleepLog s, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSleepLog.insert(s);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertSleeps(final List<SleepLog> list,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSleepLog.insert(list);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertPlan(final PlannedWorkout p, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlannedWorkout.insert(p);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertPlans(final List<PlannedWorkout> list,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlannedWorkout.insert(list);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertWater(final WaterLog w, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWaterLog.insert(w);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertBody(final BodyMetric b, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBodyMetric.insert(b);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertBodies(final List<BodyMetric> list,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBodyMetric.insert(list);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMeal(final Meal m, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMeal.handle(m);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSleep(final SleepLog s, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSleepLog.handle(s);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlan(final PlannedWorkout p, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPlannedWorkout.handle(p);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBody(final BodyMetric b, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBodyMetric.handle(b);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DailyActivity>> observeActivity(final String uid) {
    final String _sql = "SELECT * FROM daily_activity WHERE userId = ? ORDER BY day DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_activity"}, new Callable<List<DailyActivity>>() {
      @Override
      @NonNull
      public List<DailyActivity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "steps");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<DailyActivity> _result = new ArrayList<DailyActivity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyActivity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final int _tmpSteps;
            _tmpSteps = _cursor.getInt(_cursorIndexOfSteps);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new DailyActivity(_tmpId,_tmpUserId,_tmpDay,_tmpSteps,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object activityById(final String id,
      final Continuation<? super DailyActivity> $completion) {
    final String _sql = "SELECT * FROM daily_activity WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DailyActivity>() {
      @Override
      @Nullable
      public DailyActivity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "steps");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final DailyActivity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final int _tmpSteps;
            _tmpSteps = _cursor.getInt(_cursorIndexOfSteps);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _result = new DailyActivity(_tmpId,_tmpUserId,_tmpDay,_tmpSteps,_tmpSynced);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object unsyncedActivity(final String uid,
      final Continuation<? super List<DailyActivity>> $completion) {
    final String _sql = "SELECT * FROM daily_activity WHERE userId = ? AND synced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DailyActivity>>() {
      @Override
      @NonNull
      public List<DailyActivity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "steps");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<DailyActivity> _result = new ArrayList<DailyActivity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyActivity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final int _tmpSteps;
            _tmpSteps = _cursor.getInt(_cursorIndexOfSteps);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new DailyActivity(_tmpId,_tmpUserId,_tmpDay,_tmpSteps,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Meal>> observeMeals(final String uid) {
    final String _sql = "SELECT * FROM meals WHERE userId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"meals"}, new Callable<List<Meal>>() {
      @Override
      @NonNull
      public List<Meal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<Meal> _result = new ArrayList<Meal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Meal _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final String _tmpMealType;
            _tmpMealType = _cursor.getString(_cursorIndexOfMealType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final int _tmpProtein;
            _tmpProtein = _cursor.getInt(_cursorIndexOfProtein);
            final int _tmpCarbs;
            _tmpCarbs = _cursor.getInt(_cursorIndexOfCarbs);
            final int _tmpFat;
            _tmpFat = _cursor.getInt(_cursorIndexOfFat);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new Meal(_tmpId,_tmpUserId,_tmpDay,_tmpMealType,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpCreatedAt,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object unsyncedMeals(final String uid,
      final Continuation<? super List<Meal>> $completion) {
    final String _sql = "SELECT * FROM meals WHERE userId = ? AND synced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Meal>>() {
      @Override
      @NonNull
      public List<Meal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<Meal> _result = new ArrayList<Meal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Meal _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final String _tmpMealType;
            _tmpMealType = _cursor.getString(_cursorIndexOfMealType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final int _tmpProtein;
            _tmpProtein = _cursor.getInt(_cursorIndexOfProtein);
            final int _tmpCarbs;
            _tmpCarbs = _cursor.getInt(_cursorIndexOfCarbs);
            final int _tmpFat;
            _tmpFat = _cursor.getInt(_cursorIndexOfFat);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new Meal(_tmpId,_tmpUserId,_tmpDay,_tmpMealType,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpCreatedAt,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SleepLog>> observeSleep(final String uid) {
    final String _sql = "SELECT * FROM sleep_logs WHERE userId = ? ORDER BY day DESC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sleep_logs"}, new Callable<List<SleepLog>>() {
      @Override
      @NonNull
      public List<SleepLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMin");
          final int _cursorIndexOfQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "quality");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<SleepLog> _result = new ArrayList<SleepLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SleepLog _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final int _tmpDurationMin;
            _tmpDurationMin = _cursor.getInt(_cursorIndexOfDurationMin);
            final int _tmpQuality;
            _tmpQuality = _cursor.getInt(_cursorIndexOfQuality);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new SleepLog(_tmpId,_tmpUserId,_tmpDay,_tmpDurationMin,_tmpQuality,_tmpCreatedAt,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object unsyncedSleep(final String uid,
      final Continuation<? super List<SleepLog>> $completion) {
    final String _sql = "SELECT * FROM sleep_logs WHERE userId = ? AND synced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SleepLog>>() {
      @Override
      @NonNull
      public List<SleepLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMin");
          final int _cursorIndexOfQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "quality");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<SleepLog> _result = new ArrayList<SleepLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SleepLog _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final int _tmpDurationMin;
            _tmpDurationMin = _cursor.getInt(_cursorIndexOfDurationMin);
            final int _tmpQuality;
            _tmpQuality = _cursor.getInt(_cursorIndexOfQuality);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new SleepLog(_tmpId,_tmpUserId,_tmpDay,_tmpDurationMin,_tmpQuality,_tmpCreatedAt,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlannedWorkout>> observePlans(final String uid) {
    final String _sql = "SELECT * FROM planned_workouts WHERE userId = ? ORDER BY dayOfWeek ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"planned_workouts"}, new Callable<List<PlannedWorkout>>() {
      @Override
      @NonNull
      public List<PlannedWorkout> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMin");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<PlannedWorkout> _result = new ArrayList<PlannedWorkout>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlannedWorkout _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final int _tmpDurationMin;
            _tmpDurationMin = _cursor.getInt(_cursorIndexOfDurationMin);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new PlannedWorkout(_tmpId,_tmpUserId,_tmpDayOfWeek,_tmpType,_tmpDurationMin,_tmpNote,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object unsyncedPlans(final String uid,
      final Continuation<? super List<PlannedWorkout>> $completion) {
    final String _sql = "SELECT * FROM planned_workouts WHERE userId = ? AND synced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PlannedWorkout>>() {
      @Override
      @NonNull
      public List<PlannedWorkout> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMin");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<PlannedWorkout> _result = new ArrayList<PlannedWorkout>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlannedWorkout _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final int _tmpDurationMin;
            _tmpDurationMin = _cursor.getInt(_cursorIndexOfDurationMin);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new PlannedWorkout(_tmpId,_tmpUserId,_tmpDayOfWeek,_tmpType,_tmpDurationMin,_tmpNote,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WaterLog>> observeWater(final String uid) {
    final String _sql = "SELECT * FROM water_logs WHERE userId = ? ORDER BY day DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"water_logs"}, new Callable<List<WaterLog>>() {
      @Override
      @NonNull
      public List<WaterLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfMl = CursorUtil.getColumnIndexOrThrow(_cursor, "ml");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<WaterLog> _result = new ArrayList<WaterLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WaterLog _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final int _tmpMl;
            _tmpMl = _cursor.getInt(_cursorIndexOfMl);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new WaterLog(_tmpId,_tmpUserId,_tmpDay,_tmpMl,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object waterById(final String id, final Continuation<? super WaterLog> $completion) {
    final String _sql = "SELECT * FROM water_logs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WaterLog>() {
      @Override
      @Nullable
      public WaterLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfMl = CursorUtil.getColumnIndexOrThrow(_cursor, "ml");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final WaterLog _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final int _tmpMl;
            _tmpMl = _cursor.getInt(_cursorIndexOfMl);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _result = new WaterLog(_tmpId,_tmpUserId,_tmpDay,_tmpMl,_tmpSynced);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object unsyncedWater(final String uid,
      final Continuation<? super List<WaterLog>> $completion) {
    final String _sql = "SELECT * FROM water_logs WHERE userId = ? AND synced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WaterLog>>() {
      @Override
      @NonNull
      public List<WaterLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfMl = CursorUtil.getColumnIndexOrThrow(_cursor, "ml");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<WaterLog> _result = new ArrayList<WaterLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WaterLog _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final int _tmpMl;
            _tmpMl = _cursor.getInt(_cursorIndexOfMl);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new WaterLog(_tmpId,_tmpUserId,_tmpDay,_tmpMl,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BodyMetric>> observeBody(final String uid) {
    final String _sql = "SELECT * FROM body_metrics WHERE userId = ? ORDER BY day DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"body_metrics"}, new Callable<List<BodyMetric>>() {
      @Override
      @NonNull
      public List<BodyMetric> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfWeightKg = CursorUtil.getColumnIndexOrThrow(_cursor, "weightKg");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<BodyMetric> _result = new ArrayList<BodyMetric>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BodyMetric _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final float _tmpWeightKg;
            _tmpWeightKg = _cursor.getFloat(_cursorIndexOfWeightKg);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new BodyMetric(_tmpId,_tmpUserId,_tmpDay,_tmpWeightKg,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object unsyncedBody(final String uid,
      final Continuation<? super List<BodyMetric>> $completion) {
    final String _sql = "SELECT * FROM body_metrics WHERE userId = ? AND synced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BodyMetric>>() {
      @Override
      @NonNull
      public List<BodyMetric> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfWeightKg = CursorUtil.getColumnIndexOrThrow(_cursor, "weightKg");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final List<BodyMetric> _result = new ArrayList<BodyMetric>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BodyMetric _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final float _tmpWeightKg;
            _tmpWeightKg = _cursor.getFloat(_cursorIndexOfWeightKg);
            final boolean _tmpSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSynced);
            _tmpSynced = _tmp != 0;
            _item = new BodyMetric(_tmpId,_tmpUserId,_tmpDay,_tmpWeightKg,_tmpSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
