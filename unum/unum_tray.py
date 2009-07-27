#! /usr/bin/env python

import pygtk
pygtk.require('2.0')

import gtk
import pynotify
import webbrowser
import threading
import subprocess
import multiprocessing
import webbrowser
import time
import socket

# import xmlrpclib

# srvr = xmlrpclib.ServerProxy('http://localhost:8765/')

pynotify.init("unum")

hosts = None


edges = {'left': 'right',
         'right': 'left',
         'up': 'down',
         'down': 'up'}



def create_synerge_conf(hosts, remote, edge):
    op_edge = edges[edge]
    output = []
    output.append('section: screens')
    output.append('\t%s:' % socket.gethostname())
    for machine in hosts:
        output.append('\t%s:' % machine)
    output.append('end')
    output.append('section: links')
    output.append('\t%s:' % socket.gethostname())
    output.append('\t\t%s = %s' % (edge, remote))
    output.append('\t%s:' % remote)
    output.append('\t\t%s = %s' % (op_edge, socket.gethostname()))
    output.append('end')
    return '\n'.join(output)



## ----------------- Callbacks
def help_cb(widget, data=None):
    webbrowser.open(help_path)

def quit_cb(widget, data = None):
    if data:
        data.set_visible(False)
    gtk.main_quit()

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


## -----------------   Not sure where this belongs...
def identify_cb(widget, data=None):
    simple_msg("Unum Identification", socket.gethostname())
    #srvr.notify('Identify machines', '%s\n(other machines TODO)' % socket.gethostname())

## ---- plugin callbacks
def synergy_cb(widget, data=None):
    # Kill running synergy
    subprocess.call(['pkill', 'synergy'])
    max_x, max_y = get_screen_size()
    while True:
        x, y = get_pointer_loc()
        if x == 0:
            server_screen_edge = 'left'
            break
        if y == 0:
            server_screen_edge = 'up'
            break
        if x == (max_x - 1):
            server_screen_edge = 'right'
            break
        if y == (max_y - 1):
            server_screen_edge = 'down'
            break
        time.sleep(.05)
    simple_msg("Edge selected", server_screen_edge)
    remote_edge = edges[server_screen_edge]
    # generate file
    #FIXME: using global for hosts
    #FIXME: remote host hard coded
    file_contents = create_synerge_conf(hosts, 'godel', server_screen_edge)
    with open("/home/jw/.unum/synergy.conf", 'w') as fh:
        fh.write(file_contents)
    # Start synergy
    class synergy_callback_thread(multiprocessing.Process):
        def run(self):
            subprocess.call(['synergys', '--config', '/home/jw/.unum/synergy.conf'])
    synergy_callback_thread().start()
    #TODO: this is the stupid way, use paramiko in the future
    # start remote synergy only if not running (or just kill it first)
    subprocess.call(['ssh', 'godel.local', 'pkill',  'synergy'])
    subprocess.call(['ssh', 'godel.local', 'nohup', 'synergyc', '%s.local' % socket.gethostname(), '&'])
    # Tell user it worked
    simple_msg("Synergy info", "local host:%s\nlocal edge:%s\nremote host:%s\nremote edge:%s" % (socket.gethostname(), server_screen_edge, 'unknown', remote_edge))

def build_ssh_callback(host):
    """Uses multiprocessing so that processes will survive death of the host.
    """
    def callback(widget, data=None):
        class ssh_callback_thread(multiprocessing.Process):
            def run(self):
                subprocess.call(['gnome-terminal', '-e', 'ssh %s.local' % host])
        ssh_callback_thread().start()
    return callback

def build_mount_callback(host):
    """Uses multiprocessing so that processes will survive death of the host.
    """
    def callback(widget, data=None):
        class mount_callback_thread(multiprocessing.Process):
            def run(self):
                pass
                ## FIXME: Ensure directory exists
                ## BROKEN
#                subprocess.call(['gnome-open', '-e', 'sshfs', ' %s.local:' % host, '/home/jw/unum/hosts/%s' % host])
#                subprocess.call(['gnome-open', '~/unum/hosts/%s' % host])
                ## FIXME: Change menu to allow unmount
        callback_thread().start()
    return callback
## --------------------- End Callbacks


def setup_popup_menu(icon, unum_hosts):
    global hosts
    hosts = unum_hosts
    menu = gtk.Menu()
    for machine in sorted(unum_hosts):
        submenu_item = gtk.MenuItem(machine)
        if not unum_hosts[machine]:
            submenu_item.set_sensitive(False)
        menu.append(submenu_item)
        submenu = gtk.Menu()
        menuItem = gtk.MenuItem("SSH to this machine")
        menuItem.connect('activate', build_ssh_callback(machine))
        submenu.append(menuItem)
        menuItem = gtk.MenuItem("Browse the web from this machine")
        menuItem.set_sensitive(False)
        submenu.append(menuItem)
        menuItem = gtk.MenuItem("Services hosted on this machine")
        menuItem.set_sensitive(False)
        submenu.append(menuItem)
        menuItem = gtk.MenuItem("Use synergy with this machine.")
        menuItem.connect('activate', synergy_cb)
#        menuItem.set_sensitive(False)
        submenu.append(menuItem)
        menuItem = gtk.MenuItem("Mount this machine's home directory")
        menuItem.set_sensitive(False)
        menuItem.connect('activate', build_mount_callback(machine))
        submenu.append(menuItem)

        submenu_item.set_submenu(submenu)

    sep = gtk.SeparatorMenuItem()
    menu.append(sep)
    menuItem = gtk.ImageMenuItem(gtk.STOCK_PROPERTIES)
    menuItem.connect('activate', identify_cb)
    menu.append(menuItem)
    menuItem = gtk.MenuItem("Configure IPython distributed environment")
    menuItem.set_sensitive(False)
    menu.append(menuItem)
    menuItem = gtk.MenuItem("Configure Erlang distributed environment")
    menuItem.set_sensitive(False)
    menu.append(menuItem)
    menuItem = gtk.MenuItem("Add EC2 Instances to the Constellation")
    menuItem.set_sensitive(False)
    menu.append(menuItem)

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


def get_screen_size():
    display = gtk.gdk.Display(None)
    screen = display.get_screen(0)
    y = screen.get_height()
    x = screen.get_width()
    return (x, y)

def get_pointer_loc():
    display = gtk.gdk.Display(None)
    a, x, y, b = display.get_pointer()
    return (x,y)

def main(unum_hosts):
    icon = gtk.status_icon_new_from_file("icon.png")
    icon.set_tooltip("Unum Constellation Manager")
    icon.connect('activate', activate_icon_cb)
    icon.connect('popup-menu', popup_menu_cb, setup_popup_menu(icon, unum_hosts))
    icon.set_visible(True)

    # spin GTK off in a background thread
    gtk.gdk.threads_init()
    class gtkmain(threading.Thread):
      def run(self):
        gtk.main()
    gtkmain().start()


if __name__ == '__main__':
    main(unum_hosts)


