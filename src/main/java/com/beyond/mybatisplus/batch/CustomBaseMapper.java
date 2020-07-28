package com.beyond.mybatisplus.batch;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.beyond.mybatisplus.batch.sqlinject.BatchEntity;
import com.beyond.mybatisplus.batch.sqlinject.BatchEntity2;
import com.beyond.mybatisplus.batch.sqlinject.BatchWrapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 自定义, 扩展Mapper基类
 * Author  XRQ
 * Date  2020/6/17
 */
public interface CustomBaseMapper<T> extends BaseMapper<T> {


    /**
     * 根据 ID 查询
     * 不可改名
     * @param id 主键ID
     */
    T selectByIdForUpdate(Serializable id);


    /**
     * 查询（根据ID 批量查询）
     * 不可改名
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    List<T> selectBatchByIdsForUpdate(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

    /**
     * 批量插入, 只会插入设置的字段(调用过set方法的字段)
     * @param listForUpdate 插入列表
     */
    default int insertSetColumnsBatch(@NotEmpty List<? extends T> listForUpdate){
        return insertSetColumnsBatch(BatchEntity.wrap(listForUpdate));
    }

    /**
     * 批量插入, 只会插入设置的字段(调用过set方法的字段)
     * @param batchEntity 插入列表
     */
    int insertSetColumnsBatch(@Param(Constants.ENTITY) BatchEntity batchEntity);

    /**
     * 批量插入, 只会插入设置的字段(调用过set方法的字段)
     * @param listForUpdate 更新列表
     */
    default int updateSetColumnsBatchById(@NotEmpty List<? extends T> listForUpdate){
        return updateSetColumnsBatchById(BatchEntity.wrap(listForUpdate));
    }

    /**
     * 批量更新, 只会更新设置的字段(调用过set方法的字段)
     * @param batchEntity 更新列表
     */
    int updateSetColumnsBatchById(@Param(Constants.ENTITY) BatchEntity batchEntity);


    /**
     * 批量插入, 只会插入不为null的字段 (速度比 insertSetColumnsBatch 快5-10倍)
     * @param listForUpdate 插入列表
     */
    default int insertNotNullColumnsBatch(@NotEmpty List<T> listForUpdate){
        return insertNotNullColumnsBatch(new BatchEntity2<>(listForUpdate));
    }

    /**
     * 批量插入, 只会插入不为null的字段 (速度比 insertSetColumnsBatch 快5-10倍)
     * @param batchEntity 插入列表
     */
    int insertNotNullColumnsBatch(@Param(Constants.ENTITY) BatchEntity2<T> batchEntity);

    /**
     * 批量更新, 只会更新不为null的字段 (速度比 updateSetColumnsBatch 快5-10倍)
     * @param listForUpdate 更新列表
     */
    default int updateNotNullColumnsBatchById(@NotEmpty List<T> listForUpdate){
        return updateNotNullColumnsBatchById(new BatchEntity2<>(listForUpdate));
    }

    /**
     * 批量更新, 只会更新不为null的字段 (速度比 updateSetColumnsBatch 快5-10倍)
     * @param batchEntity 更新列表
     */
    int updateNotNullColumnsBatchById(@Param(Constants.ENTITY) BatchEntity2<T> batchEntity);

    /**
     * 批量插入 (自动)(推荐)
     * @param list 插入列表
     */
    default int insertBatch(@NotEmpty List<T> list){
        if (CollectionUtils.isEmpty(list)){
            throw new RuntimeException("list cannot be empty");
        }
        T t = list.get(0);
        if (t instanceof BatchWrapper){
            return insertSetColumnsBatch(list);
        }else {
            return insertNotNullColumnsBatch(list);
        }
    }


    /**
     * 批量更新 (自动)(推荐)
     * @param list 更新列表
     */
    default int updateBatchById(@NotEmpty List<T> list){
        if (CollectionUtils.isEmpty(list)){
            throw new RuntimeException("list cannot be empty");
        }
        T t = list.get(0);
        if (t instanceof BatchWrapper){
            return updateSetColumnsBatchById(list);
        }else {
            return updateNotNullColumnsBatchById(list);
        }
    }
}
