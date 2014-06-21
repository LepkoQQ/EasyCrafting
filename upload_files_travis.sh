#!/bin/bash
dir="./build/libs"

if [ -d "$dir" ]
then
   cd "$dir"
   curl --remote-name http://mods.lepko.net/archive/travis/easycrafting/buildinfo.txt
   echo "$TRAVIS_BUILD_NUMBER,$TRAVIS_COMMIT" >> buildinfo.txt
   find . -type f -exec curl --user $FTP_USER:$FTP_PASSWORD --ftp-create-dirs -T {} ftp://lepko.net/domains/lepko.net/public_html/mods/archive/travis/easycrafting/{} \;
else
   echo "Error: $dir not found."
fi