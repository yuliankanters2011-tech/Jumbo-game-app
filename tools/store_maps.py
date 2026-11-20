"""Create two offline, north-up 1024px crops of Kadaster's public BRT map."""
import math, io, pathlib, urllib.request, urllib.parse, time
from PIL import Image
out = pathlib.Path('docs/maps'); out.mkdir(exist_ok=True)
for name, lat, lon in [('dongen',51.627785,4.942128),('breda',51.594238,4.747577)]:
    n=256*2**16
    left=round((lon+180)/360*n)-512
    top=round((1-math.asinh(math.tan(math.radians(lat)))/math.pi)*n/2)-512
    canvas=Image.new('RGB',(1024,1024))
    for x in range(left//256,(left+1023)//256+1):
        for y in range(top//256,(top+1023)//256+1):
            url='https://service.pdok.nl/brt/achtergrondkaart/wmts/v2_0?'+urllib.parse.urlencode(dict(SERVICE='WMTS',REQUEST='GetTile',VERSION='1.0.0',LAYER='standaard',STYLE='default',FORMAT='image/png',TILEMATRIXSET='EPSG:3857',TILEMATRIX='16',TILEROW=y,TILECOL=x))
            for attempt in range(3):
                try:
                    with urllib.request.urlopen(url,timeout=20) as res: tile=Image.open(io.BytesIO(res.read())).convert('RGB')
                    assert tile.size==(256,256);break
                except Exception:
                    if attempt==2:raise
                    time.sleep(2)
            canvas.paste(tile,(x*256-left,y*256-top))
    canvas.save(out/(name+'.png'),optimize=True)
