package com.kryeit.telepost.post;

import com.kryeit.telepost.MinecraftServerSupplier;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

import static com.kryeit.telepost.post.Post.WORLD;


public class PostBuilder {

    private static final Identifier DEFAULT_STRUCTURE = Identifier.of("minecraft:default");

    public static String getBiomeName(Optional<RegistryKey<Biome>> biome) {
        if (biome.isEmpty()) return "";
        String input = biome.get().getValue().toString();
        String[] parts = input.split(":");

        return parts.length > 1 ? parts[1] : "";
    }

    public static Optional<StructureTemplate> getStructureTemplate(Optional<RegistryKey<Biome>> biome) {
        StructureTemplateManager manager = MinecraftServerSupplier.getServer().getStructureTemplateManager();
        Optional<StructureTemplate> template = manager.getTemplate(Identifier.of(getBiomeName(biome)));

        if (template.isEmpty()) {
            template = manager.getTemplate(DEFAULT_STRUCTURE);
        }
        return template;
    }

    public static void placeStructure(Post post) {
        //FluidState state = WORLD.getBlockState(new BlockPos(post.getX(), post.getY() - 1, post.getZ())).getFluidState();
        //if (!state.isIn(FluidTags.WATER) && CompatAddon.WORLD_EDIT.isLoaded())
        //    PostAccommodation.accommodate(post);

        BlockPos pos = post.getBlockPos();

        RegistryEntry<Biome> biome = post.getBiome();

        Optional<StructureTemplate> template = getStructureTemplate(biome.getKey());

        if (template.isPresent()) {
            StructurePlacementData placementData = new StructurePlacementData();

            BlockBox boundingBox = template.get().calculateBoundingBox(placementData, pos);

            template.get().place(
                    ((ServerWorld) WORLD),
                    pos.add(-boundingBox.getBlockCountX()/2, -1, -boundingBox.getBlockCountZ()/2),
                    pos.add(-boundingBox.getBlockCountX()/2, -1, -boundingBox.getBlockCountZ()/2),
                    placementData,
                    Random.create(0),
                    Block.NOTIFY_ALL);
        }
    }
}
