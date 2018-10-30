**Notice: Development on this repository is currently on pause during our v3 rearchitecture. Please see [storj/storj](https://github.com/storj/storj) for ongoing v3 development.**

# java-libstorj

[![Storj.io](https://storj.io/img/storj-badge.svg)](https://storj.io)
[![Build Status](https://travis-ci.org/storj/java-libstorj.svg?branch=master)](https://travis-ci.org/storj/java-libstorj)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.storj/libstorj-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.storj/libstorj-java)
[![Javadocs](http://javadoc.io/badge/io.storj/libstorj-java.svg)](http://javadoc.io/doc/io.storj/libstorj-java)

Java library for encrypted file transfer on the Storj network via bindings to [libstorj](https://github.com/Storj/libstorj).

For building Android applications it is recommended to use the [android-libstorj](https://github.com/Storj/android-libstorj) library.

## Setup

The library consists of a Java archive (JAR) and native shared libraries.

The JAR can be fetched via Gradle or Maven, or downloaded from [Maven Central](http://search.maven.org/#artifactdetails|io.storj|libstorj-java|).

### Gradle

```Gradle
dependencies {
    compile 'io.storj:libstorj-java:0.8.3'
}
```

### Maven

```XML
<dependency>
  <groupId>io.storj</groupId>
  <artifactId>libstorj-java</artifactId>
  <version>0.8.3</version>
</dependency>
```

### Native Libraries

Pre-build native libraries for Windows, Linux and macOS can be downloaded from the [latest release](https://github.com/storj/java-libstorj/releases/latest) on GitHub.

The native libraries should be extracted to a location within the Java libary path. Usually it is just enough to have them all in the working directory of the application.

On Linux it might be also necessary to set the `LD_LIBRARY_PATH` environment variable to the same location.

Some system you may observe the error `Problem with the SSL CA cert (path? access rights?)`. This happens if the curl library cannot detect the location of the SSL CA certificates bundle. The latter can be manually set using the `STORJ_CAINFO` environment variable. If there is no suitable bundle on the system, one can be downloaded from [here](https://curl.haxx.se/docs/caextract.html).
