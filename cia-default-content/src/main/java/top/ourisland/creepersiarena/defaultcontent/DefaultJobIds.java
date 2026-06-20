package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.job.JobId;

public final class DefaultJobIds {

    public static final String
            CREEPER_VALUE = "cia:creeper",
            MOISON_VALUE = "cia:moison",
            AVENGER_VALUE = "cia:avenger",
            BLOODLINE_VALUE = "cia:bloodline",
            GOLEM_VALUE = "cia:golem",
            WOLONG_VALUE = "cia:wolong",
            YSAHAN_VALUE = "cia:ysahan";

    public static final JobId
            CREEPER = JobId.of(DefaultContentIds.key("creeper")),
            MOISON = JobId.of(DefaultContentIds.key("moison")),
            AVENGER = JobId.of(DefaultContentIds.key("avenger")),
            BLOODLINE = JobId.of(DefaultContentIds.key("bloodline")),
            GOLEM = JobId.of(DefaultContentIds.key("golem")),
            WOLONG = JobId.of(DefaultContentIds.key("wolong")),
            YSAHAN = JobId.of(DefaultContentIds.key("ysahan"));

    private DefaultJobIds() {
    }

}
