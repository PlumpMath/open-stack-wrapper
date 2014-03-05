# open-stack-wrapper

A Clojure library to work with open-stack rest api



# generating and using the java library
(requirements: Maven and Leiningen)

## Generating java library and installing in local maven repo



```
$ cd ew_snmp
$ lein clean
$ lein install
```

### Testing library from eclipse project (java code)

** Prior to test you'll need a login.properties file** in this directory `./open-stack-wrapper-test/src/test/resources/test/login.properties`

With these properties:
```
username=your-openstack-username
password=your-openstack-password
url=your-openstack-login-entrypoint(horizon)-include-port(http://8.21.28.222:5000)

```


### and with maven, test src/test/java/test/OpenStackTest
```
$ cd open-stack-wrapper-test
$ mvn clean
$ mvn test
```



## License

Copyright ©enterpriseweb.com 2014 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
