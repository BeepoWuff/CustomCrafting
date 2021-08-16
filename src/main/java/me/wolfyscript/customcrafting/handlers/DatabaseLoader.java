package me.wolfyscript.customcrafting.handlers;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.utilities.util.NamespacedKey;

public abstract class DatabaseLoader extends ResourceLoader {

    protected DatabaseLoader(CustomCrafting customCrafting, NamespacedKey namespacedKey) {
        super(customCrafting, namespacedKey);
    }
}
