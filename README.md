# What is it?

Unum is conceived of as a platform/management layer for ALL your computers, physical or virtual.  It depends on N2N, a peer-to-peer VPN, (although it could be easily changed to work with any VPN) .  On top of your vpn network, it configures a message bus (Apache ActiveMQ) and a data store (Apache CouchDB) to allow plugins/apps to work across any/all of your machines.

# Current state

Very alpha.  ATM, it does not fully configure the message bus, nor do more than the minimum with the data store.

# Installation for use/testing (Ubuntu)

## Install n2n_edge from http://unum.whitlark.org/~jw or compile it yourself after cloning http://github.com/jwhitlark/N2N  Note that my version is currently out of date; you might be better just using the original from www.ntop.org (requires svn).  There is version 1 in the Ubuntu repositories, but that's not recommended.
Then you'll need to configure it and run it with sudo.  Something like

>
> sudo edge -E -r -u nobody -g nogroup -a <<unum ip address>> -c <<your-unum-network-name>> -k <<your-passcode>> -l unum.whitlark.org:10177 -d n2n0
>

You can choose the values for above.  Probably pick something in the 10.*.*.* range, and whatever you want for your constellation name and passcode.  I'm leaving unum.whitlark.org open as a n2n supernode for the moment, but I reserve the right to close it if it starts costing me too much.

## install couchdb and start, if not already running

>
> sudo apt-get install couchdb couchdb-bin python-couchdb
>

http://wiki.apache.org/couchdb/Installing_on_Ubuntu

## Install other dependencies

>
> sudo apt-get install libnotify-bin
>

# Hacking
TODO
