package top.ourisland.creepersiarena.api.game.mutation;

/**
 * Public mutation effect registration surface.
 */
public interface IMutationRegistry {

    void registerMutation(String ownerId, IMutationEffect effect);

}
