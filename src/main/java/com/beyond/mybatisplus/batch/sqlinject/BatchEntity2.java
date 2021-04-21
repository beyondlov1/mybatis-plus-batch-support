package com.beyond.mybatisplus.batch.sqlinject;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.beyond.mybatisplus.batch.exception.MyBatisPlusBatchSqlException;
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
    //不可随意更改字段名称
    /**
     * insert into xxx (colNames) VALUES tColValues
     * colNames是有序的,与tColValues值的位置对应
     */
    private Set<String> colNames = null;
    /**
     * tColValues=(a,b,c...),(a,b,c...),(a,b,c...),....
     */
    private List<List<Object>> tColValues = null;

    // ******************* for update ******************* //

    /**
     * 拼装后类似这样
     * update xxx set
     * --循环(foreach item in colMapList)
     * item[0].colName = case idColName
     * WHEN item[0].idValueMapList[0].id Then item[0].idValueMapList[0].value
     * WHEN item[0].idValueMapList[1].id Then item[0].idValueMapList[1].value
     * WHEN item[0].idValueMapList[2].id Then item[0].idValueMapList[2].value
     * item[1].colName = case idColName
     * WHEN item[1].idValueMapList[0].id Then item[1].idValueMapList[0].value
     * WHEN item[1].idValueMapList[1].id Then item[1].idValueMapList[1].value
     * WHEN item[1].idValueMapList[2].id Then item[1].idValueMapList[2].value
     * ELSE ''
     * END
     * where idColName in ( idValueSet )
     */
    private String idColName = "id";
    private Set<Object> idValueSet = null;
    /**
     * Map<String, Object> map = new HashMap();
     * map.put("colName", colName);
     * map.put("idValueMapList", id2ValueMapList);
     * id2ValueMap存储了某个记录id和对应colName的值
     */
    private List<Map<String, Object>> colMapList = null;

    /**
     * 包装构造原来的list
     *
     * @param list      数据对象
     * @param forInsert true-insert/ false-update
     */
    public BatchEntity2(List<T> list, boolean forInsert) {
        super(list);
        if (CollectionUtils.isEmpty(list)) {
            throw new MyBatisPlusBatchSqlException("批量插入的entityList不可为空");
        }
        try {
            //这个 colNames都有用到
            if (forInsert) {
                //for insert
                colNames = new LinkedHashSet<>();
                tColValues = new ArrayList<>();
            } else {
                //for update
                idValueSet = new LinkedHashSet<>();
                colMapList = new ArrayList<>();
            }
            init(list, forInsert);
        } catch (Exception e) {
            throw new MyBatisPlusBatchSqlException("执行批量insert 初始化发生异常", e);
        }
    }

    /**
     * 初始化,包装list对象转化成mapper里的字段
     *
     * @param list      转化成 mybatis mapper里需要的格式
     *                  例子：
     * @param forInsert true-insert/ false-update
     * @see InsertNotNullColumnsBatch#getBatchInsertSql
     * @see UpdateNotNullColumnsBatchById#getBatchUpdateSql
     */
    private void init(List<T> list, boolean forInsert) {

        // For Insert
        Class clazz = list.get(0).getClass();
        //缓存中获取
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        if (tableInfo == null) {
            throw new MyBatisPlusBatchSqlException("批量插入只有实现了CustomBaseMapper接口的entity Mapper才可以使用");
        }
        Map<String, Field> fieldMap = ReflectionKit.getFieldMap(clazz);
        //先拿所有字段
        //propertyColMap key java property, value 数据库 col
        Map<String, String> propertyColMap = tableInfo.getFieldList().stream().collect(Collectors.toMap(TableFieldInfo::getProperty, TableFieldInfo::getColumn));
        if (tableInfo.getKeyProperty() == null || tableInfo.getKeyColumn() == null) {
            //判断是否设置了主键
            throw new MyBatisPlusBatchSqlException(tableInfo.getEntityType() + "没有设置" + tableInfo.getTableName() + "对应的 primary key");
        }
        propertyColMap.put(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
        //excludeSet 初始化为所有java property,后续会排除掉 value一直为空的字段
        Set<String> excludeSet = new HashSet<>(propertyColMap.keySet());
        Map<String, List<Object>> valueListMap = new HashMap<>();
        //valueMap<property,valueList> valueList为该property在每个对象中的值列表
        for (T t : list) {
            for (String property : propertyColMap.keySet()) {
                Object o = getFieldValue(fieldMap, t, property);
                if (o != null) {
                    excludeSet.remove(property);
                    List<Object> objects = valueListMap.computeIfAbsent(property, k -> new ArrayList<>());
                    objects.add(o);
                }
                //空的不放进去
            }
        }

        if (valueListMap.isEmpty()) {
            throw new MyBatisPlusBatchSqlException("invalid list, list中每个字段都为空");
        } else {
            //判断valueMap中每个property对应的list长度是否相等
            // 避免 insert into xxx (A,B,C) VALUES (a,b,c),(a,b) 的情况
            for (String property : valueListMap.keySet()) {
                List<Object> valueList = valueListMap.get(property);
                if (CollectionUtils.isEmpty(valueList)) {
                    throw new MyBatisPlusBatchSqlException("invalid list, list中每个对象字段值必须统一, list中的某字段必须都为null或者都不为null");
                }
                if (list.size() != valueList.size()) {
                    throw new MyBatisPlusBatchSqlException("invalid list, list中" + property + "字段有的为null,无法执行批量sql");
                }
            }
        }
        //移除一直为空的字段
        for (String exclude : excludeSet) {
            propertyColMap.remove(exclude);
        }

        // For insert
        //valueMap 转tColValues 正常的话 每个value中的list长度是一样的
        if (forInsert) {
            for (int i = 0; i < list.size(); i++) {
                List<Object> tColValue = new ArrayList<>();
                for (String property : propertyColMap.keySet()) {
                    //数据库的字段名
                    if (i == 0) {
                        colNames.add(propertyColMap.get(property));
                    }
                    tColValue.add(valueListMap.get(property).get(i));
                }
                tColValues.add(tColValue);
            }
        } else {
            // For Update
            idColName = tableInfo.getKeyColumn();
            for (T t : list) {
                //获取list所有id
                //组合后sql 语句里  where idColName in ( idValueSet )
                //idValueSet是所有主键的值
                Object fieldValue = getFieldValue(fieldMap, t, tableInfo.getKeyProperty());
                if (fieldValue == null) {
                    throw new MyBatisPlusBatchSqlException("batch update id 不可为空");
                }
                idValueSet.add(fieldValue);
            }
            if (idValueSet.isEmpty()) {
                throw new MyBatisPlusBatchSqlException("batch update, id 不可为空");
            }

            Map<Object, Map<String, Object>> idColName$valueMap = new HashMap<>();
            for (T t : list) {
                Map<String, Object> colName2ValueMap = new HashMap<>();
                for (String property : propertyColMap.keySet()) {
                    colName2ValueMap.put(propertyColMap.get(property), getFieldValue(fieldMap, t, property));
                }
                idColName$valueMap.put(getFieldValue(fieldMap, t, tableInfo.getKeyProperty()), colName2ValueMap);
            }

            for (String colName : propertyColMap.values()) {
                if (StringUtils.equals(colName, idColName)) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("colName", colName);
                List<Map<String, Object>> id2ValueMapList = new ArrayList<>();
                for (Object idValue : idValueSet) {
                    Map<String, Object> id2ValueMap = new HashMap<>();
                    id2ValueMap.put("id", idValue);
                    id2ValueMap.put("value", idColName$valueMap.get(idValue).get(colName));
                    id2ValueMapList.add(id2ValueMap);
                }
                map.put("idValueMapList", id2ValueMapList);
                colMapList.add(map);
            }
        }
    }


    /**
     * 反射从field 取值
     *
     * @param fieldMap  key为字段名
     * @param t         对象
     * @param fieldName 字段名
     */
    private Object getFieldValue(Map<String, Field> fieldMap, T t, String fieldName) {
        try {
            Field field = fieldMap.get(fieldName);
            field.setAccessible(true);
            return field.get(t);
        } catch (ReflectiveOperationException e) {
            throw new MyBatisPlusBatchSqlException("Error: Cannot read field in " + t.getClass().getSimpleName() + ".  Cause:", e);
        }
    }

}
