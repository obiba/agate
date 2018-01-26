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

case "$1" in
  [1-2])

    # Create agate user if it doesn't exist.
    if ! id agate > /dev/null 2>&1 ; then
      adduser --system --home-dir /var/lib/agate --no-create-home agate
    fi

    # Agate file structure on Debian
    # /etc/agate: configuration
    # /usr/share/agate: executable
    # /var/lib/agate: data runtime
    # /var/log: logs

    rm -f /usr/share/agate
    new_release="$(ls -t /usr/share/ |grep agate|head -1)"
    ln -s /usr/share/${new_release} /usr/share/agate

    if [ ! -e /var/lib/agate/conf ] ; then
      ln -s /etc/agate /var/lib/agate/conf
    fi

    chown -R agate:adm /var/lib/agate /var/log/agate /etc/agate /tmp/agate
    chmod -R 750      /var/lib/agate /var/log/agate /etc/agate/ /tmp/agate
    find /etc/agate/ -type f | xargs chmod 640

    # if upgrading to 2.0, delete old log4j config
    if [ -f "/etc/agate/log4j.properties" ]; then
      mv /etc/agate/log4j.properties /etc/agate/log4j.properties.old
    fi

    # auto start on reboot
    chkconfig --add agate

    # start agate
    echo "### You can start agate service by executing:"
    echo "sudo /etc/init.d/agate start"

  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac

exit 0
