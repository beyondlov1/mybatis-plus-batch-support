package com.beyond.mybatisplus.batch.sqlinject;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 用于批量更新传入参数
 */
public class BatchEntity extends ArrayList<Object>{

    // ******************* for insert ******************* //

    private Set<String> colNames = new LinkedHashSet<>();
    private List<List<Object>> tColValues = new ArrayList<>();

    // ******************* for update ******************* //

    private String idColName = "id";
    private Set<Object> idValueSet = new LinkedHashSet<>();
    private List<Map<String, Object>> colMapList = new ArrayList<>();

    public static BatchEntity wrap(List<?> list){
        for (Object o : list) {
            if (!(o instanceof BatchWrapper)){
                throw new RuntimeException("传入参数必须为InsertWrapper对象列表");
            }
        }
        return new BatchEntity(list);
    }

    private BatchEntity(List<?> list) {
        super(list);
        for (Object o : list) {
            BatchWrapper batchWrapper = (BatchWrapper) o;
            colNames.addAll(batchWrapper.getSetColNames());
        }
        for (Object o : list) {
            BatchWrapper t = (BatchWrapper) o;
            List<Object> colValues = new ArrayList<>();
            for (String colName : colNames) {
                colValues.add(t.getColName2ValueMap().get(colName));
            }
            tColValues.add(colValues);
        }


        BatchWrapper batchWrapper = (BatchWrapper) list.get(0);
        idColName = batchWrapper.getIdColName();
        for (Object wrapper : list) {
            idValueSet.add(((BatchWrapper)wrapper).getColName2ValueMap().get(idColName));
        }

        Map<Object, Map<String, Object>> id_colName$Value_map = new HashMap<>();
        for (Object wrapper : list) {
            Map<String, Object> colName2ValueMap = new HashMap<>();
            for (String colName : colNames) {
                colName2ValueMap.put(colName,((BatchWrapper)wrapper).getColName2ValueMap().get(colName));
            }
            id_colName$Value_map.put(((BatchWrapper)wrapper).getColName2ValueMap().get(idColName), colName2ValueMap);
        }

        for (String colName : colNames) {
            if (StringUtils.equals(colName, idColName)){
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
}
