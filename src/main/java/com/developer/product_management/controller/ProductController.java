package com.developer.product_management.controller;

import com.developer.product_management.entity.Product;
import com.developer.product_management.exception.ResourceNotFoundException;
import com.developer.product_management.response.ResponseHandler;
import com.developer.product_management.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private HttpServletResponse response;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable("id") Long productId) {
        Product product = productService.getProductById(productId);

        // Jangan memformat ukuran file di dalam Product, simpan sebagai Long
        String formattedFileSize = productService.formatFileSize(product.getFileSize());

        // Tambahkan data yang diformat ke response, tanpa mengubah field asli
        Map<String, Object> productResponse = new HashMap<>();
        productResponse.put("fileName", product.getFileName());
        productResponse.put("originalFileName", product.getOriginalFileName());
        productResponse.put("fileSize", formattedFileSize);  // Format size di sini
        productResponse.put("contentType", product.getContentType());
        productResponse.put("stock", product.getStock());

        return ResponseHandler.responseBuilder("Success fetch data", HttpStatus.OK, productResponse);
    }


    @GetMapping("/view/all")
    public ResponseEntity<Object> getAllProducts() {
        List<Product> products = productService.getAllProducts();

        // Bungkus List<Product> dalam Map
        Map<String, Object> response = new HashMap<>();
        response.put("products", products);  // Simpan List<Product> dalam key "products"

        return ResponseHandler.responseBuilder("Success fetch All data", HttpStatus.OK, response);
    }


    @PostMapping("/add")
    public ResponseEntity<Object> createProduct(@ModelAttribute Product product,
                                                @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // Panggil service untuk membuat produk baru
            Product createdProduct = productService.createProduct(product, file);

            // Menggunakan ResponseHandler untuk memberikan pesan berhasil
            return ResponseHandler.messageResponse("Success Add data", HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseHandler.messageResponse("Error processing request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable("id") Long productId,
                                                @ModelAttribute Product product,
                                                @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // Pastikan ID diperbarui dengan ID dari path variable
            product.setId(productId);

            // Panggil service untuk memperbarui produk
            Product updatedProduct = productService.updateProduct(product, file);

            // Menggunakan ResponseHandler untuk memberikan pesan berhasil
            return ResponseHandler.messageResponse("Success Update data", HttpStatus.OK);
        } catch (IOException e) {
            return ResponseHandler.messageResponse("Error processing request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseHandler.messageResponse("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable("id") Long Id) {
        productService.deleteProduct(Id);
        return ResponseHandler.messageResponse("Product Deleted Successfully  ",
                HttpStatus.OK);
    }

    @GetMapping("/file/{fileName}")
    public ResponseEntity<byte[]> viewImageByFileName(@PathVariable String fileName) {
        try {
            // Logging nama file untuk debugging
            System.out.println("Mencoba mengakses file dengan nama: " + fileName);

            // Ambil data file dan tipe konten dari service
            byte[] fileData = productService.loadFileAsBytesByFileName(fileName);
            String contentType = productService.getContentTypeByFileName(fileName);

            // Pastikan tipe konten diparse dengan benar
            MediaType mediaType = MediaType.parseMediaType(contentType);

            // Siapkan header dengan tipe konten yang sesuai
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(fileData.length);

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            // Log exception untuk debugging
            System.err.println("File tidak ditemukan: " + fileName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Log exception untuk debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    @GetMapping("/report")
    public void generateProductListReport(
            @RequestParam(value="format" ,defaultValue = "pdf") String format,
            HttpServletResponse response) throws IOException {
        try {
            // Generate the JasperPrint object
            JasperPrint jasperPrint = productService.generateJasperPrint();

            OutputStream outStream = response.getOutputStream();

            // Mengatur response berdasarkan format yang diminta
            if (format.equalsIgnoreCase("pdf")) {
                // Set response headers untuk output PDF
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=product_list.pdf");
                JasperExportManager.exportReportToPdfStream(jasperPrint, outStream);
            } else if (format.equalsIgnoreCase("excel")) {
                // Set response headers untuk output Excel
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=product_list.xlsx");

                // Export report ke Excel
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));

//                // Set konfigurasi untuk export Excel (opsional)
//                SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
//                configuration.setOnePagePerSheet(false);
//                configuration.setDetectCellType(true);
//                exporter.setConfiguration(configuration);

                exporter.exportReport();
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format specified. Use 'pdf' or 'excel'.");
                return;
            }

            // Flush dan close output stream
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report.");
        }
    }




//    @GetMapping("/report")
//    public ResponseEntity<byte[]> getProductReport() {
//        try {
//            byte[] reportBytes = productService.generateProductReport();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product_report.pdf");
//            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
//            headers.setContentLength(reportBytes.length);
//
//            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

}
