package com.github.lorenzopapi.pgr.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.ICollisionReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(ICollisionReader.class)
public interface ICollisionReaderMixin {

    @Shadow
    Stream<VoxelShape> getCollisionShapes(Entity ent, AxisAlignedBB aabb);

    @Shadow
    Stream<VoxelShape> func_230318_c_(Entity ent, AxisAlignedBB aabb, Predicate<Entity> p);

    /**
     * @author LorenzoPapi
     * @reason ask forge
     */
    @Overwrite(remap = false)
    default Stream<VoxelShape> func_234867_d_(Entity entity, AxisAlignedBB aabb, Predicate<Entity> entityPredicate) {
        Stream<VoxelShape> shapes = Stream.concat(this.getCollisionShapes(entity, aabb), this.func_230318_c_(entity, aabb, entityPredicate));
        List<AxisAlignedBB> aabbList = new ArrayList<>();
        shapes.forEach(voxelShape -> aabbList.add(voxelShape.getBoundingBox()));

        return Stream.concat(this.getCollisionShapes(entity, aabb), this.func_230318_c_(entity, aabb, entityPredicate));
    }
}
