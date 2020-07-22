package com.beyond.mybatisplus.batch.sqlinject;

/**
 * Author  XRQ
 * Date  2020/6/17
 * 自定义mybatis plus 方法
 */
public enum CustomSqlMethod {

    /**
     * insert 批量
     */
    INSERT_NOT_NULL_COLUMNS_BATCH("insertNotNullColumnsBatch", "插入多条数据", "<script> INSERT INTO %s  %s </script>"),

    UPDATE_NOT_NULL_COLUMNS_BATCH("updateNotNullColumnsBatchById", "通过id更新多条数据", "<script> update %s  %s </script>"),

    INSERT_SET_COLUMNS_BATCH("insertSetColumnsBatch", "插入多条数据", "<script> INSERT INTO %s  %s </script>"),

    UPDATE_SET_COLUMNS_BATCH("updateSetColumnsBatchById", "通过id更新多条数据", "<script> update %s  %s </script>"),

    SELECT_BY_ID_FOR_UPDATE("selectByIdForUpdate","查询 for update","SELECT %s FROM %s WHERE %s=#{%s} %s FOR UPDATE"),

    SELECT_BATCH_BY_IDS_FOR_UPDATE("selectBatchByIdsForUpdate","查询","<script>SELECT %s FROM %s WHERE %s IN (%s) %s FOR UPDATE</script>")
    ;




    private final String method;
    private final String desc;
    private final String sql;

    CustomSqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }

    public String getMethod() {
        return method;
    }

    public String getDesc() {
        return desc;
    }

    public String getSql() {
        return sql;
    }
}
