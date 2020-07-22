package com.beyond.mybatisplus.batch.sqlinject;


import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;


/**
 * @author: beyond
 * @date: 2020/06/20
 */
public class UpdateSetColumnsBatchById extends AbstractMethod {


    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        KeyGenerator keyGenerator = new NoKeyGenerator();
        String sql = getBatchUpdateSql(tableInfo);

        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        String keyProperty = null;
        String keyColumn = null;
        return this.addInsertMappedStatement(mapperClass, modelClass, CustomSqlMethod.UPDATE_SET_COLUMNS_BATCH.getMethod(), sqlSource, keyGenerator, keyProperty, keyColumn);
    }

    private String getBatchUpdateSql(TableInfo tableInfo) {
        String batchInsertSql = CustomSqlMethod.UPDATE_SET_COLUMNS_BATCH.getSql();
        String foreachSql = "<set>\n" +
                "            <foreach collection=\"et.colMapList\" open=\"\" close=\"\" separator=\",\" item=\"item\">\n" +
                "                ${item.colName} = CASE ${et.idColName}\n" +
                "                <foreach collection=\"item.idValueMapList\" item=\"idValueMap\">\n" +
                "                    WHEN #{idValueMap.id}  THEN #{idValueMap.value}\n" +
                "                </foreach>\n" +
                "                ELSE ''\n" +
                "                END\n" +
                "            </foreach>\n" +
                "        </set>\n" +
                "        where ${et.idColName} in\n" +
                "        <foreach collection=\"et.idValueSet\" item=\"idValue\" open=\"(\" close=\")\" separator=\",\">\n" +
                "            #{idValue}\n" +
                "        </foreach>";
        return String.format(batchInsertSql, tableInfo.getTableName(), foreachSql);
    }

}
