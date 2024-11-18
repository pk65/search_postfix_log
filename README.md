[![Scala CI](https://github.com/pk65/search_postfix_log/actions/workflows/scala.yml/badge.svg)](https://github.com/pk65/search_postfix_log/actions/workflows/scala.yml)

# search_mail

It searches for emails in Postfix log file and full requests related to provided email.

## Run application

```shell
sbt run
```

## Run tests

```shell
sbt test
```

## Build package

```shell
sbt "test; Universal / packageBin"
```

Show parameters

```shell
$ ./search_postfix_log-0.1.0-SNAPSHOT/bin/search_postfix_log -h
scopt 4.1.0
Usage: scopt [options]
  -e, --email <value>   mandatory user email
  -i, --input <value>   input is Postfix log file [.gz] (plain text or gzipped text)
  -o, --output <value>  output is filtered log file
  -h, --help            print help message and exit
  -v, --verbose         verbose output
  -d, --debug           debug output
```
