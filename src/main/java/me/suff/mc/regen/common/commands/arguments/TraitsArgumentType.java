package me.suff.mc.regen.common.commands.arguments;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.suff.mc.regen.common.traits.AbstractTrait;
import me.suff.mc.regen.common.traits.RegenTraitRegistry;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class TraitsArgumentType implements ArgumentType< AbstractTrait > {
    public static final DynamicCommandExceptionType INVALID_TRAIT_EXCEPTION = new DynamicCommandExceptionType((trait) -> new TranslationTextComponent("argument.regeneration.trait.invalid", new Object[]{trait}));

    public static TraitsArgumentType createArgument() {
        return new TraitsArgumentType();
    }

    @Override
    public AbstractTrait parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation location = ResourceLocation.read(reader);
        AbstractTrait trait = RegenTraitRegistry.TRAIT_REGISTRY.get().getValue(location);
        if (trait != null) {
            return trait;
        }
        throw INVALID_TRAIT_EXCEPTION.create(location);
    }

    @Override
    public < S > CompletableFuture< Suggestions > listSuggestions(CommandContext< S > context, SuggestionsBuilder builder) {
        return ISuggestionProvider.suggestResource(RegenTraitRegistry.TRAIT_REGISTRY.get().getKeys(), builder);
    }

}