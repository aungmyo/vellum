# SAQ 4: Secure transmission #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

<b>Requirement 4: Encrypt transmission of cardholder data across open, public networks</b>

Sensitive information must be encrypted during transmission over networks that are easily accessed by malicious individuals. Misconfigured
wireless networks and vulnerabilities in legacy encryption and authentication protocols continue to be targets of malicious individuals who exploit
these vulnerabilities to gain privileged access to cardholder data environments.

## 4.1 Use strong cryptography and security protocols ##

Use strong cryptography and security protocols (for example,
SSL/TLS, IPSec, SSH, etc.) to safeguard sensitive cardholder
data during transmission over open, public networks.
Examples of open, public networks that are in scope of the PCI
DSS include but are not limited to:
  * The Internet
  * Wireless technologies,
  * Global System for Mobile communications (GSM)
  * General Packet Radio Service (GPRS).

<i>
Sensitive information must be encrypted during transmission over public networks,<br>
because it is easy and common for a malicious individual to intercept and/or divert<br>
data while in transit.<br>
<br>
For example, Secure Sockets Layer (SSL) encrypts web pages and the data<br>
entered into them. When using SSL secured websites, ensure “https” is part of the<br>
URL.<br>
<br>
Note that some protocol implementations (such as SSL version 2.0 and SSH<br>
version 1.0) have documented vulnerabilities, such as buffer overflows, that an<br>
attacker can use to gain control of the affected system. Whichever security<br>
protocol is used, ensure it is configured to use only secure configurations and<br>
versions to prevent an insecure connection being used.<br>
</i>

#### 4.1.1 Wireless network encryption ####

Ensure wireless networks transmitting cardholder data or
connected to the cardholder data environment, use industry
best practices (for example, IEEE 802.11i) to implement strong
encryption for authentication and transmission.

Note: The use of WEP as a security control was prohibited as of 30 June 2010.

<i>
Malicious users use free and widely available tools to eavesdrop on wireless<br>
communications. Use of strong cryptography can limit disclosure of sensitive<br>
information across the network. Many known compromises of cardholder data<br>
stored only in the wired network originated when a malicious user expanded<br>
access from an insecure wireless network. Examples of wireless implementations<br>
requiring strong cryptography include but are not limited to GPRS, GSM, WIFI,<br>
satellite, and Bluetooth.<br>
<br>
Strong cryptography for authentication and transmission of cardholder data is<br>
required to prevent malicious users from gaining access to the wireless network—<br>
the data on the network—or utilizing the wireless networks to get to other internal<br>
networks or data. WEP encryption should never be used as the sole means of<br>
encrypting data over a wireless channel since it is not considered strong<br>
cryptography, it is vulnerable due to weak initialization vectors in the WEP key-<br>
exchange process, and it lacks required key rotation. An attacker can use freely<br>
available brute-force cracking tools to easily penetrate WEP encryption.<br>
<br>
Current wireless devices should be upgraded (example: upgrade access point<br>
firmware to WPA2) to support strong encryption. If current devices cannot be<br>
upgraded, new equipment should be purchased or other compensating controls<br>
implemented to provide strong encryption.<br>
</i>

## 4.2 Never send unprotected PANs by end-user messaging ##

Never send unprotected PANs by end-user messaging technologies (for example, e-mail, instant messaging, chat, etc.).

<i>
E-mail, instant messaging, and chat can be easily intercepted by packet-sniffing<br>
during delivery traversal across internal and public networks. Do not utilize these<br>
messaging tools to send PAN unless they provide strong encryption.<br>
</i>