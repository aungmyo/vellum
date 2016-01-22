# SAQ 8: Assign a unique ID to each person #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

<b>Assign a unique ID to each person with computer access</b>

Assigning a unique identification (ID) to each person with access ensures that each individual is uniquely accountable for his or her actions. When
such accountability is in place, actions taken on critical data and systems are performed by, and can be traced to, known and authorized users.

Note: These requirements are applicable for all accounts, including point-of-sale accounts, with administrative capabilities and all accounts used
to view or access cardholder data or to access systems with cardholder data. However, requirements 8.1, 8.2 and 8.5.8 through 8.5.15 are not
intended to apply to user accounts within a point-of-sale payment application that only have access to one card number at a time in order to
facilitate a single transaction (such as cashier accounts).

## 8.1 Assign all users a unique ID ##

Assign all users a unique ID before allowing them to access system components or card-holder data.

By ensuring each user is uniquely identified—instead of using one ID for several
employees—an organization can maintain individual responsibility for actions and
an effective audit trail per employee. This will help speed issue resolution and
containment when misuse or malicious intent occurs.

## 8.2 Authenticate all users ##

In addition to assigning a unique ID, employ at least one of the following methods to authenticate all users:
  * Something you know, such as a password or passphrase
  * Something you have, such as a token device or smart card
  * Something you are, such as a biometric

These authentication items, when used in addition to unique IDs, help protect
users’ unique IDs from being compromised (since the one attempting the
compromise needs to know both the unique ID and the password or other
authentication item).

A digital certificate is a valid option as a form of the authentication type “something you have” as long as it is unique.

## 8.3 Incorporate two-factor authentication for remote access ##

Incorporate two-factor authentication for remote access (network-level access originating from outside the network) to the network by employees, administrators, and third parties. (For example, remote authentication and dial-in service (RADIUS) with tokens; terminal access controller access control system (TACACS) with tokens; or other technologies that facilitate two-factor authentication.)

Note: Two-factor authentication requires that two of the three
authentication methods (see Req. 8.2 for descriptions of
authentication methods) be used for authentication. Using one
factor twice (e.g. using two separate passwords) is not
considered two-factor authentication.

Two-factor authentication requires two forms of authentication for higher-risk
accesses, such as those originating from outside your network. For additional
security, your organization can also consider using two-factor authentication when
accessing networks of higher security from networks of lower security—for
example, from corporate desktops (lower security) to production servers/databases
with cardholder data (high security).

This requirement is intended to apply to users that have remote access to the
network, where that remote access could lead to access to the cardholder data
environment.

In this context, remote access refers to network-level access originating from
outside an entity’s own network, either from the Internet or from an “untrusted”
network or system, such as a third party or an employee accessing the entity’s
network using his/her mobile computer. Internal LAN-to-LAN access (for example,
between two offices via secure VPN) is not considered remote access for the
purposes of this requirement.

If remote access is to an entity’s network that has appropriate segmentation, such
that remote users cannot access or impact the cardholder data environment, two-
factor authentication for remote access to that network would not required by PCI
DSS. However, two-factor authentication is required for any remote access to
networks with access to the cardholder data environment, and is recommended for
all remote access to the entity’s networks.

## 8.4 Render all passwords unreadable during transmission and storage ##

Render all passwords unreadable during transmission and storage on all system components using strong cryptography.

Many network devices and applications transmit the user ID and unencrypted
password across the network and/or also store the passwords without encryption.
A malicious individual can easily intercept the unencrypted or readable user ID and
password during transmission using a “sniffer,” or directly access the user IDs and
unencrypted passwords in files where they are stored, and use this stolen data to
gain unauthorized access. During transmission, the user credentials can be
encrypted or the tunnel can be encrypted

## 8.5 Ensure proper user identification and authentication management ##

Ensure proper user identification and authentication management for non-consumer users and administrators on all system components as follows.

Since one of the first steps a malicious individual will take to compromise a system
is to exploit weak or nonexistent passwords, it is important to implement good
processes for user identification and authentication management.

#### 8.5.1 Control addition, deletion, and modification of user IDs,credentials, and other identifier objects ####

To ensure users added to your systems are all valid and recognized users, the
addition, deletion, and modification of user IDs should be managed and controlled
by a small group with specific authority. The ability to manage these user IDs
should be limited to only this small group.

#### 8.5.2 Verify user identity before performing password resets ####

Many malicious individuals use "social engineering”—for example, calling a help
desk and acting as a legitimate user—to have a password changed so they can
utilize a user ID. Consider use of a “secret question” that only the proper user can
answer to help administrators identify the user prior to re-setting passwords.
Ensure such questions are secured properly and not shared.

#### 8.5.3 Set passwords for first-time use and resets to a unique value for each user and change immediately after the first use ####

If the same password is used for every new user set up, an internal user, former
employee, or malicious individual may know or easily discover this password, and
use it to gain access to accounts.

#### 8.5.4 Immediately revoke access for any terminated users ####

If an employee has left the company, and still has access to the network via their
user account, unnecessary or malicious access to cardholder data could occur.
This access could happen from the former employee or from a malicious user who
exploits the older and/or unused account. Consider implementing a process with
Human Resources for immediate notification when an employee is terminated so
that the user account can be quickly deactivated.

#### 8.5.5 Remove/disable inactive user accounts at least every 90 days ####

Existence of inactive accounts allows an unauthorized user exploit the unused
account to potentially access cardholder data.

#### 8.5.6 Enable accounts used by vendors for remote access only during the time period needed. Monitor vendor remote access accounts when in use ####

Allowing vendors (like POS vendors) to have 24/7 access into your network in case
they need to support your systems increases the chances of unauthorized access,
either from a user in the vendor’s environment or from a malicious individual who
finds and uses this always-ready external entry point into your network.

Monitoring of vendor access to the cardholder data environment applies in the
same way as it does for other users, such as organizational personnel. This
includes monitoring and logging of activities as required by PCI DSS Requirements
10.1 and 10.2, and verifying that usage of vendor remote accounts is in
accordance with the policy as defined in Requirements 12.3.8 and 12.3.9.

#### 8.5.8 Do not use group, shared, or generic accounts and passwords, or other authentication methods ####

If multiple users share the same authentication credentials (for example, user
account and password), it becomes impossible to assign accountability for, or to
have effective logging of, an individual’s actions, since a given action could have
been performed by anyone in the group that has knowledge of the authentication
credentials.

This requirement for unique IDs and complex passwords is often met within
administrative functions by using, for example, sudo or SSH such that the
administrator initially logs on with their own unique ID and password, and then
connects to the administrator account via sudo or SSH. Often direct root logins are
disabled to prevent use of this shared administrative account. This way, individual
accountability and audit trails are maintained. However, even with use of tools
such as sudo and SSH, the actual administrator IDs and passwords should also
meet PCI DSS requirements (if such accounts are not disabled) to prevent them
from being misused.

#### 8.5.9 Change user passwords at least every 90 days ####

Strong passwords are the first line of defense into a network since a malicious
individual will often first try to find accounts with weak or non-existent passwords.
There is more time for a malicious individual to find these weak accounts, and
compromise a network under the guise of a valid user ID, if passwords are short,
simple to guess, or valid for a long time without a change. Strong passwords can
be enforced and maintained per these requirements by enabling the password and
account security features that come with your operating system (for example,
Windows), networks, databases and other platforms.

#### 8.5.10 Require a minimum password length of at least seven characters ####

#### 8.5.11 Use passwords containing both numeric and alphabetic characters ####

#### 8.5.12  Do not allow an individual to submit a new password that is the same as any of the last four passwords he or she has used ####

#### 8.5.13  Limit repeated access attempts by locking out the user ID after not more than six attempts ####

Without account-lockout mechanisms in place, an attacker can continually attempt
to guess a password through manual or automated tools (for example, password
cracking), until they achieve success and gain access to a user’s account.

#### 8.5.14  Set the lockout duration to a minimum of 30 minutes or until administrator enables the user ID ####

If an account is locked out due to someone continually trying to guess a password,
controls to delay reactivation of these locked accounts stops the malicious
individual from continually guessing the password (they will have to stop for a
minimum of 30 minutes until the account is reactivated). Additionally, if reactivation
must be requested, the admin or help desk can validate that the account owner is
the cause (from typing errors) of the lockout.

#### 8.5.15  If a session has been idle for more than 15 minutes, require the user to re-authenticate to re-activate the terminal or session ####

When users walk away from an open machine with access to critical network or
cardholder data, that machine may be used by others in the user’s absence,
resulting in unauthorized account access and/or account misuse.

#### 8.5.16  Authenticate database access ####

Authenticate all access to any database containing cardholder data. This includes access by applications, administrators, and all other users.

Restrict user direct access or queries to databases to database administrators.

Without user authentication for access to databases and applications, the potential
for unauthorized or malicious access increases, and such access cannot be logged
since the user has not been authenticated and is therefore not known to the
system. Also, database access should be granted through programmatic methods
only (for example, through stored procedures), rather than via direct access to the
database by end users (except for DBAs, who can have direct access to the
database for their administrative duties).