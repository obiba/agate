#!/bin/bash
# for update from System-V
if [ $1 -eq 0 ] ; then
  # Package removal, not upgrade
  systemctl --no-reload disable agate.service > /dev/null 2>&1 || :
  systemctl stop agate.service > /dev/null 2>&1 || :
fi
exit 0
