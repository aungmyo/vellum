# SAQ 7: Data access #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

<b>
Guidance for Requirements 7, 8, and 9: Implement Strong Access Control Measures<br>
<br>
Requirement 7: Restrict access to cardholder data by business need to know<br>
</b>

<i>
To ensure critical data can only be accessed by authorized personnel, systems and processes must be in place to limit access based on need to<br>
know and according to job responsibilities. “Need to know” is when access rights are granted to only the least amount of data and privileges<br>
needed to perform a job.<br>
</i>

## 7.1 Limit access to system components and cardholder data ##

Limit access to system components and cardholder data to only those individuals whose job requires such access. Access limitations must include the following.

<i>
The more people who have access to cardholder data, the more risk there is that a<br>
user’s account will be used maliciously. Limiting access to those with a strong<br>
business reason for the access helps your organization prevent mishandling of<br>
cardholder data through inexperience or malice. When access rights are granted<br>
only to the least amount of data and privileges needed to perform a job, this is a<br>
called “least privilege” and “need to know,” and when privileges are assigned to<br>
individuals based on job classification and function, this is called “role-based<br>
access control” or RBAC. Role based access control enforcement is not limited to<br>
an application layer or any specific authorization solution. For example, technology<br>
including but not limited to directory services such as Active Directory or LDAP,<br>
Access Control Lists (ACLs), and TACACS are viable solutions as long as they are<br>
appropriately configured to enforce the principles of least privilege and need to<br>
know.<br>
<br>
Organizations should create a clear policy and processes for data access control<br>
based on need to know and using role-based access control, to define how and to<br>
whom access is granted, including appropriate management authorization<br>
processes.<br>
</i>

7.1.1 Restriction of access rights to privileged user IDs to least privileges necessary to perform job responsibilities

7.1.2 Assignment of privileges is based on individual personnel’s job classification and function

7.1.3 Requirement for a documented approval by authorized parties specifying required privileges.

7.1.4 Implementation of an automated access control system

## 7.2 Establish an access control system ##

Establish an access control system for systems components
with multiple users that restricts access based on a user’s need to
know, and is set to “deny all” unless specifically allowed.

This access control system must include the following:

7.2.1 Coverage of all system components

7.2.2 Assignment of privileges to individuals based on job classification and function

7.2.3 Default “deny-all” setting

Note: Some access control systems are set by default to
“allow-all,” thereby permitting access unless/until a rule is
written to specifically deny it.

<i>
Without a mechanism to restrict access based on user’s need to know, a user may<br>
unknowingly be granted access to cardholder data. Use of an automated access<br>
control system or mechanism is essential to manage multiple users. This system<br>
should be established in accordance with your organization’s access control policy<br>
and processes (including “need to know” and “role-based access control”), should<br>
manage access to all system components, and should have a default “deny-all”<br>
setting to ensure no one is granted access until and unless a rule is established<br>
specifically granting such access.<br>
</i>

