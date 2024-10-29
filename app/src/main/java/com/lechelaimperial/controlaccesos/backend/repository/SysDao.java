package com.lechelaimperial.controlaccesos.backend.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lechelaimperial.controlaccesos.backend.entity.SysConfig;

import java.util.List;
@Dao
public interface SysDao {

  @Query("SELECT * FROM SysConfig")
  List<SysConfig> getAll();

  @Query("SELECT * FROM SysConfig WHERE _id = :id")
  SysConfig getById(int id);

  @Query("SELECT * FROM SysConfig WHERE code = :code LIMIT 1")
  SysConfig findByCode(String code);
  @Update
  int update(SysConfig sysConfig);

  @Insert
  long insert(SysConfig sysConfig);

  @Delete
  public void deleteAll(List<SysConfig> list);

}
