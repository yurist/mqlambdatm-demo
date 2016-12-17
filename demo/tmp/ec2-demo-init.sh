#! /bin/bash

set -e

# MQ_URL=http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/messaging/mqadv/mqadv_dev901_linux_x86-64.tar.gz

# yum -y update
# yum -y install gcc

# mkdir -p /tmp/mq
# cd /tmp/mq
# curl -LO $MQ_URL
# tar -zxvf ./*.tar.gz

# groupadd mqm
# useradd --gid mqm --home-dir /var/mqm mqm
# useradd demo
# echo demo:demo | chpasswd
# usermod -a -G mqm root
# usermod -a -G mqm ec2-user
# usermod -a -G mqm demo

# cd /tmp/mq/MQServer
# ./mqlicense.sh -text_only -accept
# rpm -ivh *.rpm

# /opt/mqm/bin/setmqinst -p /opt/mqm -i

newgrp mqm

source /opt/mqm/bin/setmqenv -s
crtmqm -q LAMBDA_DEMO_QM
srtmqm LAMBDA_DEMO_QM

runmqsc << EOF
ALTER LISTENER('SYSTEM.DEFAULT.LISTENER.TCP') TRPTYPE(TCP) PORT(1414) CONTROL(QMGR)
START LISTENER('SYSTEM.DEFAULT.LISTENER.TCP')
DEFINE CHANNEL(PASSWORD.SVRCONN) CHLTYPE(SVRCONN)
SET CHLAUTH(PASSWORD.SVRCONN) TYPE(BLOCKUSER) USERLIST('nobody') DESCR('Allow privileged users on this channel')
SET CHLAUTH('*') TYPE(ADDRESSMAP) ADDRESS('*') USERSRC(NOACCESS) DESCR('BackStop rule')
SET CHLAUTH(PASSWORD.SVRCONN) TYPE(ADDRESSMAP) ADDRESS('*') USERSRC(CHANNEL) CHCKCLNT(REQUIRED)
ALTER AUTHINFO(SYSTEM.DEFAULT.AUTHINFO.IDPWOS) AUTHTYPE(IDPWOS) ADOPTCTX(YES)
REFRESH SECURITY TYPE(CONNAUTH)
EOF

pip install boto3 pymqi
