[uwsgi]
#plugin = python
http-socket = 0.0.0.0:80
master = 1
module = apat:app
die-on-term = 1
buffer-size = 32768
enable-threads = 1
listen = 100
threads = 100
processes = 100
pidfile = /var/run/apat.pid
chdir = /opt/dd-collector
