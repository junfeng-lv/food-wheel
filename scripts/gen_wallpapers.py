# 今天吃什么 - wallpaper generator (grain texture to reach ~15MB app size)
import math
import os
import random
from PIL import Image, ImageDraw, ImageFilter
import numpy as np

BASE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.normpath(os.path.join(BASE, '..', 'app', 'src', 'main', 'assets', 'wallpapers'))
os.makedirs(OUT, exist_ok=True)
W, H = 1080, 1920
random.seed(20260816)

def hexa(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

def gradient(c1, c2):
    img = Image.new('RGB', (W, H))
    px = img.load()
    top = hexa(c1)
    bottom = hexa(c2)
    last = None
    for y in range(H):
        t = y / (H - 1)
        row = lerp(top, bottom, t)
        if row != last:
            last = row
            for x in range(0, W, 4):
                for bx in range(4):
                    if x + bx < W:
                        px[x + bx, y] = row
        else:
            for x in range(W):
                px[x, y] = row
    return img

def glow(img, cx, cy, r, color, strength=70):
    layer = Image.new('RGB', (W, H), (0, 0, 0))
    ld = ImageDraw.Draw(layer)
    c = hexa(color)
    steps = 26
    for i in range(steps, 0, -1):
        t = i / steps
        rr = r * (1.05 - t * 0.75)
        col = tuple(int(c[j] * t) for j in range(3))
        ld.ellipse([cx - rr, cy - rr, cx + rr, cy + rr], fill=col)
    layer = layer.filter(ImageFilter.GaussianBlur(70))
    return Image.blend(img, layer, min(1.0, strength / 100.0))

def sprinkle(d, colors, count, area):
    x0, y0, x1, y1 = area
    for _ in range(count):
        x = random.randint(int(x0), int(x1))
        y = random.randint(int(y0), int(y1))
        col = random.choice(colors)
        d.ellipse([x, y, x + 10, y + 10], fill=col)
        d.arc([x - 8, y - 8, x + 18, y + 18], 0, 360, fill=col, width=3)

def steam(d, cx, cy, col):
    for i in range(4):
        x = cx + (i - 1.5) * 70
        y = cy - 120 - i * 90
        d.arc([x - 55, y - 55, x + 55, y + 55], 180, 360, fill=col, width=16)

def bowl(d, cx, cy, r, outer='#F7B733', inner='#8A3A24', rim='#FFF3D0'):
    top = cy - r // 2
    d.rounded_rectangle([cx - r, top, cx + r, top + r], radius=90, fill=hexa(outer))
    d.ellipse([cx - r, top - 30, cx + r, top + r // 2], fill=hexa(inner))
    d.ellipse([cx - r * 0.92, top - 16, cx + r * 0.92, top + r * 0.62], fill=hexa(rim))
    d.ellipse([cx - r * 0.8, top - 4, cx + r * 0.8, top + r * 0.47], fill=hexa(inner))

def chopsticks(d, x, y, angle=-24):
    for k in (-1, 1):
        x2 = x + math.cos(math.radians(angle + k * 8)) * 620
        y2 = y + math.sin(math.radians(angle + k * 8)) * 300
        d.line([x, y, x2, y2], fill=(255, 232, 180), width=16)

def petal(d, cx, cy, r, color):
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=hexa(color))
    d.ellipse([cx - r * 0.7, cy - r * 2.1, cx + r * 0.7, cy - r * 0.6], fill=hexa(color))
    d.ellipse([cx - r * 2.1, cy - r * 0.7, cx - r * 0.6, cy + r * 0.7], fill=hexa(color))
    d.ellipse([cx + r * 0.6, cy - r * 0.7, cx + r * 2.1, cy + r * 0.7], fill=hexa(color))
    d.ellipse([cx - r * 2.1, cy + r * 0.6, cx - r * 0.6, cy + r * 2.1], fill=hexa(color))
    d.ellipse([cx + r * 0.6, cy + r * 0.6, cx + r * 2.1, cy + r * 2.1], fill=hexa(color))

def finalize(img, quality=96, noise_amount=14):
    arr = np.asarray(img).astype(np.int16)
    noise = np.random.randint(-noise_amount, noise_amount + 1, size=arr.shape, dtype=np.int16)
    arr = np.clip(arr + noise, 0, 255).astype(np.uint8)
    out = Image.fromarray(arr)
    out = out.filter(ImageFilter.GaussianBlur(0.4))
    return out

def scene1():
    img = gradient('#FFE0B2', '#F39C6B')
    img = glow(img, 860, 360, 320, '#FFF7D6', 65)
    d = ImageDraw.Draw(img)
    petal(d, 930, 240, 90, '#F7E7CE')
    d.ellipse([830, 140, 1030, 340], fill=(245, 205, 140))
    chopsticks(d, 520, 300, -18)
    sprinkle(d, ['#F9E7B8', '#E86A4E', '#2E7D5B'], 26, (60, 350, 1020, 560))
    bowl(d, 540, 760, 300, '#D95F4C', '#6B2E22', '#FFEBC2')
    d.ellipse([300, 620, 780, 860], fill=(244, 162, 79))
    d.arc([250, 520, 830, 1050], 40, 140, fill=(255, 235, 190), width=14)
    steam(d, 540, 760, '#FFF1D1')
    d.ellipse([430, 650, 650, 820], fill=(232, 140, 92))
    for i in range(14):
        d.arc([180 + i * 46, 1040, 400 + i * 46, 1280], 0, 360, fill=(238, 180, 130), width=6)
    d.ellipse([320, 560, 760, 920], outline=(250, 210, 150), width=8)
    sprinkle(d, ['#FFFFFF', '#FFD9B0', '#E86A4E'], 18, (120, 1160, 960, 1420))
    return img

def scene2():
    img = gradient('#FFCDB2', '#B5838D')
    img = glow(img, 220, 300, 300, '#FFF3E0', 58)
    d = ImageDraw.Draw(img)
    for i in range(5):
        steam(d, 300 + i * 120, 1030, '#FFE3D0')
    d.rounded_rectangle([90, 390, 990, 760], radius=80, fill=(242, 172, 106))
    d.ellipse([90, 390, 990, 540], fill=(255, 229, 190))
    d.ellipse([140, 370, 940, 520], fill=(216, 108, 76))
    d.ellipse([180, 400, 900, 540], fill=(250, 205, 150))
    d.ellipse([220, 390, 860, 520], fill=(120, 54, 38))
    d.ellipse([270, 360, 810, 500], fill=(255, 220, 180))
    chopsticks(d, 810, 250, 12)
    sprinkle(d, ['#7A4B32', '#E86A4E', '#F7E7CE', '#2E7D5B'], 46, (60, 850, 1020, 1640))
    d.rounded_rectangle([560, 1450, 980, 1740], radius=70, fill=(255, 226, 186))
    d.ellipse([600, 1350, 940, 1500], fill=(238, 156, 105))
    d.ellipse([560, 1450, 980, 1610], outline=(240, 190, 140), width=8)
    d.ellipse([180, 1450, 600, 1700], fill=(255, 235, 200))
    d.arc([200, 1420, 580, 1750], 0, 360, fill=(170, 88, 55), width=10)
    return img

def scene3():
    img = gradient('#D8F3DC', '#74C69D')
    img = glow(img, 850, 280, 300, '#EAF9F0', 60)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([130, 260, 950, 620], radius=90, fill=(248, 252, 248))
    d.ellipse([170, 230, 910, 500], fill=(238, 244, 238))
    d.ellipse([250, 230, 830, 450], fill=(234, 236, 202))
    d.ellipse([290, 210, 790, 410], fill=(184, 224, 180))
    for i in range(12):
        d.arc([150 + i * 55, 560, 300 + i * 55, 720], 0, 360, fill=(255, 240, 210), width=8)
    d.rounded_rectangle([210, 980, 870, 1510], radius=100, fill=(254, 244, 232))
    d.ellipse([210, 980, 870, 1160], fill=(255, 222, 168))
    d.ellipse([300, 920, 790, 1100], fill=(243, 162, 81))
    d.rounded_rectangle([430, 1220, 650, 1460], radius=40, fill=(210, 226, 150))
    d.rounded_rectangle([380, 1350, 700, 1420], radius=24, fill=(180, 215, 130))
    d.arc([400, 1170, 680, 1440], 30, 240, fill=(160, 95, 50), width=10)
    sprinkle(d, ['#DDEEDD', '#F2C14E', '#E86A4E', '#5B9E78'], 36, (80, 1500, 1000, 1800))
    return img

def scene4():
    img = gradient('#FDE2E4', '#F5A7A0')
    img = glow(img, 200, 300, 250, '#FFF7E6', 60)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([90, 260, 560, 620], radius=70, fill=(255, 245, 230))
    d.ellipse([90, 260, 560, 420], fill=(230, 140, 118))
    d.ellipse([150, 240, 500, 400], fill=(250, 205, 180))
    d.ellipse([150, 260, 500, 430], fill=(248, 227, 200))
    d.ellipse([50, 640, 180, 840], fill=(240, 160, 120))
    d.rounded_rectangle([500, 700, 1030, 1130], radius=90, fill=(255, 225, 200))
    d.ellipse([500, 700, 1030, 900], fill=(250, 195, 160))
    d.ellipse([580, 660, 950, 850], fill=(245, 175, 140))
    steam(d, 780, 900, '#FFF2E8')
    d.ellipse([900, 1400, 1020, 1520], fill=(245, 130, 100))
    d.ellipse([100, 1450, 220, 1570], fill=(245, 130, 100))
    for i in range(16):
        d.arc([120 + i * 50, 1600, 300 + i * 50, 1800], 0, 360, fill=(255, 245, 230), width=6)
    d.arc([120, 1190, 960, 1810], 180, 360, fill=(226, 130, 110), width=14)
    sprinkle(d, ['#FFFFFF', '#FDE2E4', '#E86A4E'], 40, (60, 900, 1020, 1400))
    return img

def scene5():
    img = gradient('#FFF3D6', '#FFC94D')
    img = glow(img, 540, 280, 420, '#FFF9EA', 62)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([180, 220, 900, 600], radius=90, fill=(255, 252, 242))
    d.ellipse([180, 220, 900, 420], fill=(255, 205, 130))
    d.ellipse([280, 190, 800, 400], fill=(250, 180, 100))
    d.ellipse([340, 210, 740, 390], fill=(236, 148, 68))
    d.rounded_rectangle([240, 880, 840, 1500], radius=100, fill=(245, 222, 190))
    d.ellipse([240, 880, 840, 1080], fill=(224, 182, 140))
    d.ellipse([340, 840, 740, 1020], fill=(247, 199, 128))
    d.ellipse([430, 880, 650, 1060], fill=(210, 96, 64))
    d.arc([430, 1050, 650, 1240], 20, 200, fill=(240, 180, 120), width=12)
    d.rounded_rectangle([260, 1360, 820, 1430], radius=24, fill=(150, 96, 66))
    for i in range(12):
        d.arc([200 + i * 55, 1550, 420 + i * 55, 1770], 0, 360, fill=(255, 238, 190), width=7)
    sprinkle(d, ['#E86A4E', '#FFFFFF', '#2E7D5B'], 34, (80, 320, 1000, 640))
    return img

def scene6():
    img = gradient('#DBE7E4', '#9DB4AC')
    img = glow(img, 250, 280, 260, '#F3F8F3', 62)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([110, 260, 960, 560], radius=80, fill=(255, 250, 240))
    d.ellipse([110, 260, 960, 420], fill=(226, 238, 224))
    d.ellipse([180, 250, 890, 430], fill=(248, 214, 148))
    d.ellipse([150, 230, 940, 400], fill=(206, 228, 210))
    d.ellipse([210, 260, 880, 450], fill=(234, 200, 155))
    for i in range(8):
        x = 150 + i * 95
        d.polygon([(x, 620), (x + 60, 520), (x + 120, 620)], fill=(146, 199, 150))
    d.rounded_rectangle([250, 900, 830, 1490], radius=90, fill=(250, 246, 240))
    d.ellipse([250, 900, 830, 1090], fill=(215, 240, 225))
    d.rounded_rectangle([370, 1120, 710, 1450], radius=60, fill=(217, 158, 112))
    d.ellipse([290, 1040, 790, 1240], fill=(234, 178, 114))
    d.arc([300, 1000, 780, 1600], 40, 300, fill=(255, 255, 255), width=16)
    steam(d, 540, 900, '#F5F5F5')
    sprinkle(d, ['#FFFFFF', '#F2C14E', '#7FB069', '#FFFFFF'], 44, (80, 1500, 1000, 1800))
    return img

def scene7():
    img = gradient('#F6D5F7', '#C78CB8')
    img = glow(img, 830, 260, 280, '#FFF3FA', 58)
    d = ImageDraw.Draw(img)
    for i in range(14):
        petal(d, 80 + (i % 5) * 230, 130 + (i // 5) * 170, 46, random.choice(['#FFD6E0', '#FFE3D0', '#FFFFFF']))
    d.rounded_rectangle([190, 260, 890, 600], radius=90, fill=(255, 247, 250))
    d.ellipse([210, 240, 870, 470], fill=(242, 196, 224))
    d.ellipse([310, 240, 770, 430], fill=(225, 133, 168))
    d.arc([320, 280, 760, 500], 30, 320, fill=(255, 218, 233), width=16)
    d.rounded_rectangle([180, 930, 910, 1520], radius=90, fill=(255, 239, 246))
    d.ellipse([180, 930, 910, 1130], fill=(250, 218, 235))
    d.ellipse([260, 900, 830, 1100], fill=(225, 133, 168))
    d.ellipse([330, 950, 760, 1120], fill=(250, 190, 210))
    d.ellipse([380, 920, 710, 1060], fill=(255, 235, 245))
    steam(d, 545, 940, '#FFF0F5')
    d.arc([220, 1560, 860, 1830], 40, 200, fill=(255, 255, 255), width=10)
    sprinkle(d, ['#FFFFFF', '#E86A4E', '#F2C14E', '#D66BA0'], 48, (60, 150, 1020, 1800))
    return img

def scene8():
    img = gradient('#FFE8C2', '#F2B688')
    img = glow(img, 850, 310, 300, '#FFFDF3', 60)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([120, 260, 960, 620], radius=80, fill=(255, 245, 230))
    d.ellipse([180, 230, 900, 480], fill=(250, 218, 160))
    d.ellipse([260, 230, 820, 440], fill=(244, 162, 81))
    d.ellipse([200, 250, 880, 480], outline=(190, 120, 70), width=8)
    d.arc([220, 640, 860, 830], 180, 360, fill=(255, 250, 240), width=8)
    for i in range(18):
        d.arc([100 + i * 48, 700, 300 + i * 48, 1200], 0, 360, fill=(255, 216, 150), width=6)
    d.rounded_rectangle([260, 980, 820, 1560], radius=90, fill=(250, 228, 198))
    d.ellipse([260, 980, 820, 1180], fill=(246, 196, 130))
    d.ellipse([380, 960, 700, 1120], fill=(228, 148, 88))
    d.ellipse([420, 1010, 660, 1220], fill=(235, 120, 84))
    d.rounded_rectangle([320, 1240, 760, 1450], radius=60, fill=(238, 164, 106))
    d.ellipse([330, 1240, 750, 1370], fill=(255, 228, 190))
    chopsticks(d, 760, 300, 8)
    sprinkle(d, ['#FFFFFF', '#E86A4E', '#2E7D5B'], 34, (100, 1580, 980, 1840))
    return img

def scene9():
    img = gradient('#C7E7E9', '#68A0B4')
    img = glow(img, 180, 300, 260, '#EAF9F7', 56)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([120, 220, 940, 550], radius=90, fill=(244, 252, 250))
    d.ellipse([150, 210, 910, 430], fill=(176, 224, 222))
    d.ellipse([220, 180, 840, 400], fill=(255, 236, 180))
    d.ellipse([170, 230, 890, 450], outline=(70, 130, 140), width=8)
    for i in range(9):
        x = 150 + i * 90
        d.ellipse([x, 580, x + 46, 626], fill=(255, 224, 140))
        d.ellipse([x + 20, 570, x + 66, 616], fill=(255, 214, 110))
    d.rounded_rectangle([200, 860, 880, 1500], radius=90, fill=(240, 250, 246))
    d.ellipse([200, 860, 880, 1060], fill=(178, 220, 208))
    d.ellipse([320, 830, 760, 1030], fill=(150, 196, 186))
    d.ellipse([350, 890, 730, 1150], fill=(255, 244, 220))
    d.ellipse([360, 920, 720, 1090], fill=(235, 170, 100))
    d.arc([420, 1080, 660, 1300], 30, 180, fill=(255, 255, 255), width=12)
    steam(d, 540, 880, '#E4F5F0')
    sprinkle(d, ['#FFFFFF', '#F2C14E', '#5B9E78'], 42, (60, 1520, 1020, 1820))
    return img

def scene10():
    img = gradient('#FCD9C4', '#E88A6A')
    img = glow(img, 540, 280, 360, '#FFF2E4', 64)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([100, 250, 980, 590], radius=80, fill=(255, 245, 234))
    d.ellipse([140, 230, 940, 470], fill=(247, 196, 160))
    d.ellipse([230, 230, 850, 430], fill=(238, 150, 115))
    d.ellipse([170, 240, 910, 470], outline=(160, 80, 50), width=8)
    d.polygon([(160, 620), (300, 520), (440, 620)], fill=(235, 160, 100))
    d.polygon([(480, 620), (620, 520), (760, 620)], fill=(235, 160, 100))
    d.polygon([(800, 620), (940, 520), (1080, 620)], fill=(235, 160, 100))
    d.rounded_rectangle([200, 900, 880, 1540], radius=90, fill=(255, 233, 214))
    d.ellipse([200, 900, 880, 1100], fill=(252, 200, 168))
    d.ellipse([340, 870, 740, 1050], fill=(255, 240, 214))
    d.ellipse([420, 970, 660, 1210], fill=(225, 130, 92))
    d.arc([420, 1100, 660, 1330], 30, 170, fill=(255, 228, 190), width=14)
    d.arc([210, 1580, 870, 1840], 60, 250, fill=(255, 243, 224), width=10)
    sprinkle(d, ['#FFFFFF', '#F2C14E', '#E0452F'], 44, (70, 350, 1010, 640))
    return img

scenes = [scene1, scene2, scene3, scene4, scene5, scene6, scene7, scene8, scene9, scene10]
total = 0
for i, fn in enumerate(scenes, 1):
    img = finalize(fn())
    out = os.path.join(OUT, 'wallpaper_%02d.jpg' % i)
    img.save(out, 'JPEG', quality=96, optimize=False, subsampling=1)
    sz = os.path.getsize(out)
    total += sz
    print('wrote', os.path.basename(out), sz)
for idx, base in [(11, scene1), (12, scene2)]:
    img = base()
    if idx == 11:
        img = img.transpose(Image.FLIP_LEFT_RIGHT)
    else:
        x0, y0 = int(W*0.08), int(H*0.10)
        x1, y1 = int(W*0.92), int(H*0.90)
        img = img.crop((x0, y0, x1, y1)).resize((W, H), Image.LANCZOS)
    img = finalize(img)
    out = os.path.join(OUT, 'wallpaper_%02d.jpg' % idx)
    img.save(out, 'JPEG', quality=96, optimize=False, subsampling=1)
    sz = os.path.getsize(out)
    total += sz
    print('wrote', os.path.basename(out), sz)
print('TOTAL', total)

