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
 * > ChoiceDialog.java       < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class ChoiceDialog implements Initializable {

	private List<IChoiceDialogListener> listeners = new ArrayList<IChoiceDialogListener>();

	public void addListener(IChoiceDialogListener listener) {
		listeners.add(listener);
	}

	public ChoiceDialog(String title, String message, String leftMsg, String rightMsg) throws IOException {
		this.xtitle = title;
		this.xmessage = message;
		this.leftMsg = leftMsg;
		this.rightMsg = rightMsg;
		FXMLLoader fx = new FXMLLoader(getClass().getResource("ChoiceDialog.fxml"));
		fx.setController(this);
		StageManager.getRoot().getChildren().add((BorderPane) fx.load());
	}

	public ChoiceDialog(String title, String message, String leftMsg, String rightMsg, IChoiceDialogListener listener) throws IOException {
		this(title, message, leftMsg, rightMsg);
		addListener(listener);
	}

	private String xtitle, xmessage, leftMsg, rightMsg;

	@FXML private BorderPane background;
	@FXML private Text title;
	@FXML private Text message;
	@FXML private Button leftBtn;
	@FXML private Button rightBtn;

	@FXML private void onRightRequested() {
		this.onClose();
	}

	@FXML private void onClose() {
		background.setVisible(false);
		this.onClosed();
	}

	@FXML private void onClosed() {
		for (IChoiceDialogListener l : listeners) {
			l.onAgreed();
		}
	}

	@FXML private void onLeftRequested() {
		this.onCancel();
	}

	@FXML private void onCancel() {
		background.setVisible(false);
		this.onCancelled();
	}

	@FXML private void onCancelled() {
		for (IChoiceDialogListener l : listeners) {
			l.onCancelled();
		}
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		title.setText(xtitle);
		message.setText(xmessage);
		leftBtn.setText(leftMsg);
		leftBtn.setFocusTraversable(false);
		Platform.runLater(new Runnable() {
			@Override public void run() {
				leftBtn.requestFocus();
			}
		});
		rightBtn.setText(leftMsg);
		rightBtn.setFocusTraversable(false);
	}
}
