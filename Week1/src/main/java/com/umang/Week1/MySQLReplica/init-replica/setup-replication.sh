#!/bin/bash
set -e

echo "Waiting for source to be ready..."
until mysql -hmysql-source -uroot -proot_password -e "SELECT 1;" >/dev/null 2>&1; do
  sleep 3
done

echo "Getting master log file and position..."
MASTER_STATUS=$(mysql -hmysql-source -uroot -proot_password -e "SHOW MASTER STATUS;" | awk 'NR==2')
MASTER_LOG_FILE=$(echo $MASTER_STATUS | awk '{print $1}')
MASTER_LOG_POS=$(echo $MASTER_STATUS | awk '{print $2}')

echo "Setting up replication..."
mysql -uroot -proot_password_replica -e "
  CHANGE REPLICATION SOURCE TO
    SOURCE_HOST='mysql-source',
    SOURCE_USER='repl_user',
    SOURCE_PASSWORD='repl_password',
    SOURCE_LOG_FILE='${MASTER_LOG_FILE}',
    SOURCE_LOG_POS=${MASTER_LOG_POS},
    GET_SOURCE_PUBLIC_KEY=1;
  START REPLICA;
"

echo "Replication setup complete."
