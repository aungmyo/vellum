
```
[root@bizserver ~]# cat /usr/bin/hmail

tmp=/tmp/`basename $0`-$USER-$$.tmp

tee=0
pre=0
hostname=`hostname -s`
from=$USER@`hostname`
subject="$hostname mailer"
timestamp=`date +'%Y-%m-%d %T'`

recipients=$from

while [ $# -gt 0 ]
do
  if [ "$1" = "--pre" ]
  then
    pre=1
  elif [ "$1" = "--tee" ]
  then
    tee=1
  elif [ "$1" = "-s" ]
  then
    if [ $# -gt 0 ]
    then
      shift
      subject=$1
    fi
  else
    echo "$1" | grep -q "@"
    if [ $? -eq 0 ]
    then
      break
    else
      subject=$1
    fi
  fi
  shift
done

if [ $# -gt 0 ]
then
  recipients=$@
fi

cat > $tmp

if [ $pre -eq 0 ]
then
  cat $tmp | grep -q "<"
  if [ $? -ne 0 ]
  then
    pre=1
  fi
fi

if [ $pre -eq 1 ]
then
  cat $tmp > $tmp.tmp
  echo "<pre>" > $tmp
  cat $tmp.tmp | sed -e 's/&/\&amp;/g' | sed -e 's/</\&lt;/g' | sed -e 's/>/\&gt;/g' >> $tmp
  rm -f $tmp.tmp
fi

cat $tmp | grep -q "^Subject: "
if [ $? -eq 0 ]
then
  subject=`cat $tmp | grep -q "^Subject: " | cut -b10-99 | head -1`
fi

if [ `cat $tmp | wc -c | bc` -gt 1 ]
then
  cat $tmp | grep -v "^Subject: \|^To: \|^From: " | mutt -e 'set content_type="text/html"' -s "$subject" $recipients
fi

if [ $tee -eq 1 ]
then
  echo "from: $from"
  echo "to: $recipients"
  echo "date: $timestamp"
  echo "subject: $subject"
  echo "--"
  cat $tmp
fi

rm -f $tmp
```