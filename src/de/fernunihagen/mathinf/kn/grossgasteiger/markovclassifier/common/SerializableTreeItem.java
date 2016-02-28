package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Tree-Structure for serialization... Why must I implement my own!?
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class SerializableTreeItem<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 20151129L;

	private final T value;
	private final List<SerializableTreeItem<T>> children;

	public SerializableTreeItem(final T value, final List<SerializableTreeItem<T>> children) {
		requireNonNull(value);
		requireNonNull(children);

		this.value = value;
		this.children = children;
	}

	public T value() {
		return value;
	}

	public List<SerializableTreeItem<T>> children() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Recursive factory method for converting a JavaFx-TreeItem-Strucutre to a serializable form.
	 * @param root TreeItem-Root to convert from.
	 * @return Serializable TreeItem-structure.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static <S extends Serializable> SerializableTreeItem<S> ofTreeItem(final TreeItem<S> root) {
		final S value = root.getValue();
		final ObservableList<TreeItem<S>> rootChildren = root.getChildren();
		final List<SerializableTreeItem<S>> children = new ArrayList<>(rootChildren.size());

		for (final TreeItem<S> treeItem : rootChildren) {
			children.add(ofTreeItem(treeItem));
		}

		return new SerializableTreeItem<S>(value, children);
	}

	/**
	 * Converts a given SerializableTreeItem back to JavaFx-TreeItems in recursion.
	 * @param root Base TreeItem.
	 * @return TreeItem-list to use in a treetable
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static <S extends Serializable> TreeItem<S> toTreeItem(final SerializableTreeItem<S> root) {
		final S value = root.value();
		final List<SerializableTreeItem<S>> children = root.children();

		final TreeItem<S> treeItem = new TreeItem<S>(value);

		for (final SerializableTreeItem<S> serializableTreeItem : children) {
			treeItem.getChildren().add(toTreeItem(serializableTreeItem));
		}

		return treeItem;
	}
}
