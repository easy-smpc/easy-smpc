# EasySMPC - No-Code Secure Multi-Party Computation

> EasySMPC is an app for securely summing up distributed confidential data 
> using Secure Multi-Party Computation (SMPC). It is designed to enable 
> simple statistical analysis with maximum usability, easy access and a 
> strict no-coding approach. Your parents should be able to use this and 
> so should your physician!

---

### [Prerequisites](#prerequisites) ⚫ [Installation](#installation) ⚫ [Features](#features) ⚫ [Screenshot](#screenshot) ⚫ [Quick start guide](#quick-start-guide) ⚫ [Command line version](#command-line-version) ⚫ [Contact](#contact) ⚫ [License](#license) ⚫ [Acknowledgements](#acknowledgments)

---

## Prerequisites

EasySMPC requires [Java](https://adoptopenjdk.net/), at least in version 14. The Java
runtime is bundled in our Installer package.
Moreover, to use EasySMPC in automated mode, an e-mail account is required, which is accessible via SMTP and IMAP from the system executing EasySMPC.

To compile the app from source in addition to the Java JDK the [Maven build
system](https://maven.apache.org/) is required.

## Installation

EasySMPC does not need an installation and can be used as a Java jar package.
However, to increase portability we packaged the nessecary Java runtime with our
application in an installer to build an executable for Linux, Windows and Mac
OSX. This installer does not need any administrator priviledges and should be
run as a user. The installers for Windows and MacOS are not signed.
Thus a respective message while installing must be confirmed.

### Get binary installer

Check out [our releases
page](https://github.com/prasser/easy-smpc/releases) for Windows,
Linux and MacOS executables.

### Build from Source

To build the executable yourself, please clone this repository and build with
maven  (```mvn package```). The assembled executable should be in the target
directory. At present time some tests occasionaly fail. We're looking into that.
Until those tests are passing please compile with ```mvn package -DskipTests```.

To build the installer please build the jar package as described above and then
use the supplied scripts for your target platform. E.g.:
```
cd installer && ./linux.sh
```

## Features

EasySMPC was built to allow non-technical personell in medical research perform
simple analysis without sharing their input data. We tried to achieve a very low
threshold of technical prerequisites by using email as an, in most cases,
already established and configured communication medium.

* Easy to use
* Communication using established channels, e.g. emails
* Microsoft Excel and CSV import and export
* Automation of the protocol using IMAP-Mailboxes
* Automatic Proxy-Detection

### Security

EasySMPC uses Arithmetic Secret Sharing
[\[DZS15\]](https://www.ndss-symposium.org/ndss2015/ndss-2015-programme/aby---framework-efficient-mixed-protocol-secure-two-party-computation/),
the arithmetic extension of the GMW-Protocol
[\[GMW87\]](https://dl.acm.org/doi/10.1145/28395.28420) to achieve the private
computation of the sums. It uses a ring of size ![Ring size formula](http://latex.codecogs.com/gif.latex?p%3D2%5E%7B127%7D-1), the 12th Mersenne
prime.

### We are working on

  - Differential Privacy
  - Use EasySMPC with Slack/Mattermost/IRC/...
  - Examples and Getting Started guides
  - Further Documentation

## Screenshot

![Screenshot](doc/screenshot.png)

## Quick start guide

1. As a study initiator, click on _Create new project_ and enter the names of all participants, their e-mail addresses, the variables to sum up and your own confidential data. For performing the computation, you can choose between an automatic or a manual mode.

     a)	In _manual mode_ the users need to exchange all messages by sending and receiving e-mails manually.
     
     b) In _automatic mode_ participants receive and import the initial message manually into EasySMPC (see 2). All further messages are exchanged automatically via their respective mailboxes. If the study initiator chooses automatic mode, all participants also have to choose automated mode. The user name and connection details provided for the automated mode will be saved for future use, the password will not be saved and must be re-entered if the study was closed in between.
2. As a participant, you copy the message you received via email into the clipboard, click on _Participate in project_ in EasySMPC and paste the content. You will now see the study definition and can enter your own confidential data and mail box details if applicable.
3. As an initiator or participant, you now click on proceed. If running in automated mode, EasySMPC will automatically perform all steps until the final result is displayed. If running in manual mode, all users need to send and receive e-mails prepared by EasySMPC to perform the computation.
4. The final perspective shows the result of the secure addition of all variables.

## Command line version
There is also a command line version of EasySMPC. After [building](#build-from-source) or downloading from [our releases
page](https://github.com/prasser/easy-smpc/releases) use the jar easy-smpc-cli-*{Version}*.jar either as a creator or a participant. The command line version only supports the _automatic mode_.

### Creator
Execute the program with `java -jar easy-smpc-cli-*{Version}*.jar -create -l STUDY_NAME -b FILES_PATH_VARIABLES -d FILES_PATH_DATA -f PARTICIPANTS -a EMAIL_ADDRESS -p PASSWORD -i IMAP_HOST -x IMAP_PORT -y IMAP_ENCRYPTION -s SMTP_HOST -z SMTP_PORT -q SMTP_ENCRYPTION`. The parameters mean to the following:
1. `-create`: Indicates the creation of a new EasySMPC process.
2. `-l STUDY_NAME`: Name/title of the study. Muste be same same for the creator and all participants.
3. `-b FILE_PATH_VARIABLES`: The path to the Excel or CSV-files storing the variable names in the format *firstFile.xlsx,secondFile.csv,...*. The data needs to be row-oriented and must have at least one column. In the case of more than one column, EasySMPC will concatenate all columns of a row into single column and use this as the name of the variable. The variable names are shared with all participants.
4. `-d FILE_PATH_DATA`: The path to the Excel or CSV-files storing the creator's data in the format *firstFile.xlsx,secondFile.csv,...*. The data needs to be row-oriented and must have at least two columns. The last column is regarded as the data value and therefore must always contain numbers only. One dot a as a decimal separator is allowed but not necessary. In case of exact two columns, the first column will be regarded as the sole name. In the case of more than two columns, EasySMPC will concatenate all columns of a row but the last column to a single column and match this name with the variable names defined with `-b`. Variable names for which no value can be found will be set to zero.
5. `-f FILE_PATH_PARTICIPANTS`: The names and e-mail addresses of the participants in the form *name1,emailAddress1;name2,emailAddress2;name3,emailAddress3...*. The first name and e-mail address will be the creator. The e-mail addresses in this parameter will always be the e-mail addresses used for receiving (see parameters `-a` and `-v`). 
6. `-a EMAIL_ADDRESS`: E-mail address to be used. If the parameter `-v` is set, this parameter will only be used for the receiving mail address.
7. `-m PASSWORD`: Password of the e-mail address used. If the parameter `-v` is set, this parameter will be used as password for the receiving mail address.
8. `-i IMAP_HOST`: Hostname of the IMAP server.
9. `-x IMAP_PORT`: Port of the IMAP server.
10. `-y IMAP_ENCRYPTION`: IMAP server uses SSL/TLS or Starttls. Use either SSLTLS or STARTTLS.
11. `-s SMTP_HOST`: Hostname of the SMTP server.
12. `-z SMTP_PORT`: Port of the SMTP server.
13. `-q SMTP_ENCRYPTION`: SMTP server uses SSL/TLS or Starttls. Use either SSLTLS or STARTTLS.
 
After executing check the result in the file `result_<study name>_<timestamp>.xlsx` or check the file easy-smpc.log for details of errors.

Please note that in addition to the parameters mentioned above the following *optional* parameters exists:
1. `-h`: Pass this parameter if the data in the data and variables files are oriented horizontally.
1. `-e`: Pass this parameter if the data in the data and variables files have headers, which need to be skipped.
1. `-j N_COLUMNS_TO_SKIP`: Pass this parameter to skip the first n columns.
1. `-v EMAIL_ADDRESS_SENDING`: Pass this parameter if the e-mail address used to send the e-mails is supposed to differ from the receiving e-mail address.
1. `-p PASSWORD_SENDING`: Pass this as the password for the receiving e-mail address if the parameter `-v` is set.
1. `-n LOGON_NAME_RECEIVING`: Pass this parameter if the logon name to the receiving e-mail servers deviates from the e-mail address used (e.g. the logon name is `name` and not `name@domain.org`). The receiving e-mail address still needs to be passed.
1. `-w LOGON_NAME_SENDING`: The same as parameter ´-n´ for the sending e-mail address. The sending e-mail address still needs to be passed. The logon name is not copied from the parameter ´-n´. Thus, if the same logon name is used for receiving and sending the parameters ´-n´ and ´-w´ need both to be set.
1. `-t AUTH_MECHANISMS_RECEIVING`: Pass this parameter to set the IMAP auth mechanisms of the receiving e-mail account. For details, we refer to the property `mail.imap.auth.mechanisms` in the [Jakarta e-mail documentation](https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/imap/package-summary.html).
1. `-u AUTH_MECHANISMS_SENDING`: Pass this parameter to set the SMTP auth mechanisms of the sending e-mail account. For details, we refer to the property `mail.smtp.auth.mechanisms` in the [Jakarta e-mail documentation](https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/smtp/package-summary.html).

### Participant
Execute the program with `java -jar easy-smpc-cli-*{Version}*.jar -participate -l STUDY_NAME -d FILE_PATH_DATA -o PARTICIPANT_NAME -a EMAIL_ADDRESS -p PASSWORD -i IMAP_HOST -x IMAP_PORT -y IMAP_ENCRYPTION -s SMTP_HOST -z SMTP_PORT -q SMTP_ENCRYPTION`. Most parameters are explained in the section [Creator](#creator), other parameters are described below:
1. `-participate`: Indicates the participation in a new EasySMPC process.
3. `-o PARTICIPANT_NAME`: Name of the participant as defined in the participants files by the creator.
4. `-d FILE_PATH_DATA`: The data of the participant to add. The format is the same as for the [Creator](#creator).

After executing check the result in the file `result_<study name>_<timestamp>.xlsx` or check the file easy-smpc.log for details of errors.

### Sample 
Data for a sample can be found in the folder `sample-cli`. A sample process with this data might can be started with these three commands: 
 1. `java -jar easy-smpc-cli-*{Version}*.jar -create -b "-create -l "sample Study" -b ./sample/variables.xlsx -d "./sample/PKU comorbidities.xlsx" -f "Creator,easysmpc.dev0@gmail.com;Participant1,easysmpc.dev1@gmail.com;Participant2,easysmpc.dev2@gmail.com" -a easysmpc.dev0@gmail.com -p thePassword -i imap.gmail.com -x 993 -y SSLTLS -s smtp.gmail.com -z 465 -q SSLTLS`
2. `java -jar easy-smpc-cli-*{Version}*.jar -participate -l "sample Study" -d "./sample/PKU comorbidities.xlsx" -o Participant1 -a easysmpc.dev1@gmail.com -p thePassword-i imap.ionos.de -x 993 -y SSLTLS -s smtp.ionos.de -z 465 -q SSLTLS5`
3. `java -jar easy-smpc-cli-*{Version}*.jar -participate -l "sample Study" -d "./sample/PKU comorbidities.xlsx" -o Participant2 -a easysmpc.dev2@gmail.com -p &r6=Jbh9 -i imap.ionos.de -x 993 -y SSLTLS -s smtp.ionos.de -z 465 -q SSLTLS`

The single commands are expected to start on different folders or better different computers. If they start on one computer, errors of writing log and result files can happen. Also in this minimal test the same data file `PKU comorbidities.xlsx` is used for each party. However, in a real-world usage each participant would use different data in the file

### Troubleshooting
#### Neither an error nor a result
Should the program wit for an unreasonable time without throwing an error, first check whether EasySMPC-related e-mails are in a spam folder (the title of the e-mails start with [EasySMPC]). If so just copy them into the regular inbox.
If nothing can be found in the spam folder, it is likely that the different programs are using different EasySMPC studies with the same name. To solve the issues either (1) delete all e-mails in all mailboxes starting with [EasySMPC] in the title or (2) restart the process with a new name for all participants as well as the creator.

#### Command line version: Error writing the result into an Excel file
When executing the command line version on Linux systems the following entries can appear in the log:
````
2022-01-01 12:00:00.000 INFO Start calculating and writing result
Exception in thread "Thread-1" java.lang.InternalError: java.lang.reflect.InvocationTargetException
        at java.desktop/sun.font.FontManagerFactory$1.run(FontManagerFactory.java:86)
        at java.base/java.security.AccessController.doPrivileged(AccessController.java:312)
        at java.desktop/sun.font.FontManagerFactory.getInstance(FontManagerFactory.java:74)
        at java.desktop/java.awt.Font.getFont2D(Font.java:497)
        at java.desktop/java.awt.Font.canDisplayUpTo(Font.java:2244)
        at java.desktop/java.awt.font.TextLayout.singleFont(TextLayout.java:469)
        at java.desktop/java.awt.font.TextLayout.<init>(TextLayout.java:530)
        at org.apache.poi.ss.util.SheetUtil.getDefaultCharWidth(SheetUtil.java:273)
        at org.apache.poi.ss.util.SheetUtil.getColumnWidth(SheetUtil.java:248)
        at org.apache.poi.ss.util.SheetUtil.getColumnWidth(SheetUtil.java:233)
        at org.apache.poi.xssf.usermodel.XSSFSheet.autoSizeColumn(XSSFSheet.java:555)
        at org.apache.poi.xssf.usermodel.XSSFSheet.autoSizeColumn(XSSFSheet.java:537)
        at org.bihealth.mi.easysmpc.dataexport.ExportExcel.exportData(ExportExcel.java:70)
        at org.bihealth.mi.easysmpc.cli.User.exportResult(User.java:414)
        at org.bihealth.mi.easysmpc.cli.User.performCommonSteps(User.java:382)
        at org.bihealth.mi.easysmpc.cli.UserCreating$1.run(UserCreating.java:101)
        at java.base/java.lang.Thread.run(Thread.java:832)
        ...
````
To resolve this please install the package `libfontconfig1` (see e.g. also [here](https://bz.apache.org/bugzilla/show_bug.cgi?id=62576))

## Contact

If you have questions or problems, we would like to invite you to
[open an issue at
Github](https://github.com/prasser/email-smpc-histogram/issues). This allows
other users to collaborate and (hopefully) answer your question in a timely
manner. If your request contains confidential information or is not suited for a
public issue, send us an email.

EasySMPC's core development team consists of:

* [Tobias Kussel](https://github.com/TKussel) - Cryptography - [tobias.kussel@cysec.de](tobias.kussel@cysec.de)
* [Armin Müller](https://github.com/burgadon) - Graphical user interface - [armin.mueller@charite.de](armin.mueller@charite.de)
* [Fabian Prasser](https://github.com/prasser) - Software design and architecture -[fabian.prasser@charite.de](fabian.prasser@charite.de)
* [Felix Nikolaus Wirth](https://github.com/fnwirth) - Graphical user interface - [felix-nikolaus.wirth@charite.de](felix-nikolaus.wirth@charite.de)

## License

This software is licensed under the Apache License 2.0. The full text is
accessible in the [LICENSE file](LICENSE).

EasySMPC uses the following dependencies:

 - [FlatLaf](https://github.com/JFormDesigner/FlatLaf) - Apache License 2.0
 - [Apache HTTPClient](https://hc.apache.org/httpcomponents-client-5.0.x/) - Apache License 2.0
 - [HPPC: High Performance Primitive Collections](https://github.com/carrotsearch/hppc) - Apache License 2.0
 - [Apache POI-OOXML](http://poi.apache.org/components/oxml4j/) - Apache License 2.0
 - [Apache Commons CSV](http://commons.apache.org/proper/commons-csv/) - Apache License 2.0
 - [jUnit](https://github.com/junit-team/junit5) (Unit tests only) - Eclipse Public License v2.0
 - [Proxy Vole](https://github.com/akuhtz/proxy-vole) - Apache License 2.0
 - [Apache Logging](https://logging.apache.org/log4j) - Apache License 2.0
 - [LMAX Disruptor](https://github.com/LMAX-Exchange/disruptor) - Apache License 2.0


## Acknowledgments

This project is partly financed by the "Collaboration on Rare Diseases" of the
Medical Informatics Initiative, funded by the German Federal Ministry of
Education and Research (BMBF).
