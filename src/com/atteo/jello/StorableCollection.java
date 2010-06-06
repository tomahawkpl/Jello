package com.atteo.jello;

import java.util.ArrayList;

public class StorableCollection<T extends Storable> {
	public static final int OPERATOR_GREATER = 0;
	public static final int OPERATOR_LOWER = 1;
	public static final int OPERATOR_GREATER_EQUAL = 2;
	public static final int OPERATOR_LOWER_EQUAL = 3;
	public static final int OPERATOR_EQUAL = 4;
	public static final int OPERATOR_NOT_EQUAL = 5;
	
	private String orderBy;
	private Expression where;
	
	@SuppressWarnings("unchecked")
	public StorableCollection() {
	}
	
	public <S> StorableCollection<T> where(String field, int operator, S value) {
		return this;
	}
	
	public StorableCollection<T> orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}
	
	public T getFirst() {
		return null;
	}
	
	public int getCount() {
		return 0;
	}
	
	public void iterate() {
		
	}
	
	public T next() {
		return null;
	}
	
	public ArrayList<T> toArrayList() {
		return null;
	}
	
	private class Expression {
		public Expression(String field, int operator, String value) {
			this.field = field;
			this.operator = operator;
			this.value = value;
		}
		
		
		String field;
		int operator;
		String value;
	}
}
