import json
import random

with open("agartala_locations.json", "r") as f:
    data = json.load(f)

# output as valid JSON but compactly
print("[")
for i, item in enumerate(data[:110]):
    keys = list(item.keys())
    random.shuffle(keys)
    
    parts = []
    for k in keys:
        v = item[k]
        if isinstance(v, str):
            parts.append(f'"{k}": "{v}"')
        elif isinstance(v, bool):
            parts.append(f'"{k}": {"true" if v else "false"}')
        else:
            parts.append(f'"{k}": {v}')
    
    line = "{ " + ", ".join(parts) + " }"
    if i < 110 - 1:
        print("  " + line + ",")
    else:
        print("  " + line)
print("]")
