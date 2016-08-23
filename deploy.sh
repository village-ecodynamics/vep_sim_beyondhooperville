#!/bin/bash
## deploy

if [ -z $1 ]
  then
    echo "You must provie a message for the commit!" 1>&2
	exit 1
fi

rm -r ./docs
mkdir ./docs
doxygen Doxyfile

git add .
git commit -m "$1"
git push