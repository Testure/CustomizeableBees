package com.turing.customizablebees.api.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import forestry.api.recipes.IMoistenerRecipe;
import forestry.api.recipes.RecipeManagers;
import forestry.factory.recipes.MoistenerRecipe;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class Moistener extends VirtualizedRegistry<IMoistenerRecipe> {
    public Moistener() {
        super();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Override
    @GroovyBlacklist
    public void onReload() {
        removeScripted().forEach(RecipeManagers.moistenerManager::removeRecipe);
        restoreFromBackup().forEach(RecipeManagers.moistenerManager::addRecipe);
    }

    public IMoistenerRecipe add(ItemStack input, ItemStack output, int time) {
        MoistenerRecipe recipe = new MoistenerRecipe(input, output, time);
        add(recipe);
        return recipe;
    }

    public void add(IMoistenerRecipe recipe) {
        if (recipe == null) return;
        addScripted(recipe);
        RecipeManagers.moistenerManager.addRecipe(recipe);
    }

    public boolean remove(IMoistenerRecipe recipe) {
        if (recipe == null) return false;
        addBackup(recipe);
        RecipeManagers.moistenerManager.removeRecipe(recipe);
        return true;
    }

    public boolean removeByInput(IIngredient input) {
        Collection<IMoistenerRecipe> recipes = new ArrayList<>(RecipeManagers.moistenerManager.recipes());
        if (recipes.removeIf(recipe -> {
            boolean found = input.test(recipe.getResource());
            if (found) remove(recipe);
            return found;
        })) return true;

        GroovyLog.msg("Error removing Forestry Moistener recipe")
                .add("could not find recipe with input %s", input)
                .error()
                .post();
        return false;
    }

    public boolean removeByOutput(IIngredient output) {
        Collection<IMoistenerRecipe> recipes = new ArrayList<>(RecipeManagers.moistenerManager.recipes());
        if (recipes.removeIf(recipe -> {
            boolean found = output.test(recipe.getProduct());
            if (found) remove(recipe);
            return found;
        })) return true;

        GroovyLog.msg("Error removing Forestry Moistener recipe")
                .add("could not find recipe with output %s", output)
                .error()
                .post();
        return false;
    }

    public boolean removeByInputAndOutput(IIngredient input, IIngredient output) {
        Collection<IMoistenerRecipe> recipes = new ArrayList<>(RecipeManagers.moistenerManager.recipes());
        if (recipes.removeIf(recipe -> {
            boolean found = input.test(recipe.getResource()) && output.test(recipe.getProduct());
            if (found) remove(recipe);
            return found;
        })) return true;

        GroovyLog.msg("Error removing Forestry Moistener recipe")
                .add("could not find recipe with input %s and output %s", input, output)
                .error()
                .post();
        return false;
    }

    public void removeAll() {
        RecipeManagers.moistenerManager.recipes().forEach(this::addBackup);
        Collection<IMoistenerRecipe> recipes = new ArrayList<>(RecipeManagers.moistenerManager.recipes());
        recipes.forEach(RecipeManagers.moistenerManager::removeRecipe);
    }

    public SimpleObjectStream<IMoistenerRecipe> streamRecipes() {
        return new SimpleObjectStream<>(RecipeManagers.moistenerManager.recipes()).setRemover(this::remove);
    }

    public class RecipeBuilder extends AbstractRecipeBuilder<IMoistenerRecipe> {
        private int time = 20;

        public RecipeBuilder setTime(int time) {
            this.time = Math.max(time, 0);
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Forestry Moistener recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 1, 1, 1);
            msg.add(time < 0, "Recipe time must be at least 0, got " + time);
        }

        @Override
        public @Nullable IMoistenerRecipe register() {
            if (!validate()) return null;
            MoistenerRecipe recipe = new MoistenerRecipe(this.input.get(0).getMatchingStacks()[0], this.output.get(0), time);
            add(recipe);
            return recipe;
        }
    }
}
