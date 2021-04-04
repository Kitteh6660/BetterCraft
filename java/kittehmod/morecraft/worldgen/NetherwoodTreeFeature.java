package kittehmod.morecraft.worldgen;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;

import kittehmod.morecraft.block.ModSaplingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.BitSetVoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldWriter;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.IWorldGenerationBaseReader;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.foliageplacer.FoliagePlacer;

public class NetherwoodTreeFeature extends Feature<BaseTreeFeatureConfig>
{
	// private boolean attemptNetherGen;
	public static final int CHANCE_BARRENS = 2;
	public static final int CHANCE_MODERATE = 6;
	public static final int CHANCE_LUSH = 10;

	public NetherwoodTreeFeature(Codec<BaseTreeFeatureConfig> codecIn, boolean netherGenAttempt) {
		super(codecIn);
		// this.attemptNetherGen = netherGenAttempt;
	}

	public static boolean isFree(IWorldGenerationBaseReader p_236410_0_, BlockPos p_236410_1_) {
		return validTreePos(p_236410_0_, p_236410_1_) || p_236410_0_.isStateAtPosition(p_236410_1_, (p_236417_0_) -> { return p_236417_0_.is(BlockTags.LOGS); });
	}

	private static boolean isVine(IWorldGenerationBaseReader p_236414_0_, BlockPos p_236414_1_) {
		return p_236414_0_.isStateAtPosition(p_236414_1_, (p_236415_0_) -> { return p_236415_0_.is(Blocks.VINE); });
	}

	private static boolean isBlockWater(IWorldGenerationBaseReader p_236416_0_, BlockPos p_236416_1_) {
		return p_236416_0_.isStateAtPosition(p_236416_1_, (p_236413_0_) -> { return p_236413_0_.is(Blocks.WATER); });
	}

	@SuppressWarnings("deprecation")
	public static boolean isAirOrLeaves(IWorldGenerationBaseReader p_236412_0_, BlockPos p_236412_1_) {
		return p_236412_0_.isStateAtPosition(p_236412_1_, (p_236411_0_) -> { return p_236411_0_.isAir() || p_236411_0_.is(BlockTags.LEAVES); });
	}

	private static boolean isGrassOrDirtOrFarmland(IWorldGenerationBaseReader p_236418_0_, BlockPos pos) {
		return p_236418_0_.isStateAtPosition(pos, (blockstate) -> {
			Block block = blockstate.getBlock();
			return isDirt(block) || block == Blocks.FARMLAND || ModSaplingBlock.EXTRA_ALLOWED_BLOCKS.contains(block);
		});
	}

	private static boolean isReplaceablePlant(IWorldGenerationBaseReader p_236419_0_, BlockPos p_236419_1_) {
		return p_236419_0_.isStateAtPosition(p_236419_1_, (p_236406_0_) -> {
			Material material = p_236406_0_.getMaterial();
			return material == Material.REPLACEABLE_PLANT;
		});
	}

	public static void setBlockKnownShape(IWorldWriter p_236408_0_, BlockPos p_236408_1_, BlockState p_236408_2_) {
		p_236408_0_.setBlock(p_236408_1_, p_236408_2_, 19);
	}

	public static boolean validTreePos(IWorldGenerationBaseReader p_236404_0_, BlockPos p_236404_1_) {
		return isAirOrLeaves(p_236404_0_, p_236404_1_) || isReplaceablePlant(p_236404_0_, p_236404_1_) || isBlockWater(p_236404_0_, p_236404_1_);
	}

	/**
	 * Called when placing the tree feature.
	 */
	private boolean place(IWorldGenerationReader generationReader, Random rand, BlockPos positionIn, Set<BlockPos> p_225557_4_, Set<BlockPos> p_225557_5_, MutableBoundingBox boundingBoxIn, BaseTreeFeatureConfig configIn) {
		int i = configIn.trunkPlacer.getTreeHeight(rand);
		int j = configIn.foliagePlacer.foliageHeight(rand, i, configIn);
		int k = i - j;
		int l = configIn.foliagePlacer.foliageRadius(rand, k);
		BlockPos blockpos = positionIn;
		BlockState blockstate = null;
		BlockState origstate = null;
		if (!configIn.fromSapling) {
			// Reduce the frequency of Netherwood trees.
			if (generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.CRIMSON_NYLIUM || block.getBlock() == Blocks.WARPED_NYLIUM)) {
				origstate = generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.CRIMSON_NYLIUM) ? Blocks.CRIMSON_NYLIUM.defaultBlockState() : Blocks.WARPED_NYLIUM.defaultBlockState();
				if (rand.nextInt(100) >= CHANCE_LUSH) {
					return false;
				}
			} else if (generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.SOUL_SAND || block.getBlock() == Blocks.SOUL_SOIL)) {
				origstate = generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.SOUL_SAND) ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.SOUL_SOIL.defaultBlockState();
				if (rand.nextInt(100) >= CHANCE_MODERATE) {
					return false;
				}
			} else if (generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.NETHERRACK)) {
				origstate = Blocks.NETHERRACK.defaultBlockState();
				if (rand.nextInt(100) >= CHANCE_BARRENS) {
					return false;
				}
				generationReader.setBlock(blockpos.below(), Blocks.SOUL_SAND.defaultBlockState(), 19);
			}
		} else {
			blockpos = positionIn;
		}
		// Hacky workaround to stop the blocks from being changed to dirt.
		if (generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.SOUL_SAND)) {
			blockstate = Blocks.SOUL_SAND.defaultBlockState();
		} else if (generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.SOUL_SOIL)) {
			blockstate = Blocks.SOUL_SOIL.defaultBlockState();
		} else if (generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.CRIMSON_NYLIUM)) {
			blockstate = Blocks.CRIMSON_NYLIUM.defaultBlockState();
		} else if (generationReader.isStateAtPosition(blockpos.below(), (block) -> block.getBlock() == Blocks.WARPED_NYLIUM)) {
			blockstate = Blocks.WARPED_NYLIUM.defaultBlockState();
		}
		if (blockpos.getY() >= 1 && blockpos.getY() + i + 1 <= 256) {
			if (!isGrassOrDirtOrFarmland(generationReader, blockpos.below())) {
				if (origstate != null) {
					generationReader.setBlock(blockpos.below(), origstate, 19); // Revert the block.
				}
				return false;
			} else {
				OptionalInt optionalint = configIn.minimumSize.minClippedHeight();
				int l1 = this.getMaxFreeTreeHeight(generationReader, i, blockpos, configIn);
				if (l1 >= i || optionalint.isPresent() && l1 >= optionalint.getAsInt()) {
					List<FoliagePlacer.Foliage> list = configIn.trunkPlacer.placeTrunk(generationReader, rand, l1, blockpos, p_225557_4_, boundingBoxIn, configIn);
					list.forEach((p_236407_8_) -> { configIn.foliagePlacer.createFoliage(generationReader, rand, configIn, l1, p_236407_8_, j, l, p_225557_5_, boundingBoxIn); });
					if (blockstate != null) {
						generationReader.setBlock(blockpos.below(), blockstate, 19);
					}
					return true;
				} else {
					if (blockstate != null) {
						generationReader.setBlock(blockpos.below(), origstate, 19); // Revert the block.
					}
					return false;
				}
			}
		} else {
			return false;
		}
	}

	private int getMaxFreeTreeHeight(IWorldGenerationBaseReader p_241521_1_, int p_241521_2_, BlockPos p_241521_3_, BaseTreeFeatureConfig p_241521_4_) {
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

		for (int i = 0; i <= p_241521_2_ + 1; ++i) {
			int j = p_241521_4_.minimumSize.getSizeAtHeight(p_241521_2_, i);

			for (int k = -j; k <= j; ++k) {
				for (int l = -j; l <= j; ++l) {
					blockpos$mutable.setWithOffset(p_241521_3_, k, i, l);
					if (!isFree(p_241521_1_, blockpos$mutable) || !p_241521_4_.ignoreVines && isVine(p_241521_1_, blockpos$mutable)) {
						return i - 2;
					}
				}
			}
		}

		return p_241521_2_;
	}

	public final boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, BaseTreeFeatureConfig p_241855_5_) {
		Set<BlockPos> set = Sets.newHashSet();
		Set<BlockPos> set1 = Sets.newHashSet();
		Set<BlockPos> set2 = Sets.newHashSet();
		MutableBoundingBox mutableboundingbox = MutableBoundingBox.getUnknownBox();
		boolean flag = this.place(p_241855_1_, p_241855_3_, p_241855_4_, set, set1, mutableboundingbox, p_241855_5_);
		if (mutableboundingbox.x0 <= mutableboundingbox.x1 && flag && !set.isEmpty()) {
			if (!p_241855_5_.decorators.isEmpty()) {
				List<BlockPos> list = Lists.newArrayList(set);
				List<BlockPos> list1 = Lists.newArrayList(set1);
				list.sort(Comparator.comparingInt(Vector3i::getY));
				list1.sort(Comparator.comparingInt(Vector3i::getY));
				p_241855_5_.decorators.forEach((p_236405_6_) -> { p_236405_6_.place(p_241855_1_, p_241855_3_, list, list1, set2, mutableboundingbox); });
			}

			VoxelShapePart voxelshapepart = this.updateLeaves(p_241855_1_, mutableboundingbox, set, set2);
			Template.updateShapeAtEdge(p_241855_1_, 3, voxelshapepart, mutableboundingbox.x0, mutableboundingbox.y0, mutableboundingbox.z0);
			return true;
		} else {
			return false;
		}
	}

	private VoxelShapePart updateLeaves(IWorld worldIn, MutableBoundingBox bbIn, Set<BlockPos> p_236403_3_, Set<BlockPos> p_236403_4_) {
		List<Set<BlockPos>> list = Lists.newArrayList();
		VoxelShapePart voxelshapepart = new BitSetVoxelShapePart(bbIn.getXSpan(), bbIn.getYSpan(), bbIn.getZSpan());
		int amt = 6;

		for (int j = 0; j < amt; ++j) {
			list.add(Sets.newHashSet());
		}

		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

		for (BlockPos blockpos : Lists.newArrayList(p_236403_4_)) {
			if (bbIn.isInside(blockpos)) {
				voxelshapepart.setFull(blockpos.getX() - bbIn.x0, blockpos.getY() - bbIn.y0, blockpos.getZ() - bbIn.z0, true, true);
			}
		}

		for (BlockPos blockpos1 : Lists.newArrayList(p_236403_3_)) {
			if (bbIn.isInside(blockpos1)) {
				voxelshapepart.setFull(blockpos1.getX() - bbIn.x0, blockpos1.getY() - bbIn.y0, blockpos1.getZ() - bbIn.z0, true, true);
			}

			for (Direction direction : Direction.values()) {
				blockpos$mutable.setWithOffset(blockpos1, direction);
				if (!p_236403_3_.contains(blockpos$mutable)) {
					BlockState blockstate = worldIn.getBlockState(blockpos$mutable);
					if (blockstate.hasProperty(BlockStateProperties.DISTANCE)) {
						list.get(0).add(blockpos$mutable.immutable());
						setBlockKnownShape(worldIn, blockpos$mutable, blockstate.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
						if (bbIn.isInside(blockpos$mutable)) {
							voxelshapepart.setFull(blockpos$mutable.getX() - bbIn.x0, blockpos$mutable.getY() - bbIn.y0, blockpos$mutable.getZ() - bbIn.z0, true, true);
						}
					}
				}
			}
		}

		for (int l = 1; l < amt; ++l) {
			Set<BlockPos> set = list.get(l - 1);
			Set<BlockPos> set1 = list.get(l);

			for (BlockPos blockpos2 : set) {
				if (bbIn.isInside(blockpos2)) {
					voxelshapepart.setFull(blockpos2.getX() - bbIn.x0, blockpos2.getY() - bbIn.y0, blockpos2.getZ() - bbIn.z0, true, true);
				}

				for (Direction direction1 : Direction.values()) {
					blockpos$mutable.setWithOffset(blockpos2, direction1);
					if (!set.contains(blockpos$mutable) && !set1.contains(blockpos$mutable)) {
						BlockState blockstate1 = worldIn.getBlockState(blockpos$mutable);
						if (blockstate1.hasProperty(BlockStateProperties.DISTANCE)) {
							int k = blockstate1.getValue(BlockStateProperties.DISTANCE);
							if (k > l + 1) {
								BlockState blockstate2 = blockstate1.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(l + 1));
								setBlockKnownShape(worldIn, blockpos$mutable, blockstate2);
								if (bbIn.isInside(blockpos$mutable)) {
									voxelshapepart.setFull(blockpos$mutable.getX() - bbIn.x0, blockpos$mutable.getY() - bbIn.y0, blockpos$mutable.getZ() - bbIn.z0, true, true);
								}

								set1.add(blockpos$mutable.immutable());
							}
						}
					}
				}
			}
		}

		return voxelshapepart;
	}

}
