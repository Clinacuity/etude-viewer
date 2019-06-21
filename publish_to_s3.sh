#!/bin/bash

if [[ "$PUBLISH_S3" = "true" ]]
then
    if [ -f "$1" ]
    then
        aws s3 cp "$1" s3://clinacuity/public-downloads/
    else
        echo "The specified path is not a file"
        exit -1
    fi
fi
