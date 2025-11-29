# Right Proper MCPatcher <img src=".idea/icon.png" align="right" width=150>

<sup>**[CurseForge](https://www.curseforge.com/minecraft/mc-mods/mcpatcher) | [Modrinth](https://modrinth.com/mod/mcpatcher)**</sup>

A clean and improved reimplementation of MCPatcher features for Minecraft 1.7.10.

This mod provides various features for texture pack artists and modpack designers, such as:

- Random mob textures
  - Mobs can have different textures applied to them, based on biome, height, or even just purely randomized.
- CTM connected textures
  - Connected glass and other block textures, with seamless tiling
- Better Glass
  - Resource packs can provide glass textures with semi-translucent effects
- Custom Item Textures
  - Different item textures based on metadata, count, NBT, etc.

Planned features:

- Custom Colors for grass and water based on biomes
- Custom Skyboxes

## Usage

This mod implements several features based on the OptiFineDoc documentation:

- Connected textures: https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/ctm.properties
- Random mobs: https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/random_entities.properties
- Custom item textures: https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/cit_single.properties

Note: the `ft_default` entry in the ctm properties shipped in this mods jar is an internal implementation detail.
Please ignore it, and do not use it in your resource packs!
This is needed because the alternative for having this marker would be an expensive resource pack scan,
which would significantly slow down resource reloads.

## Credits

Based on MCPatcher and MCPatcher: Forge, developed by `prupe` and `mist475`, respectively.

@DarkBumus for hunting down bugs and early feature testing.