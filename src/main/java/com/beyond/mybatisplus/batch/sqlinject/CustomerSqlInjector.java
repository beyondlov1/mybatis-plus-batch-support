package com.beyond.mybatisplus.batch.sqlinject;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;

import java.util.List;

/**
 * Author  XRQ
 */
public class CustomerSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new AlwaysUpdateSomeColumnById());

        methodList.add(new InsertSetColumnsBatch());
        methodList.add(new UpdateSetColumnsBatchById());
        methodList.add(new InsertNotNullColumnsBatch());
        methodList.add(new UpdateNotNullColumnsBatchById());
        methodList.add(new SelectByIdForUpdate());
        methodList.add(new SelectBatchByIdsForUpdate());

        return methodList;
    }
}
