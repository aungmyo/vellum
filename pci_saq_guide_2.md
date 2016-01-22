# Guide: SAQ 2: Passwords and security #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

<b>Do not use vendor-supplied defaults for system passwords and other security parameters.</b>

Malicious individuals (external and internal to an entity) often use vendor default passwords and other vendor default settings to compromise
systems. These passwords and settings are well known by hacker communities and are easily determined via public information.

## 2.1 Change vendor-supplied default passwords ##

Always change vendor-supplied defaults before installing a
system on the network, including but not limited to passwords,
simple network management protocol (SNMP) community strings,
and elimination of unnecessary accounts.

<i>Malicious individuals (external and internal to a company) often use vendor default<br>
settings, account names, and passwords to compromise systems. These settings<br>
are well known in hacker communities and leave your system highly vulnerable to<br>
attack.<br>
</i>

2.1.1 For wireless environments connected to the cardholder
data environment or transmitting cardholder data, change
wireless vendor defaults, including but not limited to default
wireless encryption keys, passwords, and SNMP community
strings.

<i>
Many users install these devices without management approval and do not change<br>
default settings or configure security settings. If wireless networks are not<br>
implemented with sufficient security configurations (including changing default<br>
settings), wireless sniffers can eavesdrop on the traffic, easily capture data and<br>
passwords, and easily enter and attack your network. In addition, the key<br>
exchange protocol for the older version of 802.11x encryption (WEP) has been<br>
broken and can render the encryption useless. Verify that firmware for devices are<br>
updated to support more secure protocols (for example, WPA2).<br>
</i>

## 2.2 Configuration standards for system components ##

Develop configuration standards for all system components.
Assure that these standards address all known security
vulnerabilities and are consistent with industry-accepted system
hardening standards.

Sources of industry-accepted system hardening standards may include, but are not limited to:
  * Center for Internet Security (CIS)
  * International Organization for Standardization (ISO)
  * Sys Admin Audit Network Security (SANS)
  * National Institute of Standards Technology (NIST)

<i>There are known weaknesses with many operating systems, databases, and<br>
enterprise applications, and there are also known ways to configure these systems<br>
to fix security vulnerabilities. To help those that are not security experts, security<br>
organizations have established system-hardening recommendations, which advise<br>
how to correct these weaknesses. If systems are left with these weaknesses—for<br>
example, weak file settings or default services and protocols (for services or<br>
protocols that are often not needed)—an attacker will be able to use multiple,<br>
known exploits to attack vulnerable services and protocols, and thereby gain<br>
access to your organization's network. Source websites where you can learn more<br>
about industry best practices that can help you implement configuration standards<br>
include, but are not limited to: www.nist.gov, www.sans.org, www.cisecurity.org,<br>
www.iso.org.<br>
<br>
System configuration standards must also be kept up to date to ensure that newly<br>
identified weaknesses are corrected prior to a system being installed on the<br>
network.<br>
</i>

<b>2.2.1 One primary function per server.</b>

Implement only one primary function per server to prevent
functions that require different security levels from co-existing
on the same server. (For example, web servers, database
servers, and DNS should be implemented on separate servers.)

<i>Note: Where virtualization technologies are in use, implement<br>
only one primary function per virtual system component.<br>
</i>

<i>
This is intended to ensure your organization's system configuration standards and<br>
related processes address server functions that need to have different security<br>
levels, or that may introduce security weaknesses to other functions on the same<br>
server. For example:<br>
<br>
1. A database, which needs to have strong security measures in place, would be<br>
at risk sharing a server with a web application, which needs to be open and<br>
directly face the Internet.<br>
<br>
2. Failure to apply a patch to a seemingly minor function could result in a<br>
compromise that impacts other, more important functions (such as a database)<br>
on the same server.<br>
<br>
This requirement is meant for all servers within the cardholder data environment<br>
(usually Unix, Linux, or Windows based). This requirement may not apply to<br>
systems which have the ability to natively implement security levels on a single<br>
server (e.g. mainframe).<br>
<br>
Where virtualization technologies are used, each virtual component (e.g. virtual<br>
machine, virtual switch, virtual security appliance, etc.) should be considered a<br>
“server” boundary. Individual hypervisors may support different functions, but a<br>
single virtual machine should adhere to the “one primary function” rule. Under this<br>
scenario, compromise of the hypervisor could lead to the compromise of all system<br>
functions. Consequently, consideration should also be given to the risk level when<br>
locating multiple functions or components on a single physical system.<br>
</i>

<b>2.2.2 Enable only necessary and secure services</b>

Enable only necessary and secure services, protocols,
daemons, etc., as required for the function of the system.

Implement security features for any required services, protocols
or daemons that are considered to be insecure. For example,
use secured technologies such as SSH, S-FTP, SSL, or IPSec
VPN to protect insecure services such as NetBIOS, file-sharing,
Telnet, FTP, etc.

<i>
As stated in Requirement 1.1.5, there are many protocols that a business may<br>
need (or have enabled by default) that are commonly used by malicious individuals<br>
to compromise a network. To ensure that only the necessary services and<br>
protocols are enabled and that all insecure services and protocols are adequately<br>
secured before new servers are deployed, this requirement should be part of your<br>
organization's configuration standards and related processes.<br>
</i>

<b>2.2.3 Configure system security parameters to prevent misuse.</b>

<i>
This is intended to ensure your organization’s system configuration standards and<br>
related processes specifically address security settings and parameters that have<br>
known security implications.<br>
</i>

<b>2.2.4 Remove all unnecessary functionality</b>

Remove all unnecessary functionality, such as scripts,
drivers, features, subsystems, file systems, and unnecessary
web servers.

<i>
The server-hardening standards must include processes to address unnecessary<br>
functionality with specific security implications (like removing/disabling FTP or the<br>
web server if the server will not be performing those functions).<br>
</i>

## 2.3 Encrypt all administrative access ##

Encrypt all non-console administrative access using strong
cryptography. Use technologies such as SSH, VPN, or SSL/TLS
for web-based management and other non-console administrative
access.

<i>
If remote administration is not done with secure authentication and encrypted<br>
communications, sensitive administrative or operational level information (like<br>
administrator’s passwords) can be revealed to an eavesdropper. A malicious<br>
individual could use this information to access the network, become administrator,<br>
and steal data.<br>
</i>

## 2.4 Shared hosting providers ##

Shared hosting providers must protect each entity’s hosted
environment and cardholder data. These providers must meet
specific requirements as detailed in Appendix A: Additional PCI
DSS Requirements for Shared Hosting Providers.

<i>
This is intended for hosting providers that provide shared hosting environments for<br>
multiple clients on the same server. When all data is on the same server and under<br>
control of a single environment, often the settings on these shared servers are not<br>
manageable by individual clients, allow clients to add insecure functions and<br>
scripts that impact the security of all other client environments; and thereby make it<br>
easy for a malicious individual to compromise one client's data and thereby gain<br>
access to all other clients' data. See Appendix A.<br>
</i>