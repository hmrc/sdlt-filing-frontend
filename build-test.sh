#!/bin/bash

rm -rf calc-assets/node_modules
set +x
. $NVM_DIR/nvm.sh
nvm use 4.4.5
sbt -mem 3000 clean validate test it:test dist-tgz