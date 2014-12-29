#
# (C) Copyright 2011-2012 Compal Electronics, Inc. 
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
#
#Author: Using_Lin@compal.com
#Reference from http://code.google.com/p/tessseract-ocr/wiki/ReadMe
#Reference from http://www.leptonica.org/source/README.html

# Step1
NativeFile=$(readlink -f $0)
NativeDir=$(dirname $NativeFile)
echo $NativeDir

# Step2
sudo apt-get install mtp-tools mtpfs
