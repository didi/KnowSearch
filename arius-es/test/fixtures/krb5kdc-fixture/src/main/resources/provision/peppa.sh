#!/bin/bash

set -e

addprinc.sh elasticsearch
addprinc.sh HTTP/localhost
addprinc.sh peppa
addprinc.sh george          dino

# Use this as a signal that setup is complete
python3 -m http.server 4444 &

sleep infinity