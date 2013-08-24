This guide walks through the process of using GemFire's data fabric to cache certain calls from your code.

What you'll build
-----------------
You'll build a service that requests publicly visible data from Facebook and caches it in GemFire. You'll then see that fetching the same thing again eliminates the expensive call to Facebook.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Gradle 1.7+][gradle] or [Maven 3.0+][mvn]

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/spring-guides/gs-caching-gemfire.git`
 - cd into `gs-caching-gemfire/initial`.
 - Jump ahead to [Create a bindable object for fetching data](#initial).

**When you're finished**, you can check your results against the code in `gs-caching-gemfire/complete`.
[zip]: https://github.com/spring-guides/gs-caching-gemfire/archive/master.zip
[u-git]: /understanding/Git

<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello


### Create a Gradle build file
Below is the [initial Gradle build file](https://github.com/spring-guides/gs-caching-gemfire/blob/master/initial/build.gradle). But you can also use Maven. The pom.xml file is included [right here](https://github.com/spring-guides/gs-caching-gemfire/blob/master/initial/pom.xml).

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

jar {
    baseName = 'gs-caching-gemfire'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.springsource.org/libs-snapshot" }
}

dependencies {
    compile("org.springframework.data:spring-data-gemfire:1.3.0.RELEASE")
    compile("org.springframework:spring-webmvc:3.2.4.RELEASE")
    compile("com.gemstone.gemfire:gemfire:7.0.1")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.7'
}
```
    
    

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/).


<a name="initial"></a>
Create a bindable object for fetching data
------------------------------------------
Now that you've set up the project and build system, you can focus on defining an object to capture the bits you need to pull data from Facebook.

`src/main/java/hello/Page.java`
```java
package hello;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Page {

    private String name;
    private String website;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "Page [name=" + name + ", website=" + website + "]";
    }

}
```
    
The `Page` class has `name` and `website` properties along with standard getters and setters. These are the two attributes you will gather further along in this guide.

Note that the class is marked as `@JsonIgnoreProperties(ignoreUnknown=true)`. That means that even though other attributes will be retrieved, they'll be ignored.

Query Facebook for data
-----------------------
Your next step is to create a service that queries Facebook for data about pages. 

`src/main/java/hello/FacebookLookupService.java`
```java
package hello;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

public class FacebookLookupService {

    RestTemplate restTemplate = new RestTemplate();

    @Cacheable("hello")
    public Page findPage(String page) {
        return restTemplate.getForObject("http://graph.facebook.com/" + page, Page.class);
    }

}
```
    
This service uses Spring's `RestTemplate` to query Facebook's http://graph.facebook.com API. Facebook returns a JSON object, but Spring binds the data to produce a `Page` object.

The key piece of this service is how `findPage` has been annotated with `@Cacheable("hello")`. [Spring's caching abstraction](http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/cache.html) intercepts the call to `findPage`to check whether it's already been called. If so, Spring's caching abstraction returns the cached copy. Otherwise, it proceeds to invoke the method, store the response in the cache, and then return the results to the caller.

> **Note:** You must supply the name of the cache. We named it "hello" for demonstration purposes, but in production, you should probably pick a more descriptive name. This also means different methods can be associated with different caches. This is useful if you have different configuration settings for each cache.

Later on when you run the code, you will see the time it takes to run each call and be able to discern whether or not the result was cached. This demonstrates the value of caching certain calls. If your application is constantly looking up the same data, caching the results can improve your performance dramatically.

Make the application executable
-------------------------------

Although GemFire caching can be embedded in web apps and WAR files, the simpler approach demonstrated below creates a standalone application. You package everything in a single, executable JAR file, driven by a good old Java `main()` method.

### Create an Application class

`src/main/java/hello/Application.java`
```java
package hello;

import java.io.IOException;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.support.GemfireCacheManager;

import com.gemstone.gemfire.cache.Cache;

@Configuration
@EnableCaching
@EnableGemfireRepositories
public class Application {

    @Bean
    FacebookLookupService facebookLookupService() {
        return new FacebookLookupService();
    }

    @Bean
    CacheFactoryBean cacheFactoryBean() {
        return new CacheFactoryBean();
    }

    @Bean
    LocalRegionFactoryBean<Integer, Integer> localRegionFactoryBean(final Cache cache) {
        return new LocalRegionFactoryBean<Integer, Integer>() {{
            setCache(cache);
            setName("hello");
        }};
    }

    @Bean
    GemfireCacheManager cacheManager(final Cache gemfireCache) {
        return new GemfireCacheManager() {{
            setCache(gemfireCache);
        }};
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);

        FacebookLookupService facebookLookupService = ctx.getBean(FacebookLookupService.class);

        lookupPageAndTimeIt(facebookLookupService, "SpringSource");
        lookupPageAndTimeIt(facebookLookupService, "SpringSource");

        lookupPageAndTimeIt(facebookLookupService, "gopivotal");

        ctx.close();    
    }

    private static void lookupPageAndTimeIt(FacebookLookupService bigCalculator ,String page) {
        long start = System.currentTimeMillis();
        Page results = bigCalculator.findPage(page);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Found " + results + ", and it only took " + 
                elapsed + " ms to find out!\n");
    }

}
```
    
Your runnable `main` application is combined with all the configuration details in one class.
    
At the top of the configuration are two vital annotations: `@EnableCaching` and `@EnableGemfireRepositories`. This turns on caching and adds important beans in the background to support caching with GemFire.

The first bean is an instance of `FacebookLookupService`.

The next three are needed to connect with GemFire and provide caching.
- `cacheFactoryBean` creates a GemFire cache.
- `localRegionFactoryBean` defines a GemFire region inside the cache. It is geared to be named "hello", which must match your usage of `@Cacheable("hello")`.
- `cacheManager` supports Spring's caching abstraction.

> **Note:** Two of these beans are [factory beans](http://blog.springsource.org/2011/08/09/whats-a-factorybean/). This is a common pattern used for objects that need special creation logic. Basically, `CacheFactoryBean` results in a `Cache` bean and `LocalRegionFactoryBean` results in a `LocalRegion` bean being registered with the context.

The `main()` method creates an application context based on the surrounding class. It fetches a `FacebookLookupService`, and proceeds to look up some pages on Facebook. It first looks for the **SpringSource** page twice. As you'll see when you run the application later in this guide, the first time it runs, it will take a certain amount of time to retrieve data about the Facebook page. The second time should take almost no time at all because the first lookup of that page's information was cached. 

It then looks for the **GoPivotal** page, for the first time. The lookup time will also be noticeable, i.e. not close to zero, showing this page isn't cached. That is because the caching is linked to the input parameters of `findPage`.

For demonstration purposes, the call to the `FacebookLookupService` is wrapped in a separate method to capture the time to make the call. This lets you see exactly how long any one lookup is taking.

### Build an executable JAR

Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Below are the Gradle steps, but if you are using Maven, you can find the updated pom.xml [right here](https://github.com/spring-guides/gs-caching-gemfire/blob/master/complete/pom.xml) and build it by typing `mvn clean package`.

Update your Gradle `build.gradle` file's `buildscript` section, so that it looks like this:

```groovy
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.BUILD-SNAPSHOT")
    }
}
```

Further down inside `build.gradle`, add the following to the list of applied plugins:

```groovy
apply plugin: 'spring-boot'
```
You can see the final version of `build.gradle` [right here]((https://github.com/spring-guides/gs-caching-gemfire/blob/master/complete/build.gradle).

The [Spring Boot gradle plugin][spring-boot-gradle-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.
It also searches for the `public static void main()` method to flag as a runnable class.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ ./gradlew build
```

If you are using Gradle, you can run the JAR by typing:

```sh
$ java -jar build/libs/gs-caching-gemfire-0.1.0.jar
```

If you are using Maven, you can run the JAR by typing:

```sh
$ java -jar target/gs-caching-gemfire-0.1.0.jar
```

[spring-boot-gradle-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-tools/spring-boot-gradle-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/) instead.

Run the service
-------------------
If you are using Gradle, you can run your service at the command line this way:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-caching-gemfire-0.1.0.jar
```

> **Note:** If you are using Maven, you can run your service by typing `mvn clean package && java -jar target/gs-caching-gemfire-0.1.0.jar`.


Logging output is displayed. The service should be up and running within a few seconds.

```
Found Page [name=SpringSource, website=http://www.springsource.com], and it only took 620 ms to find out!

Found Page [name=SpringSource, website=http://www.springsource.com], and it only took 0 ms to find out!

Found Page [name=Pivotal, website=http://www.gopivotal.com], and it only took 78 ms to find out!
```

From this you can see that the first call to Facebook for the SpringSource page took 620ms, which the second call took 0ms. That clearly shows that the second call was cached and never actually hit Facebook. But when GoPivotal's page was retrieved, it took 78ms, which while faster than 620ms, was definitely NOT the result of retrieving cached data.

Summary
-------

Congratulations! You've just built a service that performed an expensive operation and tagged it so that it will cache results.