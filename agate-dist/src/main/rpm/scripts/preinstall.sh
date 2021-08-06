#!/bin/sh

getent group adm >/dev/null || groupadd -r adm

getent passwd agate >/dev/null || \
	  useradd -r -g nobody -d /var/lib/agate -s /sbin/nologin -c "agate service user" agate
exit 0
