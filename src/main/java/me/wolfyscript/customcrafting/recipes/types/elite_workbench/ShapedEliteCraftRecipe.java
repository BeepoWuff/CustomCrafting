package me.wolfyscript.customcrafting.recipes.types.elite_workbench;

import me.wolfyscript.customcrafting.recipes.types.ShapedCraftingRecipe;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.config.ConfigAPI;

public class ShapedEliteCraftRecipe extends EliteCraftingRecipe implements ShapedCraftingRecipe<EliteCraftConfig> {

    private String[] shape, shapeMirrorHorizontal, shapeMirrorVertical;
    private boolean mirrorHorizontal, mirrorVertical;

    public ShapedEliteCraftRecipe(EliteCraftConfig config) {
        super(config);
        this.shape = WolfyUtilities.formatShape(config.getShape()).toArray(new String[0]);
        this.shapeMirrorVertical = new String[6];
        int j = 0;
        for(int i = this.shape.length-1; i > 0; i--){
            this.shapeMirrorVertical[j] = config.getShape()[i];
            j++;
        }
        this.shapeMirrorVertical = WolfyUtilities.formatShape(this.shapeMirrorVertical).toArray(new String[0]);
        this.shapeMirrorHorizontal = this.shape.clone();
        for(int i = 0; i < this.shapeMirrorHorizontal.length; i++){
            this.shapeMirrorHorizontal[i] = new StringBuilder(this.shapeMirrorHorizontal[i]).reverse().toString();
        }
        this.mirrorHorizontal = config.mirrorHorizontal();
        this.mirrorVertical = config.mirrorVertical();
    }

    @Override
    public void load() {
    }

    @Override
    public ShapedEliteCraftRecipe save(ConfigAPI configAPI, String namespace, String key) {
        return null;
    }

    @Override
    public ShapedEliteCraftRecipe save(EliteCraftConfig config) {
        return null;
    }


    @Override
    public String[] getShapeMirrorHorizontal() {
        return shapeMirrorHorizontal;
    }

    @Override
    public String[] getShapeMirrorVertical() {
        return shapeMirrorVertical;
    }

    @Override
    public String[] getShape() {
        return shape;
    }

    @Override
    public boolean mirrorHorizontal() {
        return mirrorHorizontal;
    }

    @Override
    public boolean mirrorVertical() {
        return mirrorVertical;
    }
}