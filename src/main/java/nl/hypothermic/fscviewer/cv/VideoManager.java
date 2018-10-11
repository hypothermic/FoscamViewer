package nl.hypothermic.fscviewer.cv;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nl.hypothermic.fscviewer.core.TransmissionProtocol;
import nl.hypothermic.fscviewer.core.VideoCodec;

/*******************************\
 * > VideoManager.java       < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class VideoManager {
	
	private final ImageView view;
	
	public final VideoPlayThread playThread;
	
	public VideoManager(String MRL, TransmissionProtocol prot, VideoCodec codec, ImageView view) {
		System.out.println("--- vdm init ---");
		this.view = view;
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(MRL);
		playThread = new VideoPlayThread(grabber, view, prot, codec);
	    playThread.start();
	    System.out.println("--- vdm init done ---");
	}
	
	public void stop() {
		System.out.println("--- vdm stop ---");
		playThread.interrupt();
	}
}
