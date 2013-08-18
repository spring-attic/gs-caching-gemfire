<#assign project_id="gs-caching-gemfire">
This guide walks through the process of using GemFire's data fabric to cache certain calls from your code.

What you'll build
-----------------
You'll build a service that requests publicly visible data from Facebook and caches it in GemFire. You'll then see that fetching the same thing again eliminates the expensive call to Facebook.

What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>


## <@how_to_complete_this_guide jump_ahead="Create a bindable object for fetching data"/>

<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>

### Create a Gradle build file

    <@snippet path="build.gradle" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Create a bindable object for fetching data
------------------------------------------
Now that you've set up the project and build system, you can focus on defining an object to capture the bits you need to pull data from Facebook.

    <@snippet path="src/main/java/hello/Page.java" prefix="complete"/>
    
The `Page` class has `name` and `website` properties along with standard getters and setters. These are the two attributes you will gather further along in this guide.

Note that the class is marked as `@JsonIgnoreProperties(ignoreUnknown=true)`. That means that even though other attributes will be retrieved, they'll be ignored.

Query Facebook for data
-----------------------
Your next step is to create a service that queries Facebook for data about pages. 

    <@snippet path="src/main/java/hello/FacebookLookupService.java" prefix="complete"/>
    
This service uses Spring's `RestTemplate` to query Facebook's http://graph.facebook.com API. Facebook returns a JSON object, but Spring binds the data to produce a `Page` object.

The key piece of this service is how `findPage` has been annotated with `@Cacheable("hello")`. [Spring's caching abstraction](http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/cache.html) intercepts the call to `findPage`to check whether it's already been called. If so, Spring's caching abstraction returns the cached copy. Otherwise, it proceeds to invoke the method, store the response in the cache, and then return the results to the caller.

> **Note:** You must supply the name of the cache. We named it "hello" for demonstration purposes, but in production, you should probably pick a more descriptive name. This also means different methods can be associated with different caches. This is useful if you have different configuration settings for each cache.

Later on when you run the code, you will see the time it takes to run each call and be able to discern whether or not the result was cached. This demonstrates the value of caching certain calls. If your application is constantly looking up the same data, caching the results can improve your performance dramatically.

Make the application executable
-------------------------------

Although GemFire caching can be embedded in web apps and WAR files, the simpler approach demonstrated below creates a standalone application. You package everything in a single, executable JAR file, driven by a good old Java `main()` method.

### Create an Application class

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>
    
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

<@build_an_executable_jar_subhead/>

<@build_an_executable_jar_with_gradle/>

<@run_the_application_with_gradle/>

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