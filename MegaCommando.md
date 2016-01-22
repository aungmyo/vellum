This article is the first installment of the [Bin Bash](BinBash.md) series :)

## Introduction ##

We write lots of cron scripts to perform routine maintenance tasks on our systems, so...

We might want a script to perform various tasks in succession, but also be able to invoke those individually in an adhoc fashion e.g. to test them.

## Template ##

The following is a basic template, with one custom command for illustration, namely <tt>command1_df()</tt>.

```
#!/bin/bash

set -o nounset

command1_df() { # partition
  partition=$1
  df -h | grep $1
}

command0_help() {
  echo "The following commands are available:" 
  cat $0 | grep '^command[0-9]_' | sed 's/^command\([0-9]\)_\(.*\)() { #\(.*\)/\2:\3/' | sort
}

command0_default() {
  echo "Sorry but the default invocation of this script has no functionality yet."
  command0_help
}

invoke() {
  if [ $# -gt 0 ]
  then
    command=$1
    shift
  else
    command=default
  fi
  command$#_$command $@
}

invoke $@
```

Firstly, we always set the bash option <tt>nounset</tt> so that if our script encounters an unset variable, this is treated as an inherently unsafe bug, and the script will abort.

We implement custom commands in the script as per the <tt>command1_df()</tt> example, using a specific naming convention whereby the function name is prefixed by "command" and a digit which is the number of arguments that are expected, courtesy of <tt>$#</tt> in <tt>invoke()</tt>.

If no command-line arguments are provided, we will invoke the function <tt>command0_default()</tt>.

#### Help ####

The above implementation of <tt>command0_help()</tt> lists all the functions at our command, where we document the expected arguments in the script itself via a comment next to the function declaration.

```xml

evanx@beethoven:~$ sh scripts/test.sh
Sorry but the default invocation of this script has no functionality yet.
The following commands are available:
df: partition
```
where <tt>command0_help()</tt> will parse the script itself for command functions, and print any comments regarding their usage.

We might provide help on a specific command as follows.
```sh

command1_help() {
command0_help | grep $1
}
```

For example,

```xml

evanx@beethoven:~$ sh scripts/test.sh help df
df: partition
```

#### Invocation ####

Consider our first custom function, using <tt>df</tt>.

```shell

command1_df() { # partition
partition=$1
df -h | grep $1
}
```

We invoke the <tt>command1_df()</tt> with one argument, which we see in its implementation is
just a pattern we grep from the output of <tt>df</tt>.

```xml

evanx@beethoven:~$ sh scripts/test.sh df home
/dev/sdb2             128G  109G   13G  90% /home
```

If we do not provide the correct number of arguments that the function is expecting, the script will abort as follows.

```xml

evanx@beethoven:~$ sh scripts/test.sh df
scripts/test.sh: 25: command0_df: not found
```
where the intentioned function is actually prefixed by <tt>command1</tt> since it requires 1 argument.

If we provide an invalid command, the script will abort as follows.

```xml

evans@beethoven:~$ sh scripts/test.sh dff home
scripts/test.sh: 25: command1_dff: not found
```

### Example ###

As an example, let's implement a new command to print yesterday's date in our preferred format.

```
command0_yesterday() { # print yesterday's date in YYYY-MM-DD format
  date -d 'yesterday' +'%Y-%m-%d'
}
```

Let's check the usage.

```xml

evans@beethoven:~$ sh scripts/test.sh help yesterday
yesterday: print yesterday's date in YYYY-MM-DD format
```

And let's invoke it.

```xml

evans@beethoven:~$ sh scripts/test.sh yesterday
2013-04-26
```

## Conclusion ##

We introduce a template for a convenient menu-esque approach to invoking functions in a bash script.

The implementation will ensure that <tt>command</tt> functions are invoked with the correct number of arguments, by virtue of the using the number of arguments in the function name itself e.g. <tt>command1</tt> will prefix functions requiring one argument only.

## Coming up ##

In an upcoming article, we'll use the above approach for LSI MegaRAID commands as follows.
```xml

$ sh scripts/megaraid.sh
commands:
alarmDsply:
alarmSilence:
battery:
events:
getProp: prop
help: command
megahelp: (MegaCli64 -help)
latestEvents:
ldGetProp: ld
ldInfo:
ldInfo: ld
ldInfo_state: ld
ldSetProp: ld prop
pdGetMissing:
pdInfo:
pdInfo: slot
pdList:
pdMakeGood: slot
pdRbld_showProg: slot
pdRbld_start: slot
pdReplaceMissing: slot array row
setProp: prop
state_notify: ld
```

where for example the <tt>ldSetProp</tt> function is implemented as follows.

```
megaraid2_ldSetProp() { # ld prop
  /opt/MegaRAID/CmdTool2/CmdTool264 -ldSetProp $2 -l$1 -a0
}
```

Such scripts provide a useful reference for such commands, if nothing else :)

Another example we will present in a further article is a script for keytool and openssl commands.

```xml

$ ./keytool.sh
commands:
certreq: csr_file
changealias: alias destalias
connect: ip port cert
delete: alias
exportcert: pem_output_file
exportp12: p12_file
genkey: alias
importcert: pem_file
importp12: p12_file pass
list:
list: alias
newkeypass: alias keypass new
pkcs12: p12_file
printcert: alias
```

## Sneak preview 2 ##

In a further upcoming article, we'll implement some basic monitoring, including a diskspace check as follows.

```
command0_diskspace() {
  for usage in `df -h | grep "% /" | sed 's/.* \([0-9]*\)%.*/\1/'`
  do
    if [ $usage -gt 40 ]
    then
      echo "WARNING diskspace" `df -h | grep " ${usage}% /"`
    fi
  done
}
```

```xml

# sh scripts/nightly.sh diskspace
WARNING diskspace 1.6T 701G 768G 48% /files
```

## Resources ##

See the [Bin Bash](BinBash.md) page.

