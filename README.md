# PAC4J Extensions

PAC4J Extensions is a set of extensions to the [PAC4J](http://www.pac4j.org) library developed internally by [IDC](http://www.idc.com) and published as open source.

It provides the following modules:
- **Database configuration of SAML clients** - This module allows you to configure a set of SAML2 clients using a relational database, such as Oracle DB. You need not change your PAC4J static configuration (e.g. a Spring XML file) to make configuration changes to the application. You just add a new row to a database table or modify an existing row and then restart your application. You can also implement a reload mechanism that will allow you to make configuration changes even without restarting the application.


## Building

You need [Apache Maven](http://maven.apache.org) to build the project. The project also requires Java 1.8, so be sure to have JDK 1.8 installed.

First, you need to check out the code from GitHub:

```shell
git clone git@github.com:jkacer/pac4j-extensions.git
```

To build the project:

```shell
cd pac4j-extensions
mvn clean package
```

Then grab JAR files from `target` directories of individual modules.

You can also install all modules into your local Maven repository:

```shell
cd pac4j-extensions
mvn clean install
```

## Build Status

Maven artifacts are built via Travis:
[![Build Status](https://travis-ci.org/jkacer/pac4j-extensions.png?branch=master)](https://travis-ci.org/jkacer/pac4j-extensions).

The latest released version version is:
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.idc.webchannel.pac4j/extensions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.idc.webchannel.pac4j/extensions),
available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Ccom.idc.webchannel.pac4j)

## License

This code is released under the Apache 2.0 License.
Please see file LICENSE for more details.
