package top.ourisland.creepersiarena.api.cosmetic;

public interface IParticleCosmetic extends ICosmetic {

    ParticleSchedule schedule();

    void spawn(ParticleCosmeticContext context);

}
