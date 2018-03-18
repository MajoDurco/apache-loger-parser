# Apache access log parser writen in Scala

First pull docker image containing the code with ready environment:
```sh
$ docker pull majodurco/apache-scala-log
```
Run the image:
```sh
$ docker run -it -p 8080:80 --rm majodurco/apache-scala-log:latest 
```
Now inside container:
```sh
  $ cd log
```
### Run the code:
```sh
$ sbt "run /var/log/apache2/access.log"
```
### Run the tests: 
```sh
$ sbt test
```
Implementation is located in:
`/root/log/src/main/scala/example`

Test are in:
`/root/log/src/test/scala/example`
