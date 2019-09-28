package com.mrcrayfish.filters;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class FilterEntry
{
    private ResourceLocation tag;
    private String translationKey;
    private ItemStack icon;
    private boolean enabled = true;
    private List<Item> items = new ArrayList<>();

    public FilterEntry(ResourceLocation tag, ItemStack icon)
    {
        this.tag = tag;
        this.translationKey = String.format("gui.tag_filter.%s.%s", tag.getNamespace(), tag.getPath().replace("/", "."));
        this.icon = icon;
    }

    public ResourceLocation getTag()
    {
        return tag;
    }

    public ItemStack getIcon()
    {
        return this.icon;
    }

    public String getName()
    {
        return I18n.format(this.translationKey);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    void add(Item item)
    {
        this.items.add(item);
    }

    void add(Block block)
    {
        this.items.add(block.asItem());
    }

    void clear()
    {
        this.items.clear();
    }

    public List<Item> getItems()
    {
        return this.items;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }
        FilterEntry that = (FilterEntry) o;
        return this.tag.equals(that.tag);
    }

    @Override
    public int hashCode()
    {
        return this.tag.hashCode();
    }
}
