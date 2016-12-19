# Running demo

You need to [install and configure AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html). Additionally, you need:

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
    ./demo.sh --mq-jar-dir <mq java client jar directory>/java/lib --key-pair <your existing key pair> --deployment-bucket <existing S3 bucket>
    

    
   

