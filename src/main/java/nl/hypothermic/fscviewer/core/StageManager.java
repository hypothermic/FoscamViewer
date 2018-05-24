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
 * www.github.com/hypothermic  *
 *  See LICENSE.md for legal   *
\*******************************/

public class StageManager {
	
	private static Stage xs;
	
	public static void initialize(Stage ns) {
		if (xs == null) {
			xs = ns;
		} else {
			throw new RuntimeException("Invalid attempt to re-initialize StageManager");
		}
	}
	
	public static void setTitle(final String title) {
		xs.setTitle(title);
	}
	
	public static void setIcon(final Image img) {
		xs.getIcons().add(img);
	}
	
	public static void setIcon(final String url) {
		xs.getIcons().add(new Image(url));
	}
	
	public static AnchorPane getRoot() {
		return (AnchorPane) xs.getScene().getRoot();
	}
	
	// Minimize to taskbar
	public static void minimize() {
		xs.setIconified(true);
	}
	
	// From taskbar to visible
	public static void deminimize() {
		xs.setIconified(false);
	}
	
	// Is minimized?
	public static boolean isMinimized() {
		return xs.isIconified();
	}
	
	// Set fullscreen
	public static void fullscreen() {
		xs.setFullScreen(true);
	}
	
	// From fullscreen to normal
	public static void defullscreen() {
		xs.setFullScreen(false);
	}
	
	// Is fullscreen?
	public static boolean isFullscreen() {
		return xs.isFullScreen();
	}
	
	private static Boolean isMaximized = false;
	
	// Fill whole screen
	public static void maximize() {
		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();

		xs.setX(bounds.getMinX());
		xs.setY(bounds.getMinY());
		xs.setWidth(bounds.getWidth());
		xs.setHeight(bounds.getHeight());
		isMaximized = true;
	}
	
	public static void demaximize() {
		xs.setWidth(1080);
		xs.setHeight(620);
		isMaximized = false;
	}
	
	public static Boolean isMaximized() {
		return isMaximized;
	}
	
	// adapted from: https://stackoverflow.com/a/13460743/9107324
	private static double xOffset;
	private static double yOffset;
	
	public static void onWindowMoveStart(MouseEvent event) {
		xOffset = event.getSceneX();
        yOffset = event.getSceneY();
	}
	
	public static void onWindowMoveStop(MouseEvent event) {
		xs.setX(event.getScreenX() - xOffset);
        xs.setY(event.getScreenY() - yOffset);
	}
	
	/*public static void restart(Locale loc) throws IOException {
		main.restart(loc);
	}*/
	
	public static File filechooserSave(FileChooser fc) {
		return fc.showSaveDialog(xs);
	}
}
