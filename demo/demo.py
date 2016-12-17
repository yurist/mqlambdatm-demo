import subprocess
from subprocess import Popen
import sys
import boto3
import argparse

codeArchiveName = 'sample-mysql-message-processor-1.0-SNAPSHOT.jar'
codeArchiveDir = '../message-processors/sample-mysql-message-processor/target/'


def build():
    print ("Building sample lambda message consumer...")

    if Popen.wait(subprocess.Popen(["mvn", "clean", "package"], cwd = "../message-processors")):
        error("mvn failed")

def main():
    
    args = get_args()

    build()

    s3 = boto3.resource('s3')

    print ("Uploading jar...")

    s3.meta.client.upload_file(
        codeArchiveDir + codeArchiveName,
        args.deployment_bucket,
        codeArchiveName)

    cf = boto3.client('cloudformation')

    with open('cfn/demo.yaml', 'r') as t:
        template = t.read()

    print ("Creating stack...")

    response = cf.create_stack(
       StackName = args.stack_name,
       TemplateBody = template,
       DisableRollback = True, 
       Capabilities = ['CAPABILITY_IAM'],
       Parameters = [
         {
             'ParameterKey': 'KeyPair',
             'ParameterValue': args.key_pair
         },
         {
             'ParameterKey': 'S3Bucket',
             'ParameterValue': args.deployment_bucket
         },
         {
             'ParameterKey': 'S3Key',
             'ParameterValue': codeArchiveName
         }
       ])

    print ("Waiting for stack create complete, this may take a few minutes...")
    cf.get_waiter('stack_create_complete').wait(StackName = stackName)
    print("Done")

def get_args():
    
    p = argparse.ArgumentParser()
    p.add_argument('--key-pair', help = 'Existing keypair name to be used for SSH instance access')
    p.add_argument('--deployment-bucket', help = 'Existing S3 bucket. Will be used to upload jar files for lambda deployment')
    p.add_argument('--stack-name', default = 'MQLambdaDemoStack', help = 'The stack name to create')
    return p.parse_args()

def error(msg):
    print msg
    sys.exit(1)

if __name__ == '__main__':
    main()
