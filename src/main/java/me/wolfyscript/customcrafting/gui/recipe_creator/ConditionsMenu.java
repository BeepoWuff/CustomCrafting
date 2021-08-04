package me.wolfyscript.customcrafting.gui.recipe_creator;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.CCWindow;
import me.wolfyscript.customcrafting.gui.MainCluster;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.conditions.EliteWorkbenchConditionButton;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.conditions.WorldBiomeConditionButton;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.conditions.WorldNameConditionButton;
import me.wolfyscript.customcrafting.recipes.ICustomRecipe;
import me.wolfyscript.customcrafting.recipes.conditions.*;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.gui.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.gui.button.buttons.ActionButton;
import me.wolfyscript.utilities.util.inventory.PlayerHeadUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConditionsMenu extends CCWindow {

    private static final String BACK = "back";

    public ConditionsMenu(GuiCluster<CCCache> cluster, CustomCrafting customCrafting) {
        super(cluster, "conditions", 45, customCrafting);
    }

    @Override
    public void onInit() {
        registerButton(new ActionButton<>(BACK, new ButtonState<>(MainCluster.BACK, PlayerHeadUtils.getViaURL("864f779a8e3ffa231143fa69b96b14ee35c16d669e19c75fd1a7da4bf306c"), (cache, guiHandler, player, inventory, slot, event) -> {
            guiHandler.openPreviousWindow();
            return true;
        })));

        registerButton(new ActionButton<>("conditions.world_time", new ButtonState<>("world_time", Material.CLOCK, (cache, guiHandler, player, inventory, slot, event) -> {
            var conditions = guiHandler.getCustomCache().getRecipe().getConditions();
            if (event instanceof InventoryClickEvent clickEvent) {
                if (clickEvent.getClick().isRightClick()) {
                    //Change Mode
                    Condition condition = conditions.getByType(WorldTimeCondition.class);
                    if (condition == null) {
                        conditions.setCondition(new WorldTimeCondition());
                    } else {
                        conditions.removeCondition(condition);
                    }
                } else {
                    //Change Value
                    openChat("world_time", guiHandler, (guiHandler1, player1, s, strings) -> {
                        try {
                            long value = Long.parseLong(s);
                            conditions.getByType(WorldTimeCondition.class).setTime(value);
                        } catch (NumberFormatException ex) {
                            api.getChat().sendKey(player1, "recipe_creator", "valid_number");
                        }
                        return false;
                    });
                }
            }
            return true;
        }, (hashMap, cache, guiHandler, player, inventory, itemStack, slot, help) -> {
            ICustomRecipe<?> recipeConfig = guiHandler.getCustomCache().getRecipe();
            hashMap.put("%VALUE%", recipeConfig.getConditions().getByType(WorldTimeCondition.class).getTime());
            hashMap.put("%MODE%", recipeConfig.getConditions().getByType(WorldTimeCondition.class).getOption().getDisplayString(api));
            return itemStack;
        })));

        registerButton(new ActionButton<>("conditions.player_experience", new ButtonState<>("player_experience", Material.EXPERIENCE_BOTTLE, (cache, guiHandler, player, inventory, slot, event) -> {
            var conditions = guiHandler.getCustomCache().getRecipe().getConditions();
            if (event instanceof InventoryClickEvent) {
                if (((InventoryClickEvent) event).getClick().isRightClick()) {
                    //Change Mode
                    conditions.getByType(ExperienceCondition.class).toggleOption();
                } else {
                    //Change Value
                    openChat("player_experience", guiHandler, (guiHandler1, player1, s, strings) -> {
                        try {
                            int value = Integer.parseInt(s);
                            conditions.getByType(ExperienceCondition.class).setExpLevel(value);
                        } catch (NumberFormatException ex) {
                            api.getChat().sendKey(player1, "recipe_creator", "valid_number");
                        }
                        return false;
                    });
                }
            }
            return true;
        }, (hashMap, cache, guiHandler, player, inventory, itemStack, slot, help) -> {
            ICustomRecipe<?> recipeConfig = guiHandler.getCustomCache().getRecipe();
            hashMap.put("%VALUE%", recipeConfig.getConditions().getByType(ExperienceCondition.class).getExpLevel());
            hashMap.put("%MODE%", recipeConfig.getConditions().getByType(ExperienceCondition.class).getOption().getDisplayString(api));
            return itemStack;
        })));

        registerButton(new ActionButton<>("conditions.weather", new ButtonState<>("weather", Material.WATER_BUCKET, (cache, guiHandler, player, inventory, slot, event) -> {
            ICustomRecipe<?> recipeConfig = guiHandler.getCustomCache().getRecipe();
            var conditions = recipeConfig.getConditions();
            if (event instanceof InventoryClickEvent) {
                if (((InventoryClickEvent) event).getClick().isRightClick()) {
                    //Change Mode
                    conditions.getByType(WeatherCondition.class).toggleOption();
                } else {
                    //Change Value
                    conditions.getByType(WeatherCondition.class).toggleWeather();
                }
            }
            return true;
        }, (hashMap, cache, guiHandler, player, inventory, itemStack, slot, help) -> {
            ICustomRecipe<?> recipeConfig = guiHandler.getCustomCache().getRecipe();
            hashMap.put("%VALUE%", recipeConfig.getConditions().getByType(WeatherCondition.class).getWeather().getDisplay(api));
            hashMap.put("%MODE%", recipeConfig.getConditions().getByType(WeatherCondition.class).getOption().getDisplayString(api));
            return itemStack;
        })));

        registerButton(new ActionButton<>("conditions.advanced_workbench", new ButtonState<>("advanced_workbench", Material.CRAFTING_TABLE, (cache, guiHandler, player, inventory, slot, event) -> {
            if (event instanceof InventoryClickEvent && ((InventoryClickEvent) event).getClick().isLeftClick()) {
                //Change Mode
                guiHandler.getCustomCache().getRecipe().getConditions().getByType(AdvancedWorkbenchCondition.class).toggleOption();
            }
            return true;
        }, (hashMap, cache, guiHandler, player, inventory, itemStack, slot, help) -> {
            hashMap.put("%MODE%", guiHandler.getCustomCache().getRecipe().getConditions().getByType(AdvancedWorkbenchCondition.class).getOption().getDisplayString(api));
            return itemStack;
        })));

        registerButton(new EliteWorkbenchConditionButton());
        registerButton(new WorldBiomeConditionButton());
        registerButton(new WorldNameConditionButton());

        registerButton(new ActionButton<>("conditions.permission", new ButtonState<>("permission", Material.REDSTONE, (cache, guiHandler, player, inventory, slot, event) -> {
            if (event instanceof InventoryClickEvent) {
                if (((InventoryClickEvent) event).getClick().isRightClick()) {
                    //Change Mode
                    guiHandler.getCustomCache().getRecipe().getConditions().getByType(PermissionCondition.class).toggleOption();
                } else {
                    //SET Custom Permission String
                    openChat("permission", guiHandler, (guiHandler1, player1, s, strings) -> {
                        guiHandler.getCustomCache().getRecipe().getConditions().getByType(PermissionCondition.class).setPermission(s.trim());
                        return false;
                    });
                }
            }
            return true;
        }, (hashMap, cache, guiHandler, player, inventory, itemStack, slot, help) -> {
            ICustomRecipe<?> recipeConfig = guiHandler.getCustomCache().getRecipe();
            hashMap.put("%VALUE%", recipeConfig.getConditions().getByType(PermissionCondition.class).getPermission());
            hashMap.put("%MODE%", recipeConfig.getConditions().getByType(PermissionCondition.class).getOption().getDisplayString(api));
            return itemStack;
        })));

        registerButton(new ActionButton<>("conditions.craft_delay", new ButtonState<>("craft_delay", Material.CLOCK, (cache, guiHandler, player, inventory, slot, event) -> {
            var conditions = guiHandler.getCustomCache().getRecipe().getConditions();
            if (event instanceof InventoryClickEvent) {
                if (((InventoryClickEvent) event).getClick().isRightClick()) {
                    //Change Mode
                    conditions.getByType(CraftDelayCondition.class).toggleOption();
                } else {
                    //Change Value
                    openChat("craft_delay", guiHandler, (guiHandler1, player1, s, strings) -> {
                        try {
                            long value = Long.parseLong(s);
                            conditions.getByType(CraftDelayCondition.class).setDelay(value);
                        } catch (NumberFormatException ex) {
                            api.getChat().sendKey(player1, "recipe_creator", "valid_number");
                        }
                        return false;
                    });
                }

            }
            return true;
        }, (hashMap, cache, guiHandler, player, inventory, itemStack, slot, help) -> {
            ICustomRecipe<?> recipeConfig = guiHandler.getCustomCache().getRecipe();
            hashMap.put("%VALUE%", recipeConfig.getConditions().getByType(CraftDelayCondition.class).getDelay());
            hashMap.put("%MODE%", recipeConfig.getConditions().getByType(CraftDelayCondition.class).getOption().getDisplayString(api));
            return itemStack;
        })));

        registerButton(new ActionButton<>("conditions.craft_limit", new ButtonState<>("craft_limit", Material.BARRIER, (cache, guiHandler, player, inventory, slot, event) -> {
            var conditions = guiHandler.getCustomCache().getRecipe().getConditions();
            if (event instanceof InventoryClickEvent) {
                if (((InventoryClickEvent) event).getClick().isRightClick()) {
                    //Change Mode
                    conditions.getByType(CraftLimitCondition.class).toggleOption();
                } else {
                    //Change Value
                    openChat("craft_limit", guiHandler, (guiHandler1, player1, s, strings) -> {
                        try {
                            conditions.getByType(CraftLimitCondition.class).setLimit(Long.parseLong(s));
                        } catch (NumberFormatException ex) {
                            api.getChat().sendKey(player1, "recipe_creator", "valid_number");
                        }
                        return false;
                    });
                }

            }
            return true;
        }, (hashMap, cache, guiHandler, player, inventory, itemStack, slot, help) -> {
            ICustomRecipe<?> recipeConfig = guiHandler.getCustomCache().getRecipe();
            hashMap.put("%VALUE%", recipeConfig.getConditions().getByType(CraftLimitCondition.class).getLimit());
            hashMap.put("%MODE%", recipeConfig.getConditions().getByType(CraftLimitCondition.class).getOption().getDisplayString(api));
            return itemStack;
        })));
    }

    @Override
    public void onUpdateAsync(GuiUpdate<CCCache> event) {
        super.onUpdateAsync(event);
        CCCache cache = event.getGuiHandler().getCustomCache();
        event.setButton(0, BACK);
        List<String> values = new ArrayList<>();
        values.add("conditions.world_time");
        values.add("conditions.world_name");
        values.add("conditions.world_biome");
        values.add("conditions.weather");
        switch (cache.getRecipeType().getType()) {
            case WORKBENCH -> values.addAll(Arrays.asList("conditions.permission", "conditions.player_experience", "conditions.advanced_workbench", "conditions.craft_delay", "conditions.craft_limit"));
            case ELITE_WORKBENCH -> values.addAll(Arrays.asList("conditions.permission", "conditions.player_experience", "conditions.elite_workbench", "conditions.craft_delay", "conditions.craft_limit"));
            case BREWING_STAND, GRINDSTONE -> values.addAll(Arrays.asList("conditions.permission", "conditions.player_experience"));
            default -> {/*No special conditions!*/}
        }
        for (int i = 0, item = 9; i < values.size() && item < 45; i++, item++) {
            event.setButton(item, values.get(i));
        }
    }
}
