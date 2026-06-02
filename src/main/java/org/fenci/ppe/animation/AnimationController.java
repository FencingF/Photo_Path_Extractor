package org.fenci.ppe.animation;

import org.fenci.ppe.Main;
import org.fenci.ppe.data.JSONData;
import org.fenci.ppe.map.Coordinate;
import org.fenci.ppe.map.Map;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AnimationController {

    private final Map map;
    private final List<JSONData> data;
    private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

    private int currentIndex = 0;
    private long delayMs = 30;
    private boolean playing = false;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;

    // Called whenever the frame index changes, so the UI slider stays in sync
    private Runnable onFrameChange;

    public AnimationController(Map map, List<JSONData> data) {
        this.map = map;
        this.data = data;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void setOnFrameChange(Runnable r) {
        this.onFrameChange = r;
    }

    public void play() {
        if (playing) return;
        if (currentIndex >= data.size()) currentIndex = 0; // replay from start if at end
        playing = true;
        scheduleNext();
    }

    public void pause() {
        playing = false;
        if (task != null) task.cancel(false);
    }

    public void replay() {
        pause();
        currentIndex = 0;
        // reset rainbow by seeking to frame 0
        play();
    }

    /** Seek to a specific frame index (0-based). Safe to call while playing or paused. */
    public void seekTo(int index) {
        boolean wasPlaying = playing;
        pause();
        currentIndex = Math.max(0, Math.min(index, data.size() - 1));
        renderFrame(currentIndex);
        if (wasPlaying) play();
    }

    public void setDelay(long ms) {
        boolean wasPlaying = playing;
        pause();
        this.delayMs = Math.max(1, ms);
        if (wasPlaying) play();
    }

    public long getDelay() { return delayMs; }
    public int getCurrentIndex() { return currentIndex; }
    public int getTotalFrames() { return data.size(); }
    public boolean isPlaying() { return playing; }

    private void scheduleNext() {
        if (!playing) return;
        task = scheduler.schedule(() -> {
            if (!playing || currentIndex >= data.size()) {
                playing = false;
                return;
            }
            renderFrame(currentIndex);
            currentIndex++;
            if (currentIndex < data.size()) {
                scheduleNext();
            } else {
                playing = false;
                // Show replay prompt
                SwingUtilities.invokeLater(() -> map.drawTopText("Done — press Replay ↺"));
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private Color colorAtIndex(int index) {
        Color c = Color.RED;
        for (int i = 0; i < index; i++) {
            c = Main.getNextRainbowColor(c);
        }
        return c;
    }

    private void renderFrame(int index) {
        JSONData d = data.get(index);
        Color color = colorAtIndex(index);
        SwingUtilities.invokeLater(() -> {
            map.clearPoints();
            if (!map.isVisible(d.latitude(), d.longitude())) {
                map.centerOn(d.latitude(), d.longitude());
            }
            map.addPoint(new Coordinate(d.latitude(), d.longitude(), color));
            map.drawTopText(formatter.format(d.date()));
        });
        if (onFrameChange != null) {
            SwingUtilities.invokeLater(onFrameChange);
        }
    }
}