# clean old init
if [ -e /etc/init.d/agate ]; then
  service agate stop
  chkconfig --del agate
  systemctl daemon-reload
fi
exit 0
