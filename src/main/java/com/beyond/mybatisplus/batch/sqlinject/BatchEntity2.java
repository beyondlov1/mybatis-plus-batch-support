package com.beyond.mybatisplus.batch.sqlinject;


import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.beyond.mybatisplus.batch.BatchHelper;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author  XRQ
 * Date  2020/6/21
 */
public class BatchEntity2<T> extends ArrayList<T> {

    // ******************* for insert ******************* //

    private Set<String> colNames = new LinkedHashSet<>();
    private List<List<Object>> tColValues = new ArrayList<>();

    // ******************* for update ******************* //

    private String idColName = "id";
    private Set<Object> idValueSet = new LinkedHashSet<>();
    private List<Map<String, Object>> colMapList = new ArrayList<>();

    public BatchEntity2(List<T> list) {
        super(list);
        if (CollectionUtils.isEmpty(list)) {
            throw new RuntimeException("批量插入的entityList不可为空");
        }
        try {
            init(list);
        } catch (Exception e) {
            throw new RuntimeException("执行批量insert 初始化发生异常");
        }
    }


    private void init(List<T> list) {

        // For Insert
        Class clazz = list.get(0).getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        if (tableInfo == null) {
            throw new RuntimeException("批量插入只有实现了CustomBaseMapper接口的entity Mapper才可以使用");
        }
        Map<String, Field> fieldMap = ReflectionKit.getFieldMap(clazz);
        //先拿所有字段
        //key java property, value 数据库 col
        Map<String, String> propertyColMap = tableInfo.getFieldList().stream().collect(Collectors.toMap(TableFieldInfo::getProperty, TableFieldInfo::getColumn));
        propertyColMap.put(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
        Set<String> excludeSet = new HashSet<>(propertyColMap.keySet());
        Map<String, List<Object>> valueMap = new HashMap<>();

        for (T t : list) {
            for (String property : propertyColMap.keySet()) {
                Object o = getFieldValue(fieldMap, t, property);
                if (o != null) {
                    excludeSet.remove(property);
                }
                List<Object> objects = valueMap.computeIfAbsent(property, k -> new ArrayList<>());
                objects.add(o);
            }
        }
        //移除一直为空的字段
        for (String exclude : excludeSet) {
            propertyColMap.remove(exclude);
        }
        //valueMap 转tColValues 正常的话 每个value中的list长度是一样的
        for (int i = 0; i < list.size(); i++) {
            List<Object> tColValue = new ArrayList<>();
            for (String colName : propertyColMap.keySet()) {
                //数据库的字段名
                if (i == 0) {
                    colNames.add(propertyColMap.get(colName));
                }
                tColValue.add(valueMap.get(colName).get(i));
            }
            tColValues.add(tColValue);
        }


        // For Update
        BatchHelper.EntityInfo entityInfo = BatchHelper.getEntityInfo(list.get(0).getClass());
        idColName = entityInfo.getIdColName();
        for (T t : list) {
            idValueSet.add(getFieldValue(entityInfo, t, entityInfo.getIdPropertyName()));
        }

        Map<Object, Map<String, Object>> id_colName$Value_map = new HashMap<>();
        for (T t : list) {
            Map<String, Object> colName2ValueMap = new HashMap<>();
            for (String colName : colNames) {
                colName2ValueMap.put(colName, getFieldValueByColName(entityInfo, t,colName));
            }
            id_colName$Value_map.put(getFieldValue(entityInfo, t, entityInfo.getIdPropertyName()), colName2ValueMap);
        }

        for (String colName : colNames) {
            if (StringUtils.equals(colName, idColName)) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("colName", colName);
            List<Map<String, Object>> id2ValueMapList = new ArrayList<>();
            for (Object idValue : idValueSet) {
                Map<String, Object> id2ValueMap = new HashMap<>();
                id2ValueMap.put("id", idValue);
                id2ValueMap.put("value", id_colName$Value_map.get(idValue).get(colName));
                id2ValueMapList.add(id2ValueMap);
            }
            map.put("idValueMapList", id2ValueMapList);
            colMapList.add(map);
        }
    }

    private String getPropertyNameByColName(String colName, BatchHelper.EntityInfo entityInfo){
        return entityInfo.getColName2FieldName().get(colName);
    }


    private Object getFieldValueByColName(BatchHelper.EntityInfo entityInfo, T t, String colName) {
        return getFieldValue(entityInfo, t, entityInfo.getColName2FieldName().get(colName));
    }

    private Object getFieldValue(BatchHelper.EntityInfo entityInfo, T t, String fieldName) {
        Field field = entityInfo.getFieldName2Field().get(fieldName);
        field.setAccessible(true);
        try {
            return field.get(t);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getFieldValue(Map<String, Field> fieldMap, T t, String fieldName) {
        try {
            Field field = fieldMap.get(fieldName);
            field.setAccessible(true);
            return field.get(t);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error: Cannot read field in " + t.getClass().getSimpleName() + ".  Cause:", e);
        }
    }

}
