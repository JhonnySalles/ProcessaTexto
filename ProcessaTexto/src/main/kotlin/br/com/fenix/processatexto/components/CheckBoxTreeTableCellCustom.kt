package br.com.fenix.processatexto.components

import com.jfoenix.controls.JFXCheckBox
import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.TreeTableCell
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.cell.CheckBoxTreeTableCell
import javafx.util.Callback
import javafx.util.StringConverter

// Reimplementado a classe apenas para trocar o checkbox pelo JFoenix.
open class CheckBoxTreeTableCellCustom<S, T> @JvmOverloads constructor(getSelectedProperty: Callback<Int, ObservableValue<Boolean>>? = null, converter: StringConverter<T>? = null) :
    TreeTableCell<S, T>() {

    private val checkBox: JFXCheckBox
    private var showLabel = false
    private var booleanProperty: ObservableValue<Boolean>? = null

    private val converter: ObjectProperty<StringConverter<T>?> = object : SimpleObjectProperty<StringConverter<T>?>(this, "converter") {
        override fun invalidated() {
            updateShowLabel()
        }
    }

    fun converterProperty(): ObjectProperty<StringConverter<T>?> {
        return converter
    }

    fun setConverter(value: StringConverter<T>?) {
        converterProperty().set(value)
    }

    fun getConverter(): StringConverter<T>? {
        return converterProperty().get()
    }

    private val selectedStateCallback: ObjectProperty<Callback<Int, ObservableValue<Boolean>>?> = SimpleObjectProperty<Callback<Int, ObservableValue<Boolean>>?>(
        this, "selectedStateCallback"
    )

    init {
        this.getStyleClass().add("check-box-tree-table-cell")
        checkBox = JFXCheckBox()
        setGraphic(null)
        setSelectedStateCallback(getSelectedProperty)
        setConverter(converter)
    }

    fun selectedStateCallbackProperty(): ObjectProperty<Callback<Int, ObservableValue<Boolean>>?> {
        return selectedStateCallback
    }

    fun setSelectedStateCallback(value: Callback<Int, ObservableValue<Boolean>>?) {
        selectedStateCallbackProperty().set(value)
    }

    fun getSelectedStateCallback(): Callback<Int, ObservableValue<Boolean>>? {
        return selectedStateCallbackProperty().get()
    }

    @SuppressWarnings("unchecked")
    @Override
    override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            setText(null)
            setGraphic(null)
        } else {
            val c = getConverter()
            if (showLabel && c != null) {
                setText(c.toString(item))
            }
            setGraphic(checkBox)
            if (booleanProperty is BooleanProperty) {
                checkBox.selectedProperty().unbindBidirectional(booleanProperty as BooleanProperty)
            }
            val obsValue: ObservableValue<*>? = selectedProperty
            if (obsValue is BooleanProperty) {
                booleanProperty = obsValue as ObservableValue<Boolean>
                checkBox.selectedProperty().bindBidirectional(booleanProperty as BooleanProperty)
            }
            checkBox.disableProperty().bind(
                Bindings.not(
                    getTreeTableView().editableProperty().and(
                        getTableColumn().editableProperty()
                    ).and(
                        editableProperty()
                    )
                )
            )
        }
    }

    private fun updateShowLabel() {
        showLabel = converter != null
        checkBox.setAlignment(if (showLabel) Pos.CENTER_LEFT else Pos.CENTER)
    }

    private val selectedProperty: ObservableValue<*>? get() = if (getSelectedStateCallback() != null) getSelectedStateCallback()!!.call(index) else tableColumn.getCellObservableValue(index)

    companion object {
        fun <S> forTreeTableColumn(
            column: TreeTableColumn<S, Boolean>,
        ): Callback<TreeTableColumn<S, Boolean>, TreeTableCell<S, Boolean>> {
            return forTreeTableColumn(null, null)
        }

        fun <S, T> forTreeTableColumn(
            getSelectedProperty: Callback<Int, ObservableValue<Boolean>>,
        ): Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> {
            return forTreeTableColumn(getSelectedProperty, null)
        }

        fun <S, T> forTreeTableColumn(
            getSelectedProperty: Callback<Int, ObservableValue<Boolean>>?,
            converter: StringConverter<T>?,
        ): Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> {
            return Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> { list -> CheckBoxTreeTableCell(getSelectedProperty, converter) }
        }
    }
}