package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * TODO min-/maxlength per Listener
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class NumberTextField extends TextField {

	private final IntegerProperty defaultValue = new SimpleIntegerProperty(10);

	public NumberTextField() {
		this.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
				if (newValue.isEmpty()) {
					NumberTextField.this.textProperty().set(String.valueOf(NumberTextField.this.defaultValue.get()));
				}
			}
		});
	}

	@Override
	public void replaceText(final int start, final int end, final String text) {
		if (validate(text)) {
			super.replaceText(start, end, text);
		}

	}

	@Override
	public void replaceSelection(final String text) {
		if (validate(text)) {
			super.replaceSelection(text);
		}
	}

	private boolean validate(final String text) {
		return ("".equals(text) || text.matches("[0-9]"));
	}

	public void setDefaultValue(final int newDefaultValue) {
		if (newDefaultValue < 0) {
			return;
		}
		defaultValue.set(newDefaultValue);
	}

	public int getDefaultValue() {
		return defaultValue.get();
	}

	public int getValue() {
		return Integer.valueOf(getText());
	}

	public NumberTextField setValue(final int newValue) {
		setText(String.valueOf(newValue));
		return this;
	}
}
