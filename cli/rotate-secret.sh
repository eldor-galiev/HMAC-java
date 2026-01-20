#!/bin/bash

NEW_SECRET=$(openssl rand -base64 32)

cat config.json | sed "s|\"secret\": \".*\"|\"secret\": \"$NEW_SECRET\"|" > config.json.tmp
mv config.json.tmp config.json