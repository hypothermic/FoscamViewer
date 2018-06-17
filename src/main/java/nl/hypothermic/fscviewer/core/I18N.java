package nl.hypothermic.fscviewer.core;

import java.util.ResourceBundle;

import javafx.stage.Stage;
import nl.hypothermic.fscviewer.FoscamViewer;
import nl.hypothermic.fscviewer.ui.IController;

/*******************************\
 * > I18N.java            < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class I18N {

    private static ResourceBundle bundle;

    public static void initialize(ResourceBundle newbundle) {
    	if (bundle == null) {
    		bundle = newbundle;
    	} else {
    		throw new RuntimeException("Invalid attempt to re-initialize internationalization core");
    	}
    }

    public static String getString(String key) {
    	return bundle.getString(key);
    }
}
