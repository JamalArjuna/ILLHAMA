#!/usr/bin/env python3
"""Membuat aset placeholder: tileset, sprite, ikon, panel, peta, efek suara, dan musik latar.

Jalankan dari folder paket:  python3 tools/gen_assets.py
Semua keluaran dilepas CC0 1.0 (domain publik). Kualitasnya sengaja sederhana (placeholder):
ganti dengan aset gratisan yang lebih bagus kapan saja, selama nama file dan ukuran tetap.
"""
import json
import math
import random
import wave
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ART = ROOT / "assets" / "art"
SFX = ROOT / "assets" / "sfx"
MAP = ROOT / "map"
for d in (ART, SFX, MAP):
    d.mkdir(parents=True, exist_ok=True)

T = 16  # ukuran tile
rng = random.Random(7)


def rgb(r, g, b, a=255):
    return (r, g, b, a)


CLEAR = (0, 0, 0, 0)
OUT = rgb(60, 40, 30)


def new(w, h, color=CLEAR):
    return Image.new("RGBA", (w, h), color)


def put(img, x, y, c):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), c)


def noise(img, ox, oy, base, alt, density):
    for y in range(T):
        for x in range(T):
            img.putpixel((ox + x, oy + y), alt if rng.random() < density else base)


# ---------------------------------------------------------------- tileset
GRASS = rgb(110, 170, 70)
GRASS_D = rgb(92, 152, 58)
GRASS_L = rgb(132, 190, 88)
WOOD = rgb(176, 128, 80)
WOOD_D = rgb(140, 96, 56)


def tile_origin(i):
    return (i % 4) * T, (i // 4) * T


def make_tileset():
    img = new(64, 64)
    d = ImageDraw.Draw(img)

    def grass(i):
        ox, oy = tile_origin(i)
        for y in range(T):
            for x in range(T):
                r = rng.random()
                c = GRASS_D if r < 0.12 else (GRASS_L if r < 0.2 else GRASS)
                img.putpixel((ox + x, oy + y), c)
        return ox, oy

    grass(0)
    ox, oy = grass(1)
    for fx, fy, col in [(3, 4, (255, 255, 255)), (11, 3, (250, 220, 80)), (8, 11, (240, 130, 170)), (13, 12, (255, 255, 255))]:
        put(img, ox + fx, oy + fy, rgb(*col))
        put(img, ox + fx + 1, oy + fy, rgb(*col))

    ox, oy = tile_origin(2)  # jalan
    noise(img, ox, oy, rgb(206, 180, 130), rgb(188, 162, 114), 0.18)

    ox, oy = tile_origin(3)  # tanah belum diolah
    noise(img, ox, oy, rgb(140, 98, 60), rgb(122, 84, 50), 0.2)

    for i, (base, line) in {4: (rgb(122, 82, 50), rgb(92, 58, 34)), 5: (rgb(84, 56, 38), rgb(58, 38, 28))}.items():
        ox, oy = tile_origin(i)
        for y in range(T):
            for x in range(T):
                img.putpixel((ox + x, oy + y), base)
        for y in (2, 6, 10, 14):
            for x in range(T):
                img.putpixel((ox + x, oy + y), line)
        if i == 5:  # kilau air
            for fx, fy in [(3, 4), (9, 8), (12, 12), (5, 13)]:
                put(img, ox + fx, oy + fy, rgb(110, 130, 170))

    # pagar horizontal (6) dan vertikal (7)
    ox, oy = grass(6)
    for x in range(T):
        for y in (5, 6, 10, 11):
            put(img, ox + x, oy + y, WOOD)
        put(img, ox + x, oy + 7, WOOD_D)
        put(img, ox + x, oy + 12, WOOD_D)
    for px in (1, 12):
        for y in range(3, 15):
            put(img, ox + px, oy + y, WOOD_D)
            put(img, ox + px + 1, oy + y, WOOD)
    ox, oy = grass(7)
    for y in range(T):
        for x in (5, 6, 9, 10):
            put(img, ox + x, oy + y, WOOD)
        put(img, ox + 7, oy + y, WOOD_D)
        put(img, ox + 11, oy + y, WOOD_D)
    for py in (1, 12):
        for x in range(3, 13):
            put(img, ox + x, oy + py, WOOD_D)
            put(img, ox + x, oy + py + 1, WOOD)

    # air (8)
    ox, oy = tile_origin(8)
    for y in range(T):
        for x in range(T):
            img.putpixel((ox + x, oy + y), rgb(70, 130, 200))
    for fx, fy in [(2, 3), (3, 3), (4, 3), (9, 7), (10, 7), (11, 7), (4, 12), (5, 12), (6, 12), (13, 2), (12, 13)]:
        put(img, ox + fx, oy + fy, rgb(130, 180, 232))

    # dinding (9)
    def wall(i):
        ox, oy = tile_origin(i)
        for y in range(T):
            for x in range(T):
                img.putpixel((ox + x, oy + y), WOOD)
        for x in (0, 4, 8, 12):
            for y in range(T):
                put(img, ox + x, oy + y, WOOD_D)
        for y in range(T):
            put(img, ox + 15, oy + y, WOOD_D)
        return ox, oy

    wall(9)

    # atap (10)
    ox, oy = tile_origin(10)
    for y in range(T):
        for x in range(T):
            img.putpixel((ox + x, oy + y), rgb(178, 70, 56))
    for y in (3, 7, 11, 15):
        for x in range(T):
            put(img, ox + x, oy + y, rgb(130, 46, 38))
    for row, y0 in enumerate((0, 4, 8, 12)):
        off = 0 if row % 2 == 0 else 4
        for x in range(off, T, 8):
            for y in range(y0, y0 + 3):
                put(img, ox + x, oy + y, rgb(150, 56, 46))

    # pintu (11)
    ox, oy = wall(11)
    for y in range(2, T):
        for x in range(4, 12):
            put(img, ox + x, oy + y, rgb(104, 66, 38))
    for x in range(4, 12):
        put(img, ox + x, oy + 2, OUT)
    for y in range(2, T):
        put(img, ox + 4, oy + y, OUT)
        put(img, ox + 11, oy + y, OUT)
    put(img, ox + 9, oy + 9, rgb(240, 200, 80))
    put(img, ox + 9, oy + 10, rgb(240, 200, 80))

    # pohon (12)
    ox, oy = grass(12)
    for y in range(10, 16):
        for x in (7, 8):
            put(img, ox + x, oy + y, rgb(110, 76, 44))
    for y in range(T):
        for x in range(T):
            if (x - 8) ** 2 + (y - 7) ** 2 <= 36:
                shade = rgb(46, 120, 60)
                if (x - 6) ** 2 + (y - 5) ** 2 <= 8:
                    shade = rgb(72, 152, 82)
                elif (x - 10) ** 2 + (y - 10) ** 2 <= 8:
                    shade = rgb(34, 96, 48)
                img.putpixel((ox + x, oy + y), shade)

    # papan toko (13)
    ox, oy = wall(13)
    for y in range(4, 12):
        for x in range(2, 14):
            put(img, ox + x, oy + y, rgb(240, 220, 170))
    for x in range(2, 14):
        put(img, ox + x, oy + 4, OUT)
        put(img, ox + x, oy + 11, OUT)
    for y in range(4, 12):
        put(img, ox + 2, oy + y, OUT)
        put(img, ox + 13, oy + y, OUT)
    d.ellipse((ox + 5, oy + 5, ox + 10, oy + 10), fill=rgb(240, 190, 50), outline=rgb(160, 110, 20))

    # jendela (14)
    ox, oy = wall(14)
    for y in range(4, 12):
        for x in range(4, 12):
            put(img, ox + x, oy + y, rgb(170, 220, 240))
    for k in range(4, 12):
        put(img, ox + k, oy + 4, OUT)
        put(img, ox + k, oy + 11, OUT)
        put(img, ox + 4, oy + k, OUT)
        put(img, ox + 11, oy + k, OUT)
        put(img, ox + 7, oy + k, WOOD_D)
        put(img, ox + k, oy + 7, WOOD_D)

    # shipping bin (15)
    ox, oy = grass(15)
    for y in range(5, 15):
        for x in range(2, 14):
            put(img, ox + x, oy + y, WOOD)
    for x in range(2, 14):
        put(img, ox + x, oy + 5, rgb(200, 156, 100))
        put(img, ox + x, oy + 6, WOOD_D)
        put(img, ox + x, oy + 14, OUT)
    for y in range(5, 15):
        put(img, ox + 2, oy + y, OUT)
        put(img, ox + 13, oy + y, OUT)
    for y in (9, 11):
        for x in range(3, 13):
            put(img, ox + x, oy + y, WOOD_D)
    for x in range(2, 14):
        put(img, ox + x, oy + 4, OUT)
    img.save(ART / "tileset.png")
    return img


# ---------------------------------------------------------------- pemain
def make_player():
    img = new(64, 64)
    skin = rgb(240, 200, 160)
    hair = rgb(70, 45, 30)
    shirt = rgb(60, 130, 200)
    pants = rgb(84, 72, 120)
    hat = rgb(230, 200, 90)
    hat_d = rgb(190, 160, 60)
    dark = rgb(40, 30, 30)
    for row, direction in enumerate(("bawah", "kiri", "kanan", "atas")):
        for frame in range(4):
            ox, oy = frame * T, row * T
            # topi jerami
            for x in range(3, 13):
                put(img, ox + x, oy + 3, hat)
                put(img, ox + x, oy + 4, hat_d)
            for x in range(5, 11):
                put(img, ox + x, oy + 1, hat)
                put(img, ox + x, oy + 2, hat)
            # kepala
            for y in range(5, 9):
                for x in range(5, 11):
                    put(img, ox + x, oy + y, skin)
            if direction == "bawah":
                put(img, ox + 6, oy + 6, dark)
                put(img, ox + 9, oy + 6, dark)
            elif direction == "atas":
                for y in range(5, 8):
                    for x in range(5, 11):
                        put(img, ox + x, oy + y, hair)
            elif direction == "kiri":
                put(img, ox + 6, oy + 6, dark)
                for y in range(5, 9):
                    put(img, ox + 10, oy + y, hair)
            else:
                put(img, ox + 9, oy + 6, dark)
                for y in range(5, 9):
                    put(img, ox + 5, oy + y, hair)
            # badan dan lengan
            for y in range(9, 13):
                for x in range(5, 11):
                    put(img, ox + x, oy + y, shirt)
            if direction in ("bawah", "atas"):
                swing = 1 if frame in (1, 3) else 0
                for y in range(9, 12):
                    put(img, ox + 4, oy + y + (swing if frame == 1 else 0), skin)
                    put(img, ox + 11, oy + y + (swing if frame == 3 else 0), skin)
            else:
                for y in range(9, 12):
                    put(img, ox + (7 if direction == "kiri" else 8), oy + y, skin)
            # kaki
            lift_l = 1 if frame == 1 else 0
            lift_r = 1 if frame == 3 else 0
            for y in range(13, 16 - lift_l):
                put(img, ox + 6, oy + y, pants)
                put(img, ox + 7, oy + y, pants)
            for y in range(13, 16 - lift_r):
                put(img, ox + 8, oy + y, pants)
                put(img, ox + 9, oy + y, pants)
    img.save(ART / "player.png")


# ---------------------------------------------------------------- tanaman
def blade(img, x, y_bottom, height, color, lean=0):
    for k in range(height):
        put(img, x + (lean if k > height // 2 else 0), y_bottom - k, color)


def make_crops():
    img = new(80, 48)  # 5 kolom (tahap 0,1,2,3,layu) x 3 baris (padi, jagung, cabai)
    g1, g2 = rgb(80, 170, 70), rgb(50, 130, 55)
    gold, gold_d = rgb(226, 192, 72), rgb(190, 150, 40)
    brown = rgb(130, 96, 60)

    def cell(col, row):
        return col * T, row * T

    # padi
    ox, oy = cell(0, 0)
    blade(img, ox + 7, oy + 14, 2, g1)
    blade(img, ox + 9, oy + 14, 2, g1)
    ox, oy = cell(1, 0)
    for x, h in [(5, 4), (7, 6), (9, 5), (11, 4)]:
        blade(img, ox + x, oy + 14, h, g1)
    ox, oy = cell(2, 0)
    for x, h in [(4, 7), (6, 9), (8, 10), (10, 9), (12, 7)]:
        blade(img, ox + x, oy + 14, h, g1, 1)
        blade(img, ox + x + 1, oy + 14, h - 1, g2, 1)
    ox, oy = cell(3, 0)
    for x, h in [(4, 8), (6, 10), (8, 11), (10, 10), (12, 8)]:
        blade(img, ox + x, oy + 14, h, gold_d, 1)
        for k in range(3):
            put(img, ox + x + 1, oy + 14 - h + k, gold)
            put(img, ox + x + 2, oy + 14 - h + k + 1, gold)
    ox, oy = cell(4, 0)
    for x, h in [(4, 3), (7, 4), (10, 3)]:
        blade(img, ox + x, oy + 14, h, brown, 2)

    # jagung
    ox, oy = cell(0, 1)
    blade(img, ox + 8, oy + 14, 2, g1)
    put(img, ox + 7, oy + 13, g1)
    put(img, ox + 9, oy + 13, g1)
    ox, oy = cell(1, 1)
    blade(img, ox + 8, oy + 14, 6, g2)
    for k, (dx, dy) in enumerate([(-2, 8), (-1, 9), (2, 9), (3, 8)]):
        put(img, ox + 8 + dx, oy + 14 - dy + 2, g1)
        put(img, ox + 8 + dx, oy + 14 - dy + 3, g1)
    ox, oy = cell(2, 1)
    blade(img, ox + 8, oy + 14, 11, g2)
    for dx, dy in [(-3, 4), (-2, 5), (-4, 3), (3, 6), (4, 5), (2, 7), (-3, 8), (3, 9)]:
        put(img, ox + 8 + dx, oy + 14 - dy, g1)
        put(img, ox + 8 + dx + (1 if dx < 0 else -1), oy + 14 - dy, g1)
    ox, oy = cell(3, 1)
    blade(img, ox + 8, oy + 14, 13, g2)
    for dx, dy in [(-3, 3), (-2, 4), (-4, 2), (3, 5), (4, 4), (-3, 9), (3, 10)]:
        put(img, ox + 8 + dx, oy + 14 - dy, g1)
        put(img, ox + 8 + dx + (1 if dx < 0 else -1), oy + 14 - dy, g1)
    for y in range(5, 10):
        put(img, ox + 9, oy + y, gold)
        put(img, ox + 10, oy + y, gold_d)
    put(img, ox + 9, oy + 4, g1)
    put(img, ox + 10, oy + 4, g1)
    ox, oy = cell(4, 1)
    blade(img, ox + 8, oy + 14, 7, brown, 3)
    for dx, dy in [(-2, 4), (-3, 3), (3, 3)]:
        put(img, ox + 8 + dx, oy + 14 - dy, brown)

    # cabai
    ox, oy = cell(0, 2)
    put(img, ox + 8, oy + 14, g2)
    put(img, ox + 8, oy + 13, g2)
    put(img, ox + 7, oy + 12, g1)
    put(img, ox + 9, oy + 12, g1)
    d = ImageDraw.Draw(img)
    ox, oy = cell(1, 2)
    blade(img, ox + 8, oy + 14, 5, g2)
    d.ellipse((ox + 5, oy + 7, ox + 11, oy + 12), fill=g1)
    ox, oy = cell(2, 2)
    blade(img, ox + 8, oy + 14, 4, brown)
    d.ellipse((ox + 3, oy + 3, ox + 13, oy + 12), fill=g1)
    d.ellipse((ox + 5, oy + 5, ox + 9, oy + 9), fill=g2)
    ox, oy = cell(3, 2)
    blade(img, ox + 8, oy + 14, 4, brown)
    d.ellipse((ox + 3, oy + 3, ox + 13, oy + 12), fill=g1)
    d.ellipse((ox + 5, oy + 5, ox + 9, oy + 9), fill=g2)
    red = rgb(210, 40, 40)
    for fx, fy in [(4, 8), (11, 7), (7, 11), (9, 4)]:
        put(img, ox + fx, oy + fy, red)
        put(img, ox + fx, oy + fy + 1, red)
        put(img, ox + fx, oy + fy + 2, rgb(160, 20, 20))
    ox, oy = cell(4, 2)
    blade(img, ox + 8, oy + 14, 4, brown, 2)
    d.ellipse((ox + 4, oy + 8, ox + 12, oy + 13), fill=rgb(150, 120, 70))
    img.save(ART / "crops.png")


# ---------------------------------------------------------------- hama
def make_pests():
    img = new(96, 16)
    d = ImageDraw.Draw(img)

    def o(i):
        return i * T

    # 0 wereng cokelat
    x = o(0)
    d.ellipse((x + 4, 7, x + 11, 12), fill=rgb(150, 100, 50))
    d.ellipse((x + 10, 8, x + 13, 11), fill=rgb(120, 78, 38))
    d.polygon([(x + 4, 7), (x + 11, 6), (x + 12, 10), (x + 6, 10)], fill=rgb(200, 160, 100))
    for lx in (5, 7, 9):
        put(img, x + lx, 13, OUT)
        put(img, x + lx, 14, OUT)
    put(img, x + 12, 9, rgb(20, 20, 20))
    # 1 tikus sawah
    x = o(1)
    d.ellipse((x + 2, 7, x + 11, 13), fill=rgb(130, 120, 110))
    d.ellipse((x + 9, 6, x + 14, 11), fill=rgb(150, 140, 130))
    d.rectangle((x + 10, 4, x + 11, 6), fill=rgb(220, 160, 160))
    d.rectangle((x + 13, 4, x + 14, 6), fill=rgb(220, 160, 160))
    put(img, x + 13, 8, rgb(20, 20, 20))
    put(img, x + 15, 9, rgb(240, 150, 150))
    for tx in range(0, 3):
        put(img, x + tx, 12 - (tx // 2), rgb(220, 160, 160))
    put(img, x + 4, 14, OUT)
    put(img, x + 9, 14, OUT)
    # 2 ulat grayak
    x = o(2)
    for k, (cx, cy) in enumerate([(3, 11), (6, 9), (9, 9), (12, 11)]):
        d.ellipse((x + cx - 2, cy - 2, x + cx + 2, cy + 2), fill=rgb(110, 150, 70) if k % 2 == 0 else rgb(140, 120, 70))
    d.ellipse((x + 11, 9, x + 15, 13), fill=rgb(110, 76, 44))
    put(img, x + 13, 10, rgb(240, 240, 240))
    put(img, x + 14, 10, rgb(20, 20, 20))
    # 3 belalang
    x = o(3)
    d.ellipse((x + 3, 7, x + 12, 11), fill=rgb(110, 170, 60))
    d.ellipse((x + 11, 6, x + 14, 10), fill=rgb(90, 150, 50))
    put(img, x + 13, 7, rgb(20, 20, 20))
    for k in range(5):
        put(img, x + 3 + k, 12 + (k // 3), rgb(80, 130, 40))
    for k in range(4):
        put(img, x + 2 + k, 11 + k // 2, rgb(80, 130, 40))
    put(img, x + 8, 12, OUT)
    put(img, x + 10, 12, OUT)
    put(img, x + 14, 5, OUT)
    put(img, x + 15, 4, OUT)
    # 4 kutu daun
    x = o(4)
    for cx, cy in [(5, 10), (9, 8), (10, 12), (4, 6), (12, 9)]:
        d.ellipse((x + cx - 1, cy - 1, x + cx + 1, cy + 1), fill=rgb(150, 210, 90))
        put(img, x + cx, cy - 1, rgb(200, 240, 140))
        put(img, x + cx + 1, cy, rgb(20, 20, 20))
    # 5 lalat buah
    x = o(5)
    d.ellipse((x + 5, 7, x + 11, 12), fill=rgb(220, 140, 40))
    d.ellipse((x + 10, 7, x + 13, 10), fill=rgb(60, 40, 30))
    put(img, x + 12, 8, rgb(210, 30, 30))
    for k in range(3):
        put(img, x + 4 + k, 5 + k, rgb(190, 220, 240))
        put(img, x + 5 + k, 5 + k, rgb(190, 220, 240))
        put(img, x + 8 + k, 5, rgb(190, 220, 240))
    for k in range(6, 11, 2):
        put(img, x + k, 13, OUT)
        put(img, x + k, 14, OUT)
    img.save(ART / "pests.png")
    # ilustrasi besar untuk Journal (64x64)
    for i, nama in enumerate(["wereng_cokelat", "tikus_sawah", "ulat_grayak_jagung", "belalang", "kutu_daun", "lalat_buah"]):
        tile = img.crop((i * T, 0, (i + 1) * T, T)).resize((64, 64), Image.NEAREST)
        tile.save(ART / f"jurnal_{nama}.png")


# ---------------------------------------------------------------- ikon
def make_icons():
    img = new(64, 64)
    d = ImageDraw.Draw(img)

    def o(i):
        return (i % 4) * T, (i // 4) * T

    gray = rgb(170, 175, 185)
    gray_d = rgb(110, 115, 125)
    # 0 cangkul
    x, y = o(0)
    d.line((x + 3, y + 13, x + 11, y + 4), fill=WOOD_D, width=2)
    d.polygon([(x + 9, y + 2), (x + 14, y + 4), (x + 12, y + 8), (x + 8, y + 6)], fill=gray, outline=gray_d)
    # 1 penyiram
    x, y = o(1)
    d.rectangle((x + 4, y + 6, x + 11, y + 13), fill=rgb(70, 130, 200), outline=rgb(40, 80, 140))
    d.line((x + 11, y + 8, x + 15, y + 4), fill=rgb(40, 80, 140), width=2)
    d.arc((x + 3, y + 2, x + 10, y + 9), 180, 360, fill=rgb(40, 80, 140))
    for k in range(3):
        put(img, x + 15, y + 6 + k * 2, rgb(160, 210, 250))
    # 2 kaca pembesar
    x, y = o(2)
    d.ellipse((x + 3, y + 2, x + 10, y + 9), fill=rgb(190, 230, 245), outline=rgb(80, 80, 90), width=1)
    d.line((x + 9, y + 9, x + 14, y + 14), fill=WOOD_D, width=2)
    put(img, x + 5, y + 4, rgb(255, 255, 255))
    # 3 penyemprot
    x, y = o(3)
    d.rectangle((x + 4, y + 6, x + 10, y + 14), fill=rgb(220, 230, 240), outline=gray_d)
    d.rectangle((x + 5, y + 3, x + 9, y + 5), fill=gray_d)
    d.line((x + 9, y + 3, x + 13, y + 3), fill=gray_d)
    for k in range(3):
        put(img, x + 14, y + 2 + k * 2, rgb(160, 210, 250))
    # 4 perangkap
    x, y = o(4)
    d.rectangle((x + 2, y + 6, x + 13, y + 13), outline=gray_d, fill=rgb(210, 215, 220))
    for k in range(3, 13, 3):
        d.line((x + k, y + 6, x + k, y + 13), fill=gray_d)
    d.polygon([(x + 2, y + 6), (x + 8, y + 2), (x + 13, y + 6)], fill=rgb(150, 105, 60), outline=OUT)
    # 5 biji
    x, y = o(5)
    d.rectangle((x + 3, y + 3, x + 12, y + 14), fill=rgb(236, 220, 180), outline=OUT)
    d.ellipse((x + 6, y + 6, x + 9, y + 11), fill=rgb(110, 170, 70))
    d.rectangle((x + 4, y + 4, x + 11, y + 4), fill=rgb(200, 60, 50))
    # 6 tanda seru
    x, y = o(6)
    d.ellipse((x + 2, y + 2, x + 13, y + 13), fill=rgb(250, 210, 60), outline=rgb(160, 90, 20))
    d.rectangle((x + 7, y + 4, x + 8, y + 9), fill=rgb(200, 30, 30))
    d.rectangle((x + 7, y + 11, x + 8, y + 11), fill=rgb(200, 30, 30))
    # 7 koin
    x, y = o(7)
    d.ellipse((x + 2, y + 2, x + 13, y + 13), fill=rgb(245, 195, 50), outline=rgb(160, 110, 20))
    d.rectangle((x + 7, y + 5, x + 8, y + 10), fill=rgb(200, 150, 30))
    # 8 bintang, 9 bintang kosong
    star = [(8, 1), (10, 6), (15, 6), (11, 9), (13, 14), (8, 11), (3, 14), (5, 9), (1, 6), (6, 6)]
    x, y = o(8)
    d.polygon([(x + a, y + b) for a, b in star], fill=rgb(250, 210, 60), outline=rgb(160, 110, 20))
    x, y = o(9)
    d.polygon([(x + a, y + b) for a, b in star], fill=rgb(200, 200, 205), outline=rgb(130, 130, 140))
    # 10 padi, 11 jagung, 12 cabai (ikon hasil panen)
    x, y = o(10)
    d.line((x + 8, y + 14, x + 8, y + 5), fill=rgb(190, 150, 40), width=1)
    for k in range(4):
        put(img, x + 6, y + 4 + k * 2, rgb(226, 192, 72))
        put(img, x + 10, y + 5 + k * 2, rgb(226, 192, 72))
        put(img, x + 7, y + 3 + k * 2, rgb(226, 192, 72))
        put(img, x + 9, y + 4 + k * 2, rgb(226, 192, 72))
    x, y = o(11)
    d.ellipse((x + 5, y + 2, x + 11, y + 13), fill=rgb(240, 205, 60), outline=rgb(170, 130, 20))
    d.polygon([(x + 3, y + 14), (x + 6, y + 6), (x + 8, y + 14)], fill=rgb(80, 160, 70))
    d.polygon([(x + 13, y + 14), (x + 10, y + 6), (x + 8, y + 14)], fill=rgb(60, 140, 60))
    x, y = o(12)
    d.polygon([(x + 4, y + 3), (x + 12, y + 3), (x + 9, y + 14), (x + 6, y + 10)], fill=rgb(210, 40, 40), outline=rgb(140, 20, 20))
    d.rectangle((x + 4, y + 2, x + 8, y + 3), fill=rgb(60, 140, 60))
    # 13 pestisida nabati, 14 kimia
    x, y = o(13)
    d.rectangle((x + 4, y + 5, x + 11, y + 14), fill=rgb(90, 180, 90), outline=rgb(40, 100, 40))
    d.rectangle((x + 6, y + 2, x + 9, y + 4), fill=rgb(60, 60, 60))
    d.rectangle((x + 5, y + 8, x + 10, y + 11), fill=rgb(235, 245, 220))
    x, y = o(14)
    d.rectangle((x + 4, y + 5, x + 11, y + 14), fill=rgb(210, 60, 60), outline=rgb(130, 20, 20))
    d.rectangle((x + 6, y + 2, x + 9, y + 4), fill=rgb(60, 60, 60))
    d.rectangle((x + 5, y + 8, x + 10, y + 11), fill=rgb(245, 235, 220))
    put(img, x + 7, y + 9, rgb(40, 20, 20))
    put(img, x + 8, y + 9, rgb(40, 20, 20))
    # 15 gembok
    x, y = o(15)
    d.arc((x + 4, y + 1, x + 11, y + 9), 180, 360, fill=gray_d, width=2)
    d.rectangle((x + 3, y + 7, x + 12, y + 14), fill=rgb(230, 190, 60), outline=rgb(140, 100, 20))
    put(img, x + 7, y + 10, rgb(60, 40, 20))
    put(img, x + 8, y + 10, rgb(60, 40, 20))
    img.save(ART / "ikon.png")


# ---------------------------------------------------------------- UI
def make_ui():
    # panel 9-slice 48x48, sudut 16
    panel = new(48, 48)
    d = ImageDraw.Draw(panel)
    d.rounded_rectangle((0, 0, 47, 47), radius=6, fill=rgb(248, 236, 208), outline=rgb(96, 64, 40), width=2)
    d.rounded_rectangle((3, 3, 44, 44), radius=4, outline=rgb(222, 200, 160), width=1)
    panel.save(ART / "panel.png")
    # slot hotbar 20x20
    slot = new(20, 20)
    d = ImageDraw.Draw(slot)
    d.rounded_rectangle((0, 0, 19, 19), radius=3, fill=rgb(60, 42, 30, 220), outline=rgb(150, 110, 70), width=1)
    slot.save(ART / "slot.png")
    sel = new(20, 20)
    d = ImageDraw.Draw(sel)
    d.rounded_rectangle((0, 0, 19, 19), radius=3, fill=rgb(60, 42, 30, 220), outline=rgb(250, 210, 60), width=2)
    sel.save(ART / "slot_terpilih.png")
    # bingkai penanda petak target 16x16
    tgt = new(16, 16)
    d = ImageDraw.Draw(tgt)
    c = rgb(255, 255, 255, 230)
    for a, b in [((0, 0), (4, 0)), ((0, 0), (0, 4)), ((15, 0), (11, 0)), ((15, 0), (15, 4)),
                 ((0, 15), (4, 15)), ((0, 15), (0, 11)), ((15, 15), (11, 15)), ((15, 15), (15, 11))]:
        d.line((a, b), fill=c)
    tgt.save(ART / "penanda_petak.png")


# ---------------------------------------------------------------- peta
def make_map(tileset):
    cols, rows = 30, 17
    grid = [[0] * cols for _ in range(rows)]
    for y in range(rows):
        for x in range(cols):
            if rng.random() < 0.08:
                grid[y][x] = 1
    # pohon tepi
    for x in range(cols):
        grid[0][x] = 12
        grid[16][x] = 12
    for y in range(rows):
        grid[y][0] = 12
        grid[y][29] = 12
    for x, y in [(1, 5), (1, 11), (2, 14), (8, 15), (12, 14), (16, 13), (17, 3), (15, 2), (10, 2), (26, 8), (28, 5), (19, 14), (14, 11), (18, 9)]:
        grid[y][x] = 12
    # rumah pemain: atap baris 1-2, dinding baris 3, pintu (5,3)
    for x in range(3, 8):
        grid[1][x] = 10
        grid[2][x] = 10
        grid[3][x] = 9
    grid[3][4] = 14
    grid[3][6] = 14
    grid[3][5] = 11
    # toko: atap baris 2-3, dinding baris 4, pintu (22,4)
    for x in range(20, 26):
        grid[2][x] = 10
        grid[3][x] = 10
        grid[4][x] = 9
    grid[4][20] = 14
    grid[4][22] = 11
    grid[4][24] = 13
    # jalan
    grid[4][5] = 2
    for x in range(5, 23):
        grid[5][x] = 2
    # pagar lahan + lahan 8x6 (kolom 3..10, baris 7..12)
    for x in range(2, 12):
        grid[6][x] = 6
        grid[13][x] = 6
    for y in range(7, 13):
        grid[y][2] = 7
        grid[y][11] = 7
    grid[6][6] = 2
    grid[6][7] = 2
    for y in range(7, 13):
        for x in range(3, 11):
            grid[y][x] = 3
    # shipping bin
    grid[7][13] = 15
    # kolam
    for y in range(11, 15):
        for x in range(22, 28):
            grid[y][x] = 8
    blocked = [6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
    data = {
        "catatan": "Peta satu layar: 30x17 tile 16px = 480x272 piksel, tanpa kamera. Id tile sama dengan urutan di tileset.png (4 kolom, kiri ke kanan, atas ke bawah).",
        "tileSize": T,
        "kolom": cols,
        "baris": rows,
        "tileset": "assets/art/tileset.png",
        "namaTile": {
            "0": "rumput", "1": "rumput_bunga", "2": "jalan", "3": "tanah", "4": "tanah_diolah", "5": "tanah_basah",
            "6": "pagar_horizontal", "7": "pagar_vertikal", "8": "air", "9": "dinding", "10": "atap", "11": "pintu",
            "12": "pohon", "13": "papan_toko", "14": "jendela", "15": "shipping_bin",
        },
        "tileTerblokir": blocked,
        "ground": grid,
        "lahan": {"originTile": [3, 7], "kolom": 8, "baris": 6},
        "spawn": [5, 5],
        "objek": [
            {"id": "pintu_rumah", "jenis": "tidur", "tile": [5, 3], "berdiriDi": [5, 4]},
            {"id": "pintu_toko", "jenis": "toko", "tile": [22, 4], "berdiriDi": [22, 5]},
            {"id": "shipping_bin", "jenis": "bin", "tile": [13, 7], "berdiriDi": [12, 7]},
        ],
    }
    (MAP / "map.json").write_text(json.dumps(data, ensure_ascii=False, indent=1), encoding="utf-8")
    csv = ",\n".join(",".join(str(v + 1) for v in row) for row in grid)
    tmx = f"""<?xml version="1.0" encoding="UTF-8"?>
<map version="1.10" orientation="orthogonal" renderorder="right-down" width="{cols}" height="{rows}" tilewidth="{T}" tileheight="{T}" infinite="0" nextlayerid="2" nextobjectid="1">
 <tileset firstgid="1" name="tileset" tilewidth="{T}" tileheight="{T}" tilecount="16" columns="4">
  <image source="../assets/art/tileset.png" width="64" height="64"/>
 </tileset>
 <layer id="1" name="ground" width="{cols}" height="{rows}">
  <data encoding="csv">
{csv}
  </data>
 </layer>
</map>
"""
    (MAP / "map.tmx").write_text(tmx, encoding="utf-8")
    # pratinjau peta + HUD kasar, skala 3x
    prev = Image.new("RGBA", (cols * T, rows * T))
    for y in range(rows):
        for x in range(cols):
            tid = grid[y][x]
            tile = tileset.crop(((tid % 4) * T, (tid // 4) * T, (tid % 4) * T + T, (tid // 4) * T + T))
            prev.paste(tile, (x * T, y * T))
    prev.resize((cols * T * 3, rows * T * 3), Image.NEAREST).save(MAP / "pratinjau_peta.png")


# ---------------------------------------------------------------- audio
SR = 22050


def tone(freq, dur, kind="square", vol=0.4, attack=0.005, release=0.05):
    n = int(SR * dur)
    t = np.arange(n) / SR
    if kind == "square":
        w = np.sign(np.sin(2 * np.pi * freq * t))
    elif kind == "triangle":
        w = 2 * np.abs(2 * ((t * freq) % 1) - 1) - 1
    else:
        w = np.sin(2 * np.pi * freq * t)
    env = np.ones(n)
    a = max(1, int(SR * attack))
    r = max(1, int(SR * release))
    env[:a] = np.linspace(0, 1, a)
    env[-r:] = np.minimum(env[-r:], np.linspace(1, 0, r))
    return w * env * vol


def noise_burst(dur, vol=0.4, smooth=1):
    n = int(SR * dur)
    w = np.random.default_rng(3).uniform(-1, 1, n)
    if smooth > 1:
        k = np.ones(smooth) / smooth
        w = np.convolve(w, k, mode="same")
    return w * vol


def save_wav(name, data):
    data = np.clip(data, -1, 1)
    pcm = (data * 32767).astype("<i2")
    with wave.open(str(SFX / name), "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(SR)
        f.writeframes(pcm.tobytes())


def make_audio():
    # cangkul: hentakan tanah
    n = int(SR * 0.18)
    env = np.exp(-np.arange(n) / (SR * 0.04))
    thump = np.sin(2 * np.pi * 110 * np.arange(n) / SR) * 0.6 * env
    save_wav("sfx_cangkul.wav", thump + noise_burst(0.18, 0.5, 4) * env)
    # siram: desis air
    n = int(SR * 0.5)
    env = np.sin(np.linspace(0, np.pi, n)) ** 0.7
    save_wav("sfx_siram.wav", noise_burst(0.5, 0.45, 2) * env)
    # panen: arpeggio naik
    notes = [523.25, 659.25, 783.99, 1046.5]
    save_wav("sfx_panen.wav", np.concatenate([tone(f, 0.09, "square", 0.3) for f in notes]))
    # beli: koin dua nada
    save_wav("sfx_beli.wav", np.concatenate([tone(987.77, 0.07, "square", 0.3), tone(1318.5, 0.25, "square", 0.3, release=0.2)]))
    # jual: jingle naik
    notes = [392, 494, 587, 698, 784, 988]
    parts = [tone(f, 0.08, "triangle", 0.45) for f in notes[:-1]] + [tone(notes[-1], 0.3, "triangle", 0.45, release=0.25)]
    save_wav("sfx_jual.wav", np.concatenate(parts))
    # gagal: buzz turun
    n = int(SR * 0.35)
    t = np.arange(n) / SR
    f = np.linspace(220, 110, n)
    ph = 2 * np.pi * np.cumsum(f) / SR
    env = np.linspace(1, 0, n) ** 0.5
    save_wav("sfx_gagal.wav", np.sign(np.sin(ph)) * 0.3 * env)

    # musik latar: loop sekitar 21 detik
    bpm = 90
    beat = 60 / bpm
    chords = [(261.63, [0, 4, 7]), (220.0, [0, 3, 7]), (174.61, [0, 4, 7]), (196.0, [0, 4, 7])]
    pent = [523.25, 587.33, 659.25, 783.99, 880.0, 1046.5]
    r = random.Random(11)
    total = int(SR * beat * 4 * 8)
    out = np.zeros(total)
    pos = 0
    for bar in range(8):
        root, _ = chords[bar % 4]
        for b in (0, 2):
            seg = tone(root / 2, beat * 2, "triangle", 0.35, release=0.2)
            s = int(SR * beat * (bar * 4 + b))
            out[s:s + len(seg)] += seg[: total - s]
        for e in range(8):
            if r.random() < 0.72:
                f = r.choice(pent)
                seg = tone(f, beat * 0.5, "square", 0.12, release=0.12)
                s = int(SR * beat * (bar * 4 + e * 0.5))
                out[s:s + len(seg)] += seg[: total - s]
    fade = int(SR * 0.05)
    out[:fade] *= np.linspace(0, 1, fade)
    out[-fade:] *= np.linspace(1, 0, fade)
    save_wav("bgm_ladang.wav", out * 0.9)


def main():
    ts = make_tileset()
    make_player()
    make_crops()
    make_pests()
    make_icons()
    make_ui()
    make_map(ts)
    make_audio()
    print("selesai")


if __name__ == "__main__":
    main()
