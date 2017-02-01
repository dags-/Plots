## Setting up a PlotWorld
To create a new PlotWorld, you must first create a WorldGenerator for it.  
The WorldGenerator determines how large plots will be, how wide the boundary walls and paths will be, and what materials will be found at various heights in the world.

**Note**  
Area protection in a PlotWorld is bound to the dimensions set in it's WorldGenerator. As such, the sizes of the plots, paths, walls cannot be changed _after_ the world has been created.

### 1. Create a WorldGenerator
1. Start editing a new WorldGenerator: `/gen create <name of generator>`
2. Define generator properties:
  1. Plot size: `/gen dim plot <plot width> <plot breadth>`
  2. Wall width: `/gen dim wall <width>`
  3. Path width: `/gen dim path <width>`
  4. Biome: `/gen biome <biome name>`
  5. Gamerule: `/gen rule <gamerule name> <value>` _(can be used multiple times for different rules)_
  6. Define material layer(s): `/gen layer <plot material> <path material> <wall material> <layer thickness>`
3. Save the WorldGenerator: `/gen save`

###### On 'Material Layers':
You can define what materials should be used throughout the plotworld, at different layers, using the `/gen layer` command.  
Commands for the default WorldGenerator would like something like this:
 1. `/gen layer bedrock bedrock bedrock 1` (ie the base of the world will be bedrock)
 2. `/gen layer dirt stone bedrock 5` (underground layer, 5 blocks deep)
 3. `/gen layer grass gravel bedrock 1` (ground layer, 1 block deep)
 4. `/gen layer air air stone_slab 1` (above-ground layer, 1 block high - ie plots will be bordered by stone_slab blocks)

### 2. Create a PlotWorld
Once you have set up a WorldGenerator, you can use it to create a new PlotWorld:
 1. Create the world: `/plotworld create <generator name> <new world name>`
 2. Teleport to the world: `/worldtp <new world name>`
