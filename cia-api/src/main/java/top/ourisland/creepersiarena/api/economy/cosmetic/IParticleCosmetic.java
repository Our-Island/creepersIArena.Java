package top.ourisland.creepersiarena.api.economy.cosmetic;

public interface IParticleCosmetic extends ICosmetic {

    ParticleSchedule schedule();

    void spawn(ParticleCosmeticContext context);

}
