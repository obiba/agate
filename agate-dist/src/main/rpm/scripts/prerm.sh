#!/bin/sh
# prerm script for agate
#
# see: dh_installdeb(1)

set -e

# summary of how this script can be called:
#        * <prerm> `remove'
#        * <old-prerm> `upgrade' <new-version>
#        * <new-prerm> `failed-upgrade' <old-version>
#        * <conflictor's-prerm> `remove' `in-favour' <package> <new-version>
#        * <deconfigured's-prerm> `deconfigure' `in-favour'
#          <package-being-installed> <version> `removing'
#          <conflicting-package> <version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package

NAME=agate

case "$1" in

  0)
    chkconfig --del agate

    # Read configuration variable file if it is present
    [ -r /etc/default/$NAME ] && . /etc/default/$NAME

    if which service >/dev/null 2>&1; then
            service agate stop
    elif which invoke-rc.d >/dev/null 2>&1; then
            invoke-rc.d agate stop
    else
            /etc/init.d/agate stop
    fi
  ;;

  1)
  ;;

  *)
    echo "prerm called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac


exit 0
