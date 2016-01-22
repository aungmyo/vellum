We present a miniscule Millis utility class for handling intervals, in milliseconds, not least because we record timestamps as per System.currentTimeMillis, i.e the number of milliseconds since the Unix epoch. As such we can skirt around the issue of the time as seen on clocks, with their time zones and calendars and what-not.

Isn't <a href='http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/TimeUnit.html'>TimeUnit</a> the best thing since sliced bread?! Indeed anything and everything from Doug Lea is always thus :)

That said, for the purposes of some <a href='http://code.google.com/p/vellum/wiki/Timestamped'><tt>Timestamped</tt></a> thingies, we find ourselves cobbling together a <tt>Millis</tt> util class.

```
public class Millis {
    
    public static long elapsedMillis(long startMillis) {
        return System.currentTimeMillis() - startMillis;
    }

    public static boolean isElapsed(long startMillis, long millis) {
        return (System.currentTimeMillis() - startMillis) > millis;
    }
    ...
```

This util class primarily deals with time <i>intervals</i>. We often express time intervals as a <tt>long</tt> value without an explicit time unit, which is then assumed to be milliseconds. Furthermore we often treat the timestamp of an event as the time interval since the Unix epoch, thanks to <tt>System.currentTimeMillis()</tt>.

Time without its time zone, like measurement without units, will cause us problems at some stage. Remember what happened to that Arianne rocket when the units got mixed up? So we have to be careful with numbers without qualification. Hence Doug Lea's introduction of <tt>TimeUnit</tt> in his superlative <tt>java.util.concurrent</tt> package, is sooo good, providing safety <i>and</i> convenience.

<h4>Time and tide</h4>

While the "epochal time" is absolute, the time of an event as recorded on our clock and calendar is relative to the <tt>TimeZone</tt> for which that clock is configured. And the problem is there are so many clocks in the world, and so few of them have the same time! ;)

<a href='http://docs.oracle.com/javase/7/docs/api/java/lang/System.html#currentTimeMillis'>System#currentTimeMillis javadocs</a> says,
<blockquote>
<blockquote>See the description of the class Date for a discussion of slight discrepancies that may arise between "computer time" and coordinated universal time (UTC).<br>
</blockquote></blockquote>

We will certainly do that, but not today!

<h4>Time conversion</h4>

Of course we often want to convert to and from millis.

```
    public static long toSeconds(long millis) {
        return millis/1000;
    }

    public static long toMinutes(long millis) {
        return millis/1000/60;
    }

    public static long toHours(long millis) {
        return millis/1000/60/60;
    }

    public static long toDays(long millis) {
        return millis/1000/60/60/24;
    }
    
    public static long fromSeconds(long seconds) {
        return seconds*1000;
    }

    public static long fromMinutes(long minutes) {
        return minutes*60*1000;
    }

    public static long fromHours(long hours) {
        return hours*60*60*1000;
    }
    
    public static long fromDays(long days) {
        return days*24*60*60*1000;
    }
```

Not exactly rocket science - but when rocket scientists get these wrong, their rockets tend to explode.

Actually the above type of interval conversions are comprehensively and beautifully handled by <tt>TimeUnit</tt>, as the following rewrite illustrates.
```
    public static long fromDays(long days) {
        return TimeUnit.DAYS.toMillis(days);
    }
```

<h4>Format</h4>

We roll in a format method.
```
    public static String format(long millis) {
        if (millis == 0) return "00:00:00,000";
        long hour = millis/Millis.fromHours(1);
        long minute = (millis % Millis.fromHours(1))/Millis.fromMinutes(1);
        long second = (millis % Millis.fromMinutes(1))/Millis.fromSeconds(1);
        long millisecond = millis % Millis.fromSeconds(1);
        return String.format("%02d:%02d:%02d,%03d", hour, minute, second, millisecond);        
    }
```
where this is used for logging and stuff, just to make <tt>millis</tt> time intervals more readable.

Let's test.

```
public class MillisTest {

    @Test
    public void testIntervalMillis() {
        Assert.assertEquals(Millis.format(1001), "00:00:01,001");
        Assert.assertEquals(Millis.format(60888), "00:01:00,888");
        Assert.assertEquals(Millis.format(3600999), "01:00:00,999");
    }    
```
But now let's do the wrong thing.
```
    @Test
    public void breakingBad() {
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date(0)));
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis())));
        System.out.println(Millis.format(System.currentTimeMillis() % Millis.fromDays(1)));
    }    
```
We see the folly of using <tt>SimpleDateFormat</tt> and pretending that a time interval is a time of day, on the day of the Epoch. When using <tt>Date</tt> you're putting your clock on the block.
```
02:00:00
22:43:50
20:43:50,437
```
where my default time zone seems to be 2 hours ahead of Greenwich.

Recall that at the moment of time on Earth that the Unix epoch happened way back around <tt>1970-01-01</tt>, actually the time was <tt>00:00:00</tt> only up there in Greenwich. So for most of us, our clocks where not <tt>00:00:00</tt> at the time of the Epoch. Gutting. In fact for a good deal of us, it wasn't even 1970 yet - it was still 1969 - yeah baby!

<h4>In parsing</h4>

We implement some parsing.
```
    public static long parse(String string) {
        int index = string.indexOf(" ");
        if (index > 0) {
            return TimeUnit.valueOf(string.substring(index + 1)).toMillis(
                Long.parseLong(string.substring(0, index)));
        } else if (string.length() >= 2 &&
                Character.isLowerCase(string.charAt(string.length() - 1)) && 
                Character.isDigit(string.charAt(string.length() - 2))) {            
            long value = Long.parseLong(string.substring(0, string.length() - 1));    
            if (string.endsWith("d")) {
                return TimeUnit.DAYS.toMillis(value);
            } else if (string.endsWith("h")) {
                return TimeUnit.HOURS.toMillis(value);
            } else if (string.endsWith("m")) {
                return TimeUnit.MINUTES.toMillis(value);
            } else if (string.endsWith("s")) {
                return TimeUnit.SECONDS.toMillis(value);
            }
        }  
        throw new ParseRuntimeException(string);
    }    
```
The test case below illustrates the functionality of this method.
```
    @Test
    public void testParse() {
        Assert.assertEquals(Millis.parse("1 SECONDS"), 1000);
        Assert.assertEquals(Millis.parse("1m"), 60000);
        Assert.assertEquals(Millis.parse("60m"), 3600000);
        Assert.assertEquals(Millis.parse("60m"), Millis.parse("1h"));
        Assert.assertEquals(Millis.parse("24h"), Millis.parse("1d"));
    }
```
So this is used for configuration settings of time intervals e.g. in our app's <tt>context.xml</tt>.
```
  <parameter name="interval" value="45s"/>
```

<h4>Conclusion</h4>

In this Timestamped series, we'll sometimes make use of a <tt>Millis</tt> convenience class which is presented here. This class is a utility for time intervals, rather than "time" per se.

We express time intervals in milliseconds, and timestamps as the time interval since the Unix epoch, in order to steer clear of time zones (and <tt>Date</tt> and <tt>Calendar</tt>), for now.

<h4>Further treatment</h4>

At some stage we should gloss over that unrepentant <a href='http://docs.oracle.com/javase/7/docs/api/java/util/Date.html'><tt>Date</tt></a>, crucial <a href='http://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html'><tt>TimeZone</tt></a>, cardinal <a href='http://en.wikipedia.org/wiki/Coordinated_Universal_Time'>UTC</a>, stupendous <a href='http://docs.oracle.com/javase/7/docs/api/java/util/Calendar.html'><tt>Calendar</tt></a>, and serendipitous <a href='http://threeten.sourceforge.net/apidocs-2012-10-25/'>JSR 310</a>.

<h4>Resources</h4>

https://code.google.com/p/vellum/ - where i collate these articles and their code - e.g. see the <a href='http://code.google.com/p/vellum/source/browse/trunk/src/vellum/datatype/Millis.java'><tt>Millis</tt></a> class.
