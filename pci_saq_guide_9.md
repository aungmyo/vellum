# SAQ 9: Restrict physical access to cardholder data #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

Any physical access to data or systems that house cardholder data provides the opportunity for individuals to access devices or data and to
remove systems or hardcopies, and should be appropriately restricted. For the purposes of Requirement 9, “onsite personnel” refers to full-time
and part-time employees, temporary employees, contractors and consultants who are physically present on the entity’s premises. A “visitor” refers
to a vendor, guest of any onsite personnel, service workers, or anyone who needs to enter the facility for a short duration, usually not more than
one day. “Media” refers to all paper and electronic media containing cardholder data.

## 9.1 Limit and monitor physical access ##

Use appropriate facility entry controls to limit and monitor physical access to systems in the cardholder data environment.

Without physical access controls, unauthorized persons could potentially gain
access to the building and to sensitive information, and could alter system
configurations, introduce vulnerabilities into the network, or destroy or steal
equipment.

<b>9.1.1 Use video cameras and/or access control mechanisms</b>

Use video cameras and/or access control mechanisms to
monitor individual physical access to sensitive areas. Review
collected data and correlate with other entries.
Store for at least three months, unless otherwise restricted by law.

Note: “Sensitive areas” refers to any data center, server room
or any area that houses systems that store, process, or transmit
cardholder data. This excludes the areas where only point-of-
sale terminals are present, such as the cashier areas in a retail
store.

When investigating physical breaches, these controls can help identify individuals
that physically access those sensitive areas storing cardholder data. Examples of
sensitive areas include corporate database server rooms, back-end server room of
a retail location that stores cardholder data, and storage areas for large quantities
of cardholder data,

<b>9.1.2 Restrict physical access to publicly accessible network jacks.</b>

For example, areas accessible to visitors should not have
network ports enabled unless network access is explicitly
authorized.

Restricting access to network jacks will prevent malicious individuals from plugging
into readily available network jacks that may allow them access into internal
network resources. Consider turning off network jacks while not in use, and
reactivating them only while needed. In public areas such as conference rooms,
establish private networks to allow vendors and visitors to access Internet only so
that they are not on your internal network.

<b>9.1.3 Restrict physical access to networking equipment</b>

Restrict physical access to wireless access points,
gateways, handheld devices, networking/communications
hardware, and telecommunication lines.

Without security over access to wireless components and devices, malicious users
could use your organization’s unattended wireless devices to access your network
resources, or even connect their own devices to your wireless network to gain
unauthorized access. Additionally, securing networking and communications
hardware prevents malicious users from intercepting network traffic or physically
connecting their own devices to your wired network resources.
Consider placing wireless access points, gateways and networking/
communications hardware in secure storage areas, such as within locked closets
or server rooms. For wireless networks, ensure strong encryption is enabled. Also
consider enabling automatic device lockout on wireless handheld devices after a
long idle period, and set your devices to require a password when powering on.

## 9.2 Distinguish between onsite personnel and visitors ##

Develop procedures to easily distinguish between onsite personnel and visitors, especially in areas where cardholder data is accessible.

Without badge systems and door controls, unauthorized and malicious users can
easily gain access to your facility to steal, disable, disrupt, or destroy critical
systems and cardholder data. For optimum control, consider implementing badge
or card access system in and out of work areas that contain cardholder data.
Identifying authorized visitors so they are easily distinguished from onsite
personnel prevents unauthorized visitors from being granted access to areas
containing cardholder data.

## 9.3 Visitor controls ##

Visitor controls are important to reduce the ability of unauthorized and malicious
persons to gain access to your facilities (and potentially, to cardholder data).

Visitor controls are important to ensure visitors only enter areas they are
authorized to enter, that they are identifiable as visitors so personnel can monitor
their activities, and that their access is restricted to just the duration of their
legitimate visit.

<b>9.3.1 Authorized before entering areas where cardholder data is processed or maintained.</b>

<b>9.3.2 Given a physical token (for example, a badge or access device) that expires and that identifies the visitors as not onsite personnel.</b>

<b>9.3.3 Asked to surrender the physical token before leaving the facility or at the date of expiration.</b>

## 9.4 Use a visitor log to maintain a physical audit trail of visitor activity ##

Use a visitor log to maintain a physical audit trail of visitor activity. Document the visitor’s name, the firm represented, and
the onsite personnel authorizing physical access on the log.
Retain this log for a minimum of three months, unless otherwise
restricted by law.

<i>A visitor log documenting minimum information on the visitor is easy and<br>
inexpensive to maintain and will assist, during a potential data breach<br>
investigation, in identifying physical access to a building or room, and potential<br>
access to cardholder data. Consider implementing logs at the entry to facilities and<br>
especially into zones where cardholder data is present.<br>
</i>

## 9.5 Store media back-ups in a secure location ##

Store media back-ups in a secure location, preferably an off-site facility, such as an alternate or back-up site, or a commercial
storage facility. Review the location’s security at least annually.

<i>
If stored in a non-secured facility, backups that contain cardholder data may easily<br>
be lost, stolen, or copied for malicious intent. For secure storage, consider<br>
contracting with a commercial data storage company OR, for a smaller entity,<br>
using a safe-deposit box at a bank.<br>
</i>

## 9.6 Physically secure all media ##

Cardholder data is susceptible to unauthorized viewing, copying, or scanning if it is
unprotected while it is on removable or portable media, printed out, or left on
someone’s desk.

## 9.7 Maintain strict control over the distribution of media ##

Maintain strict control over the internal or external distribution of any kind of media.

Procedures and processes help protect cardholder data on media distributed to
internal and/or external users. Without such procedures data can be lost or stolen
and used for fraudulent purposes.

<b>9.7.1 Classify media so the sensitivity of the data can be determined.</b>

It is important that media be identified such that its classification status can be
easily discernable. Media not identified as confidential may not be adequately
protected or may be lost or stolen.

<b>9.7.2 Send the media by secured courier or other delivery method that can be accurately tracked.</b>

Media may be lost or stolen if sent via a non-trackable method such as regular
postal mail. Use the services of a secure courier to deliver any media that contains
cardholder data, so that you can use their tracking systems to maintain inventory
and location of shipments.

## 9.8 Ensure management approves removal of media ##

Ensure management approves any and all media that is moved from a secured area (especially when media is distributed to individuals).

<i>
Cardholder data leaving secure areas without a process approved by management<br>
can lead to lost or stolen data. Without a firm process, media locations are not<br>
tracked, nor is there a process for where the data goes or how it is protected.<br>
</i>

## 9.9 Maintain strict control over media ##

Maintain strict control over the storage and accessibility of media.

Without careful inventory methods and storage controls, stolen or missing media
could go unnoticed for an indefinite amount of time.

<b>9.9.1 Properly maintain inventory logs of all media and conduct media inventories at least annually.</b>

If media is not inventoried, stolen or lost media may not be noticed for a long time or at all.

## 9.10 Destroy media when it is no longer needed for business or legal reasons ##

If steps are not taken to destroy information contained on hard disks, portable
drives, CD/DVDs, or paper prior to disposal, malicious individuals may be able to
retrieve information from the disposed media, leading to a data compromise. For
example, malicious individuals may use a technique known as “dumpster diving,”
where they search through trash cans and recycle bins looking for information they
can use to launch an attack.

Examples of methods for securely destroying electronic media include secure
wiping, degaussing, or physical destruction (such as grinding or shredding hard
disks).

<b>9.10.1 Shred, incinerate, or pulp hardcopy materials so that cardholder data cannot be reconstructed.</b>

<b>9.10.2 Render cardholder data on electronic media unrecoverable so that cardholder data cannot be reconstructed.</b>








