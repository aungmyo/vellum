# PCI SAQ D Overview #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

http://en.wikipedia.org/wiki/Payment_Card_Industry_Data_Security_Standard

https://www.pcisecuritystandards.org

## Vulnerabilities ##

Post-mortem compromise analysis has shown common security weaknesses that are addressed by PCI
DSS, but were not in place in the organizations when the compromises occurred. PCI DSS was designed
and includes detailed requirements for exactly this reason—to minimize the chance of compromise and
the effects if a compromise does occur.

Investigations after compromises consistently show common PCI DSS violations, including but not limited
to:

  * Storage of magnetic stripe data (Requirement 3.2). It is important to note that many compromised entities are unaware that their systems are storing this data.

  * Inadequate access controls due to improperly installed merchant POS systems, allowing malicious users in via paths intended for POS vendors (Requirements 7.1, 7.2, 8.2 and 8.3)

  * Default system settings and passwords not changed when system was set up (Requirement 2.1)

  * Unnecessary and insecure services not removed or secured when system was set up (Requirements 2.2.2 and 2.2.4)

  * Poorly coded web applications resulting in SQL injection and other vulnerabilities, which allow access to the database storing cardholder data directly from the web site (Requirement 6.5)

  * Missing and outdated security patches (Requirement 6.1)

  * Lack of logging (Requirement 10)

  * Lack of monitoring (via log reviews, intrusion detection/prevention, quarterly vulnerability scans, and file integrity monitoring systems) (Requirements 10.6, 11.2, 11.4 and 11.5)

  * Poorly implemented network segmentation resulting in the cardholder data environment being unknowingly exposed to weaknesses in other parts of the network that have not been secured according to PCI DSS (for example, from unsecured wireless access points and vulnerabilities introduced via employee e-mail and web browsing) (Requirements 1.2, 1.3 and 1.4)

## Requirements ##

<b>Build and Maintain a Secure Network</b>

Requirement 1: Install and maintain a firewall configuration to protect cardholder data

Requirement 2: Do not use vendor-supplied defaults for system passwords and other security parameters

<b>Protect Cardholder Data</b>

Requirement 3: Protect stored cardholder data

Requirement 4: Encrypt transmission of cardholder data across open, public networks

<b>Maintain a Vulnerability Management Program</b>

Requirement 5: Use and regularly update anti-virus software or programs

Requirement 6: Develop and maintain secure systems and applications

<b>Implement Strong Access Control Measures</b>

Requirement 7: Restrict access to cardholder data by business need-to-know

Requirement 8: Assign a unique ID to each person with computer access

Requirement 9: Restrict physical access to cardholder data

<b>Regularly Monitor and Test Networks</b>

Requirement 10: Track and monitor all access to network resources and cardholder data

Requirement 11: Regularly test security systems and processes Maintain an Information Security Policy

Requirement 12: Maintain a policy that addresses information security for all personnel

## Questionaire ##

1. Install and maintain a firewall configuration to protect cardholder data

2. Do not use vendor-supplied defaults for system passwords and other security parameters

3. Protect stored cardholder data

4. Encrypt transmission of cardholder data across open, public networks

5. Use and regularly update anti-virus software or programs

6. Develop and maintain secure systems and applications

7. Restrict access to cardholder data by business need to know

8. Assign a unique ID to each person with computer access

9. Restrict physical access to cardholder data

10. Track and monitor all access to network resources and cardholder data

11. Regularly test security systems and processes

12. Maintain a policy that addresses information security for all personnel
