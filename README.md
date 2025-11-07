# Agate [![Build Status](https://app.travis-ci.com/obiba/agate.svg?branch=master)](https://app.travis-ci.com/github/obiba/agate)

Central authentication server for OBiBa applications.

* See [download instructions](http://www.obiba.org/pages/products/agate/#download).
* Read the [documentation](http://agatedoc.obiba.org).
* Have a bug or a question? Please create an issue on [GitHub](https://github.com/obiba/agate/issues).
* Continuous integration is on [Travis](https://travis-ci.org/obiba/agate).

## For developers

Make sure you use **Java 21**:

```
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

Or use [SDKMAN!](https://sdkman.io/).

During development, run in debug mode:

* `make all debug` in one terminal to start a fresh empty agate REST server on [HTTP port 8081](http://localhost:8081) or [HTTPS port 8444](https://localhost:8444)

See `make help` for other targets.

## Mailing list

Have a question? Ask on our mailing list!

obiba-users@googlegroups.com

[http://groups.google.com/group/obiba-users](http://groups.google.com/group/obiba-users)

## License

OBiBa software are open source and made available under the [GPL3 licence](http://www.obiba.org/node/62). OBiBa software are free of charge.

## OBiBa acknowledgments

If you are using OBiBa software, please cite our work in your code, websites, publications or reports.

"The work presented herein was made possible using the OBiBa suite (www.obiba.org), a  software suite developed by Maelstrom Research (www.maelstrom-research.org)"
