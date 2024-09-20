package com.developer.product_management.service.impl;

import com.developer.product_management.entity.Product;
import com.developer.product_management.entity.Stock;
import com.developer.product_management.exception.ResourceNotFoundException;
import com.developer.product_management.repository.ProductRepository;
import com.developer.product_management.repository.StockRepository;
import com.developer.product_management.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@AllArgsConstructor
@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    private ProductRepository productRepository;
    private StockRepository stockRepository;
    private static final Path root = Paths.get("upload");


    @Override
    public Product getProductById(Long Id) {
        Product product = productRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not exist with given id : " + Id));
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @SneakyThrows
    @Override
    public Product createProduct(Product product, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            // Validasi ukuran file
            if (file.getSize() > (1024 * 1024)) { // Ukuran file dalam bytes
                throw new RuntimeException("File size exceeds maximum limit");
            }

            // Menghasilkan nama file yang unik
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // Set metadata file pada produk
            product.setFileData(file.getBytes());
            product.setFileName(uniqueFileName); // Gunakan nama file yang unik
            product.setOriginalFileName(originalFileName);
            product.setFileSize(file.getSize());
            product.setContentType(file.getContentType());
        }

        // Simpan produk ke database
        return productRepository.save(product);
    }


    @SneakyThrows
    @Override
    public Product updateProduct(Product updatedProduct, MultipartFile file) throws IOException {
        // Mencari produk berdasarkan ID
        Product productFound = productRepository.findById(updatedProduct.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product does not exist with given id: " + updatedProduct.getId()));

        // Memperbarui informasi produk
        productFound.setNameProduct(updatedProduct.getNameProduct());
        productFound.setCategory(updatedProduct.getCategory());

        // Memproses file jika ada
        if (file != null && !file.isEmpty()) {
            // Validasi ukuran file
            if (file.getSize() > (1024 * 1024)) { // Ukuran file dibatasi maksimal 1MB
                throw new IOException("File size exceeds maximum limit");
            }

            // Konversi MultipartFile ke byte[]
            byte[] fileBytes = file.getBytes();

            // Set metadata file baru pada produk
            productFound.setFileData(fileBytes);
            productFound.setFileName(file.getName());
            productFound.setOriginalFileName(file.getOriginalFilename());
            productFound.setFileSize(file.getSize());
            productFound.setContentType(file.getContentType());
        }

        // Memperbarui informasi stok jika ada
        if (updatedProduct.getStock() != null) {
            Stock stock = updatedProduct.getStock();
            if (stock.getId() == null) {
                // Jika stok baru, simpan stok terlebih dahulu
                stock = stockRepository.save(stock);
            } else {
                // Jika stok sudah ada, perbarui stok
                Stock finalStock = stock;
                Stock existingStock = stockRepository.findById(stock.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Stock does not exist with given id: " + finalStock.getId()));
                existingStock.setStockProduct(stock.getStockProduct());
                existingStock.setTotalAvailable(stock.getTotalAvailable());
                existingStock.setTotalDefect(stock.getTotalDefect());
                stock = stockRepository.save(existingStock);
            }
            productFound.setStock(stock);
        } else {
            // Jika stok dihapus, hapus stok dari produk
            if (productFound.getStock() != null) {
                // Hapus stok dari produk tetapi tidak menghapus data stok dari basis data
                productFound.setStock(null);
            }
        }

        // Menyimpan data yang telah diperbarui ke repository
        return productRepository.save(productFound);
    }

    @Override
    public void deleteProduct(Long Id) {
        Product product = productRepository.findById(Id).orElseThrow(
                () -> new ResourceNotFoundException("Product is not exist whit given id : " + Id)
        );
        productRepository.deleteById(Id);
    }

    @Override
    public byte[] loadFileAsBytesByFileName(String fileName) {
        return productRepository.findByFileName(fileName)
                .map(Product::getFileData)
                .orElseThrow(() -> new ResourceNotFoundException("File tidak ditemukan untuk file: " + fileName));
    }

    @Override
    public String getContentTypeByFileName(String fileName) {
        return productRepository.findByFileName(fileName)
                .map(Product::getContentType)
                .orElseThrow(() -> new ResourceNotFoundException("Content type tidak ditemukan untuk file: " + fileName));
    }


    @Override
    public String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null) return "0 B";

        double size = (double) sizeInBytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};

        int unitIndex = 0;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

//    @SneakyThrows
//    @Override
//    public byte[] generateProductReport() {
//        // Pastikan file di-load dari classpath
//        InputStream jasperStream = new ClassPathResource("reports/product_report.jasper").getInputStream();
//        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
//
//        // Parameter dan data untuk report
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("title", "Product Report");
//
//        // Asumsikan ada method untuk mendapatkan data source
//        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(getProductData());
//
//        // Fill report
//        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
//
//        // Output dalam bentuk PDF
//        return JasperExportManager.exportReportToPdf(jasperPrint);
//    }

    @Autowired
    private Collection<?> getProductData() {
        // Mengambil semua data produk dari database
        List<Product> products = productRepository.findAll();

        // Pastikan data produk tidak kosong
        if (products.isEmpty()) {
            throw new RuntimeException("No product data available.");
        }

        // Mengembalikan list produk untuk digunakan sebagai datasource di laporan
        return products;
    }

    @Autowired
    private DataSource dataSource;

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JasperPrint generateJasperPrint() throws Exception {
        // Load the .jrxml file and compile it to .jasper
        InputStream fileReport = new ClassPathResource("/reports/Product_List.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(fileReport);

        // Fill the report with data from the database
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, getConnection());
        return jasperPrint;
    }


}