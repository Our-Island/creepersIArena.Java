package top.ourisland.creepersiarena.core.command.model;

/**
 * Mutating wallet operation used by /ciaa economy.
 */
public enum EconomyOperation {

    GIVE("give"),
    TAKE("take"),
    SET("set");

    private final String id;

    EconomyOperation(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

}
