package org.jisho.textosJapones.util.converter;

import javafx.util.StringConverter;

public class IntegerConverter extends StringConverter<Integer> {

	@Override
	public String toString(Integer object) {
		if (object == null)
			return null;
		return String.valueOf(object);
	}

	@Override
	public Integer fromString(String string) {
		if (string == null || string.isEmpty())
			return null;
		return Integer.valueOf(string);
	}

}
