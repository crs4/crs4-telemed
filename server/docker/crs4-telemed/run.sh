#!/bin/bash
DB_IP_ADDR=$1
NET_IF=$2
IP_ADDR=$(ifconfig $NET_IF | grep "inet addr:" | cut -d : -f 2 | cut -d " " -f 1)

sed -i 's/tcp_listen.*/tcp_listen\t\t'$IP_ADDR:3478'/' /etc/restund.conf
sed -i 's/turn_relay_addr[^6].*/turn_relay_addr\t\t'$IP_ADDR'/' /etc/restund.conf
sed -i 's/status_http_addr.*/status_http_addr\t\t'$IP_ADDR'/' /etc/restund.conf
sqlite3 /opt/crs4-telemed/server/most/demo.db 'update voip_turnserver set address = "'$DB_IP_ADDR'" where id = 1'
sqlite3 /opt/crs4-telemed/server/most/demo.db 'update voip_sipserver set address = "'$DB_IP_ADDR'" where id = 1'
sqlite3 /opt/crs4-telemed/server/most/demo.db 'update provider_client set url = "http://'$DB_IP_ADDR'", redirect_uri = "http://'$DB_IP_ADDR'" where id = 1'
service mysql start
service asterisk start
restund

cd /opt/crs4-telemed && make run