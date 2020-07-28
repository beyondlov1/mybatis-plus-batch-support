package com.beyond.mybatisplus.batch.sqlinject;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;


/**
 * @author: XRQ
 */
public class InsertNotNullColumnsBatch extends AbstractMethod {


    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {

        KeyGenerator keyGenerator = new NoKeyGenerator();
        String sql = getBatchInsertSql(tableInfo);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        String keyProperty = null;
        String keyColumn = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                /* 自增主键 */
                keyGenerator = new Jdbc3KeyGenerator();
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            } else {
                if (null != tableInfo.getKeySequence()) {
                    keyGenerator = TableInfoHelper.genKeyGenerator(CustomSqlMethod.INSERT_NOT_NULL_COLUMNS_BATCH.getMethod(), tableInfo, builderAssistant);
                    keyProperty = tableInfo.getKeyProperty();
                    keyColumn = tableInfo.getKeyColumn();
                }
            }
        }

        return this.addInsertMappedStatement(mapperClass, modelClass, CustomSqlMethod.INSERT_NOT_NULL_COLUMNS_BATCH.getMethod(), sqlSource, keyGenerator, keyProperty, keyColumn);
    }


    private String getBatchInsertSql(TableInfo tableInfo) {
        String batchInsertSql = CustomSqlMethod.INSERT_NOT_NULL_COLUMNS_BATCH.getSql();
        String foreachSql = "<foreach item=\"item\" index=\"index\" collection=\"et.colNames\" open=\"(\" separator=\",\" close=\")\">\n" +
                "            ${item}\n" +
                "         </foreach>\n" +
                "         values\n" +
                "        <foreach item=\"item\" index=\"index\" collection=\"et.tColValues\" open=\"\" separator=\",\" close=\"\">\n" +
                "         <foreach item=\"itemColValue\" index=\"index\" collection=\"item\" open=\"(\" separator=\",\" close=\")\">\n" +
                "             #{itemColValue}\n" +
                "         </foreach>\n" +
                "        </foreach>";
        return String.format(batchInsertSql, tableInfo.getTableName(), foreachSql);
    }


}
