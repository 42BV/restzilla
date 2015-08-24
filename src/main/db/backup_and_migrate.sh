#!/bin/bash
# settings for migration
DATABASE_NAME="uasdb"
SCHEMA_NAME="public"
DB_USER="uas"
DB_PASSWORD="<<passwd>>"
HOST="localhost"
APP_NAME="uas-backend"

# first make a database dump
HOMEDIR_SCRIPT=`pwd`
PGUSER="postgres"
HOMEDIR_PGUSER="~${PGUSER}"
TIME=`date '+%Y%m%d_%H%M%S'`
BACKUP_DIR="/data/backups/${APP_NAME}/db_backup_before_migration"
BACKUP_FILE="${BACKUP_DIR}/${APP_NAME}_database_${DATABASE_NAME}_${TIME}.sql.gz"
MIGRATE_LOG="${BACKUP_DIR}/${APP_NAME}_migrate_${DATABASE_NAME}_${TIME}.log"

# just make backup dir if not exists
#mkdir -p $BACKUP_DIR
#chown -R $PGUSER:$PGUSER $BACKUP_DIR

echo "start backup database in "${BACKUP_FILE}
echo "log will be written in ${MIGRATE_LOG}"

echo $(date)" ==== start ====" >> ${MIGRATE_LOG}
echo "start backup database"  >> ${MIGRATE_LOG}

# cd naar de home van postgres
eval cd ${HOMEDIR_PGUSER}
sudo -u $PGUSER /usr/bin/pg_dump ${DATABASE_NAME} -c | gzip -f > ${BACKUP_FILE}
DUMPANDZIP_STATUS="${PIPESTATUS[@]}"
echo "dump and zip status:"  ${DUMPANDZIP_STATUS} >> ${MIGRATE_LOG}

echo "backup database in "${BACKUP_FILE} >> ${MIGRATE_LOG}

# Terug naar waar het script staat
cd ${HOMEDIR_SCRIPT}

if [ "${DUMPANDZIP_STATUS}" == "0 0" ]
then
    # now do the database update with liquibase
    echo "liquibase postgres version "
    java -jar lib/liquibase.jar update
else
    echo "***********************************************************************************" >> ${MIGRATE_LOG}
    echo "liquibase update not executed because the backup of the database was not succesfull" >> ${MIGRATE_LOG}
    echo "***********************************************************************************" >> ${MIGRATE_LOG}
fi