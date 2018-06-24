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
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import nl.hypothermic.fscviewer.core.StageManager;

/*******************************\
 * > ButtonDialog.java       < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class ButtonDialog implements Initializable {
	
	private List<IButtonDialogListener> listeners = new ArrayList<IButtonDialogListener>();

    public void addListener(IButtonDialogListener listener) {
        listeners.add(listener);
    }
    
    public ButtonDialog(String title, String message, String propMsg) throws IOException {
		this.xtitle = title; this.xmessage = message; this.xbtnMsg = propMsg;
		FXMLLoader fx = new FXMLLoader(getClass().getResource("ButtonDialog.fxml"));
		fx.setController(this);
		StageManager.getRoot().getChildren().add((BorderPane) fx.load());
	}
	
	public ButtonDialog(String title, String message, String propMsg, IButtonDialogListener listener) throws IOException {
		this.xtitle = title; this.xmessage = message; this.xbtnMsg = propMsg;
		FXMLLoader fx = new FXMLLoader(getClass().getResource("ButtonDialog.fxml"));
		fx.setController(this);
		StageManager.getRoot().getChildren().add((BorderPane) fx.load());
		this.addListener(listener);
	}
	
	private String xtitle, xmessage, xbtnMsg;
	
	@FXML private BorderPane background;
	@FXML private Text title;
	@FXML private Text message;
	@FXML private Button btn;
	
	@FXML private void onCloseRequested() {
		this.onClose();
	}
	
	@FXML private void onClose() {
		background.setVisible(false);
		this.onClosed();
	}
	
	@FXML private void onClosed() {
		for (IButtonDialogListener l : listeners) {
			l.onCompleted();
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
		for (IButtonDialogListener l : listeners) {
			l.onCancelled();
		}
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		title.setText(xtitle);
		message.setText(xmessage);
		btn.setText(xbtnMsg);
		btn.setFocusTraversable(false);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				btn.requestFocus();
			}
		});
	}
}
