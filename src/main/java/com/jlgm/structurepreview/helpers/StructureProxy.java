package com.jlgm.structurepreview.helpers;

import com.minecolonies.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Proxy class translating the structures method to something we can use.
 */
public class StructureProxy
{
    private static final ItemStack                              DEFAULT_ICON   = new ItemStack(Blocks.GRASS);
    private final Structure structure;
    private final Block[][][] blocks;
    private final IBlockState[][][]  metadata;
    private final List<TileEntity> tileEntities = new ArrayList<>();
    private final List<Entity>     entities     = new ArrayList<>();
    private final int       width;
    private final int       height;
    private final int       length;
    private       ItemStack icon;
    private       BlockPos  offset;
    
    /**
     * @param worldObj the world.
     * @param name the string where the structure is saved at.
     */
    public StructureProxy(final World worldObj, final String name)
    {
        this.structure = new Structure(worldObj, name, new PlacementSettings());
        BlockPos size = structure.getSize(Rotation.NONE);

        this.width = size.getX();
        this.height = size.getY();
        this.length = size.getZ();

        offset = new BlockPos(0, 0, 0);

        this.blocks = new Block[width][height][length];
        this.metadata = new IBlockState[width][height][length];

        for(Template.BlockInfo info: structure.getBlockInfo())
        {
            BlockPos tempPos = info.pos;
            blocks[tempPos.getX()][tempPos.getY()][tempPos.getZ()] = info.blockState.getBlock();
            metadata[tempPos.getX()][tempPos.getY()][tempPos.getZ()] = info.blockState;
        }
    }

    //MINECOLONIES START

    public BlockPos getOffset()
    {
        return offset;
    }

    public void setOffset(BlockPos pos)
    {
        offset = pos;
    }

    public String getType()
    {
        if (hasOffset())
        {
            return "Hut";
        }
        return "Decoration";
    }

    private boolean hasOffset()
    {
        return !BlockPosUtil.isEqual(offset, 0, 0, 0);
    }

    //MINECOLONIES END

    @SuppressWarnings("deprecation")
    public IBlockState getBlockState(@NotNull final BlockPos pos)
    {
        return metadata[pos.getX()][pos.getY()][pos.getZ()];
    }

    /**
     * return a tileEntity at a certain position.
     * @param pos the position.
     * @return the tileEntity.
     */
    public TileEntity getTileEntity(final BlockPos pos)
    {
        for (final TileEntity tileEntity : this.tileEntities)
        {
            if (tileEntity.getPos().equals(pos))
            {
                return tileEntity;
            }
        }

        return null;
    }

    /**
     * Return a list of tileEntities.
     * @return list of them.
     */
    public List<TileEntity> getTileEntities()
    {
        return this.tileEntities;
    }

    /**
     * Sets tileEntities.
     * @param pos at position.
     * @param tileEntity the entity to set.
     */
    public void setTileEntity(final BlockPos pos, final TileEntity tileEntity)
    {
        if (isInvalid(pos))
        {
            return;
        }

        removeTileEntity(pos);

        if (tileEntity != null)
        {
            this.tileEntities.add(tileEntity);
        }
    }

    /**
     * Removes a tileEntity at a position.
     * @param pos the position to remove it at.
     */
    public void removeTileEntity(final BlockPos pos)
    {
        final Iterator<TileEntity> iterator = this.tileEntities.iterator();

        while (iterator.hasNext())
        {
            final TileEntity tileEntity = iterator.next();
            if (tileEntity.getPos().equals(pos))
            {
                iterator.remove();
            }
        }
    }

    /**
     * Return all entities.
     * @return the list of entities.
     */
    @NotNull
    public List<Entity> getEntities()
    {
        return this.entities;
    }

    /**
     * Add an entitiy.
     * @param entity the entity to add.
     */
    public void addEntity(final Entity entity)
    {
        if (entity == null || entity.getUniqueID() == null || entity instanceof EntityPlayer)
        {
            return;
        }

        for (final Entity e : this.entities)
        {
            if (entity.getUniqueID().equals(e.getUniqueID()))
            {
                return;
            }
        }

        this.entities.add(entity);
    }

    /**
     * Remove a certain entitiy.
     * @param entity that should be removed.
     */
    public void removeEntity(final Entity entity)
    {
        if (entity == null || entity.getUniqueID() == null)
        {
            return;
        }

        final Iterator<Entity> iterator = this.entities.iterator();
        while (iterator.hasNext())
        {
            final Entity e = iterator.next();
            if (entity.getUniqueID().equals(e.getUniqueID()))
            {
                iterator.remove();
            }
        }
    }

    /**
     * Getter of the width.
     * @return the width.
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Getter of the length.
     * @return the length
     */
    public int getLength()
    {
        return this.length;
    }

    /**
     * Getter of the height.
     * @return the height
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Checks if a position is inside the structure.
     * @param pos the position.
     * @return true if so.
     */
    private boolean isInvalid(final BlockPos pos)
    {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        return (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length);
    }

    /**
     * Rotate the structure depending on the direction it's facing.
     * @param facing directions its facing.
     */
    public void rotate(final EnumFacing facing)
    {
        structure.setPlacementSettings(new PlacementSettings().setRotation(StructureProxy.getRotationFromFacing(facing)));
    }

    /**
     * Calculates the rotation from facing.
     * @param facing the facing.
     * @return the rotation.
     */
    private static Rotation getRotationFromFacing(final EnumFacing facing)
    {
        switch(facing)
        {
            case NORTH:
                return Rotation.CLOCKWISE_180;
            case SOUTH:
                return Rotation.NONE;
            case WEST:
                return Rotation.CLOCKWISE_90;
            case EAST:
                return Rotation.COUNTERCLOCKWISE_90;
        }
        return Rotation.NONE;
    }
}