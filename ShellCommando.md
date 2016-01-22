This article is the first installment of the [Bin Bash](BinBash.md) series :)

## Introduction ##

We write lots of cron scripts to perform routine maintenance tasks on our systems, so...

We might want a script to perform various tasks in succession, but also be able to invoke those individually in an adhoc fashion e.g. to test them.

Also one often wishes to cut and paste commonly used commands into a shell script for later reference, and reuse.

So we introduce an approach for dynamically invoking parameterized functions in a bash script.

## Template ##

The following is a basic template, with one custom command for illustration, namely <tt>command1_dfh</tt>.

```
#!/bin/bash

set -o nounset

command1_dfh() { # volume
  volume=$1
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

We implement custom commands in the script as per the <tt>command1_dfh</tt> example, using a specific naming convention whereby the function name is prefixed by "command" and a digit which is the number of arguments that are expected, courtesy of <tt>"$#"</tt> in <tt>invoke()</tt>.

If no command-line arguments are provided, we will invoke the function <tt>command0_default()</tt>.

#### Help ####

The above implementation of <tt>command0_help()</tt> lists all the functions at our command, where we document the expected arguments in the script itself via a comment next to the function declaration.

```xml

evanx@beethoven:~$ sh scripts/command.sh
Sorry but the default invocation of this script has no functionality yet.
The following commands are available:
dfh: volume
```
where <tt>command0_help()</tt> will parse the script itself for command functions, and print any comments regarding their usage.

We might provide help on a specific command as follows.
```sh

command1_help() {
command0_help | grep $1
}
```
where we have "overloaded" our <tt>help</tt> function per the number of arguments.

Let's try <tt>command1_help</tt> i.e. with 1 argument.

```xml

evanx@beethoven:~$ sh scripts/command.sh help dfh
dfh: volume
```

#### Invocation ####

Consider our first custom function, using <tt>df</tt>.

```shell

command1_dfh() { # volume
volume=$1
df -h | grep $1
}
```

We invoke the <tt>command1_dfh</tt> with one argument, which is a pattern to grep the output of <tt>df</tt>.

```xml

evanx@beethoven:~$ sh scripts/command.sh dfh home
/dev/sdb2             128G  109G   13G  90% /home
```

If we provide an invalid command, the script will abort as follows.

```xml

evans@beethoven:~$ sh scripts/command.sh dff home
scripts/command.sh: 25: command1_dff: not found
```

If we do not provide the correct number of arguments that the function is expecting, the script will abort as follows.

```xml

evanx@beethoven:~$ sh scripts/command.sh dfh
scripts/command.sh: 25: command0_dfh: not found
```
where the intentioned function is actually prefixed by <tt>command1</tt> since it requires 1 argument.

However we can overload functions e.g. let's introduce <tt>command0_dfh()</tt> with no arguments, as follows.

```xml

command0_dfh() { # with no args, executes 'df -h'
df -h
}
```

Our help now shows the overloaded <tt>dfh</tt> commands as follows.

```xml

evanx@beethoven:~$ sh scripts/command.sh help dfh
dfh: volume
dfh: with no args, executes 'df -h'
```

One can implement a <tt>command0</tt> as follows...

```xml

command0_dfh() {
echo "Incorrect usage! Arguments are required for this command, e.g. the volume to grep."
}
```

where this is just help for the usage of same command with arguments :)

### Example ###

As an example, let's implement a new command to print yesterday's date in our preferred format.

```
command0_yesterday() { # print yesterday's date in YYYY-MM-DD format
  date -d 'yesterday' +'%Y-%m-%d'
}
```

Let's check the usage.

```xml

evans@beethoven:~$ sh scripts/command.sh help yesterday
yesterday: print yesterday's date in YYYY-MM-DD format
```

And let's invoke it.

```xml

evans@beethoven:~$ sh scripts/command.sh yesterday
2013-04-26
```

## Minimal ##

Below is a slightly different minimal template for dynamic invocation, without our <tt>command0_help</tt> functionality.

```
set -u

command0_() {
  echo "No default functionality implemented yet"
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  command$#_$command
else
  command0_
fi
```

```xml

evans@beethoven:~$ sh scripts/template.sh
No default functionality implemented yet

evans@beethoven:~$ sh scripts/template.sh help
scripts/template.sh: line 15: command0_help: command not found
```

## Conclusion ##

We introduce a template for a convenient menu-esque approach for dynamically invoking parameterized commands in a bash script.

The implementation will ensure that "command functions" are invoked with the correct number of arguments, by virtue of the using the number of arguments in the function name to be invoked e.g. <tt>command1</tt> will prefix functions requiring one argument only.

Furthermore this allows us to "overload" functions, according to the number of arguments, e.g. to have a more general implementation which takes no arguments, alongside an implementation which takes a number of arguments in order to be more specific.

## Sneak preview ##

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

where for example the <tt>pdInfo</tt> function with 1 argument is implemented as follows.

```
enclosure=14 

command1_pdInfo() { # slot
  /opt/MegaRAID/CmdTool2/CmdTool264 -pdInfo -physdrv [$enclosure:$1] -a0
}
```

where our script contains some context for our environment e.g. the enclosure number (14).

Actually, we can implement context based on the hostname, as follows.

```
context_beethoven() {
  enclosure=14 
}

context_`hostname -s`
```

Naturally this could be explicitly implemented using <tt>if-elif</tt> constructs, but nevertheless the above dynamic invocation approach is quite neat.

```xml

$ ./megaraid.sh pdInfo 5 | grep Error
Media Error Count: 0
Other Error Count: 0
```

Such scripts provide a useful reference for such commands, if nothing else :)

## Resources ##

See the [Bin Bash](BinBash.md) page.