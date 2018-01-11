import exeptions.InvalidInputException;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * => Design is using an array of size 300, where each index represents a second in the
 * last 5 minutes interval. The value at index 0 represents the number of events that occurred in
 * the last second within the last 5 minutes. So if 5 events occur all at once in the last second,
 * ARRAY[0] = 5. When a second goes by, ARRAY[0] = 0 if no event occurred in the last second. This will mean
 * ARRAY will look like this after 2 seconds [0,5,0,0,0,0,0,0,0,.....]. Hence finding event count is last x seconds is
 * constant time. Will just have to take the sum of values from ARRAY[0] -> ARRAY[x];
 *
 * => This design trades off faster get events count in last x seconds for accuracy. This means the number of events in last
 * x seconds might not be 100% accurate. But the accuracy is still very good enough to consider.
 * A design for 100% accuracy to get events count in last x seconds will have to trade-off space and time.
 *
 * => A synchronized list is used to to make ARRAY thread safe. Hence only 1 operations can happen on ARRAY at a time. This implies
 * tracking and managing ARRAY cant occur at once. This enables data consistency in ARRAY.
 */
public class EventsTracker {

    private static final int FIVE_MINUTES = 5 * 60 * 1000;
    private static List<Integer> ARRAY;

    EventsTracker() {
        ARRAY = Collections.synchronizedList(Arrays.asList(new Integer[(FIVE_MINUTES / 1000) - 1])); // fixed size = number of seconds in FIVE_MINUTES, hence 300
        initializeList(ARRAY);
        manageArray();
    }

    /**
     * Tracks events as they happen. Increments the value of the first index of
     * Array signifying that an event just occurred
     */
    public void track() {
        ARRAY.set(0, ARRAY.get(0) + 1);
    }

    /**
     * Retrieves event count that occurred in the last {secs} seconds. At worst case(getting events count in last 5 minutes(300s))
     * this is a constant time operation given that the size of array is 300 which is a relatively small number when it comes to computation.
     * @param secs number of seconds
     * @return event count that occurred in last {secs} seconds
     * @throws InvalidInputException thrown if {secs} > FIVE_MINUTES or < 1
     */
    public int getEventsCount(int secs) {
        if (secs > FIVE_MINUTES || secs <=0) {
            throw new InvalidInputException("numbers of seconds exceeds " + FIVE_MINUTES + " or is less than 1");
        }
        int target = secs - 1; // since array index starts from 0.
        int count = 0;
        for(int i = target; i >=0; i--) {
            count += ARRAY.get(i);
        }
        return count;
    }

    /**
     * Manages ARRAY. Runs every second and shifts values to the right. Removes events that occurred more than
     * FIVE_MINUTES ago and starts new events at 0 with value 0;
     * This helps to keep the latest number of events for the past FIVE_MINUTES in sync and updated
     */
    private void manageArray() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable runnable = this::shiftToRight;
        executor.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Checks if a timestamp is expired. That is if timestamp is more than FIVE_MINUTES old
     * @param timestamp timestamp to validate
     * @return boolean if timestamp is expired or not
     */
    public boolean isExpired(long timestamp) {
        return System.currentTimeMillis() - timestamp > FIVE_MINUTES;
    }

    /**
     * Shifts elements in ARRAY one place to the right. Element at end of
     * ARRAY becomes non existent. Element at start of ARRAY becomes 0.
     */
    private void shiftToRight() {
        synchronized (ARRAY) {
            for(int i = ARRAY.size() - 1; i > 0; i--) {
                ARRAY.set(i, ARRAY.get(i-1));
            }
            if (ARRAY.size() > 0) ARRAY.set(0, 0);
        }
    }

    /**
     * Initializes the list to all 0's
     * @param list List to initialise
     */
    private void initializeList(List<Integer> list) {
        for(int i = 0; i < list.size(); i++) {
            list.set(i,0);
        }
    }
}
