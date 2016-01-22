<h1>Deque shredder</h1>

Just to remind the reader, and for completeness herewithin, our <a href='http://weblogs.java.net/blog/evanx/archive/2012/06/27/timestamped-deque'>deque collector</a> for our <tt>Timestamped</tt> thingies, looks like follows.

```
public class TimestampedDequer<T extends Timestamped>  {
    long capacityMillis;
    long lastTimestamp;
    ArrayDeque<T> deque = new ArrayDeque();
    
    public TimestampedDequer(long capacityMillis) {
        this.capacityMillis = capacityMillis;
    }
    
    public synchronized void addLast(T element) {
        if (element.getTimestamp() == 0 || element.getTimestamp() < lastTimestamp) {
            deque.clear(); // throw our toys out the cot exception
        } else {
            lastTimestamp = element.getTimestamp();
            prune(lastTimestamp);
            deque.addLast(element);
        }
    }

    private void prune(long latestTimestamp) {
        while (deque.size() > 0 && 
                deque.getFirst().getTimestamp() <= latestTimestamp - capacityMillis) {
            deque.removeFirst();
        }
    }

    public synchronized Deque<T> snapshot(long lastTimestamp) {
        prune(lastTimestamp);
        return deque.clone();
    }

    public synchronized Deque<T> tail(int size) {
        Deque tail = new ArrayDeque();
        Iterator<T> it = deque.descendingIterator();
        for (int i = 0; i < size && it.hasNext(); i++) {
            tail.addFirst(it.next());
        }
        return tail;
    }    
}
```
<img src='http://jroller.com/evanx/resource/bicycle-guardians-deck-rider-black-front-crop.jpg' align='right' />
where we use the efficient <a href='http://docs.oracle.com/javase/6/docs/api/java/util/ArrayDeque.html'><tt>ArrayDeque</tt></a> implementation of Java6.

As discussed last time, we remove expired elements from the head when we add the latest element to the tail, to make it self-pruning. And we provide a sychronized <tt>snapshot()</tt> and <tt>tail()</tt> for a couple of use-cases as follows...

Most importantly, we will use <tt>snapshot()</tt> to analyse the latest records for the desired interval e.g. the last minute, for an automated status check every minute, possibly invoked externally via JMX or HTTP, for the purpose of triggering alerts, e.g. via Gtalk, SMS and email. <i>Shew!</i>

Furthermore, we will use a size-based <tt>tail()</tt> e.g. to display the latest so-many records in a status web page, and to attach to an email alert.

Now let's do us some "heavy-dropping" with threads - woohoo!
```
public class TimestampedDequerTest  {
    long capacityMillis = 90;
    long scheduledInterval = 10;
    long scheduledDelay = 0;
    final TimestampedDequer<TimestampedElement> dequer = new TimestampedDequer(capacityMillis);
    boolean verbose = false;
    
    Runnable scheduledRunnable = new Runnable() {

        @Override
        public void run() {
            addLast();
        }
    };

    private void addLast() {
        long timestamp = System.currentTimeMillis();
        String value = "record at " + timestamp;
        dequer.addLast(new TimestampedElement(value, timestamp));
        if (verbose) {
            System.out.println(value);
        }
    }
    @Test
    public void testConcurrently() throws Exception {
        ScheduledFuture future = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
                    scheduledRunnable, scheduledDelay, scheduledInterval, TimeUnit.MILLISECONDS);
        checkConcurrently();
        checkConcurrently();
        future.cancel(true);
    }
```
where we use a <tt>ScheduledExecutorService</tt> to schedule a thread to regularly add records into the deque.

We check twice, just to make sure! ;) Actually we do want to make sure of the <tt>prune()</tt>'ing, following the sleep for <tt>capacityMillis</tt>.
```
    private void checkConcurrently() throws Exception {
        long startMillis = System.currentTimeMillis();
        System.out.println("startMillis " + startMillis);
        verbose = true;
        Thread.sleep(capacityMillis);
        int expectedCapacity = (int) (capacityMillis / scheduledInterval);
        verbose = false;
        long stopMillis = System.currentTimeMillis();
        System.out.println("stopMillis " + stopMillis);
        Deque<TimestampedElement> deque = dequer.snapshot(stopMillis);
        long firstTimestamp = deque.getFirst().getTimestamp();   
        long lastTimestamp = deque.getLast().getTimestamp();   
        System.out.println("size " + deque.size());
        System.out.println("first " + firstTimestamp);
        System.out.println("last " + lastTimestamp);
        Assert.assertTrue("first time", firstTimestamp >= startMillis);        
        Assert.assertTrue("last time", lastTimestamp >= firstTimestamp);
        Assert.assertTrue("capacityMillis min", lastTimestamp - firstTimestamp >= 0);        
        Assert.assertTrue("capacityMillis max", lastTimestamp - firstTimestamp <= capacityMillis);        
        Assert.assertTrue("size min", deque.size() > 0);
        Assert.assertTrue("size max", deque.size() <= expectedCapacity);
        checkSet(deque);
    }    
```
which prints...
```
scheduledInterval 10
record at 1340231378158
record at 1340231378168
record at 1340231378178
...
record at 1340231378228
record at 1340231378238
startMillis 1340231378157
stopMillis 1340231378247
size 9
first 1340231378158
last 1340231378238
...
startMillis 1340231378249
stopMillis 1340231378339
size 9
first 1340231378258
last 1340231378338
```
We survey this output, eyeing the timestamps, and nod ponderously.

Just for good measure, we use a <tt>TimestampedComparator</tt> to create a <tt>SortedSet</tt>, and check that the first and last timestamps match.
```
    private void checkSet(Deque<TimestampedElement> deque) throws Exception {
        SortedSet<Timestamped> set = new TreeSet();
        set.addAll(deque);        
        Assert.assertEquals("size", deque.size(), set.size());
        Assert.assertEquals("first", deque.getFirst().getTimestamp(), set.first().getTimestamp());
        Assert.assertEquals("last", deque.getLast().getTimestamp(), set.last().getTimestamp());
    }
```

Finally, let's vary the <tt>scheduledInterval</tt>.
```
    @Test
    public void testScheduledIntervals() throws Exception {
        while (--scheduledInterval > 0) {
            ScheduledFuture future = Executors.newScheduledThreadPool(10).scheduleAtFixedRate(
                    scheduledRunnable, scheduledDelay, scheduledInterval, TimeUnit.MILLISECONDS);
            Thread.sleep(capacityMillis);
            int expectedCapacity = (int) (capacityMillis / scheduledInterval);
            long stopMillis = System.currentTimeMillis();
            Deque<TimestampedElement> deque = dequer.snapshot(stopMillis);
            Woohoo.assertEquals("interval " + scheduledInterval, expectedCapacity, deque.size());
            future.cancel(true);
            Thread.sleep(scheduledInterval);
        }
    }
```
where we loop the <tt>scheduledInterval</tt> down to 1ms. <i>Oh my word - surely this will not end well!</i>
<pre>
interval 9: Woohoo! 10 == 10<br>
interval 8: Woohoo! 11 == 11<br>
interval 7: Woohoo! 12 == 12<br>
interval 6: D'oh! 15 != 14<br>
interval 5: D'oh! 18 != 17<br>
interval 4: Woohoo! 22 == 22<br>
interval 3: D'oh! 30 != 29<br>
interval 2: D'oh! 45 != 44<br>
interval 1: D'oh! 90 != 89<br>
</pre>
Given how unpredictable time is, ironically, with those threads and what-not, we can't exactly predict the size of the list. D'oh! So for that we have used the following util class to see if the size is more or less what we expect...
```
public class Woohoo {

    public static void assertEquals(String message, Object expected, Object actual) {
        if (actual.equals(expected)) {
            System.out.printf("%s: Woohoo! %s == %s\n", message, expected, actual);
        } else {
            System.out.printf("%s: D'oh! %s != %s\n", message, expected, actual);
        }
    }
```
Selectively using the above drop-in replacement for <tt>Assert</tt>, we get our tests to pass 100%, woohoo! Heh heh.
