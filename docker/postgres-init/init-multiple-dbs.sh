#!/bin/bash
set -e

# Function to create user and database
create_service_user_and_database() {
	local database=$1
	local user=$2
	local password=$3
	echo "  Creating user and database '$database'"
	psql -U "${POSTGRES_USERNAME}" <<-EOSQL
	    CREATE USER $user WITH PASSWORD '$password';
	    CREATE DATABASE "$database";
	    GRANT ALL PRIVILEGES ON DATABASE "$database" TO $user;
        ALTER DATABASE "$database" OWNER TO $user;
EOSQL
}

# Create User Service DB
if [ -n "$USER_SERVICE_POSTGRES_DATABASE" ]; then
	create_service_user_and_database "$USER_SERVICE_POSTGRES_DATABASE" "$USER_SERVICE_POSTGRES_USERNAME" "$USER_SERVICE_POSTGRES_PASSWORD"
fi

# Create Auth Service DB
if [ -n "$AUTH_SERVICE_POSTGRES_DATABASE" ]; then
	create_service_user_and_database "$AUTH_SERVICE_POSTGRES_DATABASE" "$AUTH_SERVICE_POSTGRES_USERNAME" "$AUTH_SERVICE_POSTGRES_PASSWORD"
fi

# Create Order Service DB
if [ -n "$ORDER_SERVICE_POSTGRES_DATABASE" ]; then
	create_service_user_and_database "$ORDER_SERVICE_POSTGRES_DATABASE" "$ORDER_SERVICE_POSTGRES_USERNAME" "$ORDER_SERVICE_POSTGRES_PASSWORD"
fi

# Create grafana user
if [ -n "$GRAFANA_POSTGRES_USERNAME" ]; then
    echo "  Creating grafana user"
    psql -U "${POSTGRES_USERNAME}" <<-EOSQL
      CREATE USER $GRAFANA_POSTGRES_USERNAME WITH PASSWORD '$GRAFANA_POSTGRES_PASSWORD';
      GRANT SELECT ON ALL TABLES IN SCHEMA public TO $GRAFANA_POSTGRES_USERNAME;
EOSQL
fi


# Create prometheus collector user
if [ -n "$PROMETHEUS_EXPORTER_POSTGRES_USERNAME" ]; then
    echo "  Creating prometheus collector user"
    psql -U "${POSTGRES_USERNAME}" <<-EOSQL
    CREATE OR REPLACE FUNCTION __tmp_create_user() returns void as \$\$
    BEGIN
      IF NOT EXISTS (
          SELECT                       -- SELECT list can stay empty for this
          FROM   pg_catalog.pg_user
          WHERE  usename = '$PROMETHEUS_EXPORTER_POSTGRES_USERNAME') THEN
        CREATE USER $PROMETHEUS_EXPORTER_POSTGRES_USERNAME;
      END IF;
    END;
    \$\$ language plpgsql;

    SELECT __tmp_create_user();
    DROP FUNCTION __tmp_create_user();

    ALTER USER $PROMETHEUS_EXPORTER_POSTGRES_USERNAME WITH PASSWORD '$PROMETHEUS_EXPORTER_POSTGRES_PASSWORD';
    ALTER USER $PROMETHEUS_EXPORTER_POSTGRES_USERNAME SET SEARCH_PATH TO $PROMETHEUS_EXPORTER_POSTGRES_USERNAME,pg_catalog;

    GRANT CONNECT ON DATABASE postgres TO $PROMETHEUS_EXPORTER_POSTGRES_USERNAME;
    GRANT pg_monitor to $PROMETHEUS_EXPORTER_POSTGRES_USERNAME;
EOSQL
fi
