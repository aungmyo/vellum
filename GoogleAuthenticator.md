<h4>What is this Google Authenticator?</h4>

The <a href='http://code.google.com/p/google-authenticator'>Google Authenticator</a> is a client-side implementation of a <a href='http://en.wikipedia.org/wiki/Time-based_One-time_Password_Algorithm'>"time-based one-time password algorithm" (TOTP)</a>, in particular <a href='http://tools.ietf.org/html/rfc6238'>IETF RFC6238</a>.

Each account we configure on our Google Authenticator has a stored secret, shared with some web account. The app displays the time-based code for each secret, which changes every 30 seconds. This code is computed from the number of 30 second intervals since the UNIX time epoch, hashed with that shared secret using the <a href='http://en.wikipedia.org/wiki/Hash-based_message_authentication_code'>HMAC-SHA1 algorithm</a>. <i>Sooo simple! :)</i>

<img src='http://jroller.com/evanx/resource/google-auth-android.png' />

We see on http://code.google.com/p/google-authenticator that it also supports the counter-based <a href='http://en.wikipedia.org/wiki/HOTP'>HMAC-Based One-time Password (HOTP) algorithm</a> specified in <a href='https://tools.ietf.org/html/rfc4226'>RFC 4226</a>, which we ignore here and leave for another article perhaps.

So the question arises, can we support Google Authenticator clients for multi-factor authentication on websites that we build? Let's explore what this would entail...

<h4>Random secret in Base32</h4>

For each user account, we generate a random secret for the Google Authenticator client.

```
    void test() {
        byte[] buffer = new byte[10];
        new SecureRandom().nextBytes(buffer);
        String secret = new String(new Base32().encode(buffer));
        System.out.println("secret " + secret);
    }
```
where the secret is a 10 byte random number, which is encoded using <a href='http://en.wikipedia.org/wiki/Base32'>Base32</a> (<a href='http://tools.ietf.org/html/rfc3548'>RFC3548</a>).

This produces a string that is 16 characters long, in particular the characters A-Z and 2-7.
```
secret OVEK7TIJ3A3DM3M6
```

The user creates a new account on their Google Authenticator app, entering this secret.

<img src='http://jroller.com/evanx/resource/chrome-gauth-add.png' />

For entering codes into mobiles, we find that mixing alpha and numeric is not uber-convenient, and so one might reorder that secret e.g. OVEKTIJADMM73336, albeit thereby loosing some of its randomness. But take any of my suggestions with a pinch of salt. Heh heh, a crypto pun!

<h4>QR code</h4>

Alternatively, one can generate a QR barcode e.g. using the Google Chart API service, for the user to scan into their Google Authenticator app.
```
    String secret = "OVEK7TIJ3A3DM3M6";
    String user = "evanx";
    String host = "beethoven";

    void test() throws Exception {
        System.out.println(getQRBarcodeOtpAuthURL(user, host, secret));
        System.out.println(Strings.decodeUrl(getQRBarcodeURLQuery(user, host, secret)));
        System.out.println(getQRBarcodeURL(user, host, secret));
    }
    
    public static String getQRBarcodeURL(String user, String host, String secret) {
        return "http://chart.googleapis.com/chart?" + getQRBarcodeURLQuery(user, host, secret);
    }

    public static String getQRBarcodeURLQuery(String user, String host, String secret) {
        return "chs=200x200&chld=M%7C0&cht=qr&chl=" + 
                Strings.encodeUrl(getQRBarcodeOtpAuthURL(user, host, secret));
    }
   
    public static String getQRBarcodeOtpAuthURL(String user, String host, String secret) {
        return String.format("otpauth://totp/%s@%s&secret=%s", user, host, secret);
    }
```
where the QR code encodes the following URL.
```
otpauth://totp/evanx@beethoven?secret=OVEK7TIJ3A3DM3M6
```
The QR code can be rendered using the following Google Chart request.
```
http://chart.googleapis.com/chart?chs=200x200&chld=M%7C0&cht=qr&chl=
otpauth%3A%2F%2Ftotp%2Fevanx%40beethoven%26secret%3DOVEK7TIJ3A3DM3M6
```
Just for clarity, the following shows the decoded URL query.
```
chs=200x200&chld=M|0&cht=qr&chl=otpauth://totp/evanx@beethoven&secret=OVEK7TIJ3A3DM3M6
```

So we use this Google Chart service to render the QR code onto our computer screen so we can scan it into our phone's Google Authenticator app. <i>Otherwise we have to be poking those tiny buttons with our fat fingers again.</i>

http://chart.googleapis.com/chart?chs=200x200&chld=M%7C0&cht=qr&chl=otpauth%3A%2F%2Ftotp%2Fevanx@beethoven%3Fsecret%3DOVEK7TIJ3A3DM3M6

You can right-click on the above link, and scan the barcode into your Google Authenticator, to test it. <i>Wow, it works! So cool... :)</i> You might get prompted to install a barcode scanner first, which is so easy ;)

<img src='http://jroller.com/evanx/resource/google-qr.png' />

Having scanned this into our Google Authenticator app, it will display the time-varying code for this account, together with our other accounts. Woohoo!

<img src='http://jroller.com/evanx/resource/google-auth-android.png' />

<h4>Authentication</h4>

So when users login to our site, they enter the current 6-digit OTP code displayed on their phone for their account on our website, where this OTP changes every 30 seconds. To authenticate this on the server-side, first we get the current time index i.e. the number of 30s intervals since the UNIX time epoch.

```
  public static long getTimeIndex() {
    return System.currentTimeMillis()/1000/30;
  }
```

So far, soooo easy :)

We can then calculate the authentication code, for this time interval, using the user's secret.

```
  private static long getCode(byte[] secret, long timeIndex) 
          throws NoSuchAlgorithmException, InvalidKeyException {
    SecretKeySpec signKey = new SecretKeySpec(secret, "HmacSHA1");
    ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.putLong(timeIndex);
    byte[] timeBytes = buffer.array();
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(signKey);
    byte[] hash = mac.doFinal(timeBytes);
    int offset = hash[19] & 0xf;
    long truncatedHash = hash[offset] & 0x7f;
    for (int i = 1; i < 4; i++) {
        truncatedHash <<= 8;
        truncatedHash |= hash[offset + i] & 0xff;
    }
    return (truncatedHash %= 1000000);
  }
```
where...
  * The time index is put into an 8-byte array.
  * We use <tt>HmacSHA1</tt> to hash this array into 20 bytes.
  * The first nibble (4 bits) of the last byte is taken as an offset (from 0 to 15) into the 20-byte array.
  * Four bytes from the offset are then extracted, with the highest-order bit zero'ed.

So at this stage, i guess we have an unsigned zero-based 31-bit number with a maximum value of 2^31 minus 1 i.e. <tt>Integer.MAX_VALUE</tt> i.e. 2,147,483,647.

  * We take the lower 6 decimal digits as our one-time password. Voila!

Now the only problem is that the mobile's clock might be out by a minute or two (or indeed our workstation which we are using a test server). So we need to check codes for a few time indexes before and after the supposed time. Problem solved! <i>Hopefully it goes without saying that servers always have the correct time thanks to NTP, although you'd be surprised... ;)</i>

```
  public static boolean verifyCode(String secret, int code, long timeIndex, 
      int variance) throws Exception {
    byte[] secretBytes = new Base32().decode(secret);
    for (int i = -variance; i <= variance; i++) {
        if (getCode(secretBytes, timeIndex + i) == code) {
            return true;
        }
    }
    return false;
  }
```

So let's test this!

```
  long testTimeIndex = 45064605;
  int testCode = 111070;

  void test() throws Exception {
    System.out.println("time: " + getTimeIndex());
    System.out.println("code: " +  getCode(secret, getTimeIndex()));
    System.out.println("codes: " + getCodeList(secret, getTimeIndex(), 5));
    System.out.println("verify: " + verifyCode(secret, testCode, testTimeIndex, 5));
  }

  static long getCode(String secret, long timeIndex)
      throws NoSuchAlgorithmException, InvalidKeyException {
    return getCode(new Base32().decode(secret), timeIndex);
  }

  static List<Long> getCodeList(String secret, long timeIndex, int variance) 
      throws NoSuchAlgorithmException, InvalidKeyException {
    byte[] secretBytes = new Base32().decode(secret);
    List<Long> list = new ArrayList();
    for (int i = -variance; i <= variance; i++) {
      list.add(getCode(secretBytes, timeIndex + i));
    }
    return list;
  }
```

The following output is observed.

```
time: 45076085
code: 766710
codes: [192262, 720538, 629431, 92289, 937348, 766710, 74053, 425245, 738189, 469760, 486815]
verify: true
```

We can add the GAuth Chrome extension to our browser to compare, and of course the mobile app :)

<img src='http://jroller.com/evanx/resource/gauth.png' />

<h4>Multi-factor authentication</h4>

According to http://en.wikipedia.org/wiki/Multi-factor_authentication,
<blockquote>
Existing authentication methodologies involve three basic "factors":<br>
<ul><li>Something the user knows (e.g., password, PIN);<br>
</li><li>Something the user has (e.g., ATM card, smart card); and<br>
</li><li>Something the user is (e.g., biometric characteristic, such as a fingerprint).<br>
Authentication methods that depend on more than one factor are more difficult to compromise than single-factor methods.<br>
</blockquote></li></ul>

Clearly the only way to generate the correct OTP code is by having the secret key, so then if the user has entered their correct password (as the thing they "know"), as well as the correct OTP (proving that they "have" the secret), then two-factor authentication is "thus enabled" :)

Finally, the question arises how to handle the event of a user loosing their phone (and TOTP secret)? So I guess in addition to the "Forgot Password" one wants a "Lost phone" button ;) That is, to enable users to reset their TOTP secret.

<h4>Conclusion</h4>

The Google Authenticator mobile apps, and Chrome extension, et al, implement the IETF RFC6238 time-based one-time-password standard. This hashes the time since the epoch with a shared secret, using the HMAC-SHA1 algorithm, i.e. a SHA1-hash-based message authentication code.

Besides enabling multi-factor authentication for our personal Google account, we can easily employ Google Authenticator for multi-factor authentication on our websites.

We hash the number of 30s time intervals since the epoch with a secret using the HMAC-SHA1 algorithm. A slight complication is that someone's clock might be a bit out of whack, so we allow some leniency for that.

<h4>Coming up</h4>

In an upcoming article, we'll build a sample site using a <a href='http://twitter.github.com/bootstrap/getting-started.html#examples'>Twitter Bootstrap</a> template, enabling "Login with Google" via that <a href='https://developers.google.com/accounts/docs/OAuth2'>OAuth 2.0 API</a>, in addition to the usual <a href='http://code.google.com/p/vellum/source/browse/trunk/src/vellum/util/Passwords.java'>hashed and salted password</a>, and of course this T-OTP with Google Authenticator. For a sneak preview, see the so-called <a href='http://code.google.com/p/vellum/source/browse/trunk/src/crocserver/#crocserver%2Fhttphandler%2Faccess'><tt>crocserver</tt></a> under construction.

And then coming "soon" in 2013, we'll build a crypto server called <a href='http://code.google.com/p/vellum/source/browse/trunk/src/venigma/#venigma%2Fserver'><tt>venigma</tt></a> :)

<h4>Resources</h4>

https://code.google.com/p/vellum/ - where i will collate these articles and their code - see the <a href='http://code.google.com/p/vellum/source/browse/#svn%2Ftrunk%2Fsrc%2Fvellumdemo%2Ftotp'><tt>vellumdemo.totp</tt></a> package.