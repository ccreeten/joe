package org.cc.joe.extract.xml;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.stream.IntStream.range;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static javax.xml.transform.OutputKeys.INDENT;

public final class XmlExtractor {

    private XmlExtractor() {
    }

    public static List<XmlDocument> extract(final ByteBuffer data, final int position) {
        data.position(position);
        return range(0, data.getInt()).mapToObj(idx -> {
            final var nameSize = data.getInt() + 1;
            final var name = ascii(data, nameSize).trim();
            final var tagSize = data.getInt() + 1;
            final var tag = ascii(data, tagSize).trim();
            final var xmlSize = data.getInt();

            return new XmlDocument(
                    format("%d_%s_%s", idx, name, tag),
                    formatXml(ascii(data, xmlSize).replaceAll("\t", " ")).replaceAll("\n *?\n", "\n")
            );
        }).toList();
    }

    private static String ascii(final ByteBuffer buffer, final int size) {
        final var data = new byte[size];
        buffer.get(data);
        return new String(data, US_ASCII);
    }

    private static String formatXml(final String input) {
        try {
            final var xmlInput = new StreamSource(new StringReader(input));
            final var stringWriter = new StringWriter();
            final var xmlOutput = new StreamResult(stringWriter);

            final var transformer = transformerFactory().newTransformer();
            transformer.setOutputProperty(INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);

            return xmlOutput.getWriter().toString();
        } catch (final TransformerException e) {
            throw new AssertionError(e);
        }
    }

    private static TransformerFactory transformerFactory() {
        final var transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
        transformerFactory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");
        return transformerFactory;
    }
}
