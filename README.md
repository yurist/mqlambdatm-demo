# Overview

This is a sample implementation of [IBM MQ trigger monitor for AWS Lambda](https://github.com/yurist/mqlambdatm), together with with configuration, scripts and data to run a demo. It includes the following components:

- [Lambda function sample](https://github.com/yurist/mqlambdatm-demo/tree/master/lambda-function-sample) - A Java project implementing AWS Lambda function, triggered when messages arrive on a specially configured MQ queue. The Lambda function parses the messages and inserts corresponding records into a MySQL database. The project consists of two modules: 
- [Generic message handler](https://github.com/yurist/mqlambdatm-demo/tree/master/lambda-function-sample/generic-message-handler) - can be used as is for any MQ triggered Lambda. It implements all MQ-related queue and message manipulation logic. It provides an abstract class that needs to be extended for any application-specific message processing functionality such as database manipulation, SQS or Kinesis message propagation etc.
- [Sample message processor](https://github.com/yurist/mqlambdatm-demo/tree/master/lambda-function-sample/sample-mysql-message-processor) - a sample usage of generic message handler that parses input messages and inserts data into a MySQL database.
- [Demo](https://github.com/yurist/mqlambdatm-demo/tree/master/demo) - subcomponents to prepare and run a demo of the sample. It includes a script that builds the sample project, uploads it to AWS S3, and then creates a CloudFormation stack with all the necessary resources to run the demo.

# Demo description

The demo includes a CloudFormation template that creates all the resources necessary for running the sample end to end:

- An EC2 instance that hosts MQ Queue Manager along with MQ Lambda Trigger Monitor
- A fully configured Lambda function, triggered by MQ Lamdba Trigger Monitor, pulls messages from the triggered queue and writes records to a sample database
- An RDS MariaDB instance with a sample database
- All auxiliary resources, including a VPC instance, subnets, security groups, internet gateway and IAM roles.

The EC2 instance is configured with a cloud-init script, which runs the sample:

- Builds a Docker image with base IBM MQ developer server. The image is pulled from [IBM MQ Docker repository](https://github.com/ibm-messaging/mq-docker)
- Pulls `mqlambdatm` release
- Builds a custom image based on mq-docker, which includes MQ trigger configuration and `mqlambdatm` executable
- Creates an empy sample database
- Runs Docker container with MQ Queue Manager and `mqlambdatm` as a Queue Manager-controlled service
- Executes MQ sample `amqsput` to populate the triggered queue with sample data
- Waits for the triggered queue to become empty as a result of Lambda function processing all messages
- Displays the row counts for sample tables

# Running demo

You need to [install and configure AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html). Make sure the IAM credentials you configure with AWS CLI can create CloudFormation stacks with EC2, RDS, Lamda and IAM resources. Additionally, you need:

- JSON command line processor `jq`
- Apache Maven
- git
- unzip

Depending on your distribution: 

    sudo apt-get install jq maven git unzip

or

    sudo yum install jq maven git unzip

You need IBM MQ Java client library, com.ibm.mq.allclient.jar, If you have IBM MQ with Java option installed, it is in the directory `<MQ_INSTALLATION_PATH>/java/lib`, by default `/opt/mqm/java/lib`. If you don't have MQ installed on your computer, then you need to download IBM MQ redistributable client for Java. Follow [this link](https://www-945.ibm.com/support/fixcentral/swg/selectFixes?parent=ibm~WebSphere&product=ibm/WebSphere/WebSphere+MQ&release=9.0.1&platform=Linux+64-bit,x86_64&function=all), scroll down and click on the link above "IBM MQ JMS and Java redistributable client". You will need IBMid and password to proceed, if you don't have one you need to create it from this page.

After downloading, unzip the file into any directory.

Then:

    git clone https://github.com/yurist/mqlambdatm-demo.git
    cd mqlambdatm-demo/demo
    ./demo.sh --mq-jar-dir <absolute path to mq java client jar directory> --key-pair <your existing key pair> --deployment-bucket <existing S3 bucket>
    
The scripts reports its progress while executing. The last step, waiting for the stack creation to complete, takes between 5 to 10 minutes. Most of this time is spent initializing the RDS instance. The demo is optimized to run the IBM MQ installation simultaneously with RDS instance creation, but it still takes a while to complete.

You can `ssh` to the EC2 instance after the script finishes to check the cloud-init logs at `/var/log/cloud-init-output.log` and see the demo progress.

You can remove all demo resources by deleting the stack created by the script. The script displays the relevant instructions.

# Use of IBM software

The demo downloads and installs IBM MQ Advanced for Developers. By running the demo, you accept [IBM License agreement](http://www14.software.ibm.com/cgi-bin/weblap/lap.pl?popup=Y&li_formnum=L-APIG-A4FHQ9)

# License

[Apache License, Version 2.0](http://apache.org/licenses/LICENSE-2.0.html)
    

    
   

