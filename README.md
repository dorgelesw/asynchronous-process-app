# Java backend developer profile - Asynchronous process using Spring and injecting user context

## 1. Asynchronous process
Async process appear sometime when we have a long-running or distributed task. 
The general practice that’s followed in java world to start an async process is to create 
a separate thread or implement Runnable method.

A good practice by using `Spring` it's to create thread inside the spring context 
to be able to autowire inside the class in case process execution need to communicate with other components.

We can have two types of asynchronous process:
* Async request

It's about http client request which start with Tomcat and end Tomcat.

Normally, Tomcat get the client request, holds the connection and returns a response to the client through the controller.

We talk about async request when we want to release Tomcat thread but keep the client connection (don't return response) and run heavy processing on a different thread.

And then, when your heavy processing complete, update Tomcat with its response and return it to the client (by Tomcat).

* Async code 

When we run a method into a service in a separate thread.

## 2. Spring overview solution
### 2.1. Async code
* [TaskExecutor](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/task/TaskExecutor.html)

This is a functional interface define by `Spring` 
which extend `Executor` from java and can therefore be used as the assignment target for a lambda expression or method reference.

In practice, define a task that can be executed by multiple threads which will be an interface or an implementation of [Runnable or Callable](https://www.baeldung.com/java-runnable-callable). 
Then, use `TaskExecutor` to run the task.

Note that implementation of task should be:
```
@Component
@Scope("prototype")
```

* [@Async](https://www.baeldung.com/spring-async)

Using `@Async` annotation on a method, let spring know that this method has to be executed in a separate thread.

So, spring will take care of creating a thread and starting it. All the good stuff like `@Autowire` will still work and no need to make any changes to the existing code.

`@Async` has two rules:
* it must be applied to public methods only
* self-invocation – calling the async method from within the same class – won't work

By default, Spring uses a `SimpleAsyncTaskExecutor` to actually run these methods asynchronously. 
The defaults can be overridden at two levels – at the application level or at the individual method level.


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

## 4. Business requirements
* The application should be designed as Restful web service.
* The requests to application are asynchronous. 
Means api will not wait for the end of the computation, but it will immediately return with a feedback message.  
* The service of application should be stateless.
* The computation is running in concurrency. The application should not start the same computation for the same input.

## 5. Technical requirements
* `Java 1.8` or later.
* `Spring Boot 2.x` to start and run application.
* We will use `maven 3.x` to build application.

## 6. Solution Concept
To respond our business requirements, we will use:
* `Spring Web MVC Framework` to implement the Restful API.
* Since we don't need to persist data when the application shuts down, we will use In-memory database because memory access is faster than disk access.
So we will run H2 database as embedded database in Spring Boot.
* We will use `ThreadLocal` java tool to manage concurrent computation.
* We will use `Spring Data JPA` for mapping entity in the database.
* API Endpoint will take input natural number as a parameter and return response in `JSON` format. 

### Request Endpoint
```
http://www.providus.tech:8080/base-service?naturalNumber=10
```
### Response format
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
## 7. Technical architecture
Our application has 2 principal components.
### Business logic
* `Controller` : handle request
* `Service` : manage computation
* `Repository`: manage DAO
### Data Storage
In-memory database to persist.