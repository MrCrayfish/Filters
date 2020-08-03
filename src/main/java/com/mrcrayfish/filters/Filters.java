package com.mrcrayfish.filters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class Filters
{
    private static Filters instance;

    @OnlyIn(Dist.CLIENT)
    private Map<ItemGroup, Set<FilterEntry>> filterMap;
    @OnlyIn(Dist.CLIENT)
    public Events events;

    public Filters()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        Filters.instance = this;
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        this.filterMap = new HashMap<>();
        MinecraftForge.EVENT_BUS.register(this.events = new Events());

        this.register(ItemGroup.BUILDING_BLOCKS, new ResourceLocation("building_blocks/natural"), new ItemStack(Blocks.GRASS_BLOCK));
        this.register(ItemGroup.BUILDING_BLOCKS, new ResourceLocation("building_blocks/stones"), new ItemStack(Blocks.STONE));
        this.register(ItemGroup.BUILDING_BLOCKS, new ResourceLocation("building_blocks/woods"), new ItemStack(Blocks.OAK_LOG));
        this.register(ItemGroup.BUILDING_BLOCKS, new ResourceLocation("building_blocks/minerals"), new ItemStack(Blocks.EMERALD_BLOCK));
        this.register(ItemGroup.BUILDING_BLOCKS, new ResourceLocation("stairs"), new ItemStack(Blocks.OAK_STAIRS));
        this.register(ItemGroup.BUILDING_BLOCKS, new ResourceLocation("slabs"), new ItemStack(Blocks.OAK_SLAB));
        this.register(ItemGroup.BUILDING_BLOCKS, new ResourceLocation("forge", "glass"), new ItemStack(Blocks.GLASS));
        this.register(ItemGroup.BUILDING_BLOCKS, new ResourceLocation("building_blocks/colored"), new ItemStack(Blocks.RED_WOOL));
        this.register(ItemGroup.DECORATIONS, new ResourceLocation("decoration_blocks/vegetation"), new ItemStack(Blocks.GRASS));
        this.register(ItemGroup.DECORATIONS, new ResourceLocation("decoration_blocks/functional"), new ItemStack(Blocks.CRAFTING_TABLE));
        this.register(ItemGroup.DECORATIONS, new ResourceLocation("decoration_blocks/fences_and_walls"), new ItemStack(Blocks.OAK_FENCE));
        this.register(ItemGroup.DECORATIONS, new ResourceLocation("decoration_blocks/interior"), new ItemStack(Blocks.RED_BED));
        this.register(ItemGroup.DECORATIONS, new ResourceLocation("decoration_blocks/glass"), new ItemStack(Blocks.GLASS_PANE));
        this.register(ItemGroup.DECORATIONS, new ResourceLocation("decoration_blocks/colored"), new ItemStack(Blocks.GREEN_GLAZED_TERRACOTTA));
        this.register(ItemGroup.DECORATIONS, new ResourceLocation("decoration_blocks/special"), new ItemStack(Blocks.DRAGON_HEAD));
        this.register(ItemGroup.REDSTONE, new ResourceLocation("redstone/core"), new ItemStack(Items.REDSTONE));
        this.register(ItemGroup.REDSTONE, new ResourceLocation("redstone/components"), new ItemStack(Items.STICKY_PISTON));
        this.register(ItemGroup.REDSTONE, new ResourceLocation("redstone/inputs"), new ItemStack(Items.TRIPWIRE_HOOK));
        this.register(ItemGroup.REDSTONE, new ResourceLocation("redstone/doors"), new ItemStack(Items.OAK_DOOR));
        this.register(ItemGroup.TRANSPORTATION, new ResourceLocation("transportation/vehicles"), new ItemStack(Items.MINECART));
        this.register(ItemGroup.MISC, new ResourceLocation("miscellaneous/materials"), new ItemStack(Items.GOLD_INGOT));
        this.register(ItemGroup.MISC, new ResourceLocation("miscellaneous/eggs"), new ItemStack(Items.TURTLE_EGG));
        this.register(ItemGroup.MISC, new ResourceLocation("miscellaneous/plants_and_seeds"), new ItemStack(Items.SUGAR_CANE));
        this.register(ItemGroup.MISC, new ResourceLocation("miscellaneous/dyes"), new ItemStack(Items.RED_DYE));
        this.register(ItemGroup.MISC, new ResourceLocation("miscellaneous/discs"), new ItemStack(Items.MUSIC_DISC_MALL));
        this.register(ItemGroup.FOOD, new ResourceLocation("foodstuffs/raw"), new ItemStack(Items.BEEF));
        this.register(ItemGroup.FOOD, new ResourceLocation("foodstuffs/cooked"), new ItemStack(Items.COOKED_PORKCHOP));
        this.register(ItemGroup.FOOD, new ResourceLocation("foodstuffs/special"), new ItemStack(Items.GOLDEN_APPLE));
        this.register(ItemGroup.COMBAT, new ResourceLocation("combat/armor"), new ItemStack(Items.IRON_CHESTPLATE));
        this.register(ItemGroup.COMBAT, new ResourceLocation("combat/weapons"), new ItemStack(Items.IRON_SWORD));
        this.register(ItemGroup.COMBAT, new ResourceLocation("combat/arrows"), new ItemStack(Items.ARROW));
        this.register(ItemGroup.COMBAT, new ResourceLocation("combat/enchanting_books"), new ItemStack(Items.ENCHANTED_BOOK));
        this.register(ItemGroup.TOOLS, new ResourceLocation("tools/tools"), new ItemStack(Items.IRON_SHOVEL));
        this.register(ItemGroup.TOOLS, new ResourceLocation("tools/equipment"), new ItemStack(Items.COMPASS));
        this.register(ItemGroup.TOOLS, new ResourceLocation("tools/enchanting_books"), new ItemStack(Items.ENCHANTED_BOOK));
        this.register(ItemGroup.BREWING, new ResourceLocation("brewing/potions"), new ItemStack(Items.DRAGON_BREATH));
        this.register(ItemGroup.BREWING, new ResourceLocation("brewing/ingredients"), new ItemStack(Items.BLAZE_POWDER));
        this.register(ItemGroup.BREWING, new ResourceLocation("brewing/equipment"), new ItemStack(Items.BREWING_STAND));
    }

    public static Filters get()
    {
        return instance;
    }

    @OnlyIn(Dist.CLIENT)
    public void register(ItemGroup group, ResourceLocation tag, ItemStack icon)
    {
        this.filterMap.computeIfAbsent(group, itemGroup -> new LinkedHashSet<>()).add(new FilterEntry(tag, icon));
    }

    @OnlyIn(Dist.CLIENT)
    public Set<ItemGroup> getGroups()
    {
        return ImmutableSet.copyOf(this.filterMap.keySet());
    }

    @OnlyIn(Dist.CLIENT)
    public ImmutableList<FilterEntry> getFilters(ItemGroup group)
    {
        return ImmutableList.copyOf(this.filterMap.get(group));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasFilters(ItemGroup group)
    {
        return this.filterMap.containsKey(group);
    }
}
