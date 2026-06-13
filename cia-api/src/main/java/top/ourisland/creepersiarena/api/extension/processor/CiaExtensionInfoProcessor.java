package top.ourisland.creepersiarena.api.extension.processor;

import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;
import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Generates CIA extension metadata from {@link CiaExtensionInfo}.
 * <p>
 * The processor lives in {@code cia-api} intentionally: extension projects can use the same dependency as both
 * {@code compileOnly} API and {@code annotationProcessor} metadata generator.
 */
@SupportedAnnotationTypes(
        {
                "top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo",
                "top.ourisland.creepersiarena.api.annotation.CiaJobDef",
                "top.ourisland.creepersiarena.api.annotation.CiaModeDef",
                "top.ourisland.creepersiarena.api.annotation.CiaSkillDef"
        }
)
@SupportedOptions(CiaExtensionInfoProcessor.OPTION_EXTENSION_VERSION)
public final class CiaExtensionInfoProcessor extends AbstractProcessor {

    public static final String OPTION_EXTENSION_VERSION = "cia.extension.version";

    private static final String EXTENSION_TYPE = "top.ourisland.creepersiarena.api.extension.ICiaExtension";
    private static final String SERVICE_FILE = "META-INF/services/" + EXTENSION_TYPE;
    private static final String DESCRIPTOR_FILE = "cia-extension.yml";
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]*");
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_-]*");
    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile(
            "([a-z0-9][a-z0-9_-]*):([a-z0-9][a-z0-9_-]*(/[a-z0-9][a-z0-9_-]*)*)"
    );

    private boolean generated;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv
    ) {
        if (roundEnv.processingOver() || generated) {
            return false;
        }

        var annotated = new ArrayList<TypeElement>();
        for (var element : roundEnv.getElementsAnnotatedWith(CiaExtensionInfo.class)) {
            if (element instanceof TypeElement typeElement) {
                annotated.add(typeElement);
            } else {
                error(element, "@CiaExtensionInfo can only be applied to classes");
            }
        }

        if (annotated.isEmpty()) {
            return false;
        }

        if (annotated.size() > 1) {
            for (var element : annotated) {
                error(element, "Only one @CiaExtensionInfo entry point is allowed per extension module");
            }
            return true;
        }

        var mainType = annotated.getFirst();
        if (!validateMainType(mainType)) {
            return true;
        }

        var info = mainType.getAnnotation(CiaExtensionInfo.class);
        if (!validateInfo(mainType, info)) {
            return true;
        }
        if (!validateComponentIds(roundEnv, info.namespace())) {
            return true;
        }

        String mainClass = processingEnv.getElementUtils().getBinaryName(mainType).toString();
        String version = valueOrOption(info.version(), OPTION_EXTENSION_VERSION, "unspecified");
        String ciaVersion = info.ciaVersion().isBlank() ? version : info.ciaVersion().trim();

        try {
            writeDescriptor(mainClass, info, version, ciaVersion);
            writeServiceProvider(mainClass);
            generated = true;
        } catch (IOException exception) {
            error(mainType, "Failed to generate CIA extension metadata: " + exception.getMessage());
        }

        return true;
    }

    private void error(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private boolean validateMainType(TypeElement mainType) {
        boolean valid = true;

        if (mainType.getKind() != ElementKind.CLASS) {
            error(mainType, "@CiaExtensionInfo target must be a class");
            valid = false;
        }
        if (!mainType.getModifiers().contains(Modifier.PUBLIC)) {
            error(mainType, "CIA extension entry point must be public");
            valid = false;
        }
        if (mainType.getModifiers().contains(Modifier.ABSTRACT)) {
            error(mainType, "CIA extension entry point must not be abstract");
            valid = false;
        }
        if (!hasUsableServiceLoaderConstructor(mainType)) {
            error(mainType, "CIA extension entry point must declare a public no-argument constructor");
            valid = false;
        }

        var extensionType = processingEnv.getElementUtils().getTypeElement(EXTENSION_TYPE);
        if (extensionType == null) {
            error(mainType, "Cannot resolve " + EXTENSION_TYPE);
            return false;
        }

        var extensionMirror = extensionType.asType();
        if (!processingEnv.getTypeUtils().isAssignable(mainType.asType(), extensionMirror)) {
            error(mainType, "CIA extension entry point must implement " + EXTENSION_TYPE);
            valid = false;
        }

        return valid;
    }

    private boolean validateInfo(TypeElement mainType, CiaExtensionInfo info) {
        boolean valid = true;

        if (info.id().isBlank()) {
            error(mainType, "CIA extension id must not be blank");
            valid = false;
        } else if (!ID_PATTERN.matcher(info.id()).matches()) {
            error(mainType, "CIA extension id must match [a-z0-9_.-]+: " + info.id());
            valid = false;
        }

        if (info.namespace().isBlank()) {
            error(mainType, "CIA extension namespace must not be blank");
            valid = false;
        } else if (!NAMESPACE_PATTERN.matcher(info.namespace()).matches()) {
            error(mainType, "CIA extension namespace must match [a-z0-9][a-z0-9_-]*: " + info.namespace());
            valid = false;
        } else if ("core".equals(info.namespace()) || "minecraft".equals(info.namespace())) {
            error(mainType, "CIA extension namespace is reserved: " + info.namespace());
            valid = false;
        }

        if (info.name().isBlank()) {
            error(mainType, "CIA extension name must not be blank");
            valid = false;
        }

        if (info.apiVersion() <= 0) {
            error(mainType, "CIA extension apiVersion must be positive");
            valid = false;
        }

        for (String dependency : info.requiredDependencies()) {
            valid &= validateDependency(mainType, dependency, "requiredDependencies");
        }
        for (String dependency : info.optionalDependencies()) {
            valid &= validateDependency(mainType, dependency, "optionalDependencies");
        }

        return valid;
    }

    private boolean validateComponentIds(RoundEnvironment roundEnv, String namespace) {
        boolean valid = true;
        Set<String> seenIds = new HashSet<>();

        for (var element : roundEnv.getElementsAnnotatedWith(CiaJobDef.class)) {
            var annotation = element.getAnnotation(CiaJobDef.class);
            valid &= validateResourceId(element, annotation.id(), namespace, "job", seenIds);
        }
        for (var element : roundEnv.getElementsAnnotatedWith(CiaModeDef.class)) {
            var annotation = element.getAnnotation(CiaModeDef.class);
            valid &= validateResourceId(element, annotation.id(), namespace, "mode", seenIds);
        }
        for (var element : roundEnv.getElementsAnnotatedWith(CiaSkillDef.class)) {
            var annotation = element.getAnnotation(CiaSkillDef.class);
            boolean skillValid = validateResourceId(element, annotation.id(), namespace, "skill", seenIds);
            boolean jobValid = validateResourceId(element, annotation.job(), namespace, "skill job", null);
            valid &= skillValid && jobValid;

            if (skillValid && jobValid) {
                String skillPath = resourcePath(annotation.id());
                String jobPath = resourcePath(annotation.job());
                if (!skillPath.startsWith(jobPath + "/")) {
                    error(element, "Skill id must be nested below its owning job path: "
                            + annotation.id() + " is not below " + annotation.job());
                    valid = false;
                }
            }
        }
        return valid;
    }

    private String valueOrOption(
            String annotationValue,
            String optionName,
            String fallback
    ) {
        if (!annotationValue.isBlank()) {
            return annotationValue.trim();
        }

        String option = processingEnv.getOptions().get(optionName);
        if (option != null && !option.isBlank()) {
            return option.trim();
        }

        return fallback;
    }

    private void writeDescriptor(
            String mainClass,
            CiaExtensionInfo info,
            String version,
            String ciaVersion
    ) throws IOException {
        String descriptor = descriptorYaml(mainClass, info, version, ciaVersion);
        writeResource(DESCRIPTOR_FILE, descriptor);
    }

    private void writeServiceProvider(String mainClass) throws IOException {
        writeResource(SERVICE_FILE, mainClass + System.lineSeparator());
    }

    private boolean hasUsableServiceLoaderConstructor(TypeElement mainType) {
        boolean hasExplicitConstructor = false;
        for (var enclosed : mainType.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }

            hasExplicitConstructor = true;
            var constructor = (ExecutableElement) enclosed;
            if (constructor.getParameters().isEmpty() && constructor.getModifiers().contains(Modifier.PUBLIC)) {
                return true;
            }
        }

        return !hasExplicitConstructor;
    }

    private boolean validateDependency(
            TypeElement mainType,
            String dependency,
            String fieldName
    ) {
        if (dependency.isBlank()) {
            error(mainType, "CIA extension " + fieldName + " contains a blank dependency id");
            return false;
        }
        if (!ID_PATTERN.matcher(dependency).matches()) {
            error(mainType, "CIA extension dependency id must match [a-z0-9_.-]+: " + dependency);
            return false;
        }
        return true;
    }

    private boolean validateResourceId(
            Element element,
            String raw,
            String expectedNamespace,
            String kind,
            Set<String> seenIds
    ) {
        if (raw == null) {
            error(element, "Invalid " + kind + " id; expected strict namespace:path form: null");
            return false;
        }
        var matcher = RESOURCE_ID_PATTERN.matcher(raw);
        if (!matcher.matches()) {
            error(element, "Invalid " + kind + " id; expected strict namespace:path form: " + raw);
            return false;
        }
        String namespace = matcher.group(1);
        if (!namespace.equals(expectedNamespace)) {
            error(element, kind + " id namespace must equal extension namespace '"
                    + expectedNamespace + "': " + raw);
            return false;
        }
        if (seenIds != null && !seenIds.add(raw)) {
            error(element, "Duplicate extension resource id: " + raw);
            return false;
        }
        return true;
    }

    private String resourcePath(String raw) {
        var matcher = RESOURCE_ID_PATTERN.matcher(raw);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid resource id: " + raw);
        }
        return matcher.group(2);
    }

    private String descriptorYaml(
            String mainClass,
            CiaExtensionInfo info,
            String version,
            String ciaVersion
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("id: ").append(quote(info.id())).append('\n');
        builder.append("namespace: ").append(quote(info.namespace())).append('\n');
        builder.append("name: ").append(quote(info.name())).append('\n');
        builder.append("version: ").append(quote(version)).append('\n');
        builder.append("main: ").append(quote(mainClass)).append('\n');
        builder.append("api-version: ").append(info.apiVersion()).append('\n');
        builder.append("cia-version: ").append(quote(ciaVersion)).append('\n');
        appendStringListField(builder, "authors", info.authors(), 0);
        builder.append("dependencies:").append('\n');
        appendStringListField(builder, "required", info.requiredDependencies(), 2);
        appendStringListField(builder, "optional", info.optionalDependencies(), 2);
        builder.append("load:").append('\n');
        builder.append("  order: ").append(loadOrderName(info.loadOrder())).append('\n');
        return builder.toString();
    }

    private void writeResource(String path, String content) throws IOException {
        var filer = processingEnv.getFiler();
        var resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", path);
        try (Writer writer = resource.openWriter()) {
            writer.write(content);
        }
    }

    private String quote(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private void appendStringListField(
            StringBuilder builder,
            String key,
            String[] values,
            int indent
    ) {
        String prefix = " ".repeat(indent);
        if (values.length == 0) {
            builder.append(prefix).append(key).append(": [ ]").append('\n');
            return;
        }

        builder.append(prefix).append(key).append(":").append('\n');
        for (String value : values) {
            builder.repeat(" ", indent + 2).append("- ").append(quote(value)).append('\n');
        }
    }

    private String loadOrderName(CiaExtensionLoadOrder loadOrder) {
        return switch (loadOrder) {
            case EARLY -> "EARLY";
            case NORMAL -> "NORMAL";
            case LATE -> "LATE";
        };
    }

}
