package com.beyond.mybatisplus.batch.proxy;

import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.transform.TransformingClassGenerator;
import net.sf.cglib.transform.impl.AddPropertyTransformer;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author chenshipeng
 * @date 2020/07/13
 */
public class BatchEntityClassGeneratorStrategy extends DefaultGeneratorStrategy {
    private static final Type STRING = Type.getType(String.class);
    private static final Type MAP = Type.getType(Map.class);
    private static final Type SET = Type.getType(Set.class);
    private static final Type CALLBACK = Type.getType(Callback.class);

    private static final Signature GET_ID_COL_NAME = new Signature("getIdColName", STRING, new Type[]{});
    private static final Signature GET_COL_NAME_2_VALUE_MAP = new Signature("getColName2ValueMap", MAP, new Type[]{});
    private static final Signature GET_SET_COL_NAMES = new Signature("getSetColNames", SET, new Type[]{});
    private static final Signature GET_CALLBACK = new Signature("getCallback", CALLBACK, new Type[]{Type.getType(Integer.class)});

    @Override
    protected ClassGenerator transform(ClassGenerator cg) throws Exception {
        Map<String, Type> props = new HashMap<>(3);
        props.put("setColNames", Type.getType(Set.class));
        props.put("colName2ValueMap", Type.getType(Map.class));
        props.put("idColName", Type.getType(String.class));
        AddPropertyTransformer transformer = new AddPropertyTransformer(props);
        return new TransformingClassGenerator(cg, transformer);
    }

}
