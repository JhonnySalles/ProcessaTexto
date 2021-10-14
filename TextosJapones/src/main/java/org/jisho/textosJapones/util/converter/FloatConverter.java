package org.jisho.textosJapones.util.converter;

import javafx.util.StringConverter;

public class FloatConverter extends StringConverter<Float> {

	@Override
	public String toString(Float object) {
		if (object == null)
			return null;
		return String.valueOf(object);
	}

	@Override
	public Float fromString(String string) {
		if (string == null || string.isEmpty())
			return null;
		return Float.valueOf(string);
	}

}
