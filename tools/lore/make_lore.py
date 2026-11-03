"""Writes the lore files (assets/intersolarsystem/lore/en_us.json, hu_hu.json) from lore_texts.py.

    python3 tools/lore/make_lore.py
"""
import json
from pathlib import Path

from lore_texts import MANUALS, NOTES, TAPES, TERMINALS

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/intersolarsystem/lore"


def build(lang):
    data = {"notes": {}, "tapes": {}, "terminals": {}, "manuals": {}}
    for kind, entries in (("notes", NOTES), ("terminals", TERMINALS), ("manuals", MANUALS)):
        for id_, e in entries.items():
            data[kind][id_] = e[lang]
    for id_, e in TAPES.items():
        t = e[lang]
        data["tapes"][id_] = {"title": t["title"], "subtitle": t["subtitle"],
                              "lines": [{"at": at, "who": who, "text": text} for at, who, text in t["lines"]]}
    return data


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    for lang, code in (("en", "en_us"), ("hu", "hu_hu")):
        (OUT / (code + ".json")).write_text(json.dumps(build(lang), ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print("lore:", len(NOTES), "notes,", len(TAPES), "tapes,", len(TERMINALS), "terminals,", len(MANUALS), "manuals")
