package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.about;

import javafx.application.HostServices;
import javafx.event.ActionEvent;

/**
 * Controller for the about-dialog.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class AboutController {
	/** HostServices of parent application. Must be set before any action! */
	private HostServices hostServices;

	/**
	 * The link to the chair of communication network is clicked. Opens the corresponding website.
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void onClickChairLink(final ActionEvent event) {
		hostServices.showDocument("http://www.fernuni-hagen.de/kn/en/");
	}

	/**
	 * @param hostServices HostServices of parent application. Must be set before any action!
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void setHostServices(final HostServices hostServices) {
		this.hostServices = hostServices;
	}
}
