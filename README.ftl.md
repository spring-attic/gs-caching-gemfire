<#assign project_id="gs-caching-gemfire">

# Getting Started: Caching Data with GemFire

What you'll build
-----------------

This guide walks you through the process of turning on caching sections of code. In this case, you'll see how to request publicly visible data from Facebook, cache it, and then see that fetching the same thing again eliminates the repetitive call to Facebook.

What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>


## <@how_to_complete_this_guide/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>

### Create a Maven POM

    <@snippet path="pom.xml" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Create a bindable object for fetching data
------------------------------------------
Now that you've set up the project and build system, you can focus on defining an object to capture the bits needed when pulling data from Facebook.

    <@snippet path="src/main/java/hello/Page.java" prefix="complete"/>
    
The `Page` class has `name` and `website` properties along with standard getters and setters. These are the two attributes you will gather further along in this guide.

You'll notice the class is marked as `@JsonIgnoreProperties(ignoreUnknown=true)`. That means that even though there are other attributes that will be retrieved you are going to ignore them.

Query Facebook for data
-----------------------
Your next step is to create a service that queries Facebook for data about pages. 

    <@snippet path="src/main/java/hello/FacebookLookupService.java" prefix="complete"/>
    
This service uses Spring's `RestTemplate` to query Facebook's http://graph.facebook.com API. Facebook returns a JSON object, but as you can see, it returns a `Page` instead of a `String`.

The key piece of this service is how `findPage` has been annotated with `@Cacheable("hello")`. [Spring's caching abstraction](http://static.springsource.org/spring/docs/3.2.2.RELEASE/spring-framework-reference/html/cache.html) intercepts the call to this method to check if it's already been called. If so, it returns the cached copy. Otherwise, it proceeds to invoke the method, store the response in the cache, and then return the results to the caller.

> **Note:** You are required to supply the name of the cache.

This demonstrates the value of caching certain calls. If your application is constantly looking up the same data which is expensive to calculate, caching the results can improve your performance dramatically.

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

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>
    
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