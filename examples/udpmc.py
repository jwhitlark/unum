import socket

MCAST_GRP = '224.1.1.1'
MCAST_PORT = 5007

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
sock.bind(("0.0.0.0", 5007))
sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 255)
sock.sendto("robot", (MCAST_GRP, MCAST_PORT))
