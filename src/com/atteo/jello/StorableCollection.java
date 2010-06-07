package com.atteo.jello;

import java.util.ArrayList;

import android.util.Log;

import com.atteo.jello.index.Index;
import com.atteo.jello.klass.KlassManager;
import com.google.inject.Inject;

public class StorableCollection<T extends Storable> {
	private String orderBy = null;
	private Expression where = null;
	private Class<T> klass;
	private ArrayList<T> arrayList = null;
	
	@Inject
	static private KlassManager klassManager;

	public StorableCollection(Class<T> klass) {
		this.klass = klass;
	}

	public StorableCollection<T> where(Expression expression) {
		arrayList = null;
		if (where == null)
			where = expression;
		else
			where = new Expression(where, Expression.OPERATOR_AND, expression);
		return this;
	}

	public StorableCollection<T> orderBy(String orderBy) {
		arrayList = null;
		this.orderBy = orderBy;
		return this;
	}

	public T getFirst() {
		ArrayList<T> list = toArrayList();
		if (list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public int getCount() {
		return toArrayList().size();
	}

	public <S extends Storable> StorableCollection<S> children(Class<S> klass,
			String field) {
		StorableCollection<S> result = new StorableCollection<S>(klass);
		result.where(new Expression(field, Expression.OPERATOR_IN, this));
		return result;
	}

	public void createArrayList() {
		arrayList = new ArrayList<T>();
		String klassName = klass.getCanonicalName();

		Index index = klassManager.getIndexFor(klassName);
		
		index.iterate();
		int nextId = index.nextId();
		
		while(nextId != -1){
			T storable = StorableFactory.createStorable(klass);
			storable.setId(nextId);
			storable.load();

			if (where == null)
				arrayList.add(storable);
			else {
				if (where.evaluate(storable))
					arrayList.add(storable);

			}
			nextId = index.nextId();
		}
	}
	
	public ArrayList<T> toArrayList() {
		if (arrayList == null)
			createArrayList();
		return arrayList;
	}
}
