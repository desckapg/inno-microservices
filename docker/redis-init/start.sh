#!/bin/bash
set -e

mkdir -p /usr/local/etc/redis
echo "bind 0.0.0.0" > /usr/local/etc/redis/redis.conf
echo "appendonly yes" >> /usr/local/etc/redis/redis.conf
echo "appendfsync everysec" >> /usr/local/etc/redis/redis.conf

# Start ACL file with default user
echo "user default on nopass ~* +@all" > /usr/local/etc/redis/users.acl

# Collect users to add
touch /tmp/users_to_add

if [ -n "$USER_SERVICE_REDIS_USER" ] && [ -n "$USER_SERVICE_REDIS_USER_PASSWORD" ]; then
    echo "user $USER_SERVICE_REDIS_USER on >$USER_SERVICE_REDIS_USER_PASSWORD ~* +@all" >> /tmp/users_to_add
fi

if [ -n "$ORDER_SERVICE_REDIS_USER" ] && [ -n "$ORDER_SERVICE_REDIS_USER_PASSWORD" ]; then
    echo "user $ORDER_SERVICE_REDIS_USER on >$ORDER_SERVICE_REDIS_USER_PASSWORD ~* +@all" >> /tmp/users_to_add
fi

if [ -n "$PAYMENT_SERVICE_REDIS_USER" ] && [ -n "$PAYMENT_SERVICE_REDIS_USER_PASSWORD" ]; then
    echo "user $PAYMENT_SERVICE_REDIS_USER on >$PAYMENT_SERVICE_REDIS_USER_PASSWORD ~* +@all" >> /tmp/users_to_add
fi

# Deduplicate and append to ACL
if [ -f /tmp/users_to_add ]; then
    sort -u /tmp/users_to_add >> /usr/local/etc/redis/users.acl
fi

# Start Redis
redis-server /usr/local/etc/redis/redis.conf --aclfile /usr/local/etc/redis/users.acl

