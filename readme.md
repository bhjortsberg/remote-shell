
# Remote Shell


This is a simple remote shell. When starting the program a connected client can
run commands on the host.

    $ java RemoteShell 
    Usage:
        RemoteShell <port> <command>

        port:		Listen port
        command:	Command to execute when client
                    connect, default: bash

## Build

    $ javac RemoteShell.java

## Example usage

Start the remote shell listening on port 333 and send the output of `uptime`
to a connected client

    $ java RemoteShell 3333 uptime

Connect with a client

    $ netcat localhost 3333
     22:26:30 up 9 days, 14:31,  1 user,  load average: 1.07, 1.03, 1.09

Start as a real remote shell listening on port 3333 allowing a connected client
to send commands to bash and receiving the output

    $ java RemoteShell 3333

Connect with a client and type some commands:

    $ netcat localhost 3333

    netstat -nap|grep 3333
    tcp        0      0 127.0.0.1:56330         127.0.0.1:3333          ESTABLISHED 19887/netcat
    tcp6       0      0 :::3333                 :::*                    LISTEN      19869/java
    tcp6       0      0 127.0.0.1:3333          127.0.0.1:56330         ESTABLISHED 19869/java

    ps
      PID TTY          TIME CMD
    13927 pts/6    00:00:03 vim
    19333 pts/6    00:00:01 vim
    19869 pts/6    00:00:00 java
    19888 pts/6    00:00:00 bash
    19897 pts/6    00:00:00 ps
    26171 pts/6    00:00:02 bash

    uptime
     22:35:29 up 9 days, 14:40,  1 user,  load average: 2.22, 1.58, 1.24

    exit

    $

