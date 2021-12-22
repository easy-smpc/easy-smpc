#!/bin/bash
#==================================
#Create an EasySMPC installer for MacOS
#Usually only the version variable and the type (RPM or DEB) needs to be adapted
#==================================
VERSION="1.0.3"
TYPE="dmg"
BUILD_PATH="../target"
MAIN_JAR="easysmpc-1.0.3-generic.jar"
MAIN_CLASS="org.bihealth.mi.easysmpc.App"
APPLICATION_NAME="EasySMPC"
DESCRIPTION_TEXT="No-Code Approach to Secure Multi-Party Computation"
VENDOR="Berlin Institute of Health and Technical University of Darmstadt"
COPYRIGHT="Licensed under the Apache License, Version 2.0 (the License); you may not use this file except in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0"
ICONPATH="../src/main/resources/org/bihealth/mi/easysmpc/resources/icon.icns"
#add file associations?
LICENSE_FILE="../LICENSE"


PACKAGE_COMMAND="jpackage 	--input $BUILD_PATH\
							--main-jar $MAIN_JAR\
							--main-class $MAIN_CLASS\
							--type $TYPE\
							--dest $BUILD_PATH\
							--name $APPLICATION_NAME\
							--app-version $VERSION\
							--description \"$DESCRIPTION_TEXT\"\
							--vendor \"$VENDOR\"\
							--copyright \"$COPYRIGHT\"\
							--license-file $LICENSE_FILE\
							--mac-package-identifier $APPLICATION_NAME\
							--mac-package-name $APPLICATION_NAME
							--icon $ICONPATH"

eval $PACKAGE_COMMAND
