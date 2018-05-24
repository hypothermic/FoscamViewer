package nl.hypothermic.fscviewer.ui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
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
import nl.hypothermic.fscviewer.core.I18N;
import nl.hypothermic.fscviewer.core.Session;
import nl.hypothermic.fscviewer.core.StageManager;
import nl.hypothermic.fscviewer.core.TransmissionProtocol;
import nl.hypothermic.fscviewer.core.VideoCodec;
import nl.hypothermic.fscviewer.core.XLogger;

/*******************************\
 * > InterfaceController.java  *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic  *
 *  See LICENSE.md for legal   *
\*******************************/

public class InterfaceController implements IController {
	
	/*-*/ public InterfaceController() {
		;
	}

	@Override
	/*-*/ public void initialize(URL loc, ResourceBundle rsc) {
		menubar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
            public void handle(MouseEvent event) {
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
        	@Override
            public void handle(MouseEvent event) {
                StageManager.onWindowMoveStop(event);
            }
        });
        Platform.runLater(new Runnable() {
			@Override
			public void run() {
				connectAddr.requestFocus();
			}
        });
        connectAddr.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.TAB) {
                    event.consume();
                    connectPort.requestFocus();
                }
            }
        });
        connectPort.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    connectPort.setText(newValue.replaceAll("\\D", ""));
                }
            }
        });
        connectPort.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.TAB) {
                    event.consume();
                    connectUser.requestFocus();
                }
            }
        });
        connectUser.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
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
        videoView.setScaleX(0.1);
        videoView.setScaleY(0.1);
        rootVideoContainer.widthProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				videoView.setFitWidth((double) newValue);
				videoView.autosize();
			}
        });
        videoView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                lastclickX = event.getSceneX();
                lastclickY = event.getSceneY();
                event.consume();
            }
        });
        videoView.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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
                    	@Override
                    	public void run() {
                    		try {
								Thread.sleep(750);
							} catch (InterruptedException e) {
								;
							}
                    		synchronized (syncLock) {
                            	for (Line line : lines) {
                            		Platform.runLater(new Runnable() {
										@Override
										public void run() {
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
	/*-*/ private static XLogger log = new XLogger(System.out);
	
	/*-*/ protected static final PrintStream errorStream = new PrintStream(new OutputStream(){
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
		// TODO: add red border around field if empty.
		try {
			if (connectAddr.getText().isEmpty() && connectUser.getText().isEmpty() && connectPwd.getText().isEmpty()) throw new NumberFormatException();
			Integer.parseInt(connectPort.getText());
		} catch (NumberFormatException nfx) {
			onConnectFailed();
			return;
		}
		threadpool.execute(new Runnable() {
		    @Override
		    public void run() {
		    	// TODO: clean up, optimize, and move this messy code. It works but it's not pretty.
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
					s.f.closeInfraLed();
					onPanelInit();
				} catch (Exception e) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							onConnectFailed();
						}
					});
					return;
				}
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
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
		panelTitle.setText(panelTitle.getText() + " " + s.f.getName() + " - " + s.ctrlcl.getHost());
		s.viewcl.connect();
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
	@FXML private Label infoField;
	/*-*/ private Timeline infoFieldTask = new Timeline(new KeyFrame(Duration.millis(1000), ae -> new Runnable() {
		@Override
		public void run() {
			infoField.setText("RAM: " + Math.round(100 * (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().totalMemory()) + "%  |  "
			                + "CPU: " + Runtime.getRuntime().availableProcessors() + "c");
		}
	}.run()));
	
	/*-*/ private void onPanelInit() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (s.f.isMirrored()) {
					mirrorBtn.setSelected(true);
				} else {
					mirrorBtn.setSelected(false);
				}
				mirrorBtn.setDisable(false);
				if (s.f.isFlipped()) {
					flipBtn.setSelected(true);
				} else {
					flipBtn.setSelected(false);
				}
				flipBtn.setDisable(false);
				infraBtn.setText(I18N.getString("iface.panel.button.infraled.enable"));
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
	
	@FXML private void onMirrorRequested() {
		threadpool.execute(() -> s.f.setMirrored(!s.f.isMirrored()));
	}
	
	@FXML private void onFlipRequested() {
		threadpool.execute(() -> s.f.setFlipped(!s.f.isFlipped()));
	}
	
	@FXML private void onInfraToggleRequested() {
		if (infraState) {
			threadpool.execute(() -> s.f.closeInfraLed());
		} else {
			threadpool.execute(() -> s.f.openInfraLed());
		}
		infraState = !infraState;
		infraBtn.setText(infraState ? I18N.getString("iface.panel.button.infraled.disable") : I18N.getString("iface.panel.button.infraled.enable"));
	}
	
	@FXML private void onSnapshotRequested() {
		videoView.snapshot(new Callback<SnapshotResult, Void>() {
			@Override
			public Void call(SnapshotResult res) {
				FileChooser fc = new FileChooser();
				fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG", "*.png"));
				fc.setTitle("Save picture");
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
		threadpool.execute(() -> s.f.ptzMoveUp());
	}
	
	@FXML private void onPtzMoveDown() {
		threadpool.execute(() -> s.f.ptzMoveDown());
	}
	
	@FXML private void onPtzMoveLeft() {
		threadpool.execute(() -> s.f.ptzMoveLeft());
	}
	
	@FXML private void onPtzMoveRight() {
		threadpool.execute(() -> s.f.ptzMoveRight());
	}
	
	@FXML private void onPtzResetRequested() {
		threadpool.execute(() -> s.f.ptzResetPosition());
	}
	
	// --- Video view --- //
	@FXML private BorderPane rootVideoContainer;
	@FXML private ImageView videoView;
	/*-*/ private static double lastclickX;
	/*-*/ private static double lastclickY;
	/*-*/ private static final Object syncLock = new Object();
	/*-*/ private static volatile ArrayList<Line> lines = new ArrayList<Line>();
	
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
