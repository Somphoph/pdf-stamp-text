package org.basic.pdf;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

public class PdfTextWriterTool {
    private Logger logger = LoggerFactory.getLogger(PdfTextWriterTool.class);
    private Options options;

    public PdfTextWriterTool(){
        options = new Options();
        options.setPdFont(PDType1Font.COURIER).setFontSize(14);
    }
    public PdfTextWriterTool(PDFont pdFont,int fontSize){
        options = new Options();
        options.setPdFont(pdFont).setFontSize(fontSize);
    }

    public void write(File pdfFile, Object valueObject, Map<String,OffSetPoint> mapPropAndOffset,File output) throws IOException {
        if(!pdfFile.exists()){
            throw new FileNotFoundException("ไม่พบไฟล์ :"+pdfFile.getPath());
        }

        PDDocument doc = PDDocument.load(pdfFile);

        try {
            Set<String> keys = mapPropAndOffset.keySet();
            for (String key : keys) {
                Object val = "";
                try {
                    val = BeanUtils.getProperty(valueObject, key);
                } catch (IllegalAccessException e) {
                    logger.error("Get value from bean error : " + e);
                } catch (InvocationTargetException e) {
                    logger.error("Get value from bean error : " + e);
                } catch (NoSuchMethodException e) {
                    logger.error("Get value from bean error : " + e);
                }

                OffSetPoint offSetPoint = mapPropAndOffset.get(key);
                if (offSetPoint != null) {
                    write(doc, offSetPoint.getPageIndex(), val.toString(), offSetPoint.getX(), offSetPoint.getY());
                } else {
                    logger.error("Can't get offset point value in key :[" + key + "]");
                }
            }
            doc.save(output);
        }catch (IOException e){
            throw e;
        }finally {
            doc.close();
        }
    }

    public void write(PDDocument document,int pageIndex, String text, float offsetX, float offsetY) throws IOException {
        PDPageContentStream contentStream = createPageContentStream(document,pageIndex);
        contentStream.beginText();
        contentStream.newLineAtOffset(offsetX,offsetY);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();
    }

    /**
     * Create PDPageContentStream Object with default value to use in writer tool class.
     * @param document
     * @param pageIndex
     * @return PDPageContentStream
     * @throws IOException
     */
    protected PDPageContentStream createPageContentStream(PDDocument document,int pageIndex) throws IOException {
        PDPage page = document.getPage(pageIndex);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,true,true);

        contentStream.setFont(options.getPdFont(), options.getFontSize());

        if(options.getLeading()!=null){
            contentStream.setLeading(options.getLeading());
        }

        return contentStream;
    }

    public Options getOptions(){
        return this.options;
    }

    public class Options{
        private Float leading;
        private PDFont pdFont;
        private int fontSize;

        public Float getLeading() {
            return leading;
        }

        public Options setLeading(float leading) {
            this.leading = leading;
            return this;
        }

        public PDFont getPdFont() {
            return pdFont;
        }

        public Options setPdFont(PDFont pdFont) {
            this.pdFont = pdFont;
            return this;
        }

        public int getFontSize() {
            return fontSize;
        }

        public Options setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }
    }
}
