Noop et Al
============

![Logo](miscellaneous/logo.jpg)

Generation of standard classes from interfaces:

 * `@Noop` generates a class that does nothing and returned default values (0 or <code>null</code>). Classes of that kind are 
   often used as base for classes that handle only some event/method calls of the annotated interface.
 * `@Decor` generates a class that passes all calls to another implementation of the interface. Really useful to
   create decorators.

```java
package com.company.example;

@Noop
interface ExampleInterface {

  void setProperty(String value);
  
  String calculateSomething(String param, int thistoo);
}
```

The generated code will look like this:

```java
package com.company.example;

/**
 * @Generated ..
 */
public class NoopExampleInterface {
  
  void setProperty(String ignored) { }
  
  String calculateSomething(String ignore1, int ignored2) {
    return null;
  }

}
```
```java
package com.company.example;

/**
 * @Generated ..
 */
public class DecoratingExampleInterface {
  private final ExampleInterface decorated;
  
  public DecoratingExampleInterface(@NonNull ExampleInterface decorated) {
    this.decorated = decorated;
  }
  
  void setProperty(String value) {
    decorated.setProperty(value);
  }
  
  String calculateSomething(String param, int thistoo) {
    return decorated.calculateSomething(param, thistoo);
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
  <version>0.2.0-SNAPSHOT</version>
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

Finally, I started that project from [Butterknife](https://github.com/JakeWharton/butterknife)'s annotation processor.

License
-------

    Copyright 2015 PJ Champault

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


License
-------

    Copyright 2013 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.




