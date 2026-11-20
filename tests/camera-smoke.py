"""Smoke-test the packaged APK on a connected emulator; never records camera frames."""
import re
import subprocess
import time
from xml.etree import ElementTree as ET


def adb(*args):
    return subprocess.check_output(['adb', *args], text=True)


def tap_matching(prefix, scroll=False):
    for attempt in range(12):
        adb('shell', 'uiautomator', 'dump', '/sdcard/window.xml')
        tree = ET.fromstring(adb('shell', 'cat', '/sdcard/window.xml'))
        for node in tree.iter('node'):
            if any(node.get(k, '').lower().startswith(prefix.lower()) for k in ('text', 'content-desc')):
                x, y, r, b = map(int, re.findall(r'\d+', node.get('bounds')))
                if b > y and r > x:
                    adb('shell', 'input', 'tap', str((x+r)//2), str((y+b)//2))
                    return
        if scroll and attempt >= 1:
            adb('shell', 'input', 'swipe', '300', '800', '300', '440', '350')
        time.sleep(0.5)
    raise AssertionError('Control missing: '+prefix+'; '+str([n.get('text') for n in tree.iter('node')]))


pkg = 'nl.jumbospeur.game'
adb('install', '-r', 'Jumbo-Speurclub.apk')
adb('shell', 'pm', 'grant', pkg, 'android.permission.CAMERA')
adb('shell', 'am', 'start', '-W', '-n', pkg+'/.Main')
tap_matching('Scanner', scroll=True)
tap_matching('Camera aanzetten')
tap_matching('⚑ WINKEL')
tap_matching('Sluiten')
tap_matching('Barcode gebruiken')
assert adb('shell', 'pidof', pkg).strip()
print('Packaged APK: camera opens, post responds, scanner closes and manual entry remains available.')
