package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * For usage within FXML. Converts an arbtitary input value via a given StringConverter.
 * This converter can be given as property.
 * @param <S>
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class ConverterTableCellFactory<S> implements Callback<TableColumn<S, ?>, TableCell<S, ?>> {

	private final ObjectProperty<StringConverter<?>> converter = new SimpleObjectProperty<>();

	@Override
	public TableCell<S, ?> call(final TableColumn<S, ?> param) {
		if (converter.get() != null) {
			return new TextFieldTableCell<>(converter.get());
		}
		return new TextFieldTableCell<>();
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
