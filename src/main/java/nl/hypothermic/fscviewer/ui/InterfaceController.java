package nl.hypothermic.fscviewer.ui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import nl.hypothermic.foscamlib.containers.Account.Privilege;
import nl.hypothermic.foscamlib.containers.Credentials;
import nl.hypothermic.foscamlib.containers.IPConfig;
import nl.hypothermic.fscviewer.core.I18N;
import nl.hypothermic.fscviewer.core.Session;
import nl.hypothermic.fscviewer.core.StageManager;
import nl.hypothermic.fscviewer.core.TransmissionProtocol;
import nl.hypothermic.fscviewer.core.VideoCodec;
import nl.hypothermic.fscviewer.core.XLogger;
import nl.hypothermic.fscviewer.ui.dynamic.ChoiceDialog;
import nl.hypothermic.fscviewer.ui.dynamic.DoubleDialog;
import nl.hypothermic.fscviewer.ui.dynamic.IChoiceDialogListener;
import nl.hypothermic.fscviewer.ui.dynamic.IDoubleDialogListener;
import nl.hypothermic.fscviewer.ui.dynamic.ISingleDialogListener;
import nl.hypothermic.fscviewer.ui.dynamic.SingleDialog;

/*******************************
 * \ > InterfaceController.java * FoscamViewer by hypothermic * www.github.com/hypothermic/ * See LICENSE.md for legal * \
 *******************************/

public class InterfaceController implements IController {

	/*-*/ public InterfaceController() {
		;
	}

	@Override
	/*-*/ public void initialize(URL loc, ResourceBundle rsc) {
		menubar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				if (event.getClickCount() == 1) {
					StageManager.onWindowMoveStart(event);
				} else {
					if (StageManager.isMaximized()) {
						StageManager.demaximize();
					} else {
						StageManager.maximize();
					}
				}
			}
		});
		menubar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				StageManager.onWindowMoveStop(event);
			}
		});
		Platform.runLater(new Runnable() {
			@Override public void run() {
				connectAddr.requestFocus();
			}
		});
		connectAddr.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.TAB) {
					event.consume();
					connectPort.requestFocus();
				}
			}
		});
		connectPort.textProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					connectPort.setText(newValue.replaceAll("\\D", ""));
				}
			}
		});
		connectPort.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.TAB) {
					event.consume();
					connectUser.requestFocus();
				}
			}
		});
		connectUser.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.TAB) {
					event.consume();
					connectPwd.requestFocus();
				}
			}
		});
		connectPane.setVisible(true);
		protocolField.getItems().clear();
		protocolField.getItems().addAll("Auto", "UDP", "TCP");
		protocolField.getSelectionModel().select(0);
		codecField.getItems().clear();
		codecField.getItems().addAll("Auto", "H264", "MPEG");
		codecField.getSelectionModel().select(0);
		connectAddr.setText(System.getenv("fscviewer.cam_addr"));
		connectPort.setText(System.getenv("fscviewer.cam_port"));
		connectUser.setText(System.getenv("fscviewer.cam_username"));
		connectPwd.setText(System.getenv("fscviewer.cam_password"));
		videoView.setScaleX(0.1);
		videoView.setScaleY(0.1);
		rootVideoContainer.widthProperty().addListener(new ChangeListener() {
			@Override public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				videoView.setFitWidth((double) newValue);
				videoView.autosize();
			}
		});
		videoView.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				lastclickX = event.getSceneX();
				lastclickY = event.getSceneY();
				event.consume();
			}
		});
		videoView.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				double maxX = vidX();
				double maxY = vidY();
				if (event.getSceneX() < maxX && event.getSceneY() < maxY) {
					Line line = new Line(lastclickX, lastclickY, event.getSceneX(), event.getSceneY());
					line.setFill(null);
					line.setStroke(Color.ORANGERED);
					line.setStrokeWidth(2);
					root.getChildren().add(line);
					synchronized (syncLock) {
						lines.add(line);
					}
					threadpool.execute(new Runnable() {
						@Override public void run() {
							try {
								Thread.sleep(750);
							} catch (InterruptedException e) {
								;
							}
							synchronized (syncLock) {
								for (Line line : lines) {
									Platform.runLater(new Runnable() {
										@Override public void run() {
											root.getChildren().remove(line);
										}
									});
									lines.remove(line);
									// fixme: rsc leak
								}
							}
						}
					});
				}
				lastclickX = event.getSceneX() > maxX ? maxX : event.getSceneX();
				lastclickY = event.getSceneY() > maxY ? maxY : event.getSceneY();
			}
		});
		System.setErr(errorStream);
	}

	// --- Global vars --- //
	@FXML private AnchorPane root;
	/*-*/ private Session s;
	/*-*/ private static volatile AtomicInteger counter = new AtomicInteger();

	/*-*/ public static final class NamedThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			// note: cached threadpool will reuse depleted threads, numbers in profiler will be confusing
			return new Thread(r, "FSCV-TF-" + counter.incrementAndGet());
		}
	}

	/*-*/ public static final NamedThreadFactory ntfInstance = new NamedThreadFactory();
	/*-*/ private ExecutorService threadpool = Executors.newCachedThreadPool(ntfInstance);
	/*-*/ private static final XLogger log = new XLogger(System.out);
	/*-*/ private final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale("en", "EN"));

	/*-*/ protected static final PrintStream errorStream = new PrintStream(new OutputStream() {
		public void write(int data) {
			;
		}
	});

	/*-*/ public void prepareShutdown() {
		if (s != null) {
			s.viewcl.disconnect();
		}
		if (!threadpool.isShutdown()) {
			threadpool.shutdownNow();
		}
	}

	// --- Connect screen --- //
	@FXML private AnchorPane connectPane;
	@FXML private TextField connectAddr;
	@FXML private TextField connectPort;
	@FXML private TextField connectUser;
	@FXML private TextField connectPwd;
	@FXML private ListView protocolField;
	@FXML private ListView codecField;
	@FXML private Button connectBtn;
	@FXML private ProgressBar connectBar;
	@FXML private Label errorField;

	@FXML private void onConnectRequested() {
		connectBtn.setVisible(false);
		errorField.setVisible(false);
		connectBar.setVisible(true);
		connectBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		try {
			if (connectAddr.getText().isEmpty() || connectUser.getText().isEmpty() || connectPwd.getText().isEmpty())
				throw new NumberFormatException();
			Integer.parseInt(connectPort.getText());
		} catch (NumberFormatException nfx) {
			onConnectFailed();
			return;
		}
		threadpool.execute(new Runnable() {
			@Override public void run() {
				TransmissionProtocol prot = TransmissionProtocol.match(protocolField.getSelectionModel().getSelectedIndex());
				VideoCodec codec = VideoCodec.match(codecField.getSelectionModel().getSelectedIndex());
				s = new Session(connectAddr.getText(), Integer.parseInt(connectPort.getText()), connectUser.getText(), connectPwd.getText(), videoView, prot, codec);
				try {
					s.ctrlcl.connect();
					FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("rtsp://" + connectUser.getText() + ":" + connectPwd.getText() + "@" + connectAddr.getText() + ":" + Integer.parseInt(connectPort.getText()) + "/videoMain");
					if (prot == TransmissionProtocol.UDP) {
						grabber.setOption("rtsp_transport", "udp");
					} else if (prot == TransmissionProtocol.TCP) {
						grabber.setOption("rtsp_transport", "tcp");
					}
					if (codec == VideoCodec.H264) {
						grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
					} else if (codec == VideoCodec.MPEG4) {
						grabber.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
					}
					grabber.start();
					grabber.close();
					grabber.release();
					Session.f.closeInfraLed();
					onPanelInit();
					onAccountsInit();
					onNetworkInit();
				} catch (Exception e) {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							onConnectFailed();
						}
					});
					return;
				}
				Platform.runLater(new Runnable() {
					@Override public void run() {
						onConnectSucceeded();
					}
				});
			}
		});
	}

	/*-*/ private void onConnectFailed() {
		connectBar.setVisible(false);
		errorField.setVisible(true);
		connectBtn.setVisible(true);
	}

	/*-*/ private void onConnectSucceeded() {
		panelTitle.setText(I18N.getString("iface.panel.title") + " " + Session.f.getName() + " - " + s.ctrlcl.getHost());
		s.viewcl.connect();
		videoFloater.setVisible(true);
		connectPane.setVisible(false);
	}

	// --- Menu --- //
	@FXML private MenuBar menubar;
	@FXML private AnchorPane logo;
	@FXML private CheckBox btnMinimize;
	@FXML private CheckBox btnExit;

	@FXML private void onExitRequested() {
		System.exit(1);
	}

	@FXML private void onMinimizeRequested() {
		btnMinimize.setSelected(false);
		StageManager.minimize();
	}

	@FXML private void onMaximizeRequested() {
		StageManager.maximize();
	}

	@FXML private void onDeMaximizeRequested() {
		StageManager.demaximize();
	}

	// --- Panel --- //
	@FXML private AnchorPane rootPanelContainer;
	@FXML private ImageView panelHideBtn;
	@FXML private ImageView panelShowBtn;
	/*-*/ private boolean isPanelHidden = false;
	@FXML private Label panelTitle;
	@FXML private ToggleButton mirrorBtn;
	@FXML private ToggleButton flipBtn;
	@FXML private Button infraBtn;
	/*-*/ private boolean infraState = false;
	@FXML private Button snapBtn;
	@FXML private ImageView ptzUpBtn;
	@FXML private ImageView ptzDownBtn;
	@FXML private ImageView ptzLeftBtn;
	@FXML private ImageView ptzRightBtn;
	@FXML private Slider sliderBrightness;
	@FXML private TextField sliderFieldBrightness;
	@FXML private Slider sliderContrast;
	@FXML private TextField sliderFieldContrast;
	@FXML private Slider sliderHue;
	@FXML private TextField sliderFieldHue;
	@FXML private Slider sliderSaturation;
	@FXML private TextField sliderFieldSaturation;
	@FXML private Slider sliderSharpness;
	@FXML private TextField sliderFieldSharpness;
	@FXML private Accordion panelAccordion;
	@FXML private Label infoField;
	/*-*/ private Timeline infoFieldTask = new Timeline(new KeyFrame(Duration.millis(1000), ae -> new Runnable() {
		@Override public void run() {
			infoField.setText("RAM: " + Math.round(100 * (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().totalMemory()) + "%  |  " + "CPU: " + Runtime.getRuntime().availableProcessors() + "c");
		}
	}.run()));

	/*-*/ private void onPanelInit() {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				if (Session.f.isMirrored()) {
					mirrorBtn.setSelected(true);
				} else {
					mirrorBtn.setSelected(false);
				}
				mirrorBtn.setDisable(false);
				if (Session.f.isFlipped()) {
					flipBtn.setSelected(true);
				} else {
					flipBtn.setSelected(false);
				}
				flipBtn.setDisable(false);
				sliderBrightness.setValue(Session.f.getBrightness());
				sliderBrightness.setDisable(false);
				sliderBrightness.valueProperty().addListener(new ChangeListener<Number>() {
					@Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						Session.f.setBrightness(observable.getValue().intValue());
						sliderFieldBrightness.setText(observable.getValue().intValue() + "");
					}
				});
				sliderBrightness.setTooltip(new Tooltip("Brightness of the video"));
				sliderFieldBrightness.setTooltip(new Tooltip("Brightness of the video"));
				sliderFieldBrightness.setText(Session.f.getBrightness() + "");
				sliderFieldBrightness.textProperty().addListener(new ChangeListener<String>() {
					@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						if (!newValue.matches("\\d*")) {
							sliderFieldBrightness.setText(newValue.replaceAll("[^\\d]", ""));
						}
						int tmp = Integer.parseInt(newValue);
						if (tmp < 0 || tmp > 100) {
							sliderFieldBrightness.setText(oldValue);
						} else {
							Session.f.setBrightness(tmp);
							sliderBrightness.setValue(tmp);
						}
					}
				});
				sliderFieldBrightness.setDisable(false);
				sliderContrast.setValue(Session.f.getContrast());
				sliderContrast.setDisable(false);
				sliderContrast.valueProperty().addListener(new ChangeListener<Number>() {
					@Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						Session.f.setContrast(observable.getValue().intValue());
						sliderFieldContrast.setText(observable.getValue().intValue() + "");
					}
				});
				sliderContrast.setTooltip(new Tooltip("Contrast of the video"));
				sliderFieldContrast.setTooltip(new Tooltip("Contrast of the video"));
				sliderFieldContrast.setText(Session.f.getContrast() + "");
				sliderFieldContrast.textProperty().addListener(new ChangeListener<String>() {
					@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						if (!newValue.matches("\\d*")) {
							sliderFieldContrast.setText(newValue.replaceAll("[^\\d]", ""));
						}
						int tmp = Integer.parseInt(newValue);
						if (tmp < 0 || tmp > 100) {
							sliderFieldContrast.setText(oldValue);
						} else {
							Session.f.setContrast(tmp);
							sliderContrast.setValue(tmp);
						}
					}
				});
				sliderFieldContrast.setDisable(false);
				sliderHue.setValue(Session.f.getHue());
				sliderHue.setDisable(false);
				sliderHue.valueProperty().addListener(new ChangeListener<Number>() {
					@Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						Session.f.setHue(observable.getValue().intValue());
						sliderFieldHue.setText(observable.getValue().intValue() + "");
					}
				});
				sliderHue.setTooltip(new Tooltip("Hue of the video"));
				sliderFieldHue.setTooltip(new Tooltip("Hue of the video"));
				sliderFieldHue.setText(Session.f.getHue() + "");
				sliderFieldHue.textProperty().addListener(new ChangeListener<String>() {
					@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						if (!newValue.matches("\\d*")) {
							sliderFieldHue.setText(newValue.replaceAll("[^\\d]", ""));
						}
						int tmp = Integer.parseInt(newValue);
						if (tmp < 0 || tmp > 100) {
							sliderFieldHue.setText(oldValue);
						} else {
							Session.f.setHue(tmp);
							sliderHue.setValue(tmp);
						}
					}
				});
				sliderFieldHue.setDisable(false);
				sliderSaturation.setValue(Session.f.getSaturation());
				sliderSaturation.setDisable(false);
				sliderSaturation.valueProperty().addListener(new ChangeListener<Number>() {
					@Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						Session.f.setSaturation(observable.getValue().intValue());
						sliderFieldSaturation.setText(observable.getValue().intValue() + "");
					}
				});
				sliderSaturation.setTooltip(new Tooltip("Saturation of the video"));
				sliderFieldSaturation.setTooltip(new Tooltip("Saturation of the video"));
				sliderFieldSaturation.setText(Session.f.getSaturation() + "");
				sliderFieldSaturation.textProperty().addListener(new ChangeListener<String>() {
					@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						if (!newValue.matches("\\d*")) {
							sliderFieldSaturation.setText(newValue.replaceAll("[^\\d]", ""));
						}
						int tmp = Integer.parseInt(newValue);
						if (tmp < 0 || tmp > 100) {
							sliderFieldSaturation.setText(oldValue);
						} else {
							Session.f.setSaturation(tmp);
							sliderSaturation.setValue(tmp);
						}
					}
				});
				sliderFieldSaturation.setDisable(false);
				sliderSharpness.setValue(Session.f.getSharpness());
				sliderSharpness.setDisable(false);
				sliderSharpness.valueProperty().addListener(new ChangeListener<Number>() {
					@Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						Session.f.setSharpness(observable.getValue().intValue());
						sliderFieldSharpness.setText(observable.getValue().intValue() + "");
					}
				});
				sliderSharpness.setTooltip(new Tooltip("Sharpness of the video"));
				sliderFieldSharpness.setTooltip(new Tooltip("Sharpness of the video"));
				sliderFieldSharpness.setText(Session.f.getSharpness() + "");
				sliderFieldSharpness.textProperty().addListener(new ChangeListener<String>() {
					@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						if (!newValue.matches("\\d*")) {
							sliderFieldSharpness.setText(newValue.replaceAll("[^\\d]", ""));
						}
						int tmp = Integer.parseInt(newValue);
						if (tmp < 0 || tmp > 100) {
							sliderFieldSharpness.setText(oldValue);
						} else {
							Session.f.setSharpness(tmp);
							sliderSharpness.setValue(tmp);
						}
					}
				});
				sliderFieldSharpness.setDisable(false);
				infraBtn.setText(I18N.getString("iface.panel.button.infraled.enable"));
				panelAccordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
					@Override public void changed(ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) {
						if (newPane == null) {
							AnchorPane.setTopAnchor(panelAccordion, 561 - panelAccordion.getPrefHeight());
							accountsAccord.setExpandedPane((TitledPane) accountsAccord.getChildrenUnmodifiable().get(0));
							onAccountDetailsHide();
						} else {
							AnchorPane.setTopAnchor(panelAccordion, (double) 265);
							if (newPane.getText() == I18N.getString("iface.panel.accord.accounts")) {
								accountsRefresh();
							}
						}
					}
				});
				AnchorPane.setTopAnchor(panelAccordion, 561 - panelAccordion.getPrefHeight());
				infoFieldTask.setCycleCount(Animation.INDEFINITE);
				infoFieldTask.play();
			}
		});
	}

	@FXML private void onPanelHideRequested() {
		if (!isPanelHidden) {
			onPanelHide();
			isPanelHidden ^= true;
		}
	}

	@FXML private void onPanelShowRequested() {
		if (isPanelHidden) {
			onPanelShow();
			isPanelHidden ^= true;
		}
	}

	/*-*/ private void onPanelHide() {
		rootPanelContainer.setVisible(false);
		AnchorPane.setLeftAnchor(rootVideoContainer, 0.0);
		panelShowBtn.setVisible(true);
	}

	/*-*/ private void onPanelShow() {
		panelShowBtn.setVisible(false);
		AnchorPane.setLeftAnchor(rootVideoContainer, 275.0);
		rootPanelContainer.setVisible(true);
	}

	@FXML private void onNameChangeRequested() {
		try {
			SingleDialog sd = new SingleDialog(I18N.getString("iface.panel.title.renamedialog.title"), I18N.getString("iface.panel.title.renamedialog.info") + " \"" + Session.f.getName() + "\"", I18N.getString("iface.panel.title.renamedialog.field"), new ISingleDialogListener() {
				@Override public void onCompleted(String result) {
					threadpool.execute(() -> {
						Session.f.setName(result);
						Platform.runLater(() -> panelTitle.setText(I18N.getString("iface.panel.title") + " " + Session.f.getName() + " - " + s.ctrlcl.getHost()));
					});
				}
				@Override public void onCancelled() {
					;
				}
			});
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	@FXML private void onMirrorRequested() {
		threadpool.execute(() -> Session.f.setMirrored(!Session.f.isMirrored()));
	}

	@FXML private void onFlipRequested() {
		threadpool.execute(() -> Session.f.setFlipped(!Session.f.isFlipped()));
	}

	@FXML private void onInfraToggleRequested() {
		if (infraState) {
			threadpool.execute(() -> Session.f.closeInfraLed());
		} else {
			threadpool.execute(() -> Session.f.openInfraLed());
		}
		infraState = !infraState;
		infraBtn.setText(infraState ? I18N.getString("iface.panel.button.infraled.disable") : I18N.getString("iface.panel.button.infraled.enable"));
	}

	@FXML private void onSnapshotRequested() {
		videoView.snapshot(new Callback<SnapshotResult, Void>() {
			@Override public Void call(SnapshotResult res) {
				FileChooser fc = new FileChooser();
				fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG", "*.png"));
				fc.setTitle(I18N.getString("iface.panel.button.snapshot.save"));
				File file = StageManager.filechooserSave(fc);
				if (file != null) {
					try {
						ImageIO.write(SwingFXUtils.fromFXImage(res.getImage(), null), "png", file);
					} catch (IOException e) {
						log.severe("Failed to write image");
						e.printStackTrace();
					}
				}
				return null;
			}
		}, new SnapshotParameters(), null);
	}

	@FXML private void onPtzMoveUp() {
		threadpool.execute(() -> Session.f.ptzMoveUp());
	}

	@FXML private void onPtzMoveDown() {
		threadpool.execute(() -> Session.f.ptzMoveDown());
	}

	@FXML private void onPtzMoveLeft() {
		threadpool.execute(() -> Session.f.ptzMoveLeft());
	}

	@FXML private void onPtzMoveRight() {
		threadpool.execute(() -> Session.f.ptzMoveRight());
	}

	@FXML private void onPtzResetRequested() {
		threadpool.execute(() -> Session.f.ptzResetPosition());
	}

	// --- Accord: Accounts --- //
	@FXML private ListView accountsList;
	@FXML private Accordion accountsAccord;
	
	@FXML private TextField accountsAddUsername;
	@FXML private TextField accountsAddPassword;
	@FXML private ChoiceBox accountsAddPrivilege;
	@FXML private Button accountsAddBtn;
	/*-*/ private Node[] accountsAddNodelist = new Node[] {accountsAddUsername, accountsAddPassword, accountsAddPrivilege};
	
	@FXML private Label accountsDetailsTitle;
	@FXML private Label accountsDetailsPriv;
	
	@FXML private Label accountsDetailsMsg;
	@FXML private Button accountsDetailsPassword; // I chose to name it "Password" since "Change password" didn't fit on the button .-.
	@FXML private Button accountsDetailsDelete;
	
	@FXML private ToggleButton networkDhcpBtn;
	@FXML private TextField networkIpField;
	@FXML private TextField networkGatewayField;
	@FXML private TextField networkMaskField;
	@FXML private TextField networkDns1Field;
	@FXML private TextField networkDns2Field;

	/*-*/ private void onAccountsInit() {
		accountsAccord.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
			@Override public void changed(ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) {
				if (oldPane != null) {
					oldPane.setCollapsible(true);
				}
				if (newPane != null) {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							newPane.setCollapsible(false);
						}
					});
				}
			}
		});
		accountsAccord.setExpandedPane((TitledPane) accountsAccord.getChildrenUnmodifiable().get(0));
		accountsAddPrivilege.getItems().clear();
		accountsAddPrivilege.getItems().addAll(Privilege.VISITOR, Privilege.OPERATOR, Privilege.ADMINISTRATOR);
		accountsAddPrivilege.getSelectionModel().selectFirst();
		accountsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.isEmpty()) {
					onAccountDetailsHide();
					return;
				}
				accountsAccord.setExpandedPane((TitledPane) accountsAccord.getChildrenUnmodifiable().get(1));
				onAccountDetailsShow();
				accountsDetailsTitle.setText((String) accountsList.getSelectionModel().getSelectedItem());
			}
		});
	}

	/*-*/ private void accountsRefresh() {
		threadpool.execute(() -> {
			List<String> tmp = Session.f.getUserList();
			List<String> ses = Session.f.getSessionList();
			int thisIndex;
			for (String str : tmp) {
				thisIndex = tmp.lastIndexOf(str);
				if (ses.contains(str)) {
					str = "● " + str;
				}
				tmp.set(thisIndex, str.replaceAll("%2B[^;]", ""));
			}
			Platform.runLater(() -> {
				accountsList.getItems().clear();
				accountsList.getItems().addAll(I18N.getString("iface.panel.accord.accounts.list.title"));
				accountsList.getItems().addAll(tmp);
			});
		});
	}
	
	@FXML private void onAccountAddRequested() {
		accountsAddUsername.getStyleClass().removeAll("iface-panel-textfield-error");
		accountsAddPassword.getStyleClass().removeAll("iface-panel-textfield-error");
		if (accountsAddUsername.getText().isEmpty()) {
			accountsAddUsername.getStyleClass().add("iface-panel-textfield-error");
			return;
		}
		if (accountsAddPassword.getText().isEmpty()) {
			accountsAddPassword.getStyleClass().add("iface-panel-textfield-error");
			return;
		}
		threadpool.execute(() -> {
			Session.f.addAccount(new Credentials(accountsAddUsername.getText(), accountsAddPassword.getText()), 
									(Privilege) accountsAddPrivilege.getSelectionModel().getSelectedItem());
			accountsAddUsername.clear();
			accountsAddPassword.clear();
			accountsRefresh();
		});
	}
	
	/*-*/ private void onAccountDetailsShow() {
		accountsDetailsMsg.setVisible(false);
		accountsDetailsPassword.setVisible(true);
		accountsDetailsDelete.setVisible(true);
		accountsDetailsTitle.setVisible(true);
		accountsDetailsPriv.setVisible(true);
	}
	
	/*-*/ private void onAccountDetailsHide() {
		accountsDetailsPassword.setVisible(false);
		accountsDetailsDelete.setVisible(false);
		accountsDetailsTitle.setVisible(false);
		accountsDetailsPriv.setVisible(false);
		accountsDetailsMsg.setVisible(true);
	}
	
	@FXML private void onAccountDetailsChangePassword() throws IOException {
		String targetAcc = (String) accountsList.getSelectionModel().getSelectedItem();
		DoubleDialog dd = new DoubleDialog(I18N.getString("iface.panel.accord.accounts.details.passworddialog.title"), 
										   I18N.getString("iface.panel.accord.accounts.details.passworddialog.info") + " " + targetAcc, 
										   I18N.getString("iface.panel.accord.accounts.details.passworddialog.old"), 
										   I18N.getString("iface.panel.accord.accounts.details.passworddialog.new"));
		dd.addListener(new IDoubleDialogListener() {
			@Override public void onCompleted(String... result) {
				threadpool.execute(() -> {
					Session.f.changePassword(targetAcc, result[0], result[1]);
				});
			}
			@Override public void onCancelled() {
				;
			}
		});
	}
	
	@FXML private void onAccountDetailsDelete() {
		threadpool.execute(() -> {
			Session.f.deleteAccount((String) accountsList.getSelectionModel().getSelectedItem());
			accountsRefresh();
		});
	}
	
	/*-*/ private void onNetworkInit() {
		IPConfig cfg = Session.f.getIPConfig();
		networkDhcpBtn.setSelected(cfg.isDHCP);
		networkDhcpBtn.setText(cfg.isDHCP ? I18N.getString("iface.panel.accord.network.dhcp.enabled") : I18N.getString("iface.panel.accord.network.dhcp.disabled"));
        final UnaryOperator<Change> ipAddressFilter = c -> {
            return (c.getControlNewText().matches(JfxUtil.makePartialIPRegex()) ? c : null);
        };
        networkIpField.setTextFormatter(new TextFormatter(ipAddressFilter));
        networkIpField.setText(cfg.ip);
        networkIpField.setTooltip(new Tooltip(I18N.getString("iface.panel.accord.network.ip")));
        networkGatewayField.setTextFormatter(new TextFormatter(ipAddressFilter));
        networkGatewayField.setText(cfg.gate);
        networkGatewayField.setTooltip(new Tooltip(I18N.getString("iface.panel.accord.network.gateway")));
        networkMaskField.setTextFormatter(new TextFormatter(ipAddressFilter));
        networkMaskField.setText(cfg.mask);
        networkMaskField.setTooltip(new Tooltip(I18N.getString("iface.panel.accord.network.subnetmask")));
        networkDns1Field.setTextFormatter(new TextFormatter(ipAddressFilter));
        networkDns1Field.setText(cfg.dns1);
        networkDns1Field.setTooltip(new Tooltip(I18N.getString("iface.panel.accord.network.dns1")));
        networkDns2Field.setTextFormatter(new TextFormatter(ipAddressFilter));
        networkDns2Field.setText(cfg.dns2);
        networkDns2Field.setTooltip(new Tooltip(I18N.getString("iface.panel.accord.network.dns2")));
	}
	
	@FXML private void onDhcpToggleRequested() throws IOException {
		ChoiceDialog cd = new ChoiceDialog("Warning", "Changing DHCP status will reboot the device!", "Cancel", "Continue", new IChoiceDialogListener() {
			@Override public void onCancelled() {
				boolean isDhcpEnabled = Session.f.getIPConfig().isDHCP;
				networkDhcpBtn.setText(isDhcpEnabled ? I18N.getString("iface.panel.accord.network.dhcp.enabled") : I18N.getString("iface.panel.accord.network.dhcp.disabled"));
				networkDhcpBtn.setSelected(isDhcpEnabled);
			}
			@Override public void onAgreed() {
				networkDhcpBtn.setDisable(true);
				IPConfig oldcfg = Session.f.getIPConfig();
				oldcfg.isDHCP = !oldcfg.isDHCP;
				Session.f.setIPConfig(oldcfg);
				boolean isDhcpEnabled = Session.f.getIPConfig().isDHCP;
				networkDhcpBtn.setText(isDhcpEnabled ? I18N.getString("iface.panel.accord.network.dhcp.enabled") : I18N.getString("iface.panel.accord.network.dhcp.disabled"));
				networkDhcpBtn.setSelected(isDhcpEnabled);
				networkDhcpBtn.setDisable(false);
			}
		});
	}

	// --- Video view --- //
	@FXML private BorderPane rootVideoContainer;
	@FXML private ImageView videoView;
	@FXML private MenuBar videoFloater;
	/*-*/ private static double lastclickX;
	/*-*/ private static double lastclickY;
	/*-*/ private static final Object syncLock = new Object();
	/*-*/ private static volatile ArrayList<Line> lines = new ArrayList<Line>();

	@FXML private void onMainStreamRequested() {
		s.viewcl.connect("/videoMain");
	}

	@FXML private void onSubStreamRequested() {
		s.viewcl.connect("/videoSub");
	}

	@FXML private void onAudioStreamRequested() {
		s.viewcl.connect("/audio");
	}

	/*-*/ private double vidX() {
		return videoView.getImage().getWidth();
	}

	/*-*/ private double vidY() {
		return videoView.getImage().getHeight();
	}

	// --- About menu --- //
	@FXML private BorderPane aboutMenu;

	@FXML private void onAboutOpenRequested() {
		aboutMenu.setVisible(true);
	}

	@FXML private void onAboutCloseRequested() {
		aboutMenu.setVisible(false);
	}
}
