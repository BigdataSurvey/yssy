package com.zywl.app.base.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<T> implements Set<T> {
	
	private Set<T> set;
	
	public ConcurrentHashSet() {
		set = Collections.newSetFromMap(new ConcurrentHashMap<T,Boolean>());
	}

	public int size() {
		return set.size();
	}

	public boolean isEmpty() {
		return set.isEmpty();
	}

	public boolean contains(Object o) {
		return set.contains(o);
	}

	public Iterator<T> iterator() {
		return set.iterator();
	}

	public Object[] toArray() {
		return set.toArray();
	}

	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	public boolean add(T e) {
		return set.add(e);
	}

	public boolean remove(Object o) {
		return set.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	public boolean addAll(Collection<? extends T> c) {
		return set.addAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return set.retainAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return set.removeAll(c);
	}

	public void clear() {
		set.clear();
	}
}
