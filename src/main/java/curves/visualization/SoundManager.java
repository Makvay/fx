package curves.visualization;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;


public class SoundManager {
    private static SoundManager instance;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    private SoundManager() {
        initializeMediaPlayer();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void initializeMediaPlayer() {

        String file = "src\\main\\java\\curves\\SpinningCat.mp3";
        Media sound = new Media(new File(file).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);

        mediaPlayer.setOnEndOfMedia(() -> {
            isPlaying = false;
            mediaPlayer.stop();
        });
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            // в диапазоне от 0.0 до 1.0
            double playerVolume = Math.max(0.0, Math.min(1.0, volume));
            mediaPlayer.setVolume(playerVolume);
        }
    }

    public double getVolume() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVolume();
        }
        return 0.0;
    }


    public void playSound() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.play();
            isPlaying = true;
        }
    }

    public void stopSound() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    public void pauseSound() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;

        }
    }

    public boolean isSoundPlaying() {
        return isPlaying;
    }
}