# Encryptic #

## Intro ##

In [Cryptonomical](Cryptonomical.md) we considered both symmetric (secret key) and asymmetric (public key) algorithms, to encrypt messages in a communications session. But what about persistent data?

In [PasswordHash](PasswordHash.md) we considered securing passwords.

We also casually considered using straight Base-64 encoding for hiding sensitive data from prying eyes, but nothing more than that e.g. people with half a clue.

So now let's actually encrypt data using passwords, albeit not-so-secret ones.

## Password-based Encryption (PBE) ##

We implement the following class to support password-based encryption (PBE) using the DES symmetric algorithm. (Alternatively, we might use triple-DES or AES algorithms.)

```
public class PBECipher {
    private static final String pbeAlgorithm = "PBEWithMD5AndDES";
    private static final String defaultPassword = "Ssh ssh!";
    ...
    SecretKey secretKey;
    PBEParameterSpec parameterSpec;
    Cipher encryptCipher;
    Cipher decryptCipher;

    public PBECipher() {
        this(defaultPassword);
    }
    
    public PBECipher(String password) {
        try {
            parameterSpec = new PBEParameterSpec(salt, iterationCount);
            secretKey = createSecretKey(password);
            encryptCipher = createEncryptCipher();
            decryptCipher = createDecryptCipher();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    ...
}
```

where we offer a `defaultPassword` for the password-based encryption.

We instantiate a `PBEParameterSpec` with an arbitrary 8-byte `salt` and `iterationCount`, for some password-based key derivation function. http://en.wikipedia.org/wiki/PBKDF2

```
    private static final int iterationCount = 5;
    private static byte[] salt = {
        (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03,
        (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32
    };
```

### Crypto ###

We implement the following `private` support methods.

```
    private SecretKey createSecretKey(String secretKey) throws Exception {
        KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(pbeAlgorithm);
        return keyFactory.generateSecret(keySpec);
    }
    
    private Cipher createEncryptCipher() throws Exception {
        Cipher encryptCipher = Cipher.getInstance(pbeAlgorithm);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        return encryptCipher;
    }
    
    private Cipher createDecryptCipher() throws Exception {
        Cipher decryptCipher = Cipher.getInstance(pbeAlgorithm);
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        return decryptCipher;
    }
```

where our `secretKey` instance is created in `createSecretKey()` by a `SecretKeyFactory`, using `PBEKeySpec`.

We initialise our ciphers using the `secretKey` and the `PBEParameterSpec`.

(We discussed Java Cryptography's `Cipher` http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html in the [Cryptonomical](Cryptonomical.md) prequel.

### Ciphering ###

Finally, we use the following `public` methods for encryption/decryption.

```
    public String encrypt(String string) {
        try {
            return Base64.encode(encryptCipher.doFinal(string.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String decrypt(String string) {
        try {
            return new String(decryptCipher.doFinal(Base64.decode(string)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    
```

where in this case we support the encryption of strings only, rather than `byte` arrays.

(We discussed Base-64 encoding in PasswordHash.)

### Testing ###

So let's test this PBE cipher.

```
public class PBECipherTest {
    PBECipher cipher = new PBECipher();

    protected void test(String text) throws Exception {
        text = cipher.encrypt(text);
        System.out.println(text);
        text = cipher.decrypt(text);
        System.out.println(text);
    }
    
    protected void test() throws Exception {
        test("Let's get us some PBE with DES");
    }
    
    public static void main(String[] args) {
        try {
            new PBECipherTest().test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

which outputs the following

```
epd70uoojXEAAXNJKKs8IRHqWsFJ6aXqtpyhmCJ8MYg=
Let's get us some PBE with DES
```

OK then, that's a wrap!

## Conclusion ##

Even though the default password is in the code and so known to "everyone," at least now we can assure our managers that we are actually encrypting the data ;)