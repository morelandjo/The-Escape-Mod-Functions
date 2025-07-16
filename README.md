
# The Escape Mod Functions

A Minecraft NeoForge 1.21.1 mod that provides helper functions and utilities for The Escape modpack. This mod uses a **hybrid approach** - it generates standard Minecraft datapacks for dimension creation while providing additional utilities for world border management and configuration.

## Features

### Hybrid Dimension System

This mod combines the best of both worlds:
- **Datapack Generation**: Creates standard Minecraft datapack files for dimensions
- **Configuration Management**: Simple JSON configs for easy dimension setup
- **World Border Management**: Automatic world border application
- **Admin Commands**: Easy management and debugging tools

#### How It Works

1. You create simple JSON configuration files
2. The mod generates proper Minecraft datapack files automatically
3. Minecraft loads the dimensions normally (no special registration needed)
4. The mod applies world borders and provides management commands

#### How to Use

1. Start your server/world at least once with the mod installed
2. Navigate to your `config/temf/dimensions/` folder
3. Create JSON files for your custom dimensions
4. The mod automatically generates a datapack at `datapacks/temf_generated/`
5. Use `/temf borders` to apply world borders or restart the server

#### Example Dimension Configuration

```json
{
  "name": "void_spawn",
  "worldBorder": 5000,
  "type": "void"
}
```

#### Configuration Options

- **name**: The identifier for your dimension (must be unique)
- **worldBorder**: Sets the world border size (e.g., 5000 = 5000 blocks in each direction from 0,0)
- **type**: Currently only supports "void" (empty dimension with no blocks)

#### Supported Dimension Types

- **void**: An empty dimension with no terrain generation, perfect for spawn areas and loading zones

## Commands

The mod provides several admin commands (requires OP level 2):

- `/temf reload` - Reloads dimension configurations and regenerates datapack
- `/temf list` - Lists all dimensions with their status (loaded/not loaded)
- `/temf borders` - Applies world borders to all configured dimensions
- `/temf check <dimension>` - Shows detailed info about a specific dimension including teleport command

## Teleporting to Dimensions

Once your dimensions are loaded, use standard Minecraft commands:

```
/execute in theescapemodfunctions:void_spawn run tp @s ~ ~ ~
```

Replace `void_spawn` with your dimension name. Use `/temf check <dimension>` to get the exact command.

## File Structure

```
config/temf/dimensions/           # Your JSON configuration files
datapacks/temf_generated/         # Auto-generated datapack (don't edit manually)
```

## Installation

1. Download the mod JAR file
2. Place it in your `mods/` folder
3. Start your Minecraft server or game
4. The mod will automatically create the necessary directories

## Configuration

The mod creates configuration files in `config/temf/`:
- `dimensions/` - Folder containing your dimension JSON files
- Main config options are available in the standard NeoForge config system

## Planned Features

- Structure placement in void dimensions
- Additional dimension types (flat, custom terrain)
- Portal creation and linking
- Dimension templates and presets

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.186 or later

## License

All Rights Reserved

---

*This mod is specifically designed for The Escape modpack and provides foundational utilities for advanced gameplay mechanics using a hybrid datapack + mod approach for maximum compatibility and simplicity.*
