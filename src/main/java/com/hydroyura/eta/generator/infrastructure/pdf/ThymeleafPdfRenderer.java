package com.hydroyura.eta.generator.infrastructure.pdf;

import com.hydroyura.eta.generator.api.document.PdfRenderException;
import com.hydroyura.eta.generator.domain.document.ExercisePdfRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class ThymeleafPdfRenderer implements ExercisePdfRenderer {

    private static final String TEMPLATE_PREFIX = "/generator/templates/";

    private static final String TEMPLATE_SUFFIX = ".html";

    private static final String FONT_RESOURCE = "/generator/fonts/DejaVuSans.ttf";

    private static final String FONT_FAMILY = "DejaVu Sans";

    private final SpringTemplateEngine templateEngine;

    private final File fontFile;

    public ThymeleafPdfRenderer() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(TEMPLATE_PREFIX);
        resolver.setSuffix(TEMPLATE_SUFFIX);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        this.templateEngine = new SpringTemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
        this.fontFile = loadFont();
    }

    @Override
    public byte[] render(String templateName, Map<String, Object> model) {
        Objects.requireNonNull(templateName, "templateName must not be null");
        Objects.requireNonNull(model, "model must not be null");

        String html;
        try {
            html = templateEngine.process(templateName, new Context(Locale.ROOT, model));
        } catch (RuntimeException e) {
            throw new PdfRenderException("Failed to process template: " + templateName, e);
        }

        try (var out = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(fontFile, FONT_FAMILY);
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (PdfRenderException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfRenderException("Failed to render PDF from template: " + templateName, e);
        }
    }

    private File loadFont() {
        try (var in = getClass().getResourceAsStream(FONT_RESOURCE)) {
            if (in == null) {
                throw new PdfRenderException("Embedded font not found: " + FONT_RESOURCE);
            }
            var temp = File.createTempFile("dejavu-sans", ".ttf");
            temp.deleteOnExit();
            try (var out = new FileOutputStream(temp)) {
                in.transferTo(out);
            }
            return temp;
        } catch (PdfRenderException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfRenderException("Failed to load embedded font", e);
        }
    }
}
