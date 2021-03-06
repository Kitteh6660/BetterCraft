package kittehmod.morecraft.entity.ai;

import kittehmod.morecraft.tileentity.NetherwoodChestTileEntity;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class ModChestCatSitOnBlockGoal extends CatSitOnBlockGoal 
{
    public ModChestCatSitOnBlockGoal(CatEntity catEntity, float speedIn)
    {
        super(catEntity, speedIn);
    }

    /**
     * Return true to set given position as destination
     */
    @Override
    protected boolean isValidTarget(IWorldReader worldIn, BlockPos pos)
    {
        if (!worldIn.isEmptyBlock(pos.above()))
        {
            return false;
        }
        else
        {
            BlockState iBlockState = worldIn.getBlockState(pos);
            Block block = iBlockState.getBlock();

            if (block instanceof AbstractChestBlock)
            {
                return NetherwoodChestTileEntity.getOpenCount(worldIn, pos) < 1;
            }

            return super.isValidTarget(worldIn, pos);
        }
    }
}
