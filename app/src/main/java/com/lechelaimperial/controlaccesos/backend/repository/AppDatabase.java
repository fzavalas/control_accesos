package com.lechelaimperial.controlaccesos.backend.repository;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.lechelaimperial.controlaccesos.backend.entity.SysConfig;

@Database(
        entities = {SysConfig.class},
        version = 1)

public abstract class AppDatabase extends RoomDatabase {
  public abstract SysDao sysDao();

}