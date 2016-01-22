### Installation ###

yum install aide
md5sum /usr/sbin/aide # save somewhere eg. a google doc
aide --init
cp /var/lib/aide/aide.db.new.gz /var/lib/aide/aide.db.gz
md5sum /var/lib/aide/aide.db.gz # save somewhere eg. a google doc

**harden /etc/ssh/sshd\_config (change port etc),** check iptables (ssh only), and
**config the eth0 etc and "connect to the network"**

Hardening /etc/ssh/sshd\_config
**change port** AllowUser yourself only
**Deny root login via "PermitRootLogin no" or "forced-commands-only"** install your key so "PasswordAuthentication no"

### ssh root forced command ###

```
root@srv1 ~: vi /etc/ssh/sshd_config

AllowUsers root evanx
PermitRootLogin forced-commands-only
```

```
root@srv1 ~: cat .ssh/authorized_keys 
command="~/scripts/ssh_command.sh" ssh-rsa AAAA... evanx@evanx
```

```
root@srv1 ~: cat scripts/ssh_command.sh 

if echo $SSH_ORIGINAL_COMMAND | grep -q ^md5sum 
then
  exec $SSH_ORIGINAL_COMMAND
fi
```

```
root@srv1 ~: chmod 700 .ssh
root@srv1 ~: chmod 600 .ssh/authorized_keys 
```

```
root@srv1 ~: chmod 700 scripts/ssh_command.sh
```

```
for host in srv1 srv2
do
  echo `date +%Y-%m-%d` $host 
  ssh root@$host md5sum /usr/sbin/aide /var/lib/aide/aide.db.gz
  echo
done
```



### Check repo keys ###

```
root@srv1 ~: rpm -q --queryformat "%{SUMMARY}\n" gpg-pubkey
gpg(CentOS-5 Key (CentOS 5 Official Signing Key) <centos-5-key@centos.org>)
gpg(PostgreSQL RPM Building Project <pgsqlrpms-hackers@pgfoundry.org>)
gpg(Dag Wieers (Dag Apt Repository v1.0) <dag@wieers.com>)

root@srv1 ~:  gpg --quiet --with-fingerprint /etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-5 
gpg: new configuration file `/root/.gnupg/gpg.conf' created
gpg: WARNING: options in `/root/.gnupg/gpg.conf' are not yet active during this run
pub  1024D/E8562897 2007-01-06 CentOS-5 Key (CentOS 5 Official Signing Key) <centos-5-key@centos.org>
      Key fingerprint = 473D 66D5 2122 71FD 51CC  17B1 A8A4 47DC E856 2897
sub  1024g/1E9EA3B6 2007-01-06 [expires: 2017-01-03]
```

```
wget http://mirror.centos.org/centos/RPM-GPG-KEY-CentOS-5

cat /etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-5
```

```
root@srv1 ~: cat /etc/yum.repos.d/CentOS-* | grep ^gpgkey | sort | uniq -c
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-5
```

```
root@srv1 ~: cat /etc/yum.repos.d/* | grep ^gpgcheck | sort | uniq -c
```

```
for repo in  /etc/yum.repos.d/* 
do 
  grep -q "^gpgcheck=0" $repo
  if [ $? -eq 0 ] 
  then 
    echo $repo 
  fi
done
```


```
# vi /etc/yum.conf
gpgcheck=1
```

### Remove packages ###

```
yum remove anacron autofs avahi* bluetooth cups* firstboot xinetd
```

### Remove groups of packages ###

```
yum groupremove "Sound and Video"
yum groupremove "GNOME Desktop Environment"
yum groupremove "KDE (K Desktop Environment)"
yum groupremove "GNOME Software Development"
yum groupremove "X Window System"
yum groupremove "X Software Development"
yum groupremove "Development Libraries"
yum groupremove "Development Tools"
```

### yum verify ###

```
yum verify | grep "^[a-z]"
```

```
yum -y reinstall rpm rpm-build rpm-libs yum* 
yum -y reinstall coreutils diffutils
yum -y reinstall openssh* passwd util-linux
yum -y reinstall findutils logrotate mktemp mlocate prelink tmpwatch rsync vixie-cron
yum -y reinstall webalizer
```

<b>rebuild and verify rpmdb</b>

```
cp -ra /var/lib/rpm /root/rpmdb.`date +'%Y-%m-%d'`
rpm --rebuilddb
rpm -Va | grep "bin/\|/lib/"
```

```
md5sum /usr/sbin/aide
yum -y update aide 
md5sum /usr/sbin/aide
yum -y reinstall aide 
md5sum /usr/sbin/aide
```

yum verify | grep "^[a-z]"
yum -y reinstall aide coreutils diffutils rpm rpm-libs yum**yum verify | grep "^[a-z]"**

yum -y reinstall aide coreutils diffutils rpm rpm-libs yum**yum -y reinstall openss** openldap passwd pam pam\_passwdqc usermode util-linux
yum -y reinstall findutils logrotate mktemp mlocate prelink tmpwatch rsync sudo vixie-cron
yum -y reinstall bc curl cvs cyrus-sasl info fipscheck grep gnupg ftp less lvm2 ncurses pcre procps psmisc popt readline sqlite tcp\_wrappers vim-enhanced wget
yum -y reinstall libidn libgcrypt libuser libutempter libxslt libgpg-error

yum reinstall rpm rpm-libs yum**aide coreutils diffutils openss** openldap passwd pam pam\_passwdqc usermode util-linux findutils logrotate mktemp mlocate prelink tmpwatch rsync sudo vixie-cron bc curl cvs cyrus-sasl info fipscheck grep gnupg ftp less lvm2 ncurses pcre procps psmisc popt readline sqlite tcp\_wrappers vim-enhanced wget  libidn libgcrypt libuser libutempter libxslt libgpg-error

rpm -Va | grep "bin/\|/lib/"
yum verify | grep "^[a-z]"

yum -y reinstall rpm rpm-libs yum

## cron ##

```
for bin in mktemp tmpwatch prelink updatedb makewhatis logger logrotate
do
  rpm -qf `which $bin`
  md5sum `which $bin`
done
```


### Disable services ###

```
for service in anacron apdm autofs avahi-dnsconfd avahi-daemon bluetooth conman cups firstboot gpm \
  hidd hplip kdump kudzu isdn haldaemon messagebus mcstrans mdmonitor microcode_ctl \
  netfs nfslock portmap pcscd readahead_early readahead_later rpcidmapd rpcgssd \
  sysstat xinetd yum-updatesd
do
  echo $service
  service $service stop 
  chkconfig $service off
done

chkconfig --list | "grep :on"

rpm -qf /etc/init.d/anacron
rpm -qi anacron
```

### Diable uncommon filesystem types ###

```
root@srv1 ~: cat /etc/fstab

vi /etc/modprobe.conf

install cramfs /bin/true
install freevxfs /bin/true
install jffs2 /bin/true
install hfs /bin/true
install hfsplus /bin/true
install squashfs /bin/true
install udf /bin/true
```


### GNOME config ###

Execute the following commands to prevent gnome-volume-manager from automatically mounting devices
and media:

```
gconftool-2 --direct \
  --config-source xml:readwrite:/etc/gconf/gconf.xml.mandatory \
  --type bool \
  --set /desktop/gnome/volume_manager/automount_media false

gconftool-2 --direct \
  --config-source xml:readwrite:/etc/gconf/gconf.xml.mandatory \
  --type bool \
  --set /desktop/gnome/volume_manager/automount_drives false
```


Execute the following command to prevent the thumbnailers from automatically creating thumbnails for new
or modified folder contents:

```
# gconftool-2 --direct \
  --config-source xml:readwrite:/etc/gconf/gconf.xml.mandatory \
  --type bool \
  --set /desktop/gnome/thumbnailers/disable_all true
```

Verify Permissions on passwd, shadow, group and gshadow Files

```
cd /etc
ls -l passwd shadow group gshadow
chown root:root passwd shadow group gshadow
chmod 644 passwd group
chmod 400 shadow gshadow
```


### Sticky bit ###

Locate any directories in local partitions which are world-writable and do not have their sticky bits set.
The following command will discover and print these.
Run it once for each local partition:
```
df -h
find / -xdev -type d \( -perm -0002 -a ! -perm -1000 \) -print
```

If this command produces any output, fix each reported directory directory using the command:
```
chmod +t /dir
```


### World writables ###

The following command discovers and prints any world-writable files in local partitions.

Run it once for each local partition:
```
find / -xdev -type f -perm -0002 -print
```

If this command produces any output, fix each reported file file using the command:
```
chmod o-w file
```


### SUID/GUID ###

```
find / -xdev \( -perm -4000 -o -perm -2000 \) -type f -print
```

```
for bin in ccreds_validate ksu netreport lockfile mount.nfs mount.nfs4 rpc rlogin rsh ssh-keysign usernetctl umount.nfs umount.nfs4 userisdnctl usernetctl wall write 
do 
  which $bin && chmod -s `which $bin`  
done
```