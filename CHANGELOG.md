# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Task Screens have returned! Task display and automated completion are now possible
  - Item Tasks and Fluid Tasks can be used on both Forge and Fabric
  - Forge Energy Tasks can be used on Forge
  - Tech Reborn Energy Tasks can be used on Fabric
- The left-hand Chapter panel now remembers its pinned status across runs of the game
- When viewing a quest, the dependencies and dependents buttons are now a more visible colour when deps exist
  - In addition, the dependencies button will blink if the current quest can't be started due to an incomplete dependency
- The dependencies and dependents button tooltips now also show the chapter for dependent quests, if they're in a different chapter to the current one
- Selecting a quest outside edit mode (with either right or left-click) will preview any hidden dependents
  - Dependency lines and quest box outlines will be shown, but no other information; so the player at least knows that follow-on quest(s) exist

#### For content creators
- Edit mode can now be toggled from within the Quests GUI (new button in bottom right)
  - In an SSP world, you will require "Allow Cheats: ON" to be able to see this button
- Multiple quests can now be selected (for moving/deletion/etc.) by dragging a box with the middle mouse button
  - Hold Control to toggle selected status of quests
- The Structure Task now supports structure tags (e.g. `#minecraft:villages` will match any village)
- Added a `/ftbquests reload` command to reload quest data from file. Requires user to be in editing mode
  - Caution: not recommended for use on live servers. Take care when editing quest file data directly. Make a backup!
- Added a "Save to Server" button in the settings context menu which allows quest data to be saved immediately
  - Quest data is normally saved with a world-save, but this is a convenience to allow quest developers to save without leaving the GUI
- When in editing mode, hidden quests which would be invisible outside editing mode now have an "eye" icon to indicate that
- Quest title, subtitle and description text now supports vanilla-style raw JSON text (https://minecraft.fandom.com/wiki/Raw_JSON_text_format)
  - Standard markup (colour, bold, etc.) is supported, in addition to `clickEvent` and `hoverEvent` (click & hover events only function in subtitle and description text, not the title)
  - Any line starting with `{` or `[` followed by 0 or more whitespace followed by `"` will be treated as raw JSON text, e.g. `{ "text": "hello", "color": "green" }` or `["hello ", {"text":"world", "color":"yellow"}]`
  - Old-style markup (previously used by FTB Quests) is still supported for brevity (that is, lines which start with `{`, e.g. `{my.translation.key}`)
- Added an entity type tag `ftbquests:no_loot_crates`; any entity in this tag will never drop a loot crate on death
- Quest view minimum width config setting now also has a chapter default setting (thanks @MasterOfBob777/@DarkMega18)
- Quests now have an "Invisible" setting; if true, the quests is hidden until completed (thanks @MasterOfBob777/@DarkMega18)
  - There's a related "Invisible until X tasks completed" setting which allows the quest to appear after one or more tasks have been completed
- Quests now have a "Repeatable" setting again; the quest will reset and be available once more after any rewards are claimed (thanks @MasterOfBob777/@DarkMega18)
- Quests now have an "Exclude from Claim All" setting which allows them to ignore the "Claim All" button (thanks @MasterOfBob777/@DarkMega18)
  - May be useful for quests which have some kind of special non-item reward (e.g. running a command) which is more suitable for clicking directly
- Item Tasks have two new settings in the config screen: "Match NBT" and "Weak NBT Matching"
  - "Match NBT" is a tristate: Default means to use the current behaviour; items must be in the `itemfilters:check_nbt` tag, False means to never try NBT matching, and True means to always try NBT matching.
  - "Weak NBT Matching" is a boolean; when true, only top-level NBT fields in the filter item will be matched (so if the item being checked has extra NBT data, it will still match)
- The Location Task config screen has had tooltips added to the X/Y/Z entries to clearly explain how they are interpreted in conjunction with the W/H/D entries)

### Fixed
- Fixed an NPE in the Stats Task if the resource location is unknown
- The focused grid position (where a quest will be added) is now highlighted again
- Pressing Escape in various screens now goes back to the previous screen instead of closing the entire GUI
  - Similarly, pressing Escape when viewing a quest pops down the quest view, and when a context menu is popped up, that will be popped down
- Full JEI support has returned for quests (items -> rewards) and loot crates (reward table -> list of reward items and their weights)
- The quest auto-pin button now works again to display progress of all available quests
- Fixed item durability bars in quest buttons rendering over the chapter panel
- Fixed Dimension Tasks triggering even when dependencies weren't met
- Fixed the quest completion notification icon being missing from the quest book sidebar button when using REI instead of JEI

#### For content creators
- The Checkmark Task config screen no longer allows editing of the icon (checkmark tasks always just display a tickbox, so it wasn't useful)
- Fixed garbled text when confirming deletion of multiple quests
- Clicking a greyed out item in a context menu no longer closes the menu
- Fixed certain context menu items crashing the client when clicked
- Fixed quest-specific theme data not being loaded from ftb_quests_theme.txt (which can be overridden in resource packs) 
- Better behaviour for the multiline text editor used to edit quest description paragraphs (right click quest -> Edit -> Description)
- Fixed Reward Table editing screen shrinking every time it was reopened when adding/deleting entries
