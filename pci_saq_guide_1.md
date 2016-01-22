# SAQ 1: Firewall #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

<b>
Part of: Build and Maintain a Secure Network.<br>
<br>
Requirement 1: Install and maintain a firewall configuration to protect cardholder data<br>
</b>

Firewalls are devices that control computer traffic allowed between an entity’s networks (internal) and untrusted networks (external), as well as
traffic into and out of more sensitive areas within an entity’s internal trusted networks. The cardholder data environment is an example of a more
sensitive area within an entity’s trusted network.

A firewall examines all network traffic and blocks those transmissions that do not meet the specified security criteria.

All systems must be protected from unauthorized access from untrusted networks, whether entering the system via the Internet as e-commerce,
employee Internet access through desktop browsers, employee e-mail access, dedicated connections such as business-to-business connections,
via wireless networks, or via other sources. Often, seemingly insignificant paths to and from untrusted networks can provide unprotected pathways
into key systems. Firewalls are a key protection mechanism for any computer network.

Other system components may provide firewall functionality, provided they meet the minimum requirements for firewalls as provided in Requirement 1.
Where other system components are used within the cardholder data environment to provide firewall functionality, these devices
must be included within the scope and assessment of Requirement 1.

## 1.1 Firewall and router configuration standards ##

<b>Establish firewall and router configuration standards that include the following.</b>

<i>
Firewalls and routers are key components of the architecture that controls entry to<br>
and exit from the network. These devices are software or hardware devices that<br>
block unwanted access and manage authorized access into and out of the<br>
network. Without policies and procedures in place to document how staff should<br>
configure firewalls and routers, a business could easily lose its first line of defense<br>
in data-protection. The policies and procedures will help to ensure that the<br>
organization’s first line of defense in the protection of its data remains strong.<br>
<br>
Virtual environments where data flows do not transit a physical network should be<br>
assessed to ensure appropriate network segmentation is achieved.<br>
</i>

<b>1.1.1 A formal process for approving and testing all network connections and changes to the firewall and router configurations</b>

A policy and process for approving and testing all connections and changes to the
firewalls and routers will help prevent security problems caused by
misconfiguration of the network, router, or firewall.

Data flows between virtual machines should be included in policy and process.

<b>1.1.2 Current network diagram with all connections to cardholder data, including any wireless networks</b>

Network diagrams enable the organization to identify the location of all its network
devices. Additionally, the network diagram can be used to map the data flow of
cardholder data across the network and between individual devices in order to fully
understand the scope of the cardholder data environment. Without current network
and data flow diagrams, devices with cardholder data may be overlooked and may
unknowingly be left out of the layered security controls implemented for PCI DSS
and thus vulnerable to compromise.

Network and data flow diagrams should include virtual system components and
document Intra-host data flows.

<b>1.1.3 Requirements for a firewall at each Internet connection and between any demilitarized zone (DMZ) and the internal network zone</b>

Using a firewall on every connection coming into (and out of) the network allows
the organization to monitor and control access in and out, and to minimize the
chances of a malicious individual’s obtaining access to the internal network.

<b>1.1.4 Description of groups, roles, and responsibilities for logical management of network components</b>

This description of roles and assignment of responsibility ensures that someone is
clearly responsible for the security of all components and is aware of their
responsibility, and that no devices are left unmanaged.

<b>1.1.5 Document business justification for services, protocols, and ports</b>

Documentation and business justification for use of all services, protocols, and ports allowed, including documentation of security features implemented for those protocols considered to be insecure.

Examples of insecure services, protocols, or ports include but
are not limited to FTP, Telnet, POP3, IMAP, and SNMP.

Compromises often happen due to unused or insecure service and ports, since
these often have known vulnerabilities—and many organizations are vulnerable to
these types of compromises because they do not patch security vulnerabilities for
services, protocols, and ports they don't use (even though the vulnerabilities are
still present). Each organization should clearly decide which services, protocols,
and ports are necessary for their business, document them for their records, and
ensure that all other services, protocols, and ports and disabled or removed. Also,
organizations should consider blocking all traffic and only re-opening those ports
once a need has been determined and documented.

Additionally, there are many services, protocols, or ports that a business may need
(or have enabled by default) that are commonly used by malicious individuals to
compromise a network. If these insecure services, protocols, or ports are
necessary for business, the risk posed by use of these protocols should be clearly
understood and accepted by the organization, the use of the protocol should be
justified, and the security features that allow these protocols to be used securely
should be documented and implemented. If these insecure services, protocols, or
ports are not necessary for business, they should be disabled or removed.

<b>1.1.6 Review firewall and router rule sets at least every six months</b>

This review gives the organization an opportunity at least every six months to clean
up any unneeded, outdated, or incorrect rules, and ensure that all rule sets allow
only authorized services and ports that match business justifications.
It is advisable to undertake these reviews on a more frequent basis, such as
monthly, to ensure that the rule sets are current and match the needs of the
business without opening security holes and running unnecessary risks.

## 1.2 Restrict connections ##

<b>Restrict connections between untrusted networks and the cardholder data environment</b>

Build firewall and router configurations that restrict
connections between untrusted networks and any system
components in the cardholder data environment.

Note: An “untrusted network” is any network that is external to
the networks belonging to the entity under review, and/or which is
out of the entity's ability to control or manage.

It is essential to install network protection, namely a system component with (at a
minimum) stateful inspection firewall capability, between the internal, trusted
network and any other untrusted network that is external and/or out of the entity’s
ability to control or manage. Failure to implement this measure correctly means
that the entity will be vulnerable to unauthorized access by malicious individuals or
software.

If firewall functionality is installed but does not have rules that control or limit
certain traffic, malicious individuals may still be able to exploit vulnerable protocols
and ports to attack your network.

<b>1.2.1 Restrict inbound and outbound traffic to that which is necessary for the cardholder data environment.</b>

This requirement is intended to prevent malicious individuals from accessing the
organization's network via unauthorized IP addresses or from using services,
protocols, or ports in an unauthorized manner (for example, to send data they've
obtained from within your network out to an untrusted server.

All firewalls should include a rule that denies all inbound and outbound traffic not
specifically needed. This will prevent inadvertent holes that would allow other,
unintended and potentially harmful traffic in or out.

<b>1.2.2 Secure and synchronize router configuration files.</b>

While running configuration files are usually implemented with secure settings, the
start-up files (routers run these files only upon re-start) may not be implemented
with the same secure settings because they only run occasionally. When a router
does re-start without the same secure settings as those in the running
configuration files, it may result in weaker rules that allow malicious individuals into
the network, because the start-up files may not be implemented with the same
secure settings as the running configuration files.

<b>1.2.3 Install perimeter firewalls between any wireless networks and the cardholder data environment</b>

Install perimeter firewalls between any wireless networks and the cardholder data environment, and configure these firewalls to deny or control (if such traffic is necessary for business purposes) any traffic from the wireless environment into the cardholder data environment.

The known (or unknown) implementation and exploitation of wireless technology
within a network is a common path for malicious individuals to gain access to the
network and cardholder data. If a wireless device or network is installed without a
company’s knowledge, a malicious individual could easily and “invisibly” enter the
network. If firewalls do not restrict access from wireless networks into the payment
card environment, malicious individuals that gain unauthorized access to the
wireless network can easily connect to the payment card environment and
compromise account information.

Firewalls must be installed between all wireless networks and the CDE, regardless
of the purpose of the environment to which the wireless network is connected. This
may include, but is not limited to, corporate networks, retail stores, warehouse
environments, etc.

## 1.3 Prohibit direct public access ##

<b>Prohibit direct public access between the Internet and any system component in the cardholder data environment.</b>

<i>
A firewall's intent is to manage and control all connections between public systems<br>
and internal systems (especially those that store, process or transmit cardholder<br>
data). If direct access is allowed between public systems and the CDE, the<br>
protections offered by the firewall are bypassed, and system components storing<br>
cardholder data may be exposed to compromise.<br>
</i>

<b>1.3.1 Implement a DMZ</b>

Implement a DMZ to limit inbound traffic to only system components that provide authorized publicly accessible services, protocols, and ports.

The DMZ is that part of the network that manages connections between the
Internet (or other untrusted networks), and internal services that an organization
needs to have available to the public (like a web server). It is the first line of
defense in isolating and separating traffic that needs to communicate with the
internal network from traffic that does not.

This functionality is intended to prevent malicious individuals from accessing the
organization's network via unauthorized IP addresses or from using services,
protocols, or ports in an unauthorized manner.

<b>1.3.2 Limit inbound Internet traffic to IP addresses within the DMZ.</b>

Termination of IP connections at the DMZ provides opportunity for inspection and
restriction of source/destination, and/or inspection / blocking of content, thus
preventing unfiltered access between untrusted and trusted environments.

<b>1.3.3 Do not allow any direct connections inbound or outbound for traffic between the Internet and the cardholder data environment.</b>

Termination of IP connections both inbound and outbound provides opportunity for
inspection and restriction of source/destination, and/or inspection / blocking of
content, thus preventing unfiltered access between untrusted and trusted
environments. This helps prevent, for example, malicious individuals from sending
data they've obtained from within your network out to an external untrusted server
in an untrusted network.

<b>1.3.4 Do not allow internal addresses to pass from the Internet into the DMZ.</b>

Normally a packet contains the IP address of the computer that originally sent it.
This allows other computers in the network to know where it came from. In certain
cases, this sending IP address will be spoofed by malicious individuals.

For example, malicious individuals send a packet with a spoofed address, so that
(unless your firewall prohibits it) the packet will be able to come into your network
from the Internet, looking like it is internal, and therefore legitimate, traffic. Once
the malicious individual is inside your network, they can begin to compromise your
systems.

Ingress filtering is a technique you can use on your firewall to filter packets coming
into your network to, among other things, ensure packets are not “spoofed” to look
like they are coming from your own internal network.

For more information on packet filtering, consider obtaining information on a
corollary technique called “egress filtering.”

<b>1.3.5 Do not allow unauthorized outbound traffic from the cardholder data environment to the Internet.</b>

All traffic outbound from inside the cardholder data environment should be
evaluated to ensure that outbound traffic follows established, authorized rules.

Connections should be inspected to restrict traffic to only authorized
communications (for example by restricting source/destination addresses/ports,
and/or blocking of content).

Where environments have no inbound connectivity allowed, outbound connections
may be achieved via architectures or system components that interrupt and inspect
the IP connectivity.

<b>1.3.6 Implement stateful inspection, also known as dynamic packet filtering.</b>

(That is, only “established” connections are allowed into the network.)

A firewall that performs stateful packet inspection keeps "state" (or the status) for
each connection to the firewall. By keeping "state," the firewall knows whether
what appears to be a response to a previous connection is truly a response (since
it "remembers" the previous connection) or is a malicious individual or software
trying to spoof or trick the firewall into allowing the connection.

<b>1.3.7 Store cardholder data in a secure internal network zone.</b>

Place system components that store cardholder data (such as a database) in an internal network zone, segregated from the DMZ and other untrusted networks.

Cardholder data requires the highest level of information protection. If cardholder
data is located within the DMZ, access to this information is easier for an external
attacker, since there are fewer layers to penetrate.

Note: the intent of this requirement does not include storage in volatile memory.

<b>1.3.8 Do not disclose private IP addresses and routing information to unauthorized parties.</b>

Note: Methods to obscure IP addressing may include, but are not limited to:
  * Network Address Translation (NAT)
  * Placing servers containing cardholder data behind proxy servers/firewalls or content caches,
  * Removal or filtering of route advertisements for private networks that employ registered addressing,
  * Internal use of RFC1918 address space instead of registered addresses.

Restricting the broadcast of IP addresses is essential to prevent a hacker
“learning” the IP addresses of the internal network, and using that information to
access the network.

Effective means to meet the intent of this requirement may vary depending on the
specific networking technology being used in your environment. For example, the
controls used to meet this requirement may be different for IPv4 networks than for
IPv6 networks.

One technique to prevent IP address information from being discovered on an IPv4
network is to implement Network Address translation (NAT). NAT, which is typically
managed by the firewall, allows an organization to have internal addresses that are
visible only inside the network and external address that are visible externally. If a
firewall does not “hide” or mask the IP addresses of the internal network, a
malicious individual could discover internal IP addresses and attempt to access the
network with a spoofed IP address.

For IPv4 networks, the RFC1918 address space is reserved for internal
addressing, and should not be routable on the Internet. As such, it is preferred for
IP addressing of internal networks. However, organizations may have reasons to
utilize non-RFC1918 address space on the internal network. In these
circumstances, prevention of route advertisement or other techniques should be
used to prevent internal address space being broadcast on the Internet or
disclosed to unauthorized parties.

## 1.4 Personal firewall ##

<b>Install personal firewall software on any mobile and/or employee-owned computers.</b>

Install personal firewall software on any mobile and/or
employee-owned computers with direct connectivity to the
Internet (for example, laptops used by employees), which are
used to access the organization’s network.

<i>
If a computer does not have a firewall or anti-virus program installed, spyware,<br>
Trojans, viruses, worms and rootkits (malware) may be downloaded and/or<br>
installed unknowingly. The computer is even more vulnerable when directly<br>
connected to the Internet and not behind the corporate firewall. Malware loaded on<br>
a computer when not behind the corporate firewall can then maliciously target<br>
information within the network when the computer is re-connected to the corporate<br>
network.<br>
<br>
Note: The intent of this requirement applies to remote access computers<br>
regardless of whether they are employee owned or company owned. Systems that<br>
cannot be managed by corporate policy introduce weaknesses to the perimeter<br>
and provide opportunities that malicious individuals may exploit.<br>
</i>
