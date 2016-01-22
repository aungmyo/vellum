Below a simple svnserve setup with following steps

**create an svn user** create repository as /home/svn/svn using svnadmin
**edit config file svn/conf/svnserve.conf** edit password file svn/conf/passwd
**ensure that svn restarted via the cron on @reboot**

```
# adduser svn
# su - svn
$ svnadmin create /home/svn/svn # create a repo "svn" in svn home dir
$ ls svn/conf/
authz
passwd
svnserve.conf
```

Configure users and their SVN passwords via svnserve.conf.

```
$ vi svn/conf/svnserve.conf

[general]
anon-access = read
auth-access = write
password-db = passwd

svn@evanx:~$ vi svn/conf/passwd
[users]
evanx = evanxpassword
```

Finally we ensure that svnserve is restarted on reboot.
```
$ crontab -e
@reboot svnserve -d --listen-host myhost -r /home/svn/svn
```
where the --listen-host option is on the external IP interface rather than localhost.