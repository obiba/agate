#!/bin/bash
# for update from System-V
systemctl preset agate.service
systemctl start agate.service
exit 0