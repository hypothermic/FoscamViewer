package nl.hypothermic.fscviewer;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.bytedeco.javacpp.avutil;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import nl.hypothermic.fscviewer.core.I18N;
import nl.hypothermic.fscviewer.core.StageManager;
import nl.hypothermic.fscviewer.core.XLogger;
import nl.hypothermic.fscviewer.ui.InterfaceController;

/*******************************\
 * > FoscamViewer.java       < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class FoscamViewer extends Application {

	private Stage xs;
	private InterfaceController ic;
	private static XLogger log = new XLogger(System.out);
	
	
	@Override
	public void start(Stage xs) throws IOException {
		this.xs = xs;
		System.out.println("--- start ---");
		Thread.currentThread().setName("FSCV-UI");
		System.out.println("--- init rb ---");
		ResourceBundle i18n;
		if (prefLang != null) {
			i18n = ResourceBundle.getBundle("locale.i18n", prefLang);
		} else {
			i18n = ResourceBundle.getBundle("locale.i18n");
		}
		System.out.println("--- load UI ---");
		FXMLLoader l = new FXMLLoader(getClass().getResource("ui/Interface.fxml"), i18n);
		ic = new InterfaceController();
		StageManager.initialize(xs);
		I18N.initialize(i18n);
		l.setController(ic);
		Parent root = l.load();
	    xs.setScene(new Scene(root));
	    xs.initStyle(StageStyle.UNDECORATED);
	    System.out.println("--- show UI ---");
	    xs.show();
	    xs.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				ic.prepareShutdown();
			}
	    });
	    System.out.println("--- start done ---");
	}

	private static Locale prefLang;
	
	public static void main(String[] args) {
		System.out.println("--- main ---");
		if (args.length == 1 && !args[0].isEmpty()) {
			System.out.println("--- parsing arg lang ---");
			if (args[0].startsWith("lang=")) {
				if (args[0].length() > 5) {
					try {
						System.out.println("--- parsing arg lang done ---");
						prefLang = Locale.forLanguageTag(args[0].split("=")[1]);
					} catch (Exception x) {
						System.out.println("--- parsing arg lang fail ---");
						log.severe("Argument \"lang=\" is invalid! Ignored.");
					}
				} else {
					System.out.println("--- parsing arg lang incorrect ---");
					log.warn("Argument \"lang=\" is empty! Ignored.");
				}
			}
		}
		System.out.println("--- set av log ---");
		avutil.av_log_set_level(avutil.AV_LOG_INFO);
		System.out.println("--- launch jfx app ---");
		launch(args);
	}
}
