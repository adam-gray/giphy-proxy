giphy-proxy
=============
A simple multi-threaded HTTPS Tunnel to proxy requests to Giphy.

Running this as a proxy to handle HTTP CONNECT requests 
and allow requests to go to service that is explicitly allowed while
retaining a TLS tunnel to the endpoint. 


Build Instructions
------------------
* **Prerequisites:**
    * [Java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) and [Maven](https://maven.apache.org/download.html).

```bash
unzip giphy-proxy.zip

cd giphy-proxy

# Compile and run
mvn clean install
```


Usage
------
The software is packaged in a single Jar. It expects exactly two arguments,
the local point which to bind to, and the remote hostname:port combo where 
requests are allowed.

For example: 

```shell
cd giphy-proxy/target
java -jar giphy-proxy.jar 1312 api.giphy.com:443
curl -p -x localhost:1312 "https://api.giphy.com/v1/gifs/search?api_key=O4FTCFzVGQFAhaCzS9uFD5NFBY8tt14I&q=cat&limit=1&offset=0&rating=g&lang=en"
```


Design
-------
This software is designed to be as simple as possible and use as
few external dependencies as possible. Less code == ess bugs.

The basic flow of the application is as follows: 

* **GiphyProxy,java**
    * Bind a socket to the local port to act as a proxy between clients 
    the Giphy api
    * Creates a ConnectHandler for processing each request
    * Await connections to that socket in a loop spin up new Proxy processes
* **ConnectHandler.java**
    * Handles the plaintext connection from the client and local socket
    for HTTP CONNECT requests
    * Verifies that the endpoint requested by the user matches what was specified
    when running signal-proxy
    * Sends error responses when encountering bad endpoints or invalid requests
* **Proxy.java**
    * A wrapper class which creates two individual Tunnels to support 
    bi-directional communication between the local and remote sockets
* **Tunnel.java**
    * Copies data from one socket to another in a single direction
    * Closes the input / output streams of the sockets when finished
    * Signals to its parent Proxy if an error occurred to attempt to close
    both sockets.

Testing
-------
* Unit and integration tests can be run via IDE (I use IntelliJ IDEA) or via command line
 by invoking `mvn test` within the directory
* Unit testing was done to achieve as close to 100% coverage as possible. 
* Integration testing was done on the software and Giphy API to test successful
single thread and parallel tests, including error responses.
* Static code analysis done via SonarLint


Limitations and Potential Improvements
------------------------------------
* Only one allowed endpoint to connect to, which is a requested restriction
of this project. If required, it would be possible to store a collection of
allowed endpoints or remove the restriction completely for general use as a 
proxy. 

* Threading / Async ugliness. I prefer a more functional-styled Java since 
lambdas were introduced, but opted to use foundational structures as a trade-off
for clarity and brevity. 

* Unbound thread and memory usage. I suppose if you threw thousands of threads
at this it would probably eat up a lot of CPU and RAM, especially if the network
was slow, causing a lot of objects to blow up the heap or cause a lot of GC.

* Lack of dependencies. The only non-test dependency is for logging, as Java's
out of the box logging isn't very user-friendly, so I included that for my own
sanity during development and testing. I often times like to use a large utility
dependency such as Apache Commons or Google Guava but didn't want to add bloat 
without clear benefit. 

* Better way to test and ensure TLS strength, as this relies on the Giphy endpoint
being serious about their TLS version and key strength