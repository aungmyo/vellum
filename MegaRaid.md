# MegaRAID #

## Monitor ##

We can use the following command to the check the state of the MegaRAID controller.
```
  # /opt/MegaRAID/MegaCli//MegaCli64 -LDInfo -a0 -L0
  Adapter 0 -- Virtual Drive Information:
  Virtual Disk: 0 (Target Id: 0)
  Name:
  RAID Level: Primary-1, Secondary-3, RAID Level Qualifier-0
  Size:1427649MB
  State: Optimal
  Stripe Size: 128kB
  Number Of Drives:2
  Span Depth:3
  Default Cache Policy: WriteBack, ReadAdaptive, Direct, Write Cache OK if Bad BBU
  Current Cache Policy: WriteBack, ReadAdaptive, Direct, Write Cache OK if Bad BBU
  Access Policy: Read/Write
  Disk Cache Policy: Disk''s Default
  Exit Code: 0x00
```
where -a0 indicates the 1st adapter (and only adapter in our case) and -L0 indicates the 1st Logical Disk (aka "Virtual Disk" as opposed to "Physical Disk").

We check the status regularly via the crontab.

```
  root@server ~: crontab -l
  MAILTO=evans.summers@gmail.com
  * * * * * /scripts/check-megaraid
```

The script checks the MegaRAID state every minute using the `MegaCli64` utility as follows.
```
  root@server ~: cat /opt/scripts/check-megaraid
  PATH=/opt/MegaRAID/MegaCli:$PATH
  MegaCli64 -LDInfo -a0 -L0 | grep -q "^State: Optimal"
  if [ $? -ne 0 ]
  then
    state=``MegaCli64 -LDInfo -aALL -Lall | grep "^State: "``
    MegaCli64 -LDInfo -aALL -Lall | mail -s "MegaRAID $state" evan.summers@gmail.com
    exit 2
  fi
```
where although the cron emails output, that might be filtered out and not receive urgent attention, so we use mail explicitly to provide an appropriate subject.

Note that in the case where we have two logical disks, we need to check -L1 as well.

For the purposes of a Nagios plugin, the script is modified as follows.
```
  function check_logical {
    MegaCli64 -LDInfo -L$1 -a0 | grep -q "^State: Optimal"
    if [ $? -ne 0 ]
    then
      echo "MegaRaid CRITICAL - Logical disk $1 not optimal"
      exit 2
    fi
  }
  check_logical 0
  check_logical 1
  echo "MegaRaid OK"
  exit 0
```

If the state is not optimal we must check the the event log.
```
  MegaCli64 -AdpEventLog -GetEvents -f events.log -aALL
```

We can script this as follows.
```
  tmp=/tmp/$USER.``basename $0 .sh``
  
  function grep_event_log_error {
    MegaCli64 -AdpEventLog -GetEvents -f $tmp -aALL
    grep -i error -B5 $tmp | grep -i "^Time\|error"
  }
```

Additionally we should check the battery backup, especially if we have configured our database server not to `fsync`
to boost write performance on the assumption that in the event of a power-failure the RAID controller will still retain data not written to disk.
```
  function check_battery {
    $bin/MegaCli64 -AdpBbuCmd -GetBbuStatus -aALL
  }
```

## Check drive ##

```
[root@server ~]# /opt/MegaRAID/MegaCli/MegaCli64 -pdlist -a0
```

```
[root@server ~]# /opt/MegaRAID/MegaCli/MegaCli64 -pdlist -a0 | grep "Slot\|Error\|Firm"
Slot Number: 0
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Slot Number: 1
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Slot Number: 2
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Slot Number: 3
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Slot Number: 4
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Slot Number: 5
Media Error Count: 0
Other Error Count: 0
Firmware state: Unconfigured(bad)
```

```
[root@server ~]# /opt/MegaRAID/MegaCli/MegaCli64 -pdinfo -physdrv [14:5] -a0
Enclosure Device ID: 14
Slot Number: 5
Device Id: 11
Sequence Number: 6
Media Error Count: 0
Other Error Count: 0
Predictive Failure Count: 0
Last Predictive Failure Event Seq Number: 0
PD Type: SATA
Raw Size: 953869MB [0x74706db0 Sectors]
Non Coerced Size: 953357MB [0x74606db0 Sectors]
Coerced Size: 952720MB [0x744c8000 Sectors]
Firmware state: Unconfigured(bad)
SAS Address(0): 0x9272109756e635a
Connected Port Number: 5(path0)
Inquiry Data: ATA     ST31000528AS    CC38            9VP3PAN5
Foreign State: Foreign
```

```
[root@server ~]# sh scripts/check_megaraid_events_latest.sh
Time: Wed Jul  4 10:50:50 2012
Event Description: Enclosure PD 0e(c None/p1) sensor 5 bad
Time: Wed Jul  4 10:48:54 2012
Event Description: Inserted: PD 0b(e0xff/s11) Info: enclPd=ffff, scsiType=0, portMap=05, sasAddr=09272109756e635a,0000000000000000
Time: Wed Jul  4 10:48:54 2012
Event Description: Inserted: PD 0b(e0xff/s11)
Time: Wed Jul  4 10:48:15 2012
Event Description: State change on PD 0b(e0x0e/s5) from FAILED(11) to UNCONFIGURED_BAD(1)
Time: Wed Jul  4 10:48:15 2012
Event Description: VD 01/1 is now DEGRADED
Time: Wed Jul  4 10:48:15 2012
Event Description: State change on VD 01/1 from OPTIMAL(3) to DEGRADED(2)
Time: Wed Jul  4 10:48:15 2012
Event Description: VD 00/0 is now DEGRADED
Time: Wed Jul  4 10:48:15 2012
Event Description: State change on VD 00/0 from OPTIMAL(3) to DEGRADED(2)
Time: Wed Jul  4 10:48:15 2012
Event Description: State change on PD 0b(e0x0e/s5) from ONLINE(18) to FAILED(11)
Time: Wed Jul  4 10:48:15 2012
Event Description: Removed: PD 0b(e0x0e/s5) Info: enclPd=0e, scsiType=0, portMap=05, sasAddr=09272109756e635a,0000000000000000
Time: Wed Jul  4 10:48:15 2012
Event Description: Removed: PD 0b(e0x0e/s5)
Time: Wed Jul  4 10:48:10 2012
Event Description: PD 0b(e0x0e/s5) Path 9272109756e635a  reset (Type 03)
Time: Wed Jul  4 10:48:10 2012
Event Description: Command timeout on PD 0b(e0x0e/s5) Path 9272109756e635a, CDB: 2a 00 36 fe 61 32 00 00 10 00
```

## Rebuild ##

View the status of all disks.
```
/opt/MegaRAID/MegaCli/MegaCli64 -ldpdinfo -aall
```

When MegaRAID controller encounters unrecoverable errors on a drive its state might change as follows, as seen in the event logs.
  * <tt>FAILED</tt> due to errors
  * taken <tt>OFFLINE</tt>
  * removed from the array, and its state is <tt>UNCONFIGURED(BAD)</tt>.

Steps to rebuild a drive, and associate physical disk states, are as follows:
  * <tt>-pdMakeGood</tt> - goes from <tt>UNCONFIGURED(BAD)</tt> to <tt>UNCONFIGURED(GOOD)</tt>
  * <tt>-pdReplaceMissing</tt> - put back into array - goes from <tt>UNCONFIGURED</tt> to just <tt>OFFLINE</tt>
  * <tt>-pdRbld</tt> - start rebuilding - goes <tt>REBUILD</tt>
  * when rebuild finished, the disk is <tt>ONLINE</tt>

```
[root@server MegaCli]# ./MegaCli64 -pdmakegood -physdrv[14:5] -a0
[root@server MegaCli]# ./MegaCli64 -pdreplacemissing -physdrv[14:5] -array2 -row1 -a0
[root@server MegaCli]# ./MegaCli64 -pdrbld -start -physdrv[14:5] -a0
```

```
[root@server MegaCli]# ./MegaCli64 -pdinfo -physdrv[14:5] -a0
Enclosure Device ID: 14
Slot Number: 5
Device Id: 11
Sequence Number: 6
Media Error Count: 0
Other Error Count: 0
Predictive Failure Count: 0
Last Predictive Failure Event Seq Number: 0
PD Type: SATA
Raw Size: 953869MB [0x74706db0 Sectors]
Non Coerced Size: 953357MB [0x74606db0 Sectors]
Coerced Size: 952720MB [0x744c8000 Sectors]
Firmware state: Unconfigured(bad)
SAS Address(0): 0x9272109756e635a
Connected Port Number: 5(path0)
Inquiry Data: ATA     ST31000528AS    CC38            9VP3PAN5
Foreign State: Foreign
```

```
[root@server MegaCli]# ./MegaCli64 -pdmakegood -physdrv[14:5] -a0
Adapter: 0: EnclId-14 SlotId-5 state changed to Unconfigured-Good.
```

```
[root@server MegaCli]# ./MegaCli64  -pdgetmissing -a0
    Adapter 0 - Missing Physical drives
    No.   Array   Row   Size Expected
    0     2       1     952720 MB
```

```
[root@server MegaCli]# ./MegaCli64 -pdreplacemissing -physdrv[14:5] -array2 -row1 -a0
Adapter: 0: Missing PD at Array 2, Row 1 is replaced.
```

```
[root@server MegaCli]# ./MegaCli64 -pdrbld -start -physdrv[14:5] -a0
Started rebuild progress on device(Encl-14 Slot-5)
```

```
[root@server ~]# MegaCli64 -pdrbld -showprog -physdrv[14:5] -a0
Rebuild Progress on Device at Enclosure 14, Slot 5 Completed 71% in 113 Minutes.
```

Rebuild might take 3 hours or so.

```
[root@server MegaCli]# MegaCli64 -AdpEventLog -GetEvents -f /tmp/megaraid.log -a0

Time: Fri May 25 21:04:35 2012
Event Description: State change on PD 0b(e0x0e/s5) from OFFLINE(10) to REBUILD(14)

Time: Sat May 26 00:01:48 2012
Event Description: VD 01/1 is now OPTIMAL
```

Once complete, we see the following.

```
[root@server MegaCli]# ./MegaCli64 -pdlist -a0 | grep "Slot\|Firmware\|Foreign\|Error"
Slot Number: 0
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Foreign State: None 
Slot Number: 1
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Foreign State: None 
Slot Number: 2
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Foreign State: None 
Slot Number: 3
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Foreign State: None 
Slot Number: 4
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Foreign State: None 
Slot Number: 5
Media Error Count: 0
Other Error Count: 0
Firmware state: Online
Foreign State: None
```

## Alarm ##

Silence alarm.
```
[root@server ~]# /opt/MegaRAID/MegaCli/MegaCli64 -adpSetProp AlarmSilence -a0
Adapter 0: Set alarm to Silenced success.
```

Disable alarm.
```
[root@server ~]# /opt/MegaRAID/MegaCli/MegaCli64 -adpSetProp AlarmDsbl -a0
Adapter 0: Set alarm to Disabled success.
```

Check alarm status.
```
[root@server ~]# /opt/MegaRAID/MegaCli/MegaCli64 -adpGetProp alarmdsply -a0
Adapter 0: Alarm Status is Disabled
```

Check all adapter info for alarm.
```
[root@server ~]# /opt/MegaRAID/MegaCli/MegaCli64 -adpAllInfo -a0 | grep -i alarm
Alarm           : Present
Alarm           : Disabled
Alarm Control   : Yes
Alarm Disable   : Yes
```
