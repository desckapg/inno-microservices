#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

ENV=${1:-dev}
NAMESPACE=${2:-dev}

echo -e "${GREEN}Creating Kubernetes Secrets${NC}"
echo -e "${GREEN}Environment: ${ENV}${NC}"
echo -e "${GREEN}Namespace: ${NAMESPACE}${NC}"
echo ""

ENV_FILE=".env.${ENV}"
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}Error: File $ENV_FILE not found!${NC}"
    echo -e "${YELLOW}Please copy .env.${ENV}.example to $ENV_FILE and fill in the values${NC}"
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

echo -e "${YELLOW}Checking namespace...${NC}"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}✓ Namespace ready${NC}"
echo ""

# PostgreSQL Secrets
echo -e "${YELLOW}Creating PostgreSQL secrets...${NC}"
kubectl create secret generic postgres-credentials \
  --from-literal=postgres-password="$POSTGRES_PASSWORD" \
  --from-literal=user-service-password="$USER_SERVICE_POSTGRES_PASSWORD" \
  --from-literal=auth-service-password="$AUTH_SERVICE_POSTGRES_PASSWORD" \
  --from-literal=order-service-password="$ORDER_SERVICE_POSTGRES_PASSWORD" \
  --namespace=$NAMESPACE \
  --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}✓ PostgreSQL secrets created${NC}"

# PostgreSQL Init Scripts
echo -e "${YELLOW}Creating PostgreSQL init scripts...${NC}"
kubectl create secret generic postgres-init-scripts \
  --from-literal=init-databases.sh="#!/bin/bash
set -e

export PGPASSWORD=\"\$POSTGRES_PASSWORD\"

psql -v ON_ERROR_STOP=1 --username \"postgres\" <<-EOSQL
  CREATE DATABASE user_service_db;
  CREATE USER user_service_user WITH ENCRYPTED PASSWORD '$USER_SERVICE_POSTGRES_PASSWORD';
  GRANT ALL PRIVILEGES ON DATABASE user_service_db TO user_service_user;

  \c user_service_db
  GRANT ALL ON SCHEMA public TO user_service_user;
  GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO user_service_user;
  GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO user_service_user;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO user_service_user;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO user_service_user;

  \c postgres
  CREATE DATABASE auth_service_db;
  CREATE USER auth_service_user WITH ENCRYPTED PASSWORD '$AUTH_SERVICE_POSTGRES_PASSWORD';
  GRANT ALL PRIVILEGES ON DATABASE auth_service_db TO auth_service_user;

  \c auth_service_db
  GRANT ALL ON SCHEMA public TO auth_service_user;
  GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO auth_service_user;
  GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO auth_service_user;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO auth_service_user;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO auth_service_user;

  \c postgres
  CREATE DATABASE order_service_db;
  CREATE USER order_service_user WITH ENCRYPTED PASSWORD '$ORDER_SERVICE_POSTGRES_PASSWORD';
  GRANT ALL PRIVILEGES ON DATABASE order_service_db TO order_service_user;

  \c order_service_db
  GRANT ALL ON SCHEMA public TO order_service_user;
  GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO order_service_user;
  GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO order_service_user;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO order_service_user;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO order_service_user;
EOSQL
" \
  --namespace=$NAMESPACE \
  --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}✓ PostgreSQL init scripts created${NC}"

# MongoDB Secrets
echo -e "${YELLOW}Creating MongoDB secrets...${NC}"
kubectl create secret generic mongodb-credentials \
  --from-literal=mongodb-root-password="$MONGO_ROOT_PASSWORD" \
  --from-literal=mongodb-passwords="$MONGO_PASSWORD" \
  --namespace=$NAMESPACE \
  --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}✓ MongoDB secrets created${NC}"

# Redis Secrets
echo -e "${YELLOW}Creating Redis secrets...${NC}"
kubectl create secret generic redis-credentials \
  --from-literal=password="$REDIS_PASSWORD" \
  --namespace=$NAMESPACE \
  --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}✓ Redis secrets created${NC}"

# User Service Secrets
echo -e "${YELLOW}Creating User service secrets...${NC}"
kubectl create secret generic user-service-credentials \
  --from-literal=DB_USERNAME="$USER_SERVICE_DB_USERNAME" \
  --from-literal=DB_PASSWORD="$USER_SERVICE_DB_PASSWORD" \
  --from-literal=REDIS_USER="$USER_SERVICE_REDIS_USER" \
  --from-literal=REDIS_USER_PASSWORD="$USER_SERVICE_REDIS_USER_PASSWORD" \
  --namespace=$NAMESPACE \
  --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}✓ User service secrets created${NC}"

# Order Service Secrets
echo -e "${YELLOW}Creating Order service secrets...${NC}"
kubectl create secret generic order-service-credentials \
  --from-literal=DB_USERNAME="$ORDER_SERVICE_DB_USERNAME" \
  --from-literal=DB_PASSWORD="$ORDER_SERVICE_DB_PASSWORD" \
  --namespace=$NAMESPACE \
  --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}✓ User service secrets created${NC}"

echo ""
echo -e "${GREEN}All secrets created successfully!${NC}"
echo ""
echo -e "${YELLOW}To view secrets:${NC}"
echo "  kubectl get secrets -n $NAMESPACE"
echo ""
echo -e "${YELLOW}To decode a secret:${NC}"
echo "  kubectl get secret postgres-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-password}' | base64 --decode"