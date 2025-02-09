package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import com.codenamed.rodspawn.block.BrazierBlock;
import com.codenamed.rodspawn.block.NetherSpawnerBlock;
import com.codenamed.rodspawn.block.entity.brazier.BrazierState;
import com.codenamed.rodspawn.block.entity.nether_spawner.NetherSpawnerState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;


public class RodspawnBlocks {
        public static final DeferredRegister.Blocks BLOCKS =
                DeferredRegister.createBlocks(Rodspawn.MOD_ID);

        public static final DeferredBlock<Block> GILDED_NETHER_BRICKS = registerBlock("gilded_nether_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK)));

        public static final DeferredBlock<Block> NETHER_PILLAR = registerBlock("nether_pillar",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_BRICKS)));

        public static final DeferredBlock<Block> NETHER_SPAWNER = registerBlock("nether_spawner",
               () -> new NetherSpawnerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TRIAL_SPAWNER).lightLevel((var) -> {
                  return ((NetherSpawnerState)var.getValue(NetherSpawnerBlock.STATE)).lightLevel();
               })));

        public static final DeferredBlock<Block> BRAZIER = registerBlock("brazier", () ->
                new BrazierBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_BRICKS).noOcclusion().forceSolidOn().lightLevel((var) -> {
                    return ((BrazierState) var.getValue(BrazierBlock.STATE)).lightLevel();
                })));


        private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
            return (p_50763_) -> {
                return (Boolean)p_50763_.getValue(BlockStateProperties.LIT) ? lightValue : 0;
            };
        }

        private static Block stair(DeferredBlock<Block> baseBlock) {
            return new StairBlock(baseBlock.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(baseBlock.get()));
        }

        private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
            DeferredBlock<T> toReturn = BLOCKS.register(name, block);
            registerBlockItem(name, toReturn);
            return toReturn;
        }
        private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
            RodspawnItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
        public static void init(IEventBus eventBus) {
            BLOCKS.register(eventBus);
        }
}
