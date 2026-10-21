import sys, re, glob, os
d = sys.argv[1]
files = sorted([f for f in glob.glob(os.path.join(d, "t4_*.out")) if re.search(r"t4_\d+", os.path.basename(f))], key=lambda f: (int(re.findall(r"t4_(\d+)", os.path.basename(f))[0]), f))
for f in files:
    txt = open(f, encoding="utf-8", errors="replace").read().replace("\r", "")
    name = os.path.basename(f)
    m = re.search(r"journey (\w+) finished at (Y[\d.]+)", txt)
    story = m.group(1) if m else "?"
    end = m.group(2) if m else "(not finished)"
    complete = re.search(r"(Y[\d.]+) THE STORY IS COMPLETE", txt)
    deaths = re.findall(r"\] +(Y[\d.]+) Cause: (.*)", txt)
    opened = re.findall(r"\] +(Y[\d.]+) opened (\w+) \(age (\d+), Major (\d+), crafted (\d+)\)", txt)
    closed = re.findall(r"\] +(Y[\d.]+) closed (\w+) after ([\d.]+) years", txt)
    majors = re.findall(r"\] +(Y[\d.]+) reached major level (\d+)", txt)
    silent = re.search(r"silent results \((\d+)\)", txt)
    probs = re.search(r"problems \((\d+)(?: in (\d+) kinds)?\)", txt)
    exc = re.search(r"exceptions seen in the output: (\d+); gameError=(\S+); crash.log=(\S+)", txt)
    final = re.search(r"final: chapter index (\d+), open=(\w+), done=(\w+)", txt)
    extras = re.search(r"lessons, little stories, favors: (.*)", txt)
    print("=== %s: %s ended %s%s%s" % (name, story, end, ("; COMPLETE at " + complete.group(1)) if complete else "", ("; died: " + deaths[-1][1] + " at " + deaths[-1][0]) if deaths else ""))
    print("    majors: " + ", ".join("M%s@%s" % (b, a) for a, b in majors))
    print("    chapters: " + ", ".join("%s %s->%s" % (c[1], c[0], next((x[0] for x in closed if x[1] == c[1] and x[0] >= c[0]), "open")) for c in opened))
    if final: print("    final: index %s open=%s done=%s" % final.groups())
    print("    silent=%s problems=%s exceptions=%s%s" % (silent.group(1) if silent else "?", (probs.group(1) + ("/" + probs.group(2) if probs.group(2) else "")) if probs else "?", exc.groups() if exc else "?", ("; extras: " + extras.group(1)) if extras else ""))
