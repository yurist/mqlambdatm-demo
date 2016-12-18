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

echo '==========> mqlambdatm demo started'
echo '==========> AWS_REGION='$AWS_REGION ' STACK_ID='$STACK_ID ' MYSQL_HOST='$MYSQL_HOST

# install pre-requisites
yum install -y docker mysql56

# start docker service
service docker start

echo '==========> Cloning IBM MQ docker git repo'
pushd /tmp
rm -rf mq-docker
git clone https://github.com/ibm-messaging/mq-docker.git

# build MQ server image with samples
echo '==========> Building base MQ docker image'
# docker build -t mq-docker --build-arg MQ_PACKAGES="MQSeriesRuntime-*.rpm MQSeriesServer-*.rpm MQSeriesMsg*.rpm MQSeriesJava*.rpm MQSeriesJRE*.rpm MQSeriesGSKit*.rpm MQSeriesSamples*.rpm" ./mq-docker/server/
echo '==========> Successfully built base MQ docker image'
popd

echo '==========> Building demo docker image'
docker build -t mqlambdatm-demo ./docker/
echo '==========> Demo image built'

# wait for the stack creation to complete
echo '==========> Waiting for stack creation to complete...'
aws cloudformation wait stack-create-complete --stack-name $STACK_ID
echo '==========> Stack ready.'

# extract MySql host name

jq_select=".Stacks[] | select(.StackName == \"$STACK_ID\") | .Outputs[] | select(.OutputKey == \"MySqlInstancePublicDns\") | .OutputValue"

echo $jq_select

MYSQL_HOST=$(aws cloudformation describe-stacks | \
          jq --raw-output "$jq_select")

echo '==========> Creating sample database'
mysql -h $MYSQL_HOST --user=demouser --password=demopass < ./sql/demo.sql
echo '==========> Sample DB created'

echo '==========> Running demo docker container'
docker run -d -p 1414:1414 -e LICENSE=accept -e MQ_QMGR_NAME=$QMGR_NAME -e AWS_REGION=$AWS_REGION -v /var/lambdademo:/var/mqm --name demo mqlambdatm-demo

# Loop until "dspmq" says the queue manager is running
echo '==========> Waiting for queue manager to come up'
until [ "`state`" == "RUNNING" ]; do
    sleep 1
done
echo '==========> Queue manager running'

echo '==========> Putting sample messages to the triggered queue'
docker exec -i demo /opt/mqm/samp/bin/amqsput $TESTQ < ./sample-data/orders.txt

echo '==========> Waiting for the queue to deplete'
until [ `curdepth` == 0 ]; do
    sleep 5
done

echo "==========> Row counts from sample DB:"
mysql -h $MYSQL_HOST --user=demouser --password=demopass mqlambdademo <<EOF
select count(*) as 'orders count' from orders;
select count(*) as 'orderdetails count' from orderdetails;
EOF

echo "==========> mqlambdatm demo successfully completed"
