<h4>Safe date format</h4>

When someone once told me that <a href='http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html'><tt>SimpleDateFormat</tt></a> is not thread-safe, at some stage we incorporated a trivial <tt>synchronized</tt> <a href='http://code.google.com/p/vellum/source/browse/trunk/src/vellum/datatype/SafeDateFormat.java'>wrapper</a> for it - athough we should be using Apache Commons <a href='http://commons.apache.org/lang/api-2.5/org/apache/commons/lang/time/FastDateFormat.html'><tt>FastDateFormat</tt></a>, hey?

```
public class SafeDateFormat {

   SimpleDateFormat dateFormat;
   String pattern;

   public SafeDateFormat(String pattern) {
      this.pattern = pattern;
      dateFormat = new SimpleDateFormat(pattern);
   }

   public String getPattern() {
      return pattern;
   }

   public synchronized String format(Date date) {
      if (date == null) {
         return "";
      }
      return dateFormat.format(date);
   }

   public synchronized Date parse(String string){
       return parse(string, null);
       
   }
   
   public synchronized Date parse(String string, Date defaultValue){
      if (string == null || string.isEmpty()) {
         return defaultValue;
      }
      if (string.length() > pattern.length()) {
          string = string.substring(0, pattern.length());
      }
        try {
            return dateFormat.parse(string);
        } catch (ParseException e) {
            throw new ParseRuntimeException(string, e);
        }
   }
}
```
where this <tt>parse()</tt> truncates the timestamp string to the length of the pattern we are trying to parse.

We use the following default date and timestamp formats, just like our database.
```
public class DefaultDateFormats {
    public static final SafeDateFormat dateFormat = new SafeDateFormat("yyyy-MM-dd");
    public static final SafeDateFormat timestampFormat = new SafeDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    public static final SafeDateFormat timeFormat = new SafeDateFormat("HH:mm:ss,SSS");
}
```
