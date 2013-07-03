
# Getting Started: Caching Data with GemFire

What you'll build
-----------------

This guide walks you through the process of turning on caching sections of code. In this case, you'll see how to request publicly visible data from Facebook, cache it, and then see that fetching the same thing again eliminates the repetitive call to Facebook.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/getting-started), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/gs-caching-gemfire.git`
 - cd into `gs-caching-gemfire/initial`
 - Jump ahead to [Create a resource representation class](#initial).

**When you're finished**, you can check your results against the code in `gs-caching-gemfire/complete`.
[zip]: https://github.com/springframework-meta/gs-caching-gemfire/archive/master.zip


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to [Getting Started with Maven](../gs-maven/README.md) or [Getting Started with Gradle](../gs-gradle/README.md).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-caching-gemfire-initial</artifactId>
    <version>0.1.0</version>

    <dependencies>
        <dependency>
        	<groupId>org.springframework.data</groupId>
        	<artifactId>spring-data-gemfire</artifactId>
        	<version>1.3.0.RELEASE</version>
        </dependency>
        <dependency>
        	<groupId>org.springframework</groupId>
        	<artifactId>spring-webmvc</artifactId>
        	<version>3.2.2.RELEASE</version>
        </dependency>
        <dependency>
        	<groupId>com.gemstone.gemfire</groupId>
        	<artifactId>gemfire</artifactId>
        	<version>7.0.1</version>
        </dependency>
    </dependencies>
    
    <!-- TODO: remove once bootstrap goes GA -->
    <repositories>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
		<repository>
			<id>gemstone</id>
			<url>http://dist.gemstone.com.s3.amazonaws.com/maven/release/</url>
		</repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

TODO: mention that we're using Spring Bootstrap's [_starter POMs_](../gs-bootstrap-starter) here.

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.


<a name="initial"></a>
Create a bindable object for fetching data
------------------------------------------
Now that you've set up the project and build system, you can focus on defining an object to capture the bits needed when pulling data from Facebook.

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

You'll notice the class is marked as `@JsonIgnoreProperties(ignoreUnknown=true)`. That means that even though there are other attributes that will be retrieved you are going to ignore them.

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
    
This service uses Spring's `RestTemplate` to query Facebook's http://graph.facebook.com API. Facebook returns a JSON object, but as you can see, it returns a `Page` instead of a `String`.

The key piece of this service is how `findPage` has been annotated with `@Cacheable("hello")`. [Spring's caching abstraction](http://static.springsource.org/spring/docs/3.2.2.RELEASE/spring-framework-reference/html/cache.html) intercepts the call to this method to check if it's already been called. If so, it returns the cached copy. Otherwise, it proceeds to invoke the method, store the response in the cache, and then return the results to the caller.

> **Note:** You are required to supply the name of the cache.


Make the application executable
-------------------------------
This application uses the **maven-shade-plugin**. By adding the following code to pom.xml, you can build a runnable uber jar.

```xml
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-shade-plugin</artifactId>
				<version>2.1</version>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>shade</goal>
						</goals>
						<configuration>
							<transformers>
								<transformer
									implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
									<mainClass>hello.Application</mainClass>
								</transformer>
							</transformers>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
```

### Create a main class

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
    
At the top of our configuration are two vital annotations: `@EnableCaching` and `@EnableGemfireRepositories`. This turns on caching and adds some important beans in the background to support caching with GemFire as your data store.

The first bean is an instance of `FacebookLookupService`.

The next three are needed to connect with GemFire and provide caching.
- `cacheFactoryBean` creates a GemFire cache.
- `localRegionFactoryBean` defines a GemFire region inside the cache. It is geared to be named "hello", which must match our usage of `@Cacheable("hello")`.
- 'cacheManager` is needed to support Spring's caching abstraction.

The `main()` method creates an application context based on the surrounding class. It fetches a `FacebookLookupService`, and proceeds to lookup some pages on Facebook. It first looks for the **SpringSource** page twice. The second time should definitely be faster. It then looks for the **Pivotal** page, and you can see that the time is longer, indicating that things are being cached based on the input parameters to `findPage`.

The actual call to the `FacebookLookupService` is wrapped in a separate method to capture the process of timing the call. This lets you see exactly how long any one lookup is taking.


Run the service
---------------

Run your service with `java -jar` at the command line:

    java -jar target/gs-caching-gemfire-complete-0.1.0.jar

Logging output is displayed. The service should be up and running within a few seconds.

```
Found Page [name=SpringSource, website=http://www.springsource.com], and it only took 620 ms to find out!

Found Page [name=SpringSource, website=http://www.springsource.com], and it only took 0 ms to find out!

Found Page [name=Pivotal, website=http://www.gopivotal.com], and it only took 78 ms to find out!
```

Summary
-------

Congrats! You've just built a service that performed an expensive operation and tagged it to cache results.