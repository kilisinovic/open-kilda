#!/bin/sh
set -e
ctl=sysrepoctl
cfg=sysrepocfg

chdir /
chown -R root: /var/lib/sysrepo
test -d $NMSHOME/yang && {
    for model in `find $NMSHOME/yang  -type f`; do
        $ctl --install $model --search-dirs $NMSHOME/yang \
            && echo "[INSTALL OK] model added: $model" \
            && rm $model
    done
    $ctl --list --apply -v2
}
test -d $NMSHOME/startup && {

    for config in `find $NMSHOME/startup  -type f`; do
        $cfg --edit=$config -f xml -d startup  \
            && echo "[EDIT OK] startup edited: $config"
        $cfg --copy-from startup -d running \
            && echo "[COPY OK] startup => running" \
            && rm $config
    done
}

for main in $NMSHOME/*/main.py; do
    sh -c "($main) &"
done

exec /usr/bin/netopeer2-server -d -v1 -U /var/run/netopeer2-server.sock
