package me.wolfyscript.customcrafting.listeners;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.configs.MainConfig;
import me.wolfyscript.customcrafting.data.PlayerCache;
import me.wolfyscript.customcrafting.listeners.customevents.CustomCraftEvent;
import me.wolfyscript.customcrafting.listeners.customevents.CustomPreCraftEvent;
import me.wolfyscript.customcrafting.handlers.RecipeHandler;
import me.wolfyscript.customcrafting.items.ItemUtils;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import me.wolfyscript.customcrafting.recipes.workbench.CraftingRecipe;
import me.wolfyscript.utilities.api.WolfyUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.*;

import java.util.*;

public class CraftListener implements Listener {

    private WolfyUtilities api;

    private MainConfig config = CustomCrafting.getConfigHandler().getConfig();

    private HashMap<UUID, String> precraftedRecipes = new HashMap<>();
    private HashMap<UUID, ItemStack[]> replacements = new HashMap<>();

    public CraftListener(WolfyUtilities api) {
        this.api = api;
    }

    @EventHandler
    public void onAdvancedWorkbench(CustomPreCraftEvent event) {
        if (!event.isCancelled() && event.getRecipe().getId().equals("customcrafting:workbench")) {
            if (!CustomCrafting.getConfigHandler().getConfig().isAdvancedWorkbenchEnabled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDiscover(PlayerRecipeDiscoverEvent event) {
        if (CustomCrafting.getConfigHandler().getConfig().getDisabledRecipes().contains(event.getRecipe().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe() instanceof Keyed) {
            ItemStack result = event.getInventory().getResult();
            if (result != null && !result.getType().equals(Material.AIR) && precraftedRecipes.containsKey(event.getWhoClicked().getUniqueId()) && precraftedRecipes.get(event.getWhoClicked().getUniqueId()) != null) {
                String key = precraftedRecipes.get(event.getWhoClicked().getUniqueId());
                CraftingRecipe recipe = CustomCrafting.getRecipeHandler().getCraftingRecipe(key);
                ItemStack[] matrix = event.getInventory().getMatrix();
                boolean small = matrix.length < 9;
                RecipeHandler recipeHandler = CustomCrafting.getRecipeHandler();
                List<List<ItemStack>> ingredients = recipeHandler.getIngredients(matrix);
                if (recipe != null) {
                    Player player = (Player) event.getWhoClicked();
                    Block block = player.getTargetBlock(null, 5);
                    PlayerCache cache = CustomCrafting.getPlayerCache(player);

                    CustomCraftEvent customCraftEvent = new CustomCraftEvent(recipe, event.getRecipe(), event.getInventory());
                    if (!customCraftEvent.isCancelled()) {

                        {//---------COMMANDS AND STATISTICS-------------
                            if (config.getCommandsSuccessCrafted() != null && !config.getCommandsSuccessCrafted().isEmpty()) {
                                for (String command : config.getCommandsSuccessCrafted()) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%P%", player.getName()).replace("%UUID%", player.getUniqueId().toString()).replace("%REC%", recipe.getId()));
                                }
                            }
                            cache.addRecipeCrafts(customCraftEvent.getRecipe().getId());
                            cache.addAmountCrafted(1);
                            if (CustomCrafting.getWorkbenches().isWorkbench(block.getLocation())) {
                                cache.addAmountAdvancedCrafted(1);
                            } else {
                                cache.addAmountNormalCrafted(1);
                            }
                        }//----------------------------------------------

                        //CALCULATE AMOUNTS CRAFTABLE AND REMOVE THEM!
                        int amount = recipe.getAmountCraftable(ingredients);
                        ItemStack resultItem = event.getCurrentItem();
                        List<ItemStack> replacements = new ArrayList<>();

                        if (event.getClick().equals(ClickType.SHIFT_RIGHT) || event.getClick().equals(ClickType.SHIFT_LEFT)) {
                            api.sendDebugMessage("SHIFT-CLICK!");
                            if (resultItem != null && resultItem.getAmount() > 0) {
                                int possible = ItemUtils.getInventorySpace(player, resultItem) / resultItem.getAmount();
                                if (possible > amount) {
                                    possible = amount;
                                }
                                if (possible > 0) {
                                    api.sendDebugMessage(" possible: " + possible);
                                    replacements = recipe.removeMatrix(ingredients, event.getInventory(), small, possible);
                                }
                                for (int i = 0; i < possible; i++) {
                                    player.getInventory().addItem(resultItem);
                                }
                            }
                        } else {
                            api.sendDebugMessage("ONE-CLICK!");
                            if(event.getView().getCursor() == null || event.getView().getCursor().getType().equals(Material.AIR) || (event.getView().getCursor() != null && event.getView().getCursor().getAmount() < event.getCursor().getMaxStackSize())){
                                replacements = recipe.removeMatrix(ingredients, event.getInventory(), small, 1);
                                if(event.getView().getCursor() != null && event.getView().getCursor().isSimilar(resultItem)){

                                    event.getView().getCursor().setAmount(event.getView().getCursor().getAmount() + resultItem.getAmount());
                                }else{
                                    event.getView().setCursor(resultItem);
                                }
                            }
                            Bukkit.getScheduler().runTaskLater(CustomCrafting.getInst(), () -> {
                                PrepareItemCraftEvent event1 = new PrepareItemCraftEvent(event.getInventory(), event.getView(), false);
                                Bukkit.getPluginManager().callEvent(event1);
                            }, 2);
                        }
                        for(ItemStack itemStack : replacements){
                            if(ItemUtils.hasInventorySpace(player, itemStack)){
                                player.getInventory().addItem(itemStack);
                            }else{
                                player.getLocation().getWorld().dropItemNaturally(player.getLocation(), itemStack);
                            }
                        }
                        event.setCurrentItem(new ItemStack(Material.AIR));
                    }
                }
            }
        }
        precraftedRecipes.put(event.getWhoClicked().getUniqueId(), null);
    }

    @EventHandler
    public void onPreCraft(PrepareItemCraftEvent e) {
        Player player = (Player) e.getView().getPlayer();
        if (e.getRecipe() != null) {
            if (e.getRecipe() instanceof Keyed) {
                try {
                    ItemStack[] matrix = e.getInventory().getMatrix();
                    RecipeHandler recipeHandler = CustomCrafting.getRecipeHandler();

                    List<List<ItemStack>> ingredients = recipeHandler.getIngredients(matrix);
                    List<CraftingRecipe> recipesToCheck = new ArrayList<>(recipeHandler.getSimilarRecipes(ingredients));
                    recipesToCheck.sort(Comparator.comparing(CustomRecipe::getPriority));

                    boolean allow = false;
                    if (!recipesToCheck.isEmpty() && !CustomCrafting.getConfigHandler().getConfig().isLockedDown()) {
                        CustomPreCraftEvent customPreCraftEvent;
                        for (CraftingRecipe recipe : recipesToCheck) {
                            if (recipe != null && !recipeHandler.getDisabledRecipes().contains(recipe.getId())) {
                                customPreCraftEvent = new CustomPreCraftEvent(e.isRepair(), recipe, e.getRecipe(), e.getInventory(), ingredients);
                                boolean perm = checkWorkbenchAndPerm(player, e.getView().getPlayer().getTargetBlock(null, 5).getLocation(), recipe);
                                boolean check = recipe.check(ingredients);
                                if (!(perm && check) || recipeHandler.getDisabledRecipes().contains(recipe.getId())) {
                                    customPreCraftEvent.setCancelled(true);
                                }
                                Bukkit.getPluginManager().callEvent(customPreCraftEvent);
                                if (!customPreCraftEvent.isCancelled()) {
                                    //ALLOW
                                    precraftedRecipes.put(player.getUniqueId(), customPreCraftEvent.getRecipe().getId());
                                    e.getInventory().setResult(customPreCraftEvent.getResult());
                                    allow = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!allow) {
                        precraftedRecipes.remove(player.getUniqueId());
                        CraftingRecipe recipe = recipeHandler.getCraftingRecipe(((Keyed) e.getRecipe()).getKey().toString());
                        if (recipeHandler.getDisabledRecipes().contains(((Keyed) e.getRecipe()).getKey().toString())) {
                            e.getInventory().setResult(new ItemStack(Material.AIR));
                        } else if (recipe != null) {
                            e.getInventory().setResult(new ItemStack(Material.AIR));
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("WHAT HAPPENED? Please report!");
                    ex.printStackTrace();
                    System.out.println("WHAT HAPPENED? Please report!");
                    precraftedRecipes.remove(player.getUniqueId());
                    e.getInventory().setResult(new ItemStack(Material.AIR));
                }
            }
        }
    }

    private boolean checkWorkbenchAndPerm(Player player, Location location, CraftingRecipe recipe) {
        if (!recipe.needsAdvancedWorkbench() || (location != null && CustomCrafting.getWorkbenches().isWorkbench(location))) {
            String perm = "customcrafting.craft." + recipe.getId();
            String perm2 = "customcrafting.craft." + recipe.getId().split(":")[0];

            if (recipe.needsPermission()) {
                if (!player.hasPermission("customcrafting.craft.*")) {
                    if (!player.hasPermission(perm)) {
                        return player.hasPermission(perm2);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public HashMap<UUID, ItemStack[]> getReplacements() {
        return replacements;
    }
}