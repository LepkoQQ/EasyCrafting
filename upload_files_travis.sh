#!/bin/bash
dir="./build/libs"

if [ -d "$dir" ]
then
   cd "$dir"
   THEFILE=$(ls -t *.jar | head -n 1)
   THEFILE=${THEFILE/.jar/.commit}
   echo $TRAVIS_COMMIT > $THEFILE
   find . -type f -exec curl --user $FTP_USER:$FTP_PASSWORD --ftp-create-dirs -T {} ftp://lepko.net/domains/lepko.net/public_html/mods/archive/travis/easycrafting/{} \;
else
   echo "Error: $dir not found."
fi