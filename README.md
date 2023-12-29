# E-Commerce Web Api 
- E-Commerce Project is a basic api with microservice architecture for web applications in E-Commerce category.
- Common technologies used in project: ```Spring Boot``` , ```Spring Security```, ```Eureka```, ```Spring Cloud Gateway```, ```Spring Data Jpa```, ```Open Api```, ```Zipkin```, ```Hazelcast```, ```Lombok```, ```Json Web Token```
- Project uses JWT-Token based authentication and Spring Security for authorization
## Contents
- Services
- Security
- Distributed Tracing System
- Logging and Exception Management
- Api Documentation 
- Setup and Run The Project
## Services
#### auth-service
- Used to create and authenticate user and initialize user's basket.
- Generates Jwt-Token and returns it.
#### user-service
- Used to handle user operations.
- Has two roles: ```USER``` and ```ADMIN```.
- User can not perform any operations on other user's information.
- Admin has authority to view and update on any user.
#### product-service
- Used to handle product operations.
- Only admin can perform create, update, delete operations.
- User has only get and update via order id authorizations
#### basket-service
- Used to handle basket operations
- Every user has an initialized basket while registered.
- User can add product to basket, delete from basket , get it's basket and clear the basket.
- Admin can get, update any basket and create basket.
#### order-service
- Used to handle order operations.
- Order has three status: ```NEW_ORDER```, ```APPROVED``` and ```DENIED```.
- User can get and create its own order.
- Admin can get, approve or deny orders.
## Security
- Project includes Jwt-Token and Spring Security based security.
- GateWay filter first gets request, validates and sends the information of authenticated-user to target services with header.
- Every service also has its own security which takes token and user information sets authentication if valid.
## Distributed Tracing System
- Project uses ```Zipkin``` and ```Micrometer``` to track requests.
- Requests can be tracked by setting up zipkin (check: https://zipkin.io/pages/quickstart.html) and can be monitored via zipkin ui: http://localhost:9411
## Logging and Exception Management
- Used ```SLF4J``` framework for logging.
- Added if cases for certain situations and returned appropriate HttpStatus codes.
- Exceptions will be caught and logged during running of the project.
## Api Documentation
- Used ```Open-Api``` and ```Swagger-ui``` for documentation.
- To see the documentation, access swagger-ui on gateway: http://localhost:9000/swagger-ui.html, 9000 here is the port of the gateway, you can customize it for yourself.
- Project also has a Postman collection that can be found under "Postman Collections"
## Setup and Run the Project
- Clone the repository 
- Run mvn clean install 
- set up your database properties in .yml files of the services
- run the services 
- You can now access the api via endpoints
