## Timestamped, the trilogy ##

We herewith begin a saga about monitoring with this series entitled <i>"Timestamped: a trilogy in a few parts,"</i> this being the first part, where we introduce a map to count key things, and ensure we can sort it by its integer values.

We will be analysing logs in this unwinding series. Ultimately we gonna hook up a remote Log4j appender to digest our logs to gather stats, and make some judgement calls as to the rapidly changing status of our app.

### Counter Map ###

<img src='http://upload.wikimedia.org/wikipedia/commons/1/12/Gnome-appointment-new.svg' />

We herewith begin a saga about monitoring with this series entitled "Timestamped: a trilogy in a few parts," this being the first part, where we introduce a map to count key things, and ensure we can order it by its integer values.

<p>We will be analysing logs in this unwinding series. Ultimately we gonna hook up a remote Log4j appender to digest our logs in order to gather stats, and make some judgement calls as to the rapidly changing status of our app.<br>
<br>
<p>As you can imagine we will need to count so many key things like the number of successes and failures of one thing or another. For this purpose, we introduce the following so-called "counter map."<br>
<br>
<pre><code>public class IntegerCounterMap&lt;K&gt; extends TreeMap&lt;K, Integer&gt; {<br>
    private int totalCount = 0;<br>
<br>
    public int getInt(K key, int defaultValue) {<br>
        if (!containsKey(key)) {<br>
            return defaultValue;<br>
        } else {<br>
            return get(key);<br>
        }<br>
    }<br>
<br>
    public int getInt(K key) {<br>
        return getInt(key, 0);<br>
    }<br>
    <br>
    public void add(K key, int augend) {<br>
        totalCount += augend;<br>
        put(key, new Integer(getInt(key) + augend));<br>
    }<br>
    <br>
    public void increment(K key) {<br>
        add(key, 1);<br>
    }<br>
<br>
    public int getTotalCount() {<br>
        return totalCount;<br>
    }<br>
<br>
    public int calculateTotalCount() {<br>
        int total = 0;<br>
        for (K key : keySet()) {<br>
            total += getInt(key);<br>
        }<br>
        return total;<br>
    }<br>
    <br>
    public LinkedList&lt;K&gt; descendingValueKeys() {<br>
        return Maps.descendingValueKeys(this);<br>
    }<br>
}<br>
</code></pre>

<p>where since we are often counting things one by one e.g. as we digest log messages, we provide that <tt>increment()</tt> convenience method.<br>
<br>
Our typical key type is <tt>String</tt> e.g. the name of something we are counting e.g. <tt>"ERROR"</tt>.<br>
<br>
<pre><code>    @Test<br>
    public void testCounterMap() {<br>
        IntegerCounterMap&lt;String&gt; counterMap = new IntegerCounterMap();<br>
        counterMap.increment("INFO");<br>
        counterMap.increment("ERROR");<br>
        counterMap.increment("ERROR");<br>
        Assert.assertEquals(100*counterMap.getInt("ERROR")/counterMap.getTotalCount(), 66);<br>
        Assert.assertEquals(counterMap.getInt("WARN"), 0);<br>
        Assert.assertEquals(counterMap.getInt("INFO"), 1);<br>
        Assert.assertEquals(counterMap.getInt("ERROR"), 2);<br>
        Assert.assertEquals(counterMap.size(), 2);<br>
        Assert.assertEquals(counterMap.getTotalCount(), 3);<br>
        Assert.assertEquals(counterMap.getTotalCount(), counterMap.calculateTotalCount());<br>
   }<br>
</code></pre>

<p>where <tt>getTotalCount()</tt> is used to get a percentage of the total e.g. a 66% error rate, <i>D'oh!</i>

<p>Note that since WARN is not incremented like the others, it's not put into the map, and so the <tt>size</tt> of map is only the two keys, namely INFO and ERROR.<br>
<br>
<p>We have a <tt>descendingValueKeys()</tt> method for when we wanna display counters in descending order, to see the biggest numbers and/or worst culprits. This delegates to the following util class.<br>
<br>
<pre><code>public class Maps {  <br>
<br>
    public static &lt;K, V extends Comparable&gt; LinkedList&lt;K&gt; descendingValueKeys(Map&lt;K, V&gt; map) {<br>
        return keyLinkedList(descendingValueEntrySet(map));<br>
    }<br>
    <br>
    public static &lt;K, V extends Comparable&gt; NavigableSet&lt;Entry&lt;K, V&gt;&gt; <br>
            descendingValueEntrySet(Map&lt;K, V&gt; map) {<br>
        TreeSet set = new TreeSet(new Comparator&lt;Entry&lt;K, V&gt;&gt;() {<br>
<br>
            @Override<br>
            public int compare(Entry&lt;K, V&gt; o1, Entry&lt;K, V&gt; o2) {<br>
                return o2.getValue().compareTo(o1.getValue());<br>
            }<br>
        });<br>
        set.addAll(map.entrySet());<br>
        return set;<br>
    }<br>
<br>
    public static &lt;K, V&gt; LinkedList&lt;K&gt; keyLinkedList(NavigableSet&lt;Entry&lt;K, V&gt;&gt; entrySet) {<br>
        LinkedList&lt;K&gt; keyList = new LinkedList();<br>
        for (Map.Entry&lt;K, V&gt; entry : entrySet) {<br>
            keyList.add(entry.getKey());<br>
        }<br>
        return keyList;<br>
    }<br>
<br>
    public static &lt;K, V extends Comparable&gt; V getMinimumValue(Map&lt;K, V&gt; map) {<br>
        return getMinimumValueEntry(map).getValue();<br>
    }<br>
    ...<br>
}<br>
</code></pre>

<p>where courtesy of a <tt>TreeMap</tt>, we sort the map's entries by value, and put the thus ordered keys into a <tt>LinkedList</tt> to iterate over e.g. in a <tt>for</tt> each loop.<br>
<br>
<p>See also <a href='http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java'>stackoverflow.com</a> which helped me with that.<br>
<br>
<p>Let's test this ordering.<br>
<br>
<pre><code>    @Test<br>
    public void testDescendingMap() {<br>
        IntegerCounterMap&lt;String&gt; counterMap = new IntegerCounterMap();<br>
        counterMap.add("BWARN", 0);<br>
        counterMap.add("DEBUG", 1000000);<br>
        counterMap.add("ERROR", 5000);<br>
        counterMap.add("INFO", 1);<br>
        Assert.assertEquals(counterMap.size(), 4);<br>
        Assert.assertEquals(counterMap.descendingValueKeys().getFirst(), "DEBUG");<br>
        Assert.assertEquals(counterMap.descendingValueKeys().getLast(), "BWARN");<br>
        Assert.assertEquals(Maps.getMinimumValue(counterMap).intValue(), 0);<br>
        Assert.assertEquals(Maps.getMaximumValue(counterMap).intValue(), 1000000);<br>
        for (String key : Maps.descendingValueKeys(counterMap)) {<br>
            System.out.printf("%d %s\n", counterMap.get(key), key);<br>
        }<br>
     }<br>
</code></pre>

<p>where we use "BWARN" instead of "WARN" to be sure we aren't picking up the natural ordering. (Yes, that was hiding a bug that bit me in the bum!)<br>
<br>
<p>After some DEBUG'ing (with the help of a lot logging), we eventually get what we would have expected...<br>
<br>
<pre><code>1000000 DEBUG<br>
1001 ERROR<br>
1 INFO<br>
0 BWARN<br>
</code></pre>

<p>As so often seems to be the case, we have a million DEBUG messages, a thousand and one ERRORs, with very little INFO to go on, and no bloody warning! <i>"Put that in your internet, put that in your twitter right there."</i>

<h2>Resources</h2>

<a href='https://code.google.com/p/vellum/'>https://code.google.com/p/vellum/</a> - where i will collate these articles and their code.<br>
<br>
<h2>Coming soon</h2>

<p>In the next installment, we'll properly introduce the namesake of this series, a so-called <tt>Timestamped</tt> interface, for our "time series" or what-have-you.<br>
<br>
<pre><code>public interface Timestamped {<br>
    public long getTimestampMillis();    <br>
}<br>
</code></pre>
