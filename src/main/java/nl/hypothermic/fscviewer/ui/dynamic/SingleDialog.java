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
 * > SingleDialog.java       < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class SingleDialog implements Initializable {
	
	private List<ISingleDialogListener> listeners = new ArrayList<ISingleDialogListener>();

    public void addListener(ISingleDialogListener listener) {
        listeners.add(listener);
    }
    
    public SingleDialog(String title, String message, String propMsg) throws IOException {
		this.xtitle = title; this.xmessage = message; this.xpropMsg = propMsg;
		FXMLLoader fx = new FXMLLoader(getClass().getResource("SingleDialog.fxml"));
		fx.setController(this);
		StageManager.getRoot().getChildren().add((BorderPane) fx.load());
	}
	
	public SingleDialog(String title, String message, String propMsg, ISingleDialogListener listener) throws IOException {
		this.xtitle = title; this.xmessage = message; this.xpropMsg = propMsg;
		FXMLLoader fx = new FXMLLoader(getClass().getResource("SingleDialog.fxml"));
		fx.setController(this);
		StageManager.getRoot().getChildren().add((BorderPane) fx.load());
		this.addListener(listener);
	}
	
	private String xtitle, xmessage, xpropMsg;
	
	@FXML private BorderPane background;
	@FXML private Text title;
	@FXML private Text message;
	@FXML public TextField propField;
	
	@FXML private void onCloseRequested() {
		propField.setStyle(null);
		if (propField.getText().trim().isEmpty()) {
			propField.setStyle("-fx-border-color: #ff1a00; -fx-border-width: 1.2px; -fx-border-radius: 0.15em;");
			return;
		}
		this.onClose();
	}
	
	@FXML private void onClose() {
		background.setVisible(false);
		this.onClosed();
	}
	
	@FXML private void onClosed() {
		for (ISingleDialogListener l : listeners) {
			l.onCompleted(propField.getText());
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
		for (ISingleDialogListener l : listeners) {
			l.onCancelled();
		}
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		title.setText(xtitle);
		message.setText(xmessage);
		propField.setPromptText(xpropMsg);
		propField.setFocusTraversable(false);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				propField.requestFocus();
			}
		});
	}
}
