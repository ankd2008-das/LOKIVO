import json
import random

with open("final_100.json", "r") as f:
    data = json.load(f)

# Randomize key order for each object to reduce repetition patterns
out_data = []
for item in data:
    keys = list(item.keys())
    random.shuffle(keys)
    new_item = {k: item[k] for k in keys}
    out_data.append(new_item)

# Minify
res = json.dumps(out_data, separators=(',', ':'))
print(res)
