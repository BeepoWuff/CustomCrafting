package me.wolfyscript.customcrafting.gui.crafting;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.PlayerCache;
import me.wolfyscript.customcrafting.data.cache.EliteWorkbench;
import me.wolfyscript.customcrafting.gui.ExtendedGuiWindow;
import me.wolfyscript.customcrafting.gui.crafting.buttons.CraftingSlotButton;
import me.wolfyscript.customcrafting.gui.crafting.buttons.ResultSlotButton;
import me.wolfyscript.utilities.api.inventory.GuiUpdateEvent;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class CraftingWindow6 extends ExtendedGuiWindow {

    public CraftingWindow6(InventoryAPI inventoryAPI) {
        super("crafting_grid6", inventoryAPI, 54);
    }

    @Override
    public void onInit() {
        for (int i = 0; i < 36; i++) {
            registerButton(new CraftingSlotButton(i));
        }
        registerButton(new ResultSlotButton());
    }

    @EventHandler
    public void onUpdate(GuiUpdateEvent event) {
        if (event.verify(this)) {
            PlayerCache cache = CustomCrafting.getPlayerCache(event.getPlayer());
            EliteWorkbench eliteWorkbench = cache.getEliteWorkbench();
            if (eliteWorkbench.getContents() == null || eliteWorkbench.getCurrentGridSize() <= 0) {
                eliteWorkbench.setCurrentGridSize(6);
                eliteWorkbench.setContents(new ItemStack[36]);
            }
            event.setButton(16, "crafting", "knowledge_book");
            int slot;
            for (int i = 0; i < 36; i++) {
                slot = i + (i / 6) * 3;
                event.setButton(slot, "crafting.slot_" + i);
            }
            event.setButton(43, "result_slot");

        }
    }
}