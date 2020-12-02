### GENERATE KEYPAIR
privkey=$HOME/.ssh/id_rsa
pubkey=$HOME/.ssh/id_rsa.pub
mkdir -p $HOME/.ssh
openssl genrsa -out $privkey
openssl rsa -pubout -out $pubkey -in $privkey
cp $pubkey $HOME/.ssh/authorized_keys

### NETOPEER REQUIREMENTS
MODPATH=/usr/share/yang/modules/netopeer2
while read p; do sysrepoctl --install $p && echo installed $p; done <<EOF
$MODPATH/notifications@2008-07-14.yang
$MODPATH/nc-notifications@2008-07-14.yang
$MODPATH/ietf-x509-cert-to-name@2014-12-10.yang
$MODPATH/ietf-tcp-common@2019-07-02.yang
$MODPATH/ietf-tcp-client@2019-07-02.yang
$MODPATH/ietf-tcp-server@2019-07-02.yang
$MODPATH/ietf-ssh-common@2019-07-02.yang
$MODPATH/ietf-netconf-acm@2018-02-14.yang
$MODPATH/ietf-crypto-types@2019-07-02.yang
$MODPATH/ietf-keystore@2019-07-02.yang
$MODPATH/iana-crypt-hash@2014-08-06.yang
$MODPATH/ietf-ssh-server@2019-07-02.yang
$MODPATH/ietf-tls-common@2019-07-02.yang
$MODPATH/ietf-truststore@2019-07-02.yang
$MODPATH/ietf-tls-server@2019-07-02.yang
$MODPATH/ietf-netconf-server@2019-07-02.yang
$MODPATH/ietf-netconf-nmda@2019-01-07.yang
$MODPATH/ietf-netconf-monitoring@2010-10-04.yang
EOF

### REQUIRED FEATURES
sysrepoctl --change ietf-netconf \
    -e candidate            -e startup              -e validate \
    -e writable-running     -e rollback-on-error    -e xpath \
    -e url
echo [OK] ietf-netconf features enabled
sysrepoctl --change ietf-netconf-server \
    -e ssh-listen           -e ssh-call-home
echo [OK] ietf-netconf-server features enabled
