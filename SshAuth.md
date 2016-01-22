# ssh #

http://linux.byexamples.com/archives/297/how-to-ssh-without-password/

If you google ssh-keygen, you should find lots of examples sometimes entitled "ssh without password" for installing a public key on server eg. server for ssh.

### ssh-keygen ###

In short, we generate a key pair using ssh-keygen (or PuTTYgen on Windows).

```
$ ssh-keygen -t rsa
```

This creates two files as follows ~/.ssh/id\_rsa.pub (public key), and ~/.ssh/id\_rsa (private key).

The first is the public key, which we give out to servers e.g. using scp, or ssh-copy-id.

The private key is install on our workstations.

Since the private key gives access to the remote server to whoever has this key, it is important to protect it with a passphrase. This passphrase is used to encrypt/decrypt the file in which the private key is stored.

### ssh-agent ###

To avoid the inconvenience of having to enter the passphrase every time ssh requires access to your private key (for a new remote ssh session), this passphrase can be cached by ssh-agent. We use ssh-add to activate ssh-agent for your session, and add your decrypted private key info to the agent to use for authentication.

```
[evanx@mozart ~]$ ps x | grep ssh-agent
26995 ?        Ss     0:00 ssh-agent /bin/bash

[evanx@mozart ~]$ ssh-add
Identity added: /home/evanx/.ssh/identity (/home/evanx/.ssh/identity)
```

ssh-agent is usually started for your shell by default but is inactive until ssh-add is invoked, and this will prompt for the passphrase for the encrypted private key to be cached by ssh-agent.
If ssh-agent is not running, it can be started as follows.

```
evanx@mozart ~$ ssh-agent bash
evanx@mozart ~$ ssh-add 
```

### ssh-copy-id ###

We append the public key to ~/.ssh/authorized\_keys on servers e.g. using the ssh-copy-id command.

The private key you can copy into the .ssh/ directory on your home Linux PC, or install into putty, your phone etc.

Alternatively you can generate a new public/private key pair on each workstation PC (or smartphone) and repeat the ssh-copy-id procedure.

If the ssh-copy-id command is not available, you can scp the public the key to the server, and manually append it to .ssh/authorized\_keys as follows

```
$ cat id_rsa.pub >> .ssh/authorized_keys
```

If you are creating authorized\_keys you need to chmod is as follows.

```
$ chmod 600 .ssh/authorized_keys
```

### Trouble-shooting ###

Also ensure that your .ssh directory permissions are correct.

```
$ chmod 700 .ssh
```

If you are having problems, run ssh with -vvv option to see debugging info.

```
$ ssh -vvv -p 2200 bizswitch.net 
```

If that doesn't give an indication of the problem, we need to look in sshd logs on the server.

On Ubuntu:

```
root@mozart:~# tail /var/log/auth.log
Mar 17 13:48:30 evanx sshd[27543]: Failed password for postgres from 192.168.15.4 port 56867 ssh2
```

On CentOS:

```
[root@biz etc]# cat /etc/ssh/sshd_config | grep DEBUG
LogLevel DEBUG
```

```
root@server ~: tail /var/log/secure
Mar 17 13:50:01 server sshd[2044]: Failed password for invalid user postgres from 192.168.16.191 port 52575 ssh2
```

Finally we can run sshd in debug mode using -d option.

```
root@server ~: /usr/sbin/sshd -p 2200 -d 
debug1: sshd version OpenSSH_4.3p2
```

Then when we ssh to that port and view the debugging information that it will output to the console.

```
evanx@mozart:~$ ssh -vvv -p 2200 server
```

### Port forwarding ###

```
ssh bizswitch.net -p2200 -L 2204:biz4:22 -L 2202:dev2:22
ssh localhost -p2202
```