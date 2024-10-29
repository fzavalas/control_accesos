package com.lechelaimperial.controlaccesos.backend.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class SysConfig implements Serializable {

  @PrimaryKey(autoGenerate = true)
  public int _id;
  @ColumnInfo(name = "name")
  public String name;
  @ColumnInfo(name = "code")
  public String code;
  @ColumnInfo(name = "value")
  public String value;

  public int get_id() {
    return _id;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}