package nl.hypothermic.fscviewer.ui.dynamic;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import nl.hypothermic.fscviewer.core.StageManager;

/*******************************\
 * > DoubleDialog.java       < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class DoubleDialog implements Initializable {
	
	private List<IDoubleDialogListener> listeners = new ArrayList<IDoubleDialogListener>();

    public void addListener(IDoubleDialogListener listener) {
        listeners.add(listener);
    }
	
	public DoubleDialog(String title, String message, String propMsg, String valMsg) throws IOException {
		this.xtitle = title; this.xmessage = message; this.xpropMsg = propMsg; this.xvalMsg = valMsg;
		FXMLLoader fx = new FXMLLoader(getClass().getResource("DoubleDialog.fxml"));
		fx.setController(this);
		StageManager.getRoot().getChildren().add((BorderPane) fx.load());
	}
	
	private String xtitle, xmessage, xpropMsg, xvalMsg;
	
	@FXML private BorderPane background;
	@FXML private Text title;
	@FXML private Text message;
	@FXML private TextField prop;
	@FXML private TextField val;
	
	@FXML private void onCloseRequested() {
		prop.setStyle(null);
		val.setStyle(null);
		if (prop.getText().trim().isEmpty()) {
			prop.setStyle("-fx-border-color: #ff1a00; -fx-border-width: 1.2px; -fx-border-radius: 0.15em;");
			return;
		}
		if (val.getText().trim().isEmpty()) {
			val.setStyle("-fx-border-color: #ff1a00; -fx-border-width: 1.2px; -fx-border-radius: 0.15em;");
			return;
		}
		this.onClose();
	}
	
	@FXML private void onClose() {
		background.setVisible(false);
		this.onClosed();
	}
	
	@FXML private void onClosed() {
		for (IDoubleDialogListener l : listeners) {
			l.onCompleted(new String[] {prop.getText(), val.getText()});
		}
	}
	
	@FXML private void onCancelRequested() {
		this.onCancel();
	}
	
	@FXML private void onCancel() {
		background.setVisible(false);
		this.onCancelled();
	}
	
	@FXML private void onCancelled() {
		for (IDoubleDialogListener l : listeners) {
			l.onCancelled();
		}
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		title.setText(xtitle);
		message.setText(xmessage);
		prop.setPromptText(xpropMsg);
		val.setPromptText(xvalMsg);
		prop.setFocusTraversable(false);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				prop.requestFocus();
			}
		});
	}
}
