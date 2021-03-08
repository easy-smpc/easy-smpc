# EasySMPC - No-Code Secure Multi-Party Computation

> EasySMPC is an app for securely summing up distributed confidential data 
> using Secure Multi-Party Computation (SMPC). It is designed to enable 
> simple statistical analysis with maximum usability, easy access and a 
> strict no-coding approach. Your parents should be able to use this and 
> so should your physician!

---

### [Prerequisites](#prerequisites) ⚫ [Installation](#installation) ⚫ [Features](#features) ⚫ [Screenshot](#screenshot) ⚫ [Contact](#contact) ⚫ [License](#license) ⚫ [Acknowledgements](#acknowledgments)

---

## Prerequisites

EasySMPC requires [Java](https://www.java.com), at least in version 13. The Java
runtime is bundled in our Installer package.

To compile the app from source in addition to the Java JDK the [Maven build
system](https://maven.apache.org/) is required.

## Installation

EasySMPC does not need an installation and can be used as a Java jar package.
However, to increase portability we packaged the nessecary Java runtime with our
application in an installer to build an executable for Linux, Windows and Mac
OSX. This installer does not need any administrator priviledges and should be
run as a user.

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
* Excel and CSV import and export
* Automation of the protocol using a shared IMAP-Mailbox
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
  - Automate email communication with private mailboxes
  - Support for decimal numbers
  - Use EasySMPC with Slack/Mattermost/IRC/...
  - Examples and Getting Started guides
  - Further Documentation

## Screenshot

![Screenshot](doc/screenshot.png)

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


## Acknowledgements

This project is partly financed by the "Collaboration on Rare Diseases" of the
Medical Informatics Initiative, funded by the German Federal Ministry of
Education and Research (BMBF).
