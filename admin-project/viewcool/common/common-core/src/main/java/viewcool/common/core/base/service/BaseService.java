package viewcool.common.core.base.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import viewcool.common.core.annotation.*;
import viewcool.common.core.base.dao.BaseDaoMapper;
import viewcool.common.core.config.CoreProperties;
import viewcool.common.core.constant.AggregationType;
import viewcool.common.core.constant.GlobalDeletedFlag;
import viewcool.common.core.constant.MaskFieldTypeEnum;
import viewcool.common.core.exception.InvalidDataFieldException;
import viewcool.common.core.exception.MyRuntimeException;
import viewcool.common.core.object.*;
import viewcool.common.core.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/**
 * 所有Service的基类。
 *
 * @param <M> Model对象的类型。
 * @param <K> Model对象主键的类型。
 * @author eris
 * @date 2024-11-16
 */
@Slf4j
public abstract class BaseService<M, K extends Serializable> extends ServiceImpl<BaseDaoMapper<M>, M> implements IBaseService<M, K> {

    /**
     * 当前Service关联的主Model实体对象的Class。
     */
    protected final Class<M> modelClass;
    /**
     * 当前Service关联的主Model实体对象主键字段的Class。
     */
    protected final Class<K> idFieldClass;
    /**
     * 当前Service关联的主Model实体对象的实际表名称。
     */
    protected final String tableName;
    /**
     * 当前Service关联的主Model对象主键字段名称。
     */
    protected String idFieldName;
    /**
     * 当前Service关联的主数据表中主键列名称。
     */
    protected String idColumnName;
    /**
     * 当前Service关联的主Model对象逻辑删除字段名称。
     */
    protected String deletedFlagFieldName;
    /**
     * 当前Service关联的主数据表中逻辑删除字段名称。
     */
    protected String deletedFlagColumnName;
    /**
     * 当前Service关联的主Model对象租户Id字段。
     */
    protected Field tenantIdField;
    /**
     * 流程实例状态字段。
     */
    protected Field flowStatusField;
    /**
     * 流程最后审批状态字段
     */
    protected Field flowLatestApprovalStatusField;
    /**
     * 脱敏字段列表。
     */
    protected List<Field> maskFieldList = new LinkedList<>();
    /**
     * 自动编码字段列表。
     */
    protected List<Field> autoCodeFieldList = new LinkedList<>();
    /**
     * 唯一值验证字段列表。
     */
    protected List<Field> uniqueVerifyFieldList = new LinkedList<>();
    /**
     * 当前Service关联的主Model对象租户Id字段名称。
     */
    protected String tenantIdFieldName;
    /**
     * 当前Service关联的主数据表中租户Id列名称。
     */
    protected String tenantIdColumnName;
    /**
     * 当前Job服务源主表Model对象最后更新时间字段名称。
     */
    protected String jobUpdateTimeFieldName;
    /**
     * 当前Job服务源主表Model对象最后更新时间列名称。
     */
    protected String jobUpdateTimeColumnName;
    /**
     * 当前业务服务源主表Model对象最后更新时间字段名称。
     */
    protected String updateTimeFieldName;
    /**
     * 当前业务服务源主表Model对象最后更新时间列名称。
     */
    protected String updateTimeColumnName;
    /**
     * 当前业务服务源主表Model对象最后更新用户Id字段名称。
     */
    protected String updateUserIdFieldName;
    /**
     * 当前业务服务源主表Model对象最后更新用户Id列名称。
     */
    protected String updateUserIdColumnName;
    /**
     * 当前Service关联的主Model对象主键字段赋值方法的反射对象。
     */
    protected Method setIdFieldMethod;
    /**
     * 当前Service关联的主Model对象主键字段访问方法的反射对象。
     */
    protected Method getIdFieldMethod;
    /**
     * 当前Service关联的主Model对象逻辑删除字段赋值方法的反射对象。
     */
    protected Method setDeletedFlagMethod;
    /**
     * 当前Service关联的全局字典对象的结构列表，该字段在系统启动阶段一次性预加载，提升运行时效率。
     */
    protected final List<RelationStruct> relationGlobalDictStructList = new LinkedList<>();
    /**
     * 当前Service关联的主Model对象的所有常量字典关联的结构列表，该字段在系统启动阶段一次性预加载，提升运行时效率。
     */
    protected final List<RelationStruct> relationConstDictStructList = new LinkedList<>();
    /**
     * 当前Service关联的主Model对象的所有字典关联的结构列表，该字段在系统启动阶段一次性预加载，提升运行时效率。
     */
    protected final List<RelationStruct> localRelationDictStructList = new LinkedList<>();
    /**
     * 当前Service关联的主Model对象的所有一对一关联的结构列表，该字段在系统启动阶段一次性预加载，提升运行时效率。
     */
    protected final List<RelationStruct> localRelationOneToOneStructList = new LinkedList<>();
    /**
     * 当前Service关联的主Model对象的所有一对多关联的结构列表，该字段在系统启动阶段一次性预加载，提升运行时效率。
     */
    protected final List<RelationStruct> localRelationOneToManyStructList = new LinkedList<>();
    /**
     * 当前Service关联的主Model对象的所有多对多关联的结构列表，该字段在系统启动阶段一次性预加载，提升运行时效率。
     */
    protected final List<RelationStruct> localRelationManyToManyStructList = new LinkedList<>();
    /**
     * 当前Service关联的主Model对象的所有一对多聚合关联的结构列表，该字段在系统启动阶段一次性预加载，提升运行时效率。
     */
    protected final List<RelationStruct> localRelationOneToManyAggrStructList = new LinkedList<>();
    /**
     * 当前Service关联的主Model对象的所有多对多聚合关联的结构列表，该字段在系统启动阶段一次性预加载，提升运行时效率。
     */
    protected final List<RelationStruct> localRelationManyToManyAggrStructList = new LinkedList<>();
    /**
     * 基础表的实体对象及表信息。
     */
    protected final TableModelInfo tableModelInfo = new TableModelInfo();
    private final Map<Class<? extends MaskFieldHandler>, MaskFieldHandler> maskFieldHandlerMap = new ConcurrentHashMap<>();

    @Autowired
    private CoreProperties coreProperties;

    private static final String GROUPED_KEY = "GROUPED_KEY";
    private static final String AGGREGATED_VALUE = "AGGREGATED_VALUE";
    private static final String AND_OP = " AND ";
    private static final String ORDER_BY = " ORDER BY ";

    @Override
    public BaseDaoMapper<M> getBaseMapper() {
        return mapper();
    }

    /**
     * 构造函数，在实例化的时候，一次性完成所有有关主Model对象信息的加载。
     */
    @SuppressWarnings("unchecked")
    protected BaseService() {
        Class<?> type = getClass();
        while (!(type.getGenericSuperclass() instanceof ParameterizedType)) {
            type = type.getSuperclass();
        }
        modelClass = (Class<M>) ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0];
        idFieldClass = (Class<K>) ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[1];
        this.tableName = modelClass.getAnnotation(TableName.class).value();
        Field[] fields = ReflectUtil.getFields(modelClass);
        for (Field field : fields) {
            initializeField(field);
        }
        tableModelInfo.setModelName(modelClass.getSimpleName());
        tableModelInfo.setTableName(this.tableName);
        tableModelInfo.setKeyFieldName(idFieldName);
        tableModelInfo.setKeyColumnName(idColumnName);
    }

    @Override
    public TableModelInfo getTableModelInfo() {
        return this.tableModelInfo;
    }

    private void initializeField(Field field) {
        if (idFieldName == null && null != field.getAnnotation(TableId.class)) {
            idFieldName = field.getName();
            TableId c = field.getAnnotation(TableId.class);
            idColumnName = c == null ? idFieldName : c.value();
            setIdFieldMethod = ReflectUtil.getMethod(
                    modelClass, "set" + StrUtil.upperFirst(idFieldName), idFieldClass);
            getIdFieldMethod = ReflectUtil.getMethod(
                    modelClass, "get" + StrUtil.upperFirst(idFieldName));
        }
        if (null != field.getAnnotation(JobUpdateTimeColumn.class)) {
            jobUpdateTimeFieldName = field.getName();
            jobUpdateTimeColumnName = this.safeMapToColumnName(jobUpdateTimeFieldName);
        }
        if (null != field.getAnnotation(TableLogic.class)) {
            deletedFlagFieldName = field.getName();
            deletedFlagColumnName = this.safeMapToColumnName(deletedFlagFieldName);
            setDeletedFlagMethod = ReflectUtil.getMethod(
                    modelClass, "set" + StrUtil.upperFirst(deletedFlagFieldName), Integer.class);
        }
        if (null != field.getAnnotation(TenantFilterColumn.class)) {
            tenantIdField = field;
            tenantIdFieldName = field.getName();
            tenantIdColumnName = this.safeMapToColumnName(tenantIdFieldName);
        }
        if (null != field.getAnnotation(FlowStatusColumn.class)) {
            flowStatusField = field;
        }
        if (null != field.getAnnotation(FlowLatestApprovalStatusColumn.class)) {
            flowLatestApprovalStatusField = field;
        }
        if (null != field.getAnnotation(MaskField.class)) {
            maskFieldList.add(field);
        }
        if (null != field.getAnnotation(AutoCodeField.class)) {
            autoCodeFieldList.add(field);
        }
        if (null != field.getAnnotation(UniqueVerifyField.class)) {
            uniqueVerifyFieldList.add(field);
        }
    }

    /**
     * 获取子类中注入的Mapper类。
     *
     * @return 子类中注入的Mapper类。
     */
    protected abstract BaseDaoMapper<M> mapper();
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveBatch(Collection<M> dataList) {
        dataList.forEach(baseMapper::insert);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewOrUpdate(M data, Consumer<M> saveNew, BiConsumer<M, M> update) {
        if (data == null) {
            return;
        }
        K id = (K) ReflectUtil.getFieldValue(data, idFieldName);
        if (id == null) {
            saveNew.accept(data);
        } else {
            update.accept(data, this.getById(id));
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewOrUpdateBatch(List<M> dataList, Consumer<List<M>> saveNewBatch, BiConsumer<M, M> update) {
        if (CollUtil.isEmpty(dataList)) {
            return;
        }
        List<M> saveNewDataList = dataList.stream()
                .filter(c -> ReflectUtil.getFieldValue(c, idFieldName) == null).collect(toList());
        if (CollUtil.isNotEmpty(saveNewDataList)) {
            saveNewBatch.accept(saveNewDataList);
        }
        List<M> updateDataList = dataList.stream()
                .filter(c -> ReflectUtil.getFieldValue(c, idFieldName) != null).collect(toList());
        if (CollUtil.isNotEmpty(updateDataList)) {
            for (M data : updateDataList) {
                K id = (K) ReflectUtil.getFieldValue(data, idFieldName);
                update.accept(data, this.getById(id));
            }
        }
    }

    /**
     * 根据过滤条件删除数据。
     *
     * @param filter 过滤对象。
     * @return 删除数量。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer removeBy(M filter) {
        return mapper().delete(new QueryWrapper<>(filter));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean remove(K id) {
        return mapper().deleteById(id) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBatchOneToManyRelation(
            String relationFieldName,
            Object relationFieldValue,
            String updateUserIdFieldName,
            String updateTimeFieldName,
            List<M> dataList,
            Consumer<List<M>> batchInserter) {
        // 删除在现有数据列表dataList中不存在的从表数据。
        QueryWrapper<M> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(this.safeMapToColumnName(relationFieldName), relationFieldValue);
        if (CollUtil.isNotEmpty(dataList)) {
            Set<Object> keptIdSet = dataList.stream()
                    .filter(c -> ReflectUtil.getFieldValue(c, idFieldName) != null)
                    .map(c -> ReflectUtil.getFieldValue(c, idFieldName)).collect(toSet());
            if (CollUtil.isNotEmpty(keptIdSet)) {
                queryWrapper.notIn(idColumnName, keptIdSet);
            }
        }
        mapper().delete(queryWrapper);
        if (CollUtil.isEmpty(dataList)) {
            return;
        }
        // 没有包含主键的对象被视为新对象，为了效率最优化，这里执行批量插入。
        List<M> newDataList = dataList.stream()
                .filter(c -> ReflectUtil.getFieldValue(c, idFieldName) == null).collect(toList());
        if (CollUtil.isNotEmpty(newDataList)) {
            newDataList.forEach(o -> ReflectUtil.setFieldValue(o, relationFieldName, relationFieldValue));
            batchInserter.accept(newDataList);
        }
        // 对于主键已经存在的数据，我们视为已存在数据，这里执行逐条更新操作。
        List<M> updateDataList =
                dataList.stream().filter(c -> ReflectUtil.getFieldValue(c, idFieldName) != null).collect(toList());
        for (M updateData : updateDataList) {
            // 如果前端将更新用户Id置空，这里使用当前用户更新该字段。
            if (updateUserIdFieldName != null) {
                ReflectUtil.setFieldValue(updateData, updateUserIdFieldName, TokenData.takeFromRequest().getUserId());
            }
            // 如果前端将更新时间置空，这里使用当前时间更新该字段。
            if (updateTimeFieldName != null) {
                ReflectUtil.setFieldValue(updateData, updateTimeFieldName, new Date());
            }
            if (this.tenantIdField != null) {
                ReflectUtil.setFieldValue(updateData, tenantIdField, TokenData.takeFromRequest().getTenantId());
            }
            if (this.deletedFlagFieldName != null) {
                ReflectUtil.setFieldValue(updateData, deletedFlagFieldName, GlobalDeletedFlag.NORMAL);
            }
            @SuppressWarnings("unchecked")
            K id = (K) ReflectUtil.getFieldValue(updateData, idFieldName);
            this.compareAndSetMaskFieldData(updateData, id);
            this.setAutoCodeFieldWithOriginalData(updateData, id);
            mapper().updateById(updateData);
        }
    }

    /**
     * 判断指定字段的数据是否存在，且仅仅存在一条记录。
     * 如果是基于主键的过滤，会直接调用existId过滤函数，提升性能。在有缓存的场景下，也可以利用缓存。
     *
     * @param fieldName  待过滤的字段名(Java 字段)。
     * @param fieldValue 字段值。
     * @return 存在且仅存在一条返回true，否则false。
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean existOne(String fieldName, Object fieldValue) {
        if (fieldName.equals(this.idFieldName)) {
            return this.existId((K) fieldValue);
        }
        String columnName = MyModelUtil.mapToColumnName(fieldName, modelClass);
        return mapper().selectCount(new QueryWrapper<M>().eq(columnName, fieldValue)) == 1;
    }

    /**
     * 判断主键Id关联的数据是否存在。
     *
     * @param id 主键Id。
     * @return 存在返回true，否则false。
     */
    @Override
    public boolean existId(K id) {
        return getById(id) != null;
    }

    @Override
    public M getOne(M filter) {
        return mapper().selectOne(new QueryWrapper<>(filter));
    }

    /**
     * 返回符合 filterField = filterValue 条件的一条数据。
     *
     * @param filterField 过滤的Java字段。
     * @param filterValue 过滤的Java字段值。
     * @return 查询后的数据对象。
     */
    @SuppressWarnings("unchecked")
    @Override
    public M getOne(String filterField, Object filterValue) {
        if (filterValue == null) {
            return null;
        }
        if (filterField.equals(idFieldName)) {
            return this.getById((K) filterValue);
        }
        String columnName = this.safeMapToColumnName(filterField);
        QueryWrapper<M> queryWrapper = new QueryWrapper<M>().eq(columnName, filterValue);
        return mapper().selectOne(queryWrapper);
    }

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     *
     * @param id            主表主键Id。
     * @param relationParam 实体对象数据组装的参数构建器。
     * @return 查询结果对象。
     */
    @Override
    public M getByIdWithRelation(K id, MyRelationParam relationParam) {
        M dataObject = this.getById(id);
        this.buildRelationForData(dataObject, relationParam);
        return dataObject;
    }

    @Override
    public M getById(Serializable id) {
        return this.mapper().selectById(id);
    }

    /**
     * 获取所有数据。
     *
     * @return 返回所有数据。
     */
    @Override
    public List<M> getAllList() {
        return mapper().selectList(Wrappers.emptyWrapper());
    }

    /**
     * 获取排序后所有数据。
     *
     * @param orderByProperties 需要排序的字段属性，这里使用Java对象中的属性名，而不是数据库字段名。
     * @return 返回排序后所有数据。
     */
    @Override
    public List<M> getAllListByOrder(String... orderByProperties) {
        List<String> columns = new ArrayList<>(orderByProperties.length);
        for (String orderByProperty : orderByProperties) {
            columns.add(this.safeMapToColumnName(orderByProperty));
        }
        return mapper().selectList(new QueryWrapper<M>().orderByAsc(columns));
    }

    @Override
    public boolean existAllPrimaryKeys(Set<K> idSet) {
        if (CollUtil.isEmpty(idSet)) {
            return true;
        }
        return this.existUniqueKeyList(idFieldName, idSet);
    }

    @Override
    public boolean existAllPrimaryKeys(String ids) {
        if (StrUtil.isBlank(ids)) {
            return true;
        }
        return this.existUniqueKeyList(idFieldName, ids, idFieldClass);
    }

    @Override
    public <T> boolean existUniqueKeyList(String inFilterField, Set<T> inFilterValues) {
        if (CollUtil.isEmpty(inFilterValues)) {
            return true;
        }
        String column = this.safeMapToColumnName(inFilterField);
        return mapper().selectCount(new QueryWrapper<M>().in(column, inFilterValues)) == inFilterValues.size();
    }

    @Override
    public <T> boolean existUniqueKeyList(String inFilterField, String inFilterValues, Class<T> clazz) {
        if (StrUtil.isBlank(inFilterValues)) {
            return true;
        }
        Set<Object> valueSet;
        Set<String> values = new HashSet<>(StrUtil.split(inFilterValues, StrUtil.COMMA));
        if (clazz.equals(Integer.class)) {
            valueSet = values.stream().map(Integer::valueOf).collect(toSet());
        } else if (clazz.equals(Long.class)) {
            valueSet = values.stream().map(Long::valueOf).collect(toSet());
        } else {
            valueSet = new HashSet<>(values);
        }
        return this.existUniqueKeyList(inFilterField, valueSet);
    }

    @Override
    public <R> List<R> notExist(String filterField, Set<R> filterSet, boolean findFirst) {
        List<R> notExistIdList = new LinkedList<>();
        int start = 0;
        int count = 1000;
        if (filterSet.size() > count) {
            do {
                int end = Math.min(filterSet.size(), start + count);
                List<R> subFilterList = CollUtil.sub(filterSet, start, end);
                doNotExistQuery(filterField, subFilterList, findFirst, notExistIdList);
                if ((findFirst && CollUtil.isNotEmpty(notExistIdList)) || end == filterSet.size()) {
                    break;
                }
                start += count;
            } while (true);
        } else {
            doNotExistQuery(filterField, filterSet, findFirst, notExistIdList);
        }
        return notExistIdList;
    }

    private <R> void doNotExistQuery(
            String filterField, Collection<R> filterSet, boolean findFirst, List<R> notExistIdList) {
        String columnName = this.safeMapToColumnName(filterField);
        QueryWrapper<M> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(columnName, filterSet);
        queryWrapper.select(columnName);
        Set<Object> existIdSet = mapper().selectList(queryWrapper).stream()
                .map(c -> ReflectUtil.getFieldValue(c, filterField)).collect(toSet());
        for (R filterData : filterSet) {
            if (!existIdSet.contains(filterData)) {
                notExistIdList.add(filterData);
                if (findFirst) {
                    break;
                }
            }
        }
    }

    @Override
    public List<M> getInList(Set<K> idValues) {
        return this.getInList(idFieldName, idValues, null);
    }

    @Override
    public <T> List<M> getInList(String inFilterField, Set<T> inFilterValues) {
        return this.getInList(inFilterField, inFilterValues, null);
    }

    @Override
    public <T> List<M> getInList(String inFilterField, Set<T> inFilterValues, String orderBy) {
        if (CollUtil.isEmpty(inFilterValues)) {
            return new LinkedList<>();
        }
        String column = this.safeMapToColumnName(inFilterField);
        QueryWrapper<M> queryWrapper = new QueryWrapper<M>().in(column, inFilterValues);
        if (StrUtil.isNotBlank(orderBy)) {
            queryWrapper.last(ORDER_BY + orderBy);
        }
        return mapper().selectList(queryWrapper);
    }

    @Override
    public List<M> getInListWithRelation(Set<K> idValues, MyRelationParam relationParam) {
        List<M> resultList = this.getInList(idValues);
        this.buildRelationForDataList(resultList, relationParam);
        return resultList;
    }

    @Override
    public <T> List<M> getInListWithRelation(String inFilterField, Set<T> inFilterValues, MyRelationParam relationParam) {
        List<M> resultList = this.getInList(inFilterField, inFilterValues);
        this.buildRelationForDataList(resultList, relationParam);
        return resultList;
    }

    @Override
    public <T> List<M> getInListWithRelation(
            String inFilterField, Set<T> inFilterValues, String orderBy, MyRelationParam relationParam) {
        List<M> resultList = this.getInList(inFilterField, inFilterValues, orderBy);
        this.buildRelationForDataList(resultList, relationParam);
        return resultList;
    }
    
    @Override
    public List<M> getNotInList(Set<K> idValues) {
        return this.getNotInList(idFieldName, idValues, null);
    }

    @Override
    public <T> List<M> getNotInList(String inFilterField, Set<T> inFilterValues) {
        return this.getNotInList(inFilterField, inFilterValues, null);
    }

    @Override
    public <T> List<M> getNotInList(String inFilterField, Set<T> inFilterValues, String orderBy) {
        QueryWrapper<M> queryWrapper;
        if (CollUtil.isEmpty(inFilterValues)) {
            queryWrapper = new QueryWrapper<>();
        } else {
            String column = this.safeMapToColumnName(inFilterField);
            queryWrapper = new QueryWrapper<M>().notIn(column, inFilterValues);
        }
        if (StrUtil.isNotBlank(orderBy)) {
            queryWrapper.last(ORDER_BY + orderBy);
        }
        return mapper().selectList(queryWrapper);
    }
    
    @Override
    public List<M> getNotInListWithRelation(Set<K> idValues, MyRelationParam relationParam) {
        List<M> resultList = this.getNotInList(idValues);
        this.buildRelationForDataList(resultList, relationParam);
        return resultList;
    }

    @Override
    public <T> List<M> getNotInListWithRelation(
            String inFilterField, Set<T> inFilterValues, MyRelationParam relationParam) {
        List<M> resultList = this.getNotInList(inFilterField, inFilterValues);
        this.buildRelationForDataList(resultList, relationParam);
        return resultList;
    }

    @Override
    public <T> List<M> getNotInListWithRelation(
            String inFilterField, Set<T> inFilterValues, String orderBy, MyRelationParam relationParam) {
        List<M> resultList = this.getNotInList(inFilterField, inFilterValues, orderBy);
        this.buildRelationForDataList(resultList, relationParam);
        return resultList;
    }

    @Override
    public long getCountByFilter(M filter) {
        return mapper().selectCount(new QueryWrapper<>(filter));
    }

    @Override
    public boolean existByFilter(M filter) {
        return this.getCountByFilter(filter) > 0;
    }

    @Override
    public List<M> getListByFilter(M filter) {
        return mapper().selectList(new QueryWrapper<>(filter));
    }

    @Override
    public List<M> getListWithRelationByFilter(M filter, String orderBy, MyRelationParam relationParam) {
        QueryWrapper<M> queryWrapper = new QueryWrapper<>(filter);
        if (StrUtil.isNotBlank(orderBy)) {
            queryWrapper.last(ORDER_BY + orderBy);
        }
        List<M> resultList = mapper().selectList(queryWrapper);
        this.buildRelationForDataList(resultList, relationParam);
        return resultList;
    }

    /**
     * 获取父主键Id下的所有子数据列表。
     *
     * @param parentIdFieldName 父主键字段名字，如"courseId"。
     * @param parentId          父主键的值。
     * @return 父主键Id下的所有子数据列表。
     */
    @Override
    public List<M> getListByParentId(String parentIdFieldName, K parentId) {
        QueryWrapper<M> queryWrapper = new QueryWrapper<>();
        String parentIdColumn = this.safeMapToColumnName(parentIdFieldName);
        if (parentId != null) {
            queryWrapper.eq(parentIdColumn, parentId);
        } else {
            queryWrapper.isNull(parentIdColumn);
        }
        return mapper().selectList(queryWrapper);
    }

    /**
     * 根据指定的显示字段列表、过滤条件字符串和分组字符串，返回聚合计算后的查询结果。(基本是内部框架使用，不建议外部接口直接使用)。
     *
     * @param selectFields 选择的字段列表，多个字段逗号分隔。
     *                     NOTE: 如果数据表字段和Java对象字段名字不同，Java对象字段应该以别名的形式出现。
     *                     如: table_column_name modelFieldName。否则无法被反射回Bean对象。
     * @param whereClause  SQL常量形式的条件从句。
     * @param groupBy      SQL常量形式分组字段列表，逗号分隔。
     * @return 聚合计算后的数据结果集。
     */
    @Override
    public List<Map<String, Object>> getGroupedListByCondition(
            String selectFields, String whereClause, String groupBy) {
        return mapper().getGroupedListByCondition(tableName, selectFields, whereClause, groupBy);
    }

    /**
     * 根据指定的显示字段列表、过滤条件字符串和排序字符串，返回查询结果。(基本是内部框架使用，不建议外部接口直接使用)。
     *
     * @param selectList  选择的Java字段列表。如果为空表示返回全部字段。
     * @param filter      过滤对象。
     * @param whereClause SQL常量形式的条件从句。
     * @param orderBy     SQL常量形式排序字段列表，逗号分隔。
     * @return 查询结果。
     */
    @Override
    public List<M> getListByCondition(List<String> selectList, M filter, String whereClause, String orderBy) {
        QueryWrapper<M> queryWrapper = new QueryWrapper<>(filter);
        if (CollUtil.isNotEmpty(selectList)) {
            String[] columns = new String[selectList.size()];
            for (int i = 0; i < selectList.size(); i++) {
                columns[i] = this.safeMapToColumnName(selectList.get(i));
            }
            queryWrapper.select(columns);
        }
        if (StrUtil.isNotBlank(whereClause)) {
            queryWrapper.apply(whereClause);
        }
        if (StrUtil.isNotBlank(orderBy)) {
            queryWrapper.last(ORDER_BY + orderBy);
        }
        return mapper().selectList(queryWrapper);
    }

    /**
     * 用指定过滤条件，计算记录数量。(基本是内部框架使用，不建议外部接口直接使用)。
     *
     * @param whereClause SQL常量形式的条件从句。
     * @return 返回过滤后的数据数量。
     */
    @Override
    public Integer getCountByCondition(String whereClause) {
        return mapper().getCountByCondition(this.tableName, whereClause);
    }

    @Override
    public void maskFieldData(M data, Set<String> ignoreFieldSet) {
        if (data != null) {
            this.maskFieldDataList(CollUtil.newArrayList(data), ignoreFieldSet);
        }
    }

    @Override
    public void maskFieldDataList(List<M> dataList, Set<String> ignoreFieldSet) {
        if (CollUtil.isEmpty(maskFieldList)) {
            return;
        }
        for (Field maskField : maskFieldList) {
            if (!CollUtil.contains(ignoreFieldSet, maskField.getName())) {
                MaskField anno = maskField.getAnnotation(MaskField.class);
                for (M data : dataList) {
                    Object maskedValue = this.doMaskFieldData(data, maskField, anno);
                    ReflectUtil.setFieldValue(data, maskField, maskedValue);
                }
            }
        }
    }

    @Override
    public void compareAndSetMaskFieldData(M data, M originalData) {
        if (CollUtil.isEmpty(maskFieldList)) {
            return;
        }
        for (Field maskField : maskFieldList) {
            Object value = ReflectUtil.getFieldValue(data, maskField);
            if (value == null) {
                continue;
            }
            MaskField anno = maskField.getAnnotation(MaskField.class);
            String maskChar = String.valueOf(anno.maskChar());
            // 如果此时包含了掩码字符，说明数据没有变化，就要和原字段值脱敏后的结果比对。
            // 如果一致就用脱敏前的原值，覆盖当前提交的(包含掩码的)值，否则说明进行了部分
            // 修改，但是字段值中仍然含有掩码字符，这是不允许的。
            if (value.toString().contains(maskChar)) {
                Object maskedOriginalValue = this.doMaskFieldData(originalData, maskField, anno);
                if (ObjectUtil.notEqual(value, maskedOriginalValue)) {
                    throw new MyRuntimeException("数据验证失败，不能仅修改部分脱敏数据！");
                }
                Object originalValue = ReflectUtil.getFieldValue(originalData, maskField);
                ReflectUtil.setFieldValue(data, maskField, originalValue);
            }
        }
    }

    @Override
    public void verifyMaskFieldData(M data) {
        if (CollUtil.isEmpty(maskFieldList)) {
            return;
        }
        for (Field field : maskFieldList) {
            Object value = ReflectUtil.getFieldValue(data, field);
            if (value != null) {
                String maskChar = String.valueOf(field.getAnnotation(MaskField.class).maskChar());
                if (value.toString().contains(maskChar)) {
                    throw new MyRuntimeException("数据验证失败，字段 [" + field.getName() + "] 数据存在脱敏掩码字符！");
                }
            }
        }
    }

    @Override
    public CallResult verifyUniqueFieldValue(M data, M originalData) {
        if (CollUtil.isEmpty(uniqueVerifyFieldList)) {
            return CallResult.ok();
        }
        for (Field field : uniqueVerifyFieldList) {
            Object value = ReflectUtil.getFieldValue(data, field);
            if (originalData != null) {
                Object originalValue = ReflectUtil.getFieldValue(originalData, field);
                if (ObjectUtil.equals(originalValue, value)) {
                    continue;
                }
            }
            if (value != null) {
                M filter = ReflectUtil.newInstance(this.modelClass);
                ReflectUtil.setFieldValue(filter, field, value);
                if (this.deletedFlagFieldName != null) {
                    ReflectUtil.setFieldValue(filter, this.deletedFlagFieldName, GlobalDeletedFlag.NORMAL);
                }
                List<M> dataList = this.getListByCondition(null, filter, null, null);
                if (CollUtil.isNotEmpty(dataList)) {
                    String errorMsg = StrFormatter.format(
                            "数据表 [{}] 字段值 [{}] 已存在，不能重复！", this.tableName, field.getName());
                    return CallResult.error(errorMsg);
                }
            }
        }
        return CallResult.ok();
    }

    @Override
    public CallResult verifyRelatedData(M data, M originalData) {
        return CallResult.ok();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CallResult verifyRelatedData(M data) {
        if (data == null) {
            return CallResult.ok();
        }
        Object id = ReflectUtil.getFieldValue(data, idFieldName);
        if (id == null) {
            return this.verifyRelatedData(data, null);
        }
        M originalData = this.getById((K) id);
        if (originalData == null) {
            return CallResult.error("数据验证失败，源数据不存在！");
        }
        return this.verifyRelatedData(data, originalData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CallResult verifyRelatedData(List<M> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return CallResult.ok();
        }
        // 1. 先过滤出数据列表中的主键Id集合。
        Set<K> idList = dataList.stream()
                .filter(c -> ReflectUtil.getFieldValue(c, idFieldName) != null)
                .map(c -> (K) ReflectUtil.getFieldValue(c, idFieldName)).collect(toSet());
        // 2. 列表中，我们目前仅支持全部是更新数据，或全部新增数据，不能混着。如果有主键值，说明当前全是更新数据。
        if (CollUtil.isNotEmpty(idList)) {
            // 3. 这里是批量读取的优化，用一个主键值得in list查询，一步获取全部原有数据。然后再在内存中基于Map排序。
            List<M> originalList = this.getInList(idList);
            Map<Object, M> originalMap = originalList.stream()
                    .collect(toMap(c -> ReflectUtil.getFieldValue(c, idFieldName), c2 -> c2));
            // 迭代列表，传入当前最新数据和更新前数据进行比对，如果关联数据变化了，就对新数据进行合法性验证。
            for (M data : dataList) {
                CallResult result = this.verifyRelatedData(
                        data, originalMap.get(ReflectUtil.getFieldValue(data, idFieldName)));
                if (!result.isSuccess()) {
                    return result;
                }
            }
        } else {
            // 4. 迭代列表，传入当前最新数据，对关联数据进行合法性验证。
            for (M data : dataList) {
                CallResult result = this.verifyRelatedData(data, null);
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }
        return CallResult.ok();
    }

    @Override
    public <R> CallResult verifyImportForConstDict(List<M> dataList, String fieldName, Function<M, R> idGetter) {
        if (CollUtil.isEmpty(dataList)) {
            return CallResult.ok();
        }
        // 这里均为内部调用方法，因此出现任何错误均为代码BUG，所以我们会及时抛出异常。
        Field field = ReflectUtil.getField(modelClass, fieldName);
        if (field == null) {
            String errorMessage = StrFormatter.format("FieldName [{}] doesn't exist", fieldName);
            throw new MyRuntimeException(errorMessage);
        }
        RelationConstDict relationConstDict = field.getAnnotation(RelationConstDict.class);
        if (relationConstDict == null) {
            String errorMessage = StrFormatter.format("FieldName [{}] doesn't have RelationConstDict.", fieldName);
            throw new MyRuntimeException(errorMessage);
        }
        Method m = ReflectUtil.getMethodByName(relationConstDict.constantDictClass(), "isValid");
        for (M data : dataList) {
            R id = idGetter.apply(data);
            if (id != null) {
                boolean ok = ReflectUtil.invokeStatic(m, id);
                if (!ok) {
                    String errorMessage = String.format("数据验证失败，字段 [%s] 存在无效的常量字典值 [%s]！",
                            relationConstDict.masterIdField(), id);
                    return CallResult.error(errorMessage, data);
                }
            }
        }
        return CallResult.ok();
    }

    @Override
    public <R> CallResult verifyImportForGlobalDict(List<M> dataList, String fieldName, Function<M, R> idGetter) {
        if (CollUtil.isEmpty(dataList)) {
            return CallResult.ok();
        }
        // 这里均为内部调用方法，因此出现任何错误均为代码BUG，所以我们会及时抛出异常。
        Field field = ReflectUtil.getField(modelClass, fieldName);
        if (field == null) {
            throw new MyRuntimeException(StrFormatter.format("FieldName [{}] does not exist.", fieldName));
        }
        RelationGlobalDict relationGlobalDict = field.getAnnotation(RelationGlobalDict.class);
        if (relationGlobalDict == null) {
            throw new MyRuntimeException(
                    StrFormatter.format("FieldName [{}] doesn't have RelationGlobalDict.", fieldName));
        }
        RelationStruct relationStruct = this.relationGlobalDictStructList.stream()
                .filter(c -> c.relationField.getName().equals(fieldName)).findFirst().orElse(null);
        Assert.notNull(relationStruct, "GlobalDictRelationStruct for [" + fieldName + "] can't be NULL");
        Map<Serializable, String> dictMap = ReflectUtil.invoke(
                relationStruct.service,
                relationStruct.globalDictMethd,
                relationStruct.relationGlobalDict.dictCode(), null);
        for (M data : dataList) {
            R id = idGetter.apply(data);
            if (id != null && !dictMap.containsKey(id.toString())) {
                String errorMessage = String.format("数据验证失败，字段 [%s] 存在无效的全局编码字典值 [%s]！",
                        relationGlobalDict.masterIdField(), id);
                return CallResult.error(errorMessage, data);
            }
        }
        return CallResult.ok();
    }

    @Override
    public <R> CallResult verifyImportForDict(List<M> dataList, String fieldName, Function<M, R> idGetter) {
        if (CollUtil.isEmpty(dataList)) {
            return CallResult.ok();
        }
        // 这里均为内部调用方法，因此出现任何错误均为代码BUG，所以我们会及时抛出异常。
        Field field = ReflectUtil.getField(modelClass, fieldName);
        if (field == null) {
            throw new MyRuntimeException(StrFormatter.format("FieldName [{}] does not exist.", fieldName));
        }
        RelationDict relationDict = field.getAnnotation(RelationDict.class);
        if (relationDict == null) {
            throw new MyRuntimeException(
                    StrFormatter.format("FieldName [{}] doesn't have RelationDict.", fieldName));
        }
        BaseService<Object, Serializable> service = ApplicationContextHolder.getBean(
                this.getNormalizedSlaveServiceName(relationDict.slaveServiceName(), relationDict.slaveModelClass()));
        Set<Object> dictIdSet = service.getAllList().stream()
                .map(c -> ReflectUtil.getFieldValue(c, relationDict.slaveIdField())).collect(toSet());
        for (M data : dataList) {
            R id = idGetter.apply(data);
            if (id != null && !dictIdSet.contains(id)) {
                String errorMessage = String.format("数据验证失败，字段 [%s] 存在无效的字典表字典值 [%s]！",
                        relationDict.masterIdField(), id);
                return CallResult.error(errorMessage, data);
            }
        }
        return CallResult.ok();
    }

    @Override
    public <R> CallResult verifyImportForDatasourceDict(List<M> dataList, String fieldName, Function<M, R> idGetter) {
        if (CollUtil.isEmpty(dataList)) {
            return CallResult.ok();
        }
        // 这里均为内部调用方法，因此出现任何错误均为代码BUG，所以我们会及时抛出异常。
        Field field = ReflectUtil.getField(modelClass, fieldName);
        if (field == null) {
            throw new MyRuntimeException(StrFormatter.format("FieldName [{}] doesn't exist.", fieldName));
        }
        RelationDict relationDict = field.getAnnotation(RelationDict.class);
        if (relationDict == null) {
            throw new MyRuntimeException(
                    StrFormatter.format("FieldName [{}] doesn't have RelationDict.", fieldName));
        }
        // 验证数据源字典Id，由于被依赖的数据表，可能包含大量业务数据，因此还是分批做存在性比对更为高效。
        Set<R> idSet = dataList.stream()
                .filter(c -> idGetter.apply(c) != null).map(idGetter).collect(toSet());
        if (CollUtil.isNotEmpty(idSet)) {
            if (idSet.iterator().next() instanceof String) {
                idSet = idSet.stream().filter(c -> StrUtil.isNotBlank((String) c)).collect(toSet());
            }
            BaseService<Object, Serializable> slaveService = ApplicationContextHolder.getBean(
                    this.getNormalizedSlaveServiceName(relationDict.slaveServiceName(), relationDict.slaveModelClass()));
            List<R> notExistIdList = slaveService.notExist(relationDict.slaveIdField(), idSet, true);
            if (CollUtil.isNotEmpty(notExistIdList)) {
                R notExistId = notExistIdList.get(0);
                String errorMessage = String.format("数据验证失败，字段 [%s] 存在无效的数据源表字典值 [%s]！",
                        relationDict.masterIdField(), notExistId);
                M data = dataList.stream()
                        .filter(c -> ObjectUtil.equals(idGetter.apply(c), notExistId)).findFirst().orElse(null);
                return CallResult.error(errorMessage, data);
            }
        }
        return CallResult.ok();
    }

    @Override
    public <R> CallResult verifyImportForOneToOneRelation(List<M> dataList, String fieldName, Function<M, R> idGetter) {
        if (CollUtil.isEmpty(dataList)) {
            return CallResult.ok();
        }
        // 这里均为内部调用方法，因此出现任何错误均为代码BUG，所以我们会及时抛出异常。
        Field field = ReflectUtil.getField(modelClass, fieldName);
        if (field == null) {
            throw new MyRuntimeException(StrFormatter.format("FieldName [{}] doesn't exist", fieldName));
        }
        RelationOneToOne relationOneToOne = field.getAnnotation(RelationOneToOne.class);
        if (relationOneToOne == null) {
            throw new MyRuntimeException(
                    StrFormatter.format("FieldName [{}] doesn't have RelationOneToOne.", fieldName));
        }
        // 验证一对一关联Id，由于被依赖的数据表，可能包含大量业务数据，因此还是分批做存在性比对更为高效。
        Set<R> idSet = dataList.stream()
                .filter(c -> idGetter.apply(c) != null).map(idGetter).collect(toSet());
        if (CollUtil.isNotEmpty(idSet)) {
            BaseService<Object, Serializable> slaveService = ApplicationContextHolder.getBean(
                    this.getNormalizedSlaveServiceName(relationOneToOne.slaveServiceName(), relationOneToOne.slaveModelClass()));
            List<R> notExistIdList = slaveService.notExist(relationOneToOne.slaveIdField(), idSet, true);
            if (CollUtil.isNotEmpty(notExistIdList)) {
                R notExistId = notExistIdList.get(0);
                String errorMessage = String.format("数据验证失败，字段 [%s] 存在无效的一对一关联值 [%s]！",
                        relationOneToOne.masterIdField(), notExistId);
                M data = dataList.stream()
                        .filter(c -> ObjectUtil.equals(idGetter.apply(c), notExistId)).findFirst().orElse(null);
                return CallResult.error(errorMessage, data);
            }
        }
        return CallResult.ok();
    }

    /**
     * 集成所有与主表实体对象相关的关联数据列表。包括本地和远程服务的一对一、字典、一对多和多对多聚合运算等。
     * 也可以根据实际需求，单独调用该函数所包含的各个数据集成函数。
     * NOTE: 该方法内执行的SQL将禁用数据权限过滤。
     *
     * @param resultList    主表实体对象列表。数据集成将直接作用于该对象列表。
     * @param relationParam 实体对象数据组装的参数构建器。
     */
    @Override
    public void buildRelationForDataList(List<M> resultList, MyRelationParam relationParam) {
        this.buildRelationForDataList(resultList, relationParam, null);
    }

    /**
     * 集成所有与主表实体对象相关的关联数据列表。包括一对一、字典、一对多和多对多聚合运算等。
     * 也可以根据实际需求，单独调用该函数所包含的各个数据集成函数。
     * NOTE: 该方法内执行的SQL将禁用数据权限过滤。
     *
     * @param resultList    主表实体对象列表。数据集成将直接作用于该对象列表。
     * @param relationParam 实体对象数据组装的参数构建器。
     * @param ignoreFields  该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    @Override
    public void buildRelationForDataList(
            List<M> resultList, MyRelationParam relationParam, Set<String> ignoreFields) {
        if (relationParam == null || CollUtil.isEmpty(resultList)) {
            return;
        }
        boolean dataFilterValue = GlobalThreadLocal.setDataFilter(false);
        try {
            // 集成本地一对一和字段级别的数据关联。
            boolean buildOneToOne = relationParam.isBuildOneToOne() || relationParam.isBuildOneToOneWithDict();
            // 这里集成一对一关联。
            if (buildOneToOne) {
                this.buildOneToOneForDataList(resultList, relationParam, ignoreFields);
            }
            // 集成一对多关联
            if (relationParam.isBuildOneToMany()) {
                this.buildOneToManyForDataList(resultList, relationParam, ignoreFields);
            }
            // 这里集成多对多关联。
            if (relationParam.isBuildRelationManyToMany()) {
                this.buildManyToManyForDataList(resultList, ignoreFields);
            }
            // 这里集成字典关联
            if (relationParam.isBuildDict()) {
                // 构建全局字典关联关系
                this.buildGlobalDictForDataList(resultList, ignoreFields);
                // 构建常量字典关联关系
                this.buildConstDictForDataList(resultList, ignoreFields);
                this.buildDictForDataList(resultList, buildOneToOne, ignoreFields);
            }
            // 组装本地聚合计算关联数据
            if (relationParam.isBuildRelationAggregation()) {
                // 处理多对多场景下，根据主表的结果，进行从表聚合数据的计算。
                this.buildManyToManyAggregationForDataList(resultList, buildAggregationAdditionalWhereCriteria(), ignoreFields);
                // 处理多一多场景下，根据主表的结果，进行从表聚合数据的计算。
                this.buildOneToManyAggregationForDataList(resultList, buildAggregationAdditionalWhereCriteria(), ignoreFields);
            }
        } finally {
            GlobalThreadLocal.setDataFilter(dataFilterValue);
        }
    }

    /**
     * 该函数主要用于对查询结果的批量导出。不同于支持分页的列表查询，批量导出没有分页机制，
     * 因此在导出数据量较大的情况下，很容易给数据库的内存、CPU和IO带来较大的压力。而通过
     * 我们的分批处理，可以极大的规避该问题的出现几率。调整batchSize的大小，也可以有效的
     * 改善运行效率。
     * 我们目前的处理机制是，先从主表取出所有符合条件的主表数据，这样可以避免分批处理时，
     * 后面几批数据，因为skip过多而带来的效率问题。因为是单表过滤，不会给数据库带来过大的压力。
     * 之后再在主表结果集数据上进行分批级联处理。
     * 集成所有与主表实体对象相关的关联数据列表。包括一对一、字典、一对多和多对多聚合运算等。
     * 也可以根据实际需求，单独调用该函数所包含的各个数据集成函数。
     * NOTE: 该方法内执行的SQL将禁用数据权限过滤。
     *
     * @param resultList    主表实体对象列表。数据集成将直接作用于该对象列表。
     * @param relationParam 实体对象数据组装的参数构建器。
     * @param batchSize     每批集成的记录数量。小于等于0时将不做分批处理。
     */
    @Override
    public void buildRelationForDataList(List<M> resultList, MyRelationParam relationParam, int batchSize) {
        this.buildRelationForDataList(resultList, relationParam, batchSize, null);
    }

    /**
     * 该函数主要用于对查询结果的批量导出。不同于支持分页的列表查询，批量导出没有分页机制，
     * 因此在导出数据量较大的情况下，很容易给数据库的内存、CPU和IO带来较大的压力。而通过
     * 我们的分批处理，可以极大的规避该问题的出现几率。调整batchSize的大小，也可以有效的
     * 改善运行效率。
     * 我们目前的处理机制是，先从主表取出所有符合条件的主表数据，这样可以避免分批处理时，
     * 后面几批数据，因为skip过多而带来的效率问题。因为是单表过滤，不会给数据库带来过大的压力。
     * 之后再在主表结果集数据上进行分批级联处理。
     * 集成所有与主表实体对象相关的关联数据列表。包括一对一、字典、一对多和多对多聚合运算等。
     * 也可以根据实际需求，单独调用该函数所包含的各个数据集成函数。
     * NOTE: 该方法内执行的SQL将禁用数据权限过滤。
     *
     * @param resultList    主表实体对象列表。数据集成将直接作用于该对象列表。
     * @param relationParam 实体对象数据组装的参数构建器。
     * @param batchSize     每批集成的记录数量。小于等于0时将不做分批处理。
     * @param ignoreFields  该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    @Override
    public void buildRelationForDataList(
            List<M> resultList, MyRelationParam relationParam, int batchSize, Set<String> ignoreFields) {
        if (CollUtil.isEmpty(resultList)) {
            return;
        }
        if (batchSize <= 0) {
            this.buildRelationForDataList(resultList, relationParam);
            return;
        }
        int totalCount = resultList.size();
        int fromIndex = 0;
        int toIndex = Math.min(batchSize, totalCount);
        while (toIndex > fromIndex) {
            List<M> subResultList = resultList.subList(fromIndex, toIndex);
            this.buildRelationForDataList(subResultList, relationParam, ignoreFields);
            fromIndex = toIndex;
            toIndex = Math.min(batchSize + fromIndex, totalCount);
        }
    }

    /**
     * 集成所有与主表实体对象相关的关联数据对象。包括本地和远程服务的一对一、字典、一对多和多对多聚合运算等。
     * 也可以根据实际需求，单独调用该函数所包含的各个数据集成函数。
     * NOTE: 该方法内执行的SQL将禁用数据权限过滤。
     *
     * @param dataObject    主表实体对象。数据集成将直接作用于该对象。
     * @param relationParam 实体对象数据组装的参数构建器。
     * @param <T>           实体对象类型。
     */
    @Override
    public <T extends M> void buildRelationForData(T dataObject, MyRelationParam relationParam) {
        this.buildRelationForData(dataObject, relationParam, null);
    }

    /**
     * 集成所有与主表实体对象相关的关联数据对象。包括一对一、字典、一对多和多对多聚合运算等。
     * 也可以根据实际需求，单独调用该函数所包含的各个数据集成函数。
     * NOTE: 该方法内执行的SQL将禁用数据权限过滤。
     *
     * @param dataObject    主表实体对象。数据集成将直接作用于该对象。
     * @param relationParam 实体对象数据组装的参数构建器。
     * @param ignoreFields  该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     * @param <T>           实体对象类型。
     */
    @Override
    public <T extends M> void buildRelationForData(T dataObject, MyRelationParam relationParam, Set<String> ignoreFields) {
        if (dataObject == null || relationParam == null) {
            return;
        }
        boolean dataFilterValue = GlobalThreadLocal.setDataFilter(false);
        try {
            // 集成本地一对一和字段级别的数据关联。
            boolean buildOneToOne = relationParam.isBuildOneToOne() || relationParam.isBuildOneToOneWithDict();
            if (buildOneToOne) {
                this.buildOneToOneForData(dataObject, relationParam, ignoreFields);
            }
            // 集成一对多关联
            if (relationParam.isBuildOneToMany()) {
                this.buildOneToManyForData(dataObject, relationParam, ignoreFields);
            }
            if (relationParam.isBuildDict()) {
                // 构建全局字典关联关系
                this.buildGlobalDictForData(dataObject, ignoreFields);
                // 构建常量字典关联关系
                this.buildConstDictForData(dataObject, ignoreFields);
                // 构建本地数据字典关联关系。
                this.buildDictForData(dataObject, buildOneToOne, ignoreFields);
            }
            // 组装本地聚合计算关联数据
            if (relationParam.isBuildRelationAggregation()) {
                // 开始处理多对多场景。
                buildManyToManyAggregationForData(dataObject, buildAggregationAdditionalWhereCriteria(), ignoreFields);
                // 构建一对多场景
                buildOneToManyAggregationForData(dataObject, buildAggregationAdditionalWhereCriteria(), ignoreFields);
            }
            if (relationParam.isBuildRelationManyToMany()) {
                this.buildRelationManyToMany(dataObject, ignoreFields);
            }
        } finally {
            GlobalThreadLocal.setDataFilter(dataFilterValue);
        }
    }

    protected <T extends M> void buildLocalOneToOneDictOnly(T dataObject) {
        if (dataObject == null || CollUtil.isEmpty(this.localRelationOneToOneStructList)) {
            return;
        }
        for (RelationStruct relationStruct : this.localRelationOneToOneStructList) {
            BaseService<Object, Serializable> relationService = relationStruct.service;
            Object relationObject = ReflectUtil.getFieldValue(dataObject, relationStruct.relationField);
            if (relationObject != null) {
                @SuppressWarnings("unchecked")
                BaseService<Object, Serializable> proxyTarget =
                        (BaseService<Object, Serializable>) AopTargetUtil.getTarget(relationService);
                // 关联本地字典
                proxyTarget.buildDictForData(relationObject, false, null);
                // 关联全局字典
                proxyTarget.buildGlobalDictForData(relationObject, null);
                // 关联常量字典
                proxyTarget.buildConstDictForData(relationObject, null);
            }
        }
    }

    /**
     * 集成主表和多对多中间表之间的关联关系。
     *
     * @param dataObject   关联后的主表数据对象。
     * @param ignoreFields 该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private <T extends M> void buildRelationManyToMany(T dataObject, Set<String> ignoreFields) {
        if (dataObject == null || CollUtil.isEmpty(this.localRelationManyToManyStructList)) {
            return;
        }
        for (RelationStruct relationStruct : this.localRelationManyToManyStructList) {
            if (ignoreFields != null && ignoreFields.contains(relationStruct.relationField.getName())) {
                continue;
            }
            RelationManyToMany r = relationStruct.relationManyToMany;
            String masterIdColumn = MyModelUtil.safeMapToColumnName(r.relationMasterIdField(), r.relationModelClass());
            Object masterIdValue = ReflectUtil.getFieldValue(dataObject, idFieldName);
            Map<String, Object> filterMap = new HashMap<>(1);
            filterMap.put(masterIdColumn, masterIdValue);
            List<?> manyToManyList = relationStruct.manyToManyMapper.selectByMap(filterMap);
            ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, manyToManyList);
        }
    }

    /**
     * 为实体对象参数列表数据集成本地静态字典关联数据。
     *
     * @param resultList   主表数据列表。
     * @param ignoreFields 该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private void buildConstDictForDataList(List<M> resultList, Set<String> ignoreFields) {
        if (CollUtil.isEmpty(this.relationConstDictStructList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        for (M result : resultList) {
            this.buildConstDictForData(result, ignoreFields);
        }
    }

    /**
     * 为实体对象参数列表数据集成全局字典关联数据。
     *
     * @param resultList   主表数据列表。
     * @param ignoreFields 该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private void buildGlobalDictForDataList(List<M> resultList, Set<String> ignoreFields) {
        if (CollUtil.isEmpty(this.relationGlobalDictStructList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        for (RelationStruct relationStruct : this.relationGlobalDictStructList) {
            Set<Object> masterIdSet = resultList.stream()
                    .map(obj -> ReflectUtil.getFieldValue(obj, relationStruct.masterIdField))
                    .filter(Objects::nonNull)
                    .collect(toSet());
            if (CollUtil.isEmpty(masterIdSet) || CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                continue;
            }
            if (relationStruct.relationGlobalDict.multiSelect()) {
                List<String> normalizedMasterIdSet = masterIdSet.stream()
                        .filter(Objects::nonNull)
                        .filter(id -> StrUtil.isNotBlank(id.toString()))
                        .map(Object::toString)
                        .collect(toList());
                if (CollUtil.isNotEmpty(normalizedMasterIdSet)) {
                    String allIds = CollUtil.join(normalizedMasterIdSet, StrUtil.COMMA);
                    Set<String> idSet = new HashSet<>(StrUtil.split(allIds, StrUtil.COMMA));
                    Map<Serializable, String> dictMap = ReflectUtil.invoke(
                            relationStruct.service,
                            relationStruct.globalDictMethd,
                            relationStruct.relationGlobalDict.dictCode(), idSet);
                    MyModelUtil.makeGlobalDictListRelation(
                            modelClass, resultList, dictMap, relationStruct.relationField.getName());
                }
            } else  {
                Map<Serializable, String> dictMap = ReflectUtil.invoke(
                        relationStruct.service,
                        relationStruct.globalDictMethd,
                        relationStruct.relationGlobalDict.dictCode(), masterIdSet);
                MyModelUtil.makeGlobalDictRelation(
                        modelClass, resultList, dictMap, relationStruct.relationField.getName());
            }
        }
    }

    /**
     * 为参数实体对象数据集成本地静态字典关联数据。
     *
     * @param dataObject   实体对象。
     * @param ignoreFields 该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private <T extends M> void buildConstDictForData(T dataObject, Set<String> ignoreFields) {
        if (dataObject == null || CollUtil.isEmpty(this.relationConstDictStructList)) {
            return;
        }
        for (RelationStruct relationStruct : this.relationConstDictStructList) {
            Object id = ReflectUtil.getFieldValue(dataObject, relationStruct.masterIdField);
            if (id == null || CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                continue;
            }
            if (relationStruct.relationConstDict.multiSelect()) {
                List<String> ids = StrUtil.split(id.toString(), StrUtil.COMMA);
                Serializable firstDictId = (Serializable) relationStruct.dictMap.keySet().iterator().next();
                Set<Serializable> dictIds = this.convertByObjectType(firstDictId.getClass(), ids);
                List<Map<String, Object>> dictMapList = new LinkedList<>();
                dictIds.stream().filter(dictId -> relationStruct.dictMap.containsKey(dictId)).forEach(dictId -> {
                    String name = MapUtil.get(relationStruct.dictMap, dictId, String.class);
                    Map<String, Object> dictMap = new HashMap<>(2);
                    dictMap.put("id", id);
                    dictMap.put("name", name);
                    dictMapList.add(dictMap);
                });
                ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, dictMapList);
            } else {
                String name = MapUtil.get(relationStruct.dictMap, id, String.class);
                if (name != null) {
                    Map<String, Object> dictMap = new HashMap<>(2);
                    dictMap.put("id", id);
                    dictMap.put("name", name);
                    ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, dictMap);
                }
            }
        }
    }

    /**
     * 为参数实体对象数据集成全局字典关联数据。
     *
     * @param dataObject   实体对象。
     * @param ignoreFields 该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private <T extends M> void buildGlobalDictForData(T dataObject, Set<String> ignoreFields) {
        if (dataObject == null || CollUtil.isEmpty(this.relationGlobalDictStructList)) {
            return;
        }
        for (RelationStruct relationStruct : this.relationGlobalDictStructList) {
            Object id = ReflectUtil.getFieldValue(dataObject, relationStruct.masterIdField);
            if (id == null || CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                continue;
            }
            if (relationStruct.relationGlobalDict.multiSelect()) {
                Set<String> ids = new HashSet<>(StrUtil.split(id.toString(), StrUtil.COMMA));
                Map<Serializable, String> dictMap = ReflectUtil.invoke(
                        relationStruct.service,
                        relationStruct.globalDictMethd,
                        relationStruct.relationGlobalDict.dictCode(), ids);
                MyModelUtil.makeGlobalDictListRelation(
                        modelClass, CollUtil.newArrayList(dataObject), dictMap, relationStruct.relationField.getName());
            } else {
                Map<Serializable, String> dictMap = ReflectUtil.invoke(
                        relationStruct.service,
                        relationStruct.globalDictMethd,
                        relationStruct.relationGlobalDict.dictCode(), CollUtil.newHashSet(id));
                String name = dictMap.get(id.toString());
                if (name != null) {
                    Map<String, Object> reulstDictMap = new HashMap<>(2);
                    reulstDictMap.put("id", id);
                    reulstDictMap.put("name", name);
                    ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, reulstDictMap);
                }
            }
        }
    }

    /**
     * 为实体对象参数列表数据集成本地字典关联数据。
     *
     * @param resultList       实体对象数据列表。
     * @param hasBuiltOneToOne 性能优化参数。如果该值为true，同时注解参数RelationDict.equalOneToOneRelationField
     *                         不为空，则直接从已经完成一对一数据关联的从表对象中获取数据，减少一次数据库交互。
     * @param ignoreFields     该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private void buildDictForDataList(List<M> resultList, boolean hasBuiltOneToOne, Set<String> ignoreFields) {
        if (CollUtil.isEmpty(this.localRelationDictStructList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        for (RelationStruct relationStruct : this.localRelationDictStructList) {
            Set<Object> masterIdSet = resultList.stream()
                    .map(obj -> ReflectUtil.getFieldValue(obj, relationStruct.masterIdField))
                    .filter(Objects::nonNull)
                    .collect(toSet());
            if (CollUtil.isEmpty(masterIdSet) || CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                continue;
            }
            List<Object> relationList;
            if (relationStruct.relationDict.multiSelect()) {
                List<String> normalizedMasterIdSet = masterIdSet.stream()
                        .filter(Objects::nonNull)
                        .filter(id -> StrUtil.isNotBlank(id.toString()))
                        .map(Object::toString)
                        .collect(toList());
                String allIds = CollUtil.join(normalizedMasterIdSet, StrUtil.COMMA);
                Field f = ReflectUtil.getField(
                        relationStruct.relationDict.slaveModelClass(), relationStruct.relationDict.slaveIdField());
                Set<Object> idSet = this.convertByFieldType(f, StrUtil.split(allIds, StrUtil.COMMA));
                relationList = relationStruct.service.getInList(relationStruct.relationDict.slaveIdField(), idSet);
                MyModelUtil.makeDictListRelation(
                        modelClass, resultList, relationList, relationStruct.relationField.getName());
            } else {
                if (hasBuiltOneToOne && relationStruct.equalOneToOneRelationField != null) {
                    relationList = resultList.stream()
                            .map(obj -> ReflectUtil.getFieldValue(obj, relationStruct.equalOneToOneRelationField))
                            .filter(Objects::nonNull)
                            .collect(toList());
                } else {
                    String slaveId = relationStruct.relationDict.slaveIdField();
                    relationList = relationStruct.service.getInList(slaveId, masterIdSet);
                }
                MyModelUtil.makeDictRelation(
                        modelClass, resultList, relationList, relationStruct.relationField.getName());
            }
        }
    }

    /**
     * 为实体对象数据集成本地数据字典关联数据。
     *
     * @param dataObject       实体对象。
     * @param hasBuiltOneToOne 性能优化参数。如果该值为true，同时注解参数RelationDict.equalOneToOneRelationField
     *                         不为空，则直接从已经完成一对一数据关联的从表对象中获取数据，减少一次数据库交互。
     * @param ignoreFields     该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private <T extends M> void buildDictForData(T dataObject, boolean hasBuiltOneToOne, Set<String> ignoreFields) {
        if (dataObject == null || CollUtil.isEmpty(this.localRelationDictStructList)) {
            return;
        }
        for (RelationStruct relationStruct : this.localRelationDictStructList) {
            Object id = ReflectUtil.getFieldValue(dataObject, relationStruct.masterIdField);
            if (id == null || CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                continue;
            }
            if (relationStruct.relationDict.multiSelect()) {
                Field f = ReflectUtil.getField(
                        relationStruct.relationDict.slaveModelClass(), relationStruct.relationDict.slaveIdField());
                Set<Object> idSet = this.convertByFieldType(f, StrUtil.split(id.toString(), StrUtil.COMMA));
                List<Object> relationList = relationStruct.service.getInList(relationStruct.relationDict.slaveIdField(), idSet);
                MyModelUtil.makeDictListRelation(
                        modelClass, dataObject, relationList, relationStruct.relationField.getName());
            } else {
                Object relationObject;
                if (hasBuiltOneToOne && relationStruct.equalOneToOneRelationField != null) {
                    relationObject = ReflectUtil.getFieldValue(dataObject, relationStruct.equalOneToOneRelationField);
                } else {
                    relationObject = relationStruct.service.getOne(relationStruct.relationDict.slaveIdField(), id);
                }
                MyModelUtil.makeDictRelation(
                        modelClass, dataObject, relationObject, relationStruct.relationField.getName());
            }
        }
    }

    private Set<Object> convertByFieldType(Field f, List<String> ids) {
        Set<Object> idSet;
        if (f.getType().equals(Integer.class)) {
            idSet = ids.stream().map(Integer::valueOf).collect(toSet());
        } else if (f.getType().equals(Long.class)) {
            idSet = ids.stream().map(Long::valueOf).collect(toSet());
        } else {
            idSet = new HashSet<>(ids);
        }
        return idSet;
    }

    private <T extends Serializable> Set<Serializable> convertByObjectType(Class<T> clazz, List<String> ids) {
        Set<Serializable> idSet;
        if (clazz.equals(Integer.class)) {
            idSet = ids.stream().map(Integer::valueOf).collect(toSet());
        } else if (clazz.equals(Long.class)) {
            idSet = ids.stream().map(Long::valueOf).collect(toSet());
        } else {
            idSet = new HashSet<>(ids);
        }
        return idSet;
    }

    /**
     * 为实体对象参数列表数据集成本地一对一关联数据。
     *
     * @param resultList    实体对象数据列表。
     * @param relationParam 关联从参数对象。
     * @param ignoreFields  该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private void buildOneToOneForDataList(List<M> resultList, MyRelationParam relationParam, Set<String> ignoreFields) {
        if (CollUtil.isEmpty(this.localRelationOneToOneStructList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        boolean withDict = relationParam.isBuildOneToOneWithDict();
        for (RelationStruct relationStruct : this.localRelationOneToOneStructList) {
            if (CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                continue;
            }
            Set<Object> masterIdSet = resultList.stream()
                    .map(obj -> ReflectUtil.getFieldValue(obj, relationStruct.masterIdField))
                    .filter(Objects::nonNull)
                    .collect(toSet());
            // 从主表集合中，抽取主表关联字段的集合，再以in list形式去从表中查询。
            if (CollUtil.isNotEmpty(masterIdSet)) {
                BaseService<Object, Serializable> relationService = relationStruct.service;
                List<Object> relationList =
                        relationService.getInList(relationStruct.relationOneToOne.slaveIdField(), masterIdSet);
                Set<String> igoreMaskFieldSet = null;
                if (relationParam.getIgnoreMaskFieldMap() != null) {
                    igoreMaskFieldSet = relationParam.getIgnoreMaskFieldMap()
                            .get(relationStruct.relationOneToOne.slaveModelClass().getSimpleName());
                }
                relationService.maskFieldDataList(relationList, igoreMaskFieldSet);
                // 仅仅当需要加载从表字典关联时，才去加载。
                if (withDict && relationStruct.relationOneToOne.loadSlaveDict() && CollUtil.isNotEmpty(relationList)) {
                    @SuppressWarnings("unchecked")
                    BaseService<Object, Serializable> proxyTarget =
                            (BaseService<Object, Serializable>) AopTargetUtil.getTarget(relationService);
                    // 关联本地字典。
                    proxyTarget.buildDictForDataList(relationList, false, ignoreFields);
                    // 关联全局字典
                    proxyTarget.buildGlobalDictForDataList(relationList, ignoreFields);
                    // 关联常量字典
                    proxyTarget.buildConstDictForDataList(relationList, ignoreFields);
                }
                MyModelUtil.makeOneToOneRelation(
                        modelClass, resultList, relationList, relationStruct.relationField.getName());
            }
        }
    }

    /**
     * 为实体对象数据集成本地一对一关联数据。
     *
     * @param dataObject    实体对象。
     * @param relationParam 从表数据关联参数对象。
     * @param ignoreFields  该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private void buildOneToOneForData(M dataObject, MyRelationParam relationParam, Set<String> ignoreFields) {
        if (dataObject == null || CollUtil.isEmpty(this.localRelationOneToOneStructList)) {
            return;
        }
        boolean withDict = relationParam.isBuildOneToOneWithDict();
        for (RelationStruct relationStruct : this.localRelationOneToOneStructList) {
            if (ignoreFields != null && ignoreFields.contains(relationStruct.relationField.getName())) {
                continue;
            }
            Object id = ReflectUtil.getFieldValue(dataObject, relationStruct.masterIdField);
            if (id != null) {
                BaseService<Object, Serializable> relationService = relationStruct.service;
                Object relationObject = relationService.getOne(relationStruct.relationOneToOne.slaveIdField(), id);
                Set<String> ignoreMaskFieldSet = null;
                if (relationParam.getIgnoreMaskFieldMap() != null) {
                    ignoreMaskFieldSet = relationParam.getIgnoreMaskFieldMap()
                            .get(relationStruct.relationOneToOne.slaveModelClass().getSimpleName());
                }
                relationService.maskFieldData(relationObject, ignoreMaskFieldSet);
                // 仅仅当需要加载从表字典关联时，才去加载。
                if (withDict && relationStruct.relationOneToOne.loadSlaveDict() && relationObject != null) {
                    @SuppressWarnings("unchecked")
                    BaseService<Object, Serializable> proxyTarget =
                            (BaseService<Object, Serializable>) AopTargetUtil.getTarget(relationService);
                    // 关联本地字典
                    proxyTarget.buildDictForData(relationObject, false, ignoreFields);
                    // 关联全局字典
                    proxyTarget.buildGlobalDictForData(relationObject, ignoreFields);
                    // 关联常量字典
                    proxyTarget.buildConstDictForData(relationObject, ignoreFields);
                }
                ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, relationObject);
            }
        }
    }

    private void buildOneToManyForDataList(List<M> resultList, MyRelationParam relationParam, Set<String> ignoreFields) {
        if (CollUtil.isEmpty(this.localRelationOneToManyStructList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        for (RelationStruct relationStruct : this.localRelationOneToManyStructList) {
            if (ignoreFields != null && ignoreFields.contains(relationStruct.relationField.getName())) {
                continue;
            }
            Set<Object> masterIdSet = resultList.stream()
                    .map(obj -> ReflectUtil.getFieldValue(obj, relationStruct.masterIdField))
                    .filter(Objects::nonNull)
                    .collect(toSet());
            // 从主表集合中，抽取主表关联字段的集合，再以in list形式去从表中查询。
            if (CollUtil.isNotEmpty(masterIdSet)) {
                BaseService<Object, Serializable> relationService = relationStruct.service;
                List<Object> relationList = relationService.getInListWithRelation(
                        relationStruct.relationOneToMany.slaveIdField(), masterIdSet, MyRelationParam.dictOnly());
                MyModelUtil.makeOneToManyRelation(
                        modelClass, resultList, relationList, relationStruct.relationField.getName());
                Set<String> ignoreMaskFieldSet = null;
                if (relationParam.getIgnoreMaskFieldMap() != null) {
                    ignoreMaskFieldSet = relationParam.getIgnoreMaskFieldMap()
                            .get(relationStruct.relationOneToMany.slaveModelClass().getSimpleName());
                }
                for (M data : resultList) {
                    @SuppressWarnings("unchecked")
                    List<Object> relationDataList =
                            (List<Object>) ReflectUtil.getFieldValue(data, relationStruct.relationField.getName());
                    relationService.maskFieldDataList(relationDataList, ignoreMaskFieldSet);
                }
            }
        }
    }

    private void buildOneToManyForData(M dataObject, MyRelationParam relationParam, Set<String> ignoreFields) {
        if (dataObject == null || CollUtil.isEmpty(this.localRelationOneToManyStructList)) {
            return;
        }
        for (RelationStruct relationStruct : this.localRelationOneToManyStructList) {
            if (ignoreFields != null && ignoreFields.contains(relationStruct.relationField.getName())) {
                continue;
            }
            Object id = ReflectUtil.getFieldValue(dataObject, relationStruct.masterIdField);
            if (id != null) {
                BaseService<Object, Serializable> relationService = relationStruct.service;
                Set<Object> masterIdSet = new HashSet<>(1);
                masterIdSet.add(id);
                List<Object> relationObject = relationService.getInListWithRelation(
                        relationStruct.relationOneToMany.slaveIdField(), masterIdSet, MyRelationParam.dictOnly());
                Set<String> ignoreMaskFieldSet = null;
                if (relationParam.getIgnoreMaskFieldMap() != null) {
                    ignoreMaskFieldSet = relationParam.getIgnoreMaskFieldMap()
                            .get(relationStruct.relationOneToMany.slaveModelClass().getSimpleName());
                }
                relationService.maskFieldDataList(relationObject, ignoreMaskFieldSet);
                ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, relationObject);
            }
        }
    }

    private void buildManyToManyForDataList(List<M> resultList, Set<String> ignoreFields) {
        if (CollUtil.isEmpty(this.localRelationManyToManyStructList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        for (RelationStruct relationStruct : this.localRelationManyToManyStructList) {
            if (ignoreFields != null && ignoreFields.contains(relationStruct.relationField.getName())) {
                continue;
            }
            Set<Object> masterIdSet = resultList.stream()
                    .map(obj -> ReflectUtil.getFieldValue(obj, idFieldName))
                    .filter(Objects::nonNull)
                    .collect(toSet());
            // 从主表集合中，抽取主表关联字段的集合，再以in list形式去从表中查询。
            if (CollUtil.isNotEmpty(masterIdSet)) {
                RelationManyToMany r = relationStruct.relationManyToMany;
                String masterIdColumn = MyModelUtil.safeMapToColumnName(r.relationMasterIdField(), r.relationModelClass());
                QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
                queryWrapper.in(masterIdColumn, masterIdSet);
                List<Object> relationList = relationStruct.manyToManyMapper.selectList(queryWrapper);
                MyModelUtil.makeManyToManyRelation(
                        modelClass, idFieldName, resultList, relationList, relationStruct.relationField.getName());
            }
        }
    }

    /**
     * 根据实体对象参数列表和过滤条件，集成本地多对多关联聚合计算数据。
     *
     * @param resultList      实体对象数据列表。
     * @param criteriaListMap 过滤参数。key为主表字段名称，value是过滤条件列表。
     * @param ignoreFields    该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private void buildManyToManyAggregationForDataList(
            List<M> resultList, Map<String, List<MyWhereCriteria>> criteriaListMap, Set<String> ignoreFields) {
        if (CollUtil.isEmpty(this.localRelationManyToManyAggrStructList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        if (criteriaListMap == null) {
            criteriaListMap = new HashMap<>(this.localRelationManyToManyAggrStructList.size());
        }
        boolean defaultTenantFilter = GlobalThreadLocal.setTenantIdFilter(false);
        try {
            for (RelationStruct relationStruct : this.localRelationManyToManyAggrStructList) {
                if (!CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                    this.doBuildManyToManyAggregationForDataList(resultList, criteriaListMap, relationStruct);
                }
            }
        } finally {
            GlobalThreadLocal.setTenantIdFilter(defaultTenantFilter);
        }
    }

    private void doBuildManyToManyAggregationForDataList(
            List<M> resultList, Map<String, List<MyWhereCriteria>> criteriaListMap, RelationStruct relationStruct) {
        Set<Object> masterIdSet = resultList.stream()
                .map(obj -> ReflectUtil.getFieldValue(obj, relationStruct.masterIdField))
                .filter(Objects::nonNull)
                .collect(toSet());
        if (CollUtil.isEmpty(masterIdSet)) {
            return;
        }
        RelationManyToManyAggregation relation = relationStruct.relationManyToManyAggregation;
        // 提取关联中用到的各种字段和表数据。
        BasicAggregationRelationInfo basicRelationInfo =
                this.parseBasicAggregationRelationInfo(relationStruct, criteriaListMap);
        // 构建多表关联的where语句
        StringBuilder whereClause = new StringBuilder(256);
        // 如果需要从表聚合计算或参与过滤，则需要把中间表和从表之间的关联条件加上。
        if (!basicRelationInfo.onlySelectRelationTable) {
            whereClause.append(basicRelationInfo.relationTable)
                    .append(".")
                    .append(basicRelationInfo.relationSlaveColumn)
                    .append(" = ")
                    .append(basicRelationInfo.slaveTable)
                    .append(".")
                    .append(basicRelationInfo.slaveColumn);
        } else {
            whereClause.append("1 = 1");
        }
        List<MyWhereCriteria> criteriaList = criteriaListMap.get(relationStruct.relationField.getName());
        if (criteriaList == null) {
            criteriaList = new LinkedList<>();
        }
        MyWhereCriteria inlistFilter = new MyWhereCriteria();
        inlistFilter.setCriteria(relation.relationModelClass(),
                relation.relationMasterIdField(), MyWhereCriteria.OPERATOR_IN, masterIdSet);
        criteriaList.add(inlistFilter);
        if (StrUtil.isNotBlank(relationStruct.service.deletedFlagFieldName)) {
            MyWhereCriteria deleteFilter = new MyWhereCriteria();
            deleteFilter.setCriteria(
                    relation.slaveModelClass(),
                    relationStruct.service.deletedFlagFieldName,
                    MyWhereCriteria.OPERATOR_EQUAL,
                    GlobalDeletedFlag.NORMAL);
            criteriaList.add(deleteFilter);
        }
        String criteriaString = MyWhereCriteria.makeCriteriaString(criteriaList);
        whereClause.append(AND_OP).append(criteriaString);
        StringBuilder tableNames = new StringBuilder(64);
        tableNames.append(basicRelationInfo.relationTable);
        if (!basicRelationInfo.onlySelectRelationTable) {
            tableNames.append(", ").append(basicRelationInfo.slaveTable);
        }
        List<Map<String, Object>> aggregationMapList =
                mapper().getGroupedListByCondition(tableNames.toString(),
                        basicRelationInfo.selectList, whereClause.toString(), basicRelationInfo.groupBy);
        doMakeLocalAggregationData(aggregationMapList, resultList, relationStruct);
    }

    /**
     * 根据实体对象和过滤条件，集成本地多对多关联聚合计算数据。
     *
     * @param dataObject      实体对象。
     * @param criteriaListMap 过滤参数。key为主表字段名称，value是过滤条件列表。
     * @param ignoreFields    该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private <T extends M> void buildManyToManyAggregationForData(
            T dataObject, Map<String, List<MyWhereCriteria>> criteriaListMap, Set<String> ignoreFields) {
        if (dataObject == null || CollUtil.isEmpty(this.localRelationManyToManyAggrStructList)) {
            return;
        }
        if (criteriaListMap == null) {
            criteriaListMap = new HashMap<>(localRelationManyToManyAggrStructList.size());
        }
        boolean defaultTenantFilter = GlobalThreadLocal.setTenantIdFilter(false);
        try {
            for (RelationStruct relationStruct : this.localRelationManyToManyAggrStructList) {
                Object masterIdValue = ReflectUtil.getFieldValue(dataObject, relationStruct.masterIdField);
                if (masterIdValue == null || CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                    continue;
                }
                BasicAggregationRelationInfo basicRelationInfo =
                        this.parseBasicAggregationRelationInfo(relationStruct, criteriaListMap);
                // 组装过滤条件
                String whereClause = this.makeManyToManyWhereClause(
                        relationStruct, masterIdValue, basicRelationInfo, criteriaListMap);
                StringBuilder tableNames = new StringBuilder(64);
                tableNames.append(basicRelationInfo.relationTable);
                if (!basicRelationInfo.onlySelectRelationTable) {
                    tableNames.append(", ").append(basicRelationInfo.slaveTable);
                }
                List<Map<String, Object>> aggregationMapList =
                        mapper().getGroupedListByCondition(tableNames.toString(),
                                basicRelationInfo.selectList, whereClause, basicRelationInfo.groupBy);
                // 将查询后的结果回填到主表数据中。
                if (CollUtil.isNotEmpty(aggregationMapList)) {
                    Object value = aggregationMapList.get(0).get(AGGREGATED_VALUE);
                    if (value != null) {
                        ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, value);
                    }
                }
            }
        } finally {
            GlobalThreadLocal.setTenantIdFilter(defaultTenantFilter);
        }
    }

    /**
     * 根据实体对象参数列表和过滤条件，集成本地一对多关联聚合计算数据。
     *
     * @param resultList      实体对象数据列表。
     * @param criteriaListMap 过滤参数。key为主表字段名称，value是过滤条件列表。
     * @param ignoreFields    该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private void buildOneToManyAggregationForDataList(
            List<M> resultList, Map<String, List<MyWhereCriteria>> criteriaListMap, Set<String> ignoreFields) {
        // 处理多一多场景下，根据主表的结果，进行从表聚合数据的计算。
        if (CollUtil.isEmpty(this.localRelationOneToManyAggrStructList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        if (criteriaListMap == null) {
            criteriaListMap = new HashMap<>(localRelationOneToManyAggrStructList.size());
        }
        boolean defaultTenantFilter = GlobalThreadLocal.setTenantIdFilter(false);
        try {
            for (RelationStruct relationStruct : this.localRelationOneToManyAggrStructList) {
                if (CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                    continue;
                }
                Set<Object> masterIdSet = resultList.stream()
                        .map(obj -> ReflectUtil.getFieldValue(obj, relationStruct.masterIdField))
                        .filter(Objects::nonNull)
                        .collect(toSet());
                if (CollUtil.isNotEmpty(masterIdSet)) {
                    RelationOneToManyAggregation relation = relationStruct.relationOneToManyAggregation;
                    // 开始获取后面所需的各种关联数据。此部分今后可以移植到缓存中，无需每次计算。
                    String slaveTable = MyModelUtil.mapToTableName(relation.slaveModelClass());
                    String slaveColumnName = MyModelUtil.mapToColumnName(relation.slaveIdField(), relation.slaveModelClass());
                    Tuple2<String, String> selectAndGroupByTuple = makeSelectListAndGroupByClause(
                            slaveTable, slaveColumnName, relation.slaveModelClass(),
                            slaveTable, relation.aggregationField(), relation.aggregationType());
                    String selectList = selectAndGroupByTuple.getFirst();
                    String groupBy = selectAndGroupByTuple.getSecond();
                    List<MyWhereCriteria> criteriaList = criteriaListMap.get(relationStruct.relationField.getName());
                    if (criteriaList == null) {
                        criteriaList = new LinkedList<>();
                    }
                    MyWhereCriteria inlistFilter = new MyWhereCriteria();
                    inlistFilter.setCriteria(relation.slaveModelClass(),
                            relation.slaveIdField(), MyWhereCriteria.OPERATOR_IN, masterIdSet);
                    criteriaList.add(inlistFilter);
                    if (StrUtil.isNotBlank(relationStruct.service.deletedFlagFieldName)) {
                        MyWhereCriteria deleteFilter = new MyWhereCriteria();
                        deleteFilter.setCriteria(
                                relation.slaveModelClass(),
                                relationStruct.service.deletedFlagFieldName,
                                MyWhereCriteria.OPERATOR_EQUAL,
                                GlobalDeletedFlag.NORMAL);
                        criteriaList.add(deleteFilter);
                    }
                    String criteriaString = MyWhereCriteria.makeCriteriaString(criteriaList);
                    List<Map<String, Object>> aggregationMapList =
                            mapper().getGroupedListByCondition(slaveTable, selectList, criteriaString, groupBy);
                    doMakeLocalAggregationData(aggregationMapList, resultList, relationStruct);
                }
            }
        } finally {
            GlobalThreadLocal.setTenantIdFilter(defaultTenantFilter);
        }
    }

    /**
     * 根据实体对象和过滤条件，集成本地一对多关联聚合计算数据。
     *
     * @param dataObject      实体对象。
     * @param criteriaListMap 过滤参数。key为主表字段名称，value是过滤条件列表。
     * @param ignoreFields    该集合中的字段，即便包含注解也不会在当前调用中进行数据组装。
     */
    private <T extends M> void buildOneToManyAggregationForData(
            T dataObject, Map<String, List<MyWhereCriteria>> criteriaListMap, Set<String> ignoreFields) {
        if (dataObject == null || CollUtil.isEmpty(this.localRelationOneToManyAggrStructList)) {
            return;
        }
        if (criteriaListMap == null) {
            criteriaListMap = new HashMap<>(localRelationOneToManyAggrStructList.size());
        }
        boolean defaultTenantFilter = GlobalThreadLocal.setTenantIdFilter(false);
        try {
            for (RelationStruct relationStruct : this.localRelationOneToManyAggrStructList) {
                if (CollUtil.contains(ignoreFields, relationStruct.relationField.getName())) {
                    continue;
                }
                Object masterIdValue = ReflectUtil.getFieldValue(dataObject, relationStruct.masterIdField);
                if (masterIdValue != null) {
                    RelationOneToManyAggregation relation = relationStruct.relationOneToManyAggregation;
                    String slaveTable = MyModelUtil.mapToTableName(relation.slaveModelClass());
                    String slaveColumnName =
                            MyModelUtil.mapToColumnName(relation.slaveIdField(), relation.slaveModelClass());
                    Tuple2<String, String> selectAndGroupByTuple = makeSelectListAndGroupByClause(
                            slaveTable, slaveColumnName, relation.slaveModelClass(),
                            slaveTable, relation.aggregationField(), relation.aggregationType());
                    String selectList = selectAndGroupByTuple.getFirst();
                    String groupBy = selectAndGroupByTuple.getSecond();
                    String whereClause = this.makeOneToManyWhereClause(
                            relationStruct, masterIdValue, slaveColumnName, criteriaListMap);
                    // 获取分组聚合计算结果
                    List<Map<String, Object>> aggregationMapList =
                            mapper().getGroupedListByCondition(slaveTable, selectList, whereClause, groupBy);
                    // 将计算结果回填到主表关联字段
                    if (CollUtil.isNotEmpty(aggregationMapList)) {
                        Object value = aggregationMapList.get(0).get(AGGREGATED_VALUE);
                        if (value != null) {
                            ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, value);
                        }
                    }
                }
            }
        } finally {
            GlobalThreadLocal.setTenantIdFilter(defaultTenantFilter);
        }
    }

    /**
     * 仅仅在spring boot 启动后的监听器事件中调用，缓存所有service的关联关系，加速后续的数据绑定效率。
     */
    @Override
    public void loadRelationStruct() {
        Field[] fields = ReflectUtil.getFields(modelClass);
        for (Field f : fields) {
            initializeRelationDictStruct(f);
            initializeRelationStruct(f);
            initializeRelationAggregationStruct(f);
        }
    }

    /**
     * 缺省实现返回null，在进行一对多和多对多聚合计算时，没有额外的自定义过滤条件。如有需要，需子类自行实现。
     *
     * @return 自定义过滤条件列表。
     */
    protected Map<String, List<MyWhereCriteria>> buildAggregationAdditionalWhereCriteria() {
        return null;
    }

    /**
     * 判断当前对象的关联字段数据是否需要被验证，如果原有对象为null，表示新对象第一次插入，则必须验证。
     *
     * @param object         新对象。
     * @param originalObject 原有对象。
     * @param fieldGetter    获取需要验证字段的函数对象。
     * @param <T>            需要验证字段的类型。
     * @return 需要关联验证返回true，否则false。
     */
    protected <T> boolean needToVerify(M object, M originalObject, Function<M, T> fieldGetter) {
        if (object == null) {
            return false;
        }
        T data = fieldGetter.apply(object);
        if (data == null) {
            return false;
        }
        if (data instanceof String) {
            String stringData = (String) data;
            if (stringData.length() == 0) {
                return false;
            }
        }
        if (originalObject == null) {
            return true;
        }
        T originalData = fieldGetter.apply(originalObject);
        return !data.equals(originalData);
    }

    /**
     * 因为Mybatis Plus中QueryWrapper的条件方法都要求传入数据表字段名，因此提供该函数将
     * Java实体对象的字段名转换为数据表字段名，如果不存在会抛出异常。
     * 另外在MyModelUtil.mapToColumnName有一级缓存，对于查询过的对象字段都会放到缓存中，
     * 下次映射转换的时候，会直接从缓存获取。
     *
     * @param fieldName Java实体对象的字段名。
     * @return 对应的数据表字段名。
     */
    protected String safeMapToColumnName(String fieldName) {
        String columnName = MyModelUtil.mapToColumnName(fieldName, modelClass);
        if (columnName == null) {
            throw new InvalidDataFieldException(modelClass.getSimpleName(), fieldName);
        }
        return columnName;
    }

    /**
     * 因为Mybatis Plus在update的时候，不能将实体对象中值为null的字段，更新为null，
     * 而且忽略更新，在全部更新场景下，这个是非常重要的，所以我们写了这个函数绕开这一问题。
     * 该函数会遍历实体对象中，所有不包含@Transient注解，没有transient修饰符的字段，如果
     * 当前对象的该字段值为null，则会调用UpdateWrapper的set方法，将该字段赋值为null。
     * 相比于其他重载方法，该方法会将参数中的主键id，设置到UpdateWrapper的过滤条件中。
     *
     * @param o  实体对象。
     * @param id 实体对象的主键值。
     * @return 创建后的UpdateWrapper。
     */
    protected UpdateWrapper<M> createUpdateQueryForNullValue(M o, K id) {
        UpdateWrapper<M> uw = createUpdateQueryForNullValue(o, modelClass);
        try {
            M filter = modelClass.newInstance();
            this.setIdFieldMethod.invoke(filter, id);
            uw.setEntity(filter);
        } catch (Exception e) {
            log.error("Failed to call reflection code of BaseService.createUpdateQueryForNullValue.", e);
            throw new MyRuntimeException(e);
        }
        return uw;
    }

    /**
     * 因为Mybatis Plus在update的时候，不能将实体对象中值为null的字段，更新为null，
     * 而且忽略更新，在全部更新场景下，这个是非常重要的，所以我们写了这个函数绕开这一问题。
     * 该函数会遍历实体对象中，所有不包含@Transient注解，没有transient修饰符的字段，如果
     * 当前对象的该字段值为null，则会调用UpdateWrapper的set方法，将该字段赋值为null。
     *
     * @param o 实体对象。
     * @return 创建后的UpdateWrapper。
     */
    protected UpdateWrapper<M> createUpdateQueryForNullValue(M o) {
        return createUpdateQueryForNullValue(o, modelClass);
    }

    /**
     * 因为Mybatis Plus在update的时候，不能将实体对象中值为null的字段，更新为null，
     * 而且忽略更新，在全部更新场景下，这个是非常重要的，所以我们写了这个函数绕开这一问题。
     * 该函数会遍历实体对象中，所有不包含@Transient注解，没有transient修饰符的字段，如果
     * 当前对象的该字段值为null，则会调用UpdateWrapper的set方法，将该字段赋值为null。
     *
     * @param o     实体对象。
     * @param clazz 实体对象的class。
     * @return 创建后的UpdateWrapper。
     */
    public static <T> UpdateWrapper<T> createUpdateQueryForNullValue(T o, Class<T> clazz) {
        UpdateWrapper<T> uw = new UpdateWrapper<>();
        Field[] fields = ReflectUtil.getFields(clazz);
        List<String> nullColumnList = new LinkedList<>();
        for (Field field : fields) {
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField == null || tableField.exist()) {
                int modifiers = field.getModifiers();
                // transient类型的字段不能作为查询条件，静态字段和逻辑删除都不考虑。
                int transientMask = 128;
                if ((modifiers & transientMask) == 1
                        || Modifier.isStatic(modifiers)
                        || field.getAnnotation(TableLogic.class) != null) {
                    continue;
                }
                // 仅当实体对象参数中，当前字段值为null的时候，才会赋值给UpdateWrapper。
                // 以便在后续的更新中，可以将这些null字段的值设置到数据库表对应的字段中。
                if (ReflectUtil.getFieldValue(o, field) == null) {
                    nullColumnList.add(MyModelUtil.safeMapToColumnName(field.getName(), clazz));
                }
            }
        }
        if (CollUtil.isNotEmpty(nullColumnList)) {
            for (String nullColumn : nullColumnList) {
                uw.set(nullColumn, null);
            }
        }
        return uw;
    }

    @SuppressWarnings("unchecked")
    private void initializeRelationStruct(Field f) {
        RelationOneToOne relationOneToOne = f.getAnnotation(RelationOneToOne.class);
        if (relationOneToOne != null) {
            RelationStruct relationStruct = new RelationStruct();
            relationStruct.relationField = f;
            relationStruct.masterIdField = ReflectUtil.getField(modelClass, relationOneToOne.masterIdField());
            relationStruct.relationOneToOne = relationOneToOne;
            if (!relationOneToOne.slaveServiceClass().equals(DummyClass.class)) {
                relationStruct.service = (BaseService<Object, Serializable>)
                        ApplicationContextHolder.getBean(relationOneToOne.slaveServiceClass());
            } else {
                relationStruct.service = ApplicationContextHolder.getBean(
                        this.getNormalizedSlaveServiceName(relationOneToOne.slaveServiceName(), relationOneToOne.slaveModelClass()));
            }
            localRelationOneToOneStructList.add(relationStruct);
            return;
        }
        RelationOneToMany relationOneToMany = f.getAnnotation(RelationOneToMany.class);
        if (relationOneToMany != null) {
            RelationStruct relationStruct = new RelationStruct();
            relationStruct.relationField = f;
            relationStruct.masterIdField = ReflectUtil.getField(modelClass, relationOneToMany.masterIdField());
            relationStruct.relationOneToMany = relationOneToMany;
            if (!relationOneToMany.slaveServiceClass().equals(DummyClass.class)) {
                relationStruct.service = (BaseService<Object, Serializable>)
                        ApplicationContextHolder.getBean(relationOneToMany.slaveServiceClass());
            } else {
                relationStruct.service = ApplicationContextHolder.getBean(
                        this.getNormalizedSlaveServiceName(relationOneToMany.slaveServiceName(), relationOneToMany.slaveModelClass()));
            }
            localRelationOneToManyStructList.add(relationStruct);
            return;
        }
        RelationManyToMany relationManyToMany = f.getAnnotation(RelationManyToMany.class);
        if (relationManyToMany != null) {
            RelationStruct relationStruct = new RelationStruct();
            relationStruct.relationField = f;
            relationStruct.masterIdField = ReflectUtil.getField(modelClass, relationManyToMany.relationMasterIdField());
            relationStruct.relationManyToMany = relationManyToMany;
            String relationMapperName = relationManyToMany.relationMapperName();
            if (StrUtil.isBlank(relationMapperName)) {
                relationMapperName = relationManyToMany.relationModelClass().getSimpleName() + "Mapper";
            }
            relationStruct.manyToManyMapper = ApplicationContextHolder.getBean(StrUtil.lowerFirst(relationMapperName));
            localRelationManyToManyStructList.add(relationStruct);
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeRelationAggregationStruct(Field f) {
        RelationOneToManyAggregation relationOneToManyAggregation = f.getAnnotation(RelationOneToManyAggregation.class);
        if (relationOneToManyAggregation != null) {
            RelationStruct relationStruct = new RelationStruct();
            relationStruct.relationField = f;
            relationStruct.masterIdField = ReflectUtil.getField(modelClass, relationOneToManyAggregation.masterIdField());
            relationStruct.relationOneToManyAggregation = relationOneToManyAggregation;
            if (!relationOneToManyAggregation.slaveServiceClass().equals(DummyClass.class)) {
                relationStruct.service = (BaseService<Object, Serializable>)
                        ApplicationContextHolder.getBean(relationOneToManyAggregation.slaveServiceClass());
            } else {
                relationStruct.service = ApplicationContextHolder.getBean(this.getNormalizedSlaveServiceName(
                        relationOneToManyAggregation.slaveServiceName(), relationOneToManyAggregation.slaveModelClass()));
            }
            relationStruct.service = (BaseService<Object, Serializable>) AopTargetUtil.getTarget(relationStruct.service);
            localRelationOneToManyAggrStructList.add(relationStruct);
            return;
        }
        RelationManyToManyAggregation relationManyToManyAggregation = f.getAnnotation(RelationManyToManyAggregation.class);
        if (relationManyToManyAggregation != null) {
            RelationStruct relationStruct = new RelationStruct();
            relationStruct.relationField = f;
            relationStruct.masterIdField = ReflectUtil.getField(modelClass, relationManyToManyAggregation.masterIdField());
            relationStruct.relationManyToManyAggregation = relationManyToManyAggregation;
            if (!relationManyToManyAggregation.slaveServiceClass().equals(DummyClass.class)) {
                relationStruct.service = (BaseService<Object, Serializable>)
                        ApplicationContextHolder.getBean(relationManyToManyAggregation.slaveServiceClass());
            } else {
                relationStruct.service = ApplicationContextHolder.getBean(this.getNormalizedSlaveServiceName(
                        relationManyToManyAggregation.slaveServiceName(), relationManyToManyAggregation.slaveModelClass()));
            }
            relationStruct.service = (BaseService<Object, Serializable>) AopTargetUtil.getTarget(relationStruct.service);
            localRelationManyToManyAggrStructList.add(relationStruct);
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeRelationDictStruct(Field f) {
        RelationConstDict relationConstDict = f.getAnnotation(RelationConstDict.class);
        if (relationConstDict != null) {
            RelationStruct relationStruct = new RelationStruct();
            relationStruct.relationConstDict = relationConstDict;
            relationStruct.relationField = f;
            relationStruct.masterIdField = ReflectUtil.getField(modelClass, relationConstDict.masterIdField());
            Field dictMapField = ReflectUtil.getField(relationConstDict.constantDictClass(), "DICT_MAP");
            relationStruct.dictMap = (Map<Object, String>) ReflectUtil.getStaticFieldValue(dictMapField);
            relationConstDictStructList.add(relationStruct);
            return;
        }
        RelationGlobalDict relationGlobalDict = f.getAnnotation(RelationGlobalDict.class);
        if (relationGlobalDict != null) {
            RelationStruct relationStruct = new RelationStruct();
            relationStruct.relationGlobalDict = relationGlobalDict;
            relationStruct.relationField = f;
            relationStruct.masterIdField = ReflectUtil.getField(modelClass, relationGlobalDict.masterIdField());
            relationStruct.service = ApplicationContextHolder.getBean("globalDictService");
            relationStruct.globalDictMethd = ReflectUtil.getMethodByName(
                    relationStruct.service.getClass(), "getGlobalDictItemDictMapFromCache");
            relationGlobalDictStructList.add(relationStruct);
            return;
        }
        RelationDict relationDict = f.getAnnotation(RelationDict.class);
        if (relationDict != null) {
            RelationStruct relationStruct = new RelationStruct();
            relationStruct.relationField = f;
            relationStruct.masterIdField = ReflectUtil.getField(modelClass, relationDict.masterIdField());
            relationStruct.relationDict = relationDict;
            if (StrUtil.isNotBlank(relationDict.equalOneToOneRelationField())) {
                relationStruct.equalOneToOneRelationField =
                        ReflectUtil.getField(modelClass, relationDict.equalOneToOneRelationField());
            }
            if (!relationDict.slaveServiceClass().equals(DummyClass.class)) {
                relationStruct.service = (BaseService<Object, Serializable>)
                        ApplicationContextHolder.getBean(relationDict.slaveServiceClass());
            } else {
                relationStruct.service = ApplicationContextHolder.getBean(
                        this.getNormalizedSlaveServiceName(relationDict.slaveServiceName(), relationDict.slaveModelClass()));
            }
            localRelationDictStructList.add(relationStruct);
        }
    }

    private BasicAggregationRelationInfo parseBasicAggregationRelationInfo(
            RelationStruct relationStruct, Map<String, List<MyWhereCriteria>> criteriaListMap) {
        RelationManyToManyAggregation relation = relationStruct.relationManyToManyAggregation;
        BasicAggregationRelationInfo relationInfo = new BasicAggregationRelationInfo();
        // 提取关联中用到的各种字段和表数据。
        relationInfo.slaveTable = MyModelUtil.mapToTableName(relation.slaveModelClass());
        relationInfo.relationTable = MyModelUtil.mapToTableName(relation.relationModelClass());
        relationInfo.relationMasterColumn =
                MyModelUtil.mapToColumnName(relation.relationMasterIdField(), relation.relationModelClass());
        relationInfo.relationSlaveColumn =
                MyModelUtil.mapToColumnName(relation.relationSlaveIdField(), relation.relationModelClass());
        relationInfo.slaveColumn = MyModelUtil.mapToColumnName(relation.slaveIdField(), relation.slaveModelClass());
        // 判断是否只需要关联中间表即可，从而提升查询统计的效率。
        // 1. 统计字段为中间表字段。2. 自定义过滤条件中没有基于从表字段的过滤条件。
        relationInfo.onlySelectRelationTable =
                relation.aggregationModelClass().equals(relation.relationModelClass());
        if (relationInfo.onlySelectRelationTable && MapUtil.isNotEmpty(criteriaListMap)) {
            List<MyWhereCriteria> criteriaList =
                    criteriaListMap.get(relationStruct.relationField.getName());
            if (CollUtil.isNotEmpty(criteriaList)) {
                for (MyWhereCriteria whereCriteria : criteriaList) {
                    if (whereCriteria.getModelClazz().equals(relation.slaveModelClass())) {
                        relationInfo.onlySelectRelationTable = false;
                        break;
                    }
                }
            }
        }
        String aggregationTable = relation.aggregationModelClass().equals(relation.relationModelClass())
                ? relationInfo.relationTable : relationInfo.slaveTable;
        Tuple2<String, String> selectAndGroupByTuple = makeSelectListAndGroupByClause(
                relationInfo.relationTable, relationInfo.relationMasterColumn, relation.aggregationModelClass(),
                aggregationTable, relation.aggregationField(), relation.aggregationType());
        relationInfo.selectList = selectAndGroupByTuple.getFirst();
        relationInfo.groupBy = selectAndGroupByTuple.getSecond();
        return relationInfo;
    }

    private String makeManyToManyWhereClause(
            RelationStruct relationStruct,
            Object masterIdValue,
            BasicAggregationRelationInfo basicRelationInfo,
            Map<String, List<MyWhereCriteria>> criteriaListMap) {
        StringBuilder whereClause = new StringBuilder(256);
        whereClause.append(basicRelationInfo.relationTable)
                .append(".").append(basicRelationInfo.relationMasterColumn);
        if (masterIdValue instanceof Number) {
            whereClause.append(" = ").append(masterIdValue);
        } else {
            whereClause.append(" = '").append(masterIdValue).append("'");
        }
        // 如果需要从表聚合计算或参与过滤，则需要把中间表和从表之间的关联条件加上。
        if (!basicRelationInfo.onlySelectRelationTable) {
            whereClause.append(AND_OP)
                    .append(basicRelationInfo.relationTable)
                    .append(".")
                    .append(basicRelationInfo.relationSlaveColumn)
                    .append(" = ")
                    .append(basicRelationInfo.slaveTable)
                    .append(".")
                    .append(basicRelationInfo.slaveColumn);
        }
        List<MyWhereCriteria> criteriaList = criteriaListMap.get(relationStruct.relationField.getName());
        if (criteriaList == null) {
            criteriaList = new LinkedList<>();
        }
        if (StrUtil.isNotBlank(relationStruct.service.deletedFlagFieldName)) {
            MyWhereCriteria deleteFilter = new MyWhereCriteria();
            deleteFilter.setCriteria(
                    relationStruct.relationManyToManyAggregation.slaveModelClass(),
                    relationStruct.service.deletedFlagFieldName,
                    MyWhereCriteria.OPERATOR_EQUAL,
                    GlobalDeletedFlag.NORMAL);
            criteriaList.add(deleteFilter);
        }
        if (CollUtil.isNotEmpty(criteriaList)) {
            String criteriaString = MyWhereCriteria.makeCriteriaString(criteriaList);
            whereClause.append(AND_OP).append(criteriaString);
        }
        return whereClause.toString();
    }

    private String makeOneToManyWhereClause(
            RelationStruct relationStruct,
            Object masterIdValue,
            String slaveColumnName,
            Map<String, List<MyWhereCriteria>> criteriaListMap) {
        StringBuilder whereClause = new StringBuilder(64);
        if (masterIdValue instanceof Number) {
            whereClause.append(slaveColumnName).append(" = ").append(masterIdValue);
        } else {
            whereClause.append(slaveColumnName).append(" = '").append(masterIdValue).append("'");
        }
        List<MyWhereCriteria> criteriaList = criteriaListMap.get(relationStruct.relationField.getName());
        if (criteriaList == null) {
            criteriaList = new LinkedList<>();
        }
        if (StrUtil.isNotBlank(relationStruct.service.deletedFlagFieldName)) {
            MyWhereCriteria deleteFilter = new MyWhereCriteria();
            deleteFilter.setCriteria(
                    relationStruct.relationOneToManyAggregation.slaveModelClass(),
                    relationStruct.service.deletedFlagFieldName,
                    MyWhereCriteria.OPERATOR_EQUAL,
                    GlobalDeletedFlag.NORMAL);
            criteriaList.add(deleteFilter);
        }
        if (CollUtil.isNotEmpty(criteriaList)) {
            String criteriaString = MyWhereCriteria.makeCriteriaString(criteriaList);
            whereClause.append(AND_OP).append(criteriaString);
        }
        return whereClause.toString();
    }

    private static class BasicAggregationRelationInfo {
        private String slaveTable;
        private String slaveColumn;
        private String relationTable;
        private String relationMasterColumn;
        private String relationSlaveColumn;
        private String selectList;
        private String groupBy;
        private boolean onlySelectRelationTable;
    }

    private void doMakeLocalAggregationData(
            List<Map<String, Object>> aggregationMapList, List<M> resultList, RelationStruct relationStruct) {
        if (CollUtil.isEmpty(resultList)) {
            return;
        }
        // 根据获取的分组聚合结果集，绑定到主表总的关联字段。
        if (CollUtil.isNotEmpty(aggregationMapList)) {
            Map<String, Object> relatedMap = new HashMap<>(aggregationMapList.size());
            String groupedKey = GROUPED_KEY;
            String aggregatedValue = AGGREGATED_VALUE;
            if (!aggregationMapList.get(0).containsKey(groupedKey)) {
                groupedKey = groupedKey.toLowerCase();
                aggregatedValue = aggregatedValue.toLowerCase();
            }
            for (Map<String, Object> map : aggregationMapList) {
                relatedMap.put(map.get(groupedKey).toString(), map.get(aggregatedValue));
            }
            for (M dataObject : resultList) {
                Object masterIdValue = ReflectUtil.getFieldValue(dataObject, relationStruct.masterIdField);
                if (masterIdValue != null) {
                    Object value = relatedMap.get(masterIdValue.toString());
                    if (value != null) {
                        ReflectUtil.setFieldValue(dataObject, relationStruct.relationField, value);
                    }
                }
            }
        }
    }

    private Tuple2<String, String> makeSelectListAndGroupByClause(
            String groupTableName,
            String groupColumnName,
            Class<?> aggregationModel,
            String aggregationTableName,
            String aggregationField,
            Integer aggregationType) {
        if (!AggregationType.isValid(aggregationType)) {
            throw new IllegalArgumentException("Invalid AggregationType Value ["
                    + aggregationType + "] in Model [" + aggregationModel.getName() + "].");
        }
        StringBuilder groupedSelectList = new StringBuilder(128);
        String aggregationColumn = MyModelUtil.mapToColumnName(aggregationField, aggregationModel);
        if (StrUtil.isBlank(aggregationColumn)) {
            throw new IllegalArgumentException("Invalid AggregationField ["
                    + aggregationField + "] in Model [" + aggregationModel.getName() + "].");
        }
        if (aggregationType.equals(AggregationType.CONCAT)) {
            String aggrClause = this.makeCancatAggregationColumn(aggregationTableName, aggregationColumn, groupColumnName);
            groupedSelectList.append(groupTableName)
                    .append(".")
                    .append(groupColumnName)
                    .append(" ")
                    .append(GROUPED_KEY)
                    .append(", ")
                    .append(aggrClause)
                    .append(AGGREGATED_VALUE)
                    .append(" ");
        } else {
            String aggregationFunc = AggregationType.getAggregationFunction(aggregationType);
            // 构建Select List
            // 如：r_table.master_id groupedKey, SUM(r_table.aggr_column) aggregated_value
            groupedSelectList.append(groupTableName)
                    .append(".")
                    .append(groupColumnName)
                    .append(" ")
                    .append(GROUPED_KEY)
                    .append(", ")
                    .append(aggregationFunc)
                    .append("(")
                    .append(aggregationTableName)
                    .append(".")
                    .append(aggregationColumn)
                    .append(") ")
                    .append(AGGREGATED_VALUE)
                    .append(" ");
        }
        StringBuilder groupBy = new StringBuilder(64);
        groupBy.append(groupTableName).append(".").append(groupColumnName);
        return new Tuple2<>(groupedSelectList.toString(), groupBy.toString());
    }

    private String makeCancatAggregationColumn(String tableName, String aggregationColumn, String groupColumn) {
        StringBuilder groupedSelectList = new StringBuilder(128);
        if (coreProperties.isMySql() || coreProperties.isKingbase()) {
            groupedSelectList
                    .append("GROUP_CONCAT(")
                    .append(tableName)
                    .append(".")
                    .append(aggregationColumn)
                    .append(") ");
        } else if (coreProperties.isPostgresql() || coreProperties.isOpenGauss()) {
            groupedSelectList
                    .append("STRING_AGG(")
                    .append(tableName)
                    .append(".")
                    .append(aggregationColumn)
                    .append(",',') ");
        } else if (coreProperties.isOracle() || coreProperties.isDm()) {
            groupedSelectList
                    .append("LISTAGG(")
                    .append(tableName)
                    .append(".")
                    .append(aggregationColumn)
                    .append(",',')  WITHIN GROUP (ORDER BY ")
                    .append(tableName)
                    .append(".")
                    .append(aggregationColumn)
                    .append(") ");
        } else if (coreProperties.isSqlServer()) {
            String s = "STUFF((SELECT ',' + {} FROM {} t WHERE {} = {}.{} FOR XML PATH('')),1, 1, '')";
            groupedSelectList.append(
                    StrFormatter.format(s, aggregationColumn, tableName, groupColumn, tableName, groupColumn));
        }
        return groupedSelectList.toString();
    }

    private Object doMaskFieldData(M data, Field maskField, MaskField anno) {
        Object value = ReflectUtil.getFieldValue(data, maskField);
        if (value == null) {
            return value;
        }
        if (anno.maskType().equals(MaskFieldTypeEnum.NAME)) {
            value = MaskFieldUtil.chineseName(value.toString(), anno.maskChar());
        } else if (anno.maskType().equals(MaskFieldTypeEnum.MOBILE_PHONE)) {
            value = MaskFieldUtil.mobilePhone(value.toString(), anno.maskChar());
        } else if (anno.maskType().equals(MaskFieldTypeEnum.FIXED_PHONE)) {
            value = MaskFieldUtil.fixedPhone(value.toString(), anno.maskChar());
        } else if (anno.maskType().equals(MaskFieldTypeEnum.EMAIL)) {
            value = MaskFieldUtil.email(value.toString(), anno.maskChar());
        } else if (anno.maskType().equals(MaskFieldTypeEnum.ID_CARD)) {
            value = MaskFieldUtil.idCardNum(value.toString(), anno.noMaskPrefix(), anno.noMaskSuffix(), anno.maskChar());
        } else if (anno.maskType().equals(MaskFieldTypeEnum.BANK_CARD)) {
            value = MaskFieldUtil.bankCard(value.toString(), anno.maskChar());
        } else if (anno.maskType().equals(MaskFieldTypeEnum.CAR_LICENSE)) {
            value = MaskFieldUtil.carLicense(value.toString(), anno.maskChar());
        } else if (anno.maskType().equals(MaskFieldTypeEnum.CUSTOM)) {
            MaskFieldHandler handler =
                    maskFieldHandlerMap.computeIfAbsent(anno.handler(), ApplicationContextHolder::getBean);
            value = handler.handleMask(modelClass.getSimpleName(), maskField.getName(), value.toString(), anno.maskChar());
        }
        return value;
    }

    private void compareAndSetMaskFieldData(M data, K id) {
        if (CollUtil.isNotEmpty(maskFieldList)) {
            M originalData = this.getById(id);
            this.compareAndSetMaskFieldData(data, originalData);
        }
    }
    
    protected List<ColumnEncodedRule> calculateAutoCodeField() {
        List<ColumnEncodedRule> rules = new LinkedList<>();
        for (Field autoCodeField : autoCodeFieldList) {
            AutoCodeField anno = autoCodeField.getAnnotation(AutoCodeField.class);
            ColumnEncodedRule rule = new ColumnEncodedRule();
            rule.setFieldName(autoCodeField.getName());
            rule.setPrefix(anno.prefix());
            rule.setPrecisionTo(anno.precisionTo());
            rule.setMiddle(anno.middle());
            rule.setIdWidth(anno.idWidth());
            rules.add(rule);
        }
        return rules;
    }

    protected void setAutoCodeFieldWithOriginalData(M data, K id) {
        M originalData = this.getById(id);
        this.setAutoCodeFieldWithOriginalValue(data, originalData);
    }

    protected void setAutoCodeFieldWithOriginalValue(M data, M originalData) {
        for (Field autoCodeField : autoCodeFieldList) {
            Object originalValue = ReflectUtil.getFieldValue(originalData, autoCodeField);
            ReflectUtil.setFieldValue(data, autoCodeField, originalValue);
        }
    }

    private String getNormalizedSlaveServiceName(String slaveServiceName, Class<?> slaveModelClass) {
        if (StrUtil.isBlank(slaveServiceName)) {
            slaveServiceName = slaveModelClass.getSimpleName() + "Service";
        }
        return StrUtil.lowerFirst(slaveServiceName);
    }

    @Data
    public static class RelationStruct {
        private Field relationField;
        private Field masterIdField;
        private Field equalOneToOneRelationField;
        private Method globalDictMethd;
        private BaseService<Object, Serializable> service;
        private BaseDaoMapper<Object> manyToManyMapper;
        private Map<Object, String> dictMap;
        private RelationConstDict relationConstDict;
        private RelationGlobalDict relationGlobalDict;
        private RelationDict relationDict;
        private RelationOneToOne relationOneToOne;
        private RelationOneToMany relationOneToMany;
        private RelationManyToMany relationManyToMany;
        private RelationOneToManyAggregation relationOneToManyAggregation;
        private RelationManyToManyAggregation relationManyToManyAggregation;
    }
}