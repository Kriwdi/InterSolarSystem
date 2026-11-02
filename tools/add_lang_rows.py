"""Dev helper: appends rows (key|en|de|hu per line on stdin, key may be an enum call) before check(rows)."""
import json, sys
from pathlib import Path
p = Path(__file__).resolve().parent.parent / "src/client/java/hu/kriwdi/intersolarsystem/client/datagen/ModLangTable.java"
s = p.read_text(encoding="utf-8")
out = []
for line in sys.stdin.read().splitlines():
    if not line.strip():
        continue
    key, en, de, hu = line.split("|")
    k = key if key.startswith("ShipCommands.") else json.dumps(key)
    j = lambda t: json.dumps(t, ensure_ascii=False)
    out.append(f"\t\tadd(rows, {k},\n\t\t\t\t{j(en)},\n\t\t\t\t{j(de)},\n\t\t\t\t{j(hu)});")
marker = "\n\t\tcheck(rows);"
s = s.replace(marker, "\n" + "\n".join(out) + marker)
p.write_text(s, encoding="utf-8")
