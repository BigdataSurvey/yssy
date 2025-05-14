package com.zywl.app.base.util;

import java.util.List;

public class ListUtils {
	
	/**
	 * 
	 * @param <E>
	 * @param list 需要分页的集合
	 * @param page 从0开始
	 * @param limit 每页数量
	 * @return 基于List.subList方法，如果对返回的集合新增删除，则原集合也会受到影响
	 */
	public static <E> List<E> limitList(List<E> list, int page, int limit){
		int fromIndex = page  * limit > list.size() ? list.size() : page  * limit;
		int toIndex = (fromIndex + limit) > list.size() ? list.size() : (fromIndex + limit);
		return list.subList(fromIndex, toIndex);
	}
}
