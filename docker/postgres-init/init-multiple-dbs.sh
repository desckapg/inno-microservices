#!/bin/bash
set -e

# Function to create user and database
create_user_and_database() {
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
	create_user_and_database $USER_SERVICE_POSTGRES_DATABASE $USER_SERVICE_POSTGRES_USERNAME $USER_SERVICE_POSTGRES_PASSWORD
fi

# Create Auth Service DB
if [ -n "$AUTH_SERVICE_POSTGRES_DATABASE" ]; then
	create_user_and_database $AUTH_SERVICE_POSTGRES_DATABASE $AUTH_SERVICE_POSTGRES_USERNAME $AUTH_SERVICE_POSTGRES_PASSWORD
fi

# Create Order Service DB
if [ -n "$ORDER_SERVICE_POSTGRES_DATABASE" ]; then
	create_user_and_database $ORDER_SERVICE_POSTGRES_DATABASE $ORDER_SERVICE_POSTGRES_USERNAME $ORDER_SERVICE_POSTGRES_PASSWORD
fi

