# Example Dimension Configurations

This folder contains example JSON configuration files for creating custom dimensions with The Escape Mod Functions.

## Usage

1. Copy any of these example files to your `config/temf/dimensions/` folder
2. Modify the values as needed for your use case
3. Restart your server or use `/temf reload` to load the new configuration

## Examples Included

### void_spawn.json
A medium-sized void dimension (5000 block border) perfect for spawn areas and loading zones.

### small_testing_area.json  
A smaller void dimension (1000 block border) ideal for testing builds or small events.

### large_build_space.json
A large void dimension (20000 block border) for massive building projects or events.

## Configuration Options

- **name**: Must be unique across all dimensions
- **worldBorder**: Distance in blocks from center (0,0) to the border
- **type**: Currently only "void" is supported

## Tips

- Start with smaller borders and increase as needed
- Choose descriptive names that make sense for your use case
- The mod will automatically create an example dimension on first run
