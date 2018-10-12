package nl.hypothermic.fscviewer.cv;

import static org.bytedeco.javacpp.helper.opencv_imgproc.cvFindContours;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

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

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core.CvBox2D;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.CvSize2D32f;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nl.hypothermic.fscviewer.core.TransmissionProtocol;
import nl.hypothermic.fscviewer.core.VideoCodec;
import nl.hypothermic.fscviewer.ui.InterfaceController;

/*******************************\
 * > VideoPlayThread.java    < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class VideoPlayThread extends Thread {
	
	private FFmpegFrameGrabber grabber;
	private ImageView view;
	private TransmissionProtocol prot;
	private VideoCodec codec;
	
	private FloatControl volume; // TODO: user volume control (volume.setValue(negatieve float decibel))
	private boolean isProcessing;
	private String status;
	
	// --- Buffers --- //
	private Frame frame;
	private Image currentFrame;
	private ShortBuffer channelSamplesShortBuffer;
	private ByteBuffer outBuffer;
	
	public VideoPlayThread(FFmpegFrameGrabber grabber, ImageView view, TransmissionProtocol prot, VideoCodec codec) {
		this.grabber = grabber;
		this.view = view;
		this.prot = prot;
		this.codec = codec;
	}
	
	public void run() {
		/**
		 * Inspired from: "https://github.com/bytedeco/javacv/blob/master/samples/JavaFxPlayVideoAndAudio.java" by Dmitriy Gerashenko
		 */
		try {
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
            AudioFormat audioFormat = new AudioFormat(grabber.getSampleRate(), 16, grabber.getAudioChannels(), true, true);

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(info);
            soundLine.open(audioFormat);
            volume = (FloatControl) soundLine.getControl(FloatControl.Type.MASTER_GAIN);
            soundLine.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            
            ExecutorService executor = Executors.newSingleThreadExecutor(InterfaceController.ntfInstance);
            view.setImage(null);
            view.setScaleX(1);
            view.setScaleY(1);
            Thread.currentThread().setName("FSCV-VPT");
            isProcessing = true;
            while (!Thread.interrupted()) {
                frame = grabber.grab();
                if (frame == null) {
                    break;
                }
                if (frame.image != null) {
                    currentFrame = SwingFXUtils.toFXImage(converter.convert(frame), null);
                    Platform.runLater(() -> {
                        view.setImage(currentFrame);
                    });
                } else if (frame.samples != null) {
                    channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
                    channelSamplesShortBuffer.rewind();

                    outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);
                    
                    for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
                        short val = channelSamplesShortBuffer.get(i);
                        outBuffer.putShort(val);
                    }

                    /**
                     * We need this because soundLine.write ignores
                     * interruptions during writing.
                     */
                    try {
                        executor.submit(() -> {
                            soundLine.write(outBuffer.array(), 0, outBuffer.capacity());
                            outBuffer.clear();
                        }).get();
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            isProcessing = false;
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            soundLine.stop();
            grabber.stop();
            grabber.release();
            grabber.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void setHeight(int height) {
		grabber.setImageHeight(height);
	}
	
	public void setWidth(int width) {
		grabber.setImageWidth(width);
	}
	
	public boolean isProcessing() {
		return isProcessing;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public float getVolume() {
		return volume.getValue();
	}
	
	public void setVolume(float newValue) {
		volume.setValue(newValue);
	}
}
