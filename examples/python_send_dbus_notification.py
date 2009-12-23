#! /usr/bin/env python

import dbus


bus = dbus.SessionBus()
#bus.get_object("org.freedesktop.DBus","/org/freedesktop/DBus")
notifyService = bus.get_object('org.freedesktop.Notifications', '/org/freedesktop/Notifications')
interface = dbus.Interface(notifyService, 'org.freedesktop.Notifications')
x = ("My App",0,'/usr/share/icons/gnome/scalable/categories/stock_internet.svg',"A Test","This is the test msg.",[],{},-1)
for m in x:
    print m, type(m)
apply(interface.Notify, x)


# http://localhost:9880/dbus/session/call/org.gnome.Tomboy.RemoteControl/ListAllNotes/?service=org.gnome.Tomboy&path=/org/gnome/Tomboy/RemoteControl
# http://localhost:9880/dbus/session/call/org.freedesktop.Notifications/Notify/"My App"/0/''/"A Test"/"This is the test msg."/?service=org.freedesktop.Notifications&path=/org/freedesktop/Notifications
