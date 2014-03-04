# open-stack-wrapper

A Clojure library to work with open-stack rest api



# generating and using the java library
(requirements: Maven and Leiningen)

**Generating java library and installing in local maven repo**

Pay attention on version library (written on project.clj file)

```
$ cd ew_snmp
$ lein clean
$ lein install
```

**Testing library from mvn test or eclipse project (java code)

Prior to test you'll need a login.properties file in this directory `./open-stack-wrapper-test/src/test/login.properties`

With these properties:
```
username=your-openstack-username
password=your-openstack-password
url=your-openstack-login-entrypoint(horizon)-include-port ex: http://8.21.28.222:5000

```


And then:...

```
$ cd open-stack-wrapper/open-stack-wrapper-test
$ mvn clean
$ mvn test

```

Or, directly from eclipse: ....
1. create new java project using specific location (locate dir "open-stack-wrapper-test")   
2. enable maven nature if eclipse doesn't automatically recognize It
3. locate OpenStackTest and run as JUnit test



## License

Copyright ©enterpriseweb.com 2014 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
