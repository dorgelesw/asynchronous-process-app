# Java backend developer profile - Asynchronous process using Spring and injecting user context

## 1. Asynchronous process
Async process appear sometime when we have a long-running or distributed task. 
The general practice that’s followed in java world to start an async process it's implementation of  `Runnable` or `Callable` method.

A good practice by using `Spring` it's to create thread inside the spring context 
to be able to autowire inside the class in case process execution need to communicate with other components.

We can have two types of asynchronous process:
### 1.1. Async request

It's about http client request which start with Tomcat and end Tomcat (In case we use servlet container Tomcat).

Normally, Tomcat get the client request, holds the connection and returns a response to the client through the controller.

We talk about async request when we want to release Tomcat thread associated with a request but keep the client connection (don't return response) and run heavy processing to a new thread.

When the heavy processing completes in the asynchronous execution context, the thread generate a response and return it to the client (by Tomcat) or dispatch the request to another servlet.

Let me define servlet container element as:
1. [Contract between servlet class and servlet container](https://docs.oracle.com/javaee/7/api/javax/servlet/package-summary.html)
2. Thread Pool Request. These thread pools are necessary to control the amount of threads that are being executed simultaneously and avoid `OutOfMemoryError`. 
3. Async execution context introduced in [Servlet 3.0](https://docs.oracle.com/javaee/7/api/javax/servlet/AsyncContext.html)

[Thus we can consider this flow for async request:](https://docs.oracle.com/javaee/7/tutorial/servlets012.htm)

1. Tomcat (servlet container) get client request.
2. Take a servlet thread request available in the pool.
3. Servlet container release thread request but keep the client connection and run heavy processing to an asynchronous execution context with a new thread.
4. Thread generate a response once the heavy process ended or dispatch the request to another servlet.

### 1.2. Async code 

When we run a method into a service in a separate thread.

## 2. Spring overview solution
### 2.1. Async code
* [TaskExecutor](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/task/TaskExecutor.html)

This is a functional interface define by `Spring` 
which extend `Executor` from java and can therefore be used as the assignment target for a lambda expression or method reference.

In practice, define a task that can be executed by multiple threads which will be an interface or an implementation of [Runnable or Callable](https://www.baeldung.com/java-runnable-callable). 
Then, use `TaskExecutor` to run the task.

Note that implementation of task could be:
```
@Component
@Scope("prototype")
```
If it's stateful otherwise,
```
@Component
```

* [@Async](https://www.baeldung.com/spring-async)

Using `@Async` annotation on a method, let spring know that this method has to be executed in a separate thread.

So, spring will take care of creating a thread and starting it. All the good stuff like `@Autowire` will still work and no need to make any changes to the existing code.

`@Async` has two rules:
* it must be applied to public methods only
* self-invocation – calling the async method from within the same class – won't work

By default, Spring uses a `SimpleAsyncTaskExecutor` to actually run these methods asynchronously. 
The defaults can be overridden at two levels – at the application level or at the individual method level.

Before to use `@Async`, first at all you need to enable asynchronous processing either with Java configuration
```
@Configuration
@EnableAsync
```
or XML configuration by using the task namespace
``` 
<task:executor id="myexecutor" pool-size="5"  />
<task:annotation-driven executor="myexecutor"/>
```
### 2.2. Async request
Spring web MVC uses [asynchronous processing](https://docs.spring.io/spring-framework/docs/5.2.7.RELEASE/spring-framework-reference/web.html#mvc-ann-async-processing) support provided by `Servlet 3.1+` which allows processing an HTTP request in another thread than the request receiver thread.
Controller can use:
* `DeferredResult` and `Callable` for a single asynchronous return value or
* `HTTP Streaming` return type for multiple asynchronous values or 
* `Reactive types` for response handling waiting by reactive clients.

The kind of problems which find their usage of this functionality can be:
* A long-running task in general, since while another thread processes this request, the container thread request is free and can continue serving other requests.
* Execute intensive I/O operations.
* Do network tasks, such as handling file uploads or processing a huge volume of data coming from clients.

#### 2.2.1. [DeferredResult and Callable](https://docs.spring.io/spring-framework/docs/5.2.7.RELEASE/spring-framework-reference/web.html#mvc-ann-async-deferredresult)
The both concepts make a Java web application scalability (Servlet API Thread Pool) 
by resolving the specific problem of blocking servlet threads due to long term requests, which is releasing the container thread and processing the long-running task asynchronously in another thread.

The difference from both is that it's your responsibility to manage the thread executing the task and set the result when you use `DeferredResult`.

##### 2.2.1.1. DeferredResult
Remember that it's your responsibility to manage thread executing the task and result when you use `DeferredResult`.
I recommend creating an asynchronous task with java tool `CompletableFuture` which is a custom way to running, monitoring, debugging, and tracking asynchronous tasks.
The default executor thread pool used is `ForkJoinPool.commonPool()`.
```
 DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(taskService::execute)
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
```
###### DeferredResult Callbacks
Keep in mind that by using `DeferredResult` we can use callback function to set result. We have 3 types of callbacks:
* onCompletion(): to define a block of runnable code that's executed when an async request completes.
* onTimeout(): to register custom code to invoke once timeout occurs. In order to limit request processing time, we can pass a timeout value during the DeferredResult object creation.
* onError(): when computation fails due to some error or exception.

##### 2.2.1.2. Callable
A functional interface task Callable<V> returns a result and may throw an exception. Implementors define a single method with no arguments called `call`.
An `Executor` is required to handle Callable return values. If a `TaskExecutor` is not configured, `SimpleAsyncTaskExecutor` will be used as default.
You can use `WebAsyncTask` to set the default timeout value on a `Callable`.

#### 2.2.2. [HTTP Streaming](https://docs.spring.io/spring-framework/docs/5.2.7.RELEASE/spring-framework-reference/web.html#mvc-ann-async-http-streaming)
It's used to produce multiple asynchronous return values and have those written to the response generally as body in a `ResponseEntity`. Depending on the usage, the return values can be in different types.
We can use `CompletableFuture` to run our asynchronous tasks. But for our examples in this part, we will simple use asynchronous entity provided by the `ExecutorService`.

Note also that since we are in Spring MVC, the connection with client is keep with a time out. When Async request timed out is coming, the client side stop to receive data.
We can solve this issue by increase time out request or implement a mechanism of data pool request.

Let us now describe the different return value types:
* Objects

We use `ResponseBodyEmitter` to send a stream of objects to the client, where each object sent is written to the response with an `HttpMessageConverter`.
`HttpMessageConverter` is used to serialize the object in the corresponding http media type.

* SSE [(Server-Sent Event)](https://www.baeldung.com/spring-server-sent-events)

We use the return type `SseEmitter` to produce an SSE stream from a controller where events sent from the server are formatted according to the W3C SSE specification.

The SSE specification is being adopted by most browsers to allow streaming events unidirectionally at any time. 

The events are just a stream of UTF-8 encoded text data that follow the format defined by the specification. This format consists of a series of key-value elements (id, retry, data and event).
The data payload format in any way; we can use a simple String or a more complex JSON or XML structure.

* Raw Data

This return type is useful to bypass message conversion and stream directly to the response `OutputStream`.

We can write a primitive data types, files and graphs of Java objects to an `OutputStream`. Then, use the `StreamingResponseBody` return value type.


## 3. Sample case study: Base Compute application
Implement a restful API in Java and Spring Boot that takes in input a positive natural number and
starts a long-running computation, storing in memory the outcome of the computation.

To simulate the computation just let the thread sleep 5 seconds and then generate a random
number between 1 and 100 as outcome of the simulated computation.

The api will not wait for the end of the computation, but will immediately return with a feedback
message `The answer is being calculated, please call again later..`.
If instead the computation was already performed for the given input, the stored generated
outcome will be returned.

Be sure to not accidentally start the same computation twice for the same input if two calls arrive
near in time, or while the calculation for the given input is still in progress.
Additionally, the computation fails on the 1% of the cases. If the generated number is 99 the
computation will be considered failed, and a subsequent request for the same input will result in a
new computation started.

### 3.1. Business requirements
* The application should be designed as Restful web service.
* The service are asynchronous. 
Means api will not wait for the end of the computation, but it will immediately return with a feedback message.  
* The service of application should be stateless.
* The computation is running in concurrency. The application should not start the same computation for the same input.

### 3.2. Technical requirements
* `Java 1.8` or later.
* `Spring Boot 2.x` to start and run application.
* We will use `maven 3.x` to build application.

### 3.3. Solution Concept
To respond our business requirements, we will use:
* `Spring Web MVC Framework` to implement the Restful API.
* Since we don't need to persist data when the application shuts down, we will use In-memory database because memory access is faster than disk access.
So we will run H2 database as embedded database in Spring Boot.
* We will implement two approaches of asynchronous service as code. 
We will use first use `TaskExecutor` approach and `ThreadLocal` java tool to inject user context in our task. Then secondly, the `@Async` annotation support in Spring.
* We will use `Spring Data JPA` for mapping entity in the database.
* API Endpoint will take input natural number as a parameter and return response in `JSON` format. 

### 3.4. Request Endpoint
* Using `TaskExecutor` method
```
http://www.providus.tech:8080/base-service?naturalNumber=10
```
* Using `@Async` method
```
http://www.providus.tech:8080/async-service?naturalNumber=10
```
### 3.5. Response Format
* Computation in progress
```
{
    "naturalNumber": 10,
    "message": "The answer is being calculated, please call again later.."
}
```
* Computation completed
```
{
    "naturalNumber": 10,
    "message": "The generated number is 37."
}
```
* Error bad request if parameter is not a natural number or if it's missing.
```
{
    "timestamp": "2020-07-10T23:59:53.569+0000",
    "status": 400,
    "error": "Bad Request"
}
```
### 3.6. Technical Architecture
Our application has 2 principal components.
#### Business Logic
* `Controller` : handle request
* `Service` : manage computation
* `Repository`: manage DAO
#### Data Storage
In-memory database to persist.