#! /bin/bash 

set -e

QMGR_NAME='LAMBDA_DEMO'
TESTQ='LAMBDATEST.Q'

# check queue manager state inside the container
state()
{
    ((docker exec demo dspmq -n -m $QMGR_NAME 2> /dev/null) || echo "no queue manager yet") | awk -F '[()]' '{ print $4 }'
}

curdepth()
{
    echo "dis ql($TESTQ) curdepth" | \
        docker exec -i demo runmqsc -e | \
        grep 'CURDEPTH(' | \
        sed 's/.*CURDEPTH//' | \
        tr -d '()'
}

log()
{
    echo $1
    aws sqs send-message --queue-url $SQS_QUEUE_URL --message-body "$1"
}

done_well()
{
    aws sqs send-message --queue-url $SQS_QUEUE_URL --message-body "<ok>"
}

done_badly()
{
    aws sqs send-message --queue-url $SQS_QUEUE_URL --message-body "<error>"
}

trap done_badly ERR

log '==========> mqlambdatm demo started'
log '==========> AWS_REGION='$AWS_REGION ' STACK_ID='$STACK_ID ' MYSQL_HOST='$MYSQL_HOST

# install pre-requisites
yum install -y docker mysql56

# start docker service
service docker start

log '==========> Cloning IBM MQ docker git repo'
pushd /tmp
rm -rf mq-docker
git clone https://github.com/ibm-messaging/mq-docker.git

# build MQ server image with samples
log '==========> Building base MQ docker image'
# docker build -t mq-docker --build-arg MQ_PACKAGES="MQSeriesRuntime-*.rpm MQSeriesServer-*.rpm MQSeriesMsg*.rpm MQSeriesJava*.rpm MQSeriesJRE*.rpm MQSeriesGSKit*.rpm MQSeriesSamples*.rpm" ./mq-docker/server/
log '==========> Successfully build base MQ docker image'
popd

log '==========> Building demo docker image'
docker build -t mqlambdatm-demo ../docker/
log '==========> Demo image built'

# wait for the stack creation to complete
log '==========> Waiting for stack creation to complete...'
aws cloudformation wait stack-create-complete --stack-name $STACK_ID
log '==========> Stack ready.'

log '==========> Creating sample database'
mysql -h $MYSQL_HOST --user=demouser --password=demopass < ../sql/demo.sql
log '==========> Sample DB created'

log '==========> Running demo docker container'
docker run -d -p 1414:1414 -e LICENSE=accept -e MQ_QMGR_NAME=$QMGR_NAME -e AWS_REGION=$AWS_REGION -v /var/lambdademo:/var/mqm --name demo mqlambdatm-demo

# Loop until "dspmq" says the queue manager is running
log '==========> Waiting for queue manager to come up'
until [ "`state`" == "RUNNING" ]; do
    sleep 1
done
log '==========> Queue manager running'

log '==========> Putting sample messages to the triggered queue'
docker exec -i demo /opt/mqm/samp/bin/amqsput $TESTQ < ../sql/orders.txt

log '==========> Waiting for the queue to deplete'
until [ `curdepth` == 0 ]; do
    sleep 5
done

log "==========> Row counts from sample DB:"
log "`mysql -h $MYSQL_HOST --user=demouser --password=demopass mqlambdademo <<EOF
select count(*) as 'orders count' from orders;
select count(*) as 'orderdetails count' from orderdetails;
EOF
`"

log "==========> mqlambdatm demo successfully completed"
done_well
