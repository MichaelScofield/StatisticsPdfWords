package me.lfc;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.IOException;

/**
 * User: LuoFucong
 * Date: 12-12-31
 */
public class PDFTextReader {

    private PDFTextStripper textStripper;

    public PDFTextReader() throws IOException {
        textStripper = new PDFTextStripper("UTF-8");
    }

    public String read(String filePath, int startPage, int endPage) throws OverPageException, IOException {
        if (startPage > endPage) {
            System.out.println("Warning: startPage(=" + startPage + ") > endPage(=" + endPage + ")");
            return null;
        }
        textStripper.setStartPage(startPage);
        textStripper.setEndPage(endPage);

        String text = null;
        PDDocument document = null;
        try {
            document = PDDocument.load(filePath, true);
            if (textStripper.getStartPage() > document.getNumberOfPages()) {
                throw new OverPageException();
            }
            text = textStripper.getText(document);
        } finally {
            if (document != null) {
                document.close();
            }
        }
        return text;
    }
}
