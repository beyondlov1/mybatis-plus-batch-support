package com.beyond.mybatisplus.batch.proxy;

import com.beyond.mybatisplus.batch.BatchHelper;
import com.beyond.mybatisplus.batch.sqlinject.BatchWrapper;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * @author chenshipeng
 * @date 2020/07/13
 */
public class BatchMethodInterceptor implements MethodInterceptor {

    public static final BatchMethodInterceptor EMPTY = new BatchMethodInterceptor(null);

    private BatchHelper.EntityInfo entityInfo;

    public BatchMethodInterceptor(BatchHelper.EntityInfo entityInfo) {
        this.entityInfo = entityInfo;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        if (method.getName().startsWith("set")) {
            Map<String, Object> colName2ValueMap = ((BatchWrapper) o).getColName2ValueMap();
            Set<String> setColNames = ((BatchWrapper) o).getSetColNames();

            String fieldName = StringUtils.deCapitalize(method.getName().substring(3));
            String colName = entityInfo.getFieldName2ColName().get(fieldName);
            colName2ValueMap.put(colName, objects[0]);
            setColNames.add(colName);
        }
        return methodProxy.invokeSuper(o, objects);
    }




}
