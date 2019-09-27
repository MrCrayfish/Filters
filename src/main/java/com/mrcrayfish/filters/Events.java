package com.mrcrayfish.filters;

import com.mrcrayfish.filters.gui.widget.button.IconButton;
import com.mrcrayfish.filters.gui.widget.button.TagButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class Events
{
    private static final ResourceLocation ICONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/icons.png");
    private static Map<ItemGroup, Integer> scrollMap = new HashMap<>();

    private boolean updatedFilters;
    private List<TagButton> buttons = new ArrayList<>();
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

            event.addWidget(this.btnScrollUp = new IconButton(this.guiCenterX - 22, this.guiCenterY - 12, I18n.format("gui.button.filters.scroll_filters_up"), button ->
            {
                CreativeScreen screen = (CreativeScreen) event.getGui();
                ItemGroup group = this.getGroup(screen.getSelectedTabIndex());
                List<FilterEntry> entries = Filters.get().getFilters(group);
                if(entries != null)
                {
                    int scroll = scrollMap.computeIfAbsent(group, group1 -> 0);
                    if(scroll > 0)
                    {
                        scrollMap.put(group, scroll - 1);
                        this.updateTagButtons(screen);
                    }
                }
            }, ICONS, 0, 0));

            event.addWidget(this.btnScrollDown = new IconButton(this.guiCenterX - 22, this.guiCenterY + 127, I18n.format("gui.button.filters.scroll_filters_down"), button ->
            {
                CreativeScreen screen = (CreativeScreen) event.getGui();
                ItemGroup group = this.getGroup(screen.getSelectedTabIndex());
                List<FilterEntry> entries = Filters.get().getFilters(group);
                if(entries != null)
                {
                    int scroll = scrollMap.computeIfAbsent(group, group1 -> 0);
                    if(scroll <= entries.size() - 4 - 1)
                    {
                        scrollMap.put(group, scroll + 1);
                        this.updateTagButtons(screen);
                    }
                }
            }, ICONS, 16, 0));

            event.addWidget(this.btnEnableAll = new IconButton(this.guiCenterX - 50, this.guiCenterY + 10, I18n.format("gui.button.filters.enable_filters"), button ->
            {
                ItemGroup group = this.getGroup(((CreativeScreen) event.getGui()).getSelectedTabIndex());
                List<FilterEntry> entries = Filters.get().getFilters(group);
                if(entries != null)
                {
                    entries.forEach(entry -> entry.setEnabled(true));
                    this.buttons.forEach(TagButton::updateState);
                    Screen screen = Minecraft.getInstance().currentScreen;
                    if(screen instanceof CreativeScreen)
                    {
                        this.updateItems((CreativeScreen) screen);
                    }
                }
            }, ICONS, 32, 0));

            event.addWidget(this.btnDisableAll = new IconButton(this.guiCenterX - 50, this.guiCenterY + 32, I18n.format("gui.button.filters.disable_filters"), button ->
            {
                ItemGroup group = this.getGroup(((CreativeScreen) event.getGui()).getSelectedTabIndex());
                List<FilterEntry> entries = Filters.get().getFilters(group);
                if(entries != null)
                {
                    entries.forEach(filters -> filters.setEnabled(false));
                    this.buttons.forEach(TagButton::updateState);
                    Screen screen = Minecraft.getInstance().currentScreen;
                    if(screen instanceof CreativeScreen)
                    {
                        this.updateItems((CreativeScreen) screen);
                    }
                }
            }, ICONS, 48, 0));

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
                this.updateTagButtons(screen);
                this.updateItems(screen);
                this.currentGroup = group;
            }
        }
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
    public void onScreenDrawPost(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if(event.getGui() instanceof CreativeScreen)
        {
            CreativeScreen screen = (CreativeScreen) event.getGui();
            ItemGroup group = this.getGroup(screen.getSelectedTabIndex());
            this.guiCenterX = screen.getGuiLeft();
            this.guiCenterY = screen.getGuiTop();

            if(Filters.get().hasFilters(group))
            {
                this.showButtons();

                /* Render buttons */
                this.buttons.forEach(button ->
                {
                    button.render(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
                });

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
            else
            {
                this.hideButtons();
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
            List<FilterEntry> entries = Filters.get().getFilters(group);
            int scroll = scrollMap.computeIfAbsent(group, group1 -> 0);
            for(int i = scroll; i < scroll + 4 && i < entries.size(); i++)
            {
                TagButton button = new TagButton(this.guiCenterX - 28, this.guiCenterY + 29 * (i - scroll) + 10, entries.get(i), button1 -> this.updateItems(screen));
                this.buttons.add(button);
            }
            this.btnScrollUp.active = scroll > 0;
            this.btnScrollDown.active = scroll <= entries.size() - 4 - 1;
        }
        else
        {
            this.hideButtons();
        }
    }

    private void updateItems(CreativeScreen screen)
    {
        CreativeScreen.CreativeContainer container = screen.getContainer();
        Set<Item> categorisedItems = new LinkedHashSet<>();
        ItemGroup group = this.getGroup(screen.getSelectedTabIndex());
        if(group != null)
        {
            List<FilterEntry> entries = Filters.get().getFilters(group);
            if(entries != null)
            {
                for(FilterEntry filter : entries)
                {
                    if(filter.isEnabled())
                    {
                        categorisedItems.addAll(filter.getItems());
                    }
                }
                container.itemList.clear();
                categorisedItems.forEach(item -> item.fillItemGroup(group, container.itemList));
                container.itemList.sort(Comparator.comparingInt(o -> Item.getIdFromItem(o.getItem())));
                container.scrollTo(0);
            }
        }
    }

    private void updateFilters()
    {
        Filters.get().getGroups().forEach(group ->
        {
            List<FilterEntry> entries = Filters.get().getFilters(group);
            entries.forEach(FilterEntry::clear);

            Stream<Item> stream = ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> item.getGroup() == group);
            stream.forEach(item ->
            {
                item.getTags().forEach(location ->
                {
                    entries.forEach(filter ->
                    {
                        if(location.equals(filter.getTag()))
                        {
                            filter.add(item);
                        }
                    });
                });
            });
        });
    }

    private ItemGroup getGroup(int index)
    {
        if(index < 0 || index >= ItemGroup.GROUPS.length)
            return null;
        return ItemGroup.GROUPS[index];
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
}
