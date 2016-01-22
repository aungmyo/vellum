
```
die() {
  exitCode=$1
  shift
  echo "$@"
  exit $exitCode
}

keytool0_list() {
  keytool -keystore $keystore -storepass $pass -list
}

keytool2_genkey() {
  [ -f $keystore ] && die 1 "keystore file exists: $keystore"
  keytool -keystore $keystore -genkey -alias $1 -storetype JKS -keyalg rsa -validity 3650 -storepass $pass -keypass $pass -dname "$2" 
}

keytool1_print() {
  keytool -keystore $keystore -storepass $pass -list -v -alias $1
}

keytool1_delete() {
  keytool -keystore $keystore -storepass $pass -delete -alias $1
}

keytool1_importcert() {
  [ -f $1 ] && keytool -keystore $keystore -storepass $pass -importcert -file $1 -alias `basename $1 .pem`
}

keytool1_exportcert() {
  pem=~/keystores/tmp/$1.pem
  keytool -keystore $keystore -storepass $pass -alias $1 -exportcert -rfc > $pem
  openssl x509 -text in $pem
}

keytool1_exportp12() {
  keytool -importkeystore -srckeystore $keystore -srcalias $1 -srcstorepass $pass -srckeypass $pass \
    -destkeystore $1.p12 -deststoretype pkcs12 -destalias $1 -deststorepass $pass -destkeypass $pass
}

keytool2_connect() {
  echo "connect ip:port $1, private p12 pem $2"
  openssl x509 -text -in $2 | grep "Subject:\|Issuer"
  openssl s_client -connect $1 -cert $2 -showcerts
}

keytool1_pkcs12() {
  openssl pkcs12 -in $1.p12
}

[ $# -lt 3 ] && die 1 "usage: keystore pass command"

set -e 
set -x

keystore=$1
shift

pass=$1
shift

command=$1
shift

keytool${#}_$command $@
```