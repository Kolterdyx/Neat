package me.kolterdyx;

import me.kolterdyx.neat.Neat;
import me.kolterdyx.neat.utils.Configuration;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.ejml.simple.SimpleMatrix;
import org.graphstream.graph.Graph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.stream.sync.SourceTime;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvLoadImage;

public class Main {

    static Neat net;
    static BufferedImage image;
    static WritableRaster raster;

    public static void main(String[] args) throws InterruptedException, IOException {

        int width = 50;
        int height = width;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        raster = image.getRaster();
        pixels = new int[width*height*3];

        net = new Neat(new Configuration("config.yml"));
        String prefix = "images/evolution";

//        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("net_video.avi",800,570);

//        recorder.setFrameRate(10);
//        recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_RGB555);
//        System.out.println(""+recorder.getPixelFormat()+" "+avutil.AV_PIX_FMT_YUV420P);
//        recorder.setVideoBitrate(9000);
//        recorder.setFormat("mp4");
//        recorder.setVideoQuality(0); // maximum quality
//        recorder.start();

        for (int i = 0; i < 600; i++) {
            net.tryMutation();
            net.plotGraph();
            Thread.sleep(500);
            net.getGraph().addAttribute("ui.screenshot", prefix+i+".png");
            System.out.println("Frame: "+i);
        }

        String imgPath="images/";
        String vidPath="net_video.mp4";
        ArrayList<String> links = new ArrayList<>();
//        File f=new File(imgPath);
//        File[] f2=f.listFiles();
        for (int i = 0; i < 600; i++) {
            links.add(new File(prefix+i+".png").getAbsolutePath());
        }
        convertJPGtoMovie(links, vidPath);
        System.out.println("Video has been created at "+vidPath);

//        recorder.stop();


        SimpleMatrix data = new SimpleMatrix(new double[][]{
                new double[]{1, 0}
        });

        System.out.println(net.feed(data));




        net.plotGraph();

    }

    static int[] pixels;

    public static BufferedImage renderImage(int width, int height){

        for (int i=0;i<pixels.length;i+=3) {
            int x = i % width;
            int y = (int) Math.floor(i / height);
            SimpleMatrix data = net.feed(new SimpleMatrix(new double[][]{new double[]{x, y}}));
            pixels[i] = (int) (data.get(0)*255);
            pixels[i+1] = (int) (data.get(1)*255);
            pixels[i+2] = (int) (data.get(2)*255);
        }
        raster.setPixels(0, 0, width, height, pixels);
        image.setData(raster);
        return image;
    }

    public static void convertJPGtoMovie(ArrayList<String> links, String vidPath)
    {
        OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(vidPath,1280,720);
        try {
            recorder.setFrameRate(10);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
            recorder.setVideoBitrate(9000);
            recorder.setFormat("mp4");
            recorder.setVideoQuality(0); // maximum quality
            recorder.start();
            for (int i=0;i<links.size();i++)
            {
                recorder.record(grabberConverter.convert(cvLoadImage(links.get(i))));
                System.out.println("Frame: "+i);
            }
            recorder.stop();
        }
        catch (org.bytedeco.javacv.FrameRecorder.Exception e){
            e.printStackTrace();
        }
    }
}
