package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.util.Callback;

public final class TaskProgressBarTableCellFactory implements Callback<TableColumn<AbstractTask<?>, Double>, TableCell<AbstractTask<?>, Double>> {
	@Override
	public TableCell<AbstractTask<?>, Double> call(final TableColumn<AbstractTask<?>, Double> param) {
		return new ProgressBarTableCell<>();
	}
}
