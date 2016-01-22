# Heading #

Using ssh localhost is arguably better than su or sudo for that matter, for the same reason that
ssh keys is better than passwords i.e. because passwords are susceptible to
bruce force attack.

Also with agent forwarding its more convenient than entering a password.

<tt>AllowUsers</tt>
<pre>
[root@server etc]# cat /etc/ssh/sshd_config | grep AllowUsers<br>
</pre>

<tt>ForwardAgent</tt>
<pre>
[evanx@server ~]$ cat .ssh/config<br>
ForwardAgent yes<br>
</pre>

<tt>authorized_keys</tt>
<pre>
[other@server ~]$ cat .ssh/authorized_keys<br>
from="localhost" ssh-rsa<br>
AAAAB3NzaC1yc2EAAAADAQABAAABAQDAaIojgNHnHBGnuDdpo9s...<br>
evanx@evanx<br>
</pre>

<tt>ssh localhost</tt>
<pre>
[evanx@server ~]$ ssh other@localhost<br>
Last login: Mon Apr 30 13:03:10 2012 from localhost<br>
[other@server ~]$<br>
</pre>

<tt>/var/log/secure</tt>
<pre>
Apr 30 13:05:47 server sshd[30086]: debug1: matching key found: file<br>
/home/other/.ssh/authorized_keys, line 2<br>
Apr 30 13:05:47 server sshd[30086]: Found matching RSA key:<br>
70:bc:ce:96:c7:7f:9d:bb:1e:c1:a0:45:ce:c3:9b:08<br>
</pre>

Since the key of course is tied to a person, so we have auditing.
<pre>
[other@server ~]$ ssh-keygen -lf .ssh/authorized_keys<br>
2048 70:bc:ce:96:c7:7f:9d:bb:1e:c1:a0:45:ce:c3:9b:08  evanx@evanx (RSA)<br>
</pre>
