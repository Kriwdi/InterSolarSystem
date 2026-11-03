"""Minimal NBT reader/writer (gzip big-endian) for structure templates. Values: python int with explicit tags via
tuples is avoided; the writer takes typed wrappers (Byte, Short, Int, Long, Float, Double, IntList) where needed."""
import gzip
import io
import struct


class Byte(int): pass
class Short(int): pass
class Int(int): pass
class Long(int): pass
class Float(float): pass
class Double(float): pass
class IntArray(list): pass


def _read_payload(f, t):
    if t == 1: return Byte(struct.unpack('>b', f.read(1))[0])
    if t == 2: return Short(struct.unpack('>h', f.read(2))[0])
    if t == 3: return Int(struct.unpack('>i', f.read(4))[0])
    if t == 4: return Long(struct.unpack('>q', f.read(8))[0])
    if t == 5: return Float(struct.unpack('>f', f.read(4))[0])
    if t == 6: return Double(struct.unpack('>d', f.read(8))[0])
    if t == 7:
        n = struct.unpack('>i', f.read(4))[0]; return f.read(n)
    if t == 8:
        n = struct.unpack('>H', f.read(2))[0]; return f.read(n).decode('utf-8')
    if t == 9:
        et = f.read(1)[0]; n = struct.unpack('>i', f.read(4))[0]
        return [_read_payload(f, et) for _ in range(n)]
    if t == 10:
        d = {}
        while True:
            tt = f.read(1)[0]
            if tt == 0: return d
            n = struct.unpack('>H', f.read(2))[0]; name = f.read(n).decode('utf-8')
            d[name] = _read_payload(f, tt)
    if t == 11:
        n = struct.unpack('>i', f.read(4))[0]; return IntArray(struct.unpack('>%di' % n, f.read(4 * n)))
    if t == 12:
        n = struct.unpack('>i', f.read(4))[0]; return list(struct.unpack('>%dq' % n, f.read(8 * n)))
    raise ValueError(t)


def read(path):
    f = io.BytesIO(gzip.decompress(open(path, 'rb').read()))
    t = f.read(1)[0]
    n = struct.unpack('>H', f.read(2))[0]; f.read(n)
    return _read_payload(f, t)


def _tag(v):
    if isinstance(v, bool): return 1
    for cls, t in ((Byte, 1), (Short, 2), (Int, 3), (Long, 4), (Float, 5), (Double, 6), (IntArray, 11)):
        if isinstance(v, cls): return t
    if isinstance(v, int): return 3
    if isinstance(v, float): return 6
    if isinstance(v, (bytes, bytearray)): return 7
    if isinstance(v, str): return 8
    if isinstance(v, list): return 9
    if isinstance(v, dict): return 10
    raise TypeError(type(v))


def _write_payload(out, t, v):
    if t == 1: out.write(struct.pack('>b', int(v)))
    elif t == 2: out.write(struct.pack('>h', v))
    elif t == 3: out.write(struct.pack('>i', v))
    elif t == 4: out.write(struct.pack('>q', v))
    elif t == 5: out.write(struct.pack('>f', v))
    elif t == 6: out.write(struct.pack('>d', v))
    elif t == 7: out.write(struct.pack('>i', len(v))); out.write(v)
    elif t == 8:
        b = v.encode('utf-8'); out.write(struct.pack('>H', len(b))); out.write(b)
    elif t == 9:
        et = _tag(v[0]) if v else 0
        out.write(bytes([et])); out.write(struct.pack('>i', len(v)))
        for x in v: _write_payload(out, et, x)
    elif t == 10:
        for k, x in v.items():
            tt = _tag(x); out.write(bytes([tt]))
            b = k.encode('utf-8'); out.write(struct.pack('>H', len(b))); out.write(b)
            _write_payload(out, tt, x)
        out.write(b'\x00')
    elif t == 11:
        out.write(struct.pack('>i', len(v))); out.write(struct.pack('>%di' % len(v), *v))


def write(path, root):
    out = io.BytesIO()
    out.write(b'\x0a'); out.write(struct.pack('>H', 0))
    _write_payload(out, 10, root)
    with open(path, 'wb') as f:
        f.write(gzip.compress(out.getvalue(), mtime=0))
