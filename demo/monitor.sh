#! /bin/bash

get() {
    msg=$(aws sqs receive-message --queue-url $SQS_QUEUE_URL --wait-time-seconds 20)
    if [[ "$msg" != "" ]]; then
        receipt_handle=$(echo "$msg" | jq --raw-output .Messages[0].ReceiptHandle)
        aws sqs delete-message --queue-url $SQS_QUEUE_URL --receipt-handle $receipt_handle
        echo "$msg" | jq --raw-output .Messages[0].Body
    else
        echo ""
    fi
}

msg=`get`

until [[ "$msg" == "" || "$msg" == "<ok>" || "$msg" == "<error>" ]]; do
    echo "$msg"
    msg=`get`
done

echo "$msg"
