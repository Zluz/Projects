#!/usr/bin/env python

# from
# http://shallowsky.com/blog/programming/python-read-characters.html
print "WARNING - may mess up terminal (keys may not echo)..."

import sys, os
import termios, fcntl
import select

fd = sys.stdin.fileno()
newattr = termios.tcgetattr(fd)
newattr[3] = newattr[3] & ~termios.ICANON
newattr[3] = newattr[3] & ~termios.ECHO
termios.tcsetattr(fd, termios.TCSANOW, newattr)

oldterm = termios.tcgetattr(fd)
oldflags = fcntl.fcntl(fd, fcntl.F_GETFL)
fcntl.fcntl(fd, fcntl.F_SETFL, oldflags | os.O_NONBLOCK)

print "Type some stuff"
while True:
    inp, outp, err = select.select([sys.stdin], [], [])
    c = sys.stdin.read()
    if c == 'q':
        break
    print "-", c

# Reset the terminal:
termios.tcsetattr(fd, termios.TCSAFLUSH, oldterm)
fcntl.fcntl(fd, fcntl.F_SETFL, oldflags)

