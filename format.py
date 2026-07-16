import json

with open("agartala_locations.json", "r") as f:
    data = json.load(f)

# output as valid JSON but compactly
print("[")
for i, item in enumerate(data):
    line = json.dumps(item)
    if i < len(data) - 1:
        print("  " + line + ",")
    else:
        print("  " + line)
print("]")
