package activation.Screen.AnimatedBackGround;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
public class AnimatedBackGround extends JPanel{

    private volatile BufferedImage currentFrame;

    private final ScheduledExecutorService decoder = Executors.newSingleThreadScheduledExecutor(r ->{
        Thread thread = new Thread(r, "MenuAnimation");
        thread.setDaemon(true);
        return thread;
    });

    private final AtomicInteger generation = new AtomicInteger();
    private ScheduledFuture<?> task;

    public AnimatedBackGround(){
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
    }

    public void playLoop(Path folder, int fps){
        play(folder, fps, true, null);
    }
    public void playOnce(Path folder, int fps, Runnable onFinished){
        play(folder, fps, false, onFinished);
    }

    private void play(Path folder, int fps, boolean loop, Runnable onFinished) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Animation changes must trun on the Swing EDT");
        }

        if (fps <= 0) {
            throw new IllegalArgumentException("FPS must be greater than zero");
        }

        List<Path> frames;

        try (Stream<Path> stream = Files.list(folder)) {
            frames = stream.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().matches("frame_\\d+\\.jpg")).sorted().toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read animation folder: " + folder, ex);
        }

        if (frames.isEmpty()) {
            throw new IllegalArgumentException("No frames found in folder: " + folder);
        }
        stopPlayback();

        int id = generation.get();
        AtomicInteger position = new AtomicInteger();

        long interval = 1_000_000_000L / fps;

        task = decoder.scheduleAtFixedRate(() -> {
            if (id != generation.get()) return;

            int index = position.getAndIncrement();

            if (loop) {
                index %= frames.size();
            } else if (index >= frames.size()) {
                return;
            }

            BufferedImage image;

            try {
                image = ImageIO.read(frames.get(index).toFile());
                if (image == null) {
                    throw new IOException("Invalid image");
                }
            } catch (IOException ex) {
                System.err.println("Cannot read image: " + ex.getMessage());
                return;
            }

            boolean lastFrame = !loop && index == frames.size() - 1;

            if (lastFrame && task != null) {
                task.cancel(false);
            }
            SwingUtilities.invokeLater(() -> {
                if (id != generation.get()) return;

                currentFrame = image;
                repaint();

                if (lastFrame && onFinished != null) {
                    Timer hold = new Timer(150, e -> {
                        if (id == generation.get()) {
                            onFinished.run();
                        }
                    });
                    hold.setRepeats(false);
                    hold.start();
                }
            });
        }, 0, interval, TimeUnit.NANOSECONDS);
    }

    public void stopPlayback(){
        generation.incrementAndGet();

        if (task != null) {
            task.cancel(false);
            task =null;
        }
    }

    public void disposePlayback(){
        stopPlayback();
        decoder.shutdownNow();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponents(graphics);

        BufferedImage frame = currentFrame;
        if (frame == null) return;

        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        double scale = Math.max((double) getWidth() / frame.getWidth(), (double) getHeight() / frame.getHeight());

        int width = (int) Math.ceil(frame.getWidth() * scale);
        int height = (int) Math.ceil(frame.getHeight() * scale);

        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;

        g.drawImage(frame, x, y, width, height, null);
        g.dispose();
    }
}
