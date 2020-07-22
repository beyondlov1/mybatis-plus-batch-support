package com.beyond.mybatisplus.batch;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.beyond.mybatisplus.batch.proxy.BatchEntityClassGeneratorStrategy;
import com.beyond.mybatisplus.batch.proxy.BatchMethodInterceptor;
import com.beyond.mybatisplus.batch.proxy.PackageUtils;
import com.beyond.mybatisplus.batch.proxy.StringUtils;
import com.beyond.mybatisplus.batch.sqlinject.BatchWrapper;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author beyond
 * @date 2020/07/13
 */
public class BatchHelper {

    private static final BatchEntityClassGeneratorStrategy BATCH_ENTITY_CLASS_GENERATOR_STRATEGY = new BatchEntityClassGeneratorStrategy();

    private static Map<Class, EntityInfo> entityInfoCache = new ConcurrentHashMap<>();
    private static Map<Class, Enhancer> enhancerCache = new ConcurrentHashMap<>();
    private static Map<Class, Factory> factoryCache = new ConcurrentHashMap<>();

    public static void warmCache(String packageName){
        List<Class<?>> classes = PackageUtils.getClasses(packageName);
        for (Class<?> aClass : classes) {
            if (aClass.isInterface()){
                continue;
            }
            getFactory(aClass);
        }
    }

    /**
     * 相较于普通代理方式, 会慢3-4倍, 10000条数据执行需要20-30ms
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBatchEntity(Class<T> clz) {
        Factory factory = getFactory(clz);

        EntityInfo entityInfo = getEntityInfo(clz);
        Object o = factory.newInstance(new Callback[]{new BatchMethodInterceptor(entityInfo), NoOp.INSTANCE});

        ((BatchWrapper) o).setIdColName(entityInfo.getIdColName());
        ((BatchWrapper) o).setSetColNames(new HashSet<>());
        ((BatchWrapper) o).setColName2ValueMap(new HashMap<>(5));

        return (T)o;
    }

    private static <T> Factory getFactory(Class<T> clz) {
        Factory factory = factoryCache.get(clz);
        if (factory == null) {
            Enhancer enhancer = getEnhancer(clz);
            factory = (Factory) enhancer.create();
            factoryCache.put(clz, factory);
        }
        return factory;
    }

    private static Enhancer getEnhancer(Class clz) {
        Enhancer enhancer = enhancerCache.get(clz);
        if (enhancer != null) {
            return enhancer;
        }
        enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallbacks(new Callback[]{BatchMethodInterceptor.EMPTY, NoOp.INSTANCE});
        enhancer.setCallbackTypes(new Class[]{BatchMethodInterceptor.class, NoOp.class});
        enhancer.setStrategy(BATCH_ENTITY_CLASS_GENERATOR_STRATEGY);
        enhancer.setCallbackFilter(new CallbackFilter() {
            @Override
            public int accept(Method method) {
                switch (method.getName()) {
                    case "getIdColName":
                    case "getSetColNames":
                    case "getColName2ValueMap":
                    case "setIdColName":
                    case "setSetColNames":
                    case "setColName2ValueMap":
                        return 1;
                    default:
                        return 0;
                }
            }
        });
        enhancer.setInterfaces(new Class[]{BatchWrapper.class});
        enhancerCache.put(clz, enhancer);
        return enhancer;
    }

    public static EntityInfo getEntityInfo(Class clz) {
        EntityInfo entityInfoInCache = entityInfoCache.get(clz);
        if (entityInfoInCache != null) {
            return entityInfoInCache;
        }
        EntityInfo entityInfo = new EntityInfo();
        Field[] declaredFields = clz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            TableId tableId = declaredField.getAnnotation(TableId.class);
            if (tableId != null) {
                entityInfo.setIdColName(tableId.value());
                break;
            }
        }

        if (entityInfo.getIdColName() == null) {
            for (Field declaredField : declaredFields) {
                if (Modifier.isStatic(declaredField.getModifiers()) || Modifier.isFinal(declaredField.getModifiers())) {
                    continue;
                }
                if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(declaredField.getName(), "id")) {
                    entityInfo.setIdColName(StringUtils.humpToLine(declaredField.getName()));
                    break;
                }
            }
        }

        for (Field declaredField : declaredFields) {
            TableField tableField = declaredField.getAnnotation(TableField.class);
            if (tableField != null) {
                entityInfo.getFieldName2ColName().put(declaredField.getName(), tableField.value());
            } else {
                entityInfo.getFieldName2ColName().put(declaredField.getName(), StringUtils.humpToLine(declaredField.getName()));
            }
            entityInfo.getFieldName2Field().put(declaredField.getName(), declaredField);
        }

        entityInfoCache.put(clz, entityInfo);
        return entityInfo;
    }

    public static class EntityInfo {
        private String idColName;
        private Map<String, Field> fieldName2Field = new HashMap<>();
        private Map<String, String> fieldName2ColName = new HashMap<>();

        public String getIdColName() {
            return idColName;
        }

        public void setIdColName(String idColName) {
            this.idColName = idColName;
        }

        public Map<String, Field> getFieldName2Field() {
            return fieldName2Field;
        }

        public void setFieldName2Field(Map<String, Field> fieldName2Field) {
            this.fieldName2Field = fieldName2Field;
        }

        public Map<String, String> getFieldName2ColName() {
            return fieldName2ColName;
        }

        public void setFieldName2ColName(Map<String, String> fieldName2ColName) {
            this.fieldName2ColName = fieldName2ColName;
        }
    }
}
