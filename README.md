# jetty-server-component-ecm

Configurable ECM based components to be able to start one ore more Server instances.

The attributes and references of the components can be changed dynamically
where possible (where Jetty allows it). This means that no server restart
is needed to:

 * Add / remove server connectors
 * Change the settings of connection factories
 * Add / remove servlet contexts
 * Add / remove servlets and filters within ServletContexts 

The module can be downloaded from maven-central.

## Components

 * Jetty Server Component
 * ServletContextHandler Factory
 * Server Connector Factory
 * HttpConnectionFactory Factory
 * SslConnectionFactory Factory
 * SecureRequestCustomizer
 * HashSessionHandler Factory
 * ErrorPageErrorHandler Factory

## How to try

 * Clone the project from GitHub
 * Run "mvn clean install"
 * Go to the "tests/target/eosgi-dist/jettyServerXXX/bin" folder
 * Run "./runConsole" on linux or "runConsole" on windows
 * Open "https://localhost:4848" in the browser to see the webconsole
 * There are two pre-configured connectors with servlets:
   * http://localhost:8080/sample/helloworld
   * https://localhost:8443/sample/helloworld

## Missing features (roadmap)

 * No possibility to specify trustStore and client cert auth for
   SslConnectionFactory Factory component
 * There is no component for JDBC based session handler
 * No components for ProxyRequestCustomizer and HostHeaderCustomizer
