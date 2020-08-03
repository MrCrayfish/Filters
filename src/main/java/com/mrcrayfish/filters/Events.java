package com.mrcrayfish.filters;

import com.mrcrayfish.filters.gui.widget.button.IconButton;
import com.mrcrayfish.filters.gui.widget.button.TagButton;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class Events
{
    private static final ResourceLocation ICONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/icons.png");
    private static Map<ItemGroup, Integer> scrollMap = new HashMap<>();

    private boolean updatedFilters;
    private List<TagButton> buttons = new ArrayList<>();
    private Map<ItemGroup, FilterEntry> miscFilterMap = new HashMap<>();
    private Button btnScrollUp;
    private Button btnScrollDown;
    private Button btnEnableAll;
    private Button btnDisableAll;
    private boolean viewingFilterTab;
    private int guiCenterX = 0;
    private int guiCenterY = 0;
    private ItemGroup currentGroup = ItemGroup.BUILDING_BLOCKS;

    @SubscribeEvent
    public void onPlayerLogout(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        this.updatedFilters = false;
    }

    @SubscribeEvent
    public void onScreenInit(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if(event.getGui() instanceof CreativeScreen)
        {
            if(!this.updatedFilters)
            {
                this.updateFilters();
                this.updatedFilters = true;
            }

            this.viewingFilterTab = false;
            this.guiCenterX = ((CreativeScreen) event.getGui()).getGuiLeft();
            this.guiCenterY = ((CreativeScreen) event.getGui()).getGuiTop();

            event.addWidget(this.btnScrollUp = new IconButton(this.guiCenterX - 22, this.guiCenterY - 12, I18n.format("gui.button.filters.scroll_filters_up"), button -> this.scrollUp(), ICONS, 0, 0));
            event.addWidget(this.btnScrollDown = new IconButton(this.guiCenterX - 22, this.guiCenterY + 127, I18n.format("gui.button.filters.scroll_filters_down"), button -> this.scrollDown(), ICONS, 16, 0));
            event.addWidget(this.btnEnableAll = new IconButton(this.guiCenterX - 50, this.guiCenterY + 10, I18n.format("gui.button.filters.enable_filters"), button -> this.enableAllFilters(), ICONS, 32, 0));
            event.addWidget(this.btnDisableAll = new IconButton(this.guiCenterX - 50, this.guiCenterY + 32, I18n.format("gui.button.filters.disable_filters"), button -> this.disableAllFilters(), ICONS, 48, 0));

            this.hideButtons();

            CreativeScreen screen = (CreativeScreen) event.getGui();
            this.updateTagButtons(screen);

            ItemGroup group = this.getGroup(screen.getSelectedTabIndex());
            if(Filters.get().hasFilters(group))
            {
                this.showButtons();
                this.viewingFilterTab = true;
                this.updateItems(screen);
            }

            this.currentGroup = group;
        }
    }

    @SubscribeEvent
    public void onScreenClick(GuiScreenEvent.MouseClickedEvent.Pre event)
    {
        if(event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return;

        if(event.getGui() instanceof CreativeScreen)
        {
            for(Button button : this.buttons)
            {
                if(button.isMouseOver(event.getMouseX(), event.getMouseY()))
                {
                    if(button.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton()))
                    {
                        return;
                    }
                }
            }
        }
    }

    public void onCreativeTabChange(CreativeScreen screen, ItemGroup group)
    {
        if(Filters.get().hasFilters(group))
        {
            if(group != this.currentGroup)
            {
                this.updateItems(screen);
                this.currentGroup = group;
            }
        }
        this.updateTagButtons(screen);
    }

    @SubscribeEvent
    public void onScreenDrawPre(GuiScreenEvent.DrawScreenEvent.Pre event)
    {
        if(event.getGui() instanceof CreativeScreen)
        {
            CreativeScreen screen = (CreativeScreen) event.getGui();
            ItemGroup group = this.getGroup(screen.getSelectedTabIndex());

            if(Filters.get().hasFilters(group))
            {
                if(!this.viewingFilterTab)
                {
                    this.updateItems(screen);
                    this.viewingFilterTab = true;
                }
            }
            else
            {
                this.viewingFilterTab = false;
            }
        }
    }

    @SubscribeEvent
    public void onScreenDrawBackground(GuiContainerEvent.DrawBackground event)
    {
        if(event.getGuiContainer() instanceof CreativeScreen)
        {
            CreativeScreen screen = (CreativeScreen) event.getGuiContainer();
            ItemGroup group = this.getGroup(screen.getSelectedTabIndex());

            if(Filters.get().hasFilters(group))
            {
                /* Render buttons */
                this.buttons.forEach(button ->
                {
                    button.render(event.getMouseX(), event.getMouseY(), Minecraft.getInstance().getRenderPartialTicks());
                });
            }
        }
    }

    @SubscribeEvent
    public void onScreenDrawPost(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if(event.getGui() instanceof CreativeScreen)
        {
            CreativeScreen screen = (CreativeScreen) event.getGui();
            ItemGroup group = this.getGroup(screen.getSelectedTabIndex());

            if(Filters.get().hasFilters(group))
            {
                /* Render tooltips after so it renders above buttons */
                this.buttons.forEach(button ->
                {
                    if(button.isMouseOver(event.getMouseX(), event.getMouseY()))
                    {
                        screen.renderTooltip(button.getFilter().getName(), event.getMouseX(), event.getMouseY());
                    }
                });

                if(this.btnEnableAll.isMouseOver(event.getMouseX(), event.getMouseY()))
                {
                    screen.renderTooltip(this.btnEnableAll.getMessage(), event.getMouseX(), event.getMouseY());
                }

                if(this.btnDisableAll.isMouseOver(event.getMouseX(), event.getMouseY()))
                {
                    screen.renderTooltip(this.btnDisableAll.getMessage(), event.getMouseX(), event.getMouseY());
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseScroll(GuiScreenEvent.MouseScrollEvent.Pre event)
    {
        if(event.getGui() instanceof CreativeScreen)
        {
            CreativeScreen creativeScreen = (CreativeScreen) event.getGui();
            int guiLeft = creativeScreen.getGuiLeft();
            int guiTop = creativeScreen.getGuiTop();
            int startX = guiLeft - 32;
            int startY = guiTop + 10;
            int endX = guiLeft;
            int endY = startY + 28 * 4 + 3;
            if(event.getMouseX() >= startX && event.getMouseX() < endX && event.getMouseY() >= startY && event.getMouseY() < endY)
            {
                if(event.getScrollDelta() > 0)
                {
                    this.scrollUp();
                }
                else
                {
                    this.scrollDown();
                }
                event.setCanceled(true);
            }
        }
    }

    private void updateTagButtons(CreativeScreen screen)
    {
        if(!this.updatedFilters)
            return;

        this.buttons.clear();
        ItemGroup group = this.getGroup(screen.getSelectedTabIndex());
        if(Filters.get().hasFilters(group))
        {
            List<FilterEntry> entries = this.getFilters(group);
            int scroll = scrollMap.computeIfAbsent(group, group1 -> 0);
            for(int i = scroll; i < scroll + 4 && i < entries.size(); i++)
            {
                TagButton button = new TagButton(screen.getGuiLeft() - 28, screen.getGuiTop() + 29 * (i - scroll) + 10, entries.get(i), button1 -> this.updateItems(screen));
                this.buttons.add(button);
            }
            this.btnScrollUp.active = scroll > 0;
            this.btnScrollDown.active = scroll <= entries.size() - 4 - 1;
            this.showButtons();
        }
        else
        {
            this.hideButtons();
        }
    }

    private void updateItems(CreativeScreen screen)
    {
        CreativeScreen.CreativeContainer container = screen.getContainer();
        Set<Item> filteredItems = new LinkedHashSet<>();
        ItemGroup group = this.getGroup(screen.getSelectedTabIndex());
        if(group != null)
        {
            if(Filters.get().hasFilters(group))
            {
                List<FilterEntry> entries = Filters.get().getFilters(group);
                if(entries != null)
                {
                    for(FilterEntry filter : this.getFilters(group))
                    {
                        if(filter.isEnabled())
                        {
                            filteredItems.addAll(filter.getItems());
                        }
                    }
                    container.itemList.clear();
                    filteredItems.forEach(item -> item.fillItemGroup(group, container.itemList));
                    container.itemList.sort(Comparator.comparingInt(o -> Item.getIdFromItem(o.getItem())));
                    container.scrollTo(0);
                }
            }
        }
    }

    private void updateFilters()
    {
        Filters.get().getGroups().forEach(group ->
        {
            List<FilterEntry> entries = Filters.get().getFilters(group);
            entries.forEach(FilterEntry::clear);

            Set<Item> removed = new HashSet<>();
            List<Item> items = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.getGroup() == group || item == Items.ENCHANTED_BOOK)
                .collect(Collectors.toList());
            items.forEach(item ->
            {
                for(ResourceLocation location : item.getTags())
                {
                    for(FilterEntry filter : entries)
                    {
                        if(location.equals(filter.getTag()))
                        {
                            filter.add(item);
                            removed.add(item);
                        }
                    }
                }
            });
            items.removeAll(removed);

            if(group.getRelevantEnchantmentTypes().length == 0)
            {
                items.remove(Items.ENCHANTED_BOOK);
            }

            if(!items.isEmpty())
            {
                FilterEntry entry = new FilterEntry(new ResourceLocation("miscellaneous"), new ItemStack(Blocks.BARRIER));
                items.forEach(entry::add);
                this.miscFilterMap.put(group, entry);
            }
        });
    }

    private ItemGroup getGroup(int index)
    {
        if(index < 0 || index >= ItemGroup.GROUPS.length)
            return null;
        return ItemGroup.GROUPS[index];
    }

    private List<FilterEntry> getFilters(ItemGroup group)
    {
        if(Filters.get().hasFilters(group))
        {
            List<FilterEntry> filters = new ArrayList<>(Filters.get().getFilters(group));
            if(this.miscFilterMap.containsKey(group))
            {
                filters.add(this.miscFilterMap.get(group));
            }
            return filters;
        }
        return Collections.emptyList();
    }

    private void showButtons()
    {
        this.btnScrollUp.visible = true;
        this.btnScrollDown.visible = true;
        this.btnEnableAll.visible = true;
        this.btnDisableAll.visible = true;
        this.buttons.forEach(button -> button.visible = true);
    }

    private void hideButtons()
    {
        this.btnScrollUp.visible = false;
        this.btnScrollDown.visible = false;
        this.btnEnableAll.visible = false;
        this.btnDisableAll.visible = false;
        this.buttons.forEach(button -> button.visible = false);
    }

    private void scrollUp()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof CreativeScreen)
        {
            CreativeScreen creativeScreen = (CreativeScreen) screen;
            ItemGroup group = this.getGroup(creativeScreen.getSelectedTabIndex());
            List<FilterEntry> entries = this.getFilters(group);
            if(entries != null)
            {
                int scroll = scrollMap.computeIfAbsent(group, group1 -> 0);
                if(scroll > 0)
                {
                    scrollMap.put(group, scroll - 1);
                    this.updateTagButtons(creativeScreen);
                }
            }
        }
    }

    private void scrollDown()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof CreativeScreen)
        {
            CreativeScreen creativeScreen = (CreativeScreen) screen;
            ItemGroup group = this.getGroup(creativeScreen.getSelectedTabIndex());
            List<FilterEntry> entries = this.getFilters(group);
            if(entries != null)
            {
                int scroll = scrollMap.computeIfAbsent(group, group1 -> 0);
                if(scroll <= entries.size() - 4 - 1)
                {
                    scrollMap.put(group, scroll + 1);
                    this.updateTagButtons(creativeScreen);
                }
            }
        }
    }

    private void enableAllFilters()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof CreativeScreen)
        {
            CreativeScreen creativeScreen = (CreativeScreen) screen;
            ItemGroup group = this.getGroup(creativeScreen.getSelectedTabIndex());
            List<FilterEntry> entries = this.getFilters(group);
            if(entries != null)
            {
                entries.forEach(entry -> entry.setEnabled(true));
                this.buttons.forEach(TagButton::updateState);
                this.updateItems(creativeScreen);
            }
        }
    }

    private void disableAllFilters()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof CreativeScreen)
        {
            CreativeScreen creativeScreen = (CreativeScreen) screen;
            ItemGroup group = this.getGroup(creativeScreen.getSelectedTabIndex());
            List<FilterEntry> entries = this.getFilters(group);
            if(entries != null)
            {
                entries.forEach(filters -> filters.setEnabled(false));
                this.buttons.forEach(TagButton::updateState);
                this.updateItems(creativeScreen);
            }
        }
    }
}
