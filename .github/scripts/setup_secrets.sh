#!/bin/bash
echo "GOOGLE_CLIENT_ID=$1" >> ./local.properties
echo "DROPBOX_APP_KEY=$2" >> ./local.properties
echo "MICROSOFT_CLIENT_ID=$3" >> ./local.properties
echo "MICROSOFT_SIGNATURE_HASH=$4" >> ./local.properties
