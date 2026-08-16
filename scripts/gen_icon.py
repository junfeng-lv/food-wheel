# Generate launcher icon (wheel + bowl emblem)
from PIL import Image, ImageDraw, ImageFilter
import os

root = os.path.dirname(os.path.abspath(__file__))
out = os.path.join(root, '..', 'app', 'src', 'main', 'res', 'drawable', 'ic_launcher.png')
S = 1024
img = Image.new('RGBA', (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
# rounded square
d.rounded_rectangle([10, 10, S - 10, S - 10], radius=220, fill=(250, 220, 180, 255))
# gradient-ish overlay by concentric circles
for i in range(16, 0, -1):
    t = i / 16
    col = (int(248 * (1 - t) + 238 * t), int(164 * (1 - t) + 98 * t), int(70 * (1 - t) + 52 * t), 255)
    r = int(500 * t / 16 + 24)
    d.ellipse([S/2 - r, S/2 - r + 120, S/2 + r, S/2 + r + 120], fill=col)
# white plate
d.ellipse([280, 380, 744, 844], fill=(255, 253, 247, 255))
d.ellipse([300, 400, 724, 824], fill=(255, 232, 202, 255))
# wheel wedge sectors around plate center
cx, cy = 512, 612
cols = [(247, 183, 51), (243, 111, 79), (89, 178, 166), (232, 104, 90), (127, 176, 105)]
for i, col in enumerate(cols):
    start = i * 72
    d.pieslice([320, 420, 704, 804], start, start + 66, fill=col)
# center target
d.ellipse([480, 580, 544, 644], fill=(255, 248, 224, 255))
d.ellipse([496, 596, 528, 628], fill=(224, 69, 47, 255))
# chopsticks
d.line([360, 300, 700, 160], fill=(210, 130, 70, 255), width=28)
d.line([420, 334, 760, 194], fill=(190, 100, 55, 255), width=26)
# steam
for x, y in [(430, 270), (560, 230)]:
    d.arc([x - 60, y - 60, x + 60, y + 60], 180, 360, fill=(255, 248, 230, 220), width=18)
img = img.resize((512, 512), Image.LANCZOS)
img.save(out, 'PNG')
print('icon', out, os.path.getsize(out))
