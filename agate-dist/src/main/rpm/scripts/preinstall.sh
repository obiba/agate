#!/bin/sh

case "$1" in
  1)
    useradd -r -g nobody -d /var/lib/agate -s /sbin/nologin -c "User for Agate Server" agate
  ;;

  2)
  
    # stop the service if running
    if service agate status > /dev/null; then
      if which service >/dev/null 2>&1; then
        service agate stop
      elif which invoke-rc.d >/dev/null 2>&1; then
        invoke-rc.d agate stop
      else
        /etc/init.d/agate stop
      fi
    fi
      
    usermod -g nobody agate -d /var/lib/agate

    latest_version="$(ls -t /usr/share | grep agate- | head -1| cut -d'-' -f2)"
    if [ ! -z "$latest_version" ] ; then
      latest_version_number="${latest_version//.}"
      if [ $latest_version_number -lt 131 ] ; then
        echo
        echo "WARNING: versions before 1.3.1 have an uninstall script error, please run the"
        echo "following script to safely remove the current version before installing a new"
        echo "version:"
        echo
        echo "https://github.com/obiba/agate/releases/download/1.3.1/safely-remove-agate-package-before-1.3.1.sh.gz"
        echo
        exit 1
      fi
    fi
  ;;
esac

exit 0
