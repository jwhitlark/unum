#! /usr/bin/env python
#   Copyright (c) Jason Whitlark. All rights reserved.

import pygtk
pygtk.require('2.0')

import gtk
import pynotify
import threading
import time
import dbus
import webbrowser
import subprocess
import multiprocessing
import socket
#import paramiko
#from couchdb.client import Server

import unum_tray

# Load daemon config
# exec(open('/home/jw/.unum/unumrc').read())

# #srv = Server('http://archive.local:5984')
# #hosts_db = srv['unum_hosts']

# # Remove localhost from hosts
# try:
#     del unum_hosts[socket.gethostname()]
# except:
#     pass

#Check if remote hosts can be resolved
# def check_hosts(unum_hosts):
#     """Check if hosts are available.
#     """
#     #thread this
#     for host in unum_hosts:
#         try:
#             unum_hosts[host] = socket.gethostbyname('%s.local' % host)
#         except:
#             pass
#     return unum_hosts



def get_unum_hosts():
    session_bus = dbus.SessionBus()
    hosts = session_bus.get_object('org.whitlark.unumd', '/org/whitlark/unumd/hosts')
    all_hosts = hosts.getAllHosts()
    live_hosts = hosts.getLiveHosts()
    return (all_hosts, live_hosts)

def get_playing_music_via_dbus():
    session_bus = dbus.SessionBus()
    rhythmbox = session_bus.get_object('org.gnome.Rhythmbox', '/org/gnome/Rhythmbox/Player')
    return rhythmbox.getPlayingUri()

current_host = socket.gethostname()
unum_hosts = get_unum_hosts()

# def main(unum_hosts):
#     unum_hosts = check_hosts(unum_hosts)
#     icon = gtk.status_icon_new_from_file("icon.png")
#     icon.set_tooltip("Unum Constellation Manager")
#     icon.connect('activate', activate_icon_cb)
#     icon.connect('popup-menu', popup_menu_cb, setup_popup_menu(icon, unum_hosts))
#     icon.set_visible(True)

#     # spin GTK off in a background thread
#     gtk.gdk.threads_init()
#     class gtkmain(threading.Thread):
#       def run(self):
#         gtk.main()
#     gtkmain().start()

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
    unum_tray.icon_path = icon_path
    unum_tray.help_path = help_path
    unum_tray.main(unum_hosts, True)
    #print create_synerge_conf(unum_hosts, 'godel', 'left')
    # import time
    # while True:
    #     print get_pointer_loc()
    #     time.sleep(.1)
