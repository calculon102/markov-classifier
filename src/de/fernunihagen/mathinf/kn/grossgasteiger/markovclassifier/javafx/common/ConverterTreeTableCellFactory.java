package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * For usage within FXML. Converts an arbtitary input value via a given StringConverter.
 * This converter can be given as property.
 * @param <S>
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class ConverterTreeTableCellFactory<S> implements Callback<TreeTableColumn<S, ?>, TreeTableCell<S, ?>> {

	private final ObjectProperty<StringConverter<?>> converter = new SimpleObjectProperty<>();

	@Override
	public TreeTableCell<S, ?> call(final TreeTableColumn<S, ?> param) {
		if (converter.get() != null) {
			return new TextFieldTreeTableCell<>(converter.get());
		}
		return new TextFieldTreeTableCell<>();
	}

	public ObjectProperty<StringConverter<?>> converterProperty() {
		return converter;
	}

	public StringConverter<?> getConverter() {
		return converter.get();
	}

	public void setConverter(final StringConverter<?> newConverter) {
		converter.set(newConverter);
	}
}
