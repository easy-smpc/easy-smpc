REM ==================================
REM	Create an EasySMPC installer for Windows
REM Usually only the version variable needs to be adapted
REM Before executing the batch file the WIX toolset needs to be obtained and installed from https://wixtoolset.org/
REM ==================================
set version="0.8.0"
set buildPath="..\target"
set mainJar="EasySMPC.jar"
set mainClass=org.bihealth.mi.easysmpc.App
set applicationName=EasySMPC
set descriptionText="Tool to add sums in a secure manner"
set vendor="Medical Informatics Group@Berlin Institute of Health and University of Technical University of Darmstadt"
set copyright="Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0"
REM add file associations
set licenseFile="..\LICENSE"
REM create a desktop shortcut?
REM add win-menu-group

jpackage --input %buildPath%^
		 --main-jar %mainJar%^
		 --main-class %mainClass%^
		 --type exe^
		 --dest %buildPath%^
		 --name %applicationName%^
		 --app-version %version%^
		 --description %descriptionText%^
		 --vendor %VENDOR%^
		 --copyright %copyright%^
		 --license-file %licenseFile%^
		 --win-dir-chooser^
		 --win-menu^
		 --win-per-user-install^