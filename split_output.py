import json

with open("final_100.json", "r") as f:
    data = json.load(f)

# output as valid JSON but compactly
print("Split 1")
print(json.dumps(data[:33]))
print("Split 2")
print(json.dumps(data[33:66]))
print("Split 3")
print(json.dumps(data[66:]))
