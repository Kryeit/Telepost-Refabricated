<p align="center">
  <img width="200" src="https://kryeit.com/images/telepost_logo.png">
</p>

<h1 align="center">Telepost<br>
	<a href="https://www.curseforge.com/minecraft/mc-mods/telepost/files"><img src="https://cf.way2muchnoise.eu/versions/telepost.svg" alt="Supported Versions"></a>
	<a href="https://github.com/Kryeit/Telepost/LICENSE"><img src="https://img.shields.io/github/license/Creators-of-Create/Create?style=flat&color=900c3f" alt="License"></a>
	<a href="https://discord.gg/vq5vwuyDGU"><img src="https://img.shields.io/discord/929394649884405761?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
	<a href="https://www.curseforge.com/minecraft/mc-mods/telepost"><img src="http://cf.way2muchnoise.eu/telepost.svg" alt="CF"></a>
    <a href="https://modrinth.com/mod/telepost"><img src="https://img.shields.io/modrinth/dt/telepost?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
    <br><br>
</h1>

**Mod description**

Telepost doesn't require any other mod to function. However, it has softdepend functions for:
- [GriefDefender](https://www.spigotmc.org/resources/1-12-2-1-20-4-griefdefender-claim-plugin-grief-prevention-protection.68900/): to claim the posts when using /buildposts and also let players name a post if they have enough claim blocks.
- [WorldEdit](https://modrinth.com/plugin/worldedit): when using /buildposts, the terrain will be accommodated in order for the post to look good.
- [Bluemap](https://modrinth.com/plugin/bluemap): when using /namepost, a marker with its name will appear

They will be explained further in the bottom of the page.

<img src="https://cdn.modrinth.com/data/w8avchdW/images/392f24bba4c1cec4557709388a9691ea6637be9b.png" width="430" />

Telepost can build (if needed) posts throughout the whole map, one each 2000 blocks. These posts serve as a nerfed teleportation system. Players can only execute teleport commands in those specific locations of the world, making it fun to explore and meet up with other players!

<img src="https://cdn.modrinth.com/data/w8avchdW/images/1c2f0f10937a1d51eeee8614beba5aa0c46cd851.png" width="430" />

- /nearestpost: tells you where the nearest post is.
- /setpost: sets a home on the nearest post.
- /homepost: teleports you to your home.
- /postlist [page]: shows a pagination of the named posts.
- /invite <Player>: invite a player to your home post.
- /visit <Player/NamedPost>: teleports you to an invited post or to a Named Post
- /namepost <PostName> : gives a name to the nearest post. Only for admins.
- /unnamepost <PostName> : unnames a post. Only for admins.
- /buildposts: builds all posts. Only for admins

How to use /buildpost: place in `world/generated/structures/minecraft` a `default.nbt`, then place any other .nbt with a biome name, such as `plains.nbt`. Posts will be built with that structure depending on the biome, if none found, the default will be used.

<img src="https://cdn.modrinth.com/data/w8avchdW/images/9b2886e9c145a733c26c5c6a3982683474d295d5.png" width="430" />

- telepost.admin : Helper + you can use tp-related commands everywhere in the Overworld.
- telepost.helper : lets you visit other's home posts without needing an invitation.

<img src="https://cdn.modrinth.com/data/w8avchdW/images/b984923c23ca57afce0cf535606a99081d4c9045.png" width="430" />

- /buildposts: It also creates a claim in each post, 3d claim and also adds it to a "Posts" claimgroup
- Users are also able to use /namepost once if they have more than 80.000 earned claimblocks, doesn't consume them. NO PERMISSION NEEDED

# Posts NBT's:

There is a nbt per biome, that includes Vanilla biomes and Terralith biomes.
In total there is 133 different post nbt's, plus a `default.nbt` that is a copy of `plains.nbt`.
Special thanks to the players involved on building them:
- __Tesseract
- OlimilO
- Antonovich
- Enzo
- Slayz

*Note: the post also include blocks from Create mod, Farmers Delight, so if you use those, it will look best, if not, feel free to modify the .nbt's*

# Translations:

Feel free to translate the mod [here](https://github.com/Kryeit/Telepost-Refabricated/tree/main/src/main/resources/data/telepost/lang)

Translations are per client, therefore different players receive messages in different languages!

# Current translations:

- Spanish: by muriplz
- English: by muriplz
- German: by MrRedRhino
- French: by __Tesseract
- Chinese: by LK
- Brazilean portuguese: by Zeque
- Latvian: by bbqribs

Thanks for translating!
