// Copyright 2013 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.jaxws.extensions.exporter;

import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.ReportWriter;
import com.google.api.ads.adwords.jaxws.extensions.util.MediaReplacedElementFactory;

import com.lowagie.text.DocumentException;
import com.samskivert.mustache.Mustache;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

/**
 * Class to export reports to HTML using JMoustache, and convert HTML to PDF using Flying Saucer
 *
 * @author markbowyer@google.com (Mark R. Bowyer)
 * @author joeltoby@google.com (Joel Toby)
 * @author jtoledo@google.com (Julian Toledo)
 */
public class HTMLExporter {

  public HTMLExporter() {}

  /**
   * Exports an HTML file of the given report
   *
   * @param map the data from the Report
   * @param templateFile where to read out the HTML template
   * @param writer the {@link Writer} to which HTML should be written.  Usually an {@link ReportWriter}
   * @throws IOException error writing HTML file
   * @throws FileNotFoundException error reading template file
   */
  public static void exportHtml(final Map<String, Object> map,
      File templateFile, Writer writer) throws IOException {

    FileReader templateReader = new FileReader(templateFile);
    Mustache.compiler().compile(templateReader).execute(map, writer);

    writer.flush();
    writer.close();
    templateReader.close();
  }

  /**
   * Convert a given HTML file to a PDF file
   *
   * @param file The HTML file
   * @param reportWriter the ReportWriter to which HTML should be written
   * @throws DocumentException error creating PDF file
   * @throws IOException error closing file
   */
  public static void exportHtmlToPdf(File file, ReportWriter reportWriter)
      throws DocumentException, IOException {

    FileReader fileReader = new FileReader(file);
    Document document = XMLResource.load(fileReader).getDocument();
    exportHtmlToPdf(document, reportWriter);
    fileReader.close();
  }

  /**
   * Convert a given HTML source to a PDF file
   *
   * @param inputStream The HTML source
   * @param reportWriter the ReportWriter to which HTML should be written
   * @throws DocumentException error creating PDF file
   * @throws IOException error closing file
   */
  public static void exportHtmlToPdf(InputStream inputStream, ReportWriter reportWriter)
      throws DocumentException, IOException {

    Document document = XMLResource.load(inputStream).getDocument();
    exportHtmlToPdf(document, reportWriter);
    inputStream.close();
  }

  /**
   * Convert a given HTML report {@link InputStream} to a PDF file.<br>
   * 
   * This method does not close the report InputStream. It is the responsibility 
   * of the caller to do so.
   *
   * @param document The HTML Document
   * @param reportWriter the ReportWriter to which HTML should be written
   * @throws DocumentException error creating PDF file
   * @throws IOException error closing file
   */
  public static void exportHtmlToPdf(Document document, ReportWriter reportWriter)
      throws DocumentException, IOException {

    ITextRenderer renderer = new ITextRenderer();
    renderer.getSharedContext().setReplacedElementFactory(
        new MediaReplacedElementFactory(renderer.getSharedContext().getReplacedElementFactory()));
    renderer.setDocument(document, null);
    renderer.layout();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    renderer.createPDF(outputStream, true);
    ByteArrayInputStream is = new ByteArrayInputStream(outputStream.toByteArray());
    reportWriter.write(is);

    outputStream.flush();
    outputStream.close();
  }
}
