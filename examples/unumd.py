#! /usr/bin/env python

# Include msg passing code.


from SimpleXMLRPCServer import SimpleXMLRPCServer, SimpleXMLRPCRequestHandler
from SocketServer import ForkingMixIn
from amqplib import client_0_8 as amqp

# Load daemon config
exec(open('/home/jw/.unum/unumrc').read())



# Connect to mq
def setup_mq():
    # Lifted from rabbits and warrens - re-read to understand what' going on.
    conn = amqp.Connection(host="archive.local:5672 ", userid="guest",
                           password="guest", virtual_host="/", insist=False)
    chan = conn.channel()

    chan.queue_declare(queue="po_box", durable=True,
                       exclusive=False, auto_delete=False)
    chan.exchange_declare(exchange="sorting_room", type="direct", durable=True,
                          auto_delete=False,)

    chan.queue_bind(queue="po_box", exchange="sorting_room",
                    routing_key="jason")

    # Don't do this, chan.basic_get returns None if no msgs
    # msg = chan.basic_get("po_box")
    # print msg.body
    # chan.basic_ack(msg.delivery_tag)

    def recv_callback(msg):
        print 'Received: ' + msg.body
    chan.basic_consume(queue='po_box', no_ack=True,
                       callback=recv_callback, consumer_tag="testtag")
    while True:
        chan.wait()
    chan.basic_cancel("testtag")



class unum_server(object):
    def echo(self, msg):
        return msg + msg

    # Notify isn't going to work for a daemon, can't get session bus without gtk
    # Looks like a local queue for notifications, expiring if no one is listening.
    # def notify(self, title, msg):
    #     simple_msg(title, msg)
    #     #Add notify call to msg queues
    #     return 0

class ForkingServer(ForkingMixIn, SimpleXMLRPCServer):
    pass

serveraddr = ('', 8765)
srvr = ForkingServer(serveraddr, SimpleXMLRPCRequestHandler)
srvr.register_instance(unum_server())
srvr.register_introspection_functions()
srvr.serve_forever()
