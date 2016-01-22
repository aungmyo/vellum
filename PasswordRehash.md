March 2013 (DRAFT)

<h4>Introduction</h4>

In the [Password Salt](PasswordSalt.md) prequel, we suggested that the most secure passwords are no passwords, e.g. using Google Login, Facebook Connect, Mozilla Persona or what-have-you. Such an approach is simpler for developers and more convenient for end-users. However for internal enterprise apps, those identity services might not be suitable, so...

In said prequel, we presented an implementation using <tt>PBKDF2WithHmacSHA1</tt>, with a high number of iterations.

In this article, we cater for multiple revisions of the number of iterations and key size, and migrate hashes to the latest revision when the user logs in.

<h4>PBKDF2 recap</h4>

Paraphrasing an earlier 2012 version of the <a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'>OWASP Password Storage Cheat Sheet</a>,

> <i>General hashing algorithms (e.g. MD5, SHA) are not recommended for password storage. Instead an algorithm specifically designed for the purpose should be used such as PBKDF2 or scrypt.</i>

As presented in [Password Salt](PasswordSalt.md), we salt, hash and match our passwords using PBKDF2 as follows.

```
public class Passwords {
    public static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    public static final int ITERATION_COUNT = 30000;
    public static final int KEY_SIZE = 160;

    public static byte[] hashPassword(char[] password, byte[] salt)
            throws GeneralSecurityException {
        return hashPassword(password, salt, ITERATION_COUNT, KEY_SIZE);
    }

    public static byte[] hashPassword(char[] password, byte[] salt,
            int iterationCount, int keySize) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterationCount, keySize);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    public static boolean matches(char[] password, byte[] passwordHash, byte[] salt) 
            throws GeneralSecurityException {
        return matches(password, passwordHash, salt, ITERATION_COUNT, KEY_SIZE);
    }

    public static boolean matches(char[] password, byte[] passwordHash, byte[] salt,
            int iterationCount, int keySize) throws GeneralSecurityException {
        return Arrays.equals(passwordHash, hashPassword(password, salt, 
                iterationCount, keySize));
    }
}
```

where we check if the supplied password, and its salt, matches our hash, using the given PBKDF2 parameters.

<h4>Measuring time</h4>

According to an aforecited version of the <a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'>OWASP Password Storage Cheat Sheet</a>,

> <i>One should measure the time required and make sure that it's as large as possible without providing a significantly noticeable delay when users authenticate.</i>

Let's measure the time required, albeit on our dev machine.

```
    @Test
    public void testMatchesEffort() throws Exception {
        char[] password = "12345678".toCharArray();
        byte[] saltBytes = PasswordSalts.nextSalt();
        long startMillis = System.currentTimeMillis();
        byte[] hashBytes = Passwords.hashPassword(password, saltBytes, 30000, 160);
        System.out.println("hash time (30k): " + Millis.elapsed(startMillis));
        startMillis = System.currentTimeMillis();
        Passwords.hashPassword(password, saltBytes, 300000, 160);
        System.out.println("10x hash time (300k): " + Millis.elapsed(startMillis));
        startMillis = System.currentTimeMillis();
        assertTrue(Passwords.matches(password, hashBytes, saltBytes, 30000, 160));
        System.out.println("matches time: " + Millis.elapsed(startMillis));
        assertFalse(Passwords.matches(password, hashBytes, saltBytes, 30001, 160));
        assertFalse(Passwords.matches(password, hashBytes, saltBytes, 30000, 128));
        assertFalse(Passwords.matches("wrong".toCharArray(), 
                hashBytes, saltBytes, 30000, 160));
    }
```

which prints the duration of the hashing and matching.

```
hash time (30k): 181
10x hash time (300k): 1701
matches time: 167
```

which shows the time in milliseconds for 30,000 iterations and also 10x that.

<h4>Storage</h4>

In order to revise the algorithm parameters e.g. if we migrate to a faster host, we need to store these parameters, together with the salt and the password hash, for each user.

We might extend our SQL credential table to include the PBKDF2 parameters as follows.

```
   LOGIN VARCHAR(100) PRIMARY KEY,
   PASSWORD VARCHAR(32),
   SALT VARCHAR(32),
   PASSWORD_ITERATION_COUNT INTEGER,
   PASSWORD_KEY_SIZE INTEGER,   
```

In the sequel, we'll consider encrypting the salt, which would require another field named <tt>SALT_IV</tt> (for the AES "initialization vector").

But let's try to migrate to salty passwords without changing our database schema so much, where we pack the password hash, salt and parameters into one field, to be stored in the <tt>PASSWORD</tt> column. We can identify our legacy hash therein by its shorter length, and migrate.

```
public class PasswordHash {
    private static final byte VERSION = 255;    
    int iterationCount;
    int keySize;
    byte[] hash;
    byte[] salt;
    byte[] iv;

    public PasswordHash(byte[] hash, byte[] salt, byte[] iv, 
            int iterationCount, int keySize) {
        this.hash = hash;
        this.salt = salt;
        this.iv = iv;
        this.iterationCount = iterationCount;
        this.keySize = keySize;
    }
    ...
```

Given a new password and algorithm parameters, we generate salt, and hash the password as follows.

```
    public PasswordHash(char[] password, int iterationCount, int keySize) 
            throws GeneralSecurityException {
        this.iterationCount = iterationCount;
        this.keySize = keySize;
        this.salt = PasswordSalts.nextSalt(); // e.g. random 16 byte array
        this.hash = Passwords.hashPassword(password, salt, iterationCount, keySize);
        this.iv = new byte[0];
    }
```

We roll up the password hash, salt and parameters into a byte array for storage purposes.

```
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(VERSION);
        writeObject(new ObjectOutputStream(stream));
        return stream.toByteArray();
    }
                    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(iterationCount);
        stream.writeShort(keySize);
        if (hash.length > 255) {
            throw new IOException();
        }
        stream.write(hash.length);
        stream.write(salt.length);
        stream.write(iv.length);
        stream.write(hash);
        stream.write(salt);
        stream.write(iv);        
        stream.flush();
    }
```

where we introduce a <tt>writeObject()</tt> method as per <tt>Serializable</tt>, for the sake of conformity.

The byte array will be encoded using Base64 and stored in our <tt>PASSWORD</tt> column in the database.

In order to authenticate a user's password, we unpack the hash, salt and parameters, from the byte array retrieved from the database.

```
    public PasswordHash(byte[] bytes) throws IOException {
        InputStream stream = new ByteArrayInputStream(bytes);
        int version = stream.read();        
        if (stream.read() != VERSION) {
            throw new IOException("version mismatch");
        }
        readObject(stream);
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        iterationCount = stream.readInt();
        keySize = stream.readShort();
        hash = new byte[stream.read()];
        salt = new byte[stream.read()];
        iv = new byte[stream.read()];
        stream.read(hash);
        stream.read(salt);
        stream.read(iv);        
    }
```

We provide a method to authenticate a password against this hash-and-all.

```
    public boolean matches(char[] password) throws GeneralSecurityException {
        millis = System.currentTimeMillis();
        try {
            return Arrays.equals(hash, Passwords.hashPassword(password, salt, iterationCount, keySize));
        } finally {
            millis = Millis.elapsed(millis);
        }
    }
```

where we record the time taken to authenticate the password, for monitoring purposes, in order to assess the chosen <tt>iterationCount</tt> parameter in our production environment.

<h4>Testing</h4>

Let's test this and see what we end up with.

```
    @Test
    public void testPasswordHashMinimumKeySize() throws Exception {
        testPasswordHash(30000, 128);
    }
    
    private void testPasswordHash(int iterationCount, int keySize) throws Exception {
        char[] password = "12345678".toCharArray();
        PasswordHash passwordHash = new PasswordHash(password, iterationCount, keySize);
        byte[] hashBytes = passwordHash.getBytes();
        passwordHash = new PasswordHash(hashBytes) ;
        assertEquals(iterationCount, passwordHash.getIterationCount());
        assertEquals(keySize, passwordHash.getKeySize());
        assertTrue(PasswordHash.verifyBytes(hashBytes));
        String encodedString = Base64.encode(hashBytes);
        assertTrue(PasswordHash.verifyBytes(hashBytes));
        assertFalse(passwordHash.matches("wrong".toCharArray()));
        assertTrue(passwordHash.matches(password));
        System.out.printf("iterationCount: %d\n", iterationCount);
        System.out.printf("keySize: %d\n", keySize);
        System.out.printf("byte array length: %d\n", hashBytes.length);
        System.out.printf("encoded string: %s\n", encodedString);
        System.out.printf("encoded length: %d\n", encodedString.length());
        System.out.printf("millis: %d\n", passwordHash.getMillis());
    }
```

which prints,

```
iterationCount: 30000
keySize: 128
byte array length: 48
encoded string: /6ztAAV3KQAAdTAAgBAQADaOT6lY7axaXF56GJvOPjKMrHGQhyihJQWVzeqjyK+6
encoded length: 64
millis: 137
```

Seeing that the Base64-encoded length is 64 characters, our <tt>PASSWORD</tt> column capacity should be altered to <tt>VARCHAR(64)</tt> at least.

We note the array length is 48, which must be larger than our legacy hashes if we wish to differentiate by this.

<h4>Migration</h4>

We provide a <tt>static</tt> method to confirm that the bytes are what our <tt>PasswordHash</tt> constructor would expect.

```
public class PasswordHash {
    ...
    public static boolean verifyBytes(byte[] bytes) {
        return bytes.length >= 48;
    }   
} 
```

where we assume that the length of our legacy unsalted password hashes is less than our <tt>PasswordHash</tt> serialized array, with hash and salt <i>et al.</i>, in order to use the above method to differentiate them.

Finally we migrate to salty passwords, on the fly, as follows.

```
    public boolean matches(String user, char[] password, byte[] packedBytes) throws Exception {
        if (PasswordHash.verifyBytes(packedBytes)) {
            PasswordHash passwordHash = new PasswordHash(packedBytes);
            if (passwordHash.matches(password)) {
                monitor(passwordHash.getMillis());
                if (passwordHash.getIterationCount() != Passwords.ITERATION_COUNT
                        || passwordHash.getKeySize() != Passwords.KEY_SIZE) {
                    passwordHash = new PasswordHash(password,
                            Passwords.ITERATION_COUNT, Passwords.KEY_SIZE);
                    persistRevisedPasswordHash(user, passwordHash.getBytes());
                }
                return true;
            }
            return false;
        }
        if (matchesUnsalted(password, packedBytes)) {
            packedBytes = PackedPasswords.hashPassword(password);
            persistRevisedPasswordHash(user, packedBytes);
            return true;
        }
        return false;
    }
```

where if the password is correct, but not at the latest revision, or still a legacy unsalted hash, we take the opportunity of migrating that user's password hash to the latest salty non-cracker.

As PCI compliance requires passwords to be changed every 90 days, admittedly it's not really necessary to migrate existing passwords to higher parameters, because a new password hash will be created soon enough :)

Finally, we <tt>monitor()</tt> the time taken to authenticate the password, e.g. to log hints to revise our iteration count. An upcoming article in the <a href='https://code.google.com/p/vellum/wiki/Timestamped'>Timestamped</a> series will illustrate how we can gather time-series stats. We will record the sample size, minumum, maximum, average, and distribution of the duration of the hashing operation, for different iteration counts, aggregated by hour and day. Of course, this must be nicely graphed so we can visualise the state of affairs, at a quick glance :)

<h4>Salt cipher</h4>

According to the oft aforementioned <a href='https://www.owasp.org/index.php/Password_Storage_Cheat_Sheet'>OWASP Password Storage Cheat Sheet</a>,

> <i>An additional password storage defense mechanism involves storing the salt in a different location than the password hash.</li></ul>

<blockquote>Use of the server's filesystem is one commonly used mechanism for salt isolation, assuming the password hashes are stored in a different location such as a database.</i></blockquote>

As discussed in the prequel, it'd be simpler to encrypt the salt in the database, and store just the salt-encrypting key on the filesystem.<br>
<br>
Indeed, the <tt>iv</tt> field of <tt>PasswordHash</tt> is an "initialization vector" as required for AES encryption of the salt. In the finale of this trilogy, we'll present that aspect.<br>
<br>
<h4>Summary</h4>

While SHA-2 is recommended these days for general hashing, we should use computationally expensive algorithms for password hashing, so that the passwords are harder to crack.<br>
<br>
In the prequel, we presented an implementation using <tt>PBKDF2WithHmacSHA1</tt>, with a high number of iterations.<br>
<br>
<img src='http://jroller.com/evanx/resource/gnome-shield-250.png' align='left' />

Since the hashing operation should take as long as we are willing to make the user wait, the algorithm parameters must be tweaked according to our host's CPU.<br>
<br>
We should store of the number of iterations and key size, to enable revision thereof.<br>
<br>
We decide to encapsulate the hash, salt and parameters into a <tt>PasswordHash</tt> object. We serialize this object into a byte array, and encode with Base64 to store in our SQL database. Migration from legacy hashes is somewhat simplied thereby.<br>
<br>
We can migrate to revised hashes when the user logs in and their password is thus on hand. Having said that, and seeing that PCI compliance requires passwords to be changed every 90 days, it's perhaps not necessary to migrate existing passwords to higher parameters when a new password hash will be created soon enough.<br>
<br>
<h4>Coming up</h4>

In "Password Cipher," the finale of this <a href='https://code.google.com/p/vellum/wiki/EnigmaPosts'>sub-trilogy</a>, we'll encrypt the password salt in our database, using... password-based encryption :)<br>
<br>
Also, an upcoming article in the <a href='https://code.google.com/p/vellum/wiki/Timestamped'>Timestamped</a> series will illustrate how we can gather time-series stats, including the time taken to authenticate the password. Such stats are required to revise our iteration count.<br>
<br>
We really should try out <a href='http://en.wikipedia.org/wiki/Bcrypt'>bcrypt</a> and <a href='http://en.wikipedia.org/wiki/Scrypt'>scrypt</a>, and hopefully we will :)<br>
<br>
<h4>Resources</h4>

<a href='https://code.google.com/p/vellum/'>https://code.google.com/p/vellum/</a> - where i will collate these articles and their code - e.g. see <a href='http://code.google.com/p/vellum/source/browse/trunk/src/vellum/crypto/PasswordHash.java'><tt>PasswordHash</tt></a>.