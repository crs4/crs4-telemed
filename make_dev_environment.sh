#!/bin/bash

echo "clone libs"
git clone https://github.com/crs4/most libs/most -b develop
git clone https://github.com/crs4/most-streaming libs/most-streaming -b develop
git clone https://github.com/crs4/most-voip libs/most-voip -b develop

echo "link libs"
cd server/most/web
ln -s ../../../../libs/most/src/most/web/utils utils
ln -s ../../../../libs/most/src/most/web/users users
ln -s ../../../../libs/most/src/most/web/authentication authentication

