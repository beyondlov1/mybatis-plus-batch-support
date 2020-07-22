package com.beyond.mybatisplus.batch.sqlinject;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

/**
 * Author  XRQ
 * Date  2020/6/20
 */
public class SelectByIdForUpdate extends AbstractMethod {


    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        CustomSqlMethod selectByIdForUpdate = CustomSqlMethod.SELECT_BY_ID_FOR_UPDATE;
        SqlSource sqlSource = new RawSqlSource(configuration, String.format(selectByIdForUpdate.getSql(),
                sqlSelectColumns(tableInfo, false),
                tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty(),
                tableInfo.getLogicDeleteSql(true, true)), Object.class);
        return this.addSelectMappedStatementForTable(mapperClass, selectByIdForUpdate.getMethod(), sqlSource, tableInfo);
    }
}
