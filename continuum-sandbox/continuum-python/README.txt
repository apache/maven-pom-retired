* Files

 [[add-*]]: These scripts will add the respective projects to Continuum.

 [[cli.py]] is a small library for making interactive command line
            programs. It's used by continuum_cli.py

 [[continuum.py]] is a reusable library for interfacing with Continuum over
                  the XML-RPC interface. It's used by the integration tests.

 [[continuum_cli.py]] is a interactive command line interface to Continuum.
                      Start it with this command:
 ------------------------------------------------------------------------------
 $ python continuum_cli.py
 ------------------------------------------------------------------------------
                      and write "help" at the command prompt to list all
                      available commands.

 [[it.py]] is the integration tests. Se the next section for more information.

* Integration tests

These integration tests are executed by the it.py program. It assumes that you
have Continuum running on localhost, with the XML-RPC interface running on port
8000.

To run the tests execute the script like this:

$ python it.py
