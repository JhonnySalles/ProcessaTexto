package br.com.fenix.processatexto.components

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.util.Callback
import javafx.util.converter.NumberStringConverter
import java.util.function.UnaryOperator

class LongTextFieldTreeTableCell<S> private constructor(converter: NumberStringConverter) : TreeTableCell<S, Long>() {
    /***************************************************************************
     * * Fields * *
     */
    private val hBox: HBox = HBox()
    private val currencyLabel: Label = Label("")
    private val textField: TextField = TextField("" + 0L)

    /***************************************************************************
     * * Constructors * *
     */
    constructor() : this(NumberToStringConverter()) {}

    /***************************************************************************
     * * Properties * *
     */
    private val converter: NumberStringConverter

    init {
        this.styleClass.add("currency-text-field-tree-table-cell")
        this.converter = converter
        setupTextField()
        setupHBox()
        style = "-fx-alignment: CENTER-RIGHT;"
    }

    /** {@inheritDoc}  */
    @Override
    override fun startEdit() {
        if (!isEditable || !treeTableView.isEditable || !tableColumn.isEditable)
            return

        super.startEdit()
        if (isEditing) {
            this.text = null
            if (hBox != null) {
                this.setGraphic(hBox)
            } else {
                this.setGraphic(textField)
            }
            if (textField != null) {
                textField.text = itemText
                textField.selectAll()
                // requesting focus so that key input can immediately go into the
                // TextField (see RT-28132)
                textField.requestFocus()
            }
        }
    }

    /** {@inheritDoc}  */
    @Override
    override fun cancelEdit() {
        super.cancelEdit()
        this.text = itemText
        this.graphic = currencyLabel
        contentDisplayProperty().setValue(ContentDisplay.RIGHT)
    }

    /** {@inheritDoc}  */
    @Override
    override fun updateItem(item: Long, empty: Boolean) {
        super.updateItem(item, empty)
        if (isEmpty) {
            text = null
            setGraphic(null)
        } else {
            if (isEditing) {
                if (textField != null)
                    textField.text = itemText

                text = null
                setGraphic(hBox)
            } else {
                text = itemText
                graphic = currencyLabel
                contentDisplayProperty().setValue(ContentDisplay.RIGHT)
            }
        }
    }

    private fun setupTextField() {
        val textFormatter: TextFormatter<Number> = TextFormatter(createFilter())
        textField.setTextFormatter(textFormatter)
        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        textField.setOnAction { event ->
            if (converter == null) {
                throw IllegalStateException(
                    "Attempting to convert text input into Object, but provided "
                            + "StringConverter is null. Be sure to set a StringConverter " + "in your cell factory."
                )
            }
            if (textField.getText() != null) commitEdit(converter.fromString(textField.getText()).toLong())
            event.consume()
        }
        textField.setOnKeyReleased { t ->
            if (t.getCode() === KeyCode.ESCAPE) {
                cancelEdit()
                t.consume()
            }
        }
    }

    private fun setupHBox() {
        hBox.getChildren().add(textField)
        hBox.getChildren().add(Label(" €"))
        hBox.setPadding(Insets(hBox.getPadding().getTop() + 9.0, hBox.getPadding().getRight(), hBox.getPadding().getBottom(), hBox.getPadding().getLeft()))
    }

    private val itemText: String
        private get() = if (converter == null) {
            if (getItem() == null) "" else getItem().toString()
        } else {
            converter.toString(getItem())
        }

    companion object {
        fun <S> forTreeTableColumn(): Callback<TreeTableColumn<S, Long>, TreeTableCell<S, Long>> {
            return forTreeTableColumn<S>(NumberStringConverter())
        }

        fun <S> forTreeTableColumn(
            converter: NumberStringConverter,
        ): Callback<TreeTableColumn<S, Long>, TreeTableCell<S, Long>> {
            return Callback<TreeTableColumn<S, Long>, TreeTableCell<S, Long>> { list -> LongTextFieldTreeTableCell<S>(converter) }
        }

        // This will filter the changes
        fun createFilter(): UnaryOperator<TextFormatter.Change?> {
            //this is a simple Regex to define the acceptable Chars
            val validEditingStateRegex = "[0123456789,.-]*".toRegex()
            return UnaryOperator { change: TextFormatter.Change? ->
                val text = change!!.text
                //Check if something changed and just return if not
                if (!change.isContentChange)
                    change
                //check if the changed text validates against the regex
                else if (text.matches(validEditingStateRegex) || text.isEmpty()) //if valid return the change
                    change
                else
                    null
            }
        }
    }
}