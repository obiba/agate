#!/bin/sh

getent group adm >/dev/null || groupadd -r adm
getent passwd agate >/dev/null || \
    useradd -r -g adm -d /home/agate -s /sbin/nologin \
    -c "User for Agate Server" agate
exit 0
