package mediaplayer.util;

import javafx.util.Duration;

/**
 * Class to format time.
 */
public class TimeFormatter {
    /**
     * Converts the given {@link Duration} to hh:mm:ss or mm:ss format.
     * @param time the time in milliseconds
     * @return the formatted time
     */
    public String timeToString(Duration time) {
            int hours = (int) time.toHours();
            int minutes = (int) time.toMinutes() - hours * 60;
            int seconds = (int) time.toSeconds() - hours * 60 * 60 - minutes * 60;

            if (hours > 0) {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
            else {
                return String.format("%02d:%02d", minutes, seconds);
            }
        }
}
