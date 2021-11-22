REM ==================================
REM	Create an installer for Windows
REM Usually only the version variable needs to be adapted
REM Before executing the batch file the WIX toolset needs to be obtained and installed from https://wixtoolset.org/
REM ==================================
@SET version="1.0.0"
@SET buildPath="..\target"
@SET mainJar="easy-smpc-1.0.0.jar"
@SET mainClass=org.bihealth.mi.easysmpc.App
@SET applicationName=EasySMPC
@SET descriptionText="No-Code Approach to Secure Multi-Party Computation"
@SET vendor="Berlin Institute of Health and Technical University of Darmstadt"
@SET copyright="Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0"
@SET iconPath="..\src\main\resources\org\bihealth\mi\easysmpc\resources\icon.ico"
@SET licenseFile="..\LICENSE" 
REM add file associations?
REM add win-menu-group?

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
		 --icon %iconPath%^
		 --win-shortcut