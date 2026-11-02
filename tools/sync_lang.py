"""Writes the lang JSON files in src/main/generated from ModLangTable.java without starting Minecraft.

Produces the same output as `./gradlew runDatagen` (sorted keys, 2-space indent). The unit test LangTableTest checks
that the committed files match the table, so either this script or runDatagen can be used after editing the table.

Usage: python3 tools/sync_lang.py
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TABLE = ROOT / "src/client/java/hu/kriwdi/intersolarsystem/client/datagen/ModLangTable.java"
OUT = ROOT / "src/main/generated/assets/intersolarsystem/lang"

ENUM_PREFIX = {
    "hullKey": "ship.intersolarsystem.hull.",
    "incidentKey": "ship.intersolarsystem.incident.",
    "phaseKey": "ship.intersolarsystem.phase.",
    "subsystemKey": "ship.intersolarsystem.subsystem.",
    "faultKey": "ship.intersolarsystem.fault.",
}

STRING = r'"((?:[^"\\]|\\.)*)"'
ROW = re.compile(
    r'add\(rows,\s*(?:' + STRING + r'|ShipCommands\.(\w+)\(\w+\.(\w+)\))\s*,\s*'
    + STRING + r'\s*,\s*' + STRING + r'\s*,\s*' + STRING + r'\s*\);')


def unescape(s):
    return json.loads('"' + s + '"')


def main():
    source = TABLE.read_text(encoding="utf-8")
    langs = {"en_us": {}, "de_de": {}, "hu_hu": {}}
    for m in ROW.finditer(source):
        literal, fn, const, en, de, hu = m.groups()
        key = unescape(literal) if literal is not None else ENUM_PREFIX[fn] + const.lower()
        for code, text in (("en_us", en), ("de_de", de), ("hu_hu", hu)):
            if key in langs[code]:
                raise SystemExit("duplicate key " + key)
            langs[code][key] = unescape(text)
    for code, entries in langs.items():
        text = json.dumps(dict(sorted(entries.items())), ensure_ascii=False, indent=2)
        (OUT / (code + ".json")).write_text(text, encoding="utf-8")
        print(code, len(entries))


if __name__ == "__main__":
    main()
