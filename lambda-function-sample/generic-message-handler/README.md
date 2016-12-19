# Overview

This is a base class for IBM MQ message consumers using Lambda triggering. You can use this class as is or as a basis for your development.

If used as is it must be extended, implementing the following methods:

- `init`
- `process`
- `commit`
- `finish`

See `sample-mysql-message-processor` for a sample implementation.

Look at the [main class](https://github.com/yurist/mqlambdatm-demo/blob/master/lambda-function-sample/generic-message-handler/src/main/java/com/steinschreiber/aws/mqlambda/AbstractMessageProcessor.java) for details.
