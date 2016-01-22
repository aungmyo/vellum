while we need cron/scriptable ssh between machines, for nagios checks, rsync backups etc, i've been reading up on "scponly" and "rshell" - for "restricted shell"

but they didn't sound those were the answer eg. not in base repo's

was checking again today and came across ssh forced commands - woohoo !

http://binblog.info/2008/10/20/openssh-going-flexible-with-forced-commands/

eg. say we want someone (or nagios) to be able to invoke manco\_status script on bizserver but doesn't need full access

in this case i have configured "nagios" on bizserver with the following .ssh/authorized\_keys - with the forced command before the key and other restrictions like no port forwarding allowed etc

```
root@bizserver bizserver: cat ~nagios/.ssh/authorized_keys
command="/scripts/manco_status",no-port-forwarding,no-X11-forwarding ssh-rsa AAAAB3Nza .... FjIH5ALw== evanx@bizserver.net
```

now when i ssh (or scp), this is the only command - i don't get a shell :)

```
[evanx@bizserver.net ~]$ ssh nagios@bizserver
DiskSpace OK - 84%
```

why is this a big deal ?

its a standard mechanism in ssh to allow remote script invocation tightly controlled via ssh keys :)

so its a replacement for nagios-nrpe and more tightly controlled via ssh keys innit - whereas nagios NRPE by default anyone can check commands,
althought of course those are restricted to nagios checks in nrpe.cfg

also this "forced command" and be useed to allow rsync and scp without shell, eg. for backups

for example i create this forced command which is a wrapper script - for ssh users or groups eg. scponly

its a rsync/scp wrapper - plus can allow commands like ls and md5sum - but no other commands via ssh

but allows rsync of /backups - will reject other rsync commands we don't like the look of :)

and it customised to allow some restricted commands and/or otherwise rsync

```
root@bizserver bizserver: cat  /scripts/rsync_command.sh
#!/bin/bash

echo $SSH_ORIGINAL_COMMAND | grep -q "^rsync"
if [ $? -eq 0 ]
then
  echo $SSH_ORIGINAL_COMMAND | grep -q "^rsync --server .* /backups"
  if [ $? -eq 0 ]
  then
    exec $SSH_ORIGINAL_COMMAND
    exit $?
  else
    exit 1
  fi
else
  command=`echo "$SSH_ORIGINAL_COMMAND" | cut -f1 -d' '`
  echo $command | grep -q "ls\|md5sum"
  if [ $? -eq 0 ]
  then
    exec $SSH_ORIGINAL_COMMAND
  else
    echo "Permissed denied"
    exit 2
  fi
fi
```

```
[evanx@bizserver.net ~]$ rsync nagios@bizserver:/backups/test .

[evanx@bizserver.net ~]$ rsync nagios@bizserver:/etc/passwd .
rsync: connection unexpectedly closed (0 bytes received so far) [receiver]
rsync error: error in rsync protocol data stream (code 12) at io.c(600) [receiver=3.0.6]

[evanx@bizserver.net ~]$ ssh nagios@bizserver md5sum /etc/passwd
2d2889941ec91bce7cd8c741858992cb  /etc/passwd
```

