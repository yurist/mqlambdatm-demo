# Running demo

You need to [install and configure AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html). Additionally, you need:

- JSON command line processor `jq`
- Apache Maven
- git

Depending on your distribution: 

    sudo apt-get install jq maven git

or

    sudo yum install jq maven git

You also need to download IBM MQ redistributable client for Java. Follow [this link](https://www-945.ibm.com/support/fixcentral/swg/selectFixes?parent=ibm~WebSphere&product=ibm/WebSphere/WebSphere+MQ&release=9.0.1&platform=Linux+64-bit,x86_64&function=all), scroll down and click on the link above "IBM MQ JMS and Java redistributable client". You will need IBMid and password to proceed, you you don't have one you need to create one from this page.

After downloading, unzip the file into any directory.

Then:

    git clone https://github.com/yurist/mqlambdatm-demo.git
    cd mqlambdatm-demo/demo
    ./demo.sh --mq-jar-dir <mq jar unzip directory>/java/lib --key-pair <your existing key pair> --deployment-bucket <existing S3 bucket>
    

    
   

