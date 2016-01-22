# Mdstat #

### Reading ###

[http://wiki.centos.org/HowTos/SoftwareRAIDonCentOS5](http://wiki.centos.org/HowTos/SoftwareRAIDonCentOS5)

[http://tldp.org/HOWTO/Software-RAID-HOWTO-6.html](http://tldp.org/HOWTO/Software-RAID-HOWTO-6.html)

[https://raid.wiki.kernel.org/articles/m/d/s/Mdstat.html](https://raid.wiki.kernel.org/articles/m/d/s/Mdstat.html)

[http://radu.rendec.ines.ro/howto/raid1.html](http://radu.rendec.ines.ro/howto/raid1.html)

### Automation ###

Actually we monitor the status via `cron` or nagios using a script as follows.

```
root@server ~ # crontab -l 
MAILTO=evanx@gmail.com
4 4 * * * /opt/scripts/rdiff-backup.sh
9 9 * * * /opt/scripts/check_mdstat.sh
```

Where our monitoring script grep's `/proc/mdstat` psuedo file for underscores.

```
root@server ~ # cat /scripts/check_mdstat.sh 

if cat /proc/mdstat | grep -q "_"
then
  dmesg | grep "error\|sda\|sdb\|disk"
  mdstat /dev/md2 --detail
  cat /proc/mdstat 
  cat /proc/mdstat | mail -s "mdstat disk error on `hostname -s`" evanx@gmail.com
fi
```

### Inspect ###

Check the partition and md setup.

```
root@server ~ # cat /etc/fstab 
proc /proc proc defaults 0 0
none /dev/pts devpts gid=5,mode=620 0 0
/dev/md0 none swap sw 0 0
/dev/md1 /boot ext3 defaults 0 0
/dev/md2 / ext3 defaults 0 0

root@server ~ # df
Filesystem           1K-blocks      Used Available Use% Mounted on
/dev/md2             966683064   5829652 912133896   1% /
/dev/md1                256586     23076    220262  10% /boot
```

Check the status via `/proc/mdstat` psuedo file.

```
root@server ~ # cat /proc/mdstat 
Personalities : [raid1] [raid0] [raid6] [raid5] [raid4] [raid10] 
md0 : active raid1 sda1[0] sdb1[1]
      2102464 blocks [2/2] [UU]
      
md1 : active raid1 sda2[0] sdb2[1]
      264960 blocks [2/2] [UU]
      
md2 : active raid1 sdb3[1]
      974390336 blocks [2/1] [_U]
      
unused devices: <none>
```

Underscores in the following are faulty partitions e.g. `sda3`, which has been removed i.e. `sda3`.

Before hot removing a partition, we might see `"sda3[0](F)"` when detected as faulty because of I/O errors etc.

Manually check for I/O errors.

```
root@server ~ # dmesg | grep -i "disk\|error"
VFS: Disk quotas dquot_6.5.1
md: md driver 0.90.3 MAX_MD_DEVS=256, MD_SB_DISKS=27
sd 0:0:0:0: Attached scsi disk sda
sd 1:0:0:0: Attached scsi disk sdb
md: cannot remove active disk sda1 from md0 ... 
raid1: Disk failure on sda1, disabling device. 
```

; Remove and re-add failed partitions

In order to attempt to rebuild a partition, we can remove and re-add.

```
mdadm /dev/md2 --remove /dev/sda3
mdadm /dev/md2 --add /dev/sda3
```

### Remove faulty drive partitions ###

```
root@server ~ # mdadm /dev/md0 --remove /dev/sda1
mdadm: hot remove failed for /dev/sda1: Device or resource busy
```

We might need to --set\_faulty to remove the swap partition.

```
root@server ~ # mdadm /dev/md0 --set-faulty /dev/sda1
mdadm: set /dev/sda1 faulty in /dev/md0
```

Remove all the partitions of the faulty drive to be physically removed.

```
root@server ~ # mdadm /dev/md0 --remove /dev/sda1
root@server ~ # mdadm /dev/md1 --remove /dev/sda2
root@server ~ # mdadm /dev/md2 --remove /dev/sda3
```

```
root@server ~ # cat /proc/mdstat 
Personalities : [raid1] [raid0] [raid6] [raid5] [raid4] [raid10] 
md0 : active raid1 sdb1[1]
      2102464 blocks [2/1] [_U]
      
md1 : active raid1 sdb2[1]
      264960 blocks [2/1] [_U]
      
md2 : active raid1 sdb3[1]
      974390336 blocks [2/1] [_U]
```

### Serial number ###

Check the serial number of the drive to remove.
```
hdparm -i /dev/sda
```

### Grub ###

Ensure that grub is installed on the secondary drive so when the primary one is removed, the system still boots.

```
root@server ~ # cat /boot/grub/device.map
(hd0)   /dev/sda
(hd1)   /dev/sdb
```

One can use grub interactive shell to install boot onto both RAID1 drives, as follows.

```
root@server ~ # grub
grub> device (hd0) /dev/sda
device (hd0) /dev/sda
grub> root (hd0,1) 
root (hd0,1)
 Filesystem type is ext2fs, partition type 0xfd
grub> setup (hd0)
setup (hd0)
```

```
root@server ~ # grub
grub> device (hd1) /dev/sdb
device (hd1) /dev/sdb
grub> root (hd1,1) 
root (hd1,1)
 Filesystem type is ext2fs, partition type 0xfd
grub> setup (hd1)
setup (hd1)
```

Incidently if the `/dev/sda` is faulty, the following would also work, referring to `/dev/sdb` as `hd0`.

```
root@server ~ # grub
grub> device (hd0) /dev/sdb
grub> root (hd0,1) 
root (hd0,1)
 Filesystem type is ext2fs, partition type 0xfd
grub> setup (hd0)
setup (hd0)
```

However when the new drive is inserted, we must install grub into its MBR as well i.e. both `hd0` and `hd1`.

### grub-install ###

On CentOS (RHEL, Fedora), one can use grub-install, rather than the above interactive shell, `grub`.

```
root@server /home/evanx # vi /boot/grub/grub.conf

timeout 5
default 0

title CentOS (2.6.18-274.7.1.el5)
root (hd1,1)
kernel /vmlinuz-2.6.18-274.7.1.el5 ro root=/dev/md2 vga=0x317 selinux=0
initrd /initrd-2.6.18-274.7.1.el5.img

root@server /home/evanx # /sbin/grub-install /dev/sdb
```

where we changed the root device hd1 (where /boot resides) in grub.conf and then run grub-install to install grub in the MBR of that device.

In case the drives switch e.g. the good sdb drives becomes sda, one can create boot option for either drive as follows.

```
timeout 5
default 0

title CentOS (2.6.18-274.7.1.el5) hd0
root (hd0,1)
kernel /vmlinuz-2.6.18-274.7.1.el5 ro root=/dev/md2 vga=0x317 selinux=0
initrd /initrd-2.6.18-274.7.1.el5.img

title CentOS (2.6.18-274.7.1.el5) hd1
root (hd1,1)
kernel /vmlinuz-2.6.18-274.7.1.el5 ro root=/dev/md2 vga=0x317 selinux=0
initrd /initrd-2.6.18-274.7.1.el5.img
```

### Rebuilt ###

We check the new drive installed
```
root@server ~ # hdparm -i /dev/sdb
Model=ST1000NM0011                            , FwRev=SN02    , SerialNo=            W1N03QQ8
```

Note the serial number above to confirm it is the new drive we installed.

Partition the new drive the same as the existing OK drive.

```
root@server ~ # fdisk /dev/sdb

Command (m for help): p

Disk /dev/sdb: 1000.2 GB, 1000204886016 bytes
255 heads, 63 sectors/track, 121601 cylinders
Units = cylinders of 16065 * 512 = 8225280 bytes

   Device Boot      Start         End      Blocks   Id  System
/dev/sdb1               1         262     2104483+  fd  Linux raid autodetect
/dev/sdb2             263         295      265072+  fd  Linux raid autodetect
/dev/sdb3             296      121601   974390445   fd  Linux raid autodetect
```

Add partitions to the array.

```
root@server ~ # mdadm /dev/md0 -a /dev/sdb1
mdadm: added /dev/sdb1

root@server ~ # mdadm /dev/md1 -a /dev/sdb2
mdadm: added /dev/sdb2

root@server ~ # mdadm /dev/md2 -a /dev/sdb3
mdadm: added /dev/sdb3
```

Monitor the progress of the rebuild.

```
root@server ~ # cat /proc/mdstat

md0 : active raid1 sdb1[2] sda1[1]
      2102464 blocks [2/1] [_U]
      [=====>...............]  recovery = 28.0% (590528/2102464) finish=0.3min speed=65614K/sec     
md1 : active raid1 sdb2[2] sda2[1]       264960 blocks [2/1] [_U]
      resync=DELAYED     
md2 : active raid1 sdb3[2] sda3[1]
      974390336 blocks [2/1] [_U]
      resync=DELAYED     

root@server ~ # cat /proc/mdstat 

md0 : active raid1 sdb1[0] sda1[1]
      2102464 blocks [2/2] [UU]      
md1 : active raid1 sdb2[0] sda2[1]
      264960 blocks [2/2] [UU]      
md2 : active raid1 sdb3[2] sda3[1]
      974390336 blocks [2/1] [_U]
      [>....................]  recovery =  2.4% (23479424/974390336) finish=264.6min speed=59882K/sec
```