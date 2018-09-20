#!/bin/sh
# postrm script for agate
#

set -e

# summary of how this script can be called:
#        * <postrm> `remove'
#        * <postrm> `purge'
#        * <old-postrm> `upgrade' <new-version>
#        * <new-postrm> `failed-upgrade' <old-version>
#        * <new-postrm> `abort-install'
#        * <new-postrm> `abort-install' <old-version>
#        * <new-postrm> `abort-upgrade' <old-version>
#        * <disappearer's-postrm> `disappear' <overwriter>
#          <overwriter-version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package


case "$1" in
  0)
    userdel -f agate || true
    rm -rf /var/log/agate /tmp/agate /etc/agate /usr/share/agate*

    # Backup agate home
    timestamp="$(date '+%N')"
    echo "Backing up /var/lib/agate to /var/lib/agate-backup-$timestamp"
    mv /var/lib/agate /var/lib/agate-backup-$timestamp >/dev/null 2>&1
    chown -R root:root /var/lib/agate-backup-$timestamp >/dev/null 2>&1
  ;;

  1)
  ;;

  *)
    echo "postrm called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac

exit 0
