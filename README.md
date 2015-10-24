Butter Knife
============

![Logo](website/static/logo.png)

TBW

Field and method binding for Android views which uses annotation processing to generate boilerplate
code for you.

 * Eliminate `findViewById` calls by using `@Bind` on fields.
 * Group multiple views in a list or array. Operate on all of them at once with actions,
   setters, or properties.
 * Eliminate anonymous inner-classes for listeners by annotating methods with `@OnClick` and others.
 * Eliminate resource lookups by using resource annotations on fields.

```java
package com.company.example;

@Noop
interface ExanpleInterface {

  void setProperty(String value);
  
  String calculateSomething(String param, int thistoo);
}
```

The generated code will look like this:

```java
package com.company.example;

@Generated
  public class ExampleInterfaceNoop {
  
  void setProperty(String ignored) { }
  }
  
  String calculateSomething(String ignore1, int ignored2) {
    return null;
  }

}
```

Download
--------

No download available.

Usage
--------

To use, download the source for the version of interest build it and install it in your local Maven repository.
Then, in a Maven project:
```xml
<dependency>
  <groupId>com.pij</groupId>
  <artifactId>com.pij.noopetal</artifactId>
  <version>0.1.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.pij:com.pij.noopetal:0.1.0'
```

For the SNAPSHOT version:
```xml
<dependency>
  <groupId>com.pij</groupId>
  <artifactId>com.pij.noopetal</artifactId>
  <version>8.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>com.pij</groupId>
  <artifactId>com.pij.noopetal-compiler</artifactId>
  <version>0.2.0-SNAPSHOT</version>
  <optional>true</optional>
</dependency>
```
or Gradle:
```groovy
buildscript {
  dependencies {
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
  }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
  compile 'com.pij:com.pij.noopetal:0.2.0-SNAPSHOT'
  apt 'com.pij:com.pij.noopetal-compiler:0.2.0-SNAPSHOT'
}
```

License
-------

    Copyright 2013 PJ Champault

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



