package com.beyond.mybatisplus.batch.sqlinject;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * Author  XRQ
 * Date  2020/6/20
 */
public class SelectBatchByIdsForUpdate extends AbstractMethod {


    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        CustomSqlMethod selectBatchByIdsForUpdate = CustomSqlMethod.SELECT_BATCH_BY_IDS_FOR_UPDATE;
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, String.format(selectBatchByIdsForUpdate.getSql(),
                sqlSelectColumns(tableInfo, false), tableInfo.getTableName(), tableInfo.getKeyColumn(),
                SqlScriptUtils.convertForeach("#{item}", COLLECTION, null, "item", COMMA),
                tableInfo.getLogicDeleteSql(true, true)), Object.class);
        return addSelectMappedStatementForTable(mapperClass, selectBatchByIdsForUpdate.getMethod(), sqlSource, tableInfo);
    }
}
