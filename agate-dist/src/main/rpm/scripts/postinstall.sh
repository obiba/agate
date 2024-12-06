#!/bin/sh
# postinst script for agate
#

set -e

# summary of how this script can be called:
#        * <postinst> `configure' <most-recently-configured-version>
#        * <old-postinst> `abort-upgrade' <new version>
#        * <conflictor's-postinst> `abort-remove' `in-favour' <package>
#          <new-version>
#        * <postinst> `abort-remove'
#        * <deconfigured's-postinst> `abort-deconfigure' `in-favour'
#          <failed-install-package> <version> `removing'
#          <conflicting-package> <version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package

NAME=agate

[ -r /etc/default/$NAME ] && . /etc/default/$NAME

# Agate file structure on Debian
# /etc/agate: configuration
# /usr/share/agate: executable
# /var/lib/agate: data runtime
# /var/log: logs

rm -f /usr/share/agate
new_release="$(ls -t /usr/share/ |grep agate|head -1)"
ln -s /usr/share/${new_release} /usr/share/agate

if [ ! -e /var/lib/agate/data ] ; then
  mkdir /var/lib/agate/data
fi

if [ ! -e /var/lib/agate/work ] ; then
  mkdir /var/lib/agate/work
fi

if [ ! -e /var/lib/agate/conf ] ; then
  ln -s /etc/agate /var/lib/agate/conf
fi

# Upgrade application.yml if necessary
if grep -q "profiles:" /etc/agate/application.yml
  then
    cp /etc/agate/application.yml /etc/agate/application.yml.2.x
    cat /etc/agate/application.yml.2.x | grep -v "profiles:" > /etc/agate/application.yml
fi
if [ -f /etc/agate/application.yml ] && [ ! -f /etc/agate/application-prod.yml ]
  then
  mv -f /etc/agate/application.yml /etc/agate/application-prod.yml
fi

chown -R agate:adm /var/lib/agate /var/log/agate /etc/agate
chmod -R 750      /var/lib/agate /var/log/agate /etc/agate
find /etc/agate/ -type f -print0 | xargs -0 chmod 640

# if upgrading to 2.0, delete old log4j config
if [ -f "/etc/agate/log4j.properties" ]; then
  mv /etc/agate/log4j.properties /etc/agate/log4j.properties.old
fi

exit 0
