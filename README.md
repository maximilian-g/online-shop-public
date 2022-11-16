<div align="center">
  <img src="https://download.logo.wine/logo/Spring_Framework/Spring_Framework-Logo.wine.png" width="200px">
  <h1>Online shop</h1>
</div>

<p align="center">
  This application is an example of a real online store.
  It demonstrates the use of the Spring Boot stack and its components (Spring core, Spring Web, Spring data JPA, Spring Security).
  This project is created in educational purposes.
</p>

## Main features

* JWT authentication & authorization;
* Integration with PayPal API;
* Configurable email notifications during registration;
* Forgot password feature;
* Rest API;
* Admin panel.


## Quick start

1 - Import `init.sql` file in desired schema. MySQL workbench should be used probably.

2 - Add paypal properties from `https://developer.paypal.com/` to application.properties

3 - Add paypal properties from `https://developer.paypal.com/` to application.properties

4 - Set property `app.email.confirmation` to false if you do not want to provide email for sending confirmation.

> **Note:** Some bugs may be present.

## Some useful URLs and endpoints

URL | Description
--- | ---
/ | Main(home) page
/login | Login page
/register | Registration page
/items | Page with catalog
/admin | Admin panel page
/api/v1/** | Rest API base url for all resources

> **Note:** Some URLs are prorected from role "USER".


> **TODO:** Extended description will be added later.
