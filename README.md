# search_mail

It searches for emails in Postfix log file and full requests related to provided email.

## Run application

Please use at least JDK 17.

```shell
sbt run
```

## Run tests

```shell
sbt test
```

## Build package

```shell
sbt "clean; compile; universal:packageBin"
```

Show parameters

```shell
$ ./search_postfix_log-0.1.0-SNAPSHOT/bin/search_postfix_log -h
scopt 4.1.0
Usage: scopt [options]
  -e, --email <value>   mandatory user email
  -i, --input <value>   input is Postfix log file
  -o, --output <value>  output is filteredd log file
  -h, --help            print help message and exit
  -v, --verbose         verbose output
  -d, --debug           debug output
```
