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

echo ""
echo -e "${GREEN}All secrets created successfully!${NC}"
echo ""
echo -e "${YELLOW}To view secrets:${NC}"
echo "  kubectl get secrets -n $NAMESPACE"
echo ""
echo -e "${YELLOW}To decode a secret:${NC}"
echo "  kubectl get secret postgres-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-password}' | base64 --decode"