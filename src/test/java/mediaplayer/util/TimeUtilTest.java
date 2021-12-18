package mediaplayer.util;

import javafx.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeUtilTest {
    private TimeFormatter timeUtil;

    @BeforeEach
    public void setUp() {
        timeUtil = new TimeFormatter();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testFormatTimeShouldReturnTimeFormatWithoutHoursWhenDurationIsShorterThanAnHour() {
        Duration time = Duration.minutes(45).add(Duration.seconds(10));
        assertEquals("45:10", timeUtil.formatTime(time));
    }

    @Test
    public void testFormatTimeShouldReturnTimeFormatWithHoursWhenDurationIsLongerThanAnHour() {
        Duration time = Duration.hours(2).add(Duration.minutes(30).add(Duration.seconds(40)));
        assertEquals("02:30:40", timeUtil.formatTime(time));
    }
}
