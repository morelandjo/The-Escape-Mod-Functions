# TEMF Hybrid System Usage Guide

## How the Hybrid System Works

The Escape Mod Functions uses a hybrid approach that combines the simplicity of JSON configuration with the power of Minecraft datapacks:

### 1. Configuration Phase
- You create simple JSON files in `config/temf/dimensions/`
- Example: `void_spawn.json`

### 2. Generation Phase  
- The mod reads your JSON configs
- Automatically generates proper datapack files in `datapacks/temf_generated/`
- Creates both dimension types and dimensions following Minecraft standards

### 3. Loading Phase
- Minecraft loads the dimensions normally (no special mod registration needed)
- The mod applies world borders based on your configurations
- Dimensions become accessible via standard commands

## Step-by-Step Setup

1. **Install the mod** and start your server once
2. **Navigate to** `config/temf/dimensions/`
3. **Create your dimension configs** (see examples below)
4. **Restart server** or use `/temf reload`
5. **Verify with** `/temf list` - should show dimensions as "Active"
6. **Teleport using** standard Minecraft command:
   ```
   /execute in theescapemodfunctions:your_dimension_name run tp @s ~ ~ ~
   ```

## Example Configurations

### Small Testing Area
```json
{
  "name": "test_area",
  "worldBorder": 500,
  "type": "void"
}
```
Teleport: `/execute in theescapemodfunctions:test_area run tp @s ~ ~ ~`

### Large Build Space  
```json
{
  "name": "creative_space",
  "worldBorder": 10000,
  "type": "void"
}
```
Teleport: `/execute in theescapemodfunctions:creative_space run tp @s ~ ~ ~`

### Main Spawn Dimension
```json
{
  "name": "void_spawn",
  "worldBorder": 5000,
  "type": "void"
}
```
Teleport: `/execute in theescapemodfunctions:void_spawn run tp @s ~ ~ ~`

## Management Commands

- `/temf list` - See all dimensions and their status
- `/temf check void_spawn` - Get detailed info including teleport command
- `/temf reload` - Reload configs and regenerate datapack
- `/temf borders` - Reapply all world borders

## File Structure After Setup

```
config/temf/dimensions/
├── void_spawn.json
├── test_area.json
└── creative_space.json

datapacks/temf_generated/
├── pack.mcmeta
└── data/theescapemodfunctions/
    ├── dimension_type/
    │   ├── void_spawn.json
    │   ├── test_area.json
    │   └── creative_space.json
    └── dimension/
        ├── void_spawn.json
        ├── test_area.json
        └── creative_space.json
```

## Benefits of This Approach

- ✅ **Simple Configuration**: Easy JSON files for dimension setup
- ✅ **Standard Compatibility**: Uses normal Minecraft datapack system
- ✅ **No Complex Registration**: Dimensions load like any datapack dimension
- ✅ **World Border Management**: Automatic application of borders
- ✅ **Easy Debugging**: Clear commands to check dimension status
- ✅ **Portable**: Generated datapack works on any server
- ✅ **Hot Reload**: Change configs without restarting server

This hybrid approach gives you the best of both worlds - simple configuration with full Minecraft compatibility!
