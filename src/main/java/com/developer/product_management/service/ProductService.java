package com.developer.product_management.service;

import com.developer.product_management.entity.Product;
import lombok.SneakyThrows;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface ProductService {

    Product createProduct(Product product, MultipartFile file )throws IOException ;

    Product getProductById(Long Id);

    List<Product> getAllProducts();

    Product updateProduct( Product updatedProduct, MultipartFile file) throws IOException;

    void deleteProduct(Long Id);

//    Resource load(String fileName);

    byte[] loadFileAsBytesByFileName(String fileName);

    String getContentTypeByFileName(String fileName);

    String formatFileSize(Long sizeInBytes);

//    byte[] generateProductReport() throws JRException;

    JasperPrint generateJasperPrint() throws Exception;
}
