rest-client-tools
=======================

A REST client library designed for consuming REST endpoints using JAX-RS annotations.
The code borrows heavily from RESTEasy 2.3.4.Final.

### Motivation

The [JAX-RS 1.1 specification](https://jsr311.java.net/nonav/releases/1.1/spec/spec.html) loads only one instance of [javax.ws.rs.ext.RuntimeDelegate][1].
However, when there are two JAX-RS implementations on the classpath, such as Jersey and the [RestEasy client framework](http://docs.jboss.org/resteasy/docs/1.0.1.GA/userguide/html/RESTEasy_Client_Framework.html#Sharing_interfaces),
or even 2 different versions of the same framework implementation, exceptions like the following may occurr:

    java.lang.ClassCastException: com.sun.jersey.server.impl.provider.RuntimeDelegateImpl cannot be cast to org.jboss.resteasy.spi.ResteasyProviderFactory

To eliminate these conflicts and to make the client framework more portable rest-client-tools makes the following changes:

1. Extracted the proxy generation code from RestEasy.
2. Refactoring to no longer use the RuntimeDelegate when creating client proxies. 
3. Streamline the code as much as possible and reworked the API to have a friendlier fluent builder interface.

Client proxies created with rest-client-tools are able to run alongside any JAX-RS implementation without conflict. This allows for client to be used
in a much broader set of scenarios. For example, if a team has a service in production written using Jersey and has the need to 
consume an external REST service they can use rest-client-tools without conflict and without having to fully switch to RestEasy. 
rest-client-tools is designed and tested to be interoperable with any JAX-RS implementation. 

Additionally, there are other situations where there may be need to consume an http endpoint but don't want to put a full blown JAX-RS 
implementation on the classpath. In that case, using rest-client-tools:
 
 - include the JAX-RS api jar (so that the JAX-RS annotations are available)
 - configure JAX-RS to use the com.opower.rest.client.generator.core.BasicRuntimeDelegate (see the javadoc for details) 

Now create client proxies in applications where server side component of JAX-RS are not needed.

For more background information check out our [Engineering Blog](http://opower.github.io/2015/01/30/rest-client-tools/).

### Features

  * Specify a custom MessageBodyReader / Writer to have control over the message body serialization.
  * ClientRequestFilters allow altering the HTTP request before it is send (adding headers etc.)
  * ClientErrorInterceptors allow for custom handling of failed http service calls.
  * Automatic Hystrix circuit breaker integration when using the HystrixClient.Builder

## Development

### Release

  1. Make sure to have the latest code

        git pull

  2. Use the Maven Release Plugin 

        mvn release:prepare && mvn release:perform

### API example

A JAX-RS resource defined using annotations.  The resource interface is reusable on both client and server side.
 
    @Path("/frob")
    @Produces(MediaType.APPLICATION_JSON)
    public interface FrobResource {
        @POST
        @Path("{frobId}")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        Frob updateFrob(@PathParam("frobId")String frobId, @FormParam("name") String name);
   
        @GET
        @Path("{frobId}")
        Frob findFrob(@PathParam("frobId") String frobId);
   
        @PUT
        @Consumes(MediaType.APPLICATION_JSON)
        Response createFrob(Frob frob);
    }

Here is a server side resource implementation example:
 
    public class FrobServerResource implments FrobResource {
        /*  Notice there are no JAX-RX annotations here. That is because
         *  the annotations belong on the interface ONLY. In some cases this
         *  can cause your endpoints to not function correctly. Or if you only put
         *  an annotation here and its missing from the interface, your clients won't
         */ function correctly since they don't have access to your server side resources
        public Frob updateFrob(String frobId, String name) {
           // do work here probably with dao and stuff
        }

        public Frob findFrob(String frobId) {
           // find frob work here
        }

        public Response createFrob(Frob frob) {
            // do more work
        }
    }
   
 
On the client side, import and use Client.Builder create and customize a proxy instance for a given resource interface. 
In addition to the standard client proxy, the Client.Builder is thread-safe and intended to be singletons.
Also, calls are wrapped in HystrixCommands implementing the circuit breaker pattern for client side robustness.
The rest-client-tools proxy builder requires a UriProvider implementation that tells clients how to find a service instance.
  
    Client.Builder<FrobResource> clientBuilder = new Client.Builder<>(FrobResource.class, serviceDiscovery, serviceName, OAUTH_CLIENT_ID);

                              
You may need to alter the proxy's requests before they are sent. For instance, you may need to add a header or some other parameter to the request.
  
    ClientRequestFilter filter = new ClientRequestFilter() {
              @Override
              public void filter(ClientRequest request) {
                  // add a http header to the client requests
                  request.header("header name", "header value");
              }
          };
   
    clientBuilder.addClientRequestFilter(filter); // filters are applied in the order they are added.

By default, http response codes between 400 (Bad Request) and 599 (Network Connect Timeout) are considered errors and
cause a ClientResponseFailure to be thrown. In some cases you may want certain response codes to not be treated as an Exception.
For example, you might have an endpoint for checking for an item in inventory and want to have it return 404 if no
inventory is available. In such a case you can use a custom errorStatusCriteria.

    // The Predicate specifies which status codes should result in a ClientResponseFailure
    Predicate<Integer> criteria = new Predicate<Integer>() {
            @Override
            public boolean apply(Integer status) {
                return 404 != status && status >= BAD_REQUEST && status <= NETWORK_CONNECT_TIMEOUT;
            }
        };

    clientBuilder.errorStatusCriteria(criteria);

ClientErrorInterceptor defines the proxy's behavior in case of errors. Here is how you would specify your own list of custom ClientErrorInterceptors.

    List<ClientErrorInterceptor> interceptors = ImmutableList.<ClientErrorInterceptor>of(new ClientErrorInterceptor() {
              @Override
              public void handle(ClientResponse response) throws RuntimeException {
                  // handle the error response as you see fit
                  throw new SpecialException(response.getResponseStatus());
              }
          });
   
    clientBuilder.clientErrorInterceptors(interceptors); 
    
If your service produces and consumes JSON it is easy to use the JacksonJsonProvider for serialization.
  
    JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
     
    clientBuilder.registerProviderInstance(jsonProvider);
 
 
Client proxy instances require a ClientExecutor instance that will actually perform the http requests.
  
    ClientExecutor executor = new ApacheHttpClient4Executor();
    clientBuilder.executor(executor);
    
    
If you use the HystrixClient.Builder, then all method invocations on client proxies are wrapped with a HystrixCommand object. 
Each method on your resource interface will receive its own HystrixCommandKey. 
This key can be overridden but shouldn't need to be.
The HystrixCommandProperties and HystrixThreadPoolProperties can be configured for individual methods or for the whole resource. 
You can also specify a fallback if a command fails. Have a look:
   
    Method findFrob = FrobResource.class.getMethod("findFrob");
    clientBuilder.methodCommandKey(findFrob, HystrixCommandKey.Factory.asKey("BEAUTIFUL_SNOWFLAKE"));
   
    // this changes the command timeout for just the findFrob method with razor-like precision
    clientBuilder.methodProperties(findFrob, new ConfigurationCallback<HystrixCommandProperties.Setter>() {
                      @Override
                      public void configure(HystrixCommandProperties.Setter setter) {
                          setter.withExecutionIsolationThreadTimeoutInMilliseconds(5000);
                      }
                  });
   
    // you don't have to do it for each method invidually though. 
    // Let's change the core pool size for all of the methods on the ResourceInterface
    clientBuilder.threadPoolProperties(new ConfigurationCallback<HystrixThreadPoolProperties.Setter>() {
                      @Override
                      public void configure(HystrixThreadPoolProperties.Setter setter) {
                          setter.withCoreSize(10);
                      }
                  });
   
    // how about a fallback?
    clientBuilder.methodFallback(findFrob, new Callable<Object>() {
              @Override
              public Object call() throws Exception {
                  Frob default = new Frob();
                  default.setId("default");
                  default.setName("Generic");
                  return default;
              }
          });


HystrixClient proxy instances wrap each http call in a HystrixCommand. Any exception thrown during request processing
can trip the Hystrix circuit breaker. There are some Exceptions that are intended to be part of the normal operation of
certain API designs and should not count towards circuit breaker or trigger fallbacks. For these cases the exceptions must
be wrapped in a [HystrixBadRequestException](https://github.com/Netflix/Hystrix/wiki/How-To-Use#ErrorPropagation). When using the
HystrixClient.Builder you can provide a BadRequestCriteria instance to specify which responses or Exceptions should be wrapped in
HystrixBadRequestExceptions (not count toward circuit-breaker failures or trigger fallback logic). By default, any response
that returns a 400 Bad Request and the exception will be wrapped in a HystrixBadRequestException. Additionally, it should
be noted that beside bad responses from the server, there can be client side errors after a successful call to the server.
This can happen if there's a problem deserializing a response on the client side for instance. In such cases, the client side
exceptions are also wrapped in HystrixBadRequestExceptions.

    // you can configure the criteria per method. In this example both 400 and 404 are treated as HystrixBadRequestExceptions
    // Note the exception that was thrown is also available for inspection in your BadRequestCriteria implementation. This
    // will either be the original exception or the Exception that was thrown by any ClientErrorInterceptor you have provided.
    clientBuilder.methodBadRequestCriteria(findFrob, new HystrixClientErrorHandler.BadRequestCriteria() {
                                  @Override
                                  public boolean apply(BaseClientResponse response, Exception exception) {
                                      int status = response.getStatus();
                                      return  status == SC_NOT_FOUND || status == SC_BAD_REQUEST;
                                  }
                              })

    // you can also provide criteria for the whole resource interface
    clientBuilder.badRequestCriteria(new HystrixClientErrorHandler.BadRequestCriteria() {
                                      @Override
                                      public boolean apply(BaseClientResponse response, Exception exception) {
                                          int status = response.getStatus();
                                          return  status == SC_NOT_FOUND || status == SC_BAD_REQUEST;
                                      }
                                  })

[1]: http://docs.oracle.com/javaee/6/api/javax/ws/rs/ext/RuntimeDelegate.html#getInstance()
