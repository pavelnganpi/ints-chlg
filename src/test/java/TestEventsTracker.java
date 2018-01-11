import exeptions.InvalidInputException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestEventsTracker {
    private EventsTracker eventsTracker;

    @Before
    public void beforeEach(){
        eventsTracker = new EventsTracker();
    }

    @Test
    public void testIsExpiredReturnsTrue() {
        int secondsOld = 5 * 60 * 1001;
        assertTrue(eventsTracker.isExpired(System.currentTimeMillis() - secondsOld));
    }

    @Test
    public void testIsExpiredReturnsFalse() {
        int secondsOld = 5 * 60 * 1000;
        assertFalse(eventsTracker.isExpired(System.currentTimeMillis() - secondsOld));
    }

    @Test
    public void testTrackRecordsEventsAccurately() throws Exception {
        eventsTracker.track();
        eventsTracker.track();
        eventsTracker.track();
        eventsTracker.track();
        eventsTracker.track();
        eventsTracker.track();

        Thread.sleep(1000);
        eventsTracker.track();
        eventsTracker.track();
        Thread.sleep(1000);

        eventsTracker.track();
        eventsTracker.track();
        eventsTracker.track();
        Thread.sleep(1000);

        eventsTracker.track();
        eventsTracker.track();
        eventsTracker.track();
        eventsTracker.track();

        assertEquals(4, eventsTracker.getEventsCount(1));
        assertEquals(15, eventsTracker.getEventsCount(20));
        eventsTracker.getEventsCount(90);
    }

    @Test(expected = InvalidInputException.class)
    public void testTrackThrowsExceptionForSecsGreaterThanMaxSeconds() throws Exception {
        int FIVE_MINUTES = 5 * 60 * 1000;
        eventsTracker.getEventsCount(FIVE_MINUTES + 1);
    }

    @Test(expected = InvalidInputException.class)
    public void testTrackThrowsExceptionForSecsLessThanOne() throws Exception {
        eventsTracker.getEventsCount(0);
    }
}
