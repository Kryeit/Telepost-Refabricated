package com.kryeit.telepost.worldedit;

import com.kryeit.telepost.post.Post;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.function.mask.BlockCategoryMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskUnion;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.block.Blocks;

import static com.kryeit.telepost.config.ConfigReader.WIDTH;
import static com.kryeit.telepost.post.Post.WORLD;

public class PostAccommodation {
    public static void accommodate(Post post) {
        int width = WIDTH;
        int biggerWidth = WIDTH + 5;

        int x = post.getX();
        int y = post.getY();
        int z = post.getZ();

        Vector3 start;
        Vector3 end;

        try (EditSession editSession = getEditSession()) {

            cut(editSession, post, biggerWidth);

            start = Vector3.at(x + biggerWidth, y - 1, z + biggerWidth);
            end = Vector3.at(x - biggerWidth, y - 10, z - biggerWidth);

            removeFoliage(editSession, start.toBlockPoint(), end.toBlockPoint());

            editSession.makeCylinder(
                    BlockVector3.at(x, post.getY() - 10, z),
                    editSession.getBlock(BlockVector3.at(x, post.getY() - 1, z)),
                    width,
                    width,
                    10,
                    true
            );

        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }

        try (EditSession editSession = getEditSession()){
            start = Vector3.at(x + biggerWidth, y - 15, z + biggerWidth);
            end = Vector3.at(x - biggerWidth, y + 100, z - biggerWidth);
            smooth(editSession, start, end);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    private static void cut(EditSession editSession, Post post, int width) throws MaxChangedBlocksException {
        editSession.makeCylinder(
                BlockVector3.at(post.getX(), post.getY() - 1, post.getZ()),
                FabricAdapter.adapt(Blocks.AIR).getDefaultState(),
                width,
                width,
                100,
                true
        );
    }

    public static void smooth(EditSession editSession, Vector3 start, Vector3 end) throws MaxChangedBlocksException {
        Region region = new CuboidRegion(editSession.getWorld(), start.toBlockPoint(), end.toBlockPoint());

        BlockCategoryMask logsMask = new BlockCategoryMask(editSession.getWorld(), BlockCategories.LOGS);
        BlockCategoryMask leavesMask = new BlockCategoryMask(editSession.getWorld(), BlockCategories.LEAVES);

        // Combine masks
        Mask combinedMask = new MaskUnion(logsMask, leavesMask);

        // Negate the combined mask to apply smoothing to all blocks except logs and leaves
        Mask mask = Masks.negate(combinedMask);
        editSession.setMask(mask);

        HeightMap heightMap = new HeightMap(editSession, region, null);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        heightMap.applyFilter(filter, 10);
    }

    public static EditSession getEditSession() {
        return WorldEdit.getInstance().newEditSession(FabricAdapter.adapt(WORLD));
    }

    public static void removeFoliage(EditSession editSession, BlockVector3 start, BlockVector3 end) {
        Region region = new CuboidRegion(editSession.getWorld(), start, end);
        Mask foliageMask = getFoliageMask(editSession);

        for (BlockVector3 vec : region) {
            if (foliageMask.test(vec)) {
                try {
                    editSession.setBlock(vec, BlockTypes.AIR.getDefaultState());
                } catch (MaxChangedBlocksException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Mask getFoliageMask(EditSession editSession) {
        BlockCategoryMask logsMask = new BlockCategoryMask(editSession.getWorld(), BlockCategories.LOGS);
        BlockCategoryMask leavesMask = new BlockCategoryMask(editSession.getWorld(), BlockCategories.LEAVES);

        return new MaskUnion(logsMask, leavesMask);
    }
}
