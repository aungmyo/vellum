# Security #

We consider the PCI DSS documentation as a guide to best practices.

For example, PCI recommends firewalling, change control, IDS (intrusion detection), and FIM (file integrity monitoring).



&lt;hr&gt;


## Documentation ##

In general, for PCI compliance we need documentation laying out your policy, procedures and controls, and of course to implement those controls to ensure our policy and procedures are being implemented as planned.

<b>Nomenclature</b>

<i>“Policy”</i> is what you want to achieve, <i>“Procedure”</i> is how you achieve it, and <i>“Control”</i> is how you ensure that it is being achieved.

<i>Procedures</i> dictate what actions (eg. every day, month, year) are required to implement our <i>policies</i>, and <i>controls</i> are checks to ensure that procedures are being carried out, eg. specific annual review etc.

Oftentimes the difference between “procedure” and “control” is blurred. Additionally documenting a procedure/control might implicitly document your policy. Nevertheless it might be helpful thinking of these three as separate issues.

For example, policy is that ssh is not on default port 22. A procedure/control might be monthly scan ensure that port 22 is not open.

<b>Example: Employee exit procedure</b>

For example, a commonly required procedure is employee exit check list eg. disable accounts on all machines etc. In the annual review procedure/control, we check all accounts on servers to ensure no logins for ex-employees etc.

In general we need the initial reactive procedure (eg. disable account when employee leaves), and the subsequent proactive controls eg. annually review all accounts on all systems.

## Overview ##

We present some general recommendations for network security.

#### Scheduled reviews ####

We schedule regular compliance reviews eg. firewall rules are review every month; bi-annual review should audit logins and permissions; and annual review ensure that keys are changed.

#### Monitoring ####

In accordance with PCI, we require IDS (intrusion detection) like <tt>snort</tt> plus FIM (file integrity monitoring) like <tt>tripwire</tt>.

Obviously <tt>snort</tt> and <tt>tripwire</tt> daily reports and such, need to be monitored on daily basis.

#### Firewalling ####

Place servers on a private network (using private IP numbers) behind a firewall (with NAT translation). Use a hardware firewall, eg. even just a simple Cisco or Netgear router,
with port forwarding to the servers themselves. This is primary barrier.

Then as a secondary firewall, configure iptables on the servers themselves. Therefore there are always two barriers, so if one is temporarily misconfigured, the other acts as a safety net. (Having said that,
controls should be in place to detect such problems "immediately.")

#### SSL ####

All public access should be via SSL, ie. web access via HTTPS (not HTTP), console access via <tt>ssh</tt>, and finally BizSwitch client connections must use two-way SSL authentification via SSL certificates.

<tt>ssh</tt> access should be via <tt>SSL</tt> keys only, ie. passwords are not allowed as these are vulnerable to a brute force attack. Private <tt>ssh</tt> keys mnust be passphrase protected,
since these resides on relative insecure laptops, mobiles, etc.

#### Application tiering ####

Ideally each server (or VM) has a single function eg. web server, database server, etcetera. The web server is accessible via the internet, and "vulnerable" in that sense.
However the database server (containing sensitive data like credit card info etc), is not exposed directly to the Internet, or even the web server. Consequently someone would be to break into
the web server first, and still not have access to sensitive data. Intrusion detection on the web server should give you enough time to prevent further penetration and access to the data.

#### Change control ####

PCI states that we require "a formal process for approving and testing all external network connections and changes to the firewall and router configurations. "

We need change control eg. install RedMine or trac, and create an issue for any change eg. to <tt>iptables</tt>. This change then gets formally considered and approved
before it is applied, and provides an historical record.

A centralised logging server is advisable, which is a secure indelible record of events.

#### Updates ####

Subscribe to CentOS mailing list for security advisorie, because we need to update packages when a vulnerability is discovered eg. in openssl - and you know via CentOS mailing list.
Additionally, update the machine regularly i.e. using "yum update."