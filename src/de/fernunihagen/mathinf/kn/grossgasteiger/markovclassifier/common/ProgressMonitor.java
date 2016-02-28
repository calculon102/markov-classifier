package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common;

import java.util.ResourceBundle;

import javafx.scene.control.ProgressBar;

/**
 * Simple interface to give into model-class-methods to communicate progress to other components. Reason is to abstract away from JavaFX.
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public interface ProgressMonitor {
	/** Value for progress indicating that the progress is indeterminate. */
	public static final double INDETERMINATE_PROGRESS = ProgressBar.INDETERMINATE_PROGRESS;

	/**
	 * Updates the <code>workDone</code>, <code>totalWork</code>, and <code>progress</code> properties. Calls to updateProgress are coalesced and run later on
	 * the FX application thread, and calls to updateProgress, even from the FX Application thread, may not necessarily result in immediate updates to these
	 * properties, and intermediate workDone values may be coalesced to save on event notifications. <code>max</code> becomes the new value for
	 * <code>totalWork</code>.
	 * <p>
	 * <em>This method is safe to be called from any thread.</em>
	 * </p>
	 *
	 * @param progress A value from Double.MIN_VALUE up to max. If the value is greater than max, then it will be clamped at max. If the value passed is
	 *        negative, or Infinity, or NaN, then the resulting percentDone will be -1 (thus, indeterminate).
	 * @param max A value from Double.MIN_VALUE to Double.MAX_VALUE. Infinity and NaN are treated as -1.
	 */
	void update(double progress, double max);

	/**
	 * Updates the <code>message</code> property. Calls to updateMessage are coalesced and run later on the FX application thread, so calls to updateMessage,
	 * even from the FX Application thread, may not necessarily result in immediate updates to this property, and intermediate message values may be coalesced
	 * to save on event notifications.
	 * <p>
	 * <em>This method is safe to be called from any thread.</em>
	 * </p>
	 *
	 * @param message the new message
	 */
	void update(final String message);

	/**
	 * @return <code>true</code> if the depending task was cancelled. <code>false</code> otherwise.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	boolean isCancelled();

	/**
	 * @return Locale-specific-labels.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	ResourceBundle labels();
}
