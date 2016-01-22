# SAQ 3: Data projection #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

<b>
Guidance for Requirements 3 and 4: Protect Cardholder Data<br>
<br>
Requirement 3: Protect stored cardholder data<br>
</b>

Protection methods such as encryption, truncation, masking, and hashing are critical components of cardholder data protection. If an intruder
circumvents other security controls and gains access to encrypted data, without the proper cryptographic keys, the data is unreadable and
unusable to that person. Other effective methods of protecting stored data should be considered as potential risk mitigation opportunities. For
example, methods for minimizing risk include not storing cardholder data unless absolutely necessary, truncating cardholder data if full PAN is not
needed, and not sending unprotected PANs using end-user messaging technologies, such as e-mail and instant messaging.

Please refer to the PCI DSS Glossary of Terms, Abbreviations, and Acronyms for definitions of “strong cryptography” and other PCI DSS terms.

## 3.1 Cardholder data storage ##

Keep cardholder data storage to a minimum by implementing data retention and disposal policies, procedures and processes, as follows.

<i>
A formal data retention policy identifies what data needs to be retained, and where<br>
that data resides so it can be securely destroyed or deleted as soon as it is no<br>
longer needed. In order to define appropriate retention requirements, an entity first<br>
needs to understand their own business needs as well as any legal or regulatory<br>
obligations that apply to their industry, and/or that apply to the type of data being<br>
retained.<br>
<br>
Extended storage of cardholder data that exceeds business need creates an<br>
unnecessary risk. The only cardholder data that may be stored after authorization<br>
is the primary account number or PAN (rendered unreadable), expiration date,<br>
cardholder name, and service code.<br>
<br>
Implementing secure deletion methods ensure that the data cannot be retrieved<br>
when it is no longer needed.<br>
<br>
Remember, if you don't need it, don't store it!<br>
</i>

#### 3.1.1 Data retention and disposal policy ####

Implement a data retention and disposal policy that includes:
  * Limiting data storage amount and retention time to that which is required for legal, regulatory, and business requirements
  * Processes for secure deletion of data when no longer needed
  * Specific retention requirements for cardholder data
  * A quarterly automatic or manual process for identifying and securely deleting stored cardholder data that exceeds defined retention requirements

## 3.2 Do not store sensitive authentication data ##

Do not store sensitive authentication data after authorization (even if encrypted).

Sensitive authentication data includes the data as cited in the
following Requirements 3.2.1 through 3.2.3.

Note: it is permissible for issuers and companies that support
issuing services to store sensitive authentication data if there is a
business justification and the data is stored securely.

<i>
Sensitive authentication data consists of magnetic stripe (or track) data6, card<br>
validation code or value7, and PIN data8. Storage of sensitive authentication<br>
data after authorization is prohibited! This data is very valuable to malicious<br>
individuals as it allows them to generate counterfeit payment cards and create<br>
fraudulent transactions. See PCI DSS and PA-DSS Glossary of Terms,<br>
Abbreviations, and Acronyms for the full definition of “sensitive authentication<br>
data.”<br>
<br>
Note: It is allowable for companies that perform, facilitate, or support issuing<br>
services to store sensitive authentication data ONLY IF they have a legitimate<br>
business need to store such data. It should be noted that all PCI DSS<br>
requirements apply to issuers, and the only exception for issuers and issuer<br>
processors is that sensitive authentication data may be retained if there is a<br>
legitimate reason to do so. A legitimate reason is one that is necessary for the<br>
performance of the function being provided for the issuer and not one of<br>
convenience.<br>
<br>
Any such data must be stored securely and in accordance with PCI DSS and<br>
specific payment brand requirements.<br>
</i>

#### 3.2.1 Do not store the full contents of any magnetic stripe track ####

Do not store the full contents of any track (from the
magnetic stripe located on the back of a card, equivalent data
contained on a chip, or elsewhere). This data is alternatively
called full track, track, track 1, track 2, and magnetic stripe data.

<i>
Note: In the normal course of business, the following data elements from the magnetic stripe may need to be retained:<br>
<ul><li>The cardholder’s name<br>
</li><li>Primary account number (PAN)<br>
</li><li>Expiration date<br>
</li><li>Service code</li></ul>

To minimize risk, store only these data elements as needed for business.<br>
<br>
If full track data is stored, malicious individuals who obtain that data can reproduce and sell payment cards.<br>
</i>

#### 3.2.2 Do not store the card-verification code ####

Do not store the card-verification code or value (three-digit
or four-digit number printed on the front or back of a payment
card) used to verify card-not-present transactions.

<i>
The purpose of the card validation code is to protect "card-not-present"<br>
transactions—Internet or mail order/telephone order (MO/TO) transactions—where<br>
the consumer and the card are not present. These types of transactions can be<br>
authenticated as coming from the card owner only by requesting this card<br>
validation code, since the card owner has the card in-hand and can read the value.<br>
<br>
If this prohibited data is stored and subsequently stolen, malicious individuals can<br>
execute fraudulent Internet and MO/TO transactions.<br>
</i>

#### 3.2.3 Do not store the personal identification number (PIN) or the encrypted PIN block ####

<i>
These values should be known only to the card owner or bank that issued the card.<br>
If this prohibited data is stored and subsequently stolen, malicious individuals can<br>
execute fraudulent PIN-based debit transactions (for example, ATM withdrawals).<br>
</i>

## 3.3 Mask PAN ##

Mask PAN when displayed (the first six and last four digits are the maximum number of digits to be displayed).

Notes:
  * This requirement does not apply to employees and other parties with a legitimate business need to see the full PAN.
  * This requirement does not supersede stricter requirements in place for displays of cardholder data—for example, for point-of-sale (POS) receipts.

<i>
The display of full PAN on items such as computer screens, payment card<br>
receipts, faxes, or paper reports can result in this data being obtained by<br>
unauthorized individuals and used fraudulently. The PAN can be displayed in full<br>
form on the “merchant copy” receipts; however the paper receipts should adhere to<br>
the same security requirements as electronic copies and follow the guidelines of<br>
the PCI Data Security Standard, especially Requirement 9 regarding physical<br>
security. The full PAN can also be displayed for those with a legitimate business<br>
need to see the full PAN.<br>
<br>
This requirement relates to protection of PAN displayed on screens, paper<br>
receipts, etc., and is not to be confused with Requirement 3.4 for protection of PAN<br>
when stored in files, databases, etc.<br>
</i>

## 3.4 Render PAN unreadable anywhere it is stored ##

Render PAN unreadable anywhere it is stored (including on
portable digital media, backup media, and in logs) by using any of
the following approaches.
**One-way hashes based on strong cryptography (hash must be of the entire PAN)** Truncation (hashing cannot be used to replace the truncated segment of PAN)
**Index tokens and pads (pads must be securely stored)** Strong cryptography with associated key-management processes and procedures

Note: It is a relatively trivial effort for a malicious individual to
reconstruct original PAN data if they have access to both the
truncated and hashed version of a PAN. Where hashed and
truncated versions of the same PAN are present in an entity’s
environment, additional controls should be in place to ensure that
the hashed and truncated versions cannot be correlated to
reconstruct the original PAN.

<i>
Lack of protection of PANs can allow malicious individuals to view or download this<br>
data. PANs stored in primary storage (databases, or flat files such as text files<br>
spreadsheets) as well as non-primary storage (backup, audit logs, exception or<br>
troubleshooting logs) must all be protected. Damage from theft or loss of backup<br>
tapes during transport can be reduced by ensuring PANs are rendered unreadable<br>
via encryption, truncation, or hashing. Since audit, troubleshooting, and exception<br>
logs have to be retained, you can prevent disclosure of data in logs by rendering<br>
PANs unreadable (or removing them) in logs.<br>
<br>
By correlating hashed and truncated versions of a given PAN, a malicious<br>
individual may easily derive the original PAN value. Controls that prevent the<br>
correlation of this data will help ensure that the original PAN remains unreadable.<br>
<br>
Please refer to the PCI DSS and PA-DSS Glossary of Terms, Abbreviations, and<br>
Acronyms for definitions of “strong cryptography.”<br>
</i>

#### One-way hashes based on strong cryptography (hash must be of the entire PAN) ####

One-way hash functions such as the Secure Hash Algorithm (SHA) based on
strong cryptography can be used to render cardholder data unreadable. Hash
functions are appropriate when there is no need to retrieve the original number
(one-way hashes are irreversible).

To complicate the creation of rainbow tables it is recommended, but not a
requirement, that a salt value be input to the hash function in addition to the PAN.

#### Truncation (hashing cannot be used to replace the truncated segment of PAN) ####

The intent of truncation is that only a portion (not to exceed the first six and last
four digits) of the PAN is stored. This is different from masking, where the whole
PAN is stored but the PAN is masked when displayed (i.e., only part of the PAN is
displayed on screens, reports, receipts, etc.).

This requirement relates to protection of PAN when stored in files, databases, etc.,
and is not to be confused with Requirement 3.3 for protection of PAN displayed on
screens, paper receipts, etc.

#### Index tokens and pads (pads must be securely stored) ####

Index tokens and pads may also be used to render cardholder data unreadable. An
index token is a cryptographic token that replaces the PAN based on a given index
for an unpredictable value. A one-time pad is a system in which a private key,
generated randomly, is used only once to encrypt a message that is then
decrypted using a matching one-time pad and key.

#### Strong cryptography with associated key-management processes and procedures ####

The intent of strong cryptography (see definition and key lengths in the PCI DSS
and PA-DSS Glossary of Terms, Abbreviations, and Acronyms) is that the
encryption be based on an industry-tested and accepted algorithm (not a
proprietary or "home-grown" algorithm).

#### 3.4.1 If disk encryption is used, logical access must be managed independently ####

If disk encryption is used (rather than file- or column-level
database encryption), logical access must be managed
independently of native operating system access control
mechanisms (for example, by not using local user account
databases). Decryption keys must not be tied to user accounts.

<i>
The intent of this requirement is to address the acceptability of disk encryption for<br>
rendering cardholder data unreadable. Disk encryption encrypts data stored on a<br>
computer's mass storage and automatically decrypts the information when an<br>
authorized user requests it. Disk-encryption systems intercept operating system<br>
read and write operations and carry out the appropriate cryptographic<br>
transformations without any special action by the user other than supplying a<br>
password or pass phrase at the beginning of a session. Based on these<br>
characteristics of disk encryption, to be compliant with this requirement, the disk-<br>
encryption method cannot have:<br>
<br>1) A direct association with the operating system, or<br>
<br>2) Decryption keys that are associated with user accounts.<br>
<br>
<br>
Unknown end tag for </i><br>
<br>
<br>
<br>
<h2>3.5 Protect cryptographic keys</h2>

Protect any keys used to secure cardholder data against disclosure and misuse<br>
<br>
Note: This requirement also applies to key-encrypting keys used<br>
to protect data-encrypting keys—such key-encrypting keys must<br>
be at least as strong as the data-encrypting key.<br>
<br>
<i>
Cryptographic keys must be strongly protected because those who obtain access<br>
will be able to decrypt data. Key-encrypting keys, if used, must be at least as<br>
strong as the data-encrypting key in order to ensure proper protection of the key<br>
that encrypts the data as well as the data encrypted with that key.<br>
<br>
The requirement to protect keys from disclosure and misuse applies to both data-<br>
encrypting keys and key-encrypting keys. Because one key-encrypting key may<br>
grant access to many data-encrypting keys, the key-encrypting keys require strong<br>
protection measures. Methods for secure storage of key-encrypting keys include<br>
but are not limited to hardware security modules (HSMs) and tamper evident<br>
storage with dual control and split knowledge.<br>
</i>

<h4>3.5.1 Restrict access to cryptographic keys to the fewest number of custodians necessary</h4>

There should be very few who have access to cryptographic keys, usually only<br>
those who have key custodian responsibilities.<br>
<br>
<h4>3.5.2 Store cryptographic keys securely in the fewest possible locations and forms</h4>

Cryptographic keys must be stored securely, usually encrypted with key-encrypting<br>
keys, and stored in very few locations. It is not intended that the key-encrypting<br>
keys be encrypted, however they are to be protected against disclosure and<br>
misuse as defined in Requirement 3.5. Storing key-encrypting keys in physically<br>
and/or logically separate locations from data-encrypting keys reduces the risk of<br>
unauthorized access to both keys.<br>
<br>
<h2>3.6 Key-management processes and procedures</h2>

Fully document and implement all key-management<br>
processes and procedures for cryptographic keys used for<br>
encryption of cardholder data, including the following:<br>
<br>
Note: Numerous industry standards for key management are<br>
available from various resources including NIST, which can be<br>
found at <a href='http://csrc.nist.gov'>http://csrc.nist.gov</a>.<br>
<br>
<i>
The manner in which cryptographic keys are managed is a critical part of the<br>
continued security of the encryption solution. A good key management process,<br>
whether it is manual or automated as part of the encryption product, is based on<br>
industry standards and addresses all key elements at 3.6.1 through 3.6.8.<br>
</i>

<h4>3.6.1 Generation of strong cryptographic keys</h4>

The encryption solution must generate strong keys, as defined in the PCI DSS and<br>
PA-DSS Glossary of Terms, Abbreviations, and Acronyms under "strong<br>
cryptography."<br>
<br>
<h4>3.6.2 Secure cryptographic key distribution</h4>

The encryption solution must distribute keys securely, meaning the keys are not<br>
distributed in the clear, and only to custodians identified in 3.5.1.<br>
<br>
<h4>3.6.3 Secure cryptographic key storage</h4>

The encryption solution must store keys securely, meaning the keys are not stored<br>
in the clear (encrypt them with a key-encryption key).<br>
<br>
<h4>3.6.4 Cryptographic key changes</h4>

Cryptographic key changes for keys that have reached<br>
the end of their cryptoperiod (for example, after a defined period<br>
of time has passed and/or after a certain amount of cipher-text<br>
has been produced by a given key), as defined by the<br>
associated application vendor or key owner, and based on<br>
industry best practices and guidelines (for example, NIST Special Publication 800-57).<br>
<br>
<h4>3.6.5 Retirement or replacement of keys</h4>

Retirement or replacement (for example, archiving,<br>
destruction, and/or revocation) of keys as deemed necessary<br>
when the integrity of the key has been weakened (for example,<br>
departure of an employee with knowledge of a clear-text key),<br>
or keys are suspected of being compromised.<br>
Note: If retired or replaced cryptographic keys need to be<br>
retained, these keys must be securely archived (for example, by<br>
using a key-encryption key). Archived cryptographic keys<br>
should be used only for decryption/verification purposes.<br>
<br>
<h4>3.6.6 Split knowledge and dual control</h4>

If manual clear-text cryptographic key management<br>
operations are used, these operations must be managed using<br>
split knowledge and dual control (for example, requiring two or<br>
three people, each knowing only their own key component, to<br>
reconstruct the whole key).<br>
<br>
Note: Examples of manual key management operations<br>
include, but are not limited to: key generation, transmission,<br>
loading, storage and destruction.<br>
<br>
<h4>3.6.7 Prevention of unauthorized substitution</h4>

Prevention of unauthorized substitution of cryptographic keys.<br>
<br>
<h4>3.6.8 Key custodians</h4>

Requirement for cryptographic key custodians to formally acknowledge that they understand and accept their key-custodian responsibilities.<br>
