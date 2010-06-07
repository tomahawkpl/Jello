package com.atteo.jello;

import java.util.ArrayList;

public class Expression {
	public static final int OPERATOR_GREATER = 0;
	public static final int OPERATOR_LOWER = 1;
	public static final int OPERATOR_GREATER_EQUAL = 2;
	public static final int OPERATOR_LOWER_EQUAL = 3;
	public static final int OPERATOR_EQUAL = 4;
	public static final int OPERATOR_NOT_EQUAL = 5;
	public static final int OPERATOR_IN = 7;
	public static final int OPERATOR_OR = 8;
	public static final int OPERATOR_AND = 9;

	public Expression(Object left, int operator, Object right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	@SuppressWarnings("unchecked")
	public boolean evaluate(Storable storable) {
		Object leftValue = left, rightValue = right;
		if (leftValue instanceof String && ((String) leftValue).startsWith("."))
			leftValue = storable.getDbFieldValue(((String) leftValue).substring(1));

		if (rightValue instanceof String && ((String) rightValue).startsWith("."))
			rightValue = storable.getDbFieldValue(((String) rightValue).substring(1));

		if (leftValue instanceof Expression)
			leftValue = ((Expression) leftValue).evaluate(storable);

		if (rightValue instanceof Expression)
			rightValue = ((Expression) rightValue).evaluate(storable);

		switch (operator) {
		case OPERATOR_EQUAL:
			return leftValue.equals(rightValue);
		case OPERATOR_NOT_EQUAL:
			return !leftValue.equals(rightValue);
		case OPERATOR_AND:
			return (Boolean) leftValue && (Boolean) rightValue;
		case OPERATOR_OR:
			return (Boolean) leftValue || (Boolean) rightValue;
		case OPERATOR_GREATER:
			if (leftValue instanceof Double)
				return (Double) leftValue > (Double) rightValue;
			if (leftValue instanceof Float)
				return (Float) leftValue > (Float) rightValue;
			if (leftValue instanceof Integer)
				return (Integer) leftValue > (Integer) rightValue;
			if (leftValue instanceof Long)
				return (Long) leftValue > (Long) rightValue;
			if (leftValue instanceof Short)
				return (Short) leftValue > (Short) rightValue;

			throw new RuntimeException("Unknown datatype");
		case OPERATOR_GREATER_EQUAL:
			if (leftValue instanceof Double)
				return (Double) leftValue >= (Double) rightValue;
			if (leftValue instanceof Float)
				return (Float) leftValue >= (Float) rightValue;
			if (leftValue instanceof Integer)
				return (Integer) leftValue >= (Integer) rightValue;
			if (leftValue instanceof Long)
				return (Long) leftValue >= (Long) rightValue;
			if (leftValue instanceof Short)
				return (Short) leftValue >= (Short) rightValue;
			throw new RuntimeException("Unknown datatype");
		case OPERATOR_LOWER:
			if (leftValue instanceof Double)
				return (Double) leftValue < (Double) rightValue;
			if (leftValue instanceof Float)
				return (Float) leftValue < (Float) rightValue;
			if (leftValue instanceof Integer)
				return (Integer) leftValue < (Integer) rightValue;
			if (leftValue instanceof Long)
				return (Long) leftValue < (Long) rightValue;
			if (leftValue instanceof Short)
				return (Short) leftValue < (Short) rightValue;
			throw new RuntimeException("Unknown datatype");
		case OPERATOR_LOWER_EQUAL:
			if (leftValue instanceof Double)
				return (Double) leftValue <= (Double) rightValue;
			if (leftValue instanceof Float)
				return (Float) leftValue <= (Float) rightValue;
			if (leftValue instanceof Integer)
				return (Integer) leftValue <= (Integer) rightValue;
			if (leftValue instanceof Long)
				return (Long) leftValue <= (Long) rightValue;
			if (leftValue instanceof Short)
				return (Short) leftValue <= (Short) rightValue;
			throw new RuntimeException("Unknown datatype");
		case OPERATOR_IN:
			ArrayList<Storable> storables = ((StorableCollection<Storable>) rightValue)
					.toArrayList();
			int l = storables.size();
			int id = (Integer) leftValue;
			for (int i = 0; i < l; i++)
				if (storables.get(i).getId() == id)
					return true;
			return false;
		}

		return false;
	}

	int operator;
	Object left;
	Object right;
}
