#!/usr/bin/env python

# S2 communication script for Pimoroni Automation HAT
# experimental/python/003__AutomationHAT_test_001__key_input.py


# key input code from:
# https://github.com/akkana/scripts/blob/master/keyreader.py
# this key reader works well


import sys
import os
import termios, fcntl
import select

import time
import json

import automationhat


# from pathlib import Path


class KeyReader :
    '''
    Read keypresses one at a time, without waiting for a newline.
    echo: should characters be echoed?
    block: should we block for each character, or return immediately?
           (If !block, we'll return None if nothing is available to read.)
    '''
    def __init__(self, echo=False, block=True):
        '''Put the terminal into cbreak and noecho mode.'''
        self.fd = sys.stdin.fileno()

        self.block = block

        self.oldterm = termios.tcgetattr(self.fd)
        self.oldflags = fcntl.fcntl(self.fd, fcntl.F_GETFL)

        # Sad hack: when the destructor __del__ is called,
        # the fcntl module may already be unloaded, so we can no longer
        # call fcntl.fcntl() to set the terminal back to normal.
        # So just in case, store a reference to the fcntl module,
        # and also to termios (though I haven't yet seen a case
        # where termios was gone -- for some reason it's just fnctl).
        # The idea of keeping references to the modules comes from
        # http://bugs.python.org/issue5099
        # though I don't know if it'll solve the problem completely.
        self.fcntl = fcntl
        self.termios = termios

        newattr = termios.tcgetattr(self.fd)
        # tcgetattr returns: [iflag, oflag, cflag, lflag, ispeed, ospeed, cc]
        # where cc is a list of the tty special characters (length-1 strings)
        # except for cc[termios.VMIN] and cc[termios.VTIME] which are ints.
        self.cc_save = newattr[6]
        newattr[3] = newattr[3] & ~termios.ICANON
        if not echo:
            newattr[3] = newattr[3] & ~termios.ECHO

        if block and False:
            # VMIN and VTIME are supposed to let us do blocking reads:
            # VMIN is the minimum number of characters before it will return,
            # VTIME is how long it will wait if for characters < VMIN.
            # This is documented in man termios.
            # However, it doesn't work in python!
            # In Python, read() never returns in non-canonical mode;
            # even typing a newline doesn't help.
            cc = self.cc_save[:]   # Make a copy so we can restore VMIN, VTIME
            cc[termios.VMIN] = 1
            cc[termios.VTIME] = 0
            newattr[6] = cc
        else:
            # Put stdin into non-blocking mode.
            # We need to do this even if we're blocking, see above.
            fcntl.fcntl(self.fd, fcntl.F_SETFL, self.oldflags | os.O_NONBLOCK)

        termios.tcsetattr(self.fd, termios.TCSANOW, newattr)

    def __del__(self):
        '''Reset the terminal before exiting the program.'''
        self.termios.tcsetattr(self.fd, self.termios.TCSAFLUSH, self.oldterm)
        self.fcntl.fcntl(self.fd, self.fcntl.F_SETFL, self.oldflags)

    def getch(self):
        '''Read keyboard input, returning a string.
           Note that one key may result in a string of more than one character,
           e.g. arrow keys that send escape sequences.
           There may also be multiple keystrokes queued up since the last read.
           This function, sadly, cannot read special characters like VolumeUp.
           They don't show up in ordinary CLI reads -- you have to be in
           a window system like X to get those special keycodes.
        '''
        # Since we can't use the normal cbreak read from python,
        # use select to see if there's anything there:
        if self.block:
            inp, outp, err = select.select([sys.stdin], [], [])
        try:
            return sys.stdin.read()
        except IOError, e:
            # print "IOError:", e
            return None

def main():

    if automationhat.is_automation_hat():
        # print("Automation HAT detected.") # lib will print this anyway
        automationhat.light.power.write(1)
    else:
        print("Automation HAT not detected. Aborting.")
        return

    print("Allowable key input:")
    print("  1 - Select Relay 1")
    print("  2 - Select Relay 2")
    print("  3 - Select Relay 3")
    print("  4 - Select Digital Out 1")
    print("  5 - Select Digital Out 2")
    print("  6 - Select Digital Out 3")
    print("  + - Power ON the selected output")
    print("  - - Power OFF the selected output")
    print("  Q - Quit")



    file = False
    if len( sys.argv ) > 1:
        file = sys.argv[1]
        print "Monitoring file: ", file 
        monitor = True
    else:
        print("Starting KeyReader..")
        keyreader = KeyReader(echo=False, block=False)

    selected = ' '
    key = ' '


    print("Starting loop..")

    while True:
        print json.dumps( [ automationhat.input.read(), automationhat.analog.read(), selected ] )

        if file:
            key = ' '
            if ( os.path.exists( file ) ):
                print "# File (%s) found, reading.." % file
                with open( file, 'r' ) as input:
                    data=input.read().strip()
                print "# File contents: ", data
                if len( data )>1:
                    selected = data[0]
                    key = data[1]
                else:
                    print "# WARNING: Input file should be 2 characters."

                os.remove( file )

        else:
            key = keyreader.getch()

        if key == 'q':
            keyreader = None
            return
        elif key == '+':
            print("# Power ON %s" % selected)

            if selected == '1':
                automationhat.relay.one.write(1)
            elif selected == '2':
                automationhat.relay.two.write(1)
            elif selected == '3':
                automationhat.relay.three.write(1)
            elif selected == '4':
                automationhat.output.one.write(1)
            elif selected == '5':
                automationhat.output.two.write(1)
            elif selected == '6':
                automationhat.output.three.write(1)

        elif key == '-':
            print("# Power OFF %s" % selected)

            if selected == '1':
                automationhat.relay.one.write(0)
            elif selected == '2':
                automationhat.relay.two.write(0)
            elif selected == '3':
                automationhat.relay.three.write(0)
            elif selected == '4':
                automationhat.output.one.write(0)
            elif selected == '5':
                automationhat.output.two.write(0)
            elif selected == '6':
                automationhat.output.three.write(0)

        if key:
            # print("-%s-" % key)
            if key in "123456":
                selected = key
        # else:
            # print "None"

        time.sleep(0.1)


if __name__ == '__main__':
  main()
