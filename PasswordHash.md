# Password Hash #

This is an old defunct article (Feb 2007) that doesn't include salt! It has been superceded by [Password Salt](PasswordSalt.md) (Dec 2012).

## Introduction ##

Passwords should never be seen in clear text in the wild e.g. transferred over the network, or stored in databases. So we hash them up.

So begins our modest exploration of the  <a href='http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html'>Java Cryptography Architecture</a> .

## Message Digest ##

Our hashing helper class below is um, exceptionally trivial.

```
public class PasswordHasher {
    String algorithm = "SHA-1";

    public PasswordHasher() {
    }
    
    public PasswordHasher(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public String hashPassword(byte[] passwordBytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hashBytes = digest.digest(passwordBytes);
        String hashString = Base64.encode(hashBytes);
        return hashString;
    }
    ...
}
```

where we instantiate a `MessageDigest` with a given algorithm, e.g. `SHA-1` or `MD5`.

We encode the resulting `byte` array into a string using `Base64` encoding. This utilises 64 `ASCII` characters as "digits." Another option is hexadecimal, but that's longer since it's base-16.

### Verification ###

We might offer the following convenience-method for verifying an entered password against the known hash password for that user.

```
    public boolean verifyPassword(byte[] passwordBytes, String passwordHash) 
    throws NoSuchAlgorithmException {
        return hashPassword(passwordBytes).equals(passwordHash);
    }
```

### Kicking the tyres ###

Let's test it.

```
    public static void main(String[] args) {
        try {
            String password = "l33t!hax0r";
            PasswordHasher passwordHasher = new PasswordHasher("SHA-256");
            byte[] passwordBytes = password.getBytes();
            String passwordHash = passwordHasher.hashPassword(passwordBytes);
            System.out.println(passwordHash.length());
            System.out.println(passwordHash);
            System.out.println(passwordHasher.hashPassword(password.getBytes()));
            byte[] wrongPasswordBytes = password.toUpperCase().getBytes();
            String wrongHash = passwordHasher.hashPassword(wrongPasswordBytes);
            System.out.println(wrongHash);            
            System.out.println(
                    passwordHasher.verifyPassword(passwordBytes, passwordHash));
            System.out.println(
                    passwordHasher.verifyPassword(wrongPasswordBytes, passwordHash));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

We get the following output.

```
44
e7AswF+f11/uz9/OtB+ZE//3I4+z9H/m6//0tVhMN/w=
e7AswF+f11/uz9/OtB+ZE//3I4+z9H/m6//0tVhMN/w=
DyFwl8tWX7+dUE/JC7oumAURbl/w9+xt9AIDk+/Uf9o=
true
false
```

It looks like line-noise, so it's perfect! ;)

### Message Digest Algorithms ###

We can employ any of the following algorithms and more, http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html
when invoking `MessageDigest.getInstance()`.

```
    public static final String md5Algorithm = "MD5"; // Message Digest Algorithm
    public static final String sha1Algorithm = "SHA-1"; // Secure Hash Algorithm
    public static final String sha256Algorithm = "SHA-256";
    public static final String sha512Algorithm = "SHA-512"; 
```

For example, `SHA-256` produces a 256bit digest, i.e. 32 bytes long, which in base-64 translates to 42 and a bit characters, so make that 43 characters, padded up to 44.

### Base64 ###

Base64 encoding involves concatenating 3 bytes into 24 bits, and then splitting that into 4 "digits" of 6 bits each.

It seems that the JRE doesn't offer standard Base-64 codecs?! So we use the non-standard `sun.misc.BASE64Encoder` and `BASE64Decoder` classes as follows.

```
public class Base64 {    

    public static String encode(byte[] bytes) {
        return new sun.misc.BASE64Encoder().encode(bytes);
    }
    
    public static byte[] decode(String string) {       
        try {
            return new sun.misc.BASE64Decoder().decodeBuffer(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}    
```

The following 64 "digits" are used.

```
    final static String base64Digits = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
```


Additionally, the end of the base-64 string might get padded with one or two '=' characters.

## Conclusion ##

Passwords should never be sent across the network, or stored in databases in clear text. So we hash them using MD5 http://en.wikipedia.org/wiki/MD5 or SHA-1 http://en.wikipedia.org/wiki/SHA-1

We can dictate the desired strength, e.g. from 128bit to 512bit, e.g. by choosing the `SHA-512` algorithm rather than `SHA-1` or `MD5`.

<i>This is an old defunct article (Feb 2007) that doesn't include salt! It has been superceded by <a href='PasswordSalt.md'>Password Salt</a>.</i>