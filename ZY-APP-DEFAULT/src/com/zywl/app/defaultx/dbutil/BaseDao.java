package com.zywl.app.defaultx.dbutil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface BaseDao {
	/**
	 * 查询全部数据
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId 配置SQLID
	 * @return
	 */
	<E> List<E> findAll(String namespace, String statementId);

	/**
	 * 根据参数查询LIST集合
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId  配置SQLID
	 * @param parameters 查询参数
	 * @return
	 */
	<E> List<E> findList(String namespace, String statementId, Object parameters);

	/**
	 * 查询按长度限制的LIST集合
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId 配置SQLID
	 * @param parameters 查询参数
	 * @param top
	 * @return
	 */
	<E> List<E> findLimitList(String namespace, String statementId,
			Object parameters, Integer top);

	/**
	 * 查询按排序返回LIST集合
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId 配置SQLID
	 * @param parameters 查询参数
	 * @param sort
	 * @return
	 */
	<E> List<E> findSortList(String namespace, String statementId,
			Object parameters, Sort sort);

	/**
	 * 分页查找方法返回分页数据
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId 配置SQLID
	 * @param parameters 查询参数
	 * @param pageable
	 * @return
	 */
	<E> Page<E> findPage(String namespace, String statementId,
			Object parameters, Pageable pageable);

	/**
	 * 查询以MAP方式返回
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId 配置SQLID
	 * @param parameters 查询参数
	 * @param mapKey
	 * @return
	 */
	<V> Map<String, V> findMap(String namespace, String statementId,
			Object parameters, String mapKey);

	/**
	 * 查询前几条以MAP方式返回
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId 配置SQLID
	 * @param parameter 查询参数
	 * @param mapKey
	 * @param top 取前几条数据
	 * @return
	 */
	<V> Map<String, V> findMap(String namespace, String statementId,
			Object parameter, String mapKey, Integer top);

	/**
	 * 查找单个值
	 * 
	 * @param namespace
	 *            命名空间标识
	 * @param statementId
	 *            配置SQLID
	 * @param parameterObject
	 *            查询参数
	 * @return
	 */
	<T> T findOne(String namespace, Object statementId, Object parameterObject);

	BigDecimal sum(String namespace, String statementId, Object parameters);
	
	/**
	 * 取行数
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId 配置SQLID
	 * @param parameters 查询参数
	 * @return
	 */
	long count(String namespace, Object statementId, Object parameters);

	/**
	 * 执行更新操作，包括insert、update、delete
	 * 
	 * @param namespace 一般用实体类的全路径,如 User.class.getName()
	 * @param statementId insert、update、delete等语句标识
	 * @param parameters 操作所需参数对象，可能是实体对象或Map或其他类型等
	 * @return 注意: 返回的是操作影响的记录数,不是主键
	 */
	int execute(String namespace, String statementId, Object parameters);

	/***
	 * 批量插入优化方法
	 * 
	 * @param namespace 命名空间标识
	 * @param statementId 配置SQLID
	 * @param parameters 查询参数
	 */
	void batchInsert(String namespace, String statementId, List<?> parameters);

}
