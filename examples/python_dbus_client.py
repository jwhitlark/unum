#! /usr/bin/env python

import dbus

bus = dbus.SessionBus()

helloservice = bus.get_object('org.whitlark.hello', '/org/whitlark/hello')

hello = helloservice.get_dbus_method('hello', 'org.whitlark.hello')

print hello()
