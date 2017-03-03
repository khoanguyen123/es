# es
ES sample

### To build:
    Unix: gradlew clean build
    Windows: gradlew.bat clean build
    
### To run:
    java -jar es-0.0.1-SNAPSHOT.jar {index}
    
    where {index} is the ES index you want to run the search against.
    
### Configurations
    ##### search.txt contains the search query
    ##### application.properties contains the ES URL, user name, and password that need to be changed to fit your env
