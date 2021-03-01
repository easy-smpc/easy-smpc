# EasySMPC - No-Code Secure Sum

> EasySMPC is an app to securely sum up distributed confidential data using
> secure Multi-Party Computation. It is designed to allow easy statistical
> analysis with maximal usability, ease of access and a strict no-coding
> approach. Your parents should be able to use this, so your physician better
> should!

---

### [Prerequisites](#prerequisites) ⚫ [Installation](#installation) ⚫ [Getting Started](#getting-started) ⚫ [Features](#features) ⚫ [Examples](#examples) ⚫ [Contact](#contact) ⚫ [License](#license) ⚫ [Acknowledgements](#acknowledgments)

---

## Prerequisites

EasySMPC requires [Java](https://www.java.com), at least in version 13.

To compile the app from source in addition to the Java JDK the [Maven build
system](https://maven.apache.org/) is required.

## Installation

EasySMPC is a single-executable standalone application. It does not require any
installation. Just download or build the executable.

### Get Released Binary

Check out [our releases
page](https://github.com/prasser/email-smpc-histogram/releases) for Windows,
Linux and MacOS executables.

### Build from Source

To build the executable yourself, please clone this repository and build with
maven  (```mvn clean install```). The assembled executable should be in the target
directory.

## Getting Started

Example summing up different diagnoses.

## Features

Lots of stuff! Automatic mailbox detection! Secretshare all the things! 

EasySMPC uses Arithmetic Secret Sharing
[\[DZS15\]](https://www.ndss-symposium.org/ndss2015/ndss-2015-programme/aby---framework-efficient-mixed-protocol-secure-two-party-computation/),
the arithmetic extension of the GMW-Protocol
[\[GMW87\]](https://dl.acm.org/doi/10.1145/28395.28420) to achieve the private
computation of the sums. It uses a ring of size ![Ring size formula](http://latex.codecogs.com/gif.latex?p%3D2%5E%7B127%7D-1), the 12th mersenne
prime, and represents decimal values with a 32bit fractional part. That leaves
94 bit for the (signed) integral part. This ought to be sufficient for nearly
all real world applications (although these are famous last words).

## Examples

Nothing here, yet.

### Age Distribution of Singular Distribution

### Mean Age

## Contact

If you have questions or problems, we would like to invite you to
[open an issue at
Github](https://github.com/prasser/email-smpc-histogram/issues). This allows
other users to collaborate and (hopefully) answer your question in a timely
manner. If your request contains confidential information or is not suited for a
public issue, send us an email.

EasySMPC's core dev team consists of:

* [Tobias Kussel](https://github.com/TKussel) - Cryptography - [tobias.kussel@cysec.de](tobias.kussel@cysec.de)
* [Fabian Prasser](https://github.com/prasser) - Software design and architecture -[fabian.prasser@charite.de](fabian.prasser@charite.de)
* [Felix Nikolaus Wirth](https://github.com/fnwirth) - Graphical user interface - [felix-nikolaus.wirth@charite.de](felix-nikolaus.wirth@charite.de)

## Licence

This software is licensed under the Apache License 2.0. The full text is
accessible in the [LICENSE file](LICENSE).

The app uses the following dependencies:

 - [FlatLaf](https://github.com/JFormDesigner/FlatLaf) - Apache License 2.0
 - [Apache HTTPClient](https://hc.apache.org/httpcomponents-client-5.0.x/) - Apache License 2.0
 - [HPPC: High Performance Primitive Collections](https://github.com/carrotsearch/hppc) - Apache License 2.0
 - [Apache POI-OOXML](http://poi.apache.org/components/oxml4j/) - Apache License 2.0
 - [Apache Commons CSV](http://commons.apache.org/proper/commons-csv/) - Apache License 2.0
 - [jUnit](https://github.com/junit-team/junit5) (Unit tests only) - Eclipse Public License v2.0

## Acknowledgements

This project is partly financed by the "Collaboration on Rare Diseases" of the
Medical Informatics Initiative, funded by the German Federal Ministry of
Education and Research (BMBF).

