package dev.throwlytics.ThrowlyticsBackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Service for generating thumbnails from video files
 * Uses FFmpeg to extract frames from videos
 */
@Service
public class ThumbnailService {
    
    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;
    
    /**
     * Extract a frame from a video file at a specific frame number
     * 
     * @param videoPath Full path to video file
     * @param frameNumber Frame number to extract (0-indexed)
     * @return Byte array of JPEG image
     * @throws IOException if extraction fails
     */
    public byte[] extractFrame(Path videoPath, int frameNumber) throws IOException {
        // Create temporary output file for the frame
        File tempOutput = File.createTempFile("frame_", ".jpg");
        tempOutput.deleteOnExit();
        
        try {
            // Build FFmpeg command
            // ffmpeg -i input.mp4 -vf "select=eq(n\,FRAME_NUMBER)" -vframes 1 output.jpg
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-i", videoPath.toString(),
                    "-vf", "select=eq(n\\," + frameNumber + ")",
                    "-vframes", "1",
                    "-q:v", "2",  // High quality
                    "-y",  // Overwrite output file
                    tempOutput.getAbsolutePath()
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Wait for process to complete
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new IOException("FFmpeg failed with exit code: " + exitCode);
            }
            
            // Read the generated image
            BufferedImage image = ImageIO.read(tempOutput);
            if (image == null) {
                throw new IOException("Failed to read extracted frame");
            }
            
            // Convert to byte array (JPEG)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Frame extraction interrupted", e);
        } catch (Exception e) {
            throw new IOException("Failed to extract frame: " + e.getMessage(), e);
        } finally {
            // Cleanup
            if (tempOutput.exists()) {
                tempOutput.delete();
            }
        }
    }
    
    /**
     * Extract a frame at a specific time (in seconds) from a video
     * 
     * @param videoPath Full path to video file
     * @param timeInSeconds Time in seconds
     * @param fps FPS of the video (to convert time to frame number)
     * @return Byte array of JPEG image
     * @throws IOException if extraction fails
     */
    public byte[] extractFrameAtTime(Path videoPath, double timeInSeconds, int fps) throws IOException {
        int frameNumber = (int) (timeInSeconds * fps);
        return extractFrame(videoPath, frameNumber);
    }
    
    /**
     * Extract the first frame of a video (thumbnail)
     * 
     * @param videoPath Full path to video file
     * @return Byte array of JPEG image
     * @throws IOException if extraction fails
     */
    public byte[] extractFirstFrame(Path videoPath) throws IOException {
        return extractFrame(videoPath, 0);
    }
}

