package mediaplayer.util;

import javafx.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeUtilTest {
    private TimeUtil timeUtil;

    @BeforeEach
    public void setUp() {
        timeUtil = new TimeUtil();
    }

    @AfterEach
    public void tearDown() {
        timeUtil = null;
    }

    @Test
    public void minutesConversion() {
        Duration time = Duration.minutes(45).add(Duration.seconds(10));
        assertEquals("45:10", timeUtil.timeToString(time));
    }

    @Test
    public void hoursConversion() {
        Duration time = Duration.hours(2).add(Duration.minutes(30).add(Duration.seconds(40)));
        assertEquals("02:30:40", timeUtil.timeToString(time));
    }

    @Test
    public void secondsWithoutHoursConversion() {
        Duration time = Duration.seconds(80);
        assertEquals("01:20", timeUtil.timeToString(time));
    }

    @Test
    public void secondsWithHoursConversion() {
        Duration time = Duration.seconds(3670);
        assertEquals("01:01:10", timeUtil.timeToString(time));
    }
}
