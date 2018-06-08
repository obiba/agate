[![Build Status](https://travis-ci.org/obiba/agate.svg?branch=master)](https://travis-ci.org/obiba/agate)

# OBiBa acknowledgments

If you are using OBiBa software, please cite our work in your code, websites, publications or reports.

"The work presented herein was made possible using the OBiBa suite (www.obiba.org), a  software suite developed by Maelstrom Research (www.maelstrom-research.org)"

# Agate

## For developers

Install NodeJS, Grunt and Bower

```
sudo add-apt-repository -y ppa:chris-lea/node.js
sudo apt-get install -y nodejs
sudo npm install -g grunt-cli bower
```

If you run agate for the first time, run `make npm-install`.

Make sure you use **Java 8**:

```
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

During development, run

* `make all drop-mongo run` in one terminal to start a fresh empty agate REST server on [HTTP port 8081](http://localhost:8081) or [HTTPS port 8444](https://localhost:8444)
* `make grunt` in another terminal to start Grunt server with live reload on port **9000**

See `make help` for other targets.



## Download

See [download instructions](http://www.obiba.org/pages/products/agate/#download).

## Documentation

See [documentation](http://agatedoc.obiba.org).

## Bug tracker

Have a bug or a question? Please create an issue on [GitHub](https://github.com/obiba/agate/issues).

## Continuous integration

See [Travis](https://travis-ci.org/obiba/agate).

## Mailing list

Have a question? Ask on our mailing list!

obiba-users@googlegroups.com

[http://groups.google.com/group/obiba-users](http://groups.google.com/group/obiba-users)


## License

OBiBa software are open source and made available under the [GPL3 licence](http://www.obiba.org/node/62). OBiBa software are free of charge.
