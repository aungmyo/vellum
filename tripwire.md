# tripwire #

##### Documentation #####

http://en.wikipedia.org/wiki/Open_Source_Tripwire

http://www.thegeekstuff.com/2008/12/tripwire-tutorial-linux-host-based-intrusion-detection-system

##### Preparation #####

To build packages from source eg. tripwire, we require make and GNU C/C++ compiler.

<pre>
yum install gcc gcc-c++ autoconf automake<br>
</pre>

Alternatively

<pre>
yum groupinstall "Development Tools"<br>
</pre>

##### Download #####

Download tripwire-2.4.2-src.tar.gz from sourceforge (http://sourceforge.net/projects/tripwire).

<pre>
[root@vbox1 ~]$ tar xzvf tripwire-2.4.2-src.tar.gz<br>
[root@vbox1 ~]$ cd tripwire-2.4.2-src<br>
[root@vbox1 tripwire-2.4.2-src]$ ./configure --prefix /opt/tripwire<br>
[root@vbox1 tripwire-2.4.2-src]$ make install<br>
</pre>

##### Initialise database #####

<pre>
[root@vbox1 tripwire-2.4.2-src]$ /opt/tripwire/sbin/tripwire --init -c /opt/tripwire/etc/tw.cfg<br>
Please enter your local passphrase:<br>
Parsing policy file: /opt/tripwire/etc/tw.pol<br>
...<br>
Wrote database file: /opt/tripwire/lib/tripwire/vbox1.twd<br>
The database was successfully generated.<br>
</pre>

##### Schedule #####

<pre>
[root@vbox1 tripwire-2.4.2-src]$ crontab -e<br>
03 2 * * * /opt/tripwire/sbin/tripwire --check -c /opt/tripwire/etc/tw.cfg | /usr/bin/mail root -s "Tripwire Check" 2>&1<br>
</pre>

Run the check to see what's changed since the --init command.

<pre>
[root@vbox1 tripwire-2.4.2-src]$ /opt/tripwire/sbin/tripwire --check -c /opt/tripwire/etc/tw.cfg<br>
Parsing policy file: /opt/tripwire/etc/tw.pol<br>
...<br>
Modified:<br>
...<br>
</pre>

##### Customise #####

Update policy eg. commenting out default directories that we don't have eg. /cdrom.

<pre>
[root@vbox1 ~]$ vi /opt/tripwire/etc/twpol.txt<br>
[root@vbox1 ~]$ /opt/tripwire/sbin/tripwire --update-policy --secure-mode low -c /opt/tripwire/etc/tw.cfg /opt/tripwire/etc/twpol.txt<br>
</pre>