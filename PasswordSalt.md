This provides a long overdue update to "Password Hash" from the Enigma Prequels (2007), where that article neglected to add salt, which is embarassing for whoever wrote that article... which was unfortunately me.

We know that passwords should be hashed, and hear they should be salted.

<img src='http://www.jroller.com/evanx/resource/salt-spoon-250-border.jpg' align='left'>
<a href='http://en.wikipedia.org/wiki/SHA-2'>SHA-2</a> is recommended these days for general hashing, but we'll read that we should use computationally expensive algorithms for password hashing, so that the passwords are harder to crack. Just ask <tt>LinkedIn</tt>, who <a href='http://security.stackexchange.com/questions/15910/why-would-salt-not-have-prevented-linkedin-passwords-from-getting-cracked'>learnt this lesson a few months ago</a> (2012).<br>
<br>
The most secure passwords are no passwords, e.g. using Google Login, Facebook Connect, Mozilla Persona or what-have-you. Such an approach simplifies our implementation effort, improves security, and makes registration and login more convenient for the end-user. So that's surely the way forward for consumer sites. However for internal enterprise apps, those login services might not be suitable. Perhaps the reader can recommend an opensource identity solution which handles passwords in a PCI-compliant fashion?<br>
<br>
Having said that, since so many of us devs are lumbered with password management, let's get stuck in.<br>
<br>
<h4>Background reading</h4>

From <a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'>https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet</a>,<br>
<br>
<blockquote><i>General hashing algorithms (e.g. MD5, SHA) are not recommended for password storage. Instead an algorithm specifically designed for the purpose should be used such as bcrypt, PBKDF2 or scrypt.</i></blockquote>

From <a href='http://en.wikipedia.org/wiki/Bcrypt'>http://en.wikipedia.org/wiki/Bcrypt</a>,<br>
<br>
<blockquote><i>bcrypt is a key derivation function for passwords based on the Blowfish cipher.</i></blockquote>

From <a href='http://en.wikipedia.org/wiki/PBKDF2'>http://en.wikipedia.org/wiki/PBKDF2</a>,<br>
<br>
<blockquote><i>PBKDF2 (Password-Based Key Derivation Function 2) is a key derivation function that is part of RSA Laboratories' Public-Key Cryptography Standards (PKCS) series.</blockquote>

<blockquote>PBKDF2 applies a pseudorandom function to the input password along with a salt value, and repeats the process many times.</blockquote>

<blockquote>The added computational work makes password cracking much more difficult.</blockquote>

<blockquote>Having a salt added to the password reduces the ability to use precomputed hashes (rainbow tables) for attacks.</i></blockquote>

From <a href='http://en.wikipedia.org/wiki/Scrypt'>http://en.wikipedia.org/wiki/Scrypt</a>,<br>
<br>
<blockquote><i>A password-based key derivation function (password-based KDF) is generally designed to be computationally intensive, so that it takes a relatively long time to compute (say on the order of several hundred milliseconds).<br>
Legitimate users only need to perform the function once per operation (e.g., authentication), and so the time required is negligible.</blockquote>

<blockquote>However, a brute force attack would likely need to perform the operation billions of times at which point the time requirements become significant and, ideally, prohibitive.</i></blockquote>

<a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'><img src='https://www.owasp.org/skins/monobook/ologo.png' /></a>

From <a href='https://www.owasp.org/index.php/Hashing_Java'>https://www.owasp.org/index.php/Hashing_Java</a>,<br>
<br>
<blockquote><i>If each password is simply hashed, identical passwords will have the same hash. This has two drawbacks:</blockquote>

<blockquote>(1) Due to the birthday paradox (<a href='http://en.wikipedia.org/wiki/Birthday_paradox'>http://en.wikipedia.org/wiki/Birthday_paradox</a>), the attacker can find a password very quickly especially if the number of passwords in the database is large.</blockquote>

<blockquote>(2) An attacker can use a list of precomputed hashes (<a href='http://en.wikipedia.org/wiki/Rainbow_table'>http://en.wikipedia.org/wiki/Rainbow_table</a>) to break passwords in seconds.</blockquote>

<blockquote>In order to solve these problems, a salt can be concatenated to the password before the digest operation.</blockquote>

<blockquote>A salt is a random number of a fixed length. This salt must be different for each stored entry. It must be stored as clear text next to the hashed password.</blockquote>

<blockquote>In this configuration, an attacker must handle a brute force attack on each individual password. The database is now birthday attack and rainbow crack resistant.</i></blockquote>

Note that the sources, erm, "quoted" above, have been surreptitiously paraphrased in some places, to improve readability to suit ourselves ;)<br>
<br>
<h4>Base64</h4>

Since we store password hashes and their salts in an SQL database, we encode those bytes into text using <a href='http://en.wikipedia.org/wiki/Base64'>Base64</a>.<br>
<br>
For convenience, we introduce methods which delegate to our Base64 codec of choice e.g. from Apache commons, or the built-in Sun one.<br>
<pre><code>import sun.misc.BASE64Decoder;<br>
import sun.misc.BASE64Encoder;<br>
<br>
public class Base64 {<br>
    <br>
   public static String encode(byte[] bytes) {<br>
      return new BASE64Encoder().encode(bytes);<br>
   }<br>
<br>
   public static byte[] decode(String string) {<br>
      try {<br>
         return new BASE64Decoder().decodeBuffer(string);<br>
      } catch (Exception e) {<br>
         throw new RuntimeException(e);<br>
      }<br>
   }<br>
}<br>
</code></pre>

Actually <a href='http://commons.apache.org/codec/'>Apache Commons Codec</a> is a better choice as the Sun one gives compilations warnings to the effect that the <i>"sun.misc.BASE64Decoder is a Sun proprietary API and may be removed in a future release."</i> But we take that with a pinch of salt, so to speak.<br>
<br>
<h4>Psuedo salt</h4>

We read about <tt>SecureRandom</tt> vs <tt>Random</tt> on <a href='http://resources.infosecinstitute.com/random-number-generation-java'>infosecinstitute.com</a>, whilst enjoying <a href='http://search.dilbert.com/comic/Random%20Number%20Generator'>Dilbert's comic RNG</a> :)<br>
<br>
So apparently we must use <tt>SecureRandom</tt> to generate our salt, and not <tt>java.util.Random</tt>.<br>
<br>
<pre><code>public class PasswordSalts {<br>
   public static final int SALT_LENGTH = 16;    <br>
    <br>
   public static byte[] nextSalt() {<br>
      byte[] salt = new byte[SALT_LENGTH];<br>
      SecureRandom sr = SecureRandom.getInstance();<br>
      random.nextBytes(salt);<br>
      return salt;<br>
   }    <br>
}<br>
</code></pre>
where our salt is a 16 byte random number. Sooo easy :)<br>
<br>
Let's immerse in Base64 salts.<br>
<pre><code>   @Test<br>
   public void testSaltEncoding() throws Exception {<br>
      byte[] saltBytes = PasswordSalts.nextSalt();<br>
      String encodedSalt = Base64.encode(saltBytes);<br>
      System.out.println(encodedSalt);<br>
      assertEquals(encodedSalt.length(), 24);<br>
      assertEquals(encodedSalt.substring(22, 24), "==");<br>
   }<br>
</code></pre>

<pre><code>r2tWqOrfKpr64rpOwoRlcw==<br>
</code></pre>

So apparently a 16 byte array encoded with <tt>Base64</tt> yields a 22 character string followed by two characters of padding. Sold!<br>
<br>
<h4>Personal salt</h4>

When the user chooses a new password, we generate some salt for this specific password, and hash them together.<br>
<br>
The <a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'>OWASP example</a> presents an SQL credential table with the following columns:<br>
<pre><code>   LOGIN VARCHAR (100) PRIMARY KEY,<br>
   PASSWORD VARCHAR (32),<br>
   SALT VARCHAR (32)<br>
</code></pre>

<h4>Crypto parameters</h4>

So let's try PBKDF2. We'll leave Bcrypt and Scrypt for another day. (Your opinions on these options, parameters and what-not, are invited, so please come to the party!)<br>
<br>
<pre><code>public class Passwords {<br>
   public static final String ALGORITHM = "PBKDF2WithHmacSHA1";<br>
   public static final int ITERATION_COUNT = 8192;<br>
   public static final int KEY_SIZE = 160;<br>
<br>
   public static byte[] hashPassword(char[] password, byte[] salt)<br>
          throws GeneralSecurityException {<br>
      return hashPassword(password, salt, ITERATION_COUNT, KEY_SIZE);<br>
   }<br>
<br>
   public static byte[] hashPassword(char[] password, byte[] salt,<br>
          int iterationCount, int keySize) throws GeneralSecurityException {<br>
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterationCount, keySize);<br>
      SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);<br>
      return factory.generateSecret(spec).getEncoded();<br>
   }<br>
   ...<br>
</code></pre>

where we give a <a href='http://docs.oracle.com/javase/7/docs/api/javax/crypto/spec/PBEKeySpec.html'><tt>PBEKeySpec</tt></a> to a <a href='http://docs.oracle.com/javase/7/docs/api/javax/crypto/SecretKeyFactory.html'><tt>SecretKeyFactory</tt></a> specified with the <tt>PBKDF2WithHmacSHA1</tt> algorithm.<br>
<br>
Let's test that this salting, hashing and matching actually works.<br>
<pre><code>   @Test<br>
   public void test() throws Exception {<br>
      char[] password = "12345678".toCharArray();<br>
      byte[] salt = PasswordSalts.nextSalt();<br>
      byte[] hash = Passwords.hashPassword(password, salt);<br>
      assertTrue(Passwords.matches(password, hash, salt));<br>
      byte[] otherSaltBytes = Arrays.copyOf(salt, salt.length);<br>
      otherSaltBytes[0] ^= otherSaltBytes[0];<br>
      assertFalse(Passwords.matches(password, hash, otherSaltBytes));<br>
      assertFalse(Passwords.matches("wrong".toCharArray(), hash, salt));<br>
   }<br>
</code></pre>

where we use the following method to authenticate a supplied password, having retrieved the hash and salt from our database.<br>
<br>
<pre><code>public class Passwords {<br>
   ...<br>
   public static boolean matches(char[] password, byte[] passwordHash, <br>
          byte[] salt) throws GeneralSecurityException {<br>
      return matches(password, passwordHash, salt, ITERATION_COUNT, KEY_SIZE);<br>
   }<br>
<br>
   public static boolean matches(char[] password, byte[] passwordHash, <br>
          byte[] salt, int iterationCount, int keySize) <br>
          throws GeneralSecurityException {<br>
      return Arrays.equals(passwordHash, hashPassword(password, salt, <br>
          iterationCount, keySize));<br>
   }<br>
}<br>
</code></pre>
where we must specify the PBKDF2 parameters used to create the hash in the first place.<br>
<br>
Note that we use <tt>char<a href='.md'>.md</a></tt> so that provided passwords can be cleared from memory, e.g. using <tt>Arrays.fill()</tt> to zero the <tt>char</tt> array.<br>
<br>
<h4>Computational effort</h4>

According to the aforecited <a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'>OWASP Password Storage Cheat Sheet</a>,<br>
<br>
<blockquote><i>You should measure the time required and make sure that it's as large as possible without providing a significantly noticeable delay when your users authenticate.</i></blockquote>

Perhaps the time required to hash the password should be less than 100ms for consumer sites? And in the case of secure admin sites, a tad longer?<br>
<br>
<pre><code>   @Test<br>
   public void testEffort() throws Exception {<br>
      String password = "12345678";<br>
      long startMillis = System.currentTimeMillis();<br>
      byte[] saltBytes = Passwords.nextSalt();<br>
      Passwords.hashPassword(password, saltBytes);<br>
      System.out.println("time " + Millis.elapsed(startMillis));<br>
      if (Millis.elapsed(startMillis) &lt; 10) {<br>
         System.out.println("Ooooooo.... i'm not sure");<br>
      } else if (Millis.elapsed(startMillis) &gt; 500) {<br>
         System.out.println("Mmmmmmm.... i don't know");<br>
      }<br>
   }<br>
</code></pre>

Given that CPU power is increasing every year, surely we need a dynamic solution where we can revise the parameters at a future date? So let's extend <a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'>OWASP's</a> SQL credential table to include the PBKDF2 parameters.<br>
<br>
<pre><code>   LOGIN VARCHAR (100) PRIMARY KEY,<br>
   PASSWORD VARCHAR (32),<br>
   SALT VARCHAR (32),<br>
   PBKDF2_ITERATION_COUNT INTEGER,<br>
   PBKDF2_KEY_SIZE INTEGER,   <br>
</code></pre>

This would facilitate rehashing passwords on-the-fly to higher parameters when a user logs in and their actual password is thus on hand. This will be presented in the upcoming sequel entitled "Password Rehash" :)<br>
<br>
<h4>Secret salt</h4>

According to the oft aforementioned <a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'>OWASP Password Storage Cheat Sheet</a>,<br>
<br>
<blockquote><i>An additional password storage defense mechanism involves storing the salt in a different location than the password hash.</blockquote>

<blockquote>Use of the server's filesystem is one commonly used mechanism for salt isolation, assuming the password hashes are stored in a different location such as a database.</i></blockquote>

Is this really necessary? Well then rather than using the filesystem for salt storage per se, it'd be simpler to encrypt the salt in the database, and store just the salt-encrypting key on the filesystem. Our app then uses this key to decrypt the salts retrieved from the database.<br>
<br>
<img src='http://www.jroller.com/evanx/resource/salt-spill2.jpg' align='left'>

Moreover, rather than employing a <tt>KeyStore</tt> file, it would be simpler to specify a password in our application configuration on the filesystem, for the purpose of password-based encryption of the salt. This will be presented in the finale of this sub-trilogy, entitled <i>"Password Cipher."</i>

Then we'd have a salt hard-coded in our source code, for a password stored on the filesystem, for the encryption of the password salts stored in our database.<br>
<br>
All these things would have to be stolen, in order to have a crack at the password hashes. Hopefully this will buy us just enough time to alert our users that, erm, data is potentially amiss, and they should change their passwords rather urgently ;)<br>
<br>
<h4>Summary</h4>

Before hashing passwords, we must add salt, to protect against rainbow attacks and what-not.<br>
<br>
While SHA-2 is recommended these days for general hashing, we should use computationally expensive algorithms for password hashing, so that the passwords are harder to crack.<br>
<br>
We present an implementation using <tt>PBKDF2WithHmacSHA1</tt>, with a high number of iterations. We read that other recommended alternatives are bcrypt and scrypt.<br>
<br>
The hashing operation should take as long as we are willing to make the user wait, perhaps a few hundred milliseconds. We tweak the algorithm parameters accordingly.<br>
<br>
<h4>Coming up</h4>

In "Password Rehash," we'll cater for multiple revisions of the number of iterations and key size in the database, and migrate hashes on the fly to the latest revision when the user logs in.<br>
<br>
Finally, in "Password Cipher," we'll encrypt the password salt in our database, using... password-based encryption :)<br>
<br>
<h4>Resources</h4>

<a href='https://code.google.com/p/vellum/'>https://code.google.com/p/vellum/</a> - where i will collate these articles and their code - e.g. see <a href='http://code.google.com/p/vellum/source/browse/trunk/src/vellum/crypto/Passwords.java'><tt>Passwords</tt></a>.<br>
<br>
<p align='right' />