# PAC4J Extensions

PAC4J Extensions is a set of extensions to the [PAC4J](http://www.pac4j.org) library developed internally by [IDC](http://www.idc.com) and published as open source.

It provides the following modules:
- Database configuration of SAML clients

## Building

You need [Apache Maven](http://maven.apache.org) to build the project. The project also requires Java 1.8, so be sure to have JDK 1.8 installed.

To build the project:
```
mvn clean package
```
Then grab JAR files from `target` directories of individual modules.

You can also install all modules into your local Maven repository:
```
mvn clean install
```
