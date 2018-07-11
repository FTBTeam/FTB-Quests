# config/ftbquests/quests.json
|---|:-:|---|
|`chapters`|`chapter[]`|Chapter array|

`id` in all objects is internal and you shouldn't touch it. If missing or invalid, id will be auto-generated.

# Chapter
|---|:-:|---|
|`title`|`text_component`|Chapter title|
|`description`|`text_component[]`|Chapter description, optional|
|`icon`|`icon`|Chapter icon, optional. If not specified, it will be generated based on all quest icons|
|`quests`|`quest[]`|Quest array|

```json
{
  "title": "Chapter Title",
  "description": [
    "Chapter Description"
  ],
  "icon": "item:minecraft:cobblestone",
  "quests": [
  ]
}
```

# Quest
|---|:-:|---|
|`title`|`text_component`|Quest title|
|`x`|`int`|Quest x coordinate in chapter|
|`y`|`int`|Quest y coordinate in chapter|
|`description`|`text_component`|Short quest description, optional|
|`text`|`text_component[]`|Long quest description, optional|
|`type`|`normal, secret, invisible`|Long quest description, optional|
|`icon`|`icon`|Quest icon, optional. If not specified, it will be generated based on all task icons|
|`tasks`|`task[]`|Task array|
|`rewards`|`reward[]`|Reward array, optional|
|`depends_on`|`int[]`|Dependency array, optional. Can be other quest, chapter, task IDs|

```json
{
  "title": "Quest Title",
  "x": 4,
  "y": 2,
  "description": "Chapter Description",
  "text": [
    "Extended description",
    "Only visible when quest is clicked on"
  ],
  "icon": "item:minecraft:apple",
  "tasks": [
  ],
  "rewards": [
  ],
  "depends_on": [
    4598254452
  ]
}
```

# Tasks
### Item
|---|:-:|---|
|`item`|`item`|Item Type, see below|
|`count`|`int`|Amount of items, optional, by default 1|

### Item type
|---|:-:|---|
|`item`|`string`|Item ID|
|`count`|`int`|Stack size, optional, by default 1|
|`data`|`int`|Damage/metadata, optional, by default 0|
|`nbt`|`object`|NBT data, optional, by default null|
|`caps`|`object`|Capability data, optional, by default null|

```json
{"item": {"item": "minecraft:potion", "nbt": {"Potion": "minecraft:water"}}, "count": 3}
```

You can also just use a string if item doesn't have data and it's count is 1

```json
{"item": "minecraft:apple", "count": 20}
```

### OreDictionary type
|---|:-:|---|
|`ore`|`string`|OreDictionary name|

```json
{"item": "minecraft:cobblestone", "count": 1000}
```

```json
"minecraft:apple"
```

---

### Fluid
|---|:-:|---|
|`fluid`|`string`|Fluid ID|
|`amount`|`int`|Amount in millibuckets, optional, by default 1000|
|`nbt`|`object`|NBT data, optional, by default null|

```json
{"fluid": "water", "amount": 6000}
```

---

### Forge Energy / RF
|---|:-:|---|
|`forge_energy`|`int`|Energy amount|

```json
{"forge_energy": 50000}
```

---

### IC2 Energy (only works when IC2 is installed)
|---|:-:|---|
|`ic2_energy`|`int`|Energy amount|

```json
{"ic2_energy": 1000000}
```

---

# Rewards
### Item
|---|:-:|---|
|`item`|`string`|Item ID|
|`count`|`int`|Stack size, optional, by default 1|
|`data`|`int`|Damage/metadata, optional, by default 0|
|`nbt`|`object`|NBT data, by default null|
|`caps`|`object`|Capability data, by default null|

```json
{"item": "minecraft:red_flower", "data": 0, "count": 12, "nbt": {"display":{"Name":"Super Flower!"}}
```

---

### XP
|---|:-:|---|
|`xp`|`int`|XP points|

```json
{"xp": 200}
```

---

### XP Levels
|---|:-:|---|
|`xp_levels`|`int`|XP levels|

```json
{"xp_levels": 4}
```

---

# Example
```json
{
  "chapters": [
    {
      "title": "Minecraft",
      "description": [
        "Test chapter"
      ],
      "icon": "item:minecraft:grass",
      "quests": [
        {
          "x": 0,
          "y": 0,
          "title": "Collect Clay",
          "tasks": [
            {
              "item": "minecraft:clay_ball",
              "count": 10
            },
            {
              "item": "minecraft:clay"
            }
          ],
          "rewards": [
            {
              "item": "minecraft:apple"
            },
            {
              "xp_levels": 3
            }
          ]
        },
        {
          "x": 3,
          "y": 1,
          "title": "Water",
          "description": "Tear drops are falling like rain",
          "icon": "item:minecraft:water_bucket",
          "text": [
            "[Verse 1]",
            "Snow fall on the tree tops, the streets are bare",
            "Bright lights in the windows, the up the stairs",
            "And all of your silence, and all of your songs",
            "I still hear the choir, but you are gone",
            "",
            "[Pre-Chorus]",
            "Can't go back",
            "Can't fight this",
            "",
            "[Chorus]",
            "Water, water",
            "Tear drops are falling like rain",
            "Daughter, daughter",
            "Still hear you calling my name",
            "Oh, I see your light",
            "In the water, for all my life",
            "",
            "[Verse 2]",
            "I'm standing on my tip-toes, I'm coming undone",
            "You were my hero, when I was young",
            "We live but we never know, lost in the wind",
            "A million candles, just flickering",
            "",
            "[Pre-Chorus]",
            "Can't go back",
            "Can't fight this",
            "",
            "[Chorus]",
            "Water, water",
            "Tear drops are falling like rain",
            "Daughter, daughter",
            "Still hear you calling my name",
            "Oh, I see your light",
            "In the water, for all my life",
            "",
            "[Verse 3]",
            "Lifted high, let me lay down at your side",
            "Here I stand, 'til your soul comes back to mine",
            "",
            "[Chorus]",
            "Water, water",
            "Tear drops are falling like rain",
            "Daughter, daughter",
            "Still hear you calling my name",
            "Oh, I see your light",
            "In the water, for all my life"
          ],
          "tasks": [
            {
              "fluid": "water"
            },
            {
              "item": {
                "item": "minecraft:potion",
                "nbt": {
                  "Potion": "minecraft:water"
                }
              },
              "count": 3
            }
          ],
          "rewards": [
            {
              "item": "minecraft:book"
            }
          ]
        },
        {
          "x": 2,
          "y": 1,
          "title": "Make a Pot",
          "depends_on": [
          ],
          "tasks": [
            {
              "item": "minecraft:flower_pot"
            }
          ],
          "rewards": [
            {
              "item": "minecraft:red_flower",
              "data": 0
            },
            {
              "xp_levels": 1
            }
          ]
        }
      ]
    },
    {
      "title": "Technology",
      "icon": "item:minecraft:furnace",
      "depends_on": [
      ],
      "quests": [
        {
          "x": 0,
          "y": 0,
          "title": "Forge Energy",
          "tasks": [
            {
              "forge_energy": 2000
            }
          ]
        },
        {
          "x": 1,
          "y": 0,
          "title": "IC2 Energy",
          "tasks": [
            {
              "ic2_energy": 1000000
            }
          ]
        }
      ]
    }
  ]
}
```