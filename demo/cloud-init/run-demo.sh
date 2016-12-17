#! /bin/bash 

set -e

QMGR_NAME=LAMBDA_DEMO
TESTQ='LAMBDATEST.Q'

# check queue manager state inside the container
state()
{
    docker exec demo dspmq -n -m $QMGR_NAME | awk -F '[()]' '{ print $4 }'
}

curdepth()
{
    echo "dis ql($TESTQ) curdepth" | \
        docker exec -i demo runmqsc -e | \
        grep 'CURDEPTH(' | \
        sed 's/.*CURDEPTH//' | \
        tr -d '()'
}

# install pre-requisites
yum install -y docker mysql56

# pull IBM mq-docker image
pushd /tmp
git clone https://github.com/ibm-messaging/mq-docker.git

# build MQ server image with samples
docker build -t mq-docker --build-arg MQ_PACKAGES="MQSeriesRuntime-*.rpm MQSeriesServer-*.rpm MQSeriesMsg*.rpm MQSeriesJava*.rpm MQSeriesJRE*.rpm MQSeriesGSKit*.rpm MQSeriesSamples*.rpm" ./server/
popd

# build demo image
docker build -t mqlambdatm-demo ../docker/

# wait for the stack creation to complete
aws cloudformation wait stack-create-complete --stack-name $STACK_ID

# create sample database
mysql -h $MYSQL_HOST --user=demouser --password=demopass < ../sql/demo.sql

# run demo image
docker run -d -p 1414:1414 -e LICENSE=accept -e MQ_QMGR_NAME=$QMGR_NAME -e AWS_REGION=$AWS_REGION -v /var/lambdademo:/var/mqm --name demo mqlambda-demo

# Loop until "dspmq" says the queue manager is running
until [ "`state`" == "RUNNING" ]; do
    sleep 1
done

# put sample messages to the triggered queue
docker exec -i demo /opt/mqm/samp/bin/amqsput $TESTQ < ../sql/orders.txt

# wait until the queue is depleted
until [ `curdepth` == 0 ]; do
    sleep 5
done

echo "demo successfully completed"
