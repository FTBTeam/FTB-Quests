# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2101.1.22]

### Fixed
* Fixed Task Screen setup GUI showing blank task names when configuring a new task screen for the first time

## [2101.1.21]

### Added
* Added a "Copy ID" menu item to the context menu in the Reward Table editor screen

### Changed
* FTB XMod Compat 21.1.7+ is now required, if that mod is in your instance
* The entity selector screen for the Kill task now shows entity ID's as well as the display name
  * It's entirely possible for two mods to use the same display name for two different entities, leading to confusion
* The `/ftbquests generate_chapter_with_all_items_in_game` command has been overhauled and improved:
  * `/ftbquests generate_chapter from_entire_creative_list` - replaces the above command.  Beware: this command can cause a lot of server lag!
  * `/ftbquests generate_chapter from_inventory` - creates a new chapter with an item task & quest for each item in the inventory a player is looking at
  * `/ftbquests generate_chapter from_player_inventory` - creates a new chapter with an item task & quest for each item in the player's inventory/hotbar (not armor slots)
 
## [2101.1.20]

### Added
* Support for FTB Teams team stages, FTB Teams 2101.1.8+ required
  * These are like gamestages, but per-team instead of per-player
  * Added "Team Stage" boolean option (default false) for Stage Tasks to check for a team stage instead of player-based game stage
  * Stage Reward now grants a team stage if "Team Reward" is true
* The text color for not-started chapters in the left-hand chapter panel is now themable
  * It uses the `quest_not_started_color` value from `ftb_quests_theme.txt` (default is white as before)

### Fixed
* Fixed some logic errors related to quest exclusion and flexible mode
* Fixed UI bug causing the multiline text box editor to occasionally go blank

## [2101.1.19]

### Changed
* Updated `pt_br` translation (thanks @PrincessStellar)

### Fixed
* Fixed completing quests via `/ftbquests force_progress` not handling autoclaim rewards
* Fixed third party mods being able to crash FTB Quests during entity scanning for kill task entity selection
* Added missing chapter default setting for "Hide Text Until Quest Complete" tristate setting in quest properties
  * Additionally, if this is true, then description text will now be shown when in edit mode along with an "eye" icon indicating it would otherwise be hidden

## [2101.1.18]

### Added
* Added configurable repeat cooldowns for repeatable quests
  * See the new "Quest Repeat Cooldown" field in the Misc section of the quests properties screen
  * Repeat cooldown is triggered when the first player on the team claims the quest reward for the quest
* More visually attractive highlighting of the currently-selected chapter
  * Also added two new fields to `ftb_quest_theme.txt` resource pack file: `selected_chapter_highlight_1` and `selected_chapter_highlight_2`

## [2101.1.17]

### Added
* Support for the color picker widget when using the Multiline Text editor. Click the color swatch button on the dropdown to open the color picker. This will insert a special color code `&#RRGGBB` into the text at the current cursor position.
  * Also added support for our rainbow text color under `§z` or `&z` (both work). This will make the text cycle through rainbow colors.

### Changed
* Updated a couple of icons on the multiline text editor toolbar for better clarity

### Fixed
* Fixed potential memory leak on server shutdown (incorporated from All The Leaks, credit to them)
* Fixed bug causing chapters to not display if all quests or all quest links are currently invisible ("or" should be "and" there!)
* Fixed inserting links in the multiline quest description editor sometimes causing a client crash
* Fixed custom task data not always getting sync'd to the client when necessary
* Pending autoclaim rewards are now checked for when a player joins a team
* Multiline text editor not always clearing all formatting

## [2101.1.16]

### Added
* Quest icons (as selected by the "Icon" field in config screens) can now also use entity faces (similar to the faces used on the FTB Chunks map)
  * Clicking the "Icon" field now shows a context menu with the option to choose an item texture, image texture or entity face texture
  * Shortcuts: shift-click for items, ctrl-click for images, alt-click for entity faces

### Changed
* Currency reward type now has a default title of "⬤ <amount>"

### Fixed
* Fixed crash when creating an item task with a large (> 99) item count

## [2101.1.15]

### Added
* Quest and Stage Barrier blocks can now optionally teleport players who walk into them (once the related quest/stage is completed)
  * Right-click the block in edit mode to configure teleportation settings
* Added a Currency Reward, transfers an amount of some currency directly to player's wallet
  * FTB Library 2101.1.20+ and FTB XMod Compat 21.1.6+ required
  * Currently only Magic Coins is supported as an implementation but others may be added in future

### Fixed
* Fixed a crash with barrier shape checking

## [2101.1.14]

### Added
* Big overhaul of quest and stage barrier blocks
  * Can now be GUI-configured to select a quest by right-clicking, in edit mode
  * Creative pick-block copies current settings for easy replication
  * Renaming in Anvil still works, but is not the recommended way; easier to place one, configure and then creative-pick it to place more
  * (NeoForge only) Can also GUI-configure block camouflage and/or hide completely when open
  * Extra Jade support to conceal barrier info from non-editing players added to FTB XMod Compat 21.1.5+

### Fixes
* Fixed quests marked as invisible still showing on the pinned quests tracker
* Fixed chapter groups in the chapter side panel not being expandable/collapsible while in edit mode
* Fixed crash on SMP with the Toast Reward, added back in the previous release

## [2101.1.13]

### Added
* The fallback locale for missing translations can now be configured in the quest book properties; no longer hardcoded to `en_us`
  * If you're developing a modpack and your primary language files are in a locale other than `en_us`, you should update the fallback to that locale
  * The setting can also optionally be overridden on the client with "Fallback Locale" in the Player Preferences screen
* Alt + Left-Click (edit mode) now works to quick-open the properties editor for _all_ quest objects
  * Images, chapters, chapter groups, and also the quest book button at the very top of the chapter panel
* Improved title line for all properties editor screens (no longer just says "FTB Quests" in ugly bold text)
* Notification styles for completed quests & rewards can now be configured client-side
  * Use the "Player Preferences" button (bottom right) and see the new "Notifications" section
  * In addition to toast notifications, players can now use the chat window, action bar or disable notifications entirely  
* Improved the auto-pinned quests panel UI
  * When auto-pinning is enabled (the pushpin at the top right of the GUI), only quests in the last-viewed chapter are tracked on the game screen
  * Previously all quests in the quest book were tracked, which can become unmanageable in large quest books
  * Added "Pinned Quests" section to Player Preferences, with a couple of new settings:
    * Allow scaling of the pinned quests panel to be adjusted here (default: 0.75, as before; range 0.25 -> 2.0)
    * "Auto-pin Follows" setting to track entire quest book (if previous functionality is desired)
* Ctrl-P in the quests GUI is now a shortcut to open the Player Preferences screen

### Changed
* FTB Quests now logs a warning if the third party "FTB Quests Optimizer" mod is detected
  * This mod is _not_ recommended by FTB for use with FTB Quests; it will not improve performance, and may cause stability issues
* The entity selection screen for the Kill Task now only shows living entities, and sorts them nicely by display name
* Updated `pt_br` translation (thanks @Xlr11)

### Fixed
* Fixed players getting a chapter-completed notification for already-completed chapters when an optional quest in that chapter is completed
* The Toast Reward now actually works again
* Fixed some performance issues related to scanning quests & tasks after player inventory changes and on player login
  * This should use significantly less server CPU now, good for your TPS
* (Mod developers only) Fixed crash when using FTB Quests as dependency in a data generation environment
* The message displayed in client chat when quest files are saved locally can once more be clicked to open a file browser window

## [2101.1.12]

### Fixed
* Fixed task/reward reordering (added in last release) not always saving correctly on the server

## [2101.1.11]

### Added
* Added ability to move tasks and rewards left or right in the quest view panel (in edit mode)
  * Added "Move Left" and "Move Right" context menu entries when right-clicking tasks & rewards
  * Pressing cursor left or right while hovering tasks & rewards also moves the item left or right
* The Kill task now has the ability to use entity type tags (in addition to simple entity types)
  * If the Entity Type Tag field of the task is non-empty, it's used in preference to the entity type
* The Emergency Items screen is now a lot more player-friendly
  * Instead of forcing the player to sit at the emergency items screen for the entire countdown, the timer now counts down while the player is outside the screen

### Fixed
* Fixed issue where quests in flexible mode with multiple dependencies and not all dependencies required for completion were not getting completed

## [2101.1.10]

### Added
* Added "Hide Quests in excluded questlines" top-level (file) setting
  * False by default, so excluded quests will be shown as unavailable
  * Set to true to hide excluded quests completely

### Changed
* Loot crate items with no stored loot crate ID no longer display an "unknown loot crate" error in the tooltip
  * This is mainly for the benefit of JEI display for uninitialised loot crates where the error is unnecessary and misleading

### Fixed
* Fixed quest exclusion wrongly being detected under some circumstances (branching and merging questlines)

## [2101.1.9]

### Changed
* Added de_de translation (thanks @FlyonDE)

### Fixed
* More graceful handling when sync'ing items with invalid component data on player login
  * Should fix issues where players get kicked on login due to bad itemstacks in a quest book

## [2101.1.8]

### Added
* If all tasks in a quest are marked optional, now require at least one to be completed to complete the quest
  * Previously quest would just auto-complete, which is less useful (if that behaviour is really needed, use a quest with no tasks at all)
  * This allows for quests with a choice of two or more tasks, where completing any of the tasks will complete the quest

### Fixed
* Hotfix: fix player login exception related to exclusive quest branching feature added in 2101.1.7
* Tooltip on optional tasks now reads "Optional Task" instead of "Optional Quest"

## [2101.1.7]

### Added
* Added exclusive quest branching, where starting one quest makes certain other quests unavailable
  * New "Max Completable Dependents" integer property for quests
  * When that number of dependent quests is completed, other uncompleted dependents of that quest become unavailable to the player/team
* Improved importing of legacy quest data (from quest book data in 1.20.1 and earlier)
  * Item SNBT data is now imported, other than custom NBT data, which can't reliably be auto-converted to 1.21 item component data
* Added ja_jp translation (thanks @twister716)
* Added de_de translation (thanks @FlyonDE)
* Task Screens now show related quest names along with task names (and can search on those)
* New hotkeys when hovering chapters (same as 2101.1.6 changes made to quests):
  * Left-Alt & Left-Mouse opens directly to chapter properties
  * Right-Alt & Left-Mouse copies the chapter ID

### Fixed
* Add extra defensive null-checking to avoid crashes by mods incorrectly calling `Entity#die` with a null damage source

## [2101.1.6]

### Added
* New hotkeys when hovering quests:
  * Left-Alt & Left-Mouse opens directly to quest properties
  * Right-Alt & Left-Mouse copies the quest
* Added `/ftbquests change_progress <player> reset-all` and `... complete-all` commands
  * These are equivalent to the existing `/ftbquests change_progress <player> reset 1` and `... complete 1` but are clearer, avoiding the use of the magic "1" id which represents the whole quest book
* Added `/ftbquests reload quests` and `/ftbquests reload team_progress` variants to the existing `/ftbquests reload` command
  * Reload just the quest book data or the team progression data
* Added new quest theme property `"dependency_line_unavailable_color` to color lines drawn from currently locked quests to their dependents
  * Default colour is a slightly faded version of the `dependency_line_uncompleted_color` property
* Backspace key now actually moves back to previously viewed quest when pressed on the quest view panel (previously operated as Escape and just closed the panel)
  *  Can be disabled in client config to get old behaviour back, but why would you want to?
* The "click_event" -> "change_page" action in json text components in quest description text can now jump to a specific subpage of a quest if it has multiple pages
  * Example syntax: `[ { "text": "click me", "underlined": true, "clickEvent": { "action": "change_page", "value": "74D53BE3AB369184/2" } } ]` jumps to page 2 of the quest (note the `/2` on the end of the quest ID)

### Changed
* Now using the FTB Library 2101.1.10 config system
  * **IMPORTANT** the client config file `local/ftbquests/client-config.snbt` is now `config/ftbquests-client.snbt`
  * Existing configs are auto-migrated; players do not need to take any action
* Command rewards: replaced boolean "Run with Elevated Permission" with integer "Permission Level"
  * Permission level may be anything between 0 and 4 inclusive; see https://minecraft.wiki/w/Permission_level
  * Previous data is migrated; true value of "Run with Elevated Permission" maps to permission level 2
* Kill Entity task now has "Entity Type" and "Entity Name" properties
  * "Entity Type" is renamed from the old "Entity Name"
  * "Entity Name" can be used to require that the entity have a custom name (either a player name or a name from a name tag for non-player entities)
* Players no longer need to be in edit mode to do `/ftbquests reload` (but still must have editor permission, of course)

### Fixed
* Fixed chapter panel always starting open (and sliding shut) even if not pinned
* Fixed `/ftbquests import_reward_table_from_chest` command not correctly updating quest book id mappings for new reward table
* Quest view panel now uses the "quest_view_border" theme property from `ftb_quests_theme.txt` consistently now
  * Previously a mixture of "quest_view_border" and "widget_border" were used to draw the border lines for the view panel
* Fixed some quest button alignment issues depending on the zoom level of the quest panel
* Fixed autoclaim rewards being given to entire team even when marked as team reward
  * Also added tooltip to team reward setting in the reward properties GUI to clarify: team reward means one reward for the whole team
* Fixed multiline quest editor "L" (insert link) button sometimes inserting a spurious comma, depending on current text selection
* Leading/trailing whitespace is now silently trimmed from command text in command rewards (trailing whitespace could cause confusing failures to execute commands)
* Fixed "Disable in JEI" quest property not being correctly saved or sync'd to clients

## [2101.1.5]

### Changed
* The left-hand chapter panel now slides smoothly in and out (assuming it's not pinned)
* Separated `background` in `ftb_quests_theme.txt` into three separate properties for better resource pack configurability
  * `background` is used for the main quest panel background
  * `chapter_panel_background` is used for the chapter panel which pops out from the left (an opaque texture should be used here)
  * `key_reference_background` is used for the popup key reference panel
  * All three values by default use the existing `background_squares.png` image from FTB Library as before

### Fixed
* Fixed image aspect ratio calculations for icons with animated textures (as controlled by .mcmeta files)
  * Note: FTB Library 2101.1.9+ required
* Fixed rotated images sometimes not rendering when partially off-screen

## [2101.1.4]

### Fixed
* Fixed escaped ampersands in messages failing to parse (e.g "This \& That")
  * Fixes bug introduced by previous commit (parsing unicode escape sequences; this still works as before)

## [2101.1.3]

### Added
* Added "Hide Lock Icon" boolean quest property to allow the lock icon to be hidden on a per-quest basis
* Unicode escape sequences (e.g. `\u2022`) are now parsed in translation files
  * Note however that a double escape is required, e.g. `\\u2022`
* Added zh_tw translation (thanks @sheiun)
* Added tr_tr translation (thanks @RuyaSavascisi)
* Added uk_ua translation (thanks @GIGABAIT93)

### Changed
* Quests with "Hide Dependency Lines" set to false will now show the dependency line when (and only when) hovered with the mouse pointer
  * This is now consistent with the behaviour of "Hide Dependent Lines" and more useful in general, allowing dependency lines to be selectively shown

### Fixed
* Fixed quests in always-invisible chapters being searchable outside edit mode
* Fixed Quest Barrier blocks crashing in SMP environments

## [2101.1.2]

### Added
* Added a new "None" quest shape, which simply means to not draw any border around the quest icon
  * This doesn't affect the icon itself, which is still rendered as normal
* Locked quests (i.e. which can't be started due to dependencies) now show a small padlock icon
  * This can be disabled in player preferences if preferred - "Show Icon for Locked Quests"
* Added a new "All Table" reward type
  * This works on an existing reward table, and rewards the player with one of every reward in the table
* Added a new "Grab Copy of Item" context menu entry for item tasks in the quest view panel
  * This is intended to allow getting copies of items with custom components (e.g. FTB Filter System filters) if you don't have a copy of the item to hand

### Fixed
* Fixed a rare server-side crash which can occur when new (as in, never on this server before) players join the server
  * Timing issue with FTB Teams initialising their team data for the first time
* Fixed quest completion toasts appearing more than once if a quest has multiple optional tasks
* Image objects with no click action can now be clicked-through, allowing the background to be scrolled/panned
* Fixed issue with several ftbquests subcommands meaning they could not be used in MC functions
  * Commands are `/ftbquests change_progress`, `/ftbquests open_book`, and `/ftbquests export_reward_table_to_chest`
* Fixed tooltips sometimes rendering underneath context menus
* Fixed rewards in reward tables not being able to have a custom title set
  * Technical detail: rewards in reward tables (unlike rewards in quests) have historically not had a unique ID, but this ID is necessary for titles to work with the new translation system which the mod uses.
* Fixed recipes for craftable items (task screens etc.) not working
* Fixed loot tables for task screens not working
* The "Open Wiki" entry in the Settings context menu now opens the new FTB Quests docs at https://go.ftb.team/docs-quests
* The "Download Quest Files" entry in the Settings menu now also saves the `lang/` folder 
  * Caveat: only translations which the client knows about are included in this download (translations on the server that the client has not used won't be included)

## [2101.1.1]

### Added
* The pinned quests panel now has a "Pinned Quests" title for clarity
* Added a feedback option for command reward
  * In the case where running a command produces no obvious effect, this can help notify the player that something has happened

### Changed
* Overhauled and cleaned up many icon textures

### Fixed
* Fixed context menu tooltips sometimes appearing behind the context menu
* Fixed some issues with the reward table editor GUI (changes not getting correctly sync'd to server in some cases)

## [2101.1.0]

### Changed
* Minecraft 1.21.1 is now required; this no longer supports Minecraft 1.21

### Added
* Sidebar buttons for this and other FTB mods can now be enabled/disabled/rearranged (new functionality in FTB Library 2101.1.0)
* A few new template substitutions are available in command rewards
  * `{team_id}` - the short team name, e.g. "Dev#380df991"
  * `{long_team_id}` - the full team UUID, e.g. "380df991-f603-344c-a090-369bad2a924a"
  * `{member_count}` - the number of players in the team
  * `{online_member_count}` - the number of currently-online players in the team

### Fixed
* Fixed "Hide Quests until Dependencies Visible" setting actually checking for dependencies being _complete_
  * Added new "Hide Quests until Dependencies Complete" setting
  * So there are now two independent setting for hiding quests based on dependency visibility and/or completion

## [2100.1.5]

### Changed
* FTB Quests items are now registered to the `FTB Suite` creative tab instead of FTB Quests own tab
  * In practice, this means they share a tab with other FTB mods, but only FTB Filter System registers an item at this time

### Fixed
* Rotated images now have a correctly rotated hitbox
  * In addition, rotated images with a non-default aspect ratio now preview correctly during rotation
* Fixed copy/pasting images

## [2100.1.4]

### Fixed
* Fixed coloured text in quest titles & subtitles not showing in the quest view panel
* Command rewards now support a {team} substitution in the executed command, which is replaced with the player's short team name
  * The command setting in the reward properties screen now has a tooltip listing all available substitutions
* Filenames for new chapters are now all named after the chapter title, as intended
  * Pre-existing chapter files may be named after the hex chapter id; they will still work fine, but you can rename them if you wish
  * If you choose to rename them, also update the `filename` field in the file correspondingly

## [2100.1.3]

### Fixed
* Fixed adding tasks to existing quests sometimes losing the task type (leading to a '?' button appearing)
* Fixed images in the quest book not sync'ing to the client
* Fixed issue where using FTB Filter System filters would sometimes fail to find matching items for GUI display

## [2100.1.2]

### Fixed
* Fixed raw json text in quest descriptions not always being recognised
* Fixed a packet sync error related to translation system when on dedicated server
* Chapter filenames are now again named after the chapter title (at the time of creation), as they used to be in 1.20 and earlier

## [2100.1.1]

### Fixed
* Fixed chapter and chapter group creation popups moving in and out with the chapter panel when it's not pinned.
* Fixed crash when creating Kill and Advancement tasks
* Fixed Fluid Tasks not loading correctly from older quest book data
* Any translatable text loaded from older quest book data is now automatically migrated into the new translation system
  * A `lang/en_us.snbt` file will be auto-created under your `config/ftbquests/quests` folder when an older quest book is loaded
* Removed a misleading "Click to Submit" tooltip from fluid tasks in the quest view panel
  * Fluid tasks can only be submitted via a Task Screen

## [2100.1.0]

### Changed
* Ported to Minecraft 1.21. Support for Fabric and NeoForge.
  * Forge support may be re-added if/when Architectury adds support for Forge
* The way translations are handled has changed significantly in this release
  * Translation text is now stored separately from other quest data, under the `lang/` folder with the quest book folder hierarchy, in a file named after the locale, e.g. `lang/en_us.snbt`
  * This should make it easier to produce translations in the future, since all text is located in one place.
  * Editing language can be overridden via client preferences; default is to use whatever Minecraft language is in force. When text is edited in-game, it's stored in the appropriate language file based on the active editing locale. 
  * Text which doesn't have a translation in the current locale (but does in the `en_us` locale) is highlighted when in edit mode.
  * Changes do not affect the player experience

## [2004.2.1]

### Added
* The pinned quests panel positioned can now be adjusted in client config (use "Player Preferences" button in lower right of screen)
* A couple of other minor GUI fixes and improvements (mainly via FTB Library)

## [2004.2.0]

### Changed
* Ported to Minecraft 1.20.4. Supported on Forge, NeoForge and Fabric.
* Some GUI enhancements in a few places.

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
