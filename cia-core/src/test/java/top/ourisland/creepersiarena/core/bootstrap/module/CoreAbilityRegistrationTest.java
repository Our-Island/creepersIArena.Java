package top.ourisland.creepersiarena.core.bootstrap.module;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreAbilityRegistrationTest {

    @Test
    void everyCoreAbilityHasExactlyOneBootstrapProvider() {
        var providers = new ArrayList<>(AbilityModule.STATIC_CORE_ABILITY_IDS);
        providers.addAll(List.of(
                CoreAbilities.MUTATION,
                CoreAbilities.RESTING_REGENERATION,
                CoreAbilities.CURRENCY,
                CoreAbilities.STORE_UI,
                CoreAbilities.COSMETIC_RUNTIME
        ));

        var uniqueProviders = new HashSet<>(providers);
        assertEquals(
                providers.size(),
                uniqueProviders.size(),
                "Each core ability id must have exactly one bootstrap provider"
        );
        assertEquals(
                declaredCoreAbilityIds(),
                uniqueProviders,
                "Every CoreAbilities constant must be assigned to a bootstrap provider"
        );
    }

    private static Set<AbilityId> declaredCoreAbilityIds() {
        return Arrays.stream(CoreAbilities.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getType() == AbilityId.class)
                .map(field -> {
                    try {
                        return (AbilityId) field.get(null);
                    } catch (IllegalAccessException exception) {
                        throw new AssertionError("Cannot read CoreAbilities." + field.getName(), exception);
                    }
                })
                .collect(Collectors.toUnmodifiableSet());
    }

}
