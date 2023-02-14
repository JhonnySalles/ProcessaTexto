package org.jisho.textosJapones.components;

import java.util.function.UnaryOperator;

import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;

public class LongTextFieldTreeTableCell<S> extends TreeTableCell<S, Long> {
	
		public static <S> Callback<TreeTableColumn<S, Long>, TreeTableCell<S, Long>> forTreeTableColumn() {
	        return forTreeTableColumn(new NumberStringConverter());
	    }
	
	    public static <S> Callback<TreeTableColumn<S, Long>, TreeTableCell<S, Long>> forTreeTableColumn(
	            final NumberStringConverter converter) {
	        return list -> new LongTextFieldTreeTableCell<S>(converter);
	    }


	    /***************************************************************************
	     * * Fields * *
	     **************************************************************************/

	    private HBox hBox = new HBox();
	    private Label currencyLabel = new Label("");
	    private TextField textField = new TextField("" + 0l);


	    /***************************************************************************
	     * * Constructors * *
	     **************************************************************************/

	    public LongTextFieldTreeTableCell() {
	        this(new NumberToStringConverter());
	    }

	    private LongTextFieldTreeTableCell(NumberStringConverter converter) {
	        this.getStyleClass().add("currency-text-field-tree-table-cell");
	        this.converter = converter;
	        setupTextField();
	        setupHBox();
	        setStyle("-fx-alignment: CENTER-RIGHT;");
	    }

	    /***************************************************************************
	     * * Properties * *
	     **************************************************************************/

	    private NumberStringConverter converter = new NumberStringConverter();

	    /** {@inheritDoc} */
	    @Override
	    public void startEdit() {
	        if (!isEditable() || !getTreeTableView().isEditable() || !getTableColumn().isEditable()) {
	            return;
	        }
	        super.startEdit();

	        if (isEditing()) {
	            this.setText(null);

	            if (hBox != null) {
	                this.setGraphic(hBox);
	            } else {
	                this.setGraphic(textField);
	            }
	            if (textField != null) {
	                textField.setText(getItemText());
	                textField.selectAll();
	                // requesting focus so that key input can immediately go into the
	                // TextField (see RT-28132)
	                textField.requestFocus();
	            }
	        }
	    }

	    /** {@inheritDoc} */
	    @Override
	    public void cancelEdit() {
	        super.cancelEdit();
	        this.setText(getItemText());
	        this.setGraphic(currencyLabel);
	        contentDisplayProperty().setValue(ContentDisplay.RIGHT);
	    }

	    /** {@inheritDoc} */
	    @Override
	    public void updateItem(Long item, boolean empty) {
	        super.updateItem(item, empty);
	        if (isEmpty()) {
	            setText(null);
	            setGraphic(null);
	        } else {
	            if (isEditing()) {
	                if (textField != null) {
	                    textField.setText(getItemText());
	                }
	                setText(null);
	                setGraphic(hBox);
	            } else {
	                setText(getItemText());
	                setGraphic(currencyLabel);
	                contentDisplayProperty().setValue(ContentDisplay.RIGHT);
	            }
	        }
	    }
	    
	    // This will filter the changes
	    public static UnaryOperator<TextFormatter.Change> createFilter() {
	        //this is a simple Regex to define the acceptable Chars
	        String validEditingStateRegex = "[0123456789,.-]*";
	        return change -> {
	            String text = change.getText();
	            //Check if something changed and just return if not
	            if (!change.isContentChange()) {
	                return change;
	            }
	            //check if the changed text validates against the regex
	            if (text.matches(validEditingStateRegex) || text.isEmpty()) {
	                //if valid return the change
	                return change;
	            }
	            //otherwise return null
	            return null;
	        };
	    }

	    private void setupTextField() {
	        TextFormatter<Number> textFormatter = new TextFormatter<>(createFilter());
	        this.textField.setTextFormatter(textFormatter);
	        // Use onAction here rather than onKeyReleased (with check for Enter),
	        // as otherwise we encounter RT-34685
	        this.textField.setOnAction(event -> {
	            if (converter == null) {
	                throw new IllegalStateException("Attempting to convert text input into Object, but provided "
	                        + "StringConverter is null. Be sure to set a StringConverter " + "in your cell factory.");
	            }
	            if (textField.getText() != null)
	            	commitEdit(converter.fromString(textField.getText()).longValue());
	            event.consume();
	        });
	        this.textField.setOnKeyReleased(t -> {
	            if (t.getCode() == KeyCode.ESCAPE) {
	                cancelEdit();
	                t.consume();
	            }
	        });
	    }

	    private void setupHBox() {
	        this.hBox.getChildren().add(this.textField);
	        this.hBox.getChildren().add(new Label(" €"));
	        this.hBox.setPadding(new Insets(this.hBox.getPadding().getTop() + 9.0D, this.hBox.getPadding().getRight(), this.hBox.getPadding().getBottom(), this.hBox.getPadding().getLeft()));
	    }

	    private String getItemText() {
	        if(converter == null) {
	            return getItem() == null ? "" : getItem().toString();
	        } else {
	            return converter.toString(getItem());
	        }
	    }

}
