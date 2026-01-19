package top.ourisland.creepersiarena.bootstrap;

public enum StagePhase {
    LOAD("Bootstrap-Load"),
    START("Bootstrap-Start"),
    STOP("Bootstrap-Stop"),
    RELOAD("Bootstrap-Reload");

    private final String tag;

    StagePhase(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return tag;
    }
}
