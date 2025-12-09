package com.zywl.app.defaultx.dbutil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Options;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.service.BaseService;

/**
 * 抽象Dao父类
 * @author Doe.
 *
 */
public abstract class DaoService extends BaseService {

	protected String mapperSpace;
	
	@Resource(name = "myBatisDao")
	public BaseDao baseDao;
	
	public DaoService(String mapper){
		this.mapperSpace = "com.zywl.app.defaultx.mapper." + mapper;
	} 

	public BaseDao getBaseDao() {
		return baseDao;
	}

	/**
	 * 分页查询
	 * 
	 * @param <E>
	 * @param params 参询参数
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly = true)
	public <E>Page<E> findPage(Map<String,String> params, Pageable pageable) {
		return getBaseDao().findPage(mapperSpace, "findPage", params, pageable);
	}

	/**
	 * 集合查询
	 * 
	 * @param statementId
	 * @param params
	 * @return
	 */
	@Transactional(readOnly = true)
	public <E> List<E> findList(String statementId, Object params) {
		return getBaseDao().findList(mapperSpace, statementId, params);
	}

	/**
	 * 根据条件查询
	 * @author Doe.
	 * @param params
	 * @return
	 */
	@Transactional(readOnly = true)
	public <E> List<E> findByConditions(Object params){
		return getBaseDao().findList(mapperSpace, "findByConditions", params);
	}
	
	/**
	 * 单记录查询
	 * @param <E>
	 * 
	 * @param <T>
	 * @param params 参询参数
	 * @return
	 */
	@Transactional(readOnly = true)
	public <E> E findOne(Object params) {
		return getBaseDao().findOne(mapperSpace, "findOne", params);
	}

	/**
	 * 单记录查询
	 * 
	 * @param <T>
	 * @param params 参询参数
	 * @return
	 */
	@Transactional(readOnly = true)
	public Object findOne(Object statementId, Object params) {
		return getBaseDao().findOne(mapperSpace, statementId, params);

	}

	/**
	 * 保存作操
	 *
	 * @param params sql参数
	 */
	public int save(Object params) {
		try {
			return getBaseDao().execute(mapperSpace, "insert", params);
		} catch (Exception ex) {
			logger().error("Error mybatis save", ex);
			throw new AppException("保存失败", ex);
		}
	}

	/**
	 * 修改操作
	 *
	 * @param params sql参数
	 */
	public int update(Object params) {
		try {
			return getBaseDao().execute(mapperSpace, "update", params);
		} catch (Exception ex) {
			logger().error("Error mybatis update", ex);
			throw new AppException("更新失败", ex);
		}
	}

	/**
	 * 删除操作
	 * 
	 * @param params sql参数
	 */
	public int delete(Object params) {
		try {
			return getBaseDao().execute(mapperSpace, "delete", params);
		} catch (Exception ex) {
			logger().error("Error mybatis delete", ex);
			throw new AppException("删除失败", ex);
		}
	}

	/**
	 * 执行sql语句
	 * 
	 * @param statementId sqlId名称
	 * @param params 参数
	 */
	public int execute(String statementId, Object params) {
		try {
			return getBaseDao().execute(mapperSpace, statementId, params);
		} catch (Exception ex) {
			logger().error("Error mybatis execute", ex);
			throw new AppException("errorcode：001", ex);
		}
	}
	
    /**
     * 批量插入方法
     * @param statementId
     * @param params
     */
	public void batchInsert(String statementId, List<?> params) {
		try {
			getBaseDao().batchInsert(mapperSpace, statementId, params);
		} catch (Exception ex) {
			logger().error("Error mybatis batchInsert", ex);
			throw new AppException("批量更新失败", ex);
		}
	}

	public void insert(Object parameter){
		try {
			batchInsert("insert", Lists.newArrayList(parameter));
		} catch (Exception e) {
			logger().error("Error mybatis Insert", e);
			throw new AppException("插入失败", e);
		}
	}

	@Transactional(readOnly = true)
	public BigDecimal sum(String statementId, Object params) {
		return getBaseDao().sum(mapperSpace, statementId, params);
	}
	
	/**
	 * 查询行数
	 * @param statementId sqlId名称
	 * @param params 参数
	 * @return
	 */
	@Transactional(readOnly = true)
	public long count(String statementId, Object params) {
		return getBaseDao().count(mapperSpace, statementId, params);
	}
	
	/**
	 * 查询全部
	 * @return
	 */
	@Transactional(readOnly = true)
	public <E> List<E> findAll(){
		return getBaseDao().findAll(mapperSpace, "findAll");
	}
}
