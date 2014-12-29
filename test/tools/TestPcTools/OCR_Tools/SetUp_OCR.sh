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
if [ -f tesseract-3.01.tar.gz ];then
		echo "has source code"
else
		echo "No source code"
		wget http://tesseract-ocr.googlecode.com/files/tesseract-3.01.tar.gz
fi
###################
if [ -f leptonica-1.68.tar.gz ];then
		echo "has leptonica source code"
else
		echo "No leptionica source code"
		wget http://www.leptonica.org/source/leptonica-1.68.tar.gz
fi
###################
if [ -f eng.traineddata.gz ];then
		echo "has eng.traineddata.gz"
else
		echo "No entrainedata.gz"
		wget http://tesseract-ocr.googlecode.com/files/eng.traineddata.gz
fi
###################
if [ -f tesseract-ocr-3.01.eng.tar.gz ];then
		echo "has tesseract-ocr"
else
		echo "No tesseract-ocr"
		wget http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.01.eng.tar.gz 
fi

# Step3
sudo apt-get install autoconf automake libtool
sudo apt-get install libpng12-dev
sudo apt-get install libjpeg62-dev
sudo apt-get install libtiff4-dev
sudo apt-get install zlib1g-dev
sudo apt-get install imagemagick
sudo apt-get install moreutils
#sudo apt-get install libleptonica-dev
sudo apt-get install ffmpeg  # use to extract frame from Camera_Video

# Step4
cd $NativeDir
tar zxvf leptonica-1.68.tar.gz
cd leptonica-1.68
./configure
make 
sudo make install
sudo ldconfig

# Step5
cd $NativeDir
gzip -d eng.traineddata.gz
tar zxvf tesseract-3.01.tar.gz
tar zxvf tesseract-ocr-3.01.eng.tar.gz
cp $NativeDir/tesseract-ocr/tessdata/* $NativeDir/tesseract-3.01/tessdata/
cp $NativeDir/eng.traineddata $NativeDir/tesseract-3.01/tessdata/

cd tesseract-3.01
./autogen.sh
./configure
make
sudo make install
sudo ldconfig

sudo cp $NativeDir/eng.traineddata /usr/local/share/tessdata/

# tesseract CC.tif CCC

