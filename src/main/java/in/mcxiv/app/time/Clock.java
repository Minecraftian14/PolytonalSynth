package in.mcxiv.app.time;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Clock {

    private final TimeEvent[] events;

    public Clock(TimeEvent... events) {
        this.events = events;
    }

    public void register() {
        ExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.submit(this::tickAndWait);
    }

    private void tickAndWait() {
        for (int i = 0; i < events.length; events[i++].tick()) ;
        // Hmm how do I wait?
    }

    // @formatter:off
    public static final class ClockBuilder {
        private final ArrayList<TimeEvent> events = new ArrayList<>();
        public ClockBuilder add(TimeEvent event) {events.add(event);return this;}
        public Clock build() {return new Clock(events.toArray(value -> new TimeEvent[events.size()]));}
    }
    // @formatter:on

}
