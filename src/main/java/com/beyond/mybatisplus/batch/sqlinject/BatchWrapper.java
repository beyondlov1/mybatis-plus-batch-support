package com.beyond.mybatisplus.batch.sqlinject;

import java.util.Map;
import java.util.Set;

/**
 * 用于批量插入
 * @author chenshipeng
 */
public interface BatchWrapper {
    Map<String, Object> getColName2ValueMap();
    Set<String> getSetColNames();
    String getIdColName();

    void setColName2ValueMap(Map<String, Object> colName2ValueMap);
    void setSetColNames(Set<String> setColNames);
    void setIdColName(String idColName);
}
