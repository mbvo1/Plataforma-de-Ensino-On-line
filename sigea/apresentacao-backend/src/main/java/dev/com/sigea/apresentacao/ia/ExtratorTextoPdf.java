package dev.com.sigea.apresentacao.ia;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ExtratorTextoPdf {

    public static String extrairTexto(byte[] bytesArquivo) throws Exception {
        try (PDDocument documento = Loader.loadPDF(bytesArquivo)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(documento);
        }
    }
}
