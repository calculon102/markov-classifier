package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.util.Callback;

public final class AnalysisTreeTableIndexColCellFactory implements Callback<TreeTableColumn.CellDataFeatures<AnalysisResultBean, String>, ObservableValue<String>> {

	private static final SimpleStringProperty EMPTY = new SimpleStringProperty();

	@Override
	public ObservableValue<String> call(final CellDataFeatures<AnalysisResultBean, String> param) {
		if (param.getValue().getValue().isGroup()) {
			final String index = String.valueOf(param.getTreeTableView().getRoot().getChildren().indexOf(param.getValue()) + 1);
			return new SimpleStringProperty(index);
		}
		//
		return EMPTY;
	}

}
