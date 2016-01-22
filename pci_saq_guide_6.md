# SAQ 6: Secure systems #

Source: "Navigating DSS v2.0" from https://www.pcisecuritystandards.org

Requirement 6: Develop and maintain secure systems and applications

Unscrupulous individuals use security vulnerabilities to gain privileged access to systems. Many of these vulnerabilities are fixed by vendor-
provided security patches, which must be installed by the entities that manage the systems. All critical systems must have the most recently
released, appropriate software patches to protect against exploitation and compromise of cardholder data by malicious individuals and malicious
software.

Note: Appropriate software patches are those patches that have been evaluated and tested sufficiently to determine that the patches do not
conflict with existing security configurations. For in-house developed applications, numerous vulnerabilities can be avoided by using standard
system development processes and secure coding techniques.

## 6.1 Install critical security patches ##

Ensure that all system components and software are
protected from known vulnerabilities by having the latest vendor-supplied security patches installed.
Install critical security patches
within one month of release.

<i>
Note:An organization may consider applying a risk-based<br>
approach to prioritize their patch installations. For example, by<br>
prioritizing critical infrastructure (for example, public-facing<br>
devices and systems, databases) higher than less-critical internal<br>
devices, to ensure high-priority systems and devices are<br>
addressed within one month, and addressing less critical devices<br>
and systems within three months.<br>
<br>
There are a considerable amount of attacks using widely published exploits, often<br>
"0 day" (published within the hour) against otherwise secured systems. Without<br>
implementing the most recent patches on critical systems as soon as possible, a<br>
malicious individual can use these exploits to attack and disable the network.<br>
Consider prioritizing changes such that critical security patches on critical or at-risk<br>
systems can be installed within 30 days, and other less-risky changes are installed<br>
within 2-3 months.<br>
</i>

## 6.2 Identify and ranking newly discovered security vulnerabilities ##

Establish a process to identify and assign a risk ranking to newly discovered security vulnerabilities.

<i>
Notes:<br>
Risk rankings should be based on industry best practices. For<br>
example, criteria for ranking “High” risk vulnerabilities may include<br>
a CVSS base score of 4.0 or above, and/or a vendor-supplied<br>
patch classified by the vendor as “critical,” and/or a vulnerability<br>
affecting a critical system component.<br>
<br>
The ranking of vulnerabilities as defined in 6.2.a is considered a<br>
best practice until June 30, 2012, after which it becomes a<br>
requirement.<br>
<br>
The intention of this requirement is that organizations keep up-to-date with new<br>
vulnerabilities that may impact their environment.<br>
<br>
While it is important to monitor vendor announcements for news of vulnerabilities<br>
and patches related to their products, it is equally important to monitor common<br>
industry vulnerability news groups and mailing lists for vulnerabilities and potential<br>
workarounds that may not yet be known or resolved by the vendor.<br>
<br>
Once an organization identifies a vulnerability that could affect their environment,<br>
the risk that vulnerability poses must be evaluated and ranked. This implies that<br>
the organization has some method in place to evaluate vulnerabilities and assign<br>
risk rankings on a consistent basis. While each organization will likely have<br>
different methods for evaluating a vulnerability and assigning a risk rating based on<br>
their unique CDE, it is possible to build upon common industry accepted risk<br>
ranking systems, for example CVSS. 2.0, NIST SP 800-30, etc.<br>
<br>
Classifying the risks (for example, as “high”, “medium”, or “low”) allows<br>
organizations to identify and address high priority risk items more quickly, and<br>
reduce the likelihood that vulnerabilities posing the greatest risk will be exploited.<br>
</i>

## 6.3 Develop software applications in accordance with PCI DSS and best practices ##

<b>Develop software applications in accordance with PCI DSS and based on industry best practices.</b>

Develop software applications (internal and external, and
including web-based administrative access to applications) in
accordance with PCI DSS (for example, secure authentication
and logging), and based on industry best practices, and
incorporate information security throughout the software
development life cycle. These processes must include the
following.

<i>
Without the inclusion of security during the requirements definition, design,<br>
analysis, and testing phases of software development, security vulnerabilities can<br>
be inadvertently or maliciously introduced into the production environment.<br>
</i>

<b>6.3.1 Removal of custom application accounts, user IDs, and passwords before applications become active or are released to customers</b>

Custom application accounts, user IDs, and passwords should be removed from
production code before the application becomes active or is released to customers,
since these items may give away information about the functioning of the
application. Possession of such information could facilitate compromise of the
application and related cardholder data.

<b>6.3.2 Review of custom code prior to release to production or customers in order to identify any potential coding vulnerability.</b>

<i>Note: This requirement for code reviews applies to all custom<br>
code (both internal and public-facing), as part of the system<br>
development life cycle. Code reviews can be conducted by<br>
knowledgeable internal personnel or third parties. Web<br>
applications are also subject to additional controls, if they are<br>
public facing, to address ongoing threats and vulnerabilities<br>
after implementation, as defined at PCI DSS Requirement 6.6.<br>
<br>
Security vulnerabilities in custom code are commonly exploited by malicious<br>
individuals to gain access to a network and compromise cardholder data.<br>
<br>
Code reviews may be performed manually, or with the assistance of automated<br>
review tools. Automated review tools have functionality that reviews code for<br>
common coding mistakes and vulnerabilities. While automated review is useful, it<br>
should not generally be relied upon as the sole means of code review. An<br>
individual knowledgeable and experienced in code review should be involved in the<br>
review process in order to identify code issues that are difficult or even impossible<br>
for an automated tool to identify. Assigning code reviews to someone other than<br>
the developer of the code allows an independent, objective review to be<br>
performed.<br>
</i>

## 6.4 Change control ##

<b>Follow change control processes and procedures</b>

Follow change control processes and procedures for all
changes to system components. The processes must include the
following.

<i>
Without proper change controls, security features could be inadvertently or<br>
deliberately omitted or rendered inoperable, processing irregularities could occur,<br>
or malicious code could be introduced.<br>
</i>

<b>6.4.1 Separate development/test and production environments</b>

Due to the constantly changing state of development and test environments, they
tend to be less secure than the production environment. Without adequate
separation between environments it may be possible for the production
environment, and cardholder data, to be compromised due to vulnerabilities in a
test or development environment.

<b>6.4.2 Separation of duties between development/test and production environments</b>

Reducing the number of personnel with access to the production environment and
cardholder data minimizes risk and helps ensure that access is limited to those
individuals with a business need to know.

The intent of this requirement is to ensure that development/test functions are
separated from production functions. For example, a developer may use an
administrator-level account with elevated privileges for use in the development
environment, and have a separate account with user-level access to the production
environment.

In environments where one individual performs multiple roles (for example
application development and implementing updates to production systems), duties
should be assigned such that no one individual has end-to-end control of a process
without an independent checkpoint. For example, assign responsibility for
development, authorization and monitoring to separate individuals.

<b>6.4.3 Production data (live PANs) are not used for testing or development</b>

Security controls are usually not as stringent in the development environment. Use
of production data provides malicious individuals with the opportunity to gain
unauthorized access to production data (cardholder data).
Payment card brands and many acquires are able to provide account numbers
suitable for testing in the event that you need realistic PANs to test system
functionality prior to release.

<b>6.4.4 Removal of test data and accounts before production systems become active</b>

Test data and accounts should be removed from production code before the
application becomes active, since these items may give away information about
the functioning of the application. Possession of such information could facilitate
compromise of the application and related cardholder data.

<b>6.4.5 Change control procedures for the implementation of security patches and software modifications. Procedures must include the following.</b>

Without proper change controls, security features could be inadvertently or
deliberately omitted or rendered inoperable, processing irregularities could occur,
or malicious code could be introduced. Likewise, a change may negatively affect
security functionality of a system necessitating the change to be backed out.

<b>6.4.5.1 Documentation of impact.</b>

The impact of the change should be documented so that all affected parties will be
able to plan appropriately for any processing changes.

<b>6.4.5.2 Documented change approval by authorized parties.</b>

Approval by authorized parties indicates that the change is a legitimate and
approved change sanctioned by the organization.

<b>6.4.5.3 Functionality testing to verify that the change does not adversely impact the security of the system.</b>

Thorough testing should be performed to verify that the security of the environment
is not reduced by implementing a change. Testing should validate that all existing
security controls remain in place, are replaced with equally strong controls, or are
strengthened after any change to the environment.
For custom code changes, testing includes verifying that no coding vulnerabilities
have been introduced by the change.

<b>6.4.5.4 Back-out procedures.</b>

For each change, there should be back-out procedures in case the change fails, to
allow for restoring back to the previous state.

## 6.5 Develop applications based on secure coding guidelines ##

Prevent common coding vulnerabilities in software development
processes, to include the following.

Note: The vulnerabilities listed at 6.5.1 through 6.5.9 were current
with industry best practices when this version of PCI DSS was
published. However, as industry best practices for vulnerability
management are updated (for example, the OWASP Guide,
SANS CWE Top 25, CERT Secure Coding, etc.), the current best
practices must be used for these requirements.

<i>
The application layer is high-risk and may be targeted by both internal and external<br>
threats. Without proper security, cardholder data and other confidential company<br>
information can be exposed, resulting in harm to a company, its customers, and its<br>
reputation.<br>
<br>
As with all PCI DSS requirements, Requirements 6.5.1 through 6.5.5 and 6.5.7<br>
through 6.5.9 are the minimum controls that should be in place. This list is<br>
composed of the most common, accepted secure coding practices at the time that<br>
this version of the PCI DSS was published. As industry accepted secure coding<br>
practices change, organizational coding practices should likewise be updated to<br>
match.<br>
<br>
The examples of secure coding resources provided (SANS, CERT, and OWASP)<br>
are suggested sources of reference and have been included for guidance only. An<br>
organization should incorporate the relevant secure coding practices as applicable<br>
to the particular technology in their environment.<br>
</i>

<b>6.5.1 Injection flaws, particularly SQL injection.</b>

Also consider OS Command Injection, LDAP and XPath injection flaws as well
as other injection flaws.

<i>
Validate input to verify user data cannot modify meaning of commands and<br>
queries. Injection flaws, particularly SQL injection, are a commonly used method<br>
for compromising applications. Injection occurs when user-supplied data is sent to<br>
an interpreter as part of a command or query. The attacker's hostile data tricks the<br>
interpreter into executing unintended commands or changing data, and allows the<br>
attacker to attack components inside the network through the application, to initiate<br>
attacks such as buffer overflows, or to reveal both confidential information and<br>
server application functionality. This is also a popular way to conduct fraudulent<br>
transactions on commerce-enabled web sites. Information from requests should be<br>
validated before being sent to the application – for example, by checking for all<br>
alpha characters, mix of alpha and numeric characters, etc.<br>
</i>

<b>6.5.2 Buffer overflow</b>

<i>
Ensure that applications are not vulnerable to buffer overflow attacks. Buffer<br>
overflows happen when an application does not have appropriate bounds checking<br>
on its buffer space. To exploit a buffer overflow vulnerability, an attacker would<br>
send an application a larger amount of information than one of its particular buffers<br>
is able to handle. This can cause the information in the buffer to be pushed out of<br>
the buffer’s memory space and into executable memory space. When this occurs,<br>
the attacker has the ability to insert malicious code at the end of the buffer and<br>
then push that malicious code into executable memory space by overflowing the<br>
buffer. The malicious code is then executed and often enables the attacker remote<br>
access to the application and/or infected system.<br>
</i>

<b>6.5.3 Insecure cryptographic storage</b>

<i>
Prevent cryptographic flaws. Applications that do not utilize strong cryptographic<br>
functions properly to store data are at increased risk of being compromised and<br>
exposing cardholder data. If an attacker is able to exploit weak cryptographic<br>
processes, they may be able to gain clear-text access to encrypted data.<br>
</i>

<b>6.5.4 Insecure communications</b>

<i>
Properly encrypt all authenticated and sensitive communications. Applications that<br>
fail to adequately encrypt network traffic using strong cryptography are at<br>
increased risk of being compromised and exposing cardholder data. If an attacker<br>
is able to exploit weak cryptographic processes, they may be able to gain control of<br>
an application or even gain clear-text access to encrypted data.<br>
</i>

<b>6.5.5 Improper error handling</b>

<i>
Do not leak information via error messages or other means. Applications can<br>
unintentionally leak information about their configuration, internal workings, or<br>
violate privacy through a variety of application problems. Attackers use this<br>
weakness to steal sensitive data, or conduct more serious attacks. Also, incorrect<br>
error handling provides information that helps a malicious individual compromise<br>
the system. If a malicious individual can create errors that the application does not<br>
handle properly, they can gain detailed system information, create denial-of-<br>
service interruptions, cause security to fail, or crash the server. For example, the<br>
message "incorrect password provided" tells them the user ID provided was<br>
accurate and that they should focus their efforts only on the password. Use more<br>
generic error messages, like "data could not be verified."<br>
</i>

<b>6.5.6 “High” vulnerabilities</b>

All “High” vulnerabilities identified in the vulnerability
identification process (as defined in PCI DSS Requirement 6.2).

Note: This requirement is considered a best practice until June
30, 2012, after which it becomes a requirement.
For web applications and application interfaces (internal or
external), the following additional requirements apply:

<i>
Any high vulnerabilities noted per Requirement 6.2 that could affect the application<br>
should be accounted for during the development phase. For example, a<br>
vulnerability identified in a shared library or in the underlying operating system<br>
should be evaluated and addressed prior to the application being released to<br>
production.<br>
</i>

For web applications and application interfaces (internal or
external), the following additional requirements apply:

<i>
Web applications, both internally and externally (public) facing, have unique<br>
security risks based upon their architecture as well as their relative ease and<br>
occurrence of compromise.<br>
</i>

<b>6.5.7 Cross-site scripting (XSS)</b>

<i>
All parameters should be validated before inclusion. XSS flaws occur whenever an<br>
application takes user supplied data and sends it to a web browser without first<br>
validating or encoding that content. XSS allows attackers to execute script in the<br>
victim's browser which can hijack user sessions, deface web sites, possibly<br>
introduce worms, etc.<br>
</i>

<b>6.5.8 Improper access control</b>

Improper access control (such as insecure direct object
references, failure to restrict URL access, and directory
traversal)

<i>
Do not expose internal object references to users. A direct object reference occurs<br>
when a developer exposes a reference to an internal implementation object, such<br>
as a file, directory, database record, or key, as a URL or form parameter. Attackers<br>
can manipulate those references to access other objects without authorization.<br>
<br>
Consistently enforce access control in presentation layer and business logic for all<br>
URLs. Frequently, the only way an application protects sensitive functionality is by<br>
preventing the display of links or URLs to unauthorized users. Attackers can use<br>
this weakness to access and perform unauthorized operations by accessing those<br>
URLs directly.<br>
<br>
Protect against directory traversal. An attacker may be able to enumerate and<br>
navigate the directory structure of a website thus gaining access to unauthorized<br>
information as well as gaining further insight into the workings of the site for later<br>
exploitation.<br>
</i>

<b>6.5.9 Cross-site request forgery (CSRF)</b>

<i>
Do not reply on authorization credentials and tokens automatically submitted by<br>
browsers. A CSRF attack forces a logged-on victim's browser to send a pre-<br>
authenticated request to a vulnerable web application, which then forces the<br>
victim's browser to perform a hostile action to the benefit of the attacker. CSRF can<br>
be as powerful as the web application that it attacks.<br>
</i>

## 6.6 Address new threats and vulnerabilities on an ongoing basis ##

For public-facing web applications, address new threats and
vulnerabilities on an ongoing basis and ensure these applications
are protected against known attacks by either of the following
methods:
  * Reviewing public-facing web applications via manual or automated application vulnerability security assessment tools or methods, at least annually and after any changes
  * Installing a web-application firewall in front of public-facing web applications

<i>
Attacks on web-facing applications are common and often successful, and are<br>
allowed by poor coding practices. This requirement for reviewing applications or<br>
installing web-application firewalls is intended to greatly reduce the number of<br>
compromises on public-facing web applications that result in breaches of<br>
cardholder data.<br>
<br>
Manual or automated vulnerability security assessment tools or methods that<br>
review and/or scan for application vulnerabilities can be used to satisfy this<br>
requirement<br>
<br>
Web-application firewalls filter and block non-essential traffic at the application<br>
layer. Used in conjunction with a network-based firewall, a properly configured<br>
web-application firewall prevents application-layer attacks if applications are<br>
improperly coded or configured.<br>
</i>
