#!/bin/bash
echo "GOOGLE_CLIENT_ID=$1" >> ./local.properties
echo "DROPBOX_APP_KEY=$2" >> ./local.properties
echo "MICROSOFT_CLIENT_ID=$3" >> ./local.properties
echo "MICROSOFT_SIGNATURE_HASH=$4" >> ./local.properties

# Creating a new file form ms_auth_config.json
ls -al
echo "{}" > ./apps/storage-sample/src/main/res/raw/ms_auth_config.json