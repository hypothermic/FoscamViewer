package nl.hypothermic.fscviewer.core;

import java.io.File;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nl.hypothermic.fscviewer.FoscamViewer;
import nl.hypothermic.fscviewer.ui.IController;

/*******************************\
 * > StageManager.java       < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class StageManager {
	
	private static Stage xs;
	
	/**
	 * Initialize the StageManager.
	 * @param ns Main stage
	 */
	public static void initialize(Stage ns) {
		if (xs == null) {
			xs = ns;
		} else {
			throw new RuntimeException("Invalid attempt to re-initialize StageManager");
		}
	}
	
	/**
	 * Get the root element of the stage.
	 * @return Root AnchorPane
	 */
	public static AnchorPane getRoot() {
		return (AnchorPane) xs.getScene().getRoot();
	}
	
	/**
	 * Set title of the stage
	 * @param title New title
	 */
	public static void setTitle(final String title) {
		xs.setTitle(title);
	}
	
	/**
	 * Set icon of the stage
	 * @param img New icon
	 */
	public static void setIcon(final Image img) {
		xs.getIcons().add(img);
	}
	
	/**
	 * Set icon of the stage
	 * @param url Resource locator to new icon
	 */
	public static void setIcon(final String url) {
		xs.getIcons().add(new Image(url));
	}
	
	/**
	 * Minimize the stage to the taskbar
	 */
	public static void minimize() {
		xs.setIconified(true);
	}
	
	/**
	 * Deminimize the stage from the taskbar
	 */
	// From taskbar to visible
	public static void deminimize() {
		xs.setIconified(false);
	}
	
	/**
	 * Check if the stage is in the taskbar
	 * @return
	 */
	public static boolean isMinimized() {
		return xs.isIconified();
	}
	
	/**
	 * Set the stage to full screen
	 */
	public static void fullscreen() {
		xs.setFullScreen(true);
	}
	
	
	/**
	 * Revert from full screen to normal
	 */
	public static void defullscreen() {
		xs.setFullScreen(false);
	}
	
	
	/**
	 * Check if stage is fullscreen
	 * @return True if fullscreen, false if not
	 */
	public static boolean isFullscreen() {
		return xs.isFullScreen();
	}

	private static Boolean isMaximized = false;
	
	/**
	 * Maximize the stage
	 */
	public static void maximize() {
		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();

		xs.setX(bounds.getMinX());
		xs.setY(bounds.getMinY());
		xs.setWidth(bounds.getWidth());
		xs.setHeight(bounds.getHeight());
		isMaximized = true;
	}
	
	/**
	 * Demaximize the stage
	 */
	public static void demaximize() {
		xs.setWidth(1080);
		xs.setHeight(620);
		isMaximized = false;
	}
	
	/**
	 * Check if the stage is maximized
	 * @return True if maximized, false if not
	 */
	public static Boolean isMaximized() {
		return isMaximized;
	}
	
	// adapted from: https://stackoverflow.com/a/13460743/9107324
	private static double xOffset;
	private static double yOffset;
	
	/* (non-Javadoc)
     * 
     * Used for internal purposes only.
     * @see InterfaceController.java:72
     */
	public static void onWindowMoveStart(MouseEvent event) {
		xOffset = event.getSceneX();
        yOffset = event.getSceneY();
	}
	
	/* (non-Javadoc)
     * 
     * Used for internal purposes only.
     * @see InterfaceController.java:72
     */
	public static void onWindowMoveStop(MouseEvent event) {
		xs.setX(event.getScreenX() - xOffset);
        xs.setY(event.getScreenY() - yOffset);
	}
	
	/**
	 * Show a file chooser save dialog
	 * @param fc FileChooser instance
	 * @return File that has been chosen
	 */
	public static File filechooserSave(FileChooser fc) {
		return fc.showSaveDialog(xs);
	}
}
