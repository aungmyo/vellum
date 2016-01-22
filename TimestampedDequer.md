<h1>Timestamped Dequer</h1>

We herewith continue the no-hit wonder <i>"Timestamped: a trilogy in a few parts,"</i> this being the second part, where we introduce an interface for so-called <tt>Timestamped</tt> records, and use an <tt>ArrayDeque</tt> to gather them, with a time-based capacity.

<img src='http://upload.wikimedia.org/wikipedia/commons/1/12/Gnome-appointment-new.svg' />

We will be analysing logs in this unwinding series. Ultimately we gonna hook up a remote Log4j appender to digest our logs to gather stats, and make some judgement calls as to the rapidly changing status of our app.

Without further ado, I give you the namesake interface of this series.
```
public interface Timestamped {
    public long getTimestamp();    
}
```
which returns the timestamp in "millis" ala <tt>System.currentTimeMillis()</tt>.

Also take an adapter for Log4j's <tt>LoggingEvent</tt>.
```
public class TimestampedLoggingEventAdapter implements Timestamped {
    LoggingEvent loggingEvent;

    public TimestampedLoggingEventAdapter(LoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
    }

    @Override
    public long getTimestamp() {
        return loggingEvent.getTimeStamp();
    }
}
```
And a generic wrapped element.
```
public class TimestampedElement<T> implements Timestamped, Comparable<Timestamped> {
    T element;
    long timestamp;

    public TimestampedElement(T element, long timestamp) {
        this.element = element;
        this.timestamp = timestamp;
    }

    public T getElement() {
        return element;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Timestamped other) {
        if (timestamp < other.getTimestamp()) return -1;
        if (timestamp > other.getTimestamp()) return 1;
        else return 0;
    }    
}
```

where we implement <tt>compareTo()</tt> for <a href='http://docs.oracle.com/javase/6/docs/api/java/lang/Comparable.html'>natural ordering</a> by the timestamp.

Since duplicate timestamps are possible i.e. where two or more events occur at the same millisecond, and indeed duplicate log messages at the same time, we forgo implementing <tt>hashCode()</tt> and <tt>equals()</tt>. Imagine we add such elements to a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/Set.html'><tt>Set</tt></a>, whose javadoc describes it thus:
<blockquote>
<blockquote><tt>Set</tt> - A collection that contains no duplicate elements. More formally, sets contain no pair of elements e1 and e2 such that e1.equals(e)<br>
</blockquote>
Therefore we defer to the default <tt>hashCode()</tt> and <tt>equals()</tt> from <tt>Object</tt>, which are based on object address reference.</blockquote>

We might construct a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/SortedSet.html'><tt>SortedSet</tt></a> of <tt>Timestamped</tt> elements.
<blockquote>
<blockquote><tt>SortedSet</tt> - The elements are ordered using their natural ordering, or by a Comparator typically provided at sorted set creation time.<br>
</blockquote></blockquote>

So if we have not implemented <tt>compareTo()</tt>, then we will need a comparator.
```
public class TimestampedComparator implements Comparator<Timestamped> {

    @Override
    public int compare(Timestamped o1, Timestamped o2) {
        if (o1.getTimestamp() < o2.getTimestamp()) return -1;
        if (o1.getTimestamp() > o2.getTimestamp()) return 1;
        else return 0;
    }    
}
```

<img src='http://jroller.com/evanx/resource/bicycle-ghost-deck-white-256.jpg' align='right' />
Our inclination might be collect <tt>Timestamped</tt> elements in a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/List.html'><tt>List</tt></a>, or a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/Queue.html'><tt>Queue</tt></a> perhaps.
<blockquote>
<blockquote><tt>Queue</tt> - A collection designed for holding elements prior to processing.<br>
</blockquote>
That sounds rather appropriate for our digestive purposes, to find the ghost in the machine.</blockquote>

<h2>Deque collector</h2>

So let's introduce the namesake of this article, a collector of timestamped thingies - a <a href='http://en.wikipedia.org/wiki/Circular_buffer'>circular buffer</a>, some might call it - and impose a time-based capacity thereupon.

So we use the <a href='http://docs.oracle.com/javase/6/docs/api/java/util/Deque.html'>java.util.Deque</a> found in Java 1.6, courtesy of those most excellent gentlemen, Doug Lea and Josh Bloch. Its javadoc describes it thus:
<blockquote>
<blockquote><tt>Deque</tt> - A linear collection that supports element insertion and removal at both ends. The name deque is short for "double ended queue" and is usually pronounced "deck."<br>
</blockquote>
We use the efficient <a href='http://docs.oracle.com/javase/6/docs/api/java/util/ArrayDeque.html'><tt>ArrayDeque</tt></a> implementation.<br>
<blockquote>
<tt>ArrayDeque</tt> - This class is likely to be faster than Stack when used as a stack, and faster than LinkedList when used as a queue.<br>
</blockquote>
Fantastic. Let's get us some of that.<br>
<pre><code>public class TimestampedDequer&lt;T extends Timestamped&gt;  {<br>
    long capacityMillis;<br>
    long lastTimestamp;<br>
    ArrayDeque&lt;T&gt; deque = new ArrayDeque();<br>
    <br>
    public TimestampedDequer(long capacityMillis) {<br>
        this.capacityMillis = capacityMillis;<br>
    }<br>
    <br>
    public synchronized void addLast(T element) {<br>
        if (element.getTimestamp() == 0 || element.getTimestamp() &lt; lastTimestamp) {<br>
            deque.clear(); // throw our toys out the cot exception<br>
        } else {<br>
            lastTimestamp = element.getTimestamp();<br>
            prune(lastTimestamp);<br>
            deque.addLast(element);<br>
        }<br>
    }<br>
<br>
    private void prune(long lastTimestamp) {<br>
        while (deque.size() &gt; 0 &amp;&amp; <br>
                deque.getFirst().getTimestamp() &lt;= lastTimestamp - capacityMillis) {<br>
            deque.removeFirst();<br>
        }<br>
    }<br>
</code></pre></blockquote>

<img src='http://jroller.com/evanx/resource/arcane-deck-white-back-300.jpg' align='left' />
where we compose an <tt>ArrayDeque</tt> and <tt>synchronize</tt> it "externally" for our purposes, considering that it will be digesting log records continually, whilst being under interrogation by RMX, HTTP requests and what-not.

When we add an element onto the tail, we <tt>prune()</tt> expired elements from the head. Observe that the above implementation assumes that elements are added in chronological order. However we expect our host's time to be adjusted by <tt>NTP</tt> occassionally - hence we <tt>clear()</tt> i.e. when we encounter an eventuality that we don't play nicely with.

If we are digesting logs from multiple servers or what-have-you, the above so-called "dequer" aint gonna work, baby - it's gonna come up empty, baby. Don't shuffle this deque, baby. <i>(As Elvis might have said.)</i> We'll deal with such a handful another time.

Now, in order to analyse the contents for the desired interval, we take a <tt>snapshot()</tt>.
```
    public synchronized Deque<T> snapshot(long lastTimestamp) {
        prune(lastTimestamp);
        return deque.clone();
    }
```
which returns a defensive copy, and so is a relatively costly operation. Perhaps you could recommend an alternative strategy? Perhaps we will implement a special concurrent deque implementation in a future episode, as an exercise - taking inspiration from that <a href='http://mechanitis.blogspot.com/2011/06/dissecting-disruptor-whats-so-special.html'>Disruptor</a> thingymajig, perchance, as well as <tt>ArrayDeque</tt> itself - one that supports aggregating from multiple servers, methinks.

Another use-case is to get the tail i.e. the latest so-many elements, for informational purposes e.g. to display via a servlet, or attach to an email alert.
```
    public synchronized Deque<T> tail(int size) {
        Deque tail = new ArrayDeque();
        Iterator<T> it = deque.descendingIterator();
        for (int i = 0; i < size && it.hasNext(); i++) {
            tail.addFirst(it.next());
        }
        return tail;
    }    
```
where we use <tt>descendingIterator()</tt> to read from the tail of the deque, and <tt>addFirst()</tt> to rectify the order.

Let's test this thing.
```
public class TimestampedDequerTest  {
    TimestampedDequer<TimestampedElement> dequer = new TimestampedDequer(capacityMillis);
    boolean verbose = false;
    
    private void addLast() {
        long timestamp = System.currentTimeMillis();
        String value = "record at " + timestamp;
        dequer.addLast(new TimestampedElement(value, timestamp));
        if (verbose) {
            System.out.println(value);
        }
    }

    @Test
    public void test() throws Exception {
        check();
        check();
    }
```
where we <tt>check()</tt> twice... just to make sure. Of <tt>prune()</tt>, that is.
```
    private void check() throws Exception {
        Thread.sleep(capacityMillis);
        Assert.assertEquals(0, dequer.snapshot(System.currentTimeMillis()).size());
        Assert.assertEquals(0, dequer.tail(4).size());
        addLast();
        Assert.assertEquals(1, dequer.tail(4).size());
        Assert.assertEquals(1, dequer.snapshot(System.currentTimeMillis()).size());
        Thread.sleep(capacityMillis/2);
        Assert.assertEquals(1, dequer.snapshot(System.currentTimeMillis()).size());
        Assert.assertEquals(1, dequer.tail(4).size());
        addLast();
        Assert.assertEquals(2, dequer.tail(4).size());
        Assert.assertEquals(2, dequer.snapshot(System.currentTimeMillis()).size());
        Thread.sleep(capacityMillis/2);
        Assert.assertEquals(2, dequer.tail(4).size());
        Assert.assertEquals(1, dequer.snapshot(System.currentTimeMillis()).size());
        Assert.assertEquals(1, dequer.tail(4).size());
    }
```
<img src='http://jroller.com/evanx/resource/bicycle-guardians-deck-back-crop.jpg' align='right' />
where expect the final <tt>snapshot()</tt> to loose an element to <tt>prune()</tt>'ing, given the two half <tt>capacityMillis</tt> sleeps since the first <tt>addList()</tt>.

Considering that the purpose of this <tt>Timestamped</tt> series is reducing information overload, we'll tail off here for now, and leave the "heavy-dropping" for next week, namely, testing with threads.

Thereafter, we'll see about using our <tt>TimestampedDequer</tt> to analyse the latest <tt>deque</tt> of logs e.g. every minute, to detect when our app might be coming down like a house of cards.

<h3>Credits</h3>

Thanks to my colleague Zach Visagie at BizSwitch.net, for his kind reviews and indispensible input!
