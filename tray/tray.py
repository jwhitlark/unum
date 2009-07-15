#! /usr/bin/env python

import pygtk
pygtk.require('2.0')
import gtk
import pynotify
import threading
import time
import dbus
import webbrowser
import subprocess
import socket

icon_path = "file://home/jw/dev/unum/tray/icon.png"
help_path = "/home/jw/dev/unum/tray/help.html"

unum_machines = ['archive', 'godel', 'utopia', 'jwhitlark-ip-dt']
try:
    unum_machines.remove(socket.gethostname())
except:
    pass

pynotify.init("unum")

def help_cb(widget, data=None):
    webbrowser.open(help_path)

def quit_cb(widget, data = None):
    if data:
        data.set_visible(False)
    gtk.main_quit()

def build_ssh_callback(host):
    def callback(widget, data=None):
        class callback_thread(threading.Thread):
            def run(self):
                subprocess.call(['gnome-terminal', '-e', 'ssh %s.local' % host])
        callback_thread().start()
    return callback

def popup_menu_cb(widget, button, time, data = None):
    if button == 3:
        if data:
            data.show_all()
            data.popup(None, None, None, 3, time)
    pass

def activate_icon_cb(widget, data = None):
    msgBox = gtk.MessageDialog(parent = None, buttons = gtk.BUTTONS_OK, message_format = "StatusIcon test.")
    msgBox.run()
    msgBox.destroy()

def setup_popup_menu(icon):
    menu = gtk.Menu()
    for machine in unum_machines:
        submenu_item = gtk.MenuItem(machine)
        if machine == 'godel':
            submenu_item.set_sensitive(False)
        menu.append(submenu_item)
        submenu = gtk.Menu()
        menuItem = gtk.MenuItem("SSH to this machine")
        menuItem.connect('activate', build_ssh_callback(machine))
        submenu.append(menuItem)
        submenu_item.set_submenu(submenu)

    sep = gtk.SeparatorMenuItem()
    menu.append(sep)
    menuItem = gtk.ImageMenuItem(gtk.STOCK_HELP)
    menuItem.connect('activate', help_cb)
    menu.append(menuItem)
    menuItem = gtk.ImageMenuItem(gtk.STOCK_ABOUT)
    menuItem.connect('activate', activate_icon_cb)
    menu.append(menuItem)
    menuItem = gtk.ImageMenuItem(gtk.STOCK_QUIT)
    menuItem.connect('activate', quit_cb, icon)
    menu.append(menuItem)
    return menu

def simple_msg(title, msg):
    m = pynotify.Notification(title, msg, icon_path)
    m.show()

def get_playing_music_via_dbus():
    session_bus = dbus.SessionBus()
    rhythmbox = session_bus.get_object('org.gnome.Rhythmbox', '/org/gnome/Rhythmbox/Player')
    return rhythmbox.getPlayingUri()

def simple_test():
    icon = gtk.status_icon_new_from_file("icon.png")
    icon.set_tooltip("Unum Constellation Manager")
    icon.connect('activate', activate_icon_cb)
    icon.connect('popup-menu', popup_menu_cb, setup_popup_menu(icon))
    icon.set_visible(True)

    # spin GTK off in a background thread
    gtk.gdk.threads_init()
    class gtkmain(threading.Thread):
      def run(self):
        gtk.main()
    gtkmain().start()

    #DBus test
    #print get_playing_music_via_dbus()

    # #Console test
    # for i in range(3):
    #     print "waiting"
    #     time.sleep(1)

    # #Icon test
    # icon.set_blinking(True)
    # #Message test
    # simple_msg("Changing State", "Switching to blinking")


    # for i in range(3):
    #     print "blinking"
    #     time.sleep(1)

    # #Icon test
    # icon.set_blinking(False)
    # #Message test
    # simple_msg("Changing State", "Switching to steady")

    #Quit test
    #gtk.main_quit()

if __name__ == '__main__':
    simple_test()
