(import '(org.freedesktop.dbus DBusConnection DBusInterface)
	'(org.freedesktop DBus))
(def bus (. DBusConnection (getConnection (DBusConnection/SESSION))))
(.requestBusName bus "org.whitlark.test")
(.exportObject bus "/org/whitlark/Test/foo" (proxy [DBus] []
					      (foo
					       []
					       "This is a test")
					      (isRemote
					       []
					       true)))

(loop [] (recur))
