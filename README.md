### Prerequisites:
1. Make sure that you have `java 8` installed. Verify it by running (Output should be similar to `javac 1.8.0_211`)
```
$ javac -version    
```
2. Make sure that you have `maven` installed. Verify it by running (Output should be similar to `Apache Maven 3.6.1`)
```
$ mvn -version    
```

### Run
1. Build with `maven`
```
$ mvn clean install -U
```
2. Run tests with `maven`
```
$ mvn test
```
3. Run transaction analyser with `maven`
```
$ cd target
$ java -jar transactions-analyser.jar "ACC334455" "20/10/2018 12:00:00" "20/10/2018 19:00:00"
```

### Note
1. When running an application, `.csv` data file `transactions.csv` should be located in the same directory as `transactions-analyser.jar` executable `.jar`
