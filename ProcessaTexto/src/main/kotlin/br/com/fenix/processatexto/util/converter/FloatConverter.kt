package br.com.fenix.processatexto.util.converter

import javafx.util.StringConverter


class FloatConverter : StringConverter<Float?>() {

    override fun toString(float: Float?): String? {
        return float?.toString()
    }

    override fun fromString(string: String?): Float? {
        return if (string == null || string.isEmpty()) null else string.toFloat()
    }
}