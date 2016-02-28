package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableColumn;

/**
 * Default TreeTable-Column, but with a tooltip.
 * @param <S>
 * @param <T>
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class TooltippedTreeTableColumn<S, T> extends TreeTableColumn<S, T> {
	private final Label columnLabel = new Label();

	public TooltippedTreeTableColumn()
	{
		super();
		//
		this.setText("Test");
		this.setTooltip("Test");
		setGraphic(columnLabel);
	}

	public String getTitle() {
		return columnLabel.getText();
	}

	public void setTitle(final String text) {
		columnLabel.setText(text);
	}

	public String getTooltip() {
		return columnLabel.getTooltip() == null ? "" : columnLabel.getTooltip().getText();
	}

	public void setTooltip(final String text) {
		columnLabel.setTooltip(new Tooltip(text));
	}
}
