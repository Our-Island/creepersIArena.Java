package top.ourisland.creepersiarena.api.game.mutation;

import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

/**
 * Public mutation effect registration surface.
 */
public interface IMutationRegistry {

    void registerMutation(RegistrationOwner owner, IMutationEffect effect);

}
