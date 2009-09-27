#! /usr/bin/env python

import dbus

session_bus = dbus.SessionBus()
print session_bus
test_obj = session_bus.get_object('org.whitlark.test', '/org/whitlark/Test/foo')
print test_obj
print dir(test_obj)
print test_obj.foo()
# method = test_obj.get_dbus_method("foo")
# print dir(method)
# print method._method_name
# print method()
#print test_obj.foo()
