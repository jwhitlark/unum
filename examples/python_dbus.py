import gtk
import dbus
import dbus.service
import dbus.glib

class MyDBUSService(dbus.service.Object):
    def __init__(self):
        bus_name = dbus.service.BusName('org.whitlark.hello', bus=dbus.SessionBus())

        dbus.service.Object.__init__(self, bus_name, '/org/whitlark/hello')

    @dbus.service.method('org.whitlark.hello')
    def hello(self):
        print "got message:", self
        return "Hello,World!"

myservice = MyDBUSService()
gtk.main()
