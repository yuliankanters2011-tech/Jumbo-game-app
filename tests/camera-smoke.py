"""Smoke-test the packaged APK on a connected emulator; never records camera frames."""
import re
import subprocess
import time
from xml.etree import ElementTree as ET


def adb(*args):
    return subprocess.check_output(['adb', *args], text=True)


def tap_matching(prefix, scroll=False, tap=True):
    tree = ET.Element('hierarchy')
    for attempt in range(15):
        try:
            adb('shell', 'uiautomator', 'dump', '/sdcard/window.xml')
            tree = ET.fromstring(adb('shell', 'cat', '/sdcard/window.xml'))
        except (subprocess.CalledProcessError, ET.ParseError):
            time.sleep(1)
            continue
        for node in tree.iter('node'):
            if any(prefix.lower() in node.get(k, '').lower() for k in ('text', 'content-desc')):
                x, y, r, b = map(int, re.findall(r'\d+', node.get('bounds')))
                if b > y and r > x:
                    if tap: adb('shell', 'input', 'tap', str((x+r)//2), str((y+b)//2))
                    return
        if scroll and attempt >= 1:
            adb('shell', 'input', 'swipe', '300', '800', '300', '440', '350')
        time.sleep(0.5)
    print('::error::'+adb('logcat','-b','crash','-d')[-2000:].replace('\n',' '))
    print('::error::Missing '+prefix+'; '+str([(n.get('text'),n.get('content-desc')) for n in tree.iter('node')])[:4000])
    raise AssertionError('Control missing: '+prefix+'; '+str([n.get('text') for n in tree.iter('node')]))


pkg = 'nl.jumbospeur.game'
adb('install', '-r', 'Jumbo-Speurclub.apk')

adb('shell', 'am', 'start', '-W', '-n', pkg+'/.Main')
tap_matching('Scanner', scroll=True)
tap_matching('Camera aanzetten')
# Exercise a real first-run grant instead of pre-granting permission with adb.
tap_matching('While using the app')
tap_matching('Camera actief', tap=False)
tap_matching('⚑ WINKEL')
tap_matching('Sluiten')
tap_matching('Barcode gebruiken')
assert adb('shell', 'pidof', pkg).strip()
print('Packaged APK: camera opens, post responds, scanner closes and manual entry remains available.')

# Restart the dashboard, check in offline and drive the actual emulator sensor.
adb('shell', 'am', 'force-stop', pkg)
adb('shell', 'am', 'start', '-W', '-n', pkg+'/.Main')
tap_matching('Kies een winkel')
tap_matching('Jumbo Dongen')
tap_matching('ik ben bij de winkel')
tap_matching('Start wandelmissie')
tap_matching('Loop rustig', tap=False)

def walk(count):
    for _ in range(count):
        adb('emu', 'sensor', 'set', 'acceleration', '0:0:0')
        time.sleep(1.2)
        adb('emu', 'sensor', 'set', 'acceleration', '0:20:0')
        time.sleep(1.2)
    adb('emu', 'sensor', 'set', 'acceleration', '0:0:0')

walk(4)
tap_matching('Volg de vloerpijl', tap=False)
walk(6)
tap_matching('Sta stil', tap=False)
tap_matching('Sluiten')
print('Walk encounter: real permission path, movement, floor arrow and shelf-search phases passed.')
