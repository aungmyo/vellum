# SAQ 5: Software updates #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

<b>
Guidance for Requirements 5 and 6: Maintain a Vulnerability Management Program<br>
<br>
Requirement 5: Use and regularly update anti-virus software or programs<br>
</b>

<i>
Malicious software, commonly referred to as “malware”—including viruses, worms, and Trojans—enters the network during many business-<br>
approved activities including employee e-mail and use of the Internet, mobile computers, and storage devices, resulting in the exploitation of<br>
system vulnerabilities. Anti-virus software must be used on all systems commonly affected by malware to protect systems from current and<br>
evolving malicious software threats.<br>
</i>

## 5.1 Deploy anti-virus software ##

Deploy anti-virus software on all systems commonly
affected by malicious software (particularly personal computers
and servers).

<i>
There is a constant stream of attacks using widely published exploits, often "0 day"<br>
(published and spread throughout networks within an hour of discovery) against<br>
otherwise secured systems. Without anti-virus software that is updated regularly,<br>
these new forms of malicious software can attack and disable your network.<br>
<br>
Malicious software may be unknowingly downloaded and/or installed from the<br>
internet, but computers are also vulnerable when using removable storage devices<br>
such as CDs and DVDs, USB memory sticks and hard drives, digital cameras,<br>
personal digital assistants (PDAs) and other peripheral devices. Without anti-virus<br>
software installed, these computers may become access points into your network,<br>
and/or maliciously target information within the network.<br>
<br>
While systems that are commonly affected by malicious software typically do not<br>
include mainframes and most Unix systems (see more detail below), each entity<br>
must have a process according to PCI DSS Requirement 6.2 to identify and address<br>
new security vulnerabilities and update their configuration standards and processes<br>
accordingly. If another type of solution addresses the identical threats with a different<br>
methodology than a signature-based approach, it may still be acceptable to meet the<br>
requirement.<br>
<br>
Trends in malicious software related to operating systems an entity uses should be<br>
included in the identification of new security vulnerabilities, and methods to address<br>
new trends should be incorporated into the company's configuration standards and<br>
protection mechanisms as needed.<br>
<br>
Typically, the following operating systems are not commonly affected by malicious<br>
software: mainframes, and certain Unix servers (such as AIX, Solaris, and HP-Unix).<br>
However, industry trends for malicious software can change quickly and each<br>
organization must comply with Requirement 6.2 to identify and address new security<br>
vulnerabilities and update their configuration standards and processes accordingly.<br>
</i>

#### 5.1.1 Anti-virus ####

Ensure that all anti-virus programs are capable of
detecting, removing, and protecting against all known types of
malicious software.

<i>It is important to protect against ALL types and forms of malicious software.<br>
</i>

## 5.2 Anti-virus ##

Ensure that all anti-virus mechanisms are current, actively running, and generating audit logs.

<i>
The best anti-virus software is limited in effectiveness if it does not have current anti-<br>
virus signatures or if it isn't active in the network or on an individual's computer.<br>
<br>
Audit logs provide the ability to monitor virus activity and anti-virus reactions. Thus, it<br>
is imperative that anti-virus software be configured to generate audit logs and that<br>
these logs be managed in accordance with Requirement 10.<br>
</i>
