# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2001.4.7]

### Fixed
* Hopefully fixed problem where items matching a filter (Item Filters or FTB Filter System) don't always get displayed correctly in the GUI
  * "Hopefully" because this is a tricky one to reproduce...

## [2001.4.6]

### Fixed
* Fixed right-clicking a quest (outside edit mode) ignoring the "Hide Details Until Startable" property

## [2001.4.5]

### Fixed
* _Actually_ fixed the issue that should have been fixed in 2001.4.4
  * The previous fix worked for 50% of possible stack sizes, and the wrong 50% was used in testing...

## [2001.4.4]

### Fixed
* Fixed issue where larger itemstack sizes when creating item tasks led to a "missing" (empty) item in the task

## [2001.4.3]

### Fixed
* Fixed positioning of text entry popup when adding XP or XP Levels rewards to a quest
  * Popup was being positioned off-screen depending on game resolution and quest screen scroll position

## [2001.4.2]

### Fixed
* (Fabric only) Mod dependency fix causing conflicts with FTB Library version

## [2001.4.1]

### Fixed
* Fixed popup textfield for Checkbox task creation not rendering (more precisely, rendering off-screen...)
* Requires FTB Library 2001.2.1, which also contains several GUI-related fixes
  * Fixed client crash when double-clicking some long text lines for editing in the view quest panel
  * Fixed keypresses getting ignored after popup textfields (e.g. creating Checkbox tasks) are dismissed
  * Fixed popup textfields rendering partially offscreen under some circumstances

## [2001.4.0]

### Changed
* Significant GUI overhaul and cleanup in several places (backported improvements from 1.20.4)
  * FTB Library 2001.2.0 is a requirement

### Added
* The position of the pinned quests panel can now be adjusted in client config (no longer stuck on right side of screen)

## [2001.3.5]

### Fixed
* Fixed crash under some circumstances (not always reproducible) when opening the Key Reference panel
* Fixed images rendering over and hiding quest dependency lines (bug introduced in last release)

## [2001.3.4]

### Added
* The color of dependency lines for uncompleted quests is now themable
  * Use the "dependency_line_uncompleted_color" setting in ftb_quests_theme.txt (a resource pack file)
  * Default is #B4CCA3A3, which is the same as the previous color (a washed-out red tint)
* Added "Icon Scaling" property for quests (default: 1.0)
  * Allows the quest icon to be scaled separately from the overall quest button size, which can be useful for a good appearance with some button shapes

### Fixed
* Fixed client-side config (from "User Preferences" button) not getting properly persisted
* User Preferences screen SSP pause behaviour now follows main screen's pause behaviour

## [2001.3.3]

### Added
* Tooltips for images (the "Hover Text" image property) now support using translation keys
  * Literal text still works, as before

### Fixed
* Mitigate performance hit for calculating displayable items when item filters (either FTB Filter System or Item Filter) are in use
  * There is an unavoidable client-side performance cost here which can be noticeable when opening a large quest book with many filters, but this should reduce it somewhat
* Fixed connection lines sometimes being rendered to invisible quest dependencies
* When a player leaves a party team, their claimed-reward data is now copied back to their own team
  * Although all quest progress is reset when a player leaves a party (to the point where they joined the party), claimed-reward data should be preserved so that rewards can't be claimed more than once by a player
* Fixed some text formatting in the key reference popup panel (text wasn't always wrapping correctly)

## [2001.3.2]

### Fixed
* Fix NPE when opening on empty quest book

## [2001.3.1]

### Added
* Added back support for the `{p}` expansion in command rewards
  * Although `@p` and `@s` are the recommended ways to get the player name, `{p}` is once more supported as a workaround for mods/plugins which don't properly use vanilla command parsing
* Improved copy/paste keyboard behaviour a little
  * Ctrl-C to copy now also works on images
  * If no quests/images are selected, Ctrl-C will work to copy the hovered quest/image

### Fixed
* A quest in the first chapter set as Autofocused (feature added in 2001.2.1) wasn't being autofocused on initial opening of the quest book
* Hopefully mitigate a large amount of server lag (only noticeable in very big quest books) when an item is crafted (or manually smelted)
* Fixed the "Always Invisible" chapter property not being correctly observed when not in edit mode
* Fixed toggling editor mode not working in an SSP world which has been opened to LAN to enable cheats

## [2001.3.0]

### Changed
* Reworked support for external item filtering mods significantly
  * Added FTB Quests API (see `ItemFilterAdapter` class) to allow for registration of external item filtering mods
  * Support for item filtering mods is now handled via FTB XMod Compat (version 2001.2.0+), which uses the above API
  * The Item Filters mod is now an optional dependency
  * The newly-released FTB Filter System mod is now also an optional dependency

### Fixed
* Control-left-clicking an image now correctly toggles its selected status

## [2001.2.1]

### Added
* Images can now be multiply-selected and moved in the editor GUI, same as quest and quest link buttons
  * Ctrl-A now selects all images in the chapter in addition to quests and quest links
* A per-chapter autofocused quest can optionally be defined
  * When switching to this chapter (either by clicking it in the chapter panel, or via the Tab and number keys), the view will center on the autofocused quest
  * Autofocused quest can be defined either in the chapter properties screen, or by right-clicking any quest and selecting the "Autofocused" option  

### Fixed
* When a reward is a simple item reward, the reward button tooltip shows proper tooltip data now (e.g. including enchanted book tooltips etc.)
* Fixed flawed implementation of the Task Screen item/fluid/energy handlers on Fabric
* Fixed occasional client-side crash related to the Observation task (possibly triggered by dimension changing) - thanks @RaphaelT7

## [2001.2.0]

### Added
* Added a new Task Screen Configurator item, which can be used to remote-configure a Task Screen
  * Sneak + right-click a Task Screen with the configurator to bind it
  * Right-click the configurator to configure the bound Task Screen
  * Limitation: Task Screen must be currently loaded and in same dimension as player
* Added a new "Sequential Task Completion" quest setting (and chapter default)
  * When true, tasks in the quest must be completed in the order they were added to the quest
  * Later tasks won't even be shown (outside edit mode) in the quest view panel until previous tasks are completed
* Added "Insert Link" button in the multiline quest editor
  * This allows quest "hyperlinks" to be embedded in quest text; when clicked, the quest view jumps to the linked quest
  * Prompts for a quest ID to link to when selected; ID's can be obtained by right-clicking any quest and selected "Copy ID"

### Fixed
* Item Reward icons in the quest view panel had no tooltip with default settings
* Fixed chapter & chapter group deletions not persisting correctly (deleted chapters & groups reappeared after a server restart)
* Fixed crash when pasting quests with invalid quest ID data in the clipboard

## [2001.1.7]

### Added
* Added new editor mode hotkeys for copying & pasting selected quest (thanks @adamico)
  * Ctrl-C copies the selected quest, if any. Note: currently requires exactly one quest to be selected, copying multiples is not supported at this time.
  * Ctrl-V pastes the copied quest
  * Ctrl-Shift-V pastes the copied quest without dependencies
  * Ctrl-Alt-V pastes the copied quest as a quest link
* Added new editor mode hotkey Delete to delete selected quests
  * Shift-Delete can also be used, to delete selected quests with no confirmation - beware

### Fixed
* Fixed "Hide Quest Details Until Startable" and "Hide Quests Until Dependencies Visible" chapter properties getting switched during server -> client sync
* Cleaned up stale kubjes support files (kubejs.classfilter.txt / kubejs.plugins.txt) - they're in FTB XMod Compat now
* Fixed creating a quest in multiplayer causing the quest view panel to open for all admin players
  * Now only opens for the player who created the quest

## [2001.1.6]

### Fixed
* Fixed client crash when using Task Screens with energy tasks
* The output of `/ftbquests reset_progress` is now just sent to the command issuer, not all online admins

## [2001.1.5]

### Added
* The quest view panel is now opened automatically when a new quest is created via the GUI
  * Makes it easy to quickly edit title/subtitle/etc. via the "Edit" dropdown button in the view panel

### Fixed
* Server crash while building creative mode tabs (with specific mod combinations)

## [2001.1.4]

### Added
* Command rewards now have a "Silent" boolean property; when true, any command output (success or failure) is suppressed
* Added a `/ftbquests open_book <id>` command to open the book to a specific quest, quest link, chapter or task
* When moving images on the quest screen, scaled-up images are always treated as if they were size 1.0
  * Otherwise, scaled images have a huge snap jump, making them difficult to position neatly
  * Note also that holding Shift while moving images and quests disables grid snap completely 
* Added "Pause SSP Game" option to "Edit File" config screen (default false)
  * Game will pause when in quest screen in single-player mode (although not while in edit mode config screens)

### Fixed
* Fixed client crash while logging in under certain circumstances (related to tooltip generation for Quest Barrier block item)

## [2001.1.3]

### Fixed
* A few internal clientside performance fixes around network sync and refresh, nothing player visible
* Fixed a couple of translation keys
* Cleaned up Loot Crate item tooltips a little
* Fixed optional quests not correctly counting as optional for chapter completion purposes
* Fixed chapters with only quests links being considered empty
  * Showed the red "X" mark in edit mode and hidden entirely outside edit mode

## [2001.1.2]

### Fixed
* Fixed occasional client issue with client player being null when a creative tab rebuild done
  * Led to player login to dedicated server sometimes failing

## [2001.1.1]

### Changed
* Ported to Minecraft 1.20.1
  * Note that **FTB XMod Compat** is recommended for cross-mod integration (FTB Ranks / Luckperms, JEI / REI, Game Stages)
  * No KubeJS support in this release (KubeJS is not available on 1.20.1)
* Scroll wheel behaviour changed; scrolling now scrolls up and down; hold Shift to pan left and right; hold Ctrl to zoom in and out
  * Old behaviour can be restored via local preferences (see below)
* Quest and Chapter edit screens have been reorganised into subsections to reduce the "wall of text" effect
* Editing mode status information has been cleaned up into a status bar at the bottom of the screen

### Added
* New clientside local preferences are stored; can be accessed via the player head icon in bottom right of the GUI
  * Currently only stores scroll-wheel behaviour, but likely to expanded in the future
* Now supports a `ftbquests.editor` permission node (via FTB Ranks or Luckperms; FTB XMod Compat also required)
  * Allows players to be quest book editors without requiring full admin permissions
* Several quest settings now have chapter defaults
  * Quest Size (note that the quest size is now 0 by default in quests instead of 1; 0 means "use chapter default")
  * Repeatable flag
  * Consume Items flag for Item Tasks (in addition to the existing global "Consume Items" flag settable via "Edit File")
* There is a new in-game "Key Reference" screen, available via the grey "Info" icon on the right-hand toolbar

## [1902.4.18]

### Changed
* Removed the feature whereby double-tapping Shift opens the quest search GUI
  * This caused problems for player whose keyboards send multiple keyup/keydown events for modifier keys like Shift
  * Use Ctrl+F to open the search GUI instead

### Fixed
* Fixed issue with quest dependency validation (after adding a dependency via the quest edit screen) sometimes taking far, far too long
  * Likely to be an issue with large complex quest trees with many interleaving dependencies
* Fixed the mouse pointer warping to the screen center when opening FTB Quests from the sidebar button
* Fixed a client crash when opening a quest via JEI integration

## [1902.4.17]

### Fixed
* Open Quest & Stage Barrier Blocks no longer cause suffocation damage to players passing through them
* Per-player data is now properly treated as such within the team data file, where previously it was all conflated into a single setting per team. This includes:
  * Editing mode for each player
  * Whether the chapter side panel is pinned open (via the pin on the chapter panel)
  * Explicitly pinned quests (via the pin on the quest view panel)
  * Quest auto-pinning (via the pin at the bottom right of the GUI)
* Multiline quest editor: focus is now correctly returned to the editor after formatting code is inserted via the toolbar
* Fixed a problem where the GUI could get stuck in an open-close loop with REI's recipe viewer (and possibly other mods which have a "back" behaviour)

## [1902.4.16]

### Added
* Tasks may now be set as optional
  * Optional tasks don't need to be completed for their quest to be completed
  * Intended for use as informational tasks, e.g. if some item is involved in quest completion but not required to be turned in, this can be used as a way to show the item's recipe in the quest view panel
  * Typically used in addition to non-optional tasks (a quest with *only* optional tasks will autocomplete as soon as the player logs in!)
* Biome and Structure tasks now allow selection from a list of known biomes/structures, including tags (instead of requiring the name to be typed in)
* Saved quest book SNBT data is now sorted by key name
  * This was done since default key sort order is unpredictable and led to seemingly random changes in key ordering, which caused annoying spurious updates for quest book data under version control (which it should be for any major project)
  * This change will cause a one-time large version control change, but eliminate future unpredictability
* Very large reward table tooltips will now slowly scroll if too large to fit on screen (instead of cutting off at 10 entries)
* Observation tasks now have a default "Observe: <block-or-entity>" title

### Fixed
* Ensure that quest dependency loops are checked for when dependencies are edited via the large "Edit" screen
  * Note that it's much easier to add/remove dependencies by selecting a quest (Ctrl + Click), then right-clicking another quest and choosing "Add Selected as Dependency" or "Add as Dependency to Selected" as appropriate (and this method has always checked for dependency loops)
* Fixed potential server lag when players log in if they have uncompleted structure detection tasks in the quest book
* Fixed issue where "Terrain Loading" or "Terrain Building" message sometimes briefly (or not so briefly) flashes up when closing FTB Quests GUI 
* Added boolean "Hide Quests until Dependencies Visible" setting to chapter properties (default false)
  * This is a default for the corresponding tri-state quest property "Hide Quest until Dependencies Visible"
* Fixed a server->client desync causing weird behaviour with repeatable quests which have multiple rewards

## [1902.4.15]

### Fixed
* Fixed some Quest GUI operations causing other players' open GUIs to close, on multiplayer servers
* Another fix to item autodetection

## [1902.4.14]

### Added
* New boolean quest property "Hide Dependent Lines".
  * Default is false; if set to true, lines to dependent quests (quests which are unlocked _by_ this one) are hidden unless the quest is hovered.

### Fixed
* Fixed items not always getting detected for item task completion (depending on which GUI screen was open at the time)
  * Related to performance fixes added in 1902.4.13
* Multiline quest description editor: closing the image selection screen by pressing 'E' no longer inserts an 'e' into the editor

## [1902.4.13]

### Fixed
* Fixed crash when using Add -> Text on an empty quest description
* Fixed (or mitigated) excessive server CPU usage while scanning player inventories for item task completion in some situations
  * Player inventories are now scanned no more than every 20 ticks (configurable - see "Item Auto-detection Minimum Interval" setting in quest file settings)
  * Raising the interval is kinder to server TPS, but does result in a longer delay between picking up an item and getting a related quest completion update. Default delay of 1 second is reasonable, while avoiding lag on the server.
  * Items equipped in armor slots are no longer checked. Some modded armor items (Mekanism, PneumaticCraft, etc. can have rapidly changing NBT, which caused unnecessary inventory rescans).
* Merged the Edit and Add dropdowns in the view quest panel into a single Edit dropdown. Two buttons was unnecessary and took up excessive screen space, potentially overlapping the page number display in multi-page quests.
  * Also added more hotkeys to the view quest panel: "P" adds a page break, "I" appends an image, "L" appends a line

## [1902.4.12]

### Added
* Added a proper multi-line text editor for editing quest description text
  * Also includes a small toolbar with some handy operations (adding formatting, pagebreaks, images & undo functionality)
  * Hotkeys are available for most operations; see tooltips on each toolbar button
  * Standard edit box hotkeys (Ctrl-C, Ctrl-V, etc) all work too
  * Full text selection support; also, double-click on a word to select the word
* Multi-page support for quest description text, useful for very large descriptions
  * Split text into pages by adding a literal `{@pagebreak}` code on its own line
  * Alternatively, use "Add" -> "Page Break" in the quest view panel to add a new page after the current one
  * Multi-page descriptions have left & right buttons in the lower left, along with a page number display
  * Alternatively, you can move between pages with Page Up & Page Down keys, or by scrolling the mouse
* Quest view panel has a new "Edit" button (in edit mode only, of course) for convenience
  * Dropdown for quick edit of title, subtitle and description text
  * Note: hotkeys already existed for this (T/S/D) but this makes it clearer
* When creating observation tasks, the default block is now what the player is looking at, not just `minecraft:dirt`
* Creating a fluid task now pops up the fluid selection screen instead of defaulting to water
* The task button context menu (in the view quest panel) now includes a "Use as Quest Icon" entry
  * Convenient if your quest has multiple tasks, and you want to choose a single fixed icon for the quest
* When multiple quests are selected, the context menu now includes a "Change Size for all..." entry

### Fixed
* Task screens now correctly filter what fluids they accept
* Fixed translation keys not working in task button tooltips
* Fixed GUI view sometimes re-centering to (0,0) after adding or moving an image
* Fixed quest rewards and loot crate output not always showing up in JEI/REI display

## [1902.4.11]

### Added
* Quest connection lines now animate when a quest is hovered with the mouse (as well as when clicked to select)
  * Makes the quest flow a little more obvious
* Pre-completed quests in flexible mode now render dark like other uncompleted quests
  * The grey tick icon is still shown

### Changed
* Bumped the minimum version of FTB Lib

### Fixed

* Fixed item names for certain items appearing twice in task tooltips
* Fixed chapter panel not scrolling after group collapsed
* Tasks with custom titles now only show the title in the tooltip

## [1902.4.10]

### Added
* Improvements to image objects
  * When an image is added, the image selection GUI immediately appears now, instead of creating an object with a default image which will almost always need to be changed first thing anyway
  * Images with a non 1:1 aspect ratio are now added with a height of 1, and a width automatically chosen to match the aspect ratio
  * When the width/height of an image (as set in the editable properties) doesn't match the image's aspect ratio, "Fix up Aspect Ratio" context menu entries appear, allowing the width/height to be auto-adjusted accordingly
  * Added an "Image Tint" field to the image config, which can be used to tint images with an RGB hex code
  * Added an "Image Alpha" field to the image config, which is a 0..255 alpha value for the image
  * Note that empty tinted images can be used to add blocks of color, if desired
  * Added an "Image Ordering" field which can be used to adjust the stacking order of any overlapping images in a chapter
    * Defaults to 0; higher order-valued images are drawn on top of lower order-valued images 
* Quest reward weights are now all handled as floating-point values
  * This makes it much easier to adjust and fine-tune reward weightings if your current weights are small numbers

### Fixed
* Fixed an issue with multiline quest titles showing a unicode "LF" character in the quest tooltip when mousing over it
* Fixed the T/S/D keys working to edit quest title/subtitle/description outside edit mode
  * A cosmetic problem, since the server wouldn't accept any clientside changes made by users not in edit mode
* Fixed reward table title and icon not updating in the GUI (until client restart) after editing
  * Another cosmetic problem (updated title & icon did get sent to server correctly)

## [1902.4.9]

### Fixed
* Fixed NBT comparisons failing on data reloaded from disk
* Fixed tooltips for Image objects not supporting color markup codes
* Several fixes from FTB Library (FTB Library 1902.3.14-build.182 or later required)
  * Fixed fluid selector screen skipping fluids with source blocks instead of without (inverted check)
  * Using `command:<cmd>` as an Image object "click" field now works properly again
  * Fix crashing on invalid data in the Image object "click" field (now logs a client-side warning)
  * Animated textures (e.g. fluids) now work correctly when used as textures for Image objects

## [1902.4.8]

### Fixed
* Hotfix: network sync failure on certain combinations of quest property settings

## [1902.4.7]

### Added
* New Item Task property "Only From Task Screen", false by default
  * When true, items can only be submitted via Task Screen, and the "Submit" button is greyed out when clicking the task 
* New quest option "Hide Quest Details Until Startable" - when true, quests are visible in the quest tree but can't be opened to view their description or tasks until all dependencies are complete
  * Can be also be set on a per-chapter basis (true or false)
  * Quests use the chapter setting by default, chapter setting is False by default
* Tasks can now be copy/pasted like quests can, using the "Copy ID" context menu entry
  * Can be pasted using the context menu from the "+" add task button in the quest view panel, or as a new quest on the main quest screen
* The `/ftbquests reload` command can now also be run from the server console
* Item Task tag matching now does a full recursive check of the item NBT, even when "Weak NBT Matching" is true
  * Reminder: in weak-matching mode, NBT fields in the item being checked but not in the filter do not cause a match failure
  * This does not apply if using filters from the Item Filters mod - that mod has its own NBT matching functionality

### Fixed
* In flexible mode, quests which have uncompleted depdendcies are now rendered darker (even if their tasks can be progressed)
* Task Screens now only accept items for tasks which are in "Consume Resources" mode
* Fixed chapters with the same name but in different groups overwriting one another when saved to disk

## [1902.4.6]

### Added
* Quest Links can now be configured more independently of the quest they link to
  * They can have their own size and shape now
  * Editing a Quest Link via the "Edit" context menu now pops up a config screen with only the link properties
  * A new "Edit Linked Quest" context menu entry has been added to allow editing of the original quest via the link GUI item
  
### Fixed
* Fixed changes to Task Screens not being sync'd to nearby players on SMP 
* Fixed the "Optional" quest flag not being sync'd correctly to clients
* Fixed Observation task crashing if given a resource location with bad syntax (for block tag and entity type tag observation types)

## [1902.4.5]

### Added

* Made locked quests (which can't be started due to incomplete dependencies) more obvious in the GUI
  * Tooltip text when hovering the button states it's locked
  * The dependency button (left arrow) in the view quest panel now pulses more obviously if the quest is currently locked
* Quest link icon is now shown (top-left) in the view quest panel outside of edit mode too; click it to jump to the original quest
* The view quest panel for quest with links now shows a second link button (top-right); click it to get a context menu of quests which link to this one

### Fixed

* Restored the "Delete" context menu entry for Rewards which went missing in the last release
* Prevent invisible quest buttons from consuming mouse clicks (e.g. when an invisible quest is in the same position as a visible one)
* Fixed dependency lines being drawn to invisible quests (and appearing to end... nowhere)
* Fixed Task Screens allowing item filters to be pulled out of them
* Fixed Task Screens not rendering items when the task has more than one type of item (e.g. when using an "OR" or "Tag" filter)

## [1902.4.4]

### Added
* The currently selected chapter is now marked on the left-hand chapter panel with a small arrowhead
* (Editor mode) Added the ability to copy/paste quests (with or without dependencies) and quest _links_
  * Use the existing "Copy ID" context menu action on a quest
  * When creating new quests, additional paste options will be available if the current clipboard contents is a valid quest ID
  * Quest links may be useful to provide continuity of progression; you can paste a link to an existing quest in a new chapter
* (Editor mode) Added the ability to copy/paste images (new context menu action when right-clicking an image)
* (Editor mode) Cleaned up the context menus for quest objects a little, by moving quick property adjustments into a submenu
  * Existing context menu was getting far too large and unwieldy, for quests in particular
* (Editor mode) Added a new "flexible" progression mode for quests
  * Quests in "Flexible" mode can have their tasks progressed (but not fully completed) even if dependencies are not yet completed, effectively allowing "pre-completion" of quests
  * Progression mode can be set on quests, chapters, or the entire quest book; the mode is inherited from above if set to "Default", which is the case for quests and chapters
  * Default progression mode for the quest book is "Linear" mode, where dependencies must be completed before a quest can be started (i.e. current behaviour)
* (Editor mode) Quest titles can now contain embedded newlines (just put a "\n" sequence in the title text)
  * Useful for very long quest titles which would cause an excessively wide quest view panel
* (Editor mode) When moving one or more quests, "Moving" is displayed in the bottom left of the screen, along with the "<x> selected" message
  * More clearly differentiates selecting quests and moving them, which are similar but different operations

### Fixed
* (Editor mode) Fixed NPE when clicking in certain areas of the quest description text in the view quest panel
* (Editor mode) Fixed NPE after creating a new quest under some circumstances
* Background images now display correctly again (1.19-specific fix, due to the way MC 1.19 text component handling has changed)

## [1902.4.3]

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
  - The `change_page` action (normally only used in vanilla books) can be used in quest text to switch the quest view to another chapter, quest, or task. Use the long hex ID for the object you want to switch to; the ID for any object can be grabbed via the "Copy ID" context menu action
  - Old-style markup (previously used by FTB Quests) is still supported for brevity (that is, lines which start with `{`, e.g. `{my.translation.key}`)
- Added quick-edit hotkeys to the quest view popup
  - "T" edits the quest title
  - "S" edits the quest subtitle
  - "D" edits the quest description
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
- The Location Task config screen has had tooltips added to the X/Y/Z entries to clearly explain how they are interpreted in conjunction with the W/H/D entries
- The outline colors for locked quests and unlocked-but-unstarted quests are now themable via the `ftb_quests_theme.txt` resource pack file (previously these quest types were hardcoded to grey and white outlines, respectively)

### Fixed
- Fixed an NPE in the Stats Task if the resource location is unknown
- Pressing Escape in various screens now goes back to the previous screen instead of closing the entire GUI
  - Similarly, pressing Escape when viewing a quest pops down the quest view, and when a context menu is popped up, that will be popped down
- Full JEI support has returned for quests (items -> rewards) and loot crates (reward table -> list of reward items and their weights)
- The quest auto-pin button now works again to display progress of all available quests
- Fixed item durability bars in quest buttons rendering over the chapter panel
- Fixed Dimension Tasks triggering even when dependencies weren't met
- Fixed the quest completion notification icon being missing from the quest book sidebar button when using REI instead of JEI

#### For content creators
- The Checkmark Task config screen no longer allows editing of the icon (checkmark tasks always just display a tickbox, so it wasn't useful)
- The focused grid position (where a quest would be added) is now highlighted again
- Fixed garbled text when confirming deletion of multiple quests
- Clicking a greyed out item in a context menu no longer closes the menu
- Fixed certain context menu items crashing the client when clicked
- Fixed quest-specific (i.e. the long hex quest ID) theme data not being loaded from `ftb_quests_theme.txt` (note this file can be overridden in resource packs) 
- Better behaviour for the multiline text editor used to edit quest description paragraphs (right click quest -> Edit -> Description)
- Fixed Reward Table editing screen shrinking every time it was reopened when adding/deleting entries
