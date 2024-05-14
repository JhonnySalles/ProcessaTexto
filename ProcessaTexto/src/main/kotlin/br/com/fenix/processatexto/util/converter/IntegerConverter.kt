package br.com.fenix.processatexto.util.converter

import javafx.util.StringConverter


class IntegerConverter : StringConverter<Int?>() {

    override fun toString(integer: Int?): String? {
        return integer?.toString()
    }

    override fun fromString(string: String?): Int? {
        return if (string == null || string.isEmpty()) null else string.toInt()
    }
}