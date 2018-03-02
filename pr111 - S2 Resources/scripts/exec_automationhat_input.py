#!/usr/bin/env python

import time
import json

import automationhat


if automationhat.is_automation_hat():
    automationhat.light.power.write(1)

while True:
    # print( automationhat.input.read(), automationhat.analog.read() )
    print json.dumps( [ automationhat.input.read(), automationhat.analog.read() ] )
    time.sleep(0.1)
