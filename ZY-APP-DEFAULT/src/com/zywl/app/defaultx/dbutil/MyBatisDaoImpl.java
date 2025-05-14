package com.zywl.app.defaultx.dbutil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
/**
 * DOA核心实现类
 * 
 * @author mi
 * 
 */
public class MyBatisDaoImpl implements BaseDao {

	private SqlSessionTemplate sqlSession;

	@Override
	public <E> List<E> findList(String namespace, String statementId, Object parameters) {
		String statement = namespace + "." + statementId;
		return sqlSession.selectList(statement, parameters);

	}

	@Override
	public <E> List<E> findLimitList(String namespace, String statementId, Object parameters, Integer top) {
		String statement = namespace + "." + statementId;
		if (top != null) {
			RowBounds rowBounds = new RowBounds(0, top);
			return sqlSession.selectList(statement, parameters, rowBounds);
		} else {
			return sqlSession.selectList(statement, parameters);
		}
	}

	@Override
	public <E> List<E> findSortList(String namespace, String statementId, Object parameters, Sort sort) {
		String statement = namespace + "." + statementId;
		if (sort != null) {
			RowBounds rowBounds = new RowBounds(RowBounds.NO_ROW_OFFSET,
					RowBounds.NO_ROW_LIMIT);
			PaginationInterceptor.setPaginationOrderby(sort);
			return sqlSession.selectList(statement, parameters, rowBounds);
		} else {
			return sqlSession.selectList(statement, parameters);
		}
	}

	@Override
	public <E> Page<E> findPage(String namespace, String statementId, Object parameters, Pageable pageable) {
		String statement = namespace + "." + statementId;
		
		RowBounds rowBounds = new RowBounds((int) pageable.getOffset(), pageable.getPageSize());
		PaginationInterceptor.setPaginationOrderby(pageable.getSort());
		List<E> rows = sqlSession.selectList(statement, parameters, rowBounds);
		int total = PaginationInterceptor.getPaginationTotal();
		Page<E> page = new PageImpl<E>(rows, pageable, total);
		return page;
	}

	@Override
	public <V> Map<String, V> findMap(String namespace, String statementId,
			Object parameters, String mapKey, Integer top) {
		String statement = namespace + "." + statementId;
		if (top != null) {
			RowBounds rowBounds = new RowBounds(0, top);
			return sqlSession.selectMap(statement, parameters, mapKey,
					rowBounds);
		} else {
			return sqlSession.selectMap(statement, parameters, mapKey);
		}
	}

	@Override
	public <V> Map<String, V> findMap(String namespace, String statementId,
			Object parameters, String mapKey) {
		return findMap(namespace, statementId, parameters, mapKey, null);
	}

	public void setSqlSession(SqlSessionTemplate sqlSession) {
		this.sqlSession = sqlSession;
	}

	public SqlSessionTemplate getSqlSession() {
		return sqlSession;
	}

	public int execute(String namespace, String statementId, Object parameter) {
		String statement = namespace + "." + statementId;
		return sqlSession.update(statement, parameter);
	}

	@Override
	public void batchInsert(String namespace, String statementId, List<?> data) {
		String statement = namespace + "." + statementId;
		SqlSession batchSqlSession = null;
		try {
			batchSqlSession = sqlSession.getSqlSessionFactory().openSession(
					ExecutorType.BATCH, false);
			int batchCount = 500;// 每批commit的个数
			for (int index = 0; index < data.size(); index++) {
				Object t = data.get(index);
				batchSqlSession.insert(statement, t);
				if (index != 0 && index % batchCount == 0) {
					batchSqlSession.commit();
				}
			}
			batchSqlSession.commit();
		} finally {
			if (batchSqlSession != null) {
				batchSqlSession.close();
			}
		}
	}

	public void batchUpdate(String namespace, String statementId, List<?> data) {
		String statement = namespace + "." + statementId;
		SqlSession batchSqlSession = null;
		try {
			batchSqlSession = sqlSession.getSqlSessionFactory().openSession(
					ExecutorType.BATCH, false);
			int batchCount = 500;// 每批commit的个数
			for (int index = 0; index < data.size(); index++) {
				Object t = data.get(index);
				batchSqlSession.update(statement, t);
				if (index != 0 && index % batchCount == 0) {
					batchSqlSession.commit();
				}
			}
			batchSqlSession.commit();
		} finally {
			if (batchSqlSession != null) {
				batchSqlSession.close();
			}
		}
	}

	@Override
	public <T> T findOne(String namespace, Object statementId,
			Object parameterObject) {
		String statement = namespace + "." + statementId;

		return sqlSession.selectOne(statement, parameterObject);
	}

	@Override
	public BigDecimal sum(String namespace, String statementId, Object parameters) {
		String statement = namespace + "." + statementId;
		return sqlSession.selectOne(statement, parameters);
	}
	
	@Override
	public long count(String namespace, Object statementId, Object parameters) {
		String statement = namespace + "." + statementId;
		return sqlSession.selectOne(statement, parameters);
	}

	@Override
	public <E> List<E> findAll(String namespace, String statementId) {
		String statement = namespace + "." + statementId;
		return sqlSession.selectList(statement);
	}


}
