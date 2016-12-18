#! /bin/bash 

set -e

STACK_NAME="mqlambdatm-demo-stack"
CODE_ARCHIVE_NAME="sample-mysql-message-processor-1.0-SNAPSHOT.jar"
CODE_ARCHIVE_DIR="../lambda-function-sample/sample-mysql-message-processor/target/"

usage() {
    echo "usage:"
    echo "demo.sh [options]"
    echo
    echo "options:"
    echo "  --key-pair KEY_PAIR       -- existing EC2 keyp pair - mandatory"
    echo "  --deployment-bucket       -- existing S3 bucket for code uploading - mandatory"
    echo "  --stack-name              -- stack to create, default mqlambdatm-demo-stack"
}

while [[ $# > 1 ]]
do
    case $1 in
        --key-pair)
            KEY_PAIR="$2"
            shift
            ;;
        --deployment-bucket)
            DEPLOYMENT_BUCKET="$2"
            shift
            ;;
        --stack-name)
            STACK_NAME="$2"
            shift
            ;;
        *)
            usage
            exit
            ;;
    esac
    shift
done

if [[ "$KEY_PAIR" == "" || "$DEPLOYMENT_BUCKET" == "" || $# > 0 ]]
then
    usage
    exit
fi

echo "=======> Building sample Lambda message consumer..."
pushd ../lambda-function-sample
mvn clean package
popd

echo "=======> Uploading jar to S3..."
aws s3 cp $CODE_ARCHIVE_DIR$CODE_ARCHIVE_NAME s3://$DEPLOYMENT_BUCKET/$CODE_ARCHIVE_NAME

echo "=======> Creating CloudFormation stack..."
out=$(aws cloudformation create-stack --stack-name $STACK_NAME --capabilities CAPABILITY_IAM \
               --template-body file://cfn/demo.yaml \
               --parameters ParameterKey=KeyPair,ParameterValue=$KEY_PAIR \
                            ParameterKey=S3Bucket,ParameterValue=$DEPLOYMENT_BUCKET \
                            ParameterKey=S3Key,ParameterValue=$CODE_ARCHIVE_NAME)

STACK_ID=$(echo "$out" | jq --raw-output .StackId)

echo "=======> Waiting for stack creation to complete, this will take awhile..."
if aws cloudformation wait stack-create-complete --stack-name $STACK_ID;
then
    echo "=======> Stack successfully created"
else
    echo "=======> Stack creation failure"
fi
            
jq_select_ec2_host=".Stacks[] | select(.StackId == \"$STACK_ID\") | .Outputs[] | select(.OutputKey == \"MqInstancePublicDns\") | .OutputValue"

stacks=$(aws cloudformation describe-stacks)

EC2_HOST=$(echo $stacks | jq --raw-output "$jq_select_ec2_host")

echo "You can check demo log by running"
echo "ssh -t -i <your-key-pair-pem-file-location> ec2-user@"$EC2_HOST "tail /var/log/cloud-init-output.log"
