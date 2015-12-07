package com.inqool.dcap.mail;

import com.google.common.base.Charsets;
import com.inqool.dcap.config.Zdo;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Matus
 * Date: 29.10.2013
 * Time: 12:45
 */
@ApplicationScoped
public class TemplateService {

	@Inject
    @Zdo
	private Logger logger;

	@Inject
	private VelocityEngine velocityEngine;

    @Inject
    @ConfigProperty(name = "velocity.prefix")
    private String prefix;

    @Inject
    @ConfigProperty(name = "velocity.suffix")
    private String suffix;

	public void applyTemplate(String template, Map<String, Object> parameters, OutputStream out) throws IOException {
		checkNotNull(template);
		checkNotNull(parameters);

		VelocityContext context = new VelocityContext(parameters);

		Writer writer = new OutputStreamWriter(out, Charsets.UTF_8);

        velocityEngine.mergeTemplate(constructVelocityTemplate(template), "UTF-8", context, writer);
        writer.flush();
	}

//	public void applyXLSTemplate(String template, Map<String, Object> parameters, OutputStream out) {
//		checkNotNull(template);
//		checkNotNull(parameters);
//
//		InputStream in = getClass().getClassLoader().getResourceAsStream(template);
//
//		if (in == null) {
////			throw new TemplateNotFoundException(template);
//		}
//
//		try {
//			XLSTransformer transformer = new XLSTransformer();
//			Workbook workbook = transformer.transformXLS(in, parameters);
//			workbook.write(out);
//		} catch (InvalidFormatException | IOException e) {
//			logger.error("Failed to apply excel template {}.", template);
//		}
//	}

//    public void applyXHTMLTemplate(String template, Map<String, Object> parameters, OutputStream out) throws DocumentException, TemplateNotFoundException, IOException {
//        checkNotNull(template);
//        checkNotNull(parameters);
//        checkNotNull(out);
//
//        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
//        applyTemplate(template, parameters, tempOut);
//
//        ByteArrayInputStream in = new ByteArrayInputStream(tempOut.toByteArray());
//
//        Document document = XMLResource.load(in).getDocument();
//
//        ITextRenderer renderer = new ITextRenderer();
//        ITextFontResolver fontResolver = renderer.getFontResolver();
//
//        URL url = getClass().getClassLoader().getResource(prefix);
//
//        fontResolver.addFont(prefix+"fonts/century.ttf", "century", "latin2", true, null);
//        fontResolver.addFont(prefix+"fonts/times.ttf", "cambria", "latin2", true, null);
//
//        renderer.setDocument(document, url.toExternalForm());
//
//        renderer.layout();
//        renderer.createPDF(out);
//    }

    private String constructVelocityTemplate(String name) {
        return prefix + name + suffix;
    }
}
