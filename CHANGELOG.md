# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Multiple quests can now be selected by dragging a box with the middle mouse button. Hold Control to toggle selected status of quests.
- The Left-hand Chapter panel now remembers its pinned status across runs of the game
- When viewing a quest, the dependencies and dependents buttons are now a more visible colour when deps exist. In addition the dependencies button will blink if the current quest can't be started.
- The dependencies and dependents button tooltips now also show the chapter for dependent quests, if they in a different chapter to the current one.
- The Structure Task now supports structure tags (e.g. `#minecraft:villages` will match any village)
- Added a `/ftbquests reload` command to reload quest data from file. Requires user to be in editing mode. Caution: not recommended to use on live servers. Take care when editing quest file data directly. Make a backup!

### Fixed
- Fixed an NPE in the Stats Task if the resource location is unknown
- Item Tasks have two new settings in the config screen: "Match NBT" and "Weak NBT Matching". "Match NBT" is a tristate: Default means to use the current behaviour; items must be in the `itemfilters:check_nbt` tag, False means to never try NBT matching, and True means to always try NBT matching. "Weak NBT Matching" is a boolean; when true, only top-level NBT fields in the filter item will be check (so if the item being checked has extra NBT data, it will still match)
- Pressing Escape in various screens now goes to back to the previous screen instead of closing the entire GUI
- Similarly, pressing Escape when viewing a quest pops down the quest view, and when a context menu is popped up, that will be popped down
- Full JEI support has returned for quests (items -> rewards) and loot crates (reward table -> list of reward items and their weights)
- Clicking a greyed out item in a context menu no longer closes the menu
- Fixed certain context menu items crashing the client when clicked
- The focused grid position (where a quest will be added) is now highlighted again
- The quest auto-pin button now works again to display progress of all available quests
- The Location Task config screen has had tooltips added to the X/Y/Z entries to clearly explain how they are interpreted in conjunction with the W/H/D entries)
- The Checkmark Task config screen no longer allows editing of the icon (checkmark tasks always just display a tickbox, so it wasn't useful)
- Fixed garbled text when confirming deletion of multiple quests
- Fixed Dimension Tasks triggering even when dependencies weren't met
- Fixed item durability bars in quest buttons rendering over the chapter panel
- Fixed quest-specific theme data not being loaded from ftb_quests_theme.txt (which can be overridden in resource packs) 

